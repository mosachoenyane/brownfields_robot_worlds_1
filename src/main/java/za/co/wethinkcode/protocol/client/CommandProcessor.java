package za.co.wethinkcode.protocol.client;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import za.co.wethinkcode.client.Play;

/**
 * Parses and validates client string commands and converts them to JSON
 * commands to be sent to the server.
 * Works in coordination with the Play class to ensure correct robot context.
 */

public class CommandProcessor {
    private final Play play;
    private final Gson gson = new Gson();
    private String selectedMake;
    private String robotName;

    /**
     * Constructs a CommandProcessor linked to the main game Play instance.
     *
     * @param play the Play object managing the game session
     */
    public CommandProcessor(Play play) {
        this.play = play;
    }

    /**
     * Sets the robot make and name expected for command validation.
     *
     * @param make the robot make/model
     * @param name the robot's name
     */
    public void setRobotDetails(String make, String name) {
        this.selectedMake = make;
        this.robotName = name;
    }

    /**
     * Converts a string command input to a JSON command string if valid.
     * Returns an error JSON if invalid or unknown command.
     *
     * @param input the raw command string from the user
     * @return JSON-formatted command string or error response
     */

    public String convertToJsonCommand(String input) {
        String[] parts = input.trim().split(" ");
        if (parts.length < 1) {
            return createErrorResponse("Invalid command format");
        }
        String command = parts[0].toLowerCase();
        return switch (command) {
            case "launch" -> processLaunchCommand(parts);
            default -> processCommand(parts);

        };

    }

    private String processLaunchCommand(String[] parts) {
        if (parts.length != 3) {
            return createErrorResponse("Launch requires make and name");
        }

        String make = parts[1];
        String name = parts[2];

        if (!make.equalsIgnoreCase(selectedMake)) {
            return createErrorResponse("Invalid make. You must use: " + selectedMake);
        }
        if (!name.equalsIgnoreCase(robotName)) {
            return createErrorResponse("Invalid robot name. You must use: " + robotName);
        }

        JsonObject command = new JsonObject();
        command.addProperty("command", "launch");
        command.addProperty("robot", name);

        JsonArray args = new JsonArray();
        args.add(make);
        // Shields and shots will be set by server from config
        command.add("arguments", args);

        return gson.toJson(command);
    }

    private String processCommand(String[] parts){

        String commandType = parts[0];
        if ("launch".equalsIgnoreCase(commandType) && parts.length < 3) {
            return createErrorResponse(commandType + " requires robot make and name");
        } else if (parts.length < 2) {
            // This handles commands like 'state', 'look', etc., when no robot name is given.
            return createErrorResponse(commandType + " requires a robot name");
        }
        String name = parts[1];
        if (!name.equalsIgnoreCase(robotName)) {
            return createErrorResponse("Invalid robot name. You must use: " + robotName);
        }
        JsonObject jsonObject= new JsonObject();
        jsonObject.addProperty("command",commandType);
        jsonObject.addProperty("robot",name);
        JsonArray arguments = new JsonArray();
        jsonObject.add("arguments", arguments);
        if (parts.length > 2){
            arguments.add(parts[2]);
        }

        return gson.toJson(jsonObject);
    }

    private String createErrorResponse(String message) {
        System.out.println("here");
        JsonObject response = new JsonObject();
        JsonObject data = new JsonObject();
        response.addProperty("result", "ERROR");
        data.addProperty("message", message);
        response.add("data",data);

        return gson.toJson(response);
    }
}