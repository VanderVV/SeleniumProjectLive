package framework.actions;

import framework.config.Config;
import framework.config.Secrets;
import framework.driver.DriverFactory;
import framework.reporting.ScreenshotUtil;
import framework.reporting.StepLogger;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Reusable, explicit-wait based browser actions. Every action is reported as an Allure step
 * and an Extent log line through {@link StepLogger}. Methods take a By; the String overloads
 * accept the short form understood by {@link Locators} (e.g. "id=username", "css=.btn").
 */
public class ElementActions {

    private final int timeoutSeconds;

    public ElementActions() {
        this(Config.getInt("explicit.wait.seconds", 15));
    }

    public ElementActions(int timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }


    public WebDriver driver() {
        return DriverFactory.getDriver();
    }

    private WebDriverWait explicitWait() {
        WebDriverWait wait = new WebDriverWait(driver(), Duration.ofSeconds(timeoutSeconds));
        wait.ignoring(StaleElementReferenceException.class);
        return wait;
    }

    // ------------------------------------------------------------------ screenshots


    public void takeScreenshot(String name) {
        StepLogger.step("Screenshot: " + name, () -> ScreenshotUtil.take(name));
    }

    // ------------------------------------------------------------------ navigation

    public void open(String urlOrPath) {
        String url = urlOrPath.startsWith("http") ? urlOrPath : Config.baseUrl() + urlOrPath;
        StepLogger.step("Open " + url, () -> driver().get(url));
    }

    public String currentUrl() {
        return driver().getCurrentUrl();
    }

    public String title() {
        return driver().getTitle();
    }

    public void refresh() {
        StepLogger.step("Refresh the page", () -> driver().navigate().refresh());
    }

    public void back() {
        StepLogger.step("Navigate back", () -> driver().navigate().back());
    }

    // ------------------------------------------------------------------ waits

    public WebElement waitVisible(By locator) {
        return explicitWait().until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    public WebElement waitClickable(By locator) {
        return explicitWait().until(ExpectedConditions.elementToBeClickable(locator));
    }

    public WebElement waitPresent(By locator) {
        return explicitWait().until(ExpectedConditions.presenceOfElementLocated(locator));
    }

    public void waitInvisible(By locator) {
        StepLogger.step("Wait until " + locator + " disappears",
                () -> explicitWait().until(ExpectedConditions.invisibilityOfElementLocated(locator)));
    }

    public void waitForTextInElement(By locator, String text) {
        StepLogger.step("Wait for text '" + text + "' in " + locator,
                () -> explicitWait().until(ExpectedConditions.textToBePresentInElementLocated(locator, text)));
    }

    public void waitForUrlContains(String fragment) {
        StepLogger.step("Wait for URL to contain '" + fragment + "'",
                () -> explicitWait().until(ExpectedConditions.urlContains(fragment)));
    }

    // ------------------------------------------------------------------ click

    public void click(By locator) {
        StepLogger.step("Click " + locator, () -> waitClickable(locator).click());
    }

    public void click(String locator) {
        click(Locators.by(locator));
    }

    public void doubleClick(By locator) {
        StepLogger.step("Double-click " + locator,
                () -> new Actions(driver()).doubleClick(waitClickable(locator)).perform());
    }

    public void rightClick(By locator) {
        StepLogger.step("Right-click " + locator,
                () -> new Actions(driver()).contextClick(waitClickable(locator)).perform());
    }

    /** Fallback for elements covered by overlays or not clickable through the normal route. */
    public void jsClick(By locator) {
        StepLogger.step("JS click " + locator,
                () -> ((JavascriptExecutor) driver()).executeScript("arguments[0].click();", waitPresent(locator)));
    }

    public void hover(By locator) {
        StepLogger.step("Hover over " + locator,
                () -> new Actions(driver()).moveToElement(waitVisible(locator)).perform());
    }

    public void scrollIntoView(By locator) {
        StepLogger.step("Scroll to " + locator,
                () -> ((JavascriptExecutor) driver())
                        .executeScript("arguments[0].scrollIntoView({block:'center'});", waitPresent(locator)));
    }

    // ------------------------------------------------------------------ text input

    public void sendText(By locator, String text) {
        StepLogger.step("Type '" + text + "' into " + locator, () -> {
            WebElement element = waitVisible(locator);
            clearElement(element);
            element.sendKeys(text);
        });
    }

    public void sendText(String locator, String text) {
        sendText(Locators.by(locator), text);
    }

    /**
     * Same as sendText but the value is never written to the reports: they show ******** instead.
     * Use it for usernames and passwords, e.g. actions.sendSecret("id=password", Secrets.get("saucedemo.password")).
     */
    public void sendSecret(By locator, String secretValue) {
        StepLogger.step("Type " + Secrets.MASK + " into " + locator, () -> {
            WebElement element = waitVisible(locator);
            clearElement(element);
            element.sendKeys(secretValue);
        });
    }

    public void sendSecret(String locator, String secretValue) {
        sendSecret(Locators.by(locator), secretValue);
    }

    /** Types without clearing, appending to whatever is already in the field. */
    public void appendText(By locator, String text) {
        StepLogger.step("Append '" + text + "' to " + locator, () -> waitVisible(locator).sendKeys(text));
    }

    public void sendKey(By locator, Keys key) {
        StepLogger.step("Press " + key.name() + " in " + locator, () -> waitVisible(locator).sendKeys(key));
    }

    public void clear(By locator) {
        StepLogger.step("Clear " + locator, () -> clearElement(waitVisible(locator)));
    }

    public void clear(String locator) {
        clear(Locators.by(locator));
    }


    private void clearElement(WebElement element) {
        element.clear();
        String value = element.getAttribute("value");
        if (value != null && !value.isEmpty()) {
            element.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.BACK_SPACE);
        }
    }

