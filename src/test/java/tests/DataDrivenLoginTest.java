package tests;

import framework.base.BaseTest;
import framework.config.Config;
import framework.config.Secrets;
import framework.utils.CsvReader;
import io.qameta.allure.Allure;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * One test method, many scenarios: every row of testdata/login_users.csv becomes its own test
 * (own browser, own entry in Allure and Extent). The CSV holds secret KEYS, never real passwords.
 */
@Epic("Framework")
@Feature("Data-driven tests")
public class DataDrivenLoginTest extends BaseTest {

    private static final String SITE = Config.urlOf("saucedemo");

    @DataProvider(name = "loginUsers", parallel = true)
    public Object[][] loginUsers() {
        return CsvReader.read("testdata/login_users.csv", Config.get("csv.rows"));
    }

    @Test(dataProvider = "loginUsers")
    public void loginScenarios(String description, String userKey, String passwordKey,
                               String expected, String expectedText) {
        Allure.getLifecycle().updateTestCase(testCase -> testCase.setName(description));

        actions.open(SITE);
        actions.sendSecret("id=user-name", Secrets.get(userKey));
        actions.sendSecret("id=password", Secrets.get(passwordKey));
        actions.click("id=login-button");

        if (expected.equals("success")) {
            assertEquals(actions.getText("css=.title"), expectedText);
            assertTrue(actions.currentUrl().endsWith("/inventory.html"), "Should land on the inventory page");
        } else {
            assertTrue(actions.getText("css=[data-test='error']").contains(expectedText),
                    "Error message should contain: " + expectedText);
        }
    }
}
