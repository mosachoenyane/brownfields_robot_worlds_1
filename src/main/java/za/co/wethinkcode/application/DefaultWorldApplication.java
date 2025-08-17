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
    @Override
    public JsonObject getWorldByName(String name) {
        // If the requested name is the current world, return the full dump
        if (name != null && name.equalsIgnoreCase(world.getName())) {
            return getCurrentWorld();
        }

        // Otherwise, look it up from saved worlds (summary info)
        for (WorldRepository.WorldSummary ws : worldRepo.findAll()) {
            if (ws.name().equalsIgnoreCase(name)) {
                JsonObject jo = new JsonObject();
                jo.addProperty("name", ws.name());
                jo.addProperty("width", ws.width());
                jo.addProperty("height", ws.height());
                return jo;
            }
        }

        // Not found
        JsonObject err = new JsonObject();
        err.addProperty("error", "World not found");
        err.addProperty("name", name);
        return err;
    }
}
