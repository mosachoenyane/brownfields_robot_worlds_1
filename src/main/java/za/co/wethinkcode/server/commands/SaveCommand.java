package za.co.wethinkcode.server.commands;

import net.lemnik.eodsql.QueryTool;
import za.co.wethinkcode.server.data.ObstacleDO;
import za.co.wethinkcode.server.data.WorldDAI;
import za.co.wethinkcode.server.data.WorldDO;
import za.co.wethinkcode.server.world.World;
import za.co.wethinkcode.server.world.obstacles.Obstacle;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * The {@code SaveCommand} persists the current {@link World} configuration to the database.
 *
 * The command uses SQLite as the persistence layer and interacts with the
 * database via {@link WorldDAI}.
 */
public class SaveCommand implements Command {
    private World world;

    /**
     * Constructs a new {@code SaveCommand}.
     *
     * @param world the {@link World} instance to be persisted.
     */
    public SaveCommand(World world) {
        this.world = world;
    }

    /**
     * Executes the command to save the world and its obstacles to the database.
     * If the world name already exists, the save is skipped and a warning is returned.
     * Otherwise, a new world entry is created and all obstacles are persisted.
     *
     * @return a confirmation message indicating whether the save was successful or skipped.
     * @throws RuntimeException if a database error occurs.
     */
    @Override
    public String execute() {
        String url = "jdbc:sqlite:robot_world.db";
        try (Connection conn = DriverManager.getConnection(url)) {
            // Get the concrete DAI implementation from QueryTool
            WorldDAI dao = QueryTool.getQuery(conn, WorldDAI.class);

            // Check if the world already exists
            if (dao.findWorldByName(world.getName()) != null) {
                return "WARNING: World with name " + world.getName().toUpperCase() + " already exists";
            }

            // Create a DO and save the world
            WorldDO worldDO = new WorldDO();
            worldDO.name = world.getName();
            worldDO.height = world.getHeight();
            worldDO.width = world.getWidth();
            dao.saveWorld(worldDO);

            // Save obstacles with the new world's ID
            // We need to retrieve the new ID after saving the world
            WorldDO newWorld = dao.findWorldByName(world.getName());
            if (newWorld != null) {
                for (Obstacle obstacle : world.getObstacles()) {
                    ObstacleDO obstacleDO = new ObstacleDO();
                    obstacleDO.x = obstacle.getX();
                    obstacleDO.y = obstacle.getY();
                    obstacleDO.width = obstacle.getWidth();
                    obstacleDO.height = obstacle.getHeight();
                    dao.saveObstacle(obstacleDO, newWorld.id);
                }
            }
            return "World Data Successfully Saved";
        } catch (SQLException e) {
            throw new RuntimeException("Database error: " + e.getMessage(), e);
        }
    }

    /**
     * Returns the name of this command.
     *
     * @return the string {@code "save"}.
     */
    @Override
    public String getName() {
        return "save";
    }

    /**
     * Returns a human-readable description of the command's action.
     * In this case, it executes the save and returns the resulting message.
     *
     * @return the result of the {@link #execute()} method.
     */
    @Override
    public String display() {
        return execute();
    }
}