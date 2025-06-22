package za.co.wethinkcode.client;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import za.co.wethinkcode.server.world.WorldConfig;
import za.co.wethinkcode.client.commands.TurnRightCommand;
import za.co.wethinkcode.server.model.Robot;
import za.co.wethinkcode.server.model.Direction;
import za.co.wethinkcode.server.model.Position;
import za.co.wethinkcode.server.world.World;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

class TurnRightCommandTest {
    private World world;
    private Robot robot;
    private TurnRightCommand command;

    @BeforeEach
    void setUp() {
        world = new World(new WorldConfig());
        robot = new Robot("testBot", new Position(0, 0));
        command = new TurnRightCommand(world, robot);
    }

    @Test
    void getNameShouldReturnRight() {
        assertEquals("right", command.getName());
    }

    @Test
    void displayShouldReturnTurnedRight() {
        assertEquals("Turned right", command.display());
    }

    @Test
    void executeShouldTurnNorthToEast() {
        robot.setDirection(Direction.NORTH);
        String response = command.execute();
        assertEquals(Direction.EAST, robot.getDirection());
        verifyResponse(response, Direction.EAST);
    }

    @Test
    void executeShouldTurnEastToSouth() {
        robot.setDirection(Direction.EAST);
        String response = command.execute();
        assertEquals(Direction.SOUTH, robot.getDirection());
        verifyResponse(response, Direction.SOUTH);
    }

    @Test
    void executeShouldTurnSouthToWest() {
        robot.setDirection(Direction.SOUTH);
        String response = command.execute();
        assertEquals(Direction.WEST, robot.getDirection());
        verifyResponse(response, Direction.WEST);
    }

    @Test
    void executeShouldTurnWestToNorth() {
        robot.setDirection(Direction.WEST);
        String response = command.execute();
        assertEquals(Direction.NORTH, robot.getDirection());
        verifyResponse(response, Direction.NORTH);
    }

    private void verifyResponse(String jsonResponse, Direction expectedDirection) {
        JsonObject response = JsonParser.parseString(jsonResponse).getAsJsonObject();

        // Verify result
        assertEquals("OK", response.get("result").getAsString());

        // Verify data message
        JsonObject data = response.getAsJsonObject("data");
        assertEquals("Done", data.get("message").getAsString());

        // Verify state direction
        JsonObject state = response.getAsJsonObject("state");
        assertEquals(expectedDirection.toString(), state.get("direction").getAsString());

        // Verify state contains other required fields
        assertTrue(state.has("position"));
        assertTrue(state.has("shields"));
        assertTrue(state.has("shots"));
        assertTrue(state.has("status"));
    }
}