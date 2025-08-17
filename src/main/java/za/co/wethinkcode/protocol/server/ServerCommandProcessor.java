package za.co.wethinkcode.protocol.server;

import com.google.gson.*;
import za.co.wethinkcode.server.commands.Command;
import za.co.wethinkcode.client.commands.StateCommand;

import za.co.wethinkcode.server.model.Position;
import za.co.wethinkcode.server.model.Robot;
import za.co.wethinkcode.server.utils.PositionFinder;
import za.co.wethinkcode.server.utils.VisionFinder;
import za.co.wethinkcode.server.world.World;
import za.co.wethinkcode.server.world.obstacles.Mine;

import java.sql.*;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

/**
 * The ServerCommandProcessor class handles all incoming commands on the server side.
 * It processes messages from clients, interprets robot and world-related commands, and
 * uses a CommandFactory to create and execute the appropriate actions in the game world.
 */
public class ServerCommandProcessor {
    private final World world;
    private final Gson gson = new Gson();
    private final CommandFactory commandFactory;

    /**
     * Creates a new ServerCommandProcessor instance to handle robot and world commands.
     *
     * @param world the game world in which robots operate
     */
    public ServerCommandProcessor(World world) {
        this.world = world;
        this.commandFactory = new CommandFactory(world);
    }

    /**
     * Processes a JSON message from the client and executes the appropriate command.
     */
    public String processMessage(String message) {
        try {
            JsonObject request = gson.fromJson(message, JsonObject.class);

            if (!request.has("command")) {
                return createErrorResponse("Missing command");
            }

            String command = request.get("command").getAsString().toLowerCase();

            // Server administration commands
            return switch (command) {
                case "dump" -> commandFactory.createDumpCommand().execute();
                case "robots" -> commandFactory.createRobotsCommand().execute();

                default ->

                    // Robot operation commands
                        processRobotCommand(request);

            };

        } catch (JsonSyntaxException e) {
            return createErrorResponse("Invalid JSON format");
        } catch (Exception e) {
            return createErrorResponse(e.getMessage());
        }
    }

    /**
     * Processes robot-specific commands such as movement, launching, or combat actions.
     *
     * @param request the JSON request containing robot details and command
     * @return the JSON response string with the outcome
     */
    private String processRobotCommand(JsonObject request) {
        Robot robot = getRobotFromRequest(request);

        if (isRobotDead(robot)) {
            return createErrorResponse("Robot is DEAD and cannot execute commands");
        }

        String command = getCommandFromRequest(request);
        String robotName = getRobotNameFromRequest(request);

        return handleCommand(command, robotName, request);
    }

    /**
     * Retrieves the {@link Robot} instance from the request.
     *
     * @param request the client request containing robot name
     * @return the corresponding {@link Robot}, or {@code null} if not found
     */
    private Robot getRobotFromRequest(JsonObject request) {
        return world.getRobotByName(request.get("robot").getAsString());
    }

    /**
     * Checks whether a robot is dead.
     *
     * @param robot the robot instance
     * @return {@code true} if robot exists and is dead, {@code false} otherwise
     */
    private boolean isRobotDead(Robot robot) {
        return robot != null && robot.getStatus() == Robot.Status.DEAD;
    }

    /**
     * Extracts the command name from the request.
     *
     * @param request the client request JSON
     * @return the command string
     */
    private String getCommandFromRequest(JsonObject request) {
        return request.get("command").getAsString();
    }

    /**
     * Extracts the robot name from the request.
     *
     * @param request the client request JSON
     * @return the robot name
     */
    private String getRobotNameFromRequest(JsonObject request) {
        return request.get("robot").getAsString();
    }

    /**
     * Functional interface representing a handler for robot commands.
     */
    @FunctionalInterface
    interface CommandHandler {
        /**
         * Executes a command for a given robot.
         *
         * @param robotName the name of the robot
         * @param request   the request JSON object
         * @return a JSON response string
         */
        String handle(String robotName, JsonObject request);
    }

