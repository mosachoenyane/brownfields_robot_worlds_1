package za.co.wethinkcode.api;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.javalin.Javalin;
import io.javalin.http.HttpCode;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

public class WebApiServer {
    private final Javalin app;
    private final ApiConfig config;
    private final Gson gson = new Gson();
    private volatile boolean running = false;
    private final WorldApplication worldApp;

    public WebApiServer(ApiConfig config) {
        this(config, null);
    }

    public WebApiServer(ApiConfig config, WorldApplication worldApp) {
        this.config = config;
        this.worldApp = worldApp;

        this.app = Javalin.create(jc -> {
            jc.defaultContentType = "application/json";
            if (config.devLogging()) jc.enableDevLogging();
            jc.server(() -> new Server(new QueuedThreadPool(50, 2)));
        });

        app.get("/health", ctx -> {
            ctx.status(HttpCode.OK);
            ctx.result("{\"status\":\"UP\"}");
        });

        mountWorldRoutesIfAvailable();
        mountRobotRoutes();
    }

    private void mountRobotRoutes() {
        LaunchHandlerWeb launchHandler = new LaunchHandlerWeb();
        app.post("/robot/{robot}/launch", launchHandler::LaunchRobot);

    }

    private void mountWorldRoutesIfAvailable() {
        app.get("/world", ctx -> {
            if (worldApp == null) {
                ctx.status(HttpCode.NOT_IMPLEMENTED).result(gson.toJson(error("WorldApplication not configured")));
                return;
            }
            try {
                JsonObject payload = ok(worldApp.getCurrentWorld());
                ctx.status(HttpCode.OK).result(gson.toJson(payload));
            } catch (Exception e) {
                ctx.status(HttpCode.INTERNAL_SERVER_ERROR).result(gson.toJson(error("Failed to fetch world: " + e.getMessage())));
            }
        });

            // Rename: GET /world/all -> list all saved worlds from DB (same handler as before)
            app.get("/world/all", ctx -> {
            if (worldApp == null) {
                ctx.status(HttpCode.NOT_IMPLEMENTED).result(gson.toJson(error("WorldApplication not configured")));
                return;
            }
            try {
                JsonObject payload = ok(worldApp.listSavedWorlds());
                ctx.status(HttpCode.OK).result(gson.toJson(payload));
            } catch (Exception e) {
                ctx.status(HttpCode.INTERNAL_SERVER_ERROR).result(gson.toJson(error("Failed to list worlds: " + e.getMessage())));
            }
        });

        // New: GET /world/{name} -> fetch a specific world by name
        app.get("/world/{name}", ctx -> {
            if (worldApp == null) {
                ctx.status(HttpCode.NOT_IMPLEMENTED).result(gson.toJson(error("WorldApplication not configured")));
                return;
            }
            try {
                String name = ctx.pathParam("name");
                JsonObject data = worldApp.getWorldByName(name);
                if (data.has("error")) {
                    ctx.status(HttpCode.NOT_FOUND).result(gson.toJson(error("World not found: " + name)));
                } else {
                    ctx.status(HttpCode.OK).result(gson.toJson(ok(data)));
                }
            } catch (Exception e) {
                ctx.status(HttpCode.INTERNAL_SERVER_ERROR).result(gson.toJson(error("Failed to fetch world by name: " + e.getMessage())));
            }
        });
    }

    private JsonObject ok(JsonObject data) {
        JsonObject payload = new JsonObject();
        payload.addProperty("result", "OK");
        payload.add("data", data);
        return payload;
    }

    private JsonObject error(String message) {
        JsonObject payload = new JsonObject();
        payload.addProperty("result", "ERROR");
        payload.addProperty("error", message);
        return payload;
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

    private void safeStop() { try { stop(); } catch (Exception ignored) {} }

    public boolean isRunning() { return running; }
    public String baseUrl() { return config.baseUrl(); }
}