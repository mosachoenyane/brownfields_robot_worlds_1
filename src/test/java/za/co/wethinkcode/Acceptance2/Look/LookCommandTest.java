package za.co.wethinkcode.Acceptance2.Look;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeEach;
import za.co.wethinkcode.client.RobotWorldClient;
import za.co.wethinkcode.client.RobotWorldJsonClient;
import za.co.wethinkcode.server.model.Position;

public class LookCommandTest {
    private final static int defaultPort = 5000;
    private final static String defaultIP = "localhost";
    private final static int worldSize = 2;
    private final static Position obstacle = new Position(2,1);
    private final RobotWorldClient serverClient = new RobotWorldJsonClient();

    @BeforeEach void connectToServer(){
        serverClient.connect(defaultIP, defaultPort);
    }
}
