package za.co.wethinkcode.server;

//import za.co.wethinkcode.flow.Recorder;
import za.co.wethinkcode.server.handler.ClientHandler;
import za.co.wethinkcode.server.world.World;
import za.co.wethinkcode.server.world.WorldConfig;
import za.co.wethinkcode.application.DefaultWorldApplication;
import za.co.wethinkcode.api.WorldApplication;


import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import za.co.wethinkcode.server.world.obstacles.Mountain;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

/**
 * The RobotWorldServer class initializes the world with configuration settings
 * and starts a server that listens for client connections.
 * It also optionally starts a console interface for server administration.
 */
@Command(name = "Robot-Server", mixinStandardHelpOptions = true, version = "1.0",
        description = "Starts the Robot World Server with specified parameters")
public class RobotWorldServer {

    private static int PORT = 5000;

    @Option(
            names = {"-p", "--port"},
            description = "Port number")
    private static String newPORT;

    @Option(
            names = {"-s", "--size"},
            description = "World size")
    private static String SIZE;

    @Option(
            names = {"-o", "--obstacle"}, split = ",",
            description = "Obstacle coordinates")
    private static String[] OBSTACLE;
    private static ServerSocket serverSocket;


    /**
     * Entry point of the server application.
     * Loads world configuration, initializes the world,
     * and starts both the network server and optionally the console.
     *
     * @param args Command-line arguments. If "nogui" is passed, the server console is not started.
     */
    public static void main(String[] args) {
        RobotWorldServer app = new RobotWorldServer();
        new CommandLine(app).parseArgs(args);
        try {
            WorldConfig config = new WorldConfig("config.properties");
            if(newPORT != null ){
                System.out.println("Overriding default Port number with port number: " + PORT);
                PORT = Integer.parseInt(newPORT);
            }
            if(SIZE != null ){
                System.out.println("Overriding default World Size with size: " + SIZE);
                config.properties.setProperty("WORLD_WIDTH",SIZE);
                config.properties.setProperty("WORLD_HEIGHT",SIZE);
            }

            World world = new World(config);//

            if(OBSTACLE != null ){
                System.out.println("Placing obstacle at chosen coordinates:" + OBSTACLE[0] + "," + OBSTACLE[1]);

                world.addObstacle(new Mountain(Integer.parseInt(OBSTACLE[0]), Integer.parseInt(OBSTACLE[1]),1,1));

            }

            // Start WEB API (on port 7000) TO VERIFY ITS RUNNING
            try {
                String jdbcUrl = "jdbc:sqlite:robot_world.db"; // consider externalizing to config
                var cfg = za.co.wethinkcode.api.ApiConfig.localDefault();
                var repo = new za.co.wethinkcode.application.SQLiteWorldRepository(jdbcUrl);
                var worldApp = new za.co.wethinkcode.application.DefaultWorldApplication(world, repo);
                var api = new za.co.wethinkcode.api.WebApiServer(cfg, worldApp);
                api.start();
                System.out.println("\nWeb API running on " + cfg.baseUrl());
            } catch (Exception apiEx) {
                System.err.println("Failed to start Web API: " + apiEx.getMessage());
            }

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

    public static void startServer(World world) throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(PORT,50, InetAddress.getByName("0.0.0.0"))) {
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
    public static void close() {
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
               serverSocket.close();
                System.out.println("Server socket closed.");
            }
        } catch (IOException e) {
            System.err.println("Error closing server socket: " + e.getMessage());
            e.printStackTrace();
        }
    }

}