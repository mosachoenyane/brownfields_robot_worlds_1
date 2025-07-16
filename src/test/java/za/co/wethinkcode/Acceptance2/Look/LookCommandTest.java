package za.co.wethinkcode.Acceptance2.Look;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import za.co.wethinkcode.client.RobotWorldClient;
import za.co.wethinkcode.client.RobotWorldJsonClient;

import static org.junit.jupiter.api.Assertions.*;

public class LookCommandTest {
    private final static int defaultPort = 5000;
    private final static String defaultIP = "localhost";
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

    /**
     * TESTING robot looks at the world and sees an obstacle
     */
    @Test void lookRobotWithObstacle(){
        assertTrue(serverClient.isConnected());
        String launchRequest = "{" +
                "  \"robot\": \"HAL\"," +
                "  \"command\": \"launch\"," +
                "  \"arguments\": [\"shooter\",\"5\",\"5\"]" +
                "}";
        JsonNode launchResponse = serverClient.sendRequest(launchRequest);
        assertNotNull(launchResponse);
        assertEquals("OK", launchResponse.get("result").asText());

        String lookRequest = "{" +
                "  \"robot\": \"HAL\"," +
                "  \"command\": \"look\"" +
                "}";
        JsonNode lookResponse = serverClient.sendRequest(lookRequest);
        assertNotNull(lookResponse);
        assertEquals("OK", lookResponse.get("result").asText());

        JsonNode objects = lookResponse.get("data").get("objects");
        boolean seeObstacle = true;

        for (JsonNode i : objects){
            if("OBSTACLE".equalsIgnoreCase(i.get("type").asText())){
                int x =i.get("position").get(0).asInt();
                int y = i.get("position").get(1).asInt();

                if (x == 2 && y== 1){
                    break;
                }
            }
        }
        assertTrue(seeObstacle, "Robot sees an obstacle at (2, 1)");
    }

    @Test void lookAnotherRobotTest(){
        assertTrue(serverClient.isConnected());
    }
}
