package dk.dtu.compute.se.pisd.roborally.model.obstacles;

import dk.dtu.compute.se.pisd.roborally.model.Heading;
import dk.dtu.compute.se.pisd.roborally.model.Space;

import java.awt.*;

public abstract class Obstacle {

    private Space space;
    private Color color;
    private Heading heading;

    public Obstacle(Space space, Color color, Heading heading) {
        this.space = space;
    }
}
