package app.driver;

import app.config.TestConfig;
import org.openqa.selenium.WebDriver;

import java.time.Duration;

public final class DriverFactory 
{

    private DriverFactory() {}

    public static WebDriver createDriver() 
    {
        BrowserType browserType = BrowserType.from(TestConfig.browser());
        WebDriver driver = browserType.createDriver(TestConfig.headless());
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));
        driver.manage().timeouts().scriptTimeout(Duration.ofSeconds(20));
        driver.manage().window().maximize();

        return driver;
    }
}