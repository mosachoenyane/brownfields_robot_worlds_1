package za.co.wethinkcode.server;

import za.co.wethinkcode.protocol.server.CommandFactory;
import za.co.wethinkcode.server.world.World;

import java.util.Scanner;

/**
 * ServerConsole provides an interactive command-line interface for server operators.
 * It allows inspection of the world state, robot management, and graceful shutdown.
 */
public class ServerConsole {
    private final Scanner scanner;
    private final CommandFactory commandFactory;
    private boolean isRunning;

    // ANSI formatting
    private static final String RESET = "\u001B[0m";
    private static final String CYAN = "\u001B[36m";
    private static final String WHITE = "\u001B[37m";
    private static final String YELLOW = "\u001B[33m";
    private static final String RED = "\u001B[31m";
    private static final String GREEN = "\u001B[32m";
    private static final String BOLD = "\u001B[1m";

    /**
     * Constructs a new ServerConsole instance.
     *
     * @param scanner the Scanner used for reading input from the command line
     * @param world   the World object representing the server's game world
     */
    public ServerConsole(Scanner scanner, World world) {
        this.scanner = scanner;
        this.commandFactory = new CommandFactory(world);
        this.isRunning = true;
    }

    /**
     * Starts the console interface, allowing users to enter commands such as
     * 'help', 'robots', 'dump', and 'quit'.
     */
    public void start() {
        System.out.println(BOLD + "\n═══════════════════════════════════════════════");
        System.out.println("   🛠️  Robot World Server Console Started");
        System.out.println("═══════════════════════════════════════════════" + RESET);
        System.out.println(GREEN + " ✅ Server is up and running." + RESET);
        System.out.println(WHITE + " Type 'help' to see available commands." + RESET);

        while (isRunning) {
            System.out.print("\n" + BOLD + "Server> " + RESET);
            String input = scanner.nextLine().trim();
            processCommand(input);
        }
    }

    private void processCommand(String command) {
        String argument = null;
        if (command.contains(" ")) {
            String[] commands = command.split(" ");
            command = commands[0];
            argument = commands.length > 1 ? commands[1] : null;
        }
        switch (command.toLowerCase()) {
            case "quit":
                handleQuitCommand();
                break;
            case "help":
                showHelp();
                break;
            case "dump":
                System.out.println(commandFactory.createDumpCommand().display());
                break;
            case "robots":
                System.out.println(commandFactory.createRobotsCommand().display());
            case "save":
                if (argument == null) {
                    System.out.println(commandFactory.createSaveCommand().display());
                } else {
                    String worldName = (argument != null) ? argument : "default";
                }
                break;
            case "restore":
                if (argument == null) {
                    System.out.println("Restored the current world");
                } else {
                    System.out.println(commandFactory.createRestoreWorldCommand(argument).execute());
                }
                break;
            default:
                System.out.println(RED + " ❌ Unknown command. Type 'help' for available commands." + RESET);
        }
    }

    private void handleQuitCommand() {
        System.out.println(YELLOW + "\n👋 Disconnecting all robots and shutting down..." + RESET);
        commandFactory.createQuitCommand().execute();
        isRunning = false;
    }

    private void showHelp() {
        System.out.println(BOLD + "\n📚 Server Commands:" + RESET);
        System.out.println(WHITE + "────────────────────────────────" + RESET);
        System.out.println(" dump    - Show current world state");
        System.out.println(" robots  - List all active robots");
        System.out.println(" quit    - Disconnect all and shut down");
        System.out.println(" help    - Show this help message");
    }
}
