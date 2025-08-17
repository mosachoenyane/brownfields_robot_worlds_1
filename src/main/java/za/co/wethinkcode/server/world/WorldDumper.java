package za.co.wethinkcode.server.world;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import za.co.wethinkcode.server.world.obstacles.Obstacle;
import za.co.wethinkcode.server.model.Position;
import za.co.wethinkcode.server.model.Robot;

/**
 * WorldDumper is responsible for creating a JSON representation of the world state,
 * including all obstacles and robots present in the world.
 */
public class WorldDumper {
    private final World world;

    /**
     * Constructs a WorldDumper for the given world.
     *
     * @param world The world instance to dump.
     */
    public WorldDumper(World world) {
        this.world = world;
    }

    /**
     * Creates a JSON dump of the entire world including its dimensions,
     * obstacles, and robots.
     *
     * @return A JsonObject representing the current state of the world.
     */
    public JsonObject createWorldDump() {
        JsonObject worldDump = new JsonObject();
        worldDump.addProperty("width", world.getWidth());
        worldDump.addProperty("height", world.getHeight());
        worldDump.add("obstacles", getObstaclesJson());
        worldDump.add("robots", getRobotsJson());
        return worldDump;
    }

    /**
     * Converts all obstacles in the world to a JsonArray representation.
     * Each obstacle includes type, position, and dimensions.
     *
     * @return JsonArray of obstacles.
     */
    private JsonArray getObstaclesJson() {
        JsonArray obstaclesArray = new JsonArray();
        for (Obstacle obstacle : world.getObstacles()) {
            JsonObject obstacleJson = new JsonObject();
            obstacleJson.addProperty("type", obstacle.getType());
            obstacleJson.add("position", new Gson().toJsonTree(new int[]{obstacle.getX(), obstacle.getY()}));
            // Add scalar coordinates for easier client consumption
            obstacleJson.addProperty("x", obstacle.getX());
            obstacleJson.addProperty("y", obstacle.getY());
            obstaclesArray.add(obstacleJson);
        }
        return obstaclesArray;
    }
    /**
     * Converts all robots in the world to a JsonArray representation.
     * Each robot includes name, make, position, direction, shields, and shots.
     *
     * @return JsonArray of robots.
     */
    private JsonArray getRobotsJson() {
        JsonArray robotsArray = new JsonArray();
        for (Robot robot : world.getRobots()) {
            JsonObject robotJson = new JsonObject();
            robotJson.addProperty("name", robot.getName());
            robotJson.addProperty("make", robot.getMake());
            Position pos = robot.getPosition();
            robotJson.add("position", new Gson().toJsonTree(new int[]{pos.getX(), pos.getY()}));
            robotJson.addProperty("direction", robot.getDirection().name());
            robotJson.addProperty("shields", robot.getShields());
            robotJson.addProperty("shots", robot.getShots());
            robotsArray.add(robotJson);
        }
        return robotsArray;
    }
}