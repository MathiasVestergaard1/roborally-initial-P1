package dk.dtu.compute.se.pisd.roborally.model.obstacles;

import dk.dtu.compute.se.pisd.roborally.model.Heading;
import dk.dtu.compute.se.pisd.roborally.model.Space;

public class Wall extends Obstacle {
    /**
     * @param space   Places the wall on the board.
     * @param heading Gets the way the wall is facing on the board.
     * @author Mathias
     */
    public Wall(Space space, Heading heading) {

        super(space, heading);
    }
}
