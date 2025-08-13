// Java
package za.co.wethinkcode.application;

import java.util.List;

public interface WorldRepository {
    record WorldSummary(String name, int width, int height) {}

    List<WorldSummary> findAll();
}
