package tests;

import framework.base.BaseTest;
import framework.config.Config;
import framework.config.Secrets;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;


@Epic("Framework")
@Feature("Secrets masking")
public class SauceDemoLoginTest extends BaseTest {

    private static final String SITE = Config.urlOf("saucedemo");

    private void loginAs(String userKey) {
        actions.open(SITE);
        actions.sendSecret("id=user-name", Secrets.get(userKey));
        actions.sendSecret("id=password", Secrets.get("saucedemo.password"));
        actions.takeScreenshot("Credentials entered");
        actions.click("id=login-button");
    }

    @Test(description = "A standard account can log in and sees the product list")
    public void standardUserCanLogin() {
        loginAs("saucedemo.user.standard");

        assertEquals(actions.getText("css=.title"), "Products");
        assertTrue(actions.currentUrl().endsWith("/inventory.html"));
    }

    @Test(description = "A locked-out account is rejected with an error message")
    public void lockedOutUserIsRejected() {
        loginAs("saucedemo.user.locked");

        assertTrue(actions.getText("css=[data-test='error']").contains("this user has been locked out"));
    }
}
