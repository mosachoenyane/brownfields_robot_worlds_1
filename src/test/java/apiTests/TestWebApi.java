package apiTests;

import za.co.wethinkcode.api.ApiConfig;
import za.co.wethinkcode.api.WebApiServer;
import za.co.wethinkcode.api.WorldApplication;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.net.ServerSocket;

public class TestWebApi {
    private final WebApiServer server;
    private final String baseUrl;

    public TestWebApi(WorldApplication worldApp) {
        int port = findFreePort();
        ApiConfig cfg = new ApiConfig("localhost", port, false);
        this.server = new WebApiServer(cfg, worldApp);
        this.baseUrl = cfg.baseUrl();
    }

    public void start() {
        server.start();
    }

    public void stop() {
        server.stop();
    }

    public String baseUrl() {
        return baseUrl;
    }

    // FAKE WORLD to enable testing of the Web API connection.
    public static class FakeWorldApp implements WorldApplication {
        @Override
        public JsonObject getCurrentWorld() {
            JsonObject data = new JsonObject();
            data.addProperty("name", "test-world");
            data.addProperty("width", 10);
            data.addProperty("height", 10);
            data.add("obstacles", new JsonArray());
            data.add("robots", new JsonArray());
            return data;
        }

        @Override
        public JsonObject listSavedWorlds() {
            JsonObject outer = new JsonObject();
            outer.add("worlds", new JsonArray());
            return outer;
        }

        @Override
        public JsonObject getWorldByName(String name) {
            JsonObject notFound = new JsonObject();
            notFound.addProperty("error", "World not found");
            notFound.addProperty("name", name);
            return notFound;
        }
    }

    private static int findFreePort() {
        try (ServerSocket socket = new ServerSocket(0)) {
            socket.setReuseAddress(true);
            return socket.getLocalPort();
        } catch (IOException e) {
            throw new IllegalStateException("Could not find a free TCP port", e);
        }
    }
}
