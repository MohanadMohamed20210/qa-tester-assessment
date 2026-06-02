package app.utils;

import io.qameta.allure.Allure;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class ScreenshotHelper {

    public static void takeScreenshot(WebDriver driver, String fileName) 
    {
        try 
        {
            File src = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            File dest = new File("src/test/resources/" + fileName + ".png");
            FileUtils.copyFile(src, dest);
            Allure.addAttachment(fileName,new FileInputStream(dest));

        } 
        catch (IOException e) 
        {
            throw new RuntimeException("Screenshot failed", e);
        }
    }
}