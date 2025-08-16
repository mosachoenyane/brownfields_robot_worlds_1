package za.co.wethinkcode.client;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import za.co.wethinkcode.server.world.WorldConfig;
import za.co.wethinkcode.client.commands.TurnLeftCommand;
import za.co.wethinkcode.server.model.Robot;
import za.co.wethinkcode.server.model.Direction;
import za.co.wethinkcode.server.model.Position;
import za.co.wethinkcode.server.world.World;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

class TurnLeftCommandTest {
    private World world;
    private Robot robot;
    private TurnLeftCommand command;

    @BeforeEach
    void setUp() {
        //WorldConfig conf = new WorldConfig();
        world = new World(new WorldConfig());
        robot = new Robot("testBot", new Position(0, 0));
        command = new TurnLeftCommand(world, robot);
    }

    @Test
    void getNameShouldReturnLeft() {
        assertEquals("left", command.getName());
    }

    @Test
    void displayShouldReturnTurnedLeft() {
        assertEquals("Turned left", command.display());
    }

    @Test
    void executeShouldTurnNorthToWest() {
        robot.setDirection(Direction.NORTH);
        String response = command.execute();
        assertEquals(Direction.WEST, robot.getDirection());
        verifyResponse(response, Direction.WEST);
    }

    @Test
    void executeShouldTurnWestToSouth() {
        robot.setDirection(Direction.WEST);
        String response = command.execute();
        assertEquals(Direction.SOUTH, robot.getDirection());
        verifyResponse(response, Direction.SOUTH);
    }

    @Test
    void executeShouldTurnSouthToEast() {
        robot.setDirection(Direction.SOUTH);
        String response = command.execute();
        assertEquals(Direction.EAST, robot.getDirection());
        verifyResponse(response, Direction.EAST);
    }

    @Test
    void executeShouldTurnEastToNorth() {
        robot.setDirection(Direction.EAST);
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