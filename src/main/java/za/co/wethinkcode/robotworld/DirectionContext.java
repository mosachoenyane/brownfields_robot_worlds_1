package za.co.wethinkcode.robotworld;  // adjust to your package

import com.google.gson.JsonArray;
import za.co.wethinkcode.server.model.Position;

import java.util.Set;

public class DirectionContext {
    private final JsonArray objects;
    private final Set<String> reportedDirections;
    private final String directionName;
    private Position current;
    private int distance;

    public DirectionContext(JsonArray objects, Set<String> reportedDirections,
                            String directionName, Position current, int distance) {
        this.objects = objects;
        this.reportedDirections = reportedDirections;
        this.directionName = directionName;
        this.current = current;
        this.distance = distance;
    }

    public JsonArray getObjects() {
        return objects;
    }

    public Set<String> getReportedDirections() {
        return reportedDirections;
    }

    public String getDirectionName() {
        return directionName;
    }

    public Position getCurrent() {
        return current;
    }

    public void setCurrent(Position current) {
        this.current = current;
    }

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }
}
