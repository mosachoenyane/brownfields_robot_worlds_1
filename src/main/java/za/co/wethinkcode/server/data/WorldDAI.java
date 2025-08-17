package za.co.wethinkcode.server.data;

import net.lemnik.eodsql.BaseQuery;
import net.lemnik.eodsql.Select;
import net.lemnik.eodsql.Update;
import java.util.List;

/**
 * Data Access Interface (DAI) for interacting with the World and Obstacle tables.
 *
 * Uses EODSQL annotations to define SQL queries for saving and retrieving world data.
 */
public interface WorldDAI extends BaseQuery {

 // Saves a new world record and returns the number of rows updated.
 @Update("INSERT INTO world (name, height, width) VALUES (?{1.name}, ?{1.height}, ?{1.width})")
 int saveWorld(WorldDO world);

 // Saves a new obstacle record.the use of ?{1.x} to access the DO's field.
 @Update("INSERT INTO obstacles (x, y, width, height, world_id) VALUES (?{1.x}, ?{1.y}, ?{1.width}, ?{1.height}, ?{2})")
 void saveObstacle(ObstacleDO obstacle, int worldId);

 // Retrieves a world by its name.
 @Select("SELECT id, name, height, width FROM world WHERE name = ?{1}")
 WorldDO findWorldByName(String name);

 // Retrieves all obstacles for a given world ID.
 @Select("SELECT id, x, y, width, height, world_id FROM obstacles WHERE world_id = ?{1}")
 List<ObstacleDO> findObstaclesByWorldId(int worldId);
}
