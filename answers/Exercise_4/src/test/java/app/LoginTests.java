package app;

/*
ANTI-PATTERNS FOUND IN THE ORIGINAL SCRIPT:

    1- The original script used absolute XPath locators such as:
    /html/body/div[1]/div[2]/form/div[1]/input
    These locators are fragile because any small layout change can break the test
    plus div[1] is not good practice. I replaced them with more stable locators based on the provided DOM structure and attributes

    2- The original script used Thread.sleep
    This makes the test slower and flaky because it waits a fixed time instead of waiting for the actual element state
    plus the brower which is a child process of the test process will go to sleep state so it will not be responsive

    3- The WebDriver was created directly inside the test.
    This makes it harder to run the same test on different browsers or in headless mode for CI

    4- The URL and credentials were hard-coded inside the test.
    This is not safe and makes the test harder to run on different environments

    5- The original script mixed page locators, page actions, test data, and assertions in one place.
    This makes maintenance harder when the UI changes

    6- There was no proper assertion for the login result
    Printing the greeting text is not enough to decide if the test passed or failed.

    7- There was no screenshot capture on failure
    This makes CI failures harder to debug

REFACTOR STRUCTURE:

- config/TestConfig.java:
  Reads baseUrl, browser, headless mode, timeout, adminEmail, and adminPassword from system properties.

- config/PropertyLoader.java:
  Loads .properties files from src/test/resources and adds them to system properties.

- driver/BrowserType.java:
  Enum-based browser factory. It supports Chrome and Edge and applies headless options when needed.

- driver/DriverFactory.java:
  Creates the WebDriver based on the configured browser and headless mode.

- utils/Waits.java:
  Central place for explicit waits. This replaces Thread.sleep.

- utils/ScreenshotHelper.java:
  Takes screenshot on failure, saves it under src/test/resources/screenshots, and attaches it to Allure.

- pages/BasePage.java:
  Common page actions such as click, type, get text, and visibility check.

- pages/LoginPage.java:
  Page Object for the login page. It contains login locators and login action.

- pages/DashboardPage.java:
  Page Object for the dashboard page. It checks the user greeting using By.id("user-greeting").

- tests/LoginTests.java:
  TestNG test class using DataProvider for valid and invalid login scenarios.


  
IMPROVEMENTS IN THIS REFACTOR:

1- I used Page Object Model by moving login actions to LoginPage and dashboard checks to DashboardPage
2- I replaced absolute XPath with shorter relative XPath based on the provided DOM structure
3- I kept By.id("user-greeting") because the original script already used it and ID is more stable than absolute XPath
4- I used WebDriverWait through a Waits helper instead of Thread.sleep
5- I used DriverFactory and BrowserType enum to support Chrome, Edge, and headless mode
6- I moved URL, browser, headless mode, timeout, and credentials to config.properties
7- I used TestNG DataProvider to cover valid and invalid login cases
8- I added real assertions for dashboard loading, greeting visibility, and invalid login error
9- I added screenshot attachment on failure using Allure
*/

import app.config.TestConfig;
import app.driver.DriverFactory;
import app.pages.DashboardPage;
import app.pages.LoginPage;
import app.utils.ScreenshotHelper;
import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import org.openqa.selenium.WebDriver;
import org.testng.ITestResult;
import org.testng.annotations.*;

import static org.testng.Assert.assertTrue;

@Feature("Admin Login")
public class LoginTests 
{

    private WebDriver driver;

    @BeforeMethod(alwaysRun = true)
    public void setUp() 
    {
        driver = DriverFactory.createDriver();
    }

    @DataProvider(name = "loginData")
    public Object[][] loginData() 
    {
        return new Object[][]{
                {
                        TestConfig.adminEmail(),
                        TestConfig.adminPassword(),
                        true,
                        "Valid admin credentials should open dashboard and show user greeting"
                },
                {
                        "baaaaad-user@example.com",
                        "baaaaad-password",
                        false,
                        "Invalid credentials should stay on login page and show an error"
                }
        };
    }

    @Test(dataProvider = "loginData", groups = {"login", "smoke"})
    @Description("Refactored login test using Page Object Model, explicit waits, config properties and TestNG parameterization")
    public void login_shouldBehaveCorrectlyBasedOnCredentials(String email,String password,boolean shouldLogin,String caseDescription) 
    {
        LoginPage loginPage = new LoginPage(driver).open();
        DashboardPage dashboardPage = loginPage.loginAs(email, password);

        if (shouldLogin) 
        {
            assertTrue(dashboardPage.isLoaded(),"Dashboard should be loaded after valid login. Case: " + caseDescription);
            assertTrue(dashboardPage.isUserGreetingVisible(),"User greeting should be visible after valid login. Case: " + caseDescription);
        } 
        else 
        {
            assertTrue(loginPage.isLoginErrorVisible(),"Login error should be visible after invalid login. Case: " + caseDescription);
        }
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown(ITestResult result) 
    {
        if (!result.isSuccess()) 
        {
            ScreenshotHelper.takeScreenshot(driver, result.getName());
        }

        if (driver != null) 
        {
            driver.quit();
        }
    }
}