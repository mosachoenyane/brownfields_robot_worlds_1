package za.co.wethinkcode.server.commands;

/**
 * Represents a command executable by a robot in the server.
 * Implementations define specific robot actions.
 */
public interface Command {

    /**
     * Executes this command.
     *
     * @return the execution result as a string response
     */
    String execute();

    /**
     * Retrieves the command's name.
     *
     * @return the command identifier (e.g., "forward", "left")
     */
    String getName();

    /**
     * Provides a display-friendly representation of the command.
     *
     * @return a string suitable for user display
     */
    String display();
}
