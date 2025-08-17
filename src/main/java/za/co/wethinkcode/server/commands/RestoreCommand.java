package za.co.wethinkcode.server.commands;

import net.lemnik.eodsql.QueryTool;
import za.co.wethinkcode.server.data.ObstacleDO;
import za.co.wethinkcode.server.data.WorldDAI;
import za.co.wethinkcode.server.data.WorldDO;
import za.co.wethinkcode.server.world.World;
import za.co.wethinkcode.server.world.obstacles.Mountain;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Command to restore a saved {@link World} configuration from the database.
 *
 * If the world does not exist in the database, an error message is returned.
 */
public class RestoreCommand implements Command {
    private World world;
    private String name;

    /**
     * Creates a new {@code RestoreCommand}.
     *
     * @param world     the in-memory {@link World} object to update
     * @param worldName the name of the world to restore from the database
     */
    public RestoreCommand(World world, String worldName) {
        this.world = world;
        this.name = worldName;
    }

    /**
     * Executes the restore operation by querying the database for the world and its obstacles.
     *
     * @return a success message if the world is restored, or an error message if not found
     * @throws RuntimeException if a database error occurs
     */
    @Override
    public String execute() {
        String url = "jdbc:sqlite:robot_world.db";
        try (Connection conn = DriverManager.getConnection(url)) {
            WorldDAI dao = QueryTool.getQuery(conn, WorldDAI.class);

            // Find the world by name
            WorldDO worldDO = dao.findWorldByName(this.name);

            if (worldDO == null) {
                return "ERROR: World named " + this.name + " does not exist.";
            }

            // Update the world object from the DO
            world.setName(worldDO.name);
            world.setHeight(worldDO.height);
            world.setWidth(worldDO.width);
            world.getObstacles().clear();

            // Find obstacles and add them to the world
            for (ObstacleDO obstacleDO : dao.findObstaclesByWorldId(worldDO.id)) {
                world.addObstacle(new Mountain(obstacleDO.x, obstacleDO.y, obstacleDO.width, obstacleDO.height));
            }

            return "World " + world.getName() + " successfully restored!";
        } catch (SQLException e) {
            throw new RuntimeException("Database error: " + e.getMessage(), e);
        }
    }

    /**
     * @return the name of this command ("restore")
     */
    @Override
    public String getName() {
        return "restore";
    }

    /**
     * @return a description of what this command does
     */
    @Override
    public String display() {
        return "World to be loaded";
    }
}