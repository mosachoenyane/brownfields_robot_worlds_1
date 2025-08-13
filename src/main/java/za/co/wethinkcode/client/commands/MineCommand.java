package za.co.wethinkcode.server.commands;

import com.google.gson.JsonObject;
import za.co.wethinkcode.server.model.Position;
import za.co.wethinkcode.server.model.Robot;
import za.co.wethinkcode.server.world.World;
import za.co.wethinkcode.server.world.obstacles.Bomb;
import za.co.wethinkcode.server.world.obstacles.Obstacle;

/**
 * Command to set a bomb (mine) in the robot's current facing direction.
 */
public class SetBombCommand implements Command {
    private final World world;
    private final Robot robot;

    public SetBombCommand(World world, Robot robot) {
        this.world = world;
        this.robot = robot;
    }

    @Override
    public String execute() {
        Position targetPos = calculateTargetPosition();

        // Create bomb obstacle
        Obstacle bomb = new Bomb();
        bomb.setPosition(targetPos);

        // Try placing bomb in the world
        if (!world.addObstacle(bomb)) {
            return createErrorResponse("Bomb cannot be set here.");
        }

        bomb.setClient(robot.getName());
        return createSuccessResponse("Bomb set successfully.");
    }

    private Position calculateTargetPosition() {
        Position current = robot.getPosition();
        switch (robot.getDirection()) {
            case NORTH: return new Position(current.getX(), current.getY() - 1);
            case EAST:  return new Position(current.getX() + 1, current.getY());
            case SOUTH: return new Position(current.getX(), current.getY() + 1);
            case WEST:  return new Position(current.getX() - 1, current.getY());
            default:    return current;
        }
    }

    private String createSuccessResponse(String message) {
        JsonObject response = new JsonObject();
        response.addProperty("result", "OK");

        JsonObject data = new JsonObject();
        data.addProperty("message", message);
        response.add("data", data);

        JsonObject state = new JsonObject();
        state.add("position", worldPositionJson());
        state.addProperty("direction", robot.getDirection().name());
        state.addProperty("shields", robot.getShields());
        state.addProperty("shots", robot.getShots());
        state.addProperty("status", robot.getStatus().name());
        response.add("state", state);

        return response.toString();
    }

    private String createErrorResponse(String message) {
        JsonObject response = new JsonObject();
        response.addProperty("result", "ERROR");
        response.addProperty("message", message);
        return response.toString();
    }

    private JsonObject worldPositionJson() {
        JsonObject posJson = new JsonObject();
        posJson.add("position", new com.google.gson.Gson().toJsonTree(
                new int[]{robot.getPosition().getX(), robot.getPosition().getY()}
        ));
        return posJson;
    }

    @Override
    public String getName() {
        return "setbomb";
    }

    @Override
    public String display() {
        return "SETBOMB " + robot.getName();
    }
}
