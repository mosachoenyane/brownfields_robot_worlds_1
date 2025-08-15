package za.co.wethinkcode.server.commands;

import za.co.wethinkcode.server.model.Robot;
import za.co.wethinkcode.server.world.World;

/**
 * Command to purge a specific robot by name, killing it and
 * removing it permanently from the game world.
 * Intended for server console use only.
 */

public class PurgeCommand   implements Command {
    private final World world;
    private final String robotName;

    /**
     * Constructs a PurgeCommand with the given world and the name of the robot to purge.
     *
     * @param world     the game world
     * @param robotName the name of the robot to purge
     */
    public PurgeCommand(World world, String robotName) {
        this.world = world;
        this.robotName = robotName;
    }

    /**
     * Executes the purge command by finding the specified robot,
     * killing it, and removing it from the world.
     *
     * @return a message indicating the outcome of the purge operation
     */
    @Override
    public String execute() {
        Robot robotToPurge = world.getRobotByName(robotName);

        if (robotToPurge == null) {
            return "ERROR: Robot '" + robotName + "' not found.";
        }
        // Kill the robot
        robotToPurge.kill();
        // Remove the robot from the world's list of robots
        world.removeRobot(robotToPurge);

        return "OK: Robot '" + robotName + "' has been purged from the world.";
    }

    /**
     * Returns the name of the command.
     *
     * @return the command name "purge"
     */
    @Override
    public String getName() {
        return "purge";
    }

    /**
     * Returns a short description of the command.
     *
     * @return the command usage string
     */
    @Override
    public String display() {
        return "purge <robot name> - Kill and permanently remove a robot from the game";
    }
}
