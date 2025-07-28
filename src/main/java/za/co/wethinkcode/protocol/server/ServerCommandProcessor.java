package za.co.wethinkcode.protocol.server;

import com.google.gson.*;
import za.co.wethinkcode.server.commands.Command;
import za.co.wethinkcode.client.commands.StateCommand;

import za.co.wethinkcode.server.model.Position;
import za.co.wethinkcode.server.model.Robot;
import za.co.wethinkcode.server.utils.PositionFinder;
import za.co.wethinkcode.server.utils.VisionFinder;
import za.co.wethinkcode.server.world.World;

import java.sql.*;
import java.sql.ResultSet;

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

    private String processRobotCommand(JsonObject request) {
        Robot robot = getRobotFromRequest(request);

        if (isRobotDead(robot)) {
            return createErrorResponse("Robot is DEAD and cannot execute commands");
        }

        String command = getCommandFromRequest(request);
        String robotName = getRobotNameFromRequest(request);

        return handleCommand(command, robotName, request);
    }

    private Robot getRobotFromRequest(JsonObject request) {
        return world.getRobotByName(request.get("robot").getAsString());
    }

    private boolean isRobotDead(Robot robot) {
        return robot != null && robot.getStatus() == Robot.Status.DEAD;
    }

    private String getCommandFromRequest(JsonObject request) {
        return request.get("command").getAsString();
    }

    private String getRobotNameFromRequest(JsonObject request) {
        return request.get("robot").getAsString();
    }

    private String handleCommand(String command, String robotName, JsonObject request) {
        return switch (command) {
            case "launch" -> processLaunchCommand(robotName, request);
            case "look" -> processLookCommand(robotName);
            case "state" -> processStateCommand(robotName);
            case "forward" -> processForwardCommand(robotName, request);
            case "back" -> processBackCommand(robotName, request);
            case "turn" -> processTurnCommand(robotName, request);
            case "fire" -> processFireCommand(robotName);
            case "reload" -> processReloadCommand(robotName);
            case "repair" -> processRepairCommand(robotName);
            default -> createErrorResponse("Unsupported command: " + command);
        };
    }

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

    private String processForwardCommand(String robotName, JsonObject request) {
        Robot robot = world.getRobotByName(robotName);
        int steps = checkArguments(robot,request);
        if (steps == 0){ createErrorResponse("Robot not found");}
        Command command = commandFactory.createForwardCommand(robot, steps);
        return command.execute();
    }

    private String processBackCommand(String robotName, JsonObject request) {
        Robot robot = world.getRobotByName(robotName);
        int steps = checkArguments(robot,request);
        if (steps == 0){ createErrorResponse("Robot not found");}
        Command command = commandFactory.createBackCommand(robot, steps);
        return command.execute();
    }

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

    private String processFireCommand(String robotName) {
        Robot robot = world.getRobotByName(robotName);
        if (robot == null) {
            return createErrorResponse("Robot not found");
        }

        Command command = commandFactory.createFireCommand(robot);
        return command.execute();
    }

    private String processReloadCommand(String robotName) {
        Robot robot = world.getRobotByName(robotName);
        if (robot == null) {
            return createErrorResponse("Robot not found");
        }

        Command command = commandFactory.createReloadCommand(robot);
        return command.execute();
    }

    private String processRepairCommand(String robotName) {
        Robot robot = world.getRobotByName(robotName);
        if (robot == null) {
            return createErrorResponse("Robot not found");
        }

        Command command = commandFactory.createRepairCommand(robot);
        return command.execute();
    }

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

    private String createErrorResponse(String message) {
        JsonObject response = new JsonObject();
        JsonObject data = new JsonObject();
        response.addProperty("result", "ERROR");
        data.addProperty("message",message);
        response.add("data",data);
        return gson.toJson(response);
    }
}