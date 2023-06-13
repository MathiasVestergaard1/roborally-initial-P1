package dk.dtu.compute.se.pisd.roborally.controller;

import dk.dtu.compute.se.pisd.roborally.RoboRally;
import dk.dtu.compute.se.pisd.roborally.model.Board;
import dk.dtu.compute.se.pisd.roborally.model.Heading;
import dk.dtu.compute.se.pisd.roborally.model.Player;
import dk.dtu.compute.se.pisd.roborally.model.Space;
import dk.dtu.compute.se.pisd.roborally.model.obstacles.Checkpoint;
import dk.dtu.compute.se.pisd.roborally.model.obstacles.Conveyor;
import dk.dtu.compute.se.pisd.roborally.model.obstacles.Gear;
import dk.dtu.compute.se.pisd.roborally.model.obstacles.Obstacle;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

class GameControllerTest {

    private final int TEST_WIDTH = 10;
    private final int TEST_HEIGHT = 10;
    private GameController gameController;
    private RoboRally roboRally = new RoboRally();

    @BeforeEach
    void setUp() {
        Board board = new Board(TEST_WIDTH, TEST_HEIGHT);
        AppController appController = new AppController(this.roboRally);
        gameController = new GameController(board,appController);
        for (int i = 0; i < 6; i++) {
            Player player = new Player(board, null,"Player " + i);
            board.addPlayer(player);
            player.setSpace(board.getSpace(i, i));
            player.setHeading(Heading.values()[i % Heading.values().length]);
        }
        board.setCurrentPlayer(board.getPlayer(0));
    }

    @AfterEach
    void tearDown() {
        gameController = null;
    }

    @Test
    void moveCurrentPlayerToSpace() {
        Board board = gameController.board;
        Player player1 = board.getPlayer(0);
        Player player2 = board.getPlayer(1);

        gameController.moveCurrentPlayerToSpace(board.getSpace(0, 4));

        Assertions.assertEquals(player1, board.getSpace(0, 4).getPlayer(), "Player " + player1.getName() + " should beSpace (0,4)!");
        Assertions.assertNull(board.getSpace(0, 0).getPlayer(), "Space (0,0) should be empty!");
        Assertions.assertEquals(player2, board.getCurrentPlayer(), "Current player should be " + player2.getName() +"!");
    }

    @Test
    void moveForward() {
        Board board = gameController.board;
        Player current = board.getCurrentPlayer();

        gameController.moveForward(current);

        Assertions.assertEquals(current, board.getSpace(0, 1).getPlayer(), "Player " + current.getName() + " should beSpace (0,1)!");
        Assertions.assertEquals(Heading.SOUTH, current.getHeading(), "Player 0 should be heading SOUTH!");
        Assertions.assertNull(board.getSpace(0, 0).getPlayer(), "Space (0,0) should be empty!");
    }

    @Test
    void movePlayerOnConveyor() {
        Board board = gameController.board;
        Player current = board.getCurrentPlayer();
        Space space = board.getSpace(0,1);
        Conveyor conveyor = new Conveyor(space,Heading.SOUTH);
        space.setObstacle(conveyor);
        current.setSpace(space);
        gameController.movePlayerOnConveyor(current);


        Assertions.assertEquals(board.getSpace(0,2), current.getSpace());
        Assertions.assertEquals(Heading.SOUTH, current.getHeading());
        Assertions.assertNull(board.getSpace(0, 0).getPlayer());

    }

    @Test
    void movePlayerOnGear() {
        Board board = gameController.board;
        Player current = board.getCurrentPlayer();
        Space space = board.getSpace(0,1);
        Gear gear = new Gear(space,Heading.SOUTH);
        space.setObstacle(gear);
        current.setSpace(space);
        gameController.MovePlayerOnGear(current);

        Assertions.assertEquals(Heading.WEST,current.getHeading());
    }

    @Test
    void playerCheckpoint() {
        Board board = gameController.board;
        Player current = board.getCurrentPlayer();
        Space space = board.getSpace(1,1);
        Checkpoint checkpoint = new Checkpoint(space,Heading.SOUTH);
        space.setObstacle(checkpoint);
        current.setSpace(space);
        gameController.setCheckpointPosition(space);
        gameController.PlayerCheckpoint(current);

        Assertions.assertEquals(current.getCheckpointCounter(),1);
    }

    @Test
    void turnRight() {
        Board board = gameController.board;
        Player current = board.getCurrentPlayer();
        gameController.turnRight(current);

        Assertions.assertEquals(Heading.WEST,current.getHeading());
    }
}