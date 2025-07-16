package za.co.wethinkcode.Acceptance2.Movement;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import za.co.wethinkcode.client.RobotWorldClient;
import za.co.wethinkcode.client.RobotWorldJsonClient;

import static org.junit.jupiter.api.Assertions.*;

public class MoveForwardTest {
    private final static int defaultPort = 5000;
    private final static String defaultIP = "localhost";
    private final RobotWorldClient serverClient = new RobotWorldJsonClient();

    /**
     * Before everything, we connect to the server.
     */
    @BeforeEach
    void connectToServer() {
        serverClient.connect(defaultIP, defaultPort);
    }

    /**
     * after running all the tests, make sure we disconnect from the server.
     */
    @AfterEach
    void disconnectFromServer() {
        serverClient.disconnect();
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
}
