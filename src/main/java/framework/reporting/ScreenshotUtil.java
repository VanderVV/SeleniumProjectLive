package framework.reporting;

import framework.driver.DriverFactory;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;

/** Take a screenshot from anywhere (tests, pages, hooks); it lands in both Allure and Extent. */
public final class ScreenshotUtil {

    private ScreenshotUtil() {
    }

    /** Screenshot of the current thread's browser, attached to the Allure step list and the Extent log. */
    public static void take(String name) {
        byte[] png = ((TakesScreenshot) DriverFactory.getDriver()).getScreenshotAs(OutputType.BYTES);
        StepLogger.attachScreenshot(png, name);
    }
}
