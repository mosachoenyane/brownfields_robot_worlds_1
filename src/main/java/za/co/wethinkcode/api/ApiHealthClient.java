package za.co.wethinkcode.api;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Simple client for checking the health of an API.
 * This client sends a @code GET health request to a given base URL
 * and evaluates whether the API is up and responsive.
 */
public class ApiHealthClient {
    private final HttpClient client;

    /**
     * Creates a new {@code ApiHealthClient} with a default {@link HttpClient}.
     * The client is configured with a connection timeout of 3 seconds.
     */
    public ApiHealthClient() {
        this.client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(3))
                .build();
    }

    /**
     * Checks whether the API at the given base URL is up and healthy.
     *
     * This method performs a {@code GET} request to {@code baseUrl + "/health"}.
     * It considers the API healthy if:
     * <ul>
     *   <li>The response status code is {@code 200}</li>
     *   <li>The response body is not {@code null}</li>
     *   <li>The response body contains the string {@code "UP"}</li>
     * </ul>
     *
     * @param baseUrl the base URL of the API (e.g., {@code http://localhost:7000})
     * @return {@code true} if the API is up and healthy, {@code false} otherwise
     */
    public boolean isUp(String baseUrl) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/health"))
                    .timeout(Duration.ofSeconds(3))
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200 && response.body() != null && response.body().contains("\"UP\"");
        } catch (Exception e) {
            return false;
        }
    }
}
