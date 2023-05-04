package dk.dtu.compute.se.pisd.roborally.model.obstacles;

import dk.dtu.compute.se.pisd.roborally.model.Heading;
import dk.dtu.compute.se.pisd.roborally.model.Space;
import dk.dtu.compute.se.pisd.roborally.model.obstacles.Obstacle;

public class Wall extends Obstacle {
    /**
     *
     * @param space Places the wall on the board.
     * @param color Gets the color of the Polygon.
     * @param heading Gets the way the wall is facing on the board.
     * @author Mathias
     */
    public Wall(Space space, String color, Heading heading) {

        super(space, color, heading);
    }
}
