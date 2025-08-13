package za.co.wethinkcode.api;

public class ApiConfig {
    private final String host;
    private final int port;
    private final boolean devLogging;

    public ApiConfig(String host, int port, boolean devLogging) {
        this.host = host;
        this.port = port;
        this.devLogging = devLogging;
    }

    public String host() { return host; }
    public int port() { return port; }
    public boolean devLogging() { return devLogging; }

    public String baseUrl() {
        return "http://" + host + ":" + port;
    }

    public static ApiConfig localDefault() {
        return new ApiConfig("localhost", 7000, true);
    }
}
