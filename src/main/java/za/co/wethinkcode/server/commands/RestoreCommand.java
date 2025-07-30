package za.co.wethinkcode.server.commands;

import za.co.wethinkcode.server.world.World;
import za.co.wethinkcode.server.world.obstacles.Obstacle;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RestoreCommand implements Command {
    private final World world;
    private final String worldName;

    public RestoreCommand (World world, String worldName) {
        this.world = world;
        this.worldName = worldName;
    }

    @Override
    public String execute() {
        String url = "jdbc:sqlite:group.db";
        try (Connection conn = DriverManager.getConnection(url)) {
            /*Find the world record*/
            String selectWorldQuery = "SELECT height, width FROM world WHERE name = ?";
            try (PreparedStatement worldStmnt = conn.prepareStatement(selectWorldQuery)) {
                worldStmnt.setString(1, worldName);
                ResultSet worldRs = worldStmnt.executeQuery();
                if (!worldRs.next()) {
                    return "ERROR: World named " + worldName.toUpperCase() + " does not exist.";
                }
                int height = worldRs.getInt("height");
                int width = worldRs.getInt("width");

                /* Set world properties*/
                world.setHeight(height);
                world.setWidth(width);

                /* Load obstacles */
                List<Obstacle> obstacles = new ArrayList<>();
                String selectObstaclesQuery = "SELECT x, y, width, height FROM obstacles";
                try (Statement obsStmnt = conn.createStatement()) {
                    ResultSet obsRs = obsStmnt.executeQuery(selectObstaclesQuery);
                    while (obsRs.next()) {
                        int x = obsRs.getInt("x");
                        int y = obsRs.getInt("y");
                        int oWidth = obsRs.getInt("width");
                        int oHeight = obsRs.getInt("height");
                        obstacles.add(new Obstacle(x, y, oWidth, oHeight) {
                            @Override
                            public boolean blocksVisibility() {
                                return false;
                            }
                            @Override
                            public String getType() {
                                return "";
                            }
                        });
                    }
                }
                world.setObstacles(obstacles);

                return "World " + worldName.toUpperCase() + " successfully restored!";
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getName() {
        return "restore";
    }

    @Override
    public String display() {
        return execute();
    }
}