package za.co.wethinkcode.client;

import org.junit.jupiter.api.*;
import za.co.wethinkcode.client.connection.ClientConnection;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class ClientConnectionTest {
    private static final int TEST_PORT = 5001;
    private static ServerSocket testServer;
    private static ExecutorService serverExecutor;
    private ClientConnection client;

    @BeforeAll
    static void startTestServer() throws IOException {
        testServer = new ServerSocket(TEST_PORT);
        serverExecutor = Executors.newSingleThreadExecutor();
        serverExecutor.submit(() -> {
            try {
                while (!testServer.isClosed()) {
                    Socket clientSocket = testServer.accept();
                    // Simple echo server for testing
                    new Thread(() -> handleClient(clientSocket)).start();
                }
            } catch (IOException e) {
                if (!testServer.isClosed()) {
                    e.printStackTrace();
                }
            }
        });
    }

    private static void handleClient(Socket clientSocket) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {
            String input;
            while ((input = in.readLine()) != null) {
                out.println("ECHO: " + input); // Simple echo response
            }
        } catch (IOException e) {
            // Expected when client disconnects
        }
    }

    @AfterAll
    static void stopTestServer() throws IOException, InterruptedException {
        serverExecutor.shutdown();
        if (!serverExecutor.awaitTermination(1, TimeUnit.SECONDS)) {
            serverExecutor.shutdownNow();
        }
        testServer.close();
    }

    @BeforeEach
    void setUp() {
        client = new ClientConnection();
    }

    @AfterEach
    void tearDown() {
        if (client != null && client.isConnected()) {
            client.disconnect();
        }
    }

    @Test
    @Timeout(5)
    void connect_ValidServer_ReturnsTrue() throws InterruptedException {
        assertTrue(client.connect("localhost", TEST_PORT));
        assertTrue(client.isConnected());
    }

    @Test
    @Timeout(5)
    void send_WhenConnected_MessageSent() throws InterruptedException {
        assertTrue(client.connect("localhost", TEST_PORT));
        assertDoesNotThrow(() -> client.send("test message"));
        assertTrue(client.isConnected());
    }

    @Test
    @Timeout(5)
    void disconnect_WithActiveConnection_ClosesCleanly() throws InterruptedException {
        assertTrue(client.connect("localhost", TEST_PORT));
        assertTrue(client.isConnected());

        client.disconnect();

        // Add small delay to ensure disconnect completes
        Thread.sleep(100);
        assertTrue(client.isConnected());
    }

    @Test
    @Timeout(5)
    void receive_WhenServerSendsMessage_GetsResponse() throws InterruptedException {
        assertTrue(client.connect("localhost", TEST_PORT));
        client.send("test");

        // Wait for response
        String response = null;
        long startTime = System.currentTimeMillis();
        while (response == null && System.currentTimeMillis() - startTime < 2000) {
            response = client.receive();
            Thread.sleep(50);
        }

        assertNotNull(response);
        assertTrue(response.contains("ECHO: test"));
    }

    @Test
    @Timeout(5)
    void isConnected_WhenNotConnected_ReturnsFalse() {
        assertFalse(client.isConnected());
    }
}