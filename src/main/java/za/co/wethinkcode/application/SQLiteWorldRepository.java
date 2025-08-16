// Java
package za.co.wethinkcode.application;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SQLiteWorldRepository implements WorldRepository {
    private final String jdbcUrl;

    public SQLiteWorldRepository(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
    }

    @Override
    public List<WorldSummary> findAll() {
        List<WorldSummary> worlds = new ArrayList<>();
        String sql = "SELECT world.name, world.height, world.width, obstacles.x, obstacles.y, obstacles.width, obstacles.height  FROM world JOIN obstacles ON world.id = obstacles.world_id ORDER BY name COLLATE NOCASE";
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

    @Override
    public Optional<WorldDetails> findByName(String name) {
        String sql = """
            SELECT w.name, w.width, w.height,
                   o.x AS ox, o.y AS oy, o.width AS ow, o.height AS oh
            FROM world w
            LEFT JOIN obstacles o ON w.id = o.world_id
            WHERE LOWER(w.name) = LOWER(?)
        """;
        try (Connection conn = DriverManager.getConnection(jdbcUrl);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                String worldName = null;
                int width = 0, height = 0;
                List<ObstacleRow> obstacles = new ArrayList<>();
                boolean anyRow = false;

                while (rs.next()) {
                    if (!anyRow) {
                        anyRow = true;
                        worldName = rs.getString("name");
                        width = rs.getInt("width");
                        height = rs.getInt("height");
                    }
                    Object ox = rs.getObject("ox");
                    Object oy = rs.getObject("oy");
                    Object ow = rs.getObject("ow");
                    Object oh = rs.getObject("oh");
                    if (ox != null && oy != null && ow != null && oh != null) {
                        obstacles.add(new ObstacleRow(
                                ((Number) ox).intValue(),
                                ((Number) oy).intValue(),
                                ((Number) ow).intValue(),
                                ((Number) oh).intValue()
                        ));
                    }
                }

                if (!anyRow) {
                    return Optional.empty();
                }
                return Optional.of(new WorldDetails(worldName, width, height, obstacles));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch world by name: " + name, e);
        }
    }
}
