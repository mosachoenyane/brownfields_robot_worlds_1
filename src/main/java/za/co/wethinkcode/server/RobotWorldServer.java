package za.co.wethinkcode.server;

import za.co.wethinkcode.server.handler.ClientHandler;
import za.co.wethinkcode.server.world.World;
import za.co.wethinkcode.server.world.WorldConfig;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

/**
 * The RobotWorldServer class initializes the world with configuration settings
 * and starts a server that listens for client connections.
 * It also optionally starts a console interface for server administration.
 */
public class RobotWorldServer {
    private static final int PORT = 5000;

    /**
     * Entry point of the server application.
     * Loads world configuration, initializes the world,
     * and starts both the network server and optionally the console.
     *
     * @param args Command-line arguments. If "nogui" is passed, the server console is not started.
     */
    public static void main(String[] args) {
        try {
            WorldConfig config = new WorldConfig("config.properties");

            World world = new World(config);//


            // Start server thread
            Thread serverThread = new Thread(() -> {
                try {
                    startServer(world);
                } catch (IOException e) {
                    System.err.println("Server error: " + e.getMessage());
                }
            });
            serverThread.start();

            // Start console if not running in "both" modes
            if (args.length == 0 || !args[0].equals("nogui")) {
                new ServerConsole(new Scanner(System.in), world).start();
            }
        } catch (Exception e) {
            System.err.println("Server error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void startServer(World world) throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Robot World Server running on port " + PORT);

            while (true) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("New client connected: " + clientSocket.getInetAddress());
                    new ClientHandler(clientSocket, world).start();
                } catch (IOException e) {
                    System.err.println("Error accepting client connection: " + e.getMessage());
                }
            }
        }
    }
}