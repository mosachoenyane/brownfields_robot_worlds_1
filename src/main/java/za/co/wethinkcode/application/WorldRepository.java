// Java
package za.co.wethinkcode.application;

import java.util.List;
import java.util.Optional;

public interface WorldRepository {
    // Summary used for listing all worlds
    record WorldSummary(String name, int width, int height) {}

    // Obstacle row for a world from the DB
    record ObstacleRow(int x, int y, int width, int height) {}

    // Full details of a world including obstacles
    record WorldDetails(String name, int width, int height, List<ObstacleRow> obstacles) {}

    List<WorldSummary> findAll();

    // Find a world by name including its obstacles (if found)
    Optional<WorldDetails> findByName(String name);

    // Find all worlds with their obstacles
    List<WorldDetails> findAllDetails();
}
