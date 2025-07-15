package za.co.wethinkcode.Acceptance2.Look;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import za.co.wethinkcode.client.RobotWorldClient;
import za.co.wethinkcode.client.RobotWorldJsonClient;
import za.co.wethinkcode.server.model.Position;

import static org.junit.jupiter.api.Assertions.*;

public class LookCommandTest {
    private final static int defaultPort = 5000;
    private final static String defaultIP = "localhost";
    private final static int worldSize = 2;
    private final static Position obstacle = new Position(2,1);
    private final RobotWorldClient serverClient = new RobotWorldJsonClient();

    /**
     * Before everything we connect to the server
     */
    @BeforeEach void connectToServer(){
        serverClient.connect(defaultIP, defaultPort);
    }

    /**
     * After running all the tests make sure we disconnect
     * from the servers
     */
    @AfterEach void disconnectTheServer(){
        serverClient.disconnect();
    }

    @Test void lookRobotWithObstacle(){
        assertTrue(serverClient.isConnected());
        String launchRequest = "{" +
                "  \"robot\": \"HAL\"," +
                "  \"command\": \"launch\"," +
                "  \"arguments\": [\"shooter\",\"5\",\"5\"]" +
                "}";
        JsonNode launchResponse = serverClient.sendRequest(launchRequest);
        assertNotNull(launchResponse);

    }
}
