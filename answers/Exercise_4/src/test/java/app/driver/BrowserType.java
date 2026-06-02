package app.driver;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;

public enum BrowserType 
{

    CHROME {
        @Override
        public WebDriver createDriver(boolean headless) 
        {
            ChromeOptions options = new ChromeOptions();
            if (headless) 
            {
                options.addArguments("--headless=new");
            }
            return new ChromeDriver(options);
        }
    },

    EDGE {
        @Override
        public WebDriver createDriver(boolean headless) 
        {
            EdgeOptions options = new EdgeOptions();
            if (headless) 
            {
                options.addArguments("--headless=new");
            }
            return new EdgeDriver(options);
        }
    };

    public abstract WebDriver createDriver(boolean headless);

    public static BrowserType from(String browserName) 
    {
        if (browserName == null || browserName.isBlank()) 
        {
            return CHROME;
        }
        switch (browserName.trim().toLowerCase()) 
        {
            case "edge":
                return EDGE;
            case "chrome": 
                return CHROME;
            default: 
                throw new IllegalArgumentException("Unsupported browser: " + browserName);
        }
    }
}