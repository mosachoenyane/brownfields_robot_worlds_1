package za.co.wethinkcode.server.data;

import net.lemnik.eodsql.ResultColumn;

public class ObstacleDO {
    public  int id;
    public  int x;
    public  int y;
    public  int height;
    public  int width;

    @ResultColumn(value = "world_id")
    public  int worldid;

    public ObstacleDO(){}
}
