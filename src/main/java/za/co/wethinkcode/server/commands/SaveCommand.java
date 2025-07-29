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
        String url = "jdbc:sqlite:group.db";
        try (Connection conn = DriverManager.getConnection(url)) {
            System.out.println("SUCCESSFUL CONNECTION !");
            Statement stmnt = conn.createStatement();
            stmnt.executeUpdate("CREATE TABLE IF NOT EXISTS world (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, height INTEGER, width INTEGER)");

            // Check if the world NAME already exists in the WORLD TABLE
            String checkQuery = "SELECT COUNT(*) FROM world WHERE name = ?";
            try (PreparedStatement checkStmnt = conn.prepareStatement(checkQuery)) {
                checkStmnt.setString(1, world.getName());
                ResultSet rs = checkStmnt.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    return "WARNING: World with name '" + world.getName().toUpperCase() + "' already exists";
                }
            }



            String insertQuery = "INSERT INTO world (name, height, width) VALUES (?, ?, ?)";
            try (PreparedStatement pstmnt = conn.prepareStatement(insertQuery)){
                //pstmnt.setInt(1,1);
                pstmnt.setString(1, world.getName());
                pstmnt.setInt(2, world.getHeight());
                pstmnt.setInt(3, world.getWidth());
                pstmnt.executeUpdate();

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
