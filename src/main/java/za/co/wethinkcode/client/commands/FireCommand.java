package za.co.wethinkcode.client.commands;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import za.co.wethinkcode.server.commands.Command;
import za.co.wethinkcode.server.model.Direction;
import za.co.wethinkcode.server.model.Position;
import za.co.wethinkcode.server.model.Robot;
import za.co.wethinkcode.server.utils.ShootingValidator;
import za.co.wethinkcode.server.world.World;

/**
 * A command that allows the robot to fire a gun in its current direction.
 * Calculates firing range based on remaining shots and checks for targets along the path.
 */

public class FireCommand implements Command {
    private final World world;
    private final Robot robot;
    private final int shotDistance;

    /**
     * Constructs a {@code FireCommand}.
     *
     * @param world the world in which the robot operates
     * @param robot the robot executing the command
     */

    public FireCommand(World world, Robot robot) {
        this.world = world;
        this.robot = robot;
        this.shotDistance = calculateShotDistance(robot.getShots());
    }

    /**
     * Executes the fire command.
     * Decreases shot count, checks for hits, and returns a result.
     *
     * @return a JSON string representing the result of the command
     */

    @Override
    public String execute() {
        if (robot.getShots() <= 0) {
            return createErrorResponse("No shots available");
        }

        robot.setShots(robot.getShots() - 1);

        if (shotDistance == 0) {
            return createErrorResponse("Gun not configured for firing");
        }

        Robot hitRobot = checkForHit();
        return hitRobot != null ? createHitResponse(hitRobot) : createMissResponse();
    }

    /**
     * Checks whether firing the gun hits another robot within the firing range.
     *
     * @return the {@link Robot} that was hit, or {@code null} if no robot was hit
     */
    protected Robot checkForHit() {
        Position currentPos = robot.getPosition();
        Direction direction = robot.getDirection();

        for (int step = 1; step <= shotDistance; step++) {
            Position checkPos = calculatePositionInDirection(currentPos, direction, step);

            if (ShootingValidator.isBlocked(checkPos, world)) {
                return null;
            }

            // Check for robots in this position
            for (Robot other : world.getRobots()) {
                if (!other.equals(robot) && other.getPosition().equals(checkPos)) {
                    return other;
                }
            }
        }
        return null;
    }

    /**
     * Calculates the position a number of steps away in a given direction.
     *
     * @param start     starting {@link Position}
     * @param direction {@link Direction} to move
     * @param steps     number of steps to move
     * @return the calculated {@link Position}
     */
    private Position calculatePositionInDirection(Position start, Direction direction, int steps) {
        int newX = start.getX();
        int newY = start.getY();

        switch (direction) {
            case NORTH: newY -= steps; break;
            case EAST: newX += steps; break;
            case SOUTH: newY += steps; break;
            case WEST: newX -= steps; break;
        }

        return new Position(newX, newY);
    }

    /**
     * Creates a JSON response for a successful hit.
     *
     * @param hitRobot the {@link Robot} that was hit
     * @return JSON string describing the hit and updated robot states
     */
    private String createHitResponse(Robot hitRobot) {
        hitRobot.takeHit();

        JsonObject response = new JsonObject();
        response.addProperty("result", "OK");

        JsonObject data = new JsonObject();
        data.addProperty("message", "Hit");
        data.addProperty("distance", calculateDistance(robot.getPosition(), hitRobot.getPosition()));
        data.addProperty("robot", hitRobot.getName());
        data.add("state", createStateJson(hitRobot));
        response.add("data", data);

        JsonObject state = new JsonObject();
        state.addProperty("shots", robot.getShots());
        response.add("state", state);

        return response.toString();
    }

    /**
     * Creates a JSON representation of a robot's current state.
     *
     * @param robot the {@link Robot} to serialize
     * @return {@link JsonObject} representing the robot's state
     */
    private JsonObject createStateJson(Robot robot) {
        JsonObject state = new JsonObject();
        state.add("position", new Gson().toJsonTree(
                new int[]{robot.getPosition().getX(), robot.getPosition().getY()}));
        state.addProperty("direction", robot.getDirection().name());
        state.addProperty("shields", robot.getShields());
        state.addProperty("shots", robot.getShots());
        state.addProperty("status", robot.getStatus().name());
        return state;
    }

    /**
     * Creates a JSON response for a missed shot.
     *
     * @return JSON string indicating the shot missed
     */
    private String createMissResponse() {
        JsonObject response = new JsonObject();
        response.addProperty("result", "OK");

        JsonObject data = new JsonObject();
        data.addProperty("message", "Miss");
        response.add("data", data);

        JsonObject state = new JsonObject();
        state.addProperty("shots", robot.getShots());
        response.add("state", state);

        return response.toString();
    }

    /**
     * Calculates Manhattan distance between two positions.
     *
     * @param pos1 first {@link Position}
     * @param pos2 second {@link Position}
     * @return distance as an integer
     */
    private int calculateDistance(Position pos1, Position pos2) {
        return Math.abs(pos1.getX() - pos2.getX()) + Math.abs(pos1.getY() - pos2.getY());
    }

    /**
     * Creates a JSON response for an error condition.
     *
     * @param message error message
     * @return JSON string with result "ERROR" and the message
     */
    private String createErrorResponse(String message) {
        JsonObject response = new JsonObject();
        response.addProperty("result", "ERROR");
        response.addProperty("message", message);
        return response.toString();
    }

    /**
     * Determines the effective firing distance based on remaining shots.
     *
     * @param shots the number of shots the robot has
     * @return maximum firing distance in steps
     */
    private int calculateShotDistance(int shots) {
        if (shots <= 0) return 0;
        if (shots >= 5) return 1;
        return 6 - shots;
    }

    /**
     * Returns the name of the command.
     *
     * @return the string {@code "fire"}
     */

    @Override
    public String getName() { return "fire"; }

    /**
     * Returns a human-readable description of the command.
     *
     * @return a string describing the fire action
     */

    @Override
    public String display() { return "FIRE " + robot.getName(); }
}