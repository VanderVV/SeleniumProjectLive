package tests;

import framework.base.BaseTest;
import framework.config.Config;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;


@Epic("Framework")
@Feature("Basic element actions")
public class ElementActionsTest extends BaseTest {


    private static final String SITE = Config.urlOf("herokuapp");

    @Test(description = "sendText + click: valid login shows the success flash message")
    public void sendTextAndClick() {
        actions.open(SITE + "/login");
        actions.sendText("id=username", "tomsmith");
        actions.sendText(By.id("password"), "SuperSecretPassword!");
        actions.takeScreenshot("Credentials entered");
        actions.click("css=button[type='submit']");
        actions.takeScreenshot("After login");

        assertTrue(actions.getText("id=flash").contains("You logged into a secure area!"));
        assertTrue(actions.currentUrl().endsWith("/secure"));
    }

    @Test(description = "sendText + click: bad password shows the error flash message")
    public void invalidLoginShowsError() {
        actions.open(SITE + "/login");
        actions.sendText("id=username", "tomsmith");
        actions.sendText("id=password", "wrong");
        actions.click("css=button[type='submit']");

        assertTrue(actions.getText("id=flash").contains("Your password is invalid!"));
    }

    @Test(description = "clear: empties a field that already holds text")
    public void clearField() {
        actions.open(SITE + "/inputs");
        By number = By.cssSelector("input[type='number']");

        actions.sendText(number, "12345");
        assertEquals(actions.getValue(number), "12345");

        actions.clear(number);
        assertEquals(actions.getValue(number), "");
    }

    @Test(description = "sendText replaces existing text, appendText keeps it")
    public void sendTextReplacesAndAppends() {
        actions.open(SITE + "/inputs");
        By number = By.cssSelector("input[type='number']");

        actions.sendText(number, "10");
        actions.sendText(number, "20");
        assertEquals(actions.getValue(number), "20");

        actions.appendText(number, "5");
        assertEquals(actions.getValue(number), "205");
    }

    @Test(description = "sendKey: arrow keys change a number input")
    public void sendKey() {
        actions.open(SITE + "/inputs");
        By number = By.cssSelector("input[type='number']");

        actions.sendText(number, "5");
        actions.sendKey(number, Keys.ARROW_UP);
        assertEquals(actions.getValue(number), "6");
    }

    @Test(description = "select dropdown by visible text, value and index")
    public void selectDropdown() {
        actions.open(SITE + "/dropdown");
        By dropdown = By.id("dropdown");

        actions.selectByText(dropdown, "Option 1");
        assertEquals(actions.selectedOption(dropdown), "Option 1");

        actions.selectByValue("id=dropdown", "2");
        assertEquals(actions.selectedOption(dropdown), "Option 2");

        actions.selectByIndex(dropdown, 1);
        assertEquals(actions.selectedOption(dropdown), "Option 1");

        actions.selectByContainsText(dropdown, "tion 2");
        assertEquals(actions.selectedOption(dropdown), "Option 2");

        assertTrue(actions.options(dropdown).contains("Option 1"));
    }

    @Test(description = "check / uncheck are idempotent")
    public void checkboxes() {
        actions.open(SITE + "/checkboxes");
        By first = By.xpath("(//input[@type='checkbox'])[1]");
        By second = By.xpath("(//input[@type='checkbox'])[2]");

        actions.check(first);
        actions.check(first); // already checked: must stay checked
        assertTrue(actions.isSelected(first));

        actions.uncheck(second);
        assertFalse(actions.isSelected(second));
    }

    @Test(description = "hover reveals hidden content")
    public void hover() {
        actions.open(SITE + "/hovers");
        By firstFigure = By.cssSelector(".figure:nth-of-type(1)");
        By caption = By.cssSelector(".figure:nth-of-type(1) .figcaption");

        assertFalse(actions.isDisplayed(caption));
        actions.hover(firstFigure);
        assertTrue(actions.isDisplayed(caption));
    }

    @Test(description = "alerts: accept a JS alert and read its text")
    public void jsAlert() {
        actions.open(SITE + "/javascript_alerts");
        actions.click("xpath=//button[text()='Click for JS Alert']");

        assertEquals(actions.acceptAlert(), "I am a JS Alert");
        assertTrue(actions.getText("id=result").contains("You successfully clicked an alert"));
    }

    // ---------------------------------------------------------------- intentional failures (for report demos)

    @Test(groups = "expected-failure", description = "EXPECTED FAILURE - element not found: the locator does not exist on the page")
    public void failsElementNotFound() {
        actions.open(SITE + "/login");
        actions.sendText("id=username", "tomsmith");
        actions.click("id=this-button-does-not-exist");
    }

    @Test(groups = "expected-failure", description = "EXPECTED FAILURE - validation: the asserted message does not match the real one")
    public void failsValidation() {
        actions.open(SITE + "/login");
        actions.sendText("id=username", "tomsmith");
        actions.sendText("id=password", "SuperSecretPassword!");
        actions.click("css=button[type='submit']");

        assertEquals(actions.getText("id=flash").replace("×", "").trim(),
                "Welcome to the dashboard!", "Flash message after login");
    }

    @Test(description = "waitInvisible: loader disappears after a dynamic load")
    public void dynamicLoading() {
        actions.open(SITE + "/dynamic_loading/1");
        actions.click("css=#start button");
        actions.waitInvisible(By.id("loading"));
        assertEquals(actions.getText("css=#finish h4"), "Hello World!");
    }
}
