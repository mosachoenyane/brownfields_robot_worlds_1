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

        }
    }
}

