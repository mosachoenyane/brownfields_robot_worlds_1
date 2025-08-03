package za.co.wethinkcode.server.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import za.co.wethinkcode.robotworld.DirectionContext;
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

        for (int distance = 1; distance <= world.getVisibilityRange(); distance++) {
            current = getNextPosition(current, direction);

            DirectionContext ctx = new DirectionContext(objects, reportedDirections, directionName, current, distance);

            if (isDirectionBlocked(ctx)) {
                break;
            }
        }
    }


    boolean isDirectionBlocked(DirectionContext ctx) {
        if (!world.isPositionValid(ctx.getCurrent())) {
            handleInvalidPosition(ctx.getObjects(), ctx.getReportedDirections(), ctx.getDirectionName(), ctx.getDistance());
            return true;
        }

        return isBlockedByObstacle(ctx) || isBlockedByOtherRobot(ctx);
    }




    private boolean isBlockedByObstacle(JsonArray objects, Set<String> reportedDirections, String directionName,
                                        Position current, int distance) {
        return world.getObstacles().stream()
                .filter(obstacle -> obstacle.blocksPosition(current.getX(), current.getY()))
                .anyMatch(obstacle ->
                        handleObstacle(objects, reportedDirections, directionName, obstacle, distance));
    }


    private boolean isBlockedByOtherRobot(DirectionContext ctx) {
        return world.getRobots().stream()
                .filter(other -> !other.equals(robot))
                .anyMatch(other -> other.getPosition().equals(ctx.getCurrent()) &&
                        handleOtherRobot(ctx, ctx.getDirectionName(), ctx.getDistance()));
    }




    private boolean handleInvalidPosition(JsonArray objects, Set<String> reportedDirections, String directionName, int distance) {
        if (!reportedDirections.contains(directionName)) {
            addObject(objects, directionName, "EDGE", distance);
            reportedDirections.add(directionName);
        }
        return true;
    }
    private boolean handleObstacle(DirectionContext ctx, Obstacle obstacle) {
        if (ctx.getReportedDirections().contains(ctx.getDirectionName())) {
            return true;
        }

        addObject(ctx.getObjects(), ctx.getDirectionName(), obstacle.getType().toUpperCase(), ctx.getDistance());
        ctx.getReportedDirections().add(ctx.getDirectionName());
        return true;
    }


    private boolean handleOtherRobot(JsonArray objects, Set<String> reportedDirections, String directionName, int distance) {
        if (!reportedDirections.contains(directionName)) {
            addObject(objects, directionName, "ROBOT", distance);
            reportedDirections.add(directionName);
        }
        return true;
    }
    private void checkImmediateEdges(JsonArray objects, Set<String> reportedDirections, Position robotPos) {
        checkAndAddEdge(objects, reportedDirections, robotPos.getY() == 0, "NORTH");
        checkAndAddEdge(objects, reportedDirections, robotPos.getY() == world.getHeight() - 1, "SOUTH");
        checkAndAddEdge(objects, reportedDirections, robotPos.getX() == world.getWidth() - 1, "EAST");
        checkAndAddEdge(objects, reportedDirections, robotPos.getX() == 0, "WEST");
    }

    private void checkAndAddEdge(JsonArray objects, Set<String> reportedDirections, boolean isAtEdge, String direction) {
        if (isAtEdge && !reportedDirections.contains(direction)) {
            addObject(objects, direction, "EDGE", 1);
            reportedDirections.add(direction);
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