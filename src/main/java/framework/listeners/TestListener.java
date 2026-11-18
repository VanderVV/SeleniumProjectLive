package framework.listeners;

import framework.config.Config;
import framework.config.Secrets;
import framework.driver.DriverFactory;
import framework.reporting.ExtentManager;
import framework.reporting.ScreenshotUtil;
import framework.reporting.StepLogger;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.util.Arrays;

/** Drives the Extent report lifecycle and puts a failure screenshot in both Extent and Allure. */
public class TestListener implements ITestListener {

    @Override
    public void onTestStart(ITestResult result) {
        ExtentManager.startTest(result.getMethod().getMethodName(), result.getMethod().getDescription());
        if (result.getParameters().length > 0) {
            StepLogger.info("Data: " + Arrays.toString(result.getParameters()));
        }
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        if (Config.getBoolean("screenshot.on.pass", false) && DriverFactory.isStarted()) {
            ScreenshotUtil.take("Final state");
        }
        ExtentManager.getTest().pass("Passed");
        ExtentManager.endTest();
    }

    @Override
    public void onTestFailure(ITestResult result) {
        java.io.StringWriter trace = new java.io.StringWriter();
        result.getThrowable().printStackTrace(new java.io.PrintWriter(trace));
        ExtentManager.getTest().fail("<pre>" + Secrets.mask(trace.toString()) + "</pre>");
        if (DriverFactory.isStarted()) {
            ScreenshotUtil.take("Failure screenshot");
        }
        ExtentManager.endTest();
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        if (ExtentManager.getTest() != null) {
            ExtentManager.getTest().skip(result.getThrowable() == null ? "Skipped" : result.getThrowable().toString());
        }
        ExtentManager.endTest();
    }

    @Override
    public void onFinish(ITestContext context) {
        ExtentManager.flush();
    }
}
