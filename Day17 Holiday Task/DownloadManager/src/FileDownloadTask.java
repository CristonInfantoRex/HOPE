import java.io.File;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * FileDownloadTask is responsible for downloading a single remote file.
 *
 * <p>Workflow:
 * <ol>
 *   <li>HEAD request → get content-length + check Accept-Ranges support.</li>
 *   <li>If range requests are supported, split the file into {@code maxChunks} chunks.</li>
 *   <li>Submit a {@link ChunkDownloadTask} for each chunk to an internal thread pool.</li>
 *   <li>Wait for all chunks, verify that none failed, then close the file.</li>
 *   <li>Fall back to single-threaded download if the server does not support ranges.</li>
 * </ol>
 *
 * <p>Implements {@link Runnable} so it can be submitted to the file-level
 * {@link ExecutorService} inside {@link DownloadManager}.
 */
public class FileDownloadTask implements Runnable {

    // ------------------------------------------------------------------ config

    /** Maximum number of parallel chunk threads per file. */
    private final int maxChunks;

    /** Minimum chunk size in bytes — very small files won't be split. */
    private static final long MIN_CHUNK_SIZE = 512 * 1024; // 512 KB

    /** HTTP connection timeout, ms. */
    private static final int CONNECT_TIMEOUT_MS = 10_000;

    /** HTTP read timeout, ms. */
    private static final int READ_TIMEOUT_MS    = 30_000;

    // ------------------------------------------------------------------ state

    private final String         url;
    private final String         outputDir;
    private final DownloadProgress progress;

    // ------------------------------------------------------------------ ctor

    /**
     * @param url        Remote URL to download.
     * @param outputDir  Directory where the file will be saved.
     * @param progress   Pre-created progress tracker (also passed to chunk tasks).
     * @param maxChunks  Maximum number of concurrent chunk-download threads.
     */
    public FileDownloadTask(String url,
                            String outputDir,
                            DownloadProgress progress,
                            int maxChunks) {
        this.url       = url;
        this.outputDir = outputDir;
        this.progress  = progress;
        this.maxChunks = maxChunks;
    }

    // ------------------------------------------------------------------ Runnable

    @Override
    public void run() {
        progress.setStatus(DownloadProgress.Status.DOWNLOADING);
        System.out.printf("[%s] Starting download...%n", progress.getFileName());

        try {
            // ---- Step 1: probe the server ----------------------------------------
            ServerInfo info = probeServer(url);
            progress.setTotalBytes(info.contentLength);

            // ---- Step 2: create output file --------------------------------------
            File outDir  = new File(outputDir);
            if (!outDir.exists()) outDir.mkdirs();
            File outFile = new File(outDir, progress.getFileName());

            if (info.acceptsRanges && info.contentLength > 0) {
                // ---- Step 3a: multi-threaded chunked download --------------------
                chunkedDownload(info, outFile);
            } else {
                // ---- Step 3b: single-threaded fallback ---------------------------
                System.out.printf("[%s] Server does not support ranges — using single thread.%n",
                        progress.getFileName());
                singleThreadedDownload(outFile);
            }

            // ---- Step 4: verify --------------------------------------------------
            if (progress.getFailedChunks() == 0) {
                progress.setStatus(DownloadProgress.Status.COMPLETED);
                System.out.printf("[%s] ✓ Completed in %s  (%s)%n",
                        progress.getFileName(),
                        formatDuration(progress.getElapsedMs()),
                        DownloadProgress.formatBytes(progress.getDownloadedBytes()));
            } else {
                throw new RuntimeException(progress.getFailedChunks() + " chunk(s) failed permanently.");
            }

        } catch (Exception ex) {
            progress.setErrorMessage(ex.getMessage());
            progress.setStatus(DownloadProgress.Status.FAILED);
            System.err.printf("[%s] ✗ Download failed: %s%n", progress.getFileName(), ex.getMessage());
        }
    }

    // ------------------------------------------------------------------ chunked download

