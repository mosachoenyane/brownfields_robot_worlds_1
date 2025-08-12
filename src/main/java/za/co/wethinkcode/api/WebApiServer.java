package za.co.wethinkcode.api;

import io.javalin.Javalin;
import io.javalin.http.HttpCode;

public class WebApiServer {
    private final Javalin app;

    public WebApiServer() {
        this.app = Javalin.create(config -> {
            // Javalin 4.x API
            config.defaultContentType = "application/json";
            config.enableDevLogging();
        });

        app.get("/health", ctx -> {
            ctx.status(HttpCode.OK);
            ctx.result("{\"status\":\"UP\"}");
        });
    }

    public void start(int port) {
        app.start(port);
    }

    public void stop() {
        app.stop();
    }
}