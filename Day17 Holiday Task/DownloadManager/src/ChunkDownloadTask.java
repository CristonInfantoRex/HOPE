import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Callable;

/**
 * ChunkDownloadTask downloads a specific byte range of a remote file.
 * It uses HTTP Range requests and writes directly to the correct position
 * in a RandomAccessFile (pre-allocated by FileDownloadTask).
 *
 * Retry logic is built-in: failed chunks are retried up to MAX_RETRIES times
 * with an exponential back-off delay.
 */
public class ChunkDownloadTask implements Callable<Boolean> {

    // ------------------------------------------------------------------ config

    /** Maximum number of times a failed chunk will be retried. */
    private static final int MAX_RETRIES = 3;

    /** Base delay (ms) before the first retry — doubles on each attempt. */
    private static final long RETRY_BASE_DELAY_MS = 1_000;

    /** Socket read-timeout in milliseconds. */
    private static final int READ_TIMEOUT_MS = 30_000;

    /** Socket connect-timeout in milliseconds. */
    private static final int CONNECT_TIMEOUT_MS = 10_000;

    /** Number of bytes read from the stream in each iteration. */
    private static final int BUFFER_SIZE = 16 * 1024; // 16 KB

    // ------------------------------------------------------------------ state

    private final String fileUrl;
    private final long startByte;
    private final long endByte;
    private final int chunkIndex;
    private final RandomAccessFile outputFile;
    private final DownloadProgress progress;

    // ------------------------------------------------------------------ ctor

    /**
     * @param fileUrl     Remote URL of the file.
     * @param startByte   First byte index of this chunk (inclusive).
     * @param endByte     Last byte index of this chunk (inclusive).
     * @param chunkIndex  Zero-based index used for logging.
     * @param outputFile  Shared, pre-allocated output file (thread-safe writes
     *                    are achieved by using seek + write under a lock provided
     *                    by {@link DownloadProgress}).
     * @param progress    Progress tracker shared with the owning FileDownloadTask.
     */
    public ChunkDownloadTask(String fileUrl,
                             long startByte,
                             long endByte,
                             int chunkIndex,
                             RandomAccessFile outputFile,
                             DownloadProgress progress) {
        this.fileUrl     = fileUrl;
        this.startByte   = startByte;
        this.endByte     = endByte;
        this.chunkIndex  = chunkIndex;
        this.outputFile  = outputFile;
        this.progress    = progress;
    }

    // ------------------------------------------------------------------ Callable

    /**
     * Attempts to download the chunk, retrying on failure.
     *
     * @return {@code true} if the chunk was downloaded successfully,
     *         {@code false} if all retry attempts were exhausted.
     */
    @Override
    public Boolean call() {
        int attempt = 0;
        long delay  = RETRY_BASE_DELAY_MS;

        while (attempt <= MAX_RETRIES) {
            if (attempt > 0) {
                System.out.printf("  [Chunk %d] Retry %d/%d for bytes %d-%d%n",
                        chunkIndex, attempt, MAX_RETRIES, startByte, endByte);
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return false;
                }
                delay *= 2; // exponential back-off
            }

            try {
                downloadChunk();
                return true; // success
            } catch (Exception ex) {
                System.err.printf("  [Chunk %d] Attempt %d failed: %s%n",
                        chunkIndex, attempt + 1, ex.getMessage());
            }

            attempt++;
        }

        System.err.printf("  [Chunk %d] FAILED after %d attempts (bytes %d-%d)%n",
                chunkIndex, MAX_RETRIES + 1, startByte, endByte);
        progress.markChunkFailed();
        return false;
    }

    // ------------------------------------------------------------------ internals

    /**
     * Opens an HTTP connection, sets the Range header, and streams the response
     * body into the output file at the correct offset.
     */
    private void downloadChunk() throws Exception {
        HttpURLConnection conn = openConnection();

        int responseCode = conn.getResponseCode();
        // 206 Partial Content  — server honoured the Range header
        // 200 OK               — server ignores Range and sends full file
        //                        (caller should have verified Accept-Ranges first)
        if (responseCode != HttpURLConnection.HTTP_PARTIAL
                && responseCode != HttpURLConnection.HTTP_OK) {
            throw new RuntimeException("Unexpected HTTP response: " + responseCode);
        }

        byte[] buffer  = new byte[BUFFER_SIZE];
        long   filePos = startByte;

        try (InputStream in = conn.getInputStream()) {
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                // Synchronize writes so concurrent chunks don't interleave.
                synchronized (outputFile) {
                    outputFile.seek(filePos);
                    outputFile.write(buffer, 0, bytesRead);
                }
                filePos += bytesRead;
                progress.addDownloadedBytes(bytesRead);

                // Honour cancellation / interrupt signals.
                if (Thread.currentThread().isInterrupted()) {
                    throw new InterruptedException("Chunk download interrupted");
                }
            }
        } finally {
            conn.disconnect();
        }
    }

    /**
     * Creates and configures the HTTP connection with the Range header.
     */
    private HttpURLConnection openConnection() throws Exception {
        URL url = new URL(fileUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(CONNECT_TIMEOUT_MS);
        conn.setReadTimeout(READ_TIMEOUT_MS);
        conn.setRequestMethod("GET");
        // Request only the bytes that belong to this chunk.
        conn.setRequestProperty("Range", "bytes=" + startByte + "-" + endByte);
        conn.setRequestProperty("User-Agent", "JavaDownloadManager/1.0");
        return conn;
    }
}
