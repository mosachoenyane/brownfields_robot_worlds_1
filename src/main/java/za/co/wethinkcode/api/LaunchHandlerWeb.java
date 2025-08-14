package za.co.wethinkcode.api;

import com.fasterxml.jackson.databind.JsonNode;
import io.javalin.http.Context;
import org.jetbrains.annotations.NotNull;
import za.co.wethinkcode.client.RobotWorldJsonClient;


public class LaunchHandlerWeb {
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
}