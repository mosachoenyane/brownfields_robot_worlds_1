package za.co.wethinkcode.server.world.obstacles;

import za.co.wethinkcode.server.model.Position;

/**
 * Represents a Mine obstacle in the robot world.
 * Mines occupy a single cell and block visibility.
 */
public class Mine extends Obstacle {

    /**
     * Creates a Mine at the specified coordinates.
     *
     * @param x The X-coordinate of the mine.
     * @param y The Y-coordinate of the mine.
     */
    public Mine(int x, int y) {
        super(x, y, 1, 1);
    }

    /**
     * Creates a Mine at the position of a Position object.
     *
     * @param position The position where the mine should be placed.
     */
    public Mine(Position position){
        super(position.getX(), position.getY(), 1, 1);
    }

    /**
     * Indicates that this obstacle blocks visibility.
     *
     * @return true, since mines block visibility.
     */
    @Override
    public boolean blocksVisibility() {
        return true;
    }

    /**
     * Returns the type of obstacle as a string.
     *
     * @return "mine" to represent this obstacle type.
     */
    @Override
    public String getType() {
        return "mine";
    }
}