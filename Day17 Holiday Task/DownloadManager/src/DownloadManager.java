import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * DownloadManager orchestrates the parallel downloading of multiple files.
 *
 * <p>It maintains:
 * <ul>
 *   <li>A <em>file-level</em> {@link ExecutorService} whose thread-count equals
 *       {@code maxConcurrentFiles} — so at most that many files are active at once.</li>
 *   <li>One {@link DownloadProgress} per URL, kept in a list that is also handed
 *       to the {@link ProgressReporter}.</li>
 * </ul>
 *
 * <p>Usage:
 * <pre>{@code
 * DownloadManager mgr = new DownloadManager("downloads", 3, 4);
 * mgr.addUrl("https://example.com/file1.zip");
 * mgr.addUrl("https://example.com/file2.iso");
 * mgr.downloadAll();
 * mgr.printSummary();
 * }</pre>
 */
public class DownloadManager {

    // ------------------------------------------------------------------ config

    /** Directory where all downloaded files will be placed. */
    private final String outputDir;

    /** Maximum number of files downloaded simultaneously. */
    private final int maxConcurrentFiles;

    /**
     * Maximum number of chunk-download threads per individual file.
     * Passed through to each {@link FileDownloadTask}.
     */
    private final int maxChunksPerFile;

    // ------------------------------------------------------------------ state

    private final List<String>           urls         = new ArrayList<>();
    private final List<DownloadProgress> progressList = new ArrayList<>();

    // ------------------------------------------------------------------ ctor

    /**
     * @param outputDir          Destination directory for all downloaded files.
     * @param maxConcurrentFiles Number of files to download in parallel (≥ 1).
     * @param maxChunksPerFile   Max chunk threads per file (≥ 1).
     */
    public DownloadManager(String outputDir, int maxConcurrentFiles, int maxChunksPerFile) {
        if (maxConcurrentFiles < 1) throw new IllegalArgumentException("maxConcurrentFiles must be >= 1");
        if (maxChunksPerFile   < 1) throw new IllegalArgumentException("maxChunksPerFile must be >= 1");

        this.outputDir          = outputDir;
        this.maxConcurrentFiles = maxConcurrentFiles;
        this.maxChunksPerFile   = maxChunksPerFile;
    }

    // ------------------------------------------------------------------ public API

    /**
     * Adds a URL to the download queue.  Must be called before {@link #downloadAll()}.
     */
    public void addUrl(String url) {
        if (url == null || url.isBlank()) throw new IllegalArgumentException("URL must not be blank");
        urls.add(url.trim());
    }

    /**
     * Adds multiple URLs at once.
     */
    public void addUrls(List<String> urlList) {
        urlList.forEach(this::addUrl);
    }

    /**
     * Starts downloading all queued URLs, blocks until every download has
     * either completed or permanently failed, then stops the progress reporter.
     */
    public void downloadAll() {
        if (urls.isEmpty()) {
            System.out.println("[DownloadManager] No URLs to download.");
            return;
        }

        System.out.printf("[DownloadManager] Starting %d file(s)  " +
                "(max %d concurrent, max %d chunks/file, output: '%s')%n",
                urls.size(), maxConcurrentFiles, maxChunksPerFile, outputDir);

        // ---- Build progress trackers ----------------------------------------
        for (String url : urls) {
            progressList.add(new DownloadProgress(url));
        }

        // ---- Start the progress reporter on a daemon thread -----------------
        ProgressReporter reporter  = new ProgressReporter(progressList);
        Thread           repThread = new Thread(reporter, "ProgressReporter");
        repThread.setDaemon(true);
        repThread.start();

        // ---- Submit all file downloads to the file-level thread pool --------
        ExecutorService fileExecutor = Executors.newFixedThreadPool(maxConcurrentFiles);

        for (int i = 0; i < urls.size(); i++) {
            String           url      = urls.get(i);
            DownloadProgress progress = progressList.get(i);

            FileDownloadTask task = new FileDownloadTask(url, outputDir, progress, maxChunksPerFile);
            fileExecutor.submit(task);
        }

        // ---- Wait for all file tasks to finish ------------------------------
        fileExecutor.shutdown();
        try {
            boolean done = fileExecutor.awaitTermination(24, TimeUnit.HOURS);
            if (!done) {
                System.err.println("[DownloadManager] Warning: timed out waiting for downloads.");
                fileExecutor.shutdownNow();
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            fileExecutor.shutdownNow();
            System.err.println("[DownloadManager] Interrupted while waiting for downloads.");
        }

        // ---- Stop progress reporter -----------------------------------------
        reporter.stop();
        try { repThread.join(2_000); } catch (InterruptedException ignored) {}
    }

    /**
     * Prints a final summary table after {@link #downloadAll()} returns.
     */
    public void printSummary() {
        int completed = 0;
        int failed    = 0;

        System.out.println("\n" + "═".repeat(64));
        System.out.println("  DOWNLOAD SUMMARY");
        System.out.println("═".repeat(64));

        for (DownloadProgress p : progressList) {
            String status;
            if (p.getStatus() == DownloadProgress.Status.COMPLETED) {
                status = String.format("✓ OK  (%s in %s)",
                        DownloadProgress.formatBytes(p.getDownloadedBytes()),
                        formatDuration(p.getElapsedMs()));
                completed++;
            } else if (p.getStatus() == DownloadProgress.Status.FAILED) {
                status = "✗ FAILED — " + p.getErrorMessage();
                failed++;
            } else {
                status = "? " + p.getStatus();
            }
            System.out.printf("  %-35s  %s%n", truncate(p.getFileName(), 35), status);
        }

        System.out.println("─".repeat(64));
        System.out.printf("  Total: %d succeeded, %d failed%n", completed, failed);
        System.out.println("═".repeat(64));
    }

    // ------------------------------------------------------------------ accessors

    /** Returns the live list of progress objects (read-only use intended). */
    public List<DownloadProgress> getProgressList() {
        return progressList;
    }

    // ------------------------------------------------------------------ helpers

    private static String formatDuration(long ms) {
        if (ms < 1_000)  return ms + " ms";
        if (ms < 60_000) return String.format("%.1f s", ms / 1000.0);
        return String.format("%d m %d s", ms / 60_000, (ms % 60_000) / 1_000);
    }

    private static String truncate(String s, int max) {
        if (s == null) return "";
        return (s.length() <= max) ? s : s.substring(0, max - 1) + "…";
    }
}
