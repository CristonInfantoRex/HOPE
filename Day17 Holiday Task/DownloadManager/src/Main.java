import java.util.Arrays;
import java.util.List;

/**
 * Main — entry point for the Java Multi-threaded Download Manager.
 *
 * <p><b>Usage:</b>
 * <pre>
 *   java Main [url1] [url2] ... [urlN]
 *   java Main                          # uses built-in demo URLs
 * </pre>
 *
 * <p><b>Compile:</b>
 * <pre>
 *   javac -d out src\*.java
 * </pre>
 *
 * <p><b>Run (demo):</b>
 * <pre>
 *   java -cp out Main
 * </pre>
 *
 * <p><b>Run (custom URLs):</b>
 * <pre>
 *   java -cp out Main https://example.com/a.zip https://example.com/b.iso
 * </pre>
 *
 * <h2>Configuration constants</h2>
 * <table border="1" cellpadding="4">
 *   <tr><th>Constant</th><th>Default</th><th>Meaning</th></tr>
 *   <tr><td>MAX_CONCURRENT_FILES</td><td>3</td><td>Files downloaded in parallel</td></tr>
 *   <tr><td>MAX_CHUNKS_PER_FILE</td><td>4</td><td>Chunk threads per file</td></tr>
 *   <tr><td>OUTPUT_DIR</td><td>downloads</td><td>Output folder</td></tr>
 * </table>
 */
public class Main {

    // ------------------------------------------------------------------ tunables

    /** Max files downloading at the same time. */
    private static final int    MAX_CONCURRENT_FILES = 3;

    /**
     * Max chunk threads per file.
     * Each chunk thread uses ~1 socket + ~16 KB buffer, so keep this reasonable.
     */
    private static final int    MAX_CHUNKS_PER_FILE  = 4;

    /** Directory where downloaded files are saved. */
    private static final String OUTPUT_DIR           = "downloads";

    // ------------------------------------------------------------------ demo URLs (used when no args supplied)

    /**
     * A small set of publicly available files used to demonstrate the manager.
     *
     * <p>All three are served by the Apache CDN or speed-test servers with
     * Accept-Ranges support.  Feel free to replace them with any URLs you need.
     */
    private static final List<String> DEMO_URLS = List.of(
        // ~9 MB  — Apache Ant 1.10.14 binary zip (Apache CDN)
        "https://dlcdn.apache.org/ant/binaries/apache-ant-1.10.14-bin.zip",

        // ~10 MB — Calibre Linux installer (fast CDN, range-capable)
        "https://download.calibre-ebook.com/6.29.0/calibre-6.29.0-x86_64.txz",

        // ~1 MB  — speed test file from Thinkbroadband (always available)
        "http://ipv4.download.thinkbroadband.com/1MB.zip"
    );

    // ------------------------------------------------------------------ main

    public static void main(String[] args) {

        // ---- Banner ---------------------------------------------------------
        printBanner();

        // ---- Resolve URL list -----------------------------------------------
        List<String> urls;
        if (args.length > 0) {
            urls = Arrays.asList(args);
            System.out.println("Downloading " + urls.size() + " file(s) from command-line arguments.\n");
        } else {
            urls = DEMO_URLS;
            System.out.println("No URLs provided — running with built-in demo URLs.\n");
        }

        // ---- Configure & run DownloadManager --------------------------------
        DownloadManager manager = new DownloadManager(OUTPUT_DIR, MAX_CONCURRENT_FILES, MAX_CHUNKS_PER_FILE);
        manager.addUrls(urls);

        long wallStart = System.currentTimeMillis();
        manager.downloadAll();
        long wallMs    = System.currentTimeMillis() - wallStart;

        // ---- Print final summary --------------------------------------------
        manager.printSummary();
        System.out.printf("%nAll downloads finished in %.1f s%n", wallMs / 1000.0);
    }

    // ------------------------------------------------------------------ helpers

    private static void printBanner() {
        System.out.println("""
                ╔══════════════════════════════════════════════════════════╗
                ║   Java Multi-threaded Download Manager                  ║
                ║   Parallel files  |  Chunked HTTP Range  |  Auto-retry  ║
                ╚══════════════════════════════════════════════════════════╝
                """);
    }
}
