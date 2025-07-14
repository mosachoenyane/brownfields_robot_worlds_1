package za.co.wethinkcode.Acceptance2.Launch;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import za.co.wethinkcode.client.RobotWorldClient;
import za.co.wethinkcode.client.RobotWorldJsonClient;
import za.co.wethinkcode.server.model.Position;

import static org.junit.jupiter.api.Assertions.*;

    /**
     * As a player,
     * I want to launch my robot in the online robot world
     * So that I can break the record for the most robot kills
     */
    public class LaunchRobotTests2 {
        private final static int DEFAULT_PORT = 5000;
        private final static String DEFAULT_IP = "localhost";
        private final static int WORLD_SIZE = 2; // A 2x2 world
        private final static Position obstaclePosition = new Position(1, 1); // Obstacle at [1,1]
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
        void launchOneRobotWithObstacle(){
            // Given that I am connected to a running Robot Worlds server and a world of size 2x2
            // AND the world has an obstacle at coordinate [1,1]
            // WHEN I launch 8 robots into the world each robot cannot be in position [1,1].
            assertTrue(serverClient.isConnected());

            // When I send a valid launch request to the server
            String request = "{" +
                    "  \"robot\": \"HAL\"," +
                    "  \"command\": \"launch\"," +
                    "  \"arguments\": [\"shooter\",\"5\",\"5\"]" +
                    "}";
            JsonNode response = serverClient.sendRequest(request);

            // Then I should get a valid response from the server
            assertNotNull(response.get("result"));
            assertEquals("OK", response.get("result").asText());

            // And the position should be (x:0, y:0)
            assertNotNull(response.get("data"));
            assertNotNull(response.get("data").get("position"));
            assertEquals(0, response.get("data").get("position").get(0).asInt());
            assertEquals(0, response.get("data").get("position").get(1).asInt());

            // And I should also get the state of the robot
            assertNotNull(response.get("state"));

        }
        @Test
        void launchEightRobotsWithObstacle() {
            // Given that I am connected to a running Robot Worlds server and a world of size 2x2
            // AND the world has an obstacle at coordinate [1,1]
            // WHEN I launch 8 robots into the world each robot cannot be in position [1,1].
            assertTrue(serverClient.isConnected());

            // When I send a valid launch request to the server
            String request1 = "{" +
                    "  \"robot\": \"HAL\"," + "  \"command\": \"launch\"," + "  \"arguments\": [\"shooter\",\"5\",\"5\"]" + "}";
            String request2 = "{" +
                    "  \"robot\": \"BEN\"," + "  \"command\": \"launch\"," + "  \"arguments\": [\"shooter\",\"5\",\"5\"]" + "}";
            String request3 = "{" +
                    "  \"robot\": \"SAM\"," + "  \"command\": \"launch\"," + "  \"arguments\": [\"shooter\",\"5\",\"5\"]" + "}";
            String request4 = "{" +
                    "  \"robot\": \"WAZZ\"," + "  \"command\": \"launch\"," + "  \"arguments\": [\"shooter\",\"5\",\"5\"]" + "}";
            String request5 = "{" +
                    "  \"robot\": \"JOBZ\"," + "  \"command\": \"launch\"," + "  \"arguments\": [\"shooter\",\"5\",\"5\"]" + "}";
            String request6 = "{" +
                    "  \"robot\": \"REITU\"," + "  \"command\": \"launch\"," + "  \"arguments\": [\"shooter\",\"5\",\"5\"]" + "}";
            String request7 = "{" +
                    "  \"robot\": \"MOSA\"," + "  \"command\": \"launch\"," + "  \"arguments\": [\"shooter\",\"5\",\"5\"]" + "}";
            String request8 = "{" +
                    "  \"robot\": \"BENJI\"," + "  \"command\": \"launch\"," + "  \"arguments\": [\"shooter\",\"5\",\"5\"]" + "}";

            JsonNode response1 = serverClient.sendRequest(request1);
            JsonNode response2 = serverClient.sendRequest(request2);
            JsonNode response3 = serverClient.sendRequest(request3);
            JsonNode response4 = serverClient.sendRequest(request4);
            JsonNode response5 = serverClient.sendRequest(request5);
            JsonNode response6 = serverClient.sendRequest(request6);
            JsonNode response7 = serverClient.sendRequest(request7);
            JsonNode response8 = serverClient.sendRequest(request8);


            // Then I should get a valid response from the server
            assertNotNull(response8.get("result"));
            assertEquals("OK", response8.get("result").asText());

            // And the position should be (x:0, y:0)
            assertNotNull(response8.get("data"));
            assertNotNull(response8.get("data").get("position"));

            // And I should also get the state of the robot
            assertNotNull(response8.get("state"));

            // Check that all robots are not launched at the obstacle position
            assertNotEquals(obstaclePosition.getX(), response1.get("data").get("position").get(0).asInt());
            assertNotEquals(obstaclePosition.getY(), response1.get("data").get("position").get(1).asInt());
        }
        @Test
        void LaunchShouldSucceed() {
            // Given that I am connected to a running Robot Worlds server
            // And the world is of size 1x1 (The world is configured or hardcoded to this size)
            assertTrue(serverClient.isConnected());

            // When I send a valid launch request to the server
            String request = "{" +
                    "  \"robot\": \"HAL\"," +
                    "  \"command\": \"launch\"," +
                    "  \"arguments\": [\"shooter\",\"5\",\"5\"]" +
                    "}";
            JsonNode response = serverClient.sendRequest(request);

            // Then I should get a valid response from the server
            assertNotNull(response.get("result"));
            assertEquals("OK", response.get("result").asText());

            // And the position should be (x:0, y:0)
            assertNotNull(response.get("data"));
            assertNotNull(response.get("data").get("position"));
            assertEquals(0, response.get("data").get("position").get(0).asInt());
            assertEquals(0, response.get("data").get("position").get(1).asInt());

            // And I should also get the state of the robot
            assertNotNull(response.get("state"));
        }



    }

