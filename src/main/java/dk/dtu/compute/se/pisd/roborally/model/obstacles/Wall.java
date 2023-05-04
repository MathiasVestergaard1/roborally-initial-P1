package dk.dtu.compute.se.pisd.roborally.model.obstacles;

import dk.dtu.compute.se.pisd.roborally.model.Heading;
import dk.dtu.compute.se.pisd.roborally.model.Space;
import dk.dtu.compute.se.pisd.roborally.model.obstacles.Obstacle;

public class Wall extends Obstacle {
    public Wall(Space space, String color, Heading heading) {

        super(space, color, heading);
    }
}
