package framework.reporting;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.MediaEntityBuilder;
import framework.config.Secrets;
import io.qameta.allure.Allure;

import java.util.function.Supplier;

/**
 * Single place that writes to BOTH reports: every framework action becomes an Allure step
 * (with automatic pass/fail status) and an Extent log line.
 */
public final class StepLogger {

    private StepLogger() {
    }

    public static <T> T step(String rawName, Supplier<T> action) {
        String name = Secrets.mask(rawName);
        info(name);
        return Allure.step(name, (Allure.ThrowableRunnable<T>) action::get);
    }

    public static void step(String name, Runnable action) {
        step(name, () -> {
            action.run();
            return null;
        });
    }

    public static void info(String message) {
        ExtentTest test = ExtentManager.getTest();
        if (test != null) {
            test.info(Secrets.mask(message));
        }
    }

    public static void attachScreenshot(byte[] png, String title) {
        Allure.addAttachment(title, "image/png", new java.io.ByteArrayInputStream(png), "png");
        ExtentTest test = ExtentManager.getTest();
        if (test != null) {
            String base64 = java.util.Base64.getEncoder().encodeToString(png);
            test.info(title, MediaEntityBuilder.createScreenCaptureFromBase64String(base64, title).build());
        }
    }
}
