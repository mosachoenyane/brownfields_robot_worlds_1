package za.co.wethinkcode.server.world;

import za.co.wethinkcode.server.model.Direction;
import za.co.wethinkcode.server.model.Position;
import za.co.wethinkcode.server.model.Robot;

import java.util.*;

import za.co.wethinkcode.server.world.obstacles.Lake;
import za.co.wethinkcode.server.world.obstacles.Mountain;
import za.co.wethinkcode.server.world.obstacles.Obstacle;
import za.co.wethinkcode.server.world.obstacles.Pit;

/**
 * Represents the game world with a fixed size, obstacles, and robots.
 * Manages world boundaries, obstacle generation, robot tracking, and visibility.
 */
public class World {
    private final int width;
    private final int height;
    private final int visibilityRange;
    private final int maxShieldStrength;
    private final int reloadTime;
    private final int repairTime;
    private final int maxShots;
    private final List<Obstacle> obstacles;
    private final List<Robot> robots;
    private final Random random = new Random();

    public World(WorldConfig worldConfig) {
        this.width = worldConfig.getWidth();
        this.height = worldConfig.getHeight();
        this.visibilityRange = worldConfig.getVisibilityRange();
        this.maxShieldStrength = worldConfig.getMaxShieldStrength();
        this.obstacles = new ArrayList<>();
        this.robots = new ArrayList<>();
        this.reloadTime = worldConfig.getReloadTime();
        this.repairTime = worldConfig.getRepairTime();
        this.maxShots = worldConfig.getMaxShots();


        generateObstacles(worldConfig.getNumMountains(), worldConfig.getNumLakes(), worldConfig.getNumPits());
    }

    private void generateObstacles(int mountains, int lakes, int pits) {//generates each obs
        Set<Position> occupiedPositions = new HashSet<>();

        // Generate mountains
        for (int i = 0; i < mountains; i++) {
            obstacles.add(createRandomObstacle("mountain", occupiedPositions));
        }

        // Generate lakes
        for (int i = 0; i < lakes; i++) {
            obstacles.add(createRandomObstacle("lake", occupiedPositions));
        }

        // Generate pits
        for (int i = 0; i < pits; i++) {
            obstacles.add(createRandomObstacle("pit", occupiedPositions));
        }
    }

