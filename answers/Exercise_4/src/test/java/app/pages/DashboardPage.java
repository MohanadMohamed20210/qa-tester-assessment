package app.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class DashboardPage extends BasePage 
{

    private final By userGreeting = By.id("user-greeting");

    public DashboardPage(WebDriver driver) 
    {
        super(driver);
    }

    public boolean isLoaded() 
    {
        return isUserGreetingVisible();
    }

    public boolean isUserGreetingVisible() 
    {
        return isDisplayed(userGreeting) && !getUserGreetingText().isBlank();
    }

    public String getUserGreetingText()
    {
        if (!isDisplayed(userGreeting)) 
        {
            return "";
        }

        return text(userGreeting).trim();
    }
}