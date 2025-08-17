package apiTests;

import org.junit.jupiter.api.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class GetRequestTests {
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
        {
        }
    }
}