    private Obstacle createRandomObstacle(String type, Set<Position> occupied) {
        final int maxAttempts = 100;
        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            int x = random.nextInt(width);
            int y = random.nextInt(height);
            int sizeX = 1 + random.nextInt(3);
            int sizeY = 1 + random.nextInt(3);

            if (!withinBounds(x, y, sizeX, sizeY)) continue;
            List<Position> positions = getObstaclePositions(x, y, sizeX, sizeY);

            if (positions.stream().anyMatch(occupied::contains)) continue;
            occupied.addAll(positions);

            return switch (type) {
                case "mountain" -> new Mountain(x, y, sizeX, sizeY);
                case "lake" -> new Lake(x, y, sizeX, sizeY);
                case "pit" -> new Pit(x, y, sizeX, sizeY);
                default -> null ;
            };
        }
        return null;
    }

    private boolean withinBounds(int x, int y, int sizeX, int sizeY){
        return x + sizeX <= width && y + sizeY <= height;
    }

    private List<Position> getObstaclePositions(int x, int y, int sizeX, int sizeY) {
        List<Position> positions = new ArrayList<>(sizeX * sizeY);

        for (int directionX = 0; directionX < sizeX; directionX++) {
            for (int directionY = 0; directionY < sizeY; directionY++) {
                positions.add(new Position(x + directionX, y + directionY));
            }
        }
        return positions;
    }

    /**
     * Checks if a given position is within the world's boundaries.
     *
     * @param position The position to check.
     * @return True if the position is inside the world bounds; false otherwise.
     */
    public boolean isPositionValid(Position position) {
        int x = position.getX();
        int y = position.getY();
        return x >= (width * -1) && x < width &&   //Also looks at negative x
                y >= (height * -1) && y < height;  // Also looks at negative y
    }

    /**
     * Checks if a position is blocked by any obstacle.
     *
     * @param position The position to check.
     * @return True if blocked by an obstacle; false otherwise.
     */
    public boolean isPositionBlocked(Position position) {//
        for (Obstacle obstacle : obstacles) {
            if (obstacle.blocksPosition(position.getX(), position.getY())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns a list of obstacles visible to a given robot based on its position and direction.
     *
     * @param robot The robot whose visibility is checked.
     * @return List of visible obstacles.
     */
    public List<Obstacle> getVisibleObstacles(Robot robot) {
        List<Obstacle> visible = new ArrayList<>();
        Position robotPos = robot.getPosition();

        for (Obstacle obstacle : obstacles) {
            if (isObstacleVisible(obstacle, robotPos, robot.getDirection())) {
                visible.add(obstacle);
            }
        }
        return visible;
    }

    private boolean isObstacleVisible(Obstacle obstacle, Position robotPos, Direction facing) {
        int dx = obstacle.getX() - robotPos.getX();
        int dy = obstacle.getY() - robotPos.getY();

        if (manhattanDistance(dx, dy) > visibilityRange) return false;
        return isInFieldOfView(dx, dy, facing);
    }

    private int manhattanDistance(int dx, int dy) {
        return Math.abs(dx) + Math.abs(dy);
    }

    private boolean isInFieldOfView(int dx, int dy, Direction facing) {
        switch (facing) {
            case NORTH: return isFacingTheNorth(dx, dy);
            case EAST:  return isFacingTheEast(dx, dy);
            case SOUTH: return isFacingSouth(dx, dy);
            case WEST:  return isFacingWest(dx, dy);
            default:    return false;
        }
    }
    private boolean isFacingTheNorth(int dx, int dy){
        return dy >= 0 && Math.abs(dx) <= dy;
    }

    private  boolean isFacingTheEast(int dx, int dy){
        return dx >= 0 && Math.abs(dy) <= dx;
    }

    private boolean isFacingSouth(int dx,int dy){
        return dy <= 0 && Math.abs(dx) <= -dx;
    }

    private boolean isFacingWest(int dx, int dy){
        return dx <= 0 && Math.abs(dy) <= -dx;
    }

    /**
     * Adds a robot to the world.
     *
     * @param robot The robot to add.
     */
    public synchronized void addRobot(Robot robot) {
        robots.add(robot);
    }

    /**
     * Removes a robot from the world.
     *
     * @param robot The robot to remove.
     */
    public synchronized void removeRobot(Robot robot) {
        robots.remove(robot);
    }

    /**
     * Returns a copy of the list of robots currently in the world.
     *
     * @return List of robots.
     */
    public synchronized List<Robot> getRobots() {
        return new ArrayList<>(robots);
    }

    /**
     * Finds and returns a robot by name (case-insensitive).
     *
     * @param name The name of the robot.
     * @return The robot if found; null otherwise.
     */
    public synchronized Robot getRobotByName(String name) {
        for (Robot robot : robots) {
            if (robot.getName().equalsIgnoreCase(name)) {
                return robot;
            }
        }
        return null;
    }

    /**
     * Returns the world's width.
     *
     * @return Width in units.
     */
    public int getWidth() {
        return width;
    }

    /**
     * Returns the world's height.
     *
     * @return Height in units.
     */
    public int getHeight() {
        return height;
    }

    /**
     * Returns the list of obstacles present in the world.
     *
     * @return List of obstacles.
     */
    public List<Obstacle> getObstacles() {
        return obstacles;
    }

    /**
     * Returns the visibility range for robots in the world.
     *
     * @return Visibility range in units.
     */
    public int getVisibilityRange() {
        return visibilityRange;
    }

    /**
     * Returns the maximum shield strength allowed for robots.
     *
     * @return Maximum shield strength.
     */
    public int getMaxShieldStrength() {
        return maxShieldStrength;
    }



    public void addObstacle(Pit Pit) {
    }

    public void addObstacle(Lake Lake) {
    }

    public void addObstacle(Mountain mountain) {
    }

    /**
     * Returns the reload time for robot weapons.
     *
     * @return Reload time in ticks.
     */
    public int getReloadTime() {
        return reloadTime;
    }

    /**
     * Returns the maximum number of shots a robot can fire before reloading.
     *
     * @return Maximum shots.
     */
    public int getMaxShots() {
        return maxShots;
    }

    /**
     * Returns the repair time for robots.
     *
     * @return Repair time in ticks.
     */
    public int getRepairTime() {
        return repairTime;
    }

}
