package za.co.wethinkcode.api;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.javalin.Javalin;
import io.javalin.http.HttpCode;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import za.co.wethinkcode.api.WorldApplication;

public class WebApiServer {
    private final Javalin app;
    private final ApiConfig config;
    private final Gson gson = new Gson();
    private volatile boolean running = false;

    // Application service (can be null if not wired yet)
    private final WorldApplication worldApp;

    // Constructor without application service (health-only)
    public WebApiServer(ApiConfig config) {
        this(config, null);
    }

    // Constructor with application service (enables /world)
    public WebApiServer(ApiConfig config, WorldApplication worldApp) {
        this.config = config;
        this.worldApp = worldApp;

        this.app = Javalin.create(jc -> {
            jc.defaultContentType = "application/json";
            if (config.devLogging()) {
                jc.enableDevLogging();
            }
            jc.server(() -> new Server(new QueuedThreadPool(50, 2)));
        });

        // Connection-level endpoint
        app.get("/health", ctx -> {
            ctx.status(HttpCode.OK);
            ctx.result("{\"status\":\"UP\"}");
        });

        // Mount application endpoints if service is provided
        mountWorldRoutesIfAvailable();
    }

    private void mountWorldRoutesIfAvailable() {
        // GET /world -> Current world snapshot via application service
        app.get("/world", ctx -> {
            if (worldApp == null) {
                JsonObject error = new JsonObject();
                error.addProperty("result", "ERROR");
                error.addProperty("error", "WorldApplication not configured");
                ctx.status(HttpCode.NOT_IMPLEMENTED).result(gson.toJson(error));
                return;
            }
            try {
                JsonObject data = worldApp.getCurrentWorld();
                JsonObject payload = new JsonObject();
                payload.addProperty("result", "OK");
                payload.add("data", data);
                ctx.status(HttpCode.OK).result(gson.toJson(payload));
            } catch (Exception e) {
                JsonObject error = new JsonObject();
                error.addProperty("result", "ERROR");
                error.addProperty("error", "Failed to fetch world: " + e.getMessage());
                ctx.status(HttpCode.INTERNAL_SERVER_ERROR).result(gson.toJson(error));
            }
        });
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