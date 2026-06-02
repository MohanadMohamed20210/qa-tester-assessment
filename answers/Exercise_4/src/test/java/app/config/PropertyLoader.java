package app.config;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Properties;

public final class PropertyLoader 
{

    private static final String CONFIG_DIRECTORY = "src/test/resources";
    private static final String[] EXTENSIONS = {"properties"};

    private PropertyLoader() {}

    public static void loadAllPropertiesIntoSystemProperties() 
    {
        File folder = new File(CONFIG_DIRECTORY);

        if (!folder.exists())
        {
            throw new RuntimeException("Config directory does not exist: " + folder.getAbsolutePath());
        }

        Collection<File> files = FileUtils.listFiles(folder, EXTENSIONS, true);

        for (File file : files) 
        {
            loadPropertyFile(file);
        }
    }

    private static void loadPropertyFile(File file) 
    {
        Properties properties = new Properties();

        try (InputStream inputStream = FileUtils.openInputStream(file)) 
        {
            properties.load(inputStream);

            for (String propertyName : properties.stringPropertyNames()) 
            {
                if (System.getProperty(propertyName) == null) 
                {
                    System.setProperty(propertyName, properties.getProperty(propertyName));
                }
            }
        } 
        catch (IOException e) 
        {
            throw new RuntimeException("Failed to load properties from file: " + file.getAbsolutePath(), e);
        }
    }
}