package za.co.wethinkcode.client.commands;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * Represents a visual scan result for a robot.
 * Stores visible objects and builds a JSON response.
 */

public class LookCommand {
    private final JsonArray objects = new JsonArray();
    private final int visibilityRange;

    /**
     * Constructs a {@code LookCommand} with a visibility range.
     *
     * @param visibilityRange how far the robot can see
     */

    public LookCommand(int visibilityRange) {
        this.visibilityRange = visibilityRange;
    }

    /**
     * Adds a visible object to the scan result.
     *
     * @param direction the direction where the object is seen
     * @param type      the type of object (e.g., "robot", "obstacle")
     * @param distance  how far the object is from the robot
     */

    public void addObject(String direction, String type, int distance) {
        JsonObject obj = new JsonObject();
        obj.addProperty("direction", direction);
        obj.addProperty("type", type);
        obj.addProperty("distance", distance);
        objects.add(obj);
    }

    /**
     * Returns the scan result as a JSON object.
     *
     * @return a JSON representation of what the robot sees
     */

    public JsonObject toJson() {
        JsonObject visionJson = new JsonObject();
        visionJson.add("objects", objects);
        visionJson.addProperty("visibilityRange", visibilityRange);
        return visionJson;
    }
}