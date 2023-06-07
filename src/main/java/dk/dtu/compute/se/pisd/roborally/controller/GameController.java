/*
 *  This file is part of the initial project provided for the
 *  course "Project in Software Development (02362)" held at
 *  DTU Compute at the Technical University of Denmark.
 *
 *  Copyright (C) 2019, 2020: Ekkart Kindler, ekki@dtu.dk
 *
 *  This software is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; version 2 of the License.
 *
 *  This project is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this project; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */
package dk.dtu.compute.se.pisd.roborally.controller;

import dk.dtu.compute.se.pisd.roborally.model.*;
import org.jetbrains.annotations.NotNull;

/**
 * ...
 *
 * @author Ekkart Kindler, ekki@dtu.dk
 *
 */
public class GameController {

    final public Board board;

    public GameController(@NotNull Board board) {
        this.board = board;
    }

    /**
     * This is just some dummy controller operation to make a simple move to see something
     * happening on the board. This method should eventually be deleted!
     *
     * @param space the space to which the current player should move
     */
    public void moveCurrentPlayerToSpace(@NotNull Space space)  {
        // TODO Assignment V1: method should be implemented by the students:
        //   - the current player should be moved to the given space
        //     (if it is free()
        //   - and the current player should be set to the player
        //     following the current player
        //   - the counter of moves in the game should be increased by one
        //     if the player is moved
        Player Test = space.getPlayer();
        Player currentplayer = board.getCurrentPlayer();
        if (Test == null){
            space.setPlayer(currentplayer);
            currentplayer.setSpace(space);
            int Step = board.getStep();
            board.setStep(Step+1);
        }
        else {
            this.moveCurrentPlayerToSpace(space);
            return;
        }
        int Playercount = board.getPlayersNumber();
        int Currentplayernumber = board.getPlayerNumber(currentplayer);
        int NextPlayerNumber = 0;
        if (Currentplayernumber < Playercount-1){
            NextPlayerNumber = Currentplayernumber + 1;
        }
        Player nextPlayer = board.getPlayer(NextPlayerNumber);
        board.setCurrentPlayer(nextPlayer);

    }

    /**
     * A method called when no corresponding controller operation is implemented yet.
     * This method should eventually be removed.
     */
    public void notImplememted() {
        // XXX just for now to indicate that the actual method to be used by a handler
        //     is not yet implemented
    };

            if(space.getObstacle() == null || !(space.getObstacle() instanceof Conveyor)) {
                return;
            }

            Heading heading = space.getObstacle().getHeading();

            ArrayList<Space> spacelist = new ArrayList<Space>();
            spacelist.add(space);

            while (true) {
                Space nexttarget = board.getNeighbour(space, heading);
                if (nexttarget != null && space.getPlayer() != null) {
                    spacelist.add(nexttarget);
                    space = nexttarget;
                } else {
                    break;
                }
            }
            for (int i = spacelist.size() - 1; i >= 0; i--) {
                Space somespace = spacelist.get(i);
                Player someplayer = somespace.getPlayer();
                Space neighbortarget = board.getNeighbour(somespace, heading);
                if (neighbortarget != null) {
                    neighbortarget.setPlayer(someplayer);
                }
            }
        }
    }

    public void MovePlayerOnGear(Player player) {
        Space space = player.getSpace();
        if (player != null && player.board == board && space != null) {
            if(space.getObstacle() == null || !(space.getObstacle() instanceof Gear)) {
                return;
            }
            Heading heading = player.getHeading();
            heading = heading.next();
            player.setHeading(heading);
        }
    }


    public void spawnNewCheckpoint(Space space) {
        int randomNumber = (int) (Math.random() * checkpointPositions.size());
        if (checkpointPositions.get(randomNumber) == space) {
            if (randomNumber == 0) {
                randomNumber++;
            } else {
                randomNumber--;
            }
        }

        Space newSpace = checkpointPositions.get(randomNumber);
        space.setObstacle(null);

        Obstacle obstacle = new Checkpoint(space, "Green", Heading.SOUTH);
        newSpace.setObstacle(obstacle);

        appController.update(space);
    }

    public void PlayerCheckpoint(Player player) {
        Space space = player.getSpace();
        if (player != null && player.board == board && space != null){
            if (space.getObstacle() == null || !(space.getObstacle() instanceof Checkpoint)){
                return;
            }
            player.IncreaseCheckpoint();

            if (player.getCheckpointCounter() == 4) {
                endGame();
            } else {
                spawnNewCheckpoint(space);
            }
        }
    }
/**

    /**
     * @param player checks if there is a wall in front of the player.
     *               If there is a wall it checks which way the header is facing, if it's the same way as the player,
     *               the player can't continue. And if the wall is on the opposite side it also can't move through.
     * @author Mathias
     */
    public boolean wallCheck(@NotNull Player player){
        Space space = player.getSpace();
        Obstacle wall = space.getObstacle();
        Heading playerHeading = player.getHeading();
        if(wall != null && wall instanceof Wall){
            Heading wallHeading = wall.getHeading();
            if(wallHeading == playerHeading){
                return true;
            }
        }

        Space nextSpace = board.getNeighbour(space, playerHeading);
        Obstacle nextWall = nextSpace.getObstacle();
        if(nextWall == null || !(nextWall instanceof Wall)){
            return false;
        }
        Heading nextWallHeading = nextWall.getHeading().next().next();
        if(nextWallHeading == playerHeading){
            return true;
        }
        return false;
    }
    /**
     * This method moves the player forward two spaces, by calling the moveForward method twice.
     *
     * @param player the player that should be moved.
     * @author Mads Hauberg
     */
    public void fastForward(@NotNull Player player) {
        moveForward(player);
        moveForward(player);
    }

    /**
     * This method turns the player 90 degrees to the right.
     *
     * @param player the player that should be turned.
     * @author Mads Hauberg
     */
    public void turnRight(@NotNull Player player) {
        if (player != null && player.board == board) {
            player.setHeading(player.getHeading().next());
        }
    }

    /**
     * This method turns the player 90 degrees to the left.
     *
     * @param player the player that should be turned
     * @author Mads Hauberg
     */
    public void turnLeft(@NotNull Player player) {
        if (player != null && player.board == board) {
            player.setHeading(player.getHeading().prev());
        }
    }

    public void uTurn(@NotNull Player player) {
        if (player != null && player.board == board) {
            player.setHeading(player.getHeading().prev());
            player.setHeading(player.getHeading().prev());
        }
    }

    public void optionCard() {
        board.setPhase(Phase.PLAYER_INTERACTION);
    }
    /**
    * This method called "moveCards" takes two parameter "source" and "target", both of type "CommandCardField".
    * The Method moves a "CommandCard" object from the source to the "target" field.
    * it first gets the CommandCard object from the source field and assign it to the sourceCard variable.
    * it then gets the "commandCard" objet from the "target" field and assign it to the "targetCard" variable.
    * if the "sourceCard" is not null and "targetCard" is null then the method proceeds to give the "sourceCard" object to the "target" field.
    * if either "sourceCard" is null or "targetCard" is not null then the methods returns false to indicate that the move was unsuccessful.
     *
     * @param source the cardField the card came from
     * @param target the cardField the card is going to
     *
     * @return boolean
    */
    public boolean moveCards(@NotNull CommandCardField source, @NotNull CommandCardField target) {
        CommandCard sourceCard = source.getCard();
        CommandCard targetCard = target.getCard();
        if (sourceCard != null & targetCard == null) {
            target.setCard(sourceCard);
            source.setCard(null);
            return true;
        } else {
            return false;
        }
    }

}
