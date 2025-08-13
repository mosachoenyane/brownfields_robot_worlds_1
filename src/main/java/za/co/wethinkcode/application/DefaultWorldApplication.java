package za.co.wethinkcode.application;

import com.google.gson.JsonObject;
import za.co.wethinkcode.api.WorldApplication;
import za.co.wethinkcode.server.world.World;
import za.co.wethinkcode.server.world.WorldDumper;

public class DefaultWorldApplication implements WorldApplication {
    private final World world;

    public DefaultWorldApplication(World world) {
        this.world = world;
    }

    @Override
    public JsonObject getCurrentWorld() {

        synchronized (world) {
            JsonObject data = new WorldDumper(world).createWorldDump();
            data.addProperty("name", world.getName());

            return data;
        }
    }
}