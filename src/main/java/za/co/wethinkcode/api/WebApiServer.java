package za.co.wethinkcode.api;

import io.javalin.Javalin;
import io.javalin.http.HttpCode;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

public class WebApiServer {
    private final Javalin app;

    public WebApiServer() {
        this.app = Javalin.create(config -> {
            // Javalin 4.x API
            config.defaultContentType = "application/json";
            config.enableDevLogging();

            // Resolve THREADING Issues
            config.server(() -> {
                QueuedThreadPool threadPool = new QueuedThreadPool(50, 2);
                return new Server(threadPool);
            });
        });

        app.get("/health", ctx -> {
            ctx.status(HttpCode.OK);
            ctx.result("{\"status\":\"UP\"}");
        });
    }

    public void start(int port) {
        // Bind explicitly to localhost to ensure browser can reach it
        app.start("localhost", port);
    }

    public void stop() {
        app.stop();
    }
}