    // Initialize command map
    private final Map<String, CommandHandler> commandMap = Map.of(
            "launch", this::processLaunchCommand,
            "forward", this::processForwardCommand,
            "back", this::processBackCommand,
            "turn", this::processTurnCommand,
            "look", (name, req) -> processLookCommand(name),
            "state", (name, req) -> processStateCommand(name),
            "fire", (name, req) -> processFireCommand(name),
            "reload", (name, req) -> processReloadCommand(name),
            "repair", (name, req) -> processRepairCommand(name),
            "mine", (name, req) -> processMineCommand(name, req)
    );

    /**
     * Routes a command to the appropriate handler.
     *
     * @param command   the command string
     * @param robotName the robot name
     * @param request   the request JSON
     * @return the JSON response string
     */
    private String handleCommand(String command, String robotName, JsonObject request) {
        CommandHandler handler = commandMap.get(command);
        return (handler != null)
                ? handler.handle(robotName, request)
                : createErrorResponse("Unsupported command: " + command);
    }

    /**
     * Handles the "launch" command to create and place a robot in the world.
     *
     * @param robotName the name of the robot
     * @param request   the request JSON containing arguments
     * @return the JSON response string
     */
    private String processLaunchCommand(String robotName, JsonObject request) {
        try {
            if (!request.has("arguments")) {
                return createErrorResponse("Launch requires arguments: [make]");
            }

            JsonArray args = request.getAsJsonArray("arguments");
            if (args.size() < 1) {
                return createErrorResponse("Launch requires make");
            }

            String make = args.get(0).getAsString();

            // Get values from world config
            int shields = world.getMaxShieldStrength();
            int shots = world.getMaxShots();

            // Check if robot exists
            if (world.getRobotByName(robotName) != null) {
                return createErrorResponse("Too many of you in this world");
            }

            Position pos = new PositionFinder(world).findRandomOpenPosition();
            if (pos == null) {
                return createErrorResponse("No more space in this world");
            }

            Robot robot = new Robot(robotName, pos);
            robot.setMake(make);
            robot.setShields(shields);
            robot.setShots(shots);
            world.addRobot(robot);

            return createSuccessResponse(robot);
        } catch (Exception e) {
            return createErrorResponse("Invalid launch parameters: " + e.getMessage());
        }
    }

    /**
     * Handles the "look" command for a robot.
     *
     * @param robotName the robot name
     * @return the JSON response string containing visible objects
     */
    private String processLookCommand(String robotName) {
        Robot robot = world.getRobotByName(robotName);
        if (robot == null) {
            return createErrorResponse("Robot not found");
        }

        VisionFinder visionFinder = new VisionFinder(world, robot);
        JsonObject visionData = visionFinder.findInAbsoluteDirections(robot);

        JsonObject response = new JsonObject();
        response.addProperty("result", "OK");
        response.add("data", visionData);
        response.add("state", new StateCommand(robot).toJson());
        return gson.toJson(response);
    }

    /**
     * Handles the "state" command for a robot.
     *
     * @param robotName the robot name
     * @return the JSON response string containing the robot’s state
     */
    private String processStateCommand(String robotName) {
        Robot robot = world.getRobotByName(robotName);
        if (robot == null) {
            return createErrorResponse("Robot not found");
        }

        JsonObject response = new JsonObject();
        response.addProperty("result", "OK");
        response.add("state", new StateCommand(robot).toJson());
        return gson.toJson(response);
    }

    /**
     * Validates movement command arguments.
     *
     * @param robot   the robot
     * @param request the JSON request
     * @return the step count, or 0 if invalid
     */
    private int checkArguments(Robot robot,JsonObject request){

        if (robot == null) {
            return 0;
        }

        int steps = 1; // Default step count
        if (request.has("arguments")) {
            JsonArray args = request.getAsJsonArray("arguments");
            if (!args.isEmpty()) {
                steps = args.get(0).getAsInt();
            }
        }
        return  steps;
    }

