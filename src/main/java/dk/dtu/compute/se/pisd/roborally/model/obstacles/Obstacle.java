package dk.dtu.compute.se.pisd.roborally.model.obstacles;

import dk.dtu.compute.se.pisd.roborally.model.Heading;
import dk.dtu.compute.se.pisd.roborally.model.Space;

public abstract class Obstacle {

    private Space space;
    private String color;
    private Heading heading;

    public Obstacle(Space space, Heading heading) {
        this.space = space;
        this.color = color;
        this.heading = heading;
    }

    public String getColor() {
        return this.color;
    }

    public Heading getHeading() {
        return this.heading;
    }
}