    private void chunkedDownload(ServerInfo info, File outFile) throws Exception {
        int numChunks = computeChunkCount(info.contentLength);
        progress.setTotalChunks(numChunks);

        System.out.printf("[%s] Splitting into %d chunks (file size: %s)%n",
                progress.getFileName(),
                numChunks,
                DownloadProgress.formatBytes(info.contentLength));

        // Pre-allocate file on disk to avoid fragmentation and enable parallel writes.
        try (RandomAccessFile raf = new RandomAccessFile(outFile, "rw")) {
            raf.setLength(info.contentLength);

            ExecutorService chunkExecutor = Executors.newFixedThreadPool(numChunks);
            List<Future<Boolean>> futures = new ArrayList<>();

            long chunkSize = info.contentLength / numChunks;

            for (int i = 0; i < numChunks; i++) {
                long start = i * chunkSize;
                long end   = (i == numChunks - 1) ? info.contentLength - 1 : start + chunkSize - 1;

                ChunkDownloadTask task = new ChunkDownloadTask(url, start, end, i, raf, progress);
                futures.add(chunkExecutor.submit(task));
            }

            // ---- Wait for all chunks ---------------------------------
            chunkExecutor.shutdown();
            boolean finished = chunkExecutor.awaitTermination(2, TimeUnit.HOURS);

            if (!finished) {
                chunkExecutor.shutdownNow();
                throw new RuntimeException("Chunk executor timed out.");
            }

            // ---- Collect results -------------------------------------
            int failed = 0;
            for (Future<Boolean> f : futures) {
                try {
                    if (!f.get()) failed++;
                } catch (Exception ex) {
                    failed++;
                }
            }

            if (failed > 0) {
                throw new RuntimeException(failed + " chunk(s) failed after all retries.");
            }

            // Mark all chunks as completed if no failures were recorded.
            for (int i = progress.getCompletedChunks(); i < numChunks; i++) {
                progress.markChunkCompleted();
            }
        }
    }

    // ------------------------------------------------------------------ single-threaded fallback

    private void singleThreadedDownload(File outFile) throws Exception {
        progress.setTotalChunks(1);

        try (RandomAccessFile raf = new RandomAccessFile(outFile, "rw")) {
            ChunkDownloadTask task = new ChunkDownloadTask(url, 0, Long.MAX_VALUE, 0, raf, progress);
            boolean ok = task.call();
            if (ok) {
                progress.markChunkCompleted();
            } else {
                throw new RuntimeException("Single-threaded download failed.");
            }
        }
    }

    // ------------------------------------------------------------------ server probing

    /**
     * Sends a HEAD request (falling back to GET if HEAD is not allowed) to
     * determine file size and range-request support.
     */
    private ServerInfo probeServer(String fileUrl) throws Exception {
        HttpURLConnection conn = openConnection(fileUrl, "HEAD");
        int code = conn.getResponseCode();

        // Some servers reject HEAD — retry with GET.
        if (code == HttpURLConnection.HTTP_BAD_METHOD || code == 405) {
            conn.disconnect();
            conn = openConnection(fileUrl, "GET");
            code = conn.getResponseCode();
        }

        if (code / 100 != 2) {
            conn.disconnect();
            throw new RuntimeException("Server returned HTTP " + code + " for " + fileUrl);
        }

        long   contentLength = conn.getContentLengthLong();
        String acceptRanges  = conn.getHeaderField("Accept-Ranges");
        boolean supportsRange = "bytes".equalsIgnoreCase(acceptRanges);

        conn.disconnect();

        System.out.printf("[%s] Content-Length: %s | Accept-Ranges: %s%n",
                progress.getFileName(),
                contentLength >= 0 ? DownloadProgress.formatBytes(contentLength) : "unknown",
                supportsRange ? "yes" : "no");

        return new ServerInfo(contentLength, supportsRange);
    }

    private HttpURLConnection openConnection(String fileUrl, String method) throws Exception {
        URL u = new URL(fileUrl);
        HttpURLConnection conn = (HttpURLConnection) u.openConnection();
        conn.setConnectTimeout(CONNECT_TIMEOUT_MS);
        conn.setReadTimeout(READ_TIMEOUT_MS);
        conn.setRequestMethod(method);
        conn.setRequestProperty("User-Agent", "JavaDownloadManager/1.0");
        return conn;
    }

    // ------------------------------------------------------------------ helpers

    /**
     * Decides how many chunks to use, capped at {@code maxChunks} and
     * ensuring each chunk is at least {@link #MIN_CHUNK_SIZE} bytes.
     */
    private int computeChunkCount(long fileSize) {
        if (fileSize <= MIN_CHUNK_SIZE) return 1;
        int bySize = (int) Math.min(maxChunks, fileSize / MIN_CHUNK_SIZE);
        return Math.max(1, bySize);
    }

    private static String formatDuration(long ms) {
        if (ms < 1_000)  return ms + " ms";
        if (ms < 60_000) return String.format("%.1f s", ms / 1000.0);
        return String.format("%d m %d s", ms / 60_000, (ms % 60_000) / 1_000);
    }

    // ------------------------------------------------------------------ inner record

    /** Simple holder for server probe results. */
    private static class ServerInfo {
        final long    contentLength;
        final boolean acceptsRanges;

        ServerInfo(long contentLength, boolean acceptsRanges) {
            this.contentLength = contentLength;
            this.acceptsRanges = acceptsRanges;
        }
    }
}
