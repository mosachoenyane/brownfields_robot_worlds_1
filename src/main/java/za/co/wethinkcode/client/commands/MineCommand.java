package za.co.wethinkcode.client.commands;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import za.co.wethinkcode.client.RobotWorldClient;

/**
 * Represents the "mine" command that instructs a robot
 * to place a mine in the robot world.
 * <p>
 * This command builds a JSON request, sends it to the server,
 * and parses the response to determine success or failure.
 */
public class MineCommand {
    private final RobotWorldClient client;
    private final String robotName;

    /**
     * Constructs a new {@code MineCommand}.
     *
     * @param client    the client used to communicate with the robot world server
     * @param robotName the name of the robot executing the command
     */
    public MineCommand(RobotWorldClient client, String robotName) {
        this.client = client;
        this.robotName = robotName;
    }

    /**
     * Executes the mine command by sending a request to the server.
     * <p>
     * If successful, the robot places a mine in its current position.
     *
     * @return a human-readable string describing the outcome
     *         (e.g., success message, failure reason, or error details)
     */
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
