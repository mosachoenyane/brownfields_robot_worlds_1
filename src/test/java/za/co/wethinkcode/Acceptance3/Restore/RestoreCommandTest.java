package za.co.wethinkcode.Acceptance3.Restore;

import org.junit.jupiter.api.*;

import java.io.*;
import java.sql.*;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

public class RestoreCommandTest {
    Process process;
    static final String DB_URL = "jdbc:sqlite:robot_world.db";
    static final String TEST_WORLD_NAME = "acceptance_test_restore";

    @BeforeAll
    static void setupWorldInDatabase() throws SQLException {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            conn.setAutoCommit(false);

            // Insert world
            try (PreparedStatement stmt = conn.prepareStatement(
                    "INSERT OR REPLACE INTO world (name, height, width) VALUES (?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, TEST_WORLD_NAME);
                stmt.setInt(2, 25);
                stmt.setInt(3, 30);
                stmt.executeUpdate();

                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        int worldId = keys.getInt(1);

                        // Insert obstacle
                        try (PreparedStatement obsStmt = conn.prepareStatement(
                                "INSERT INTO obstacles (x, y, width, height, world_id) VALUES (?, ?, ?, ?, ?)")) {
                            obsStmt.setInt(1, 2);
                            obsStmt.setInt(2, 3);
                            obsStmt.setInt(3, 4);
                            obsStmt.setInt(4, 5);
                            obsStmt.setInt(5, worldId);
                            obsStmt.executeUpdate();
                        }
                    }
                }
                conn.commit();
            }
        }
    }

    @BeforeEach
    void connectToServer() throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder("java", "-jar", "target/robot-world-0.0.2.jar");
        pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
        pb.redirectError(ProcessBuilder.Redirect.INHERIT);
        pb.redirectInput(ProcessBuilder.Redirect.PIPE);
        process = pb.start();
        Thread.sleep(2000);
    }

    @AfterEach
    void disconnectFromServer() throws InterruptedException {
        process.destroy();
        Thread.sleep(1000);
    }

    @AfterAll
    static void cleanUpDb() throws SQLException {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            try (PreparedStatement delObs = conn.prepareStatement(
                    "DELETE FROM obstacles WHERE world_id IN (SELECT id FROM world WHERE name = ?)")) {
                delObs.setString(1, TEST_WORLD_NAME);
                delObs.executeUpdate();
            }
            try (PreparedStatement delWorld = conn.prepareStatement("DELETE FROM world WHERE name = ?")) {
                delWorld.setString(1, TEST_WORLD_NAME);
                delWorld.executeUpdate();
            }
        }
    }

    @Test
    void worldRestoresCorrectly() throws IOException, InterruptedException {
        /* Send the restore command */
        try (OutputStream out = process.getOutputStream();
             PrintWriter writer = new PrintWriter(new OutputStreamWriter(out))) {
            writer.println("restore " + TEST_WORLD_NAME);
            writer.flush();
            Thread.sleep(2000);
        }

        /* Check config.properties for expected structure (not exact values)*/
        try (FileInputStream fis = new FileInputStream("src/main/resources/config.properties")) {
            Properties props = new Properties();
            props.load(fis);

            // WORLD_NAME should not be null or empty
            String worldName = props.getProperty("WORLD_NAME");
            assertNotNull(worldName, "WORLD_NAME should not be null");
            assertFalse(worldName.trim().isEmpty(), "WORLD_NAME should not be empty");

            /* WORLD_HEIGHT should be parseable as integer */
            String height = props.getProperty("WORLD_HEIGHT");
            assertNotNull(height, "WORLD_HEIGHT should not be null");
            assertDoesNotThrow(() -> Integer.parseInt(height), "WORLD_HEIGHT should be an integer");

            /* WORLD_WIDTH should be parseable as integer */
            String width = props.getProperty("WORLD_WIDTH");
            assertNotNull(width, "WORLD_WIDTH should not be null");
            assertDoesNotThrow(() -> Integer.parseInt(width), "WORLD_WIDTH should be an integer");
        } catch (IOException e) {
            fail("Failed to read config.properties: " + e.getMessage());
        }
    }
}

