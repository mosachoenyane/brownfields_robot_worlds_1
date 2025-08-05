package za.co.wethinkcode.server.commands;

import za.co.wethinkcode.server.world.World;

import java.sql.*;

public class SaveCommand implements Command{
    World world;
    public SaveCommand(World world) {
        this.world = world;
    }
    @Override
    public String execute() {
        String url = "jdbc:sqlite:robot_world.db";
        try (Connection conn = DriverManager.getConnection(url)) {
            System.out.println("SUCCESSFUL CONNECTION !");
            Statement stmnt = conn.createStatement();
            stmnt.executeUpdate("CREATE TABLE IF NOT EXISTS world (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, height INTEGER, width INTEGER)");
            stmnt.executeUpdate("CREATE TABLE IF NOT EXISTS obstacles (id INTEGER PRIMARY KEY AUTOINCREMENT, x INTEGER, y INTEGER, width INTEGER, height INTEGER, world_id INTEGER, FOREIGN KEY (world_id) REFERENCES world(id))");

//            stmnt.executeUpdate("DROP TABLE IF EXISTS world");
//            stmnt.executeUpdate("DROP TABLE IF EXISTS obstacles");


            // Check if the world NAME already exists in the WORLD TABLE
            String checkQuery = "SELECT COUNT(*) FROM world WHERE name = ?";
            try (PreparedStatement checkStmnt = conn.prepareStatement(checkQuery)) {
                checkStmnt.setString(1, world.getName());
                ResultSet rs = checkStmnt.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    return "WARNING: World with name " + world.getName().toUpperCase() + " already exists";
                }
            }
            String insertWorldQuery = "INSERT INTO world (name, height, width) VALUES (?, ?, ?)";
            try (PreparedStatement pstmntWorld = conn.prepareStatement(insertWorldQuery)) {
                pstmntWorld.setString(1, world.getName());
                pstmntWorld.setInt(2, world.getHeight());
                pstmntWorld.setInt(3, world.getWidth());
                pstmntWorld.executeUpdate();
                    }
                String insertObjQuery = "INSERT INTO obstacles (x, y, width, height, world_id) VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement pstmntObj = conn.prepareStatement(insertObjQuery)) {
                    for (var obstacle : world.getObstacles()) {
                        pstmntObj.setInt(1, obstacle.getX());
                        pstmntObj.setInt(2, obstacle.getY());
                        pstmntObj.setInt(3, obstacle.getWidth());
                        pstmntObj.setInt(4, obstacle.getHeight());
                        // Retrieve the last inserted world ID
                        String lastWorldIdQuery = "SELECT id FROM world WHERE name = ?";
                        try (PreparedStatement lastWorldIdStmnt = conn.prepareStatement(lastWorldIdQuery)) {
                            lastWorldIdStmnt.setString(1, world.getName());
                            ResultSet rs = lastWorldIdStmnt.executeQuery();
                            if (rs.next()) {
                                int worldId = rs.getInt("id");
                                pstmntObj.setInt(5, worldId); // Set the world_id for each obstacle
                            } else {
                                throw new SQLException("Failed to retrieve the last inserted world ID.");
                            }
                        }
                        pstmntObj.executeUpdate();
                    }
                    }


                String selectQuery = "SELECT * FROM world";
                try (Statement retrieveStmnt = conn.createStatement()){
                    ResultSet rs = retrieveStmnt.executeQuery(selectQuery);
                    while(rs.next()){
                        int id = rs.getInt("id");
                        String name = rs.getString("name");
                        int height = rs.getInt("height");
                        int width = rs.getInt("width");
                        System.out.println("ID: " + id + ", Name: " + name + ", Height: " + height + ", Width: " + width);
                    }

                    String selectQuery1 = "SELECT * FROM obstacles";
                    try (Statement retrieveStmnt1 = conn.createStatement()){
                        ResultSet rs1 = retrieveStmnt1.executeQuery(selectQuery1);
                        while(rs.next()){
                            int id = rs1.getInt("id");
                            int x = rs1.getInt("x");
                            int y = rs1.getInt("y");
                            int height = rs1.getInt("height");
                            int width = rs1.getInt("width");
                            System.out.println("ID: " + id + " x: " + x + ", y: " + y);
                        }


                    }
            return "World Data Successfully Saved";
            }  catch (SQLException e) {
                throw new RuntimeException(e);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
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
