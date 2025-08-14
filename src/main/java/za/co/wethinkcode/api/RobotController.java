package za.co.wethinkcode.api;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/robot")
public class RobotController {

    private final RobotService robotService;

    public RobotController(RobotService robotService) {
        this.robotService = robotService;
    }

    @PostMapping(
            value = "/{name}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<String> postRobotCommand(
            @PathVariable String name,
            @RequestBody String requestJson) {

        // Pass the path name and request JSON to the service
        String responseJson = robotService.processRobotCommand(name, requestJson);

        // Return JSON back to the client
        return ResponseEntity.ok(responseJson);
    }
}
