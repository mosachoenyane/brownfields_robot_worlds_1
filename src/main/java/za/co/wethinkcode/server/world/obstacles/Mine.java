package za.co.wethinkcode.server.world.obstacles;

import za.co.wethinkcode.server.model.Position;

public class Mine extends Obstacle {

    public Mine(int x, int y) {
        super(x, y, 1, 1);
    }
    public Mine(Position position){
        super(position.getX(), position.getY(), 1, 1);
    }

    @Override
    public boolean blocksVisibility() {
        return true;
    }

    @Override
    public String getType() {
        return "mine";
    }
}