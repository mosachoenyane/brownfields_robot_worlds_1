// Java
package za.co.wethinkcode.application;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SQLiteWorldRepository implements WorldRepository {
    private final String jdbcUrl;

    public SQLiteWorldRepository(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
    }

    @Override
    public List<WorldSummary> findAll() {
        List<WorldSummary> worlds = new ArrayList<>();
        String sql = "SELECT name, height, width FROM world ORDER BY name COLLATE NOCASE";
        try (Connection conn = DriverManager.getConnection(jdbcUrl);
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                worlds.add(new WorldSummary(
                        rs.getString("name"),
                        rs.getInt("width"),
                        rs.getInt("height")
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch worlds", e);
        }
        return worlds;
    }
}
