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
}