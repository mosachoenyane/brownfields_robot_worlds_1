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

public class SaveCommand implements Command {
    private World world;

    public SaveCommand(World world) {
        this.world = world;
    }

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

    @Override
    public String getName() {
        return "save";
    }

    @Override
    public String display() {
        return execute();
    }
}