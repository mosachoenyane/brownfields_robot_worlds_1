package za.co.wethinkcode.client.commands;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import za.co.wethinkcode.client.RobotWorldClient;

public class MineCommand {
    private final RobotWorldClient client;
    private final String robotName;

    public MineCommand(RobotWorldClient client, String robotName) {
        this.client = client;
        this.robotName = robotName;
    }

    public String execute() {
        try {
            ObjectMapper mapper = new ObjectMapper();

            // Build JSON request
            JsonNode request = mapper.createObjectNode()
                    .put("robot", robotName)
                    .put("command", "mine");

            // Send request to server
            JsonNode response = client.sendRequest(request.toString());

            // Check result
            String result = response.path("result").asText();
            if ("OK".equalsIgnoreCase(result)) {
                return "Mine successfully placed!";
            } else {
                return "Failed to place mine: " + response.path("data").path("message").asText();
            }

        } catch (Exception e) {
            return "Error sending mine command: " + e.getMessage();
        }
    }
}
