package za.co.wethinkcode.saveWorldAcceptance;

import org.junit.jupiter.api.*;
import za.co.wethinkcode.client.RobotWorldClient;
import za.co.wethinkcode.client.RobotWorldJsonClient;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;

import static org.junit.jupiter.api.Assertions.*;

public class SaveObstaclesTests {
    private final static int DEFAULT_PORT = 5000;
    private final static String DEFAULT_IP = "localhost";
    private final RobotWorldClient serverClient = new RobotWorldJsonClient();
    Process process;

    @BeforeEach
    void connectToServer() throws IOException, InterruptedException {
        try {
            ProcessBuilder pb = new ProcessBuilder("java", "-jar", "target/robot-world-0.0.2.jar", "-s", "10", "-o", "1,1");
//      pb.inheritIO(); // Inherit standard input/output/error streams
            pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
            pb.redirectError(ProcessBuilder.Redirect.INHERIT);
            pb.redirectInput(ProcessBuilder.Redirect.PIPE);
            process = pb.start();
            serverClient.connect(DEFAULT_IP, DEFAULT_PORT);
            Thread.sleep(2000);
            //Files.deleteIfExists(Paths.get("robot_world.db"));
        }catch (Exception e){
            serverClient.connect(DEFAULT_IP, DEFAULT_PORT);
        }
    }

    @AfterEach
    void disconnectFromServer() throws InterruptedException {
        serverClient.disconnect();
        process.destroy();
        Thread.sleep(1000);
    }

    @Test
    void ObstaclesIsSaved() throws IOException, InterruptedException, SQLException {
        // Send the save command
//        try (OutputStream out = process.getOutputStream();
//             PrintWriter writer = new PrintWriter(new OutputStreamWriter(out))) {
//            writer.println("save"); // Use println to ensure a newline is sent
//            writer.flush();
//            Thread.sleep(2000); // Increased sleep to ensure server processes the command
//        }
        // check if the database file exists
        assertTrue(Files.exists(Paths.get("robot_world.db")));

        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:robot_world.db")) {
            // check if connection is open
            assertFalse(connection.isClosed());
            try (PreparedStatement stmt = connection.prepareStatement("SELECT * FROM obstacles");
                 ResultSet result = stmt.executeQuery()) {
                assertTrue(result.next(), "Obstacle should be saved in the obstacles table");
                Object x = result.getObject("x");
                Object y = result.getObject("y");
                Object width = result.getObject("width");
                Object height = result.getObject("height");
                assertTrue(x instanceof Integer);
                assertTrue(y instanceof Integer);
                assertTrue(width instanceof Integer);
                assertTrue(height instanceof Integer);

            }
        }

    }


}



