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

import dk.dtu.compute.se.pisd.roborally.RoboRally;
import dk.dtu.compute.se.pisd.roborally.model.*;
import dk.dtu.compute.se.pisd.roborally.model.obstacles.Checkpoint;
import dk.dtu.compute.se.pisd.roborally.model.obstacles.Conveyor;
import dk.dtu.compute.se.pisd.roborally.model.obstacles.Obstacle;
import dk.dtu.compute.se.pisd.roborally.model.obstacles.Wall;
import dk.dtu.compute.se.pisd.roborally.model.obstacles.Gear;
import org.jetbrains.annotations.NotNull;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Objects;

/**
 * ...
 *
 * @author Ekkart Kindler, ekki@dtu.dk
 *
 */
public class GameController {

    final public Board board;
    final private AppController appController;
    private ArrayList<Space> checkpointPositions = new ArrayList<>();

    public GameController(@NotNull Board board, AppController appController) {
        this.board = board;
        this.appController = appController;
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

        if (space != null && space.board == board) {
            Player currentPlayer = board.getCurrentPlayer();
            if (currentPlayer != null && space.getPlayer() == null) {
                currentPlayer.setSpace(space);
                int playerNumber = (board.getPlayerNumber(currentPlayer) + 1) % board.getPlayersNumber();
                board.setCurrentPlayer(board.getPlayer(playerNumber));
            }
        }

    }

    // XXX: V2
    /**
     * This is a method that starts the game programming phase of a board.
     * The method then goes through each player in the game and performs the following actions.
     * For each register a player can program a command for their robot to execute.
     * For each card in a players hand the method sets the CommandCardfields to have a randomly generated command card and be visible.
     * The actions set up the initial state of the programming phase, where players are programming their robots to execute a series of commands.
     * The game ensure that each player starts on an equal footing.   
     */
    public void startProgrammingPhase() {
        board.setPhase(Phase.PROGRAMMING);
        board.setCurrentPlayer(board.getPlayer(0));
        board.setStep(0);

        for (int i = 0; i < board.getPlayersNumber(); i++) {
            Player player = board.getPlayer(i);
            if (player != null) {
                for (int j = 0; j < Player.NO_REGISTERS; j++) {
                    CommandCardField field = player.getProgramField(j);
                    field.setCard(null);
                    field.setVisible(true);
                }
                for (int j = 0; j < Player.NO_CARDS; j++) {
                    CommandCardField field = player.getCardField(j);
                    field.setCard(generateRandomCommandCard());
                    field.setVisible(true);
                }
            }
        }
    }

    public void setCheckpointPositions(JSONArray checkpoints) {

        for (int i = 0; i < checkpoints.size(); i++) {
            JSONObject checkpoint = (JSONObject) checkpoints.get(i);
            JSONObject position = (JSONObject) checkpoint.get("position");
            System.out.println(checkpoint.toJSONString());
            Space space = board.getSpace(Integer.parseInt((String) position.get("x")), Integer.parseInt((String) position.get("y")));

            this.checkpointPositions.add(space);
        }
    }

    public ArrayList<Space> getCheckpointPositions() {
        return this.checkpointPositions;
    }

    public void loadPhase(JSONObject save) {
        JSONObject savedBoard = (JSONObject) save.get("board");
        JSONArray players = (JSONArray) save.get("players");
        JSONArray checkpoints = (JSONArray) save.get("checkpoints");

        setCheckpointPositions(checkpoints);

        for (Object player : players) {
            Player boardPlayer = board.getPlayer(Integer.parseInt((String) ((JSONObject) player).get("ID")));
            ArrayList<String> playerHand = (ArrayList<String>) ((JSONObject) player).get("playerHand");
            ArrayList<String> programHand = (ArrayList<String>) ((JSONObject) player).get("programHand");

            for (int j = 0; j < Player.NO_REGISTERS; j++) {
                CommandCardField field = boardPlayer.getProgramField(j);
                String stringCard = programHand.get(j);
                if (stringCard == null) {
                    field.setCard(null);
                } else {
                    CommandCard commandCard = new CommandCard(Command.valueOf(programHand.get(j)));
                    field.setCard(commandCard);
                }

                field.setVisible(true);
            }

            for (int j = 0; j < Player.NO_CARDS; j++) {
                CommandCardField field = boardPlayer.getCardField(j);
                String stringCard = playerHand.get(j);
                if (stringCard == null) {
                    field.setCard(null);
                } else {
                    CommandCard commandCard = new CommandCard(Command.valueOf(playerHand.get(j)));
                    field.setCard(commandCard);
                }

                field.setVisible(true);
            }
        }
        if (Objects.equals(savedBoard.get("phase"), "ACTIVATION")) {
            makeProgramFieldsInvisible();
            makeProgramFieldsVisible(Integer.parseInt((String) savedBoard.get("currentStep")));
        }

        board.setPhase(Phase.valueOf((String) savedBoard.get("phase")));
        board.setCurrentPlayer(board.getPlayer(Integer.parseInt((String) savedBoard.get("currentPlayer"))));
        board.setStep(Integer.parseInt((String) savedBoard.get("currentStep")));
        board.setStepMode((Boolean) savedBoard.get("stepMode"));
    }

