package framework.listeners;

import framework.config.Config;
import org.testng.IAlterSuiteListener;
import org.testng.xml.XmlSuite;

import java.util.List;

/**
 * The one place that defines parallel execution. Reads config.properties (or -D overrides) and applies it
 * to every suite, so testng.xml no longer needs parallel/thread-count attributes.
 *
 * <pre>
 *   parallel=methods            none | methods | classes | tests | instances
 *   thread.count=2              browsers running at the same time
 *   data.provider.thread.count=2
 * </pre>
 *
 * Examples: mvn test -Dparallel=classes -Dthread.count=4     mvn test -Dparallel=none
 * If "parallel" is not set anywhere, whatever testng.xml says is left untouched.
 */
public class ParallelConfigurer implements IAlterSuiteListener {

    @Override
    public void alter(List<XmlSuite> suites) {
        String mode = Config.get("parallel");
        if (mode == null || mode.isBlank()) {
            return;
        }
        XmlSuite.ParallelMode parallel = parse(mode.trim());
        int threads = Math.max(1, Config.getInt("thread.count", 2));
        int dataProviderThreads = Math.max(1, Config.getInt("data.provider.thread.count", threads));

        for (XmlSuite suite : suites) {
            suite.setParallel(parallel);
            suite.setThreadCount(threads);
            suite.setDataProviderThreadCount(dataProviderThreads);
        }
        System.out.printf("[Parallel] mode=%s, threads=%d, data-provider-threads=%d%n",
                parallel, threads, dataProviderThreads);
    }

    private static XmlSuite.ParallelMode parse(String value) {
        return switch (value.toLowerCase()) {
            case "none", "false", "off" -> XmlSuite.ParallelMode.NONE;
            case "methods" -> XmlSuite.ParallelMode.METHODS;
            case "classes" -> XmlSuite.ParallelMode.CLASSES;
            case "tests" -> XmlSuite.ParallelMode.TESTS;
            case "instances" -> XmlSuite.ParallelMode.INSTANCES;
            default -> throw new IllegalArgumentException(
                    "Invalid parallel='" + value + "'. Use none, methods, classes, tests or instances");
        };
    }
}
