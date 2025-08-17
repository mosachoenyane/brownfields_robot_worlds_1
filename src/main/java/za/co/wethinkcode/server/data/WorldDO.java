package za.co.wethinkcode.server.data;

/**
 * Data Object (DO) representing a World record in the database.
 *
 * Used by the {@link za.co.wethinkcode.server.data.WorldDAI} interface to map database rows to Java objects.
 */
public class WorldDO {

    /** Unique identifier for the world. */
    public int id;
    /** Name of the world. */
    public String name;
    /** Height of the world grid. */
    public int height;
    /** Width of the world grid. */
    public int width;

    /**
     * Default constructor required for database mapping.
     */
    public  WorldDO(){}
}
