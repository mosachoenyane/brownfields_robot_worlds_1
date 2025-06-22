package za.co.wethinkcode.protocol;

import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import za.co.wethinkcode.client.commands.LookCommand;
import za.co.wethinkcode.client.commands.StateCommand;
import za.co.wethinkcode.protocol.client.LookResponse;
import za.co.wethinkcode.server.model.Position;
import za.co.wethinkcode.server.model.Robot;
import za.co.wethinkcode.server.model.Direction;

import static org.junit.jupiter.api.Assertions.*;

class LookResponseTest {
    private LookResponse lookResponse;
    private Robot testRobot;

    @BeforeEach
    void setUp() {
        // Create test robot
        testRobot = new Robot("TestBot", new Position(5, 5));
        testRobot.setDirection(Direction.NORTH);
        testRobot.setStatus(Robot.Status.NORMAL);

        // Create LookCommand with sample data
        LookCommand lookCommand = new LookCommand(5);
        lookCommand.addObject("N", "OBSTACLE", 2);
        lookCommand.addObject("NE", "ROBOT", 3);

        // Create StateCommand
        StateCommand stateCommand = new StateCommand(testRobot);

        // Create the response we want to test
        lookResponse = new LookResponse(lookCommand, stateCommand);
    }

    @Test
    void toJson_returnsValidStructure() {
        JsonObject response = lookResponse.toJson();

        // Verify top-level structure
        assertTrue(response.has("result"));
        assertTrue(response.has("data"));
        assertTrue(response.has("state"));

        assertEquals("OK", response.get("result").getAsString());
    }

    @Test
    void toJson_dataSectionContainsCorrectVisionData() {
        JsonObject data = lookResponse.toJson().getAsJsonObject("data");

        // Verify visibility range
        assertTrue(data.has("visibilityRange"));
        assertEquals(5, data.get("visibilityRange").getAsInt());

        // Verify objects array exists and has correct size
        assertTrue(data.has("objects"));
        JsonArray objects = data.getAsJsonArray("objects");
        assertEquals(2, objects.size());

        // Verify first object
        JsonObject firstObject = objects.get(0).getAsJsonObject();
        assertEquals("N", firstObject.get("direction").getAsString());
        assertEquals("OBSTACLE", firstObject.get("type").getAsString());
        assertEquals(2, firstObject.get("distance").getAsInt());

        // Verify second object
        JsonObject secondObject = objects.get(1).getAsJsonObject();
        assertEquals("NE", secondObject.get("direction").getAsString());
        assertEquals("ROBOT", secondObject.get("type").getAsString());
        assertEquals(3, secondObject.get("distance").getAsInt());
    }

    @Test
    void toJson_stateSectionContainsRobotState() {
        JsonObject state = lookResponse.toJson().getAsJsonObject("state");

        // Verify position - handling both array and string formats
        assertTrue(state.has("position"));
        JsonElement positionElement = state.get("position");

        if (positionElement.isJsonArray()) {
            JsonArray positionArray = positionElement.getAsJsonArray();
            assertEquals(2, positionArray.size());
            assertEquals(5, positionArray.get(0).getAsInt());
            assertEquals(5, positionArray.get(1).getAsInt());
        } else {
            assertEquals("[5,5]", positionElement.getAsString());
        }

        // Verify other state properties
        assertEquals("NORTH", state.get("direction").getAsString());
        assertEquals("NORMAL", state.get("status").getAsString());
    }
}