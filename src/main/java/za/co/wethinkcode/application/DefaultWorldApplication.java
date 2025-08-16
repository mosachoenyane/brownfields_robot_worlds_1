// Java
package za.co.wethinkcode.application;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import za.co.wethinkcode.api.WorldApplication;
import za.co.wethinkcode.server.world.World;
import za.co.wethinkcode.server.world.WorldDumper;

import java.util.Optional;

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
     * - Otherwise fetches from DB, including obstacles.
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

        Optional<WorldRepository.WorldDetails> maybe = worldRepo.findByName(name);
        if (maybe.isPresent()) {
            WorldRepository.WorldDetails details = maybe.get();
            JsonObject data = new JsonObject();
            data.addProperty("name", details.name());
            data.addProperty("width", details.width());
            data.addProperty("height", details.height());

            JsonArray obstacles = new JsonArray();
            for (WorldRepository.ObstacleRow ob : details.obstacles()) {
                JsonObject o = new JsonObject();
                o.addProperty("x", ob.x());
                o.addProperty("y", ob.y());
                o.addProperty("width", ob.width());
                o.addProperty("height", ob.height());
                obstacles.add(o);
            }
            data.add("obstacles", obstacles);
            data.addProperty("source", "db");
            return data;
        }

        JsonObject notFound = new JsonObject();
        notFound.addProperty("error", "World not found");
        notFound.addProperty("name", name);
        return notFound;
    }
}