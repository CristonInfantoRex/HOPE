import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * ProgressReporter runs on a dedicated daemon thread and periodically prints
 * a live progress summary to the console.
 *
 * <p>Output format (every {@link #REPORT_INTERVAL_MS} milliseconds):
 * <pre>
 * ──────────────────────────────────────────────
 * Overall: 3 active | 1 done | 0 failed
 * ──────────────────────────────────────────────
 *   file1.zip     [████████░░░░░░░░░░░░]  40.0%   2.34 MB/s  ETA 12s
 *   file2.iso     [████████████████░░░░]  80.5%   5.10 MB/s  ETA  4s
 *   report.pdf    [████████████████████] 100.0%   done
 * ──────────────────────────────────────────────
 * </pre>
 */
public class ProgressReporter implements Runnable {

    // ------------------------------------------------------------------ config

    /** How often the progress table is refreshed (ms). */
    private static final long REPORT_INTERVAL_MS = 1_000;

    /** Width of the ASCII progress bar (in characters). */
    private static final int BAR_WIDTH = 20;

    // ------------------------------------------------------------------ state

    private final List<DownloadProgress> progressList;
    private volatile boolean running = true;

    // ------------------------------------------------------------------ ctor

    /**
     * @param progressList Live list of progress objects, one per file.
     *                     The reporter accesses this list read-only.
     */
    public ProgressReporter(List<DownloadProgress> progressList) {
        this.progressList = progressList;
    }

    // ------------------------------------------------------------------ lifecycle

    /** Stops the reporter after the current sleep interval completes. */
    public void stop() {
        this.running = false;
    }

    // ------------------------------------------------------------------ Runnable

    @Override
    public void run() {
        while (running) {
            try {
                Thread.sleep(REPORT_INTERVAL_MS);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                break;
            }
            printReport();
        }
        // Print one final report once all downloads have finished.
        printReport();
    }

    // ------------------------------------------------------------------ report building

    private void printReport() {
        int active  = 0;
        int done    = 0;
        int failed  = 0;
        long totalBytes     = 0;
        long downloadedTotal = 0;

        for (DownloadProgress p : progressList) {
            switch (p.getStatus()) {
                case DOWNLOADING -> active++;
                case COMPLETED   -> done++;
                case FAILED      -> failed++;
                default          -> {}
            }
            if (p.getTotalBytes() > 0)        totalBytes     += p.getTotalBytes();
            downloadedTotal += p.getDownloadedBytes();
        }

        String divider = "─".repeat(64);
        StringBuilder sb = new StringBuilder();
        sb.append("\n").append(divider).append("\n");

        // ---- overall line -------------------------------------------------------
        double overallPct = (totalBytes > 0)
                ? Math.min(100.0, downloadedTotal * 100.0 / totalBytes)
                : -1;

        sb.append(String.format("  Overall: %d downloading | %d done | %d failed",
                active, done, failed));
        if (overallPct >= 0) {
            sb.append(String.format("  [%s Total: %s / %s]",
                    buildBar(overallPct),
                    DownloadProgress.formatBytes(downloadedTotal),
                    DownloadProgress.formatBytes(totalBytes)));
        }
        sb.append("\n").append(divider).append("\n");

        // ---- per-file lines -----------------------------------------------------
        for (DownloadProgress p : progressList) {
            sb.append(formatFileLine(p)).append("\n");
        }

        sb.append(divider);
        System.out.println(sb);
    }

    /** Builds a single status line for one file, e.g.:
     *  "  ubuntu.iso   [████████░░░░░░░░░░░░]  40.0%   2.34 MB/s  ETA 45s" */
    private String formatFileLine(DownloadProgress p) {
        String name = truncate(p.getFileName(), 20);
        DownloadProgress.Status status = p.getStatus();

        if (status == DownloadProgress.Status.PENDING) {
            return String.format("  %-20s  [pending]", name);
        }

        if (status == DownloadProgress.Status.FAILED) {
            return String.format("  %-20s  ✗ FAILED — %s", name,
                    truncate(p.getErrorMessage(), 30));
        }

        double pct = p.getPercentage();
        String bar = (pct >= 0) ? buildBar(pct) : "??????????";

        if (status == DownloadProgress.Status.COMPLETED) {
            return String.format("  %-20s  [%s] 100.0%%  ✓ done (%s in %s)",
                    name, bar,
                    DownloadProgress.formatBytes(p.getDownloadedBytes()),
                    formatDuration(p.getElapsedMs()));
        }

        // DOWNLOADING
        String pctStr  = (pct >= 0) ? String.format("%5.1f%%", pct) : "  ??  ";
        String speed   = formatSpeed(p.getSpeedBytesPerSec());
        String eta     = (p.getEtaSeconds() >= 0)
                ? "ETA " + formatDuration(p.getEtaSeconds() * 1_000)
                : "ETA ??";
        String chunks  = String.format("chunks: %d/%d",
                p.getCompletedChunks(), p.getTotalChunks());

        return String.format("  %-20s  [%s] %s  %s  %s  (%s)",
                name, bar, pctStr, speed, eta, chunks);
    }

    // ------------------------------------------------------------------ ASCII bar

    /**
     * Builds a fixed-width filled ASCII progress bar.
     *
     * @param pct  Percentage in range [0, 100].
     * @return     String of length {@link #BAR_WIDTH}, e.g. "████░░░░░░░░".
     */
    private static String buildBar(double pct) {
        int filled = (int) Math.round(pct * BAR_WIDTH / 100.0);
        filled = Math.max(0, Math.min(BAR_WIDTH, filled));
        return "█".repeat(filled) + "░".repeat(BAR_WIDTH - filled);
    }

    // ------------------------------------------------------------------ formatting helpers

    private static String formatSpeed(double bytesPerSec) {
        if (bytesPerSec <= 0) return "  ---  ";
        return String.format("%8s/s", DownloadProgress.formatBytes((long) bytesPerSec));
    }

    private static String formatDuration(long ms) {
        if (ms < 1_000)  return ms + "ms";
        if (ms < 60_000) return String.format("%.1fs", ms / 1000.0);
        return String.format("%dm%ds", ms / 60_000, (ms % 60_000) / 1_000);
    }

    private static String truncate(String s, int max) {
        if (s == null) return "";
        return (s.length() <= max) ? s : s.substring(0, max - 1) + "…";
    }
}