    // ------------------------------------------------------------------ dropdowns (<select>)

    public void selectByText(By locator, String visibleText) {
        StepLogger.step("Select '" + visibleText + "' in " + locator,
                () -> new Select(waitVisible(locator)).selectByVisibleText(visibleText));
    }

    public void selectByText(String locator, String visibleText) {
        selectByText(Locators.by(locator), visibleText);
    }

    public void selectByValue(By locator, String value) {
        StepLogger.step("Select value '" + value + "' in " + locator,
                () -> new Select(waitVisible(locator)).selectByValue(value));
    }

    public void selectByValue(String locator, String value) {
        selectByValue(Locators.by(locator), value);
    }

    public void selectByIndex(By locator, int index) {
        StepLogger.step("Select index " + index + " in " + locator,
                () -> new Select(waitVisible(locator)).selectByIndex(index));
    }

    public void selectByIndex(String locator, int index) {
        selectByIndex(Locators.by(locator), index);
    }

    // Picks the first option whose text contains the fragment (case-insensitive).
    public void selectByContainsText(By locator, String fragment) {
        StepLogger.step("Select option containing '" + fragment + "' in " + locator, () -> {
            Select select = new Select(waitVisible(locator));
            WebElement match = select.getOptions().stream()
                    .filter(o -> o.getText().toLowerCase().contains(fragment.toLowerCase()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException(
                            "No option containing '" + fragment + "' in " + locator));
            select.selectByVisibleText(match.getText());
        });
    }

    public void deselectAll(By locator) {
        StepLogger.step("Deselect all in " + locator, () -> new Select(waitVisible(locator)).deselectAll());
    }

    public String selectedOption(By locator) {
        return new Select(waitVisible(locator)).getFirstSelectedOption().getText().trim();
    }

    public List<String> options(By locator) {
        return new Select(waitVisible(locator)).getOptions().stream()
                .map(o -> o.getText().trim())
                .collect(Collectors.toList());
    }

    // ------------------------------------------------------------------ checkboxes / radios

    public void check(By locator) {
        StepLogger.step("Check " + locator, () -> {
            WebElement element = waitClickable(locator);
            if (!element.isSelected()) element.click();
        });
    }

    public void uncheck(By locator) {
        StepLogger.step("Uncheck " + locator, () -> {
            WebElement element = waitClickable(locator);
            if (element.isSelected()) element.click();
        });
    }

    public boolean isSelected(By locator) {
        return waitPresent(locator).isSelected();
    }

    // ------------------------------------------------------------------ reading state

    public String getText(By locator) {
        return waitVisible(locator).getText().trim();
    }

    public String getText(String locator) {
        return getText(Locators.by(locator));
    }

    public String getValue(By locator) {
        return waitPresent(locator).getAttribute("value");
    }

    public String getAttribute(By locator, String attribute) {
        return waitPresent(locator).getAttribute(attribute);
    }

    public boolean isDisplayed(By locator) {
        List<WebElement> found = driver().findElements(locator);
        return !found.isEmpty() && found.get(0).isDisplayed();
    }

    public boolean isEnabled(By locator) {
        return waitPresent(locator).isEnabled();
    }

    public int count(By locator) {
        return driver().findElements(locator).size();
    }

    public List<String> texts(By locator) {
        return driver().findElements(locator).stream().map(e -> e.getText().trim()).collect(Collectors.toList());
    }

    // ------------------------------------------------------------------ alerts, frames, windows

    public String acceptAlert() {
        return StepLogger.step("Accept alert", () -> {
            Alert alert = explicitWait().until(ExpectedConditions.alertIsPresent());
            String text = alert.getText();
            alert.accept();
            return text;
        });
    }

    public String dismissAlert() {
        return StepLogger.step("Dismiss alert", () -> {
            Alert alert = explicitWait().until(ExpectedConditions.alertIsPresent());
            String text = alert.getText();
            alert.dismiss();
            return text;
        });
    }

    public void typeInAlertAndAccept(String text) {
        StepLogger.step("Type '" + text + "' in prompt and accept", () -> {
            Alert alert = explicitWait().until(ExpectedConditions.alertIsPresent());
            alert.sendKeys(text);
            alert.accept();
        });
    }

    public void switchToFrame(By locator) {
        StepLogger.step("Switch to frame " + locator,
                () -> explicitWait().until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(locator)));
    }

    public void switchToDefaultContent() {
        StepLogger.step("Switch to default content", () -> driver().switchTo().defaultContent());
    }

    /** Switches to the most recently opened window/tab. */
    public void switchToLatestWindow() {
        StepLogger.step("Switch to the newest window", () -> {
            explicitWait().until(d -> d.getWindowHandles().size() > 1);
            List<String> handles = List.copyOf(driver().getWindowHandles());
            driver().switchTo().window(handles.get(handles.size() - 1));
        });
    }
}
