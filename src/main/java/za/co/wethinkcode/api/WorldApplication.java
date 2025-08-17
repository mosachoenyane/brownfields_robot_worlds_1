// Java
package za.co.wethinkcode.api;

import com.google.gson.JsonObject;

/**
 * Abstraction for interacting with world data in the Robot World application.
 * <p>
 * This interface is designed to be implemented by components that manage
 * and expose world-related information, such as the current active world
 * and saved worlds stored in a database.
 */

public interface WorldApplication {

    /**
     * Retrieves data about the currently active world.
     *
     * @return a @link JsonObject containing the current world representation
     */
    JsonObject getCurrentWorld();

    // Returns data on all the worlds saved on the database.
    JsonObject listSavedWorlds();

    // Returns data for a specific world by name (from current world or DB).
    JsonObject getWorldByName(String name);
}