package za.co.wethinkcode.server.data;

import net.lemnik.eodsql.ResultColumn;

/**
 * Data Object (DO) representing an obstacle in a world.
 *
 * This class is used with EODSQL to map database columns to Java fields.
 * Each instance corresponds to a row in the obstacles table of the database.
 */
public class ObstacleDO {
    /** X-coordinate of the obstacle in the world grid. */
    public  int x;
    /** Y-coordinate of the obstacle in the world grid. */
    public  int y;
    /** Height of the obstacle. */
    public  int height;
    /** Width of the obstacle. */
    public  int width;

    /**
     * Foreign key referencing the ID of the world this obstacle belongs to.
     * Mapped from the database column 'world_id'.
     */
    @ResultColumn(value = "world_id")
    public  int worldid;

    /** Default constructor required by EODSQL. */
    public ObstacleDO(){}
}
