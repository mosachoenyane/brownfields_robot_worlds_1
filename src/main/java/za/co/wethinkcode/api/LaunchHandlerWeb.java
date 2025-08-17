package za.co.wethinkcode.api;

import com.fasterxml.jackson.databind.JsonNode;
import io.javalin.http.Context;
import org.jetbrains.annotations.NotNull;
import za.co.wethinkcode.client.RobotWorldJsonClient;

/**
 * HTTP request handler for robot operations in the Robot World API.
 * Provides endpoints for launching and sending commands to robots
 * by proxying requests to the RobotWorldJsonClient.
 */

public class LaunchHandlerWeb {

    /**
     * Handles the {@code /launch/:robot} endpoint.
     * Launches a robot with the given name by sending a {@code launch}
     * command to the robot server.
     *
     * @param context the Javalin {@link Context}, containing the path parameter {@code :robot}
     */

    public void LaunchRobot(Context context){
        String name = context.pathParam("robot");
        RobotWorldJsonClient client = new RobotWorldJsonClient();
        client.connect("0.0.0.0",5000);
        String request = "{" +
                "  \"robot\": \""+name+"\"," +
                "  \"command\": \"launch\"," +
                "  \"arguments\": [\"shooter\",\"5\",\"5\"]" +
                "}";
        JsonNode response = client.sendRequest(request);
        context.json(response);
        context.status(201);
    }

    /**
     * Handles the {@code /robot/:robot/:command} endpoint.
     * Sends an arbitrary command (e.g., {@code forward}, {@code turn}, etc.)
     * to the specified robot.
     *
     * @param context the Javalin {@link Context}, containing the path parameters {@code :robot} and {@code :command}
     */

    public void CommandRobot(@NotNull Context context) {
        String name = context.pathParam("robot");
        String command = context.pathParam("command");
        RobotWorldJsonClient client = new RobotWorldJsonClient();
        client.connect("0.0.0.0",5000);
        String request = "{" +
                "  \"robot\": \""+name+"\"," +
                "  \"command\": \""+command+"\"," +
                "  \"arguments\": [\"shooter\",\"5\",\"5\"]" +
                "}";
        JsonNode response = client.sendRequest(request);
        context.json(response);
        context.status(201);
    }

    // POST/robot/{name}/look
    public void LookRobot(Context context){
        String name = context.pathParam("robot");
        RobotWorldJsonClient client = new RobotWorldJsonClient();
        client.connect("0.0.0.0",5000);

        String request = "{" +
                "  \"robot\": \""+name+"\"," +
                "  \"command\": \"look\"" +
                "}";

        JsonNode response = client.sendRequest(request);
        context.json(response);
        context.status(200); // OK for a read/scan action
    }
}
