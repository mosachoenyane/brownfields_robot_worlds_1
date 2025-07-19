package za.co.wethinkcode.Acceptance2.Movement;

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

public class MoveForwardTest {
    private final static int defaultPort = 5000;
    private final static String defaultIP = "localhost";
    private final RobotWorldClient serverClient = new RobotWorldJsonClient();

    Process process;


    @BeforeEach
    void connectToServer() throws IOException, InterruptedException {
        String path = Files.readString(Paths.get("src/main/resources/serverName")).trim();
        ProcessBuilder pb = new ProcessBuilder("java", "-jar", path);
        pb.inheritIO(); // Inherit standard input/output/error streams
        process = pb.start();
        Thread.sleep(1000);
        serverClient.connect(defaultIP, defaultPort);
    }

    @AfterEach
    void disconnectFromServer() throws InterruptedException {
        serverClient.disconnect();
        process.destroy();
        Thread.sleep(1000);

    }

    /**
     * Test moving the robot forward.
     * Launch the robot and move it forward by 5 steps.
     */
    @Test
    void testMoveForward() {
        assertTrue(serverClient.isConnected(), "Client should be connected");

        // Launch robot
        String launchRequest = "{" +
                "  \"robot\": \"HAL\"," +
                "  \"command\": \"launch\"," +
                "  \"arguments\": [\"shooter\",\"5\",\"5\"]" +
                "}";
        JsonNode launchResponse = serverClient.sendRequest(launchRequest);
        assertNotNull(launchResponse);
        assertEquals("OK", launchResponse.get("result").asText(), "Launch should return OK");

        // Move forward 5 steps
        String forwardRequest = "{" +
                "  \"robot\": \"HAL\"," +
                "  \"command\": \"forward\"," +
                "  \"arguments\": [\"5\"]" +
                "}";
        JsonNode forwardResponse = serverClient.sendRequest(forwardRequest);
        assertNotNull(forwardResponse);
        assertEquals("OK", forwardResponse.get("result").asText(), "Forward should return OK");

        // Optional: Print or assert position
        JsonNode position = forwardResponse.get("state").get("position");
        System.out.println("New robot position: " + position);
        assertNotNull(position, "Position should be present in state");
    }

    @Test
    void testMoveBackward() {
        assertTrue(serverClient.isConnected(), "Client should be connected");

        // Launch robot
        String launchRequest = "{" +
                "  \"robot\": \"BENJI\"," +
                "  \"command\": \"launch\"," +
                "  \"arguments\": [\"shooter\",\"5\",\"5\"]" +
                "}";
        JsonNode launchResponse = serverClient.sendRequest(launchRequest);
        assertNotNull(launchResponse);
        assertEquals("OK", launchResponse.get("result").asText(), "Launch should return OK");

        // Move backward 5 steps
        String backwardRequest = "{" +
                "  \"robot\": \"BENJI\"," +
                "  \"command\": \"forward\"," +
                "  \"arguments\": [\"5\"]" +
                "}";
        JsonNode backwardResponse = serverClient.sendRequest(backwardRequest);
        assertNotNull(backwardResponse);
        assertEquals("OK", backwardResponse.get("result").asText(), "Backward should return OK");

        JsonNode position = backwardResponse.get("state").get("position");
        System.out.println("New robot position: " + position);
        assertNotNull(position, "Robot position should change according to the steps");
        assertEquals("NORTH", backwardResponse.get("state").get("direction").asText(), "Direction should remain North after moving backward");
    }
}
