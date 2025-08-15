package za.co.wethinkcode.server;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import za.co.wethinkcode.server.commands.PurgeCommand;
import za.co.wethinkcode.server.model.Position;
import za.co.wethinkcode.server.model.Robot;
import za.co.wethinkcode.server.world.World;
import za.co.wethinkcode.server.world.WorldConfig;

import static org.junit.jupiter.api.Assertions.*;

public class PurgeCommandTest {
    private World mockWorld;
    private Robot mockRobot;
    private PurgeCommand purgeCommand;

    private World createTestWorld(int width, int height) {
        WorldConfig config = new WorldConfig() {{
            properties.setProperty("WORLD_WIDTH",  String.valueOf(width));
            properties.setProperty("WORLD_HEIGHT", String.valueOf(height));
            properties.setProperty("NUM_PITS",      "0");
            properties.setProperty("NUM_MOUNTAINS","0");
            properties.setProperty("NUM_LAKES",     "0");
            properties.setProperty("VISIBILITY_RANGE","5");
            properties.setProperty("MAX_SHIELD_STRENGTH","5");
            properties.setProperty("REPAIR_TIME","5");
            properties.setProperty("RELOAD_TIME","5");
        }};
        return new World(config);
    }

    @BeforeEach
    void setUp() {
        mockWorld = createTestWorld(10, 5);
        mockRobot = new Robot("TestRobot",new Position(1,1));
        mockWorld.addRobot(mockRobot);
        mockRobot = mockWorld.getRobotByName("TestRobot");
        purgeCommand = new PurgeCommand(mockWorld, "TestRobot");
    }

    @Test
    void shouldPurgeExistingRobot() {

        String result = purgeCommand.execute();

        assertEquals("OK: Robot 'TestRobot' has been purged from the world.", result, "Should return a success message.");

        assertFalse(mockRobot.isAlive(), "The robot should be dead after purging.");

        assertNull(mockWorld.getRobotByName("TestRobot"), "The robot should be removed from the world.");
    }

}
