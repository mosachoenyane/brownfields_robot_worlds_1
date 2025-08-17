package za.co.wethinkcode.server.world;

import  java.util.Properties;
import java.io.InputStream;
import java.io.IOException;

/**
 * Loads and manages configuration properties for the game world.
 * Supports loading from a properties file or using default values.
 */
public class WorldConfig {
    public Properties properties;
    private boolean usingDefaults;

    /**
     * Constructs a WorldConfig using the provided Properties object.
     *
     * @param props Properties containing configuration values.
     */
    public WorldConfig(Properties props) {
        this.properties = props;
        this.usingDefaults = false;
    }

    /**
     * Constructs a WorldConfig by loading properties from a file on the classpath.
     * Falls back to default configuration if loading fails.
     *
     * @param filename The properties file name.
     * @throws IOException If the file cannot be found or read.
     */
    public WorldConfig(String filename) throws IOException {
        properties = new Properties();
        InputStream input = null;

        try {
            input = getClass().getClassLoader().getResourceAsStream(filename);
            if (input == null) {
                throw new IOException("Configuration file not found in classpath");
            }

            properties.load(input);
            usingDefaults = false;

        } catch (IOException e) {
            //if reading from config file fails use the default configurations
            System.out.println("Could not load configuration file: " + filename);
            System.out.println("Reason: " + e.getMessage());
            setDefaults();
            usingDefaults = true;
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    System.err.println("Failed to close input stream: " + e.getMessage());
                }
            }
        }
    }
    /**
     * Constructs a WorldConfig using default property values.
     */
    public WorldConfig() {
    properties = new Properties();
    setDefaults();
    usingDefaults = true;
    }

    /**
     * Sets default values for the world configuration.
     */
    private void setDefaults() {
        properties.setProperty("WORLD_NAME", "Default World");
        properties.setProperty("WORLD_WIDTH","1");
        properties.setProperty("WORLD_HEIGHT","1");
        properties.setProperty("NUM_PITS","0");
        properties.setProperty("NUM_LAKES","0");
        properties.setProperty("NUM_MOUNTAINS","0");
        properties.setProperty("VISIBILITY_RANGE","10");
        properties.setProperty("MAX_SHIELD_STRENGTH","5");
        properties.setProperty("REPAIR_TIME","5");
        properties.setProperty("RELOAD_TIME","5");
        properties.setProperty("MAX_SHOTS", "10");
    }

    /**
     * Returns the world configs for setting.
     *
     * @return World configs as integers.
     */
    public String getName() {return properties.getProperty("WORLD_NAME");
    };
    /**
     * Returns the configured world width.
     *
     * @return The world width as an integer.
     */
    public int getWidth() {
        return Integer.parseInt(properties.getProperty("WORLD_WIDTH"));
    }
    /**
     * Returns the configured world height.
     *
     * @return The world height as an integer.
     */
    public int getHeight() {
        return Integer.parseInt(properties.getProperty("WORLD_HEIGHT"));
    }
    /**
     * Returns the visibility range for robots in the world.
     *
     * @return Visibility range as an integer.
     */
    public int getVisibilityRange() {
        return Integer.parseInt(properties.getProperty("VISIBILITY_RANGE"));
    }

    public int getNumPits() {
        return Integer.parseInt(properties.getProperty("NUM_PITS"));

    }
    public int getNumLakes() {
        return Integer.parseInt(properties.getProperty("NUM_LAKES"));
    }
    public int getNumMountains()
    {
        return Integer.parseInt(properties.getProperty("NUM_MOUNTAINS"));
    }
    public int getNumMines(){
        return Integer.parseInt(properties.getProperty("NUM_MINE"));
    }


    public boolean isUsingDefaults() {
        return usingDefaults;
    }
    /**
     * Returns whether this configuration is using default values.
     *
     * @return true if defaults are being used, false otherwise.
     */
    public int getMaxShieldStrength() {
        return Integer.parseInt(properties.getProperty("MAX_SHIELD_STRENGTH"));
    }

    public int getRepairTime() {
        return Integer.parseInt(properties.getProperty("REPAIR_TIME"));
    }
    public int getReloadTime() {
        return Integer.parseInt(properties.getProperty("RELOAD_TIME"));
    }
    public int getMaxShots() {
        return Integer.parseInt(properties.getProperty("MAX_SHOTS"));
    }

}
