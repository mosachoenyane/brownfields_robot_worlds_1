package apiTests;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.*;
import za.co.wethinkcode.api.WorldApplication;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class GetAllWorldsTests {
    private TestWebApi api;
    private HttpClient client;

    @BeforeAll
    void startApi() {
        api = new TestWebApi(new WorldsListingApp());
        api.start();
        client = HttpClient.newHttpClient();
    }

    @AfterAll
    void stopApi() {
        api.stop();
    }

    @Test
    @DisplayName("GET /world/all returns a list of saved worlds with correct structure")
    void getAllWorlds_success() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(api.baseUrl() + "/world/all"))
                .GET()
                .build();

        HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, res.statusCode());
        assertTrue(res.headers().firstValue("Content-Type").orElse("").contains("application/json"));

        JsonObject root = JsonParser.parseString(res.body()).getAsJsonObject();

        assertTrue(root.has("result"));
        assertEquals("OK", root.get("result").getAsString());
        assertTrue(root.has("data"));
        assertFalse(root.has("error"));

        JsonObject data = root.getAsJsonObject("data");
        assertTrue(data.has("worlds"));
        assertTrue(data.get("worlds").isJsonArray());

        JsonArray worlds = data.getAsJsonArray("worlds");
        assertTrue(worlds.size() >= 1, "Expected at least one world in the list");

        for (JsonElement el : worlds) {
            assertTrue(el.isJsonObject());
            JsonObject w = el.getAsJsonObject();

            assertTrue(w.has("name"));
            assertTrue(w.get("name").isJsonPrimitive());

            assertTrue(w.has("width"));
            assertTrue(w.get("width").isJsonPrimitive());
            assertTrue(w.get("width").getAsJsonPrimitive().isNumber());

            assertTrue(w.has("height"));
            assertTrue(w.get("height").isJsonPrimitive());
            assertTrue(w.get("height").getAsJsonPrimitive().isNumber());

            assertTrue(w.has("obstacles"));
            assertTrue(w.get("obstacles").isJsonArray());

            JsonArray obstacles = w.getAsJsonArray("obstacles");
            if (obstacles.size() > 0) {
                JsonElement first = obstacles.get(0);
                assertTrue(first.isJsonObject());
                JsonObject ob = first.getAsJsonObject();
                assertTrue(ob.has("x"));
                assertTrue(ob.has("y"));
                assertTrue(ob.has("width"));
                assertTrue(ob.has("height"));
                assertTrue(ob.get("x").getAsJsonPrimitive().isNumber());
                assertTrue(ob.get("y").getAsJsonPrimitive().isNumber());
                assertTrue(ob.get("width").getAsJsonPrimitive().isNumber());
                assertTrue(ob.get("height").getAsJsonPrimitive().isNumber());
            }

        }
    }
    /**
     * Test double that returns a non-empty list for /world/all.
     */
    private static class WorldsListingApp implements WorldApplication {
        @Override
        public com.google.gson.JsonObject getCurrentWorld() {
            com.google.gson.JsonObject data = new com.google.gson.JsonObject();
            data.addProperty("name", "current-world");
            data.addProperty("width", 15);
            data.addProperty("height", 15);
            data.add("obstacles", new com.google.gson.JsonArray());
            data.add("robots", new com.google.gson.JsonArray());
            return data;
        }

        @Override
        public com.google.gson.JsonObject listSavedWorlds() {
            com.google.gson.JsonArray worlds = new com.google.gson.JsonArray();

            com.google.gson.JsonObject w1 = new com.google.gson.JsonObject();
            w1.addProperty("name", "alpha");
            w1.addProperty("width", 10);
            w1.addProperty("height", 8);
            com.google.gson.JsonArray o1 = new com.google.gson.JsonArray();
            com.google.gson.JsonObject o1a = new com.google.gson.JsonObject();
            o1a.addProperty("x", 1);
            o1a.addProperty("y", 2);
            o1a.addProperty("width", 1);
            o1a.addProperty("height", 2);
            o1.add(o1a);
            w1.add("obstacles", o1);

            com.google.gson.JsonObject w2 = new com.google.gson.JsonObject();
            w2.addProperty("name", "beta");
            w2.addProperty("width", 20);
            w2.addProperty("height", 20);
            w2.add("obstacles", new com.google.gson.JsonArray());

            worlds.add(w1);
            worlds.add(w2);

            com.google.gson.JsonObject wrapper = new com.google.gson.JsonObject();
            wrapper.add("worlds", worlds);
            return wrapper;
        }

        @Override
        public com.google.gson.JsonObject getWorldByName(String name) {
            com.google.gson.JsonObject notFound = new com.google.gson.JsonObject();
            notFound.addProperty("error", "World not found");
            notFound.addProperty("name", name);
            return notFound;
        }
    }
}


