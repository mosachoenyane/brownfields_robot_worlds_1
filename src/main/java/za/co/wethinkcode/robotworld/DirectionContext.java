package za.co.wethinkcode.robotworld;  // adjust to your package

import com.google.gson.JsonArray;
import za.co.wethinkcode.server.model.Position;

import java.util.Set;

/**
 * Represents the context of scanning or checking in a specific direction within the robot world.
 *
 * It is typically used when performing visibility checks (like 'look' commands).
 */
public class DirectionContext {
    private final JsonArray objects;
    private final Set<String> reportedDirections;
    private final String directionName;
    private Position current;
    private int distance;

    /**
     * Creates a new {@code DirectionContext}.
     *
     * @param objects            JSON array of objects detected in this direction
     * @param reportedDirections a set of directions already reported to avoid duplicates
     * @param directionName      the name of the direction being scanned (e.g., "north")
     * @param current            the current {@link Position} in the scan
     * @param distance           the distance from the origin to the current position
     */
    public DirectionContext(JsonArray objects, Set<String> reportedDirections,
                            String directionName, Position current, int distance) {
        this.objects = objects;
        this.reportedDirections = reportedDirections;
        this.directionName = directionName;
        this.current = current;
        this.distance = distance;
    }

    /**
     * @return JSON array of objects found in this direction
     */
    public JsonArray getObjects() {
        return objects;
    }

    /**
     * @return the set of directions already reported
     */
    public Set<String> getReportedDirections() {
        return reportedDirections;
    }

    /**
     * @return the name of this direction (e.g., "north", "east")
     */
    public String getDirectionName() {
        return directionName;
    }

    /**
     * @return the current {@link Position} being considered in this direction
     */
    public Position getCurrent() {
        return current;
    }

    /**
     * Updates the current position in this direction.
     *
     * @param current the new {@link Position}
     */
    public void setCurrent(Position current) {
        this.current = current;
    }

    /**
     * @return the distance from the origin to the current position
     */
    public int getDistance() {
        return distance;
    }

    /**
     * Updates the distance from the origin to the current position.
     *
     * @param distance the new distance value
     */
    public void setDistance(int distance) {
        this.distance = distance;
    }
}
