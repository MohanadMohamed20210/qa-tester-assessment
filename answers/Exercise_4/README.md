# QA Automation Test Assessment

This project is a Java-based QA automation assessment using Selenium WebDriver, TestNG, Maven, and Allure Report.

## Project Overview

The project automates login testing using the Page Object Model design pattern.

It includes:

- Selenium WebDriver for browser automation
- TestNG for test execution
- Maven for project build and dependency management
- Allure for test reporting
- Screenshot capture for test evidence
- Config file support for browser and test settings

## Project Structure

```text
src
├── main
│   └── java
│       └── app
│           └── App.java
└── test
    ├── java
    │   └── app
    │       ├── LoginTests.java
    │       ├── config
    │       │   ├── PropertyLoader.java
    │       │   └── TestConfig.java
    │       ├── driver
    │       │   ├── BrowserType.java
    │       │   └── DriverFactory.java
    │       ├── pages
    │       │   ├── BasePage.java
    │       │   ├── DashboardPage.java
    │       │   └── LoginPage.java
    │       └── utils
    │           ├── ScreenshotHelper.java
    │           └── Waits.java
    └── resources
        ├── config.properties
        ├── suite1.xml
        └── login_shouldBehaveCorrectlyBasedOnCredentials.png