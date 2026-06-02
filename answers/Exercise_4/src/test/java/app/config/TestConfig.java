package app.config;

public final class TestConfig {

    static {
        PropertyLoader.loadAllPropertiesIntoSystemProperties();
    }

    private TestConfig() {}

    public static String baseUrl() 
    {
        return System.getProperty("baseUrl");
    }

    public static String browser() 
    {
        return System.getProperty("browser", "chrome");
    }

    public static boolean headless() 
    {
        return Boolean.parseBoolean(System.getProperty("headless", "false"));
    }

    public static int timeoutSeconds() 
    {
        return Integer.parseInt(System.getProperty("timeout", "10"));
    }

    public static String adminEmail() 
    {
        return System.getProperty("adminEmail");
    }

    public static String adminPassword() 
    {
        return System.getProperty("adminPassword");
    }
}