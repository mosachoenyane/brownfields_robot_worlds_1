package za.co.wethinkcode.protocol;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import za.co.wethinkcode.client.RobotWorldClient;
import za.co.wethinkcode.client.RobotWorldJsonClient;

import static org.junit.jupiter.api.Assertions.*;

public class LookCommandTest {
    private final static String ipAddress = "localhost";
    private final static int portNumber = 5000;
    private final RobotWorldClient serverClient = new RobotWorldJsonClient();

    @BeforeEach void ConnetToServer(){
        serverClient.connect(ipAddress, portNumber);
    }

    @AfterEach void disconnectTheServer(){
        serverClient.disconnect();
    }

    @Test void lookEmptyWorld(){
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

        assertNotNull(lookResponse.get("data"));
        JsonNode objects = lookResponse.get("data").get("objects");
        assertNotNull(objects);
        assertTrue(objects.isArray());
        assertEquals(4, objects.size(), "Expected no objects in view in an empty world");
    }
}
