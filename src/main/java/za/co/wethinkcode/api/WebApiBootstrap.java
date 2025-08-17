package za.co.wethinkcode.api;

/**
 * Entry point for starting the Web API server.
 * Bootstraps the {@link WebApiServer} with a default local configuration
 * and performs a simple health check probe after startup.
 */

public class WebApiBootstrap {

    /**
     * Main method that starts the API server.
     * The server is initialized using {@link ApiConfig#localDefault()} (host: {@code localhost}, port: {@code 7000}),
     * then a simple health probe is run against the {@code /health} endpoint.
     *
     * @param args command-line arguments (currently ignored)
     */
    public static void main(String[] args) {
        ApiConfig cfg = ApiConfig.localDefault();
        WebApiServer api = new WebApiServer(cfg);
        api.start();

        // Simple probe
        ApiHealthClient probe = new ApiHealthClient();
        System.out.println("Health: " + (probe.isUp(cfg.baseUrl()) ? "UP" : "DOWN"));
    }
}
