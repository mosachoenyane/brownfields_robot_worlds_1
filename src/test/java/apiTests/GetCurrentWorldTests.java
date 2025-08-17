package apiTests;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class GetCurrentWorldTests {
    private TestWebApi api;
    private HttpClient client;

    @BeforeAll
    void startApi() {
        api = new TestWebApi(new TestWebApi.FakeWorldApp());
        api.start();
        client = HttpClient.newHttpClient();
    }

    @AfterAll
    void stopApi() {
        api.stop();
    }

    @Test
    @DisplayName("GET/world returns data of the world is currently running")
    void getCurrentWorldRequest() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(api.baseUrl() + "/world"))
                .GET()
                .build();

        HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, res.statusCode());

        // Basic header check (content type)
        assertTrue(res.headers().firstValue("Content-Type").orElse("").contains("application/json"));

        // Check if JSON is returned
        JsonObject root = JsonParser.parseString(res.body()).getAsJsonObject();

        assertTrue(root.has("result"));
        assertEquals("OK", root.get("result").getAsString());
        assertTrue(root.has("data"));
        assertTrue(!root.has("error"));

        JsonObject data = root.getAsJsonObject("data");
        assertTrue(data.has("name"));
        assertTrue(data.get("name").isJsonPrimitive());
        assertTrue(data.has("width"));
        assertTrue(data.get("width").isJsonPrimitive());
        assertTrue(data.get("width").getAsJsonPrimitive().isNumber());
        assertTrue(data.has("height"));
        assertTrue(data.get("height").isJsonPrimitive());
        assertTrue(data.get("height").getAsJsonPrimitive().isNumber());
        assertTrue(data.has("obstacles"));
        assertTrue(data.get("obstacles").isJsonArray());
        assertTrue(data.has("robots"));
        assertTrue(data.get("robots").isJsonArray());

        // Validate the OBSTACLE DATA of the world
        JsonArray obstacles = data.getAsJsonArray("obstacles");
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

