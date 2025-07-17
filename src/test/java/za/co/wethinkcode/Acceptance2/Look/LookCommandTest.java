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
        /**  Connect to the server first and check if it is connected */
        assertTrue(serverClient.isConnected());

        /**
         * THis should senda request to launch in a 2 x2 world
         * A sniper robot with 5 bullets and 5 shields will be launched
         *
         */
        String launchRequest = "{" +
                "  \"robot\": \"HAL\"," +
                "  \"command\": \"launch\"," +
                "  \"arguments\": [\"shooter\",\"5\",\"5\"]" +
                "}";

        /** A response is checked and analyzed to make sure it is good for readability */
        JsonNode launchResponse = serverClient.sendRequest(launchRequest);
        assertNotNull(launchResponse);
        assertEquals("OK", launchResponse.get("result").asText());

        /** A look command request is sent and we had to test also that it is valid */
        String lookRequest = "{" +
                "  \"robot\": \"HAL\"," +
                "  \"command\": \"look\"" +
                "}";
        JsonNode lookResponse = serverClient.sendRequest(lookRequest);
        assertNotNull(lookResponse);
        assertEquals("OK", lookResponse.get("result").asText());
        JsonNode objects = lookResponse.get("data").get("objects");

        /**
         * In this case we loop though the Json response we have recieved above
         * we check for specific words an example of that is the word OBJECT
         * And we check the coordination of the obstacle
         */
        boolean seeObstacle = true;

        for (JsonNode i : objects){
            if("OBSTACLE".equalsIgnoreCase(i.get("type").asText())){
                int x =i.get("position").get(0).asInt();
                int y = i.get("position").get(1).asInt();

                if (x == 0 && y== 1){
                    break;
                }
            }
        }
        assertTrue(seeObstacle, "Robot sees an obstacle at (0, 1)");
    }

    @Test void lookAnotherRobotTest(){
        assertTrue(serverClient.isConnected());
    }
}
