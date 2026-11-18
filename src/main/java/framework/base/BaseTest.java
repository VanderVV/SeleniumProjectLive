package framework.base;

import framework.actions.ElementActions;
import framework.driver.DriverFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;


public abstract class BaseTest {


    protected final ElementActions actions = new ElementActions();

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        DriverFactory.start();
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown() {
        DriverFactory.quit();
    }
}
