package app.pages;

import app.utils.Waits;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public abstract class BasePage 
{

    protected final WebDriver driver;
    protected final Waits waits;

    protected BasePage(WebDriver driver) 
    {
        this.driver = driver;
        this.waits = new Waits(driver);
    }

    protected void click(By locator) 
    {
        waits.clickable(locator).click();
    }

    protected void type(By locator, String value) 
    {
        waits.visible(locator).clear();
        waits.visible(locator).sendKeys(value);
    }

    protected String text(By locator) 
    {
        return waits.textOf(locator);
    }

    protected boolean isDisplayed(By locator) 
    {
        try 
        {
            return waits.visible(locator).isDisplayed();
        } 
        catch (Exception e) 
        {
            return false;
        }
    }
}