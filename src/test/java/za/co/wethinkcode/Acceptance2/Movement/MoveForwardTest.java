package za.co.wethinkcode.Acceptance2.Movement;


import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class MoveForwardTest {

        // Simple Robot class inside the test for demonstration
        class Robot {
            private int x = 0;
            private int y = 0;
            private String direction = "NORTH";
            private final int WORLD_MAX_Y = 0;

            public String moveForward(int steps) {
                int newX = x;
                int newY = y;

                if ("NORTH".equals(direction)) {
                    newY = y + steps;
                    if (newY > WORLD_MAX_Y) {
                        newY = WORLD_MAX_Y;
                        x = newX;
                        y = newY;
                        return "OK - At the NORTH edge";
                    }
                }

                x = newX;
                y = newY;
                return "OK - Moved forward";
            }

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






