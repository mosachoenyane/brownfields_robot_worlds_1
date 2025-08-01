package za.co.wethinkcode.server.commands;

import za.co.wethinkcode.server.world.World;
import za.co.wethinkcode.server.world.obstacles.Obstacle;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class RestoreCommand implements Command {

    public Properties properties;
    private final String worldName;

    public RestoreCommand (String name, String uh) {

        worldName = name;
    }

    @Override
    public String execute() {
        String url = "jdbc:sqlite:robot_world.db";
        try (Connection conn = DriverManager.getConnection(url)) {
            /*Find the world record*/
            String selectWorldQuery = "SELECT * FROM world";
            try (PreparedStatement worldStmnt = conn.prepareStatement(selectWorldQuery)) {
                ResultSet worldRs = worldStmnt.executeQuery();
                if (worldRs.next()) {
                    String worldName = worldRs.getString("name");
                    int height = worldRs.getInt("height");
                    int width = worldRs.getInt("width");
                    System.out.println(height+ " "+ width);
//                    int ObjectX = worldRs.getInt("x");
//                    int ObjectY = worldRs.getInt("y");
                    System.out.println("Data successfully print out");

                    // Rewrite WorldRs data to config.properties
                    properties = new Properties();

                    properties.setProperty("world.name", worldName);
                    properties.setProperty("world.height", String.valueOf(height));
                    properties.setProperty("world.width", String.valueOf(width));
//                    properties.setProperty("obstacle.x", String.valueOf(ObjectX));
//                    properties.setProperty("obstacle.y", String.valueOf(ObjectY));

                }else {
                    return "ERROR: World named " + " does not exist.";
                }





//                /* Load obstacles */
//                List<Obstacle> obstacles = new ArrayList<>();
//                String selectObstaclesQuery = "SELECT * FROM world JOIN obstacles ON world.id = obstacles.world_id WHERE world.name = ?";
//                try (Statement obsStmnt = conn.createStatement()) {
//                    ResultSet obsRs = obsStmnt.executeQuery(selectObstaclesQuery);
//                    while (obsRs.next()) {
//                        int x = obsRs.getInt("x");
//                        int y = obsRs.getInt("y");
//                        System.out.println("x: " + x + ", y: " + y);
//                        int oWidth = obsRs.getInt("width");
//                        int oHeight = obsRs.getInt("height");
//                        obstacles.add(new Obstacle(x, y, oWidth, oHeight) {
//                            @Override
//                            public boolean blocksVisibility() {
//                                return false;
//                            }
//                            @Override
//                            public String getType() {
//                                return "";
//                            }
//                        });
//                    }
//                }
//                world.setObstacles(obstacles);

                return "World " +" successfully restored!";
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