package dk.dtu.compute.se.pisd.roborally.model.obstacles;
import dk.dtu.compute.se.pisd.roborally.model.Heading;
import dk.dtu.compute.se.pisd.roborally.model.Space;

public class Conveyor extends Obstacle {


    /**
     * This method is our "conveyor", where the method that takes the special movement is located in our gamecontroller
     *
     * @param space the space of the conveyor
     * @param heading the heading of the conveyor
     * @author Mads Halberg
     */
    public Conveyor(Space space, Heading heading) {
        super(space, heading);
    }
}



