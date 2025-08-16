// Java
package za.co.wethinkcode.application;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import za.co.wethinkcode.api.WorldApplication;
import za.co.wethinkcode.server.world.World;
import za.co.wethinkcode.server.world.WorldDumper;

public class DefaultWorldApplication implements WorldApplication {
    private final World world;
    private final WorldRepository worldRepo;

    public DefaultWorldApplication(World world, WorldRepository worldRepo) {
        this.world = world;
        this.worldRepo = worldRepo;
    }

    @Override
    public JsonObject getCurrentWorld() {
        synchronized (world) {
            JsonObject data = new WorldDumper(world).createWorldDump();
            data.addProperty("name", world.getName());
            return data;
        }
    }

    @Override
    public JsonObject listSavedWorlds() {
        JsonArray arr = new JsonArray();
        for (WorldRepository.WorldSummary ws : worldRepo.findAll()) {
            JsonObject jo = new JsonObject();
            jo.addProperty("name", ws.name());
            jo.addProperty("width", ws.width());
            jo.addProperty("height", ws.height());
            arr.add(jo);
        }
        JsonObject data = new JsonObject();
        data.add("worlds", arr);
        return data;
    }

    /**
     * Returns JSON data for a world by name.
     * - If name matches the current in-memory world, returns the full dump (including obstacles and robots).
     * - Otherwise searches the repository summaries and returns width/height from the DB with an empty obstacles array.
     *   (If obstacle lookup is available in the repository, it can be easily added here.)
     */
    public JsonObject getWorldByName(String name) {
        if (name != null && name.equalsIgnoreCase(world.getName())) {
            synchronized (world) {
                JsonObject data = new WorldDumper(world).createWorldDump();
                data.addProperty("name", world.getName());
                data.addProperty("source", "current");
                return data;
            }
        }

        for (WorldRepository.WorldSummary ws : worldRepo.findAll()) {
            if (ws.name().equalsIgnoreCase(name)) {
                JsonObject data = new JsonObject();
                data.addProperty("name", ws.name());
                data.addProperty("width", ws.width());
                data.addProperty("height", ws.height());
                // Placeholder: obstacles for saved worlds can be populated if repository supports it.
                data.add("obstacles", new JsonArray());
                data.addProperty("source", "db");
                return data;
            }
        }

        JsonObject notFound = new JsonObject();
        notFound.addProperty("error", "World not found");
        notFound.addProperty("name", name);
        return notFound;
    }
}