    // XXX: V2 
    /**
     * This is a method that generates a random CommandCard from the set of available commands.
     * The method first creates an array of all possible command values using the "command.values()" method.
     * It then generates a random array using the "Math.random()" method.
     * This random integer is used as an index to select a random command from the array.
     * The method creates and returns a new CommandCard object using the randomly, 
     * selected command as an argument for the CommandCard constructor.
     *
     * @return CommandCard
     */
    private CommandCard generateRandomCommandCard() {
        Command[] commands = Command.values();
        int random = (int) (Math.random() * commands.length);
        return new CommandCard(commands[random]);
    }

    // XXX: V2
    /**
     * This is a method that finishes the programming phase of a board game and starts the activation phase.
     * This method prepares the game for the activation phase, where players activate their robots to execute the commands
     * that they programmed in the programming phase. By setting the game phase to activation and setting the current player
     * and step the appropriate values, the game ensures that players will take turns activating their robots in the correct order.
     *   
     */
    public void finishProgrammingPhase() {
        makeProgramFieldsInvisible();
        makeProgramFieldsVisible(0);
        board.setPhase(Phase.ACTIVATION);
        board.setCurrentPlayer(board.getPlayer(0));
        board.setStep(0);
    }

    // XXX: V2
    /**
     * This is a private method that makes the CommandCardFields for a particular register visible for all players in the game.
     * The method takes a "register" as an argument, which likely represent the index of the register that should be made visible.
     * It first checks if the register is within the valid range, if the register is outside the range,
     * the method does not make any changes to the CommandCardFields.
     * If the register is within the valid range, the method then goes through each player in the game and makes the 
     * CommandCardFields for the specified register visible.
     * This can indicate that players are in the programming phase of the game and are currently programming their robots for the
     * specified register.
     *
     * @param register the register to make visible
     */
    private void makeProgramFieldsVisible(int register) {
        if (register >= 0 && register < Player.NO_REGISTERS) {
            for (int i = 0; i < board.getPlayersNumber(); i++) {
                Player player = board.getPlayer(i);
                CommandCardField field = player.getProgramField(register);
                field.setVisible(true);
            }
        }
    }

    // XXX: V2
    /**
     * This method makes certain fields invisible in a board game.
     * THe method first loops through all the player in the game by using the "Board.getPlayersNumber" method,
     * Which returns the number of players in the board.
     * It then retrieves each player object one by one using the "board.getPlayer" method, where "i" is the index of the player being retrieved.
     * The code then loops through all the command card fields in the players program field using the "player.no.register" constant.
     * This constant represents the number of command card fields in a players program field.
     * For each command card field, the code retrieves the "CommandCardField" object from the players program field and sets its visibility
     * to false using the "setVisible(false)" method. This hides the command card field from view.
     */
    private void makeProgramFieldsInvisible() {
        for (int i = 0; i < board.getPlayersNumber(); i++) {
            Player player = board.getPlayer(i);
            for (int j = 0; j < Player.NO_REGISTERS; j++) {
                CommandCardField field = player.getProgramField(j);
                field.setVisible(false);
            }
        }
    }

    // XXX: V2
    /**
     * The method starts by calling the "board.setStepMode(false)" method which disables the step mode fom the game.
     * Step mode is a feature that allows the game to execute one step at a time.
     * After disabling the step mode, the method then calls the "continuePrograms()" method. This method contains executing the program
     * of each player in the game.  
     */
    public void executePrograms() {
        board.setStepMode(false);
        continuePrograms();
    }

    // XXX: V2
    /**
     * This method first calls "board.setStepMode(true)" which enables the step mode for the game.
     * After enabling step mode, the method then calls the "continuePrograms()" method. This likely contains the executing
     * the program of each player in the game.   
     */
    public void executeStep() {
        board.setStepMode(true);
        continuePrograms();
    }

