// Java
package za.co.wethinkcode.application;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * SQLite-backed implementation of the {@link WorldRepository}.
 * <p>
 * Provides persistence for worlds and their related data (obstacles, dimensions, etc.)
 * using JDBC and a given SQLite connection string.
 */
public class SQLiteWorldRepository implements WorldRepository {
    private final String jdbcUrl;

    /**
     * Creates a new repository that connects to the given SQLite database.
     *
     * @param jdbcUrl JDBC connection string (e.g. {@code jdbc:sqlite:worlds.db})
     */
    public SQLiteWorldRepository(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
    }

    /**
     * Retrieves a list of all saved worlds from the database.
     * <p>
     * Executes a query joining the {@code world} and {@code obstacles} tables
     * and builds a list of {@link WorldSummary} objects.
     * <p>
     * Currently, this implementation ignores obstacle details and only
     * returns the name, width, and height of each world.
     *
     * @return a list of {@link WorldSummary} objects
     * @throws RuntimeException if the query fails or the database cannot be accessed
     */
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

    @Override
    public List<WorldDetails> findAllDetails() {
        String sql = """
            SELECT w.name, w.width, w.height,
                   o.x AS ox, o.y AS oy, o.width AS ow, o.height AS oh
            FROM world w
            LEFT JOIN obstacles o ON w.id = o.world_id
            ORDER BY w.name COLLATE NOCASE
        """;
        try (Connection conn = DriverManager.getConnection(jdbcUrl);
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            Map<String, Agg> aggMap = new LinkedHashMap<>();
            while (rs.next()) {
                String name = rs.getString("name");
                Agg agg = aggMap.get(name);
                if (agg == null) {
                    agg = new Agg(rs.getInt("width"), rs.getInt("height"));
                    aggMap.put(name, agg);
                }
                Object ox = rs.getObject("ox");
                Object oy = rs.getObject("oy");
                Object ow = rs.getObject("ow");
                Object oh = rs.getObject("oh");
                if (ox != null && oy != null && ow != null && oh != null) {
                    agg.obstacles.add(new ObstacleRow(
                            ((Number) ox).intValue(),
                            ((Number) oy).intValue(),
                            ((Number) ow).intValue(),
                            ((Number) oh).intValue()
                    ));
                }
            }

            List<WorldDetails> result = new ArrayList<>();
            for (Map.Entry<String, Agg> e : aggMap.entrySet()) {
                Agg a = e.getValue();
                result.add(new WorldDetails(e.getKey(), a.width, a.height, a.obstacles));
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch all worlds with obstacles", e);
        }
    }

    private static class Agg {
        final int width;
        final int height;
        final List<ObstacleRow> obstacles = new ArrayList<>();
        Agg(int width, int height) {
            this.width = width;
            this.height = height;
        }
    }
}
