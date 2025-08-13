package za.co.wethinkcode.api;

import io.javalin.Javalin;
import io.javalin.http.HttpCode;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

public class WebApiServer {
    private final Javalin app;
    private final ApiConfig config;
    private volatile boolean running = false;

    public WebApiServer(ApiConfig config) {
        this.config = config;

        this.app = Javalin.create(jc -> {
            jc.defaultContentType = "application/json";
            if (config.devLogging()) {
                jc.enableDevLogging();
            }
            // Dedicated thread pool for the web server
            jc.server(() -> new Server(new QueuedThreadPool(50, 2)));
            // Optionally enable CORS if you plan to call from a browser later
            // jc.enableCorsForAllOrigins();
        });

        // Connection-level endpoint
        app.get("/health", ctx -> {
            ctx.status(HttpCode.OK);
            ctx.result("{\"status\":\"UP\"}");
        });

        // Note: No domain endpoints here yet. We’ll add them later.
    }

    public void start() {
        if (running) return;
        app.start(config.host(), config.port());
        running = true;
        Runtime.getRuntime().addShutdownHook(new Thread(this::safeStop, "webapi-shutdown"));
        System.out.println("Web API listening on " + config.baseUrl());
    }

    public void stop() {
        if (!running) return;
        app.stop();
        running = false;
        System.out.println("Web API stopped.");
    }

    private void safeStop() {
        try {
            stop();
        } catch (Exception ignored) {
        }
    }

    public boolean isRunning() {
        return running;
    }

    public String baseUrl() {
        return config.baseUrl();
    }
}