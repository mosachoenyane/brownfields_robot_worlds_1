package za.co.wethinkcode.server.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import za.co.wethinkcode.server.model.Direction;
import za.co.wethinkcode.server.model.Position;
import za.co.wethinkcode.server.model.Robot;
import za.co.wethinkcode.server.world.World;
import za.co.wethinkcode.server.world.obstacles.Obstacle;

import java.util.HashSet;
import java.util.Set;

/**
 * Provides vision functionality for a robot within the world.
 * Scans in all cardinal directions to detect obstacles, other robots, or world edges
 * within the robot's visibility range.
 */
public class VisionFinder {
    private final World world;
    private final Robot robot;

    /**
     * Creates a VisionFinder for the given world and robot.
     *
     * @param world The world in which the robot operates.
     * @param robot The robot whose vision is to be calculated.
     */
    public VisionFinder(World world, Robot robot) {
        this.world = world;
        this.robot = robot;
    }

    /**
     * Scans all four cardinal directions from the robot's position,
     * returning the first object detected (obstacle, robot, or edge) per direction.
     *
     * @param robot The robot for which to find visible objects.
     * @return A JsonObject containing visibility data, including detected objects and visibility range.
     */
    public JsonObject findInAbsoluteDirections(Robot robot) {
        JsonObject visionData = new JsonObject();
        JsonArray objects = new JsonArray();
        Set<String> reportedDirections = new HashSet<>();

        Position robotPos = robot.getPosition();

        // Check all four directions for obstacles and edges
        checkDirection(objects, reportedDirections, robotPos, Direction.NORTH, "NORTH");
        checkDirection(objects, reportedDirections, robotPos, Direction.EAST, "EAST");
        checkDirection(objects, reportedDirections, robotPos, Direction.SOUTH, "SOUTH");
        checkDirection(objects, reportedDirections, robotPos, Direction.WEST, "WEST");

        // Check for immediate world edges that might not be caught by obstacle check
        checkImmediateEdges(objects, reportedDirections, robotPos);

        visionData.add("objects", objects);
        visionData.addProperty("visibilityRange", world.getVisibilityRange());
        return visionData;
    }

    private void checkDirection(JsonArray objects, Set<String> reportedDirections,
                                Position robotPos, Direction direction, String directionName) {
        Position current = new Position(robotPos.getX(), robotPos.getY());
        int distance = 0;

        while (distance <= world.getVisibilityRange()) {
            current = getNextPosition(current, direction);
            distance++;

            // Check if position is valid
            if (!world.isPositionValid(current)) {
                if (!reportedDirections.contains(directionName)) {
                    addObject(objects, directionName, "EDGE", distance);
                    reportedDirections.add(directionName);
                }
                break;
            }

            // Check obstacles
            for (Obstacle obstacle : world.getObstacles()) {
                if (obstacle.blocksPosition(current.getX(), current.getY())) {
                    if (!reportedDirections.contains(directionName)) {
                        addObject(objects, directionName, obstacle.getType().toUpperCase(), distance);
                        reportedDirections.add(directionName);
                    }
                    return;
                }
            }

            // Check other robots
            for (Robot other : world.getRobots()) {
                if (!other.equals(robot) && other.getPosition().equals(current)) {
                    if (!reportedDirections.contains(directionName)) {
                        addObject(objects, directionName, "ROBOT", distance);
                        reportedDirections.add(directionName);
                    }
                    return;
                }
            }
        }
    }

    private void checkImmediateEdges(JsonArray objects, Set<String> reportedDirections, Position robotPos) {
        // Check if robot is at world edges
        if (robotPos.getY() == 0 && !reportedDirections.contains("NORTH")) {
            addObject(objects, "NORTH", "EDGE", 1);
            reportedDirections.add("NORTH");
        }
        if (robotPos.getY() == world.getHeight() - 1 && !reportedDirections.contains("SOUTH")) {
            addObject(objects, "SOUTH", "EDGE", 1);
            reportedDirections.add("SOUTH");
        }
        if (robotPos.getX() == world.getWidth() - 1 && !reportedDirections.contains("EAST")) {
            addObject(objects, "EAST", "EDGE", 1);
            reportedDirections.add("EAST");
        }
        if (robotPos.getX() == 0 && !reportedDirections.contains("WEST")) {
            addObject(objects, "WEST", "EDGE", 1);
            reportedDirections.add("WEST");
        }
    }

    private Position getNextPosition(Position current, Direction direction) {
        return switch (direction) {
            case NORTH -> new Position(current.getX(), current.getY() - 1);
            case EAST -> new Position(current.getX() + 1, current.getY());
            case SOUTH -> new Position(current.getX(), current.getY() + 1);
            case WEST -> new Position(current.getX() - 1, current.getY());
        };
    }

    private void addObject(JsonArray objects, String direction, String type, int distance) {
        JsonObject obj = new JsonObject();
        obj.addProperty("direction", direction);
        obj.addProperty("type", type);
        obj.addProperty("distance", distance);
        objects.add(obj);
    }
}