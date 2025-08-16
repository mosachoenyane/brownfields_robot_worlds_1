// Java
package za.co.wethinkcode.api;

import com.google.gson.JsonObject;

public interface WorldApplication {
    JsonObject getCurrentWorld();

    // Returns data on all the worlds saved on the database.
    JsonObject listSavedWorlds();

    // Returns data for a specific world by name (from current world or DB).
    JsonObject getWorldByName(String name);
}