import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * DownloadProgress is a thread-safe progress tracker for a single file download.
 *
 * <p>Multiple chunk threads update it simultaneously via atomic operations.
 * The owning FileDownloadTask and ProgressReporter may read it at any time
 * without holding a lock.
 */
public class DownloadProgress {

    // ------------------------------------------------------------------ identity

    /** URL of the file being tracked. */
    private final String url;

    /** Just the filename extracted from the URL (used for display). */
    private final String fileName;

    // ------------------------------------------------------------------ size

    /** Total size of the file in bytes (-1 = unknown). */
    private volatile long totalBytes = -1;

    // ------------------------------------------------------------------ counters

    /** Bytes that have been written to disk so far. */
    private final AtomicLong downloadedBytes = new AtomicLong(0);

    /** Number of chunks that completed successfully. */
    private final AtomicInteger completedChunks = new AtomicInteger(0);

    /** Number of chunks that failed permanently (after all retries). */
    private final AtomicInteger failedChunks = new AtomicInteger(0);

    /** Total number of chunks this file was split into. */
    private volatile int totalChunks = 0;

    // ------------------------------------------------------------------ timing

    private final long startTimeMs = System.currentTimeMillis();
    private volatile long endTimeMs = -1;

    // ------------------------------------------------------------------ status

    public enum Status { PENDING, DOWNLOADING, COMPLETED, FAILED }

    private volatile Status status = Status.PENDING;

    /** Human-readable error message set when status == FAILED. */
    private volatile String errorMessage = "";

    // ------------------------------------------------------------------ ctor

    public DownloadProgress(String url) {
        this.url      = url;
        this.fileName = extractFileName(url);
    }

    // ------------------------------------------------------------------ mutators (called by chunk threads)

    /** Atomically adds {@code bytes} to the downloaded-bytes counter. */
    public void addDownloadedBytes(long bytes) {
        downloadedBytes.addAndGet(bytes);
    }

    /** Records that one chunk finished successfully. */
    public void markChunkCompleted() {
        completedChunks.incrementAndGet();
    }

    /** Records that one chunk failed permanently. */
    public void markChunkFailed() {
        failedChunks.incrementAndGet();
    }

    // ------------------------------------------------------------------ lifecycle setters

    public void setTotalBytes(long totalBytes)  { this.totalBytes   = totalBytes; }
    public void setTotalChunks(int totalChunks) { this.totalChunks  = totalChunks; }

    public void setStatus(Status status) {
        this.status = status;
        if (status == Status.COMPLETED || status == Status.FAILED) {
            this.endTimeMs = System.currentTimeMillis();
        }
    }

    public void setErrorMessage(String msg) { this.errorMessage = msg; }

    // ------------------------------------------------------------------ accessors

    public String  getUrl()             { return url;             }
    public String  getFileName()        { return fileName;        }
    public long    getTotalBytes()      { return totalBytes;      }
    public long    getDownloadedBytes() { return downloadedBytes.get(); }
    public int     getCompletedChunks() { return completedChunks.get(); }
    public int     getFailedChunks()    { return failedChunks.get();    }
    public int     getTotalChunks()     { return totalChunks;     }
    public Status  getStatus()          { return status;          }
    public String  getErrorMessage()    { return errorMessage;    }

    /**
     * Download percentage (0.0 – 100.0), or -1 if total size is unknown.
     */
    public double getPercentage() {
        if (totalBytes <= 0) return -1;
        return Math.min(100.0, downloadedBytes.get() * 100.0 / totalBytes);
    }

    /**
     * Current download speed in bytes per second.
     */
    public double getSpeedBytesPerSec() {
        long elapsed = System.currentTimeMillis() - startTimeMs;
        if (elapsed <= 0) return 0;
        return downloadedBytes.get() * 1000.0 / elapsed;
    }

    /**
     * Estimated time remaining in seconds, or -1 if unknown.
     */
    public long getEtaSeconds() {
        if (totalBytes <= 0) return -1;
        double speed = getSpeedBytesPerSec();
        if (speed <= 0) return -1;
        long remaining = totalBytes - downloadedBytes.get();
        return (long) (remaining / speed);
    }

    /**
     * Total wall-clock milliseconds the download took (only valid after completion).
     */
    public long getElapsedMs() {
        long end = (endTimeMs > 0) ? endTimeMs : System.currentTimeMillis();
        return end - startTimeMs;
    }

    // ------------------------------------------------------------------ helpers

    private static String extractFileName(String url) {
        String path = url.split("\\?")[0]; // strip query string
        int slash = path.lastIndexOf('/');
        return (slash >= 0 && slash < path.length() - 1)
                ? path.substring(slash + 1)
                : "download";
    }

    /**
     * Formats a byte count as a human-readable string (e.g. "12.4 MB").
     */
    public static String formatBytes(long bytes) {
        if (bytes < 1024)            return bytes + " B";
        if (bytes < 1024 * 1024)     return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024L * 1024 * 1024) return String.format("%.2f MB", bytes / (1024.0 * 1024));
        return String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024));
    }
}
