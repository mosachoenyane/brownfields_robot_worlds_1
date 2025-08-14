package za.co.wethinkcode.client.commands;

import org.json.JSONObject;
import za.co.wethinkcode.server.commands.Command;
import za.co.wethinkcode.server.world.World;
import za.co.wethinkcode.server.model.Robot;

/**
 * Command for planting a mine in the robot world.
 * This is sent from the client to the server, and the server
 * will handle placing the mine in the world.
 */

public class MineCommand implements Command {
    private final Robot robot;
    private final World world;

    public MineCommand(World world, Robot robot){
        this.world = world;
        this.robot = robot;
    }
    @Override
    public String execute(Robot target){
        /* Json */
        JSONObject requesr = new JSONObject();
        requesr.put("robot", target.getName());
        requesr.put("command", "mine");

        JSONObject response = world.handleCommand(requesr);

        if (response.has("result") && "OK".equalsIgnoreCase(response.getString("result"))){
            return "Mine placed at " + target.getPosition();
        } else{
            return "Failed to plant mine... you might be empty: ";
        }
    }
}