    /**
     * Handles the "forward" command.
     */
    private String processForwardCommand(String robotName, JsonObject request) {
        Robot robot = world.getRobotByName(robotName);
        int steps = checkArguments(robot,request);
        if (steps == 0){ createErrorResponse("Robot not found");}
        Command command = commandFactory.createForwardCommand(robot, steps);
        return command.execute();
    }

    /**
     * Handles the "back" command.
     */
    private String processBackCommand(String robotName, JsonObject request) {
        Robot robot = world.getRobotByName(robotName);
        int steps = checkArguments(robot,request);
        if (steps == 0){ createErrorResponse("Robot not found");}
        Command command = commandFactory.createBackCommand(robot, steps);
        return command.execute();
    }

    /**
     * Handles the "turn" command (left or right).
     */
    private String processTurnCommand(String robotName, JsonObject request) {
        Robot robot = world.getRobotByName(robotName);
        if (robot == null) {
            return createErrorResponse("Robot not found");
        }

        if (!request.has("arguments")) {
            return createErrorResponse("Turn requires direction argument");
        }

        JsonArray args = request.getAsJsonArray("arguments");
        if (args.size() < 1) {
            return createErrorResponse("Turn requires direction argument");
        }

        String direction = args.get(0).getAsString().toLowerCase();
        Command command;

        switch (direction) {
            case "left":
                command = commandFactory.createTurnLeftCommand(robot);
                break;
            case "right":
                command = commandFactory.createTurnRightCommand(robot);
                break;
            default:
                return createErrorResponse("Invalid direction. Must be 'left' or 'right'");
        }

        return command.execute();
    }

    /**
     * Handles the "fire" command.
     */
    private String processFireCommand(String robotName) {
        Robot robot = world.getRobotByName(robotName);
        if (robot == null) {
            return createErrorResponse("Robot not found");
        }

        Command command = commandFactory.createFireCommand(robot);
        return command.execute();
    }

    /**
     * Handles the "reload" command.
     */
    private String processReloadCommand(String robotName) {
        Robot robot = world.getRobotByName(robotName);
        if (robot == null) {
            return createErrorResponse("Robot not found");
        }

        Command command = commandFactory.createReloadCommand(robot);
        return command.execute();
    }

    /**
     * Handles the "repair" command.
     */
    private String processRepairCommand(String robotName) {
        Robot robot = world.getRobotByName(robotName);
        if (robot == null) {
            return createErrorResponse("Robot not found");
        }

        Command command = commandFactory.createRepairCommand(robot);
        return command.execute();
    }

    /**
     * Creates a standard success JSON response for robot actions.
     *
     * @param robot the robot
     * @return JSON response string
     */
    private String createSuccessResponse(Robot robot) {
        JsonObject response = new JsonObject();
        response.addProperty("result", "OK");

        JsonObject data = new JsonObject();
        data.add("position", gson.toJsonTree(new int[]{
                robot.getPosition().getX(),
                robot.getPosition().getY()
        }));

        response.add("data", data);
        response.add("state", new StateCommand(robot).toJson());

        return gson.toJson(response);
    }

    /**
     * Creates a standard error JSON response.
     *
     * @param message the error message
     * @return JSON response string
     */
    private String createErrorResponse(String message) {
        JsonObject response = new JsonObject();
        JsonObject data = new JsonObject();
        response.addProperty("result", "ERROR");
        data.addProperty("message",message);
        response.add("data",data);
        return gson.toJson(response);
    }

    /**
     * Handles the "mine" command to place a mine at the robot’s position.
     */
    private String processMineCommand(String robotName, JsonObject request) {
        Robot robot = world.getRobotByName(robotName);
        if (robot == null) return createErrorResponse("Robot not found");

        Mine mine = new Mine(robot.getPosition());
        world.addObstacle(mine);

        JsonObject response = new JsonObject();
        response.addProperty("result", "OK");
        JsonObject data = new JsonObject();
        data.addProperty("message", "Mine placed successfully");
        response.add("data", data);
        response.add("state", new StateCommand(robot).toJson());
        return gson.toJson(response);
    }
}