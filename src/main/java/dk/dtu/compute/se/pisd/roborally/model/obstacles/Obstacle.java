package dk.dtu.compute.se.pisd.roborally.model.obstacles;

import dk.dtu.compute.se.pisd.roborally.model.Heading;
import dk.dtu.compute.se.pisd.roborally.model.Space;

import java.awt.*;

public abstract class Obstacle {

    private Space space;
    private String color;
    private Heading heading = Heading.SOUTH;

    public Obstacle(Space space, String color, Heading heading) {
        this.space = space;
    }

    public String getColor() {
        return this.color;
    }

    public Heading getHeading() {
        return this.heading;
    }
}
