package za.co.wethinkcode.server.world.obstacles;

import za.co.wethinkcode.server.model.Position;

public class Mine extends Obstacle {
    public Mine(Position position) {
        super(position,1, 1);
    }

    @Override
    public String getType() {
        return "mine";
    }
}

