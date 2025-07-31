package za.co.wethinkcode.saveWorldAcceptance;

import org.junit.jupiter.api.*;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class SaveWorldTests {
    Process process;

    @BeforeEach
    void connectToServer() throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder("java", "-jar", "target/robot-world-0.0.2.jar","-s","10");
//        pb.inheritIO(); // Inherit standard input/output/error streams
        pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
        pb.redirectError(ProcessBuilder.Redirect.INHERIT);
        pb.redirectInput(ProcessBuilder.Redirect.PIPE);
        process = pb.start();
        Thread.sleep(2000);
        Files.deleteIfExists(Paths.get("group.db"));

    }

    @AfterEach
    void disconnectFromServer() throws InterruptedException {
        process.destroy();
        Thread.sleep(1000);
    }
    @Test
    void worldIsSaved() throws IOException, InterruptedException, SQLException {
        // Send the save command
        try (OutputStream out = process.getOutputStream();
             PrintWriter writer = new PrintWriter(new OutputStreamWriter(out))) {
            writer.println("save"); // Use println to ensure a newline is sent
            writer.flush();
            Thread.sleep(2000); // Increased sleep to ensure server processes the command
        }

        // check if the database file exists
        assertTrue(Files.exists(Paths.get("group.db")));

        // Connect to the database
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:group.db")) {
            // check if connection is open
            assertFalse(connection.isClosed());

            // query the world table
            try (PreparedStatement stmt = connection.prepareStatement("SELECT * FROM world");
                 ResultSet result = stmt.executeQuery()) {

                assertTrue(result.next(), "No rows found in the world table");

                // verify with the  schema check name
                String worldName = result.getString("Name");
                //System.out.println("World Name: " + worldName);
                //assertTrue("SUCCESSFUL CONNECTION !"));
                assertEquals(10,result.getInt("Height"));
                assertEquals(10,result.getInt("Height"));
                // Assert the world name
                assertEquals(worldName, worldName);
            }
        }

    }
}
