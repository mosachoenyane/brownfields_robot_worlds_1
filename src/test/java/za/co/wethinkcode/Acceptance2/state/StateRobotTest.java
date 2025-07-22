package za.co.wethinkcode.Acceptance2.state;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import za.co.wethinkcode.client.RobotWorldClient;
import za.co.wethinkcode.client.RobotWorldJsonClient;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

public class StateRobotTest {

    private final static int DEFAULT_PORT = 5000;
    private final static String DEFAULT_IP = "localhost";
    private final RobotWorldClient serverClient = new RobotWorldJsonClient();
    Process process;

    @BeforeEach
    void connectToServer() throws IOException, InterruptedException {
//        String path = Files.readString(Paths.get("src/main/resources/serverName")).trim();
//        ProcessBuilder pb = new ProcessBuilder("java", "-jar", path,"-s","2","-o","1,1");
//        pb.inheritIO(); // Inherit standard input/output/error streams
//        process = pb.start();
//        Thread.sleep(1000);
        serverClient.connect(DEFAULT_IP, DEFAULT_PORT);
    }

    @AfterEach
    void disconnectFromServer() throws InterruptedException {
        serverClient.disconnect();
//        process.destroy();
//        Thread.sleep(1000);
    }
    @Test
    void testGetRobotState() {
        // Given I am connected to a running Robot Worlds server
        assertTrue(serverClient.isConnected(), "Should be connected to server");

        // And my robot exists in a 2x2 world
        String launchRequest = "{" +
                "\"robot\": \"HAL\"," +
                "\"command\": \"launch\"," +
                "\"arguments\": [\"shooter\",\"5\",\"5\"]" +
                "}";
        JsonNode launchResponse = serverClient.sendRequest(launchRequest);
        //System.out.println("Launch Response: " + launchResponse.toString());
        assertNotNull(launchResponse.get("result"), "Launch response result is null");
        assertEquals("OK", launchResponse.get("result").asText(), "Launch failed");

        // When I send a valid STATE request to the server
        String stateRequest = "{" +
                "\"robot\": \"HAL\"," +
                "\"command\": \"state\"," +
                "\"arguments\": []" +
                "}";
        JsonNode response = serverClient.sendRequest(stateRequest);
        //System.out.println("State Response: " + response.toString());

        // Then I should get a valid response from the server
        assertNotNull(response.get("result"), "Response result is null");
        assertEquals("OK", response.get("result").asText(), "State request failed");

        // And I should get the robot's Position
        assertNotNull(response.get("state"), "Response state is null");
        assertTrue(response.get("state").has("position"), "Position field missing in response state");
        assertTrue(response.get("state").get("position").isArray(), "Position is not an array");
        assertEquals(2, response.get("state").get("position").size(), "Position array does not have 2 elements");

        // And I should get the robot's Direction
        assertTrue(response.get("state").has("direction"), "Direction field missing in response state");
        assertTrue(response.get("state").get("direction").isTextual(), "Direction is not a string");

        // And I should get the robot's Shields
        assertTrue(response.get("state").has("shields"), "Shields field missing in response state");
        assertTrue(response.get("state").get("shields").isNumber(), "Shields is not a number");

        // And I should get the robot's Shots
        assertTrue(response.get("state").has("shots"), "Shots field missing in response state");
        assertTrue(response.get("state").get("shots").isNumber(), "Shots is not a number");

        // And I should get the robot's Status
        assertTrue(response.get("state").has("status"), "Status field missing in response");
        assertTrue(response.get("state").get("status").isTextual(), "Status is not a string");
    }
    @Test
    void invalidStateRequestShouldFail(){
        // Given that I am connected to a running Robot Worlds server
        assertTrue(serverClient.isConnected());
        // When I send an invalid state request with incorrect command
        String stateRequest = "{" +
                "\"robot\": \"HAL\"," +
                "\"command\": \"staet\"," +
                "\"arguments\": []" +
                "}";
        JsonNode response = serverClient.sendRequest(stateRequest);

        System.out.println("Invalid State Response: " + response.toString());
        // Then I should get an error response
        assertNotNull(response.get("result"));
        assertEquals("ERROR", response.get("result").asText());
        // And the message "Unsupported command"
        assertNotNull(response.get("data"));
        assertNotNull(response.get("data").get("message"));
        assertTrue(response.get("data").get("message").asText().contains("Unsupported command"));
    }

}
