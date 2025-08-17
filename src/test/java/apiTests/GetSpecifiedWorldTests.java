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
public class GetSpecifiedWorldTests {
    private TestWebApi api;
    private HttpClient client;

    @BeforeAll
    void startApi() {
        api = new TestWebApi(new ByNameCapableWorldApp());
        api.start();
        client = HttpClient.newHttpClient();
    }

    @AfterAll
    void stopApi() {
        api.stop();
    }
}