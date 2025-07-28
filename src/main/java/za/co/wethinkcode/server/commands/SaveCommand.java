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
            stmnt.executeUpdate("CREATE TABLE IF NOT EXISTS world (id INTEGER, name TEXT, height INTEGER, width INTEGER)");

            String insertQuery = "INSERT INTO world (id, name, height, width) VALUES (?, ?, ?, ?)";
            try (PreparedStatement pstmnt = conn.prepareStatement(insertQuery)){
                pstmnt.setInt(1, 1);
                pstmnt.setString(2, "Jungle");
                pstmnt.setInt(3, 2);
                pstmnt.setInt(4, 2);
                pstmnt.executeUpdate();
            }  catch (SQLException e) {
                throw new RuntimeException(e);
            }

            return "World Table Created Successfully";

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getName() {
        return "";
    }

    @Override
    public String display() {
        return "";
    }
}
