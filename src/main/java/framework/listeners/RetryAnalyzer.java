package framework.listeners;

import framework.config.Config;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

/** Re-runs a failed test up to retry.count times (config.properties or -Dretry.count=n). */
public class RetryAnalyzer implements IRetryAnalyzer {

    private final int maxRetries = Config.getInt("retry.count", 0);
    private int attempt = 0;

    @Override
    public boolean retry(ITestResult result) {
        return attempt++ < maxRetries;
    }
}
