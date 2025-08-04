package za.co.wethinkcode.server.commands;

import za.co.wethinkcode.server.RobotWorldServer;
import za.co.wethinkcode.server.world.World;
import za.co.wethinkcode.server.world.obstacles.Mountain;
import za.co.wethinkcode.server.world.obstacles.Obstacle;

import java.io.FileInputStream;
import java.io.FileOutputStream;
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
            // Debugging: Verify database connection
            System.out.println("Connected to database: " + url);

            // Query to fetch world data
            String selectWorldQuery = "SELECT id, TRIM(name, '\"') AS name, height, width FROM world WHERE UPPER(TRIM(name, '\"')) = UPPER(?)";

            try (PreparedStatement worldStmnt = conn.prepareStatement(selectWorldQuery)) {
                // Set the world name parameter
                worldStmnt.setString(1, worldName);

                try (ResultSet worldRs = worldStmnt.executeQuery()) {
                    if (!worldRs.next()) {

                        try (PreparedStatement dumpStmnt = conn.prepareStatement("SELECT id, name, height, width FROM world")) {
                            try (ResultSet dumpRs = dumpStmnt.executeQuery()) {
                                System.out.println("World table contents:");
                                boolean hasRows = false;
                                while (dumpRs.next()) {
                                    hasRows = true;
//                                    System.out.println("ID: " + dumpRs.getInt("id") + ", Name: '" + dumpRs.getString("name") +
//                                            "', Height: " + dumpRs.getInt("height") + ", Width: " + dumpRs.getInt("width"));
                                }
                                if (!hasRows) {
                                    System.out.println("World table is empty.");
                                }
                            }
                        }
                        System.out.println("No world found with name: " + worldName);
                        return "ERROR: World named " + worldName + " does not exist.";
                    }

                    // Process world data
                    int worldId = worldRs.getInt("id");
                    String retrievedWorldName = worldRs.getString("name"); // TRIMmed name
                    System.out.println(retrievedWorldName);
                    int height = worldRs.getInt("height");
                    System.out.println(height);
                    int width = worldRs.getInt("width");

                    // Update world object
                    world.setHeight(height);
                    world.setWidth(width);
                    world.getObstacles().clear();
                    System.out.println("World properties set: Height=" + height + ", Width=" + width);

                    // Query to fetch obstacles
                    String selectObstaclesQuery = "SELECT x, y, width, height FROM obstacles WHERE world_id = ?";

                    try (PreparedStatement obstacleStmnt = conn.prepareStatement(selectObstaclesQuery)) {
                        obstacleStmnt.setInt(1, worldId);

                        try (ResultSet obstacleRs = obstacleStmnt.executeQuery()) {
                            boolean hasObstacles = false;
                            while (obstacleRs.next()) {
                                int objectX = obstacleRs.getInt("x");
                                int objectY = obstacleRs.getInt("y");
                                int objectWidth = obstacleRs.getInt("width");
                                int objectHeight = obstacleRs.getInt("height");
                                world.addObstacle(new Mountain(objectX, objectY, objectWidth, objectHeight));
                                hasObstacles = true;
                            }
                            if (!hasObstacles) {
                                System.out.println("No obstacles found for world: " + retrievedWorldName);
                            }
                        }
                    }
                    Properties properties = new Properties();
                    try (FileInputStream input = new FileInputStream("src/main/resources/config.properties")) {
                        properties.load(input); // Load existing properties
                    } catch (IOException e) {
                        System.err.println("Failed to load config.properties: " + e.getMessage());
                        return "ERROR: Failed to load configuration for " + worldName + ": " + e.getMessage();
                    }
                    // Update config.properties
                    try (FileOutputStream output = new FileOutputStream("src/main/resources/config.properties")) {
                        properties.setProperty("WORLD_NAME", retrievedWorldName);
                        properties.setProperty("WORLD_HEIGHT", String.valueOf(height));
                        properties.setProperty("WORLD_WIDTH", String.valueOf(width));
                        properties.store(output, "Updated world configuration for " + retrievedWorldName);
                        System.out.println("Updated config.properties with world: " + retrievedWorldName);
                    } catch (IOException e) {
                        System.err.println("Failed to update config.properties: " + e.getMessage());
                        return "ERROR: Failed to update configuration for " + worldName + ": " + e.getMessage();
                    }

                    // Restart server
                    System.out.println("Restarting server for world: " + retrievedWorldName);
                    restartServer();

                    return "World " + retrievedWorldName + " successfully restored!";
                }
            }
        } catch (SQLException e) {
            System.err.println("Database error for world " + worldName + ": " + e.getMessage());
            return "ERROR: Database error for " + worldName + ": " + e.getMessage();
        }
    }

    @Override
    public String getName() {
        return "";
    }

    @Override
    public String display() {
        return "World to be loaded";
    }
    public String restartServer() {
        StringBuilder response = new StringBuilder("Disconnecting all robots:\n");

        // Disconnect robots one by one with a brief pause
        world.getRobots().forEach(robot -> {
            String robotName = robot.getName();
            response.append("- ").append(robotName).append("\n");
            System.out.println("Disconnecting robot: " + robotName);
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        response.append("Server shutting down...");
        System.out.println("Server shutting down...");

        // Close the current server
        RobotWorldServer.close();

        // Restart the server in a new thread
        Thread newServerThread = new Thread(() -> {
            try {
                Thread.sleep(5000);
                String[] args = {};
                RobotWorldServer.main(args);
            } catch (Exception e) {
                System.err.println("Failed to restart server: " + e.getMessage());
                e.printStackTrace();
            }
        });
        newServerThread.start();

        return response.toString();
    }
}