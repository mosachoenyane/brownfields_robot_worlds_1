package za.co.wethinkcode.api;

/**
 * Configuration class for the API server.
 * Holds the host, port, and development logging preferences for running the API.
 * Provides convenience methods for retrieving the base URL and a default local configuration.
 */
public class ApiConfig {
    private final String host;
    private final int port;
    private final boolean devLogging;

    /**
     * Creates a new API configuration.
     *
     * @param host       the host name or IP address where the API will run
     * @param port       the port number the API will bind to
     * @param devLogging whether development logging should be enabled
     */

    public ApiConfig(String host, int port, boolean devLogging) {
        this.host = host;
        this.port = port;
        this.devLogging = devLogging;
    }

    /**
     * @return the host name or IP address
     */
    public String host() { return host; }

    /**
     * @return the port number
     */
    public int port() { return port; }

    /**
     * @return {@code true} if development logging is enabled, otherwise {@code false}
     */
    public boolean devLogging() { return devLogging; }

    /**
     * Builds the base URL for the API from the host and port.
     * @return the base URL as a string (e.g., {@code http://localhost:7000})
     */
    public String baseUrl() {
        return "http://" + host + ":" + port;
    }

    /**
     * Provides a default local development configuration.
     * Defaults to {@code localhost} on port {@code 7000} with development logging enabled.
     * @return a default {@link ApiConfig} instance
     */
    public static ApiConfig localDefault() {
        return new ApiConfig("localhost", 7000, true);
    }
}