    // XXX: V2
    private void continuePrograms() {
        do {
            executeNextStep();
        } while (board.getPhase() == Phase.ACTIVATION && !board.isStepMode());
    }

    // XXX: V2
    /**
     * First gets the current player from the "board" object and assign it to the "currentPlayer" variable.
     * if the current phase of the is "phase.activation" and "currentPlayer" it is not null, then the method will proceed to execute the next steps of the game.
     * it gets the current step from "board" object and assign it to the "step" variable.
     * if step is between 0 and "player_no_register" the method checks whether the command card in the players program field at the current
     * step is an "option_left_right" command. if it is it calls the "optionCard()" method and returns.
     * if there is a command card in the players program field at the current step, the method gets the "command" enum value from the card and calls the 
     * "executeCommand" method to execute it.
     * if there are still more steps to go, the methods calls the "makeProgramFieldsVisible" method to reveal the next steps field,
     * sets the "board" step counter to the next step and sets the current player to the first player.
     * if there are no more steps to go, the method calls the "startProgrammingPhase" method to start the next phase of the game.
     */
    private void executeNextStep() {
        Player currentPlayer = board.getCurrentPlayer();
        if (board.getPhase() == Phase.ACTIVATION && currentPlayer != null) {
            int step = board.getStep();
            if (step >= 0 && step < Player.NO_REGISTERS) {
                CommandCard card = currentPlayer.getProgramField(step).getCard();
                if (card != null && card.command == Command.OPTION_LEFT_RIGHT) {
                    this.optionCard();
                    return;
                }
                if (card != null) {
                    Command command = card.command;
                    executeCommand(currentPlayer, command);
                }
                int nextPlayerNumber = board.getPlayerNumber(currentPlayer) + 1;
                if (nextPlayerNumber < board.getPlayersNumber()) {
                    board.setCurrentPlayer(board.getPlayer(nextPlayerNumber));
                } else {
                    step++;
                    if (step < Player.NO_REGISTERS) {
                        makeProgramFieldsVisible(step);
                        board.setStep(step);
                        board.setCurrentPlayer(board.getPlayer(0));
                    } else {
                        startProgrammingPhase();
                    }
                }
            } else {
                // this should not happen
                assert false;
            }
        } else {
            // this should not happen
            assert false;
        }
    }

    // XXX: V2
    private void executeCommand(@NotNull Player player, Command command) {
        if (player != null && player.board == board && command != null) {
            // XXX This is a very simplistic way of dealing with some basic cards and
            //     their execution. This should eventually be done in a more elegant way
            //     (this concerns the way cards are modelled as well as the way they are executed).

            switch (command) {
                case FORWARD:
                    this.moveForward(player);
                    break;
                case RIGHT:
                    this.turnRight(player);
                    break;
                case LEFT:
                    this.turnLeft(player);
                    break;
                case FAST_FORWARD:
                    this.fastForward(player);
                    break;
                case UTURN:
                    this.turnLeft(player);
                    this.turnLeft(player);
                    break;
                case FASTEST_FORWARD:
                    this.fastForward(player);
                    this.moveForward(player);
                    break;
                default:
                    // DO NOTHING (for now)
            }
        }
    }

    /**
     * This code is used as a way to move the player forward.
     * This is done by getting both the heading and space that the player is at.
     * If there is a player on the given space they will be moved in the direction they are pushed.
     * @param player the player that should be moved
     * @author Mads Rasmussen
     */
    public void moveForward(@NotNull Player player) {
        boolean wallCheck = wallCheck(player);
        if(wallCheck){
            return;
        }
        Space space = player.getSpace();
        if (player != null && player.board == board && space != null) {
            Heading heading = player.getHeading();

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
               for(int i=spacelist.size()-1; i>=0; i--){
                 Space somespace = spacelist.get(i);
                 Player someplayer = somespace.getPlayer();
                 Space neighbortarget = board.getNeighbour(somespace, heading);
                 if (neighbortarget != null) {
                     neighbortarget.setPlayer(someplayer);
                 }
               }
            movePlayerOnConveyor(player);
            MovePlayerOnGear(player);
            PlayerCheckpoint(player);

                // XXX note that this removes an other player from the space, when there
                //     is another player on the target. Eventually, this needs to be
                //     implemented in a way so that other players are pushed away

                /* Check om der allerede er en spiller på target. (player2)
                * Hvis ja, så skal den spiller fløttes i samme heading en gang.
                * dvs. Man skal kalde board.getNeighbour med target(det space der er "optaget") og heading (som spilleren havede)
                * Til sidst skal der kaldes target2.setPlayer(player2) og target.setPlayer(player);
                * */
        }
    }

