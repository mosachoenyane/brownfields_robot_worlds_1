package za.co.wethinkcode.api;

public class WebApiBootstrap {
    public static void main(String[] args) {
        ApiConfig cfg = ApiConfig.localDefault();
        WebApiServer api = new WebApiServer(cfg);
        api.start();

        // Simple probe
        ApiHealthClient probe = new ApiHealthClient();
        System.out.println("Health: " + (probe.isUp(cfg.baseUrl()) ? "UP" : "DOWN"));
    }
}
