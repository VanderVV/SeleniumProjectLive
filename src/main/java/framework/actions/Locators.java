package framework.actions;

import org.openqa.selenium.By;

/**
 * Turns a short string into a By so any element can be targeted without writing By code.
 * <pre>
 *   Locators.by("id=username")          Locators.by("css=.btn.primary")
 *   Locators.by("xpath=//a[1]")         Locators.by("name=email")
 *   Locators.by("text=Log in")          (link text)   Locators.by("partial=Log")
 *   Locators.by("class=error")          Locators.by("tag=h1")
 *   Locators.by("#username")            (no prefix: starts with // or ( => xpath, otherwise css)
 * </pre>
 */
public final class Locators {

    private Locators() {
    }

    public static By by(String locator) {
        int eq = locator.indexOf('=');
        if (eq > 0) {
            String type = locator.substring(0, eq).trim().toLowerCase();
            String value = locator.substring(eq + 1).trim();
            switch (type) {
                case "id":      return By.id(value);
                case "name":    return By.name(value);
                case "css":     return By.cssSelector(value);
                case "xpath":   return By.xpath(value);
                case "class":   return By.className(value);
                case "tag":     return By.tagName(value);
                case "text":    return By.linkText(value);
                case "partial": return By.partialLinkText(value);
                default:        break; // '=' belongs to a css/xpath expression, fall through
            }
        }
        String trimmed = locator.trim();
        return trimmed.startsWith("/") || trimmed.startsWith("(") ? By.xpath(trimmed) : By.cssSelector(trimmed);
    }
}
