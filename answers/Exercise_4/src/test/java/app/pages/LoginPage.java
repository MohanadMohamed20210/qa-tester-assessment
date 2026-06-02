package app.pages;

import app.config.TestConfig;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.locators.RelativeLocator;

public class LoginPage extends BasePage 
{


    private final By emailInput = By.xpath("//form //input[@type='email']");
    private final By passwordInput = By.xpath("//form //input[@type='password']");
    private final By loginButton = By.xpath("//form /button");


    // Error message locator is not available in the original script
    // This relative XPath assumes an error message appears close to the login form after invalid login
    public By getLoginError(WebElement formElement) 
    {
        return RelativeLocator.with(By.tagName("div"))
                .below(formElement)
                .near(formElement);
    }

    public LoginPage(WebDriver driver) 
    {
        super(driver);
    }

    public LoginPage open() 
    {
        driver.get(TestConfig.baseUrl());
        waits.pageReady();
        return this;
    }

    public DashboardPage loginAs(String email, String password) 
    {
        type(emailInput, email);
        type(passwordInput, password);
        click(loginButton);
        return new DashboardPage(driver);
    }

    public boolean isLoginErrorVisible() 
    {
        return isDisplayed(getLoginError(waits.visible(emailInput)));
    }

    public String getLoginErrorText() 
    {
        if (!isLoginErrorVisible()) 
        {
            return "";
        }

        return text(getLoginError(waits.visible(emailInput)));
    }
}