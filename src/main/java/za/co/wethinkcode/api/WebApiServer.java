package za.co.wethinkcode.api;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.javalin.Javalin;
import io.javalin.http.HttpCode;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

/**
 * Web API server for the Robot World application.
 * <p>
 * Wraps a {@link Javalin} instance and exposes REST endpoints for:
 * <ul>
 *   <li>{@code GET /health} — health probe for the server</li>
 *   <li>{@code GET /world} — fetch the current world (if configured)</li>
 *   <li>{@code GET /worlds} — list saved worlds from DB (if configured)</li>
 *   <li>{@code POST /robot/{robot}/launch} — launch a robot</li>
 * </ul>
 * <p>
 * Supports enabling dev logging, custom thread pools, and graceful shutdown via a shutdown hook.
 */

public class WebApiServer {
    private final Javalin app;
    private final ApiConfig config;
    private final Gson gson = new Gson();
    private volatile boolean running = false;
    private final WorldApplication worldApp;

    /**
     * Creates a new Web API server with only the given configuration.
     * World-related endpoints will not be available.
     *
     * @param config API configuration (host, port, dev logging)
     */
    public WebApiServer(ApiConfig config) {
        this(config, null);
    }

    /**
     * Creates a new Web API server with configuration and world application.
     *
     * @param config   API configuration (host, port, dev logging)
     * @param worldApp optional {@link WorldApplication} for world-related endpoints
     */
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

    /**
     * Mounts robot-related API routes (e.g., launch).
     */
    private void mountRobotRoutes() {
        LaunchHandlerWeb launchHandler = new LaunchHandlerWeb();
        app.post("/robot/{robot}/launch", launchHandler::LaunchRobot);
        app.post("/robot/{robot}/look", launchHandler::LookRobot);


    }

    /**
     * Mounts world-related API routes if {@link WorldApplication} is configured.
     * Includes:
     * <ul>
     *   <li>{@code GET /world} — fetch current world</li>
     *   <li>{@code GET /worlds} — list saved worlds</li>
     * </ul>
     */
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

        // GET /world/{name} -> fetch a specific world by name
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

    /**
     * Builds a successful JSON response with a payload.
     *
     * @param data the data to wrap
     * @return a JSON object with {@code result: "OK"} and {@code data}
     */
    private JsonObject ok(JsonObject data) {
        JsonObject payload = new JsonObject();
        payload.addProperty("result", "OK");
        payload.add("data", data);
        return payload;
    }

    /**
     * Builds an error JSON response.
     *
     * @param message error message
     * @return a JSON object with {@code result: "ERROR"} and {@code error}
     */
    private JsonObject error(String message) {
        JsonObject payload = new JsonObject();
        payload.addProperty("result", "ERROR");
        payload.addProperty("error", message);
        return payload;
    }

    /**
     * Starts the web server if it is not already running.
     * Adds a JVM shutdown hook to stop the server gracefully.
     */
    public void start() {
        if (running) return;
        app.start(config.host(), config.port());
        running = true;
        Runtime.getRuntime().addShutdownHook(new Thread(this::safeStop, "webapi-shutdown"));
        System.out.println("Web API listening on " + config.baseUrl());
    }

    /**
     * Stops the web server if it is running.
     */
    public void stop() {
        if (!running) return;
        app.stop();
        running = false;
        System.out.println("Web API stopped.");
    }
    /**
     * Attempts to stop the server gracefully, swallowing any exception.
     */
    private void safeStop() { try { stop(); } catch (Exception ignored) {} }

    /**
     * @return {@code true} if the server is currently running
     */
    public boolean isRunning() { return running; }

    /**
     * @return the base URL for this API (derived from configuration)
     */
    public String baseUrl() { return config.baseUrl(); }
}