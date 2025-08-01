package za.co.wethinkcode.server.commands;

import za.co.wethinkcode.server.world.World;
import za.co.wethinkcode.server.world.obstacles.Mountain;
import za.co.wethinkcode.server.world.obstacles.Obstacle;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class RestoreCommand implements Command {
    World world;
    public Properties properties;
    private String worldName;


    public RestoreCommand(World world, String worldName) {
        this.world = world;
        this.worldName = worldName;
        this.execute();

    }

    @Override
    public String execute() {
        String url = "jdbc:sqlite:robot_world.db";
        try (Connection conn = DriverManager.getConnection(url)) {
            /*Find the world record*/
            String selectWorldQuery = "SELECT * FROM world WHERE world.name LIKE " + worldName + " JOIN obstacles ON world.id = obstacles.world_id";
            System.out.println(selectWorldQuery);
            try (PreparedStatement worldStmnt = conn.prepareStatement(selectWorldQuery)) {
                ResultSet worldRs = worldStmnt.executeQuery();
                if (worldRs.next()) {
                    String worldName = worldRs.getString("name");
                    int height = worldRs.getInt("height");
                    int width = worldRs.getInt("width");
                    System.out.println(height + " " + width);
                    int objectX = worldRs.getInt("x");
                    int objectY = worldRs.getInt("y");
                    int objectWidth = worldRs.getInt("width");
                    int objectHeight = worldRs.getInt("height");
                    System.out.println("Data successfully print out");

                    world.setHeight(height);
                    world.setWidth(width);
                    world.getObstacles().clear();
                    world.addObstacle(new Mountain(objectX, objectY, objectWidth, objectHeight));



//                    // Rewrite WorldRs data to config.properties
//                    try (InputStream input = new FileInputStream("config.properties"){
//                        Properties properties = new Properties();
//                        properties.load(input);
//
//                        // Set the properties
//                        properties.setProperty("WORLD_NAME", worldName);
//                        properties.setProperty("WORLD_HEIGHT", String.valueOf(height));
//                        properties.setProperty("WORLD_WIDTH", String.valueOf(width));
//                        System.out.println("World name: " + worldName + ", Height: " + height + ", Width: " + width);
//                        properties.setProperty("obstacle.x", String.valueOf(ObjectX));
//                        properties.setProperty("obstacle.y", String.valueOf(ObjectY));
//
//                }else {
//                    return "ERROR: World named " + " does not exist.";
//                }


                    return "World " + " successfully restored!";
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return "";
    }

    @Override
    public String getName() {
        return "";
    }

    @Override
    public String display() {
        return "World to be loaded";
    }
}