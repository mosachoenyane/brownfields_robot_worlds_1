package za.co.wethinkcode.Acceptance2.Launch;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import za.co.wethinkcode.client.RobotWorldClient;
import za.co.wethinkcode.client.RobotWorldJsonClient;
import static org.junit.jupiter.api.Assertions.assertEquals;

    /**
     * As a player,
     * I want to launch my robot in the online robot world
     * So that I can break the record for the most robot kills
     */
    class LaunchRobotTests2 {
        private final static int DEFAULT_PORT = 5000;
        private final static String DEFAULT_IP = "localhost";
        private final RobotWorldClient serverClient = new RobotWorldJsonClient();

        @BeforeEach
        void connectToServer(){
            serverClient.connect(DEFAULT_IP, DEFAULT_PORT);
        }

        @AfterEach
        void disconnectFromServer(){
            serverClient.disconnect();
        }
        @Test
        void validLaunchShouldSucceed() {
            // Given that I am connected to a running Robot Worlds server
            // And the world is of size 1x1 (The world is configured or hardcoded to this size)
            assert serverClient.isConnected();

            // When I send a valid launch request to the server
            String request = "{" +
                    "  \"robot\": \"HAL\"," +
                    "  \"command\": \"launch\"," +
                    "  \"arguments\": [\"shooter\",\"5\",\"5\"]" +
                    "}";
            var response = serverClient.sendRequest(request);

            // Then I should get a valid response from the server
            assertEquals("OK", response.get("result").asText());

            // And the position should be (x:0, y:0)
            assertEquals(0, response.get("data").get("position").get(0).asInt());
            assertEquals(0, response.get("data").get("position").get(1).asInt());

            // And I should also get the state of the robot
            assert response.has("state");
        }

        @Test
        void invalidLaunchShouldFail(){
            // Given that, I am connected to a running Robot Worlds server
            assert serverClient.isConnected();

            // When I send an invalid launch request with the command "luanch" instead of "launch"
            String request = "{" +
                    "  \"robot\": \"HAL\"," +
                    "  \"command\": \"luanch\"," +
                    "  \"arguments\": [\"shooter\",\"5\",\"5\"]" +
                    "}";
            var response = serverClient.sendRequest(request);

            // Then I should get an error response from the server
            assertEquals("ERROR", response.get("result").asText());
        }

        @Test
        void launchRobotWithObstacle(){
            // Given that I am connected to a running Robot Worlds server and a world of size 2x2
            // AND the world has an obstacle at coordinate [1,1]
            // WHEN I launch 8 robots into the world each robot cannot be in position [1,1].
        }
    }

