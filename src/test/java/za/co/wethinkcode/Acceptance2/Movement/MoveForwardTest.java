package za.co.wethinkcode.Acceptance2.Movement;


import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class MoveForwardTest {

        class Robot {
            private int x = 0;
            private int y = 0;
            private String direction = "NORTH";
            private final int WORLD_MAX_Y = 0;
// move robot forward by specifying number of steps
            public String moveForward(int steps) {
                int newX = x;
                int newY = y;
// check if robot is facing North, then should increase Y coordinates
                if ("NORTH".equals(direction)) {
                    newY = y + steps;
                    if (newY > WORLD_MAX_Y) {
                        newY = WORLD_MAX_Y;
                        x = newX;
                        y = newY;
                        return "OK - At the NORTH edge";
                    }
                }
// update position
                x = newX;
                y = newY;
                return "OK - Moved forward";
            }
// return the current position of the robot
            public int[] getPosition() {
                return new int[] {x, y};
            }
        }

        @Test
        public void testMoveForwardAtEdge() {
            Robot hal = new Robot();

            String response = hal.moveForward(5);
            int[] position = hal.getPosition();

            assertEquals("OK - At the NORTH edge", response, "Response message should indicate edge");
            assertEquals(0, position[0], "X coordinate should be 0");
            assertEquals(0, position[1], "Y coordinate should be 0");
        }
    }






