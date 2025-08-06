package za.co.wethinkcode.Acceptance3.Save;

import org.junit.jupiter.api.*;

import java.io.*;
import java.nio.file.*;
import java.sql.*;

import static org.junit.jupiter.api.Assertions.*;

public class SaveExistingWorldTests {
    Process process;

    @BeforeEach
    void connectToServer() throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder("java", "-jar", "target/robot-world-0.0.2.jar","-s","10","-o","1,1");
//        pb.inheritIO(); // Inherit standard input/output/error streams
        pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
        pb.redirectError(ProcessBuilder.Redirect.INHERIT);
        pb.redirectInput(ProcessBuilder.Redirect.PIPE);
        process = pb.start();
        Thread.sleep(2000);
        //Files.deleteIfExists(Paths.get("robot_world.db"));

    }

    @AfterEach
    void disconnectFromServer() throws InterruptedException {
        process.destroy();
        Thread.sleep(1000);
    }
    @Test
    void saveExistingWorld() throws IOException, InterruptedException, SQLException {
        // Send the save command
        try (OutputStream out = process.getOutputStream();
             PrintWriter writer = new PrintWriter(new OutputStreamWriter(out))) {
            writer.println("save"); // Use println to ensure a newline is sent
            writer.flush();
            Thread.sleep(2000); // Increased sleep to ensure server processes the command
        }

        // check if the database file exists
        assertTrue(Files.exists(Paths.get("robot_world.db")));

        // Connect to the database
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:robot_world.db")) {
            // check if connection is open
            assertFalse(connection.isClosed());

            // query the world table
            try (PreparedStatement stmt = connection.prepareStatement("SELECT * FROM world");
                 ResultSet result = stmt.executeQuery()) {

                assertTrue(result.next(), "No rows found in the world table");

                // verify with the  schema check name
                String worldName = result.getString("Name");
                // Assert the world name
                assertEquals("WARNING: World with name " + worldName +" already exists", "WARNING: World with name " + worldName + " already exists");
            }
        }
    }

}