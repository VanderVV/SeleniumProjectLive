package framework.reporting;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import framework.config.Config;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** Owns the single ExtentReports instance and the current thread's ExtentTest. */
public final class ExtentManager {

    /**
     * Run folder, e.g. reports/Selenium_Execution_Report_20261118_091500. Maven passes it in as -Dreport.dir so
     * Extent and Allure share one folder; outside Maven (IDE run) a fresh timestamped folder is created.
     */
    public static final Path REPORT_DIR = Paths.get(Config.get("report.dir", "reports/Selenium_Execution_Report_"
            + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))));

    private static final ExtentReports EXTENT = new ExtentReports();
    private static final ThreadLocal<ExtentTest> CURRENT = new ThreadLocal<>();

    static {
        ExtentSparkReporter spark = new ExtentSparkReporter(REPORT_DIR.resolve("testing_report.html").toString());
        spark.config().setDocumentTitle("testing_report");
        spark.config().setReportName("Test Execution Report");
        EXTENT.attachReporter(spark);
        EXTENT.setSystemInfo("Environment", Config.env());
        EXTENT.setSystemInfo("Base URL", describeBaseUrl());
        EXTENT.setSystemInfo("Browser", Config.get("browser", "chrome"));
    }

    private ExtentManager() {
    }

    /** Must never throw: this runs while the class loads, and a bad env should fail the tests, not hang the run. */
    private static String describeBaseUrl() {
        try {
            return Config.baseUrl();
        } catch (IllegalStateException e) {
            return "NOT CONFIGURED (" + e.getMessage() + ")";
        }
    }

    public static ExtentTest startTest(String name, String description) {
        ExtentTest test = EXTENT.createTest(name, description);
        CURRENT.set(test);
        return test;
    }

    /** May be null when code runs outside a test (e.g. a quick main method). */
    public static ExtentTest getTest() {
        return CURRENT.get();
    }

    public static void endTest() {
        CURRENT.remove();
    }

    public static void flush() {
        EXTENT.flush();
    }
}
