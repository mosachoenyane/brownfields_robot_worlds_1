package za.co.wethinkcode.api;

import com.google.gson.JsonObject;

/**
 * Application service boundary for world-related use cases.
 * The Web API calls this interface; implementations live outside the api package.
 */
public interface WorldApplication {
    /**
     * Returns a JSON-ready snapshot of the current world suitable for API responses.
     */
    JsonObject getCurrentWorld();
}