    /**
     * This method is used to move the player 1 in the direction of the conveyor belt.
     * It is called when a player lands on a conveyor belt.
     *
     * @param player The player that should be moved.
     * @author Mads Halberg
     */
    public void movePlayerOnConveyor(Player player) {
        Space space = player.getSpace();
        if (player != null && player.board == board && space != null) {
            ArrayList<Obstacle> obstacles = space.getObstacle();
            int index = 0;
            boolean obstacleFound = false;
            for (int i=0; i< obstacles.size(); i++) {
                if(obstacles.get(i) != null && obstacles.get(i) instanceof Conveyor) {
                    obstacleFound = true;
                    index = i;
                    break;
                }
            }
            if (!obstacleFound) {
                return;
            }

            Heading heading = obstacles.get(index).getHeading();

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
            ArrayList<Obstacle> obstacles = space.getObstacle();
            boolean obstacleFound = false;
            for (int i=0; i< obstacles.size(); i++) {
                if(obstacles.get(i) != null && obstacles.get(i) instanceof Gear) {
                    obstacleFound = true;
                    break;
                }
            }
            if (!obstacleFound) {
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

        appController.update(board);
    }

    public void PlayerCheckpoint(Player player) {
        Space space = player.getSpace();
        if (player != null && player.board == board && space != null){
            ArrayList<Obstacle> obstacles = space.getObstacle();
            boolean obstacleFound = false;
            for (int i=0; i< obstacles.size(); i++) {
                if(obstacles.get(i) != null && obstacles.get(i) instanceof Checkpoint) {
                    obstacleFound = true;
                    break;
                }
            }
            if (!obstacleFound) {
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
        Heading playerHeading = player.getHeading();

        ArrayList<Obstacle> obstacles = space.getObstacle();
        for (Obstacle obstacle : obstacles) {
            if (obstacle != null && obstacle instanceof Wall) {
                Heading wallHeading = obstacle.getHeading();
                if (wallHeading == playerHeading) {
                    return true;
                }
            }
        }

        Space nextSpace = board.getNeighbour(space, playerHeading);
        ArrayList<Obstacle> nextObstacles = nextSpace.getObstacle();
        for (Obstacle nextObstacle : nextObstacles) {
            if (nextObstacle != null && nextObstacle instanceof Wall) {
                Heading wallHeading = nextObstacle.getHeading().next().next();
                if (wallHeading == playerHeading) {
                    return true;
                }
            }
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
        if (sourceCard != null && targetCard == null) {
            target.setCard(sourceCard);
            source.setCard(null);
            return true;
        } else {
            return false;
        }
    }
    /**
     * The method takes two parameter "player and option".
     * The first line of the method sets the game phase to "phase.activation". This indicates that the game is currently in 
     * the activation phase, where the player choose and execute their commands option.
     * The method then uses a switch statement to execute the chosen command option. if the option "left" the "turnLeft()" method is called
     * with the "player" parameter, which turn the player token left on the game board.,
     * The "board.getStep()" method checks if the current player is not the last player in the game, the method sets the current
     * player to the last player in the game by calling "board.setCurrentPlayer()".
     * The "makeProgramFieldsVisible()" method sets the current step to the new step by calling "board.setCurrentPlayer()", and continues the game.
     * If the new step is greater than that or equal to the total number of program fields, the method starts programming phase by calling
     * the "startProgrammingPhase()" method. Which reset the game for the next round.
     *
     * @param player the player choosing
     * @param option the options to choose from
     */
    public void choose(Player player, Command option) {
        board.setPhase(Phase.ACTIVATION);

        switch (option) {
            case LEFT -> this.turnLeft(player);
            case RIGHT -> this.turnRight(player);
        }

        int step = board.getStep();
        int nextPlayerNumber = board.getPlayerNumber(player) + 1;
        if (nextPlayerNumber < board.getPlayersNumber()) {
            board.setCurrentPlayer(board.getPlayer(nextPlayerNumber));
        } else {
            step++;
            if (step < Player.NO_REGISTERS) {
                makeProgramFieldsVisible(step);
                board.setStep(step);
                board.setCurrentPlayer(board.getPlayer(0));
            } else {
                startProgrammingPhase();
            }
        }
    }

    private void endGame() {
        appController.gameWon();
    }

    /**
     * A method called when no corresponding controller operation is implemented yet. This
     * should eventually be removed.
     */
    public void notImplemented() {
        // XXX just for now to indicate that the actual method is not yet implemented
        assert false;
    }

}