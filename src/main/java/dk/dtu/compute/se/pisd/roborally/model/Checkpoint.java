package dk.dtu.compute.se.pisd.roborally.model;

/**
 * This class define "checkpoint" whisch has three instance variables "board", "x" and "y".
 * "Board" is an instance of the "board" class and it is marked as "final,
 * "x" and "y" are two integers that represent the coordinates of a point on the board.
 * The class has a constructor that takes three parameters, "board", "x" and "y",
 * and initializes the corresponding instance with the given values.
 * @author Ali Can Erten
 */
public class Checkpoint {

    public final Board board;

    public final int x;
    public final int y;

    public Checkpoint(Board board, int x, int y){
        this.board = board;
        this.x = x;
        this.y = y;
    }

}
