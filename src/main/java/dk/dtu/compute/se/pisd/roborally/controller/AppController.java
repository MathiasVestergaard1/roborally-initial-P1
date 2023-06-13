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

import dk.dtu.compute.se.pisd.designpatterns.observer.Observer;
import dk.dtu.compute.se.pisd.designpatterns.observer.Subject;

import dk.dtu.compute.se.pisd.roborally.RoboRally;

import dk.dtu.compute.se.pisd.roborally.model.*;

import dk.dtu.compute.se.pisd.roborally.model.obstacles.Checkpoint;
import dk.dtu.compute.se.pisd.roborally.model.obstacles.Conveyor;
import dk.dtu.compute.se.pisd.roborally.model.obstacles.Gear;
import dk.dtu.compute.se.pisd.roborally.model.obstacles.Obstacle;
import dk.dtu.compute.se.pisd.roborally.model.obstacles.Wall;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.TextInputDialog;
import org.jetbrains.annotations.NotNull;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;
import java.awt.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;

/**
 * ...
 *
 * @author Ekkart Kindler, ekki@dtu.dk
 *
 */
public class AppController implements Observer {

    final private List<Integer> PLAYER_NUMBER_OPTIONS = Arrays.asList(2, 3, 4, 5, 6);
    final private List<String> PLAYER_COLORS = Arrays.asList("red", "green", "blue", "orange", "grey", "magenta");
    final private RoboRally roboRally;
    private String loadedFile = null;
    private String originBoard = null;
    private boolean onlinePlay = false;
    private GameController gameController;

    public AppController(@NotNull RoboRally roboRally) {
        this.roboRally = roboRally;
    }

    public void newGame() throws IOException, ParseException {
        ChoiceDialog<Integer> dialog = new ChoiceDialog<>(PLAYER_NUMBER_OPTIONS.get(0), PLAYER_NUMBER_OPTIONS);
        dialog.setTitle("Player number");
        dialog.setHeaderText("Select number of players");
        Optional<Integer> result = dialog.showAndWait();

        if (result.isEmpty()) {
            gameController = null;
            return;
        }
        if (gameController != null) {
            // The UI should not allow this, but in case this happens anyway.
            // give the user the option to save the game or abort this operation!
            if (!stopGame()) {
                return;
            }
        }

        int boardCount = 0;
        boolean defaultGame = false;

        JSONObject jsonFile;
        JSONObject boards = new JSONObject();

        if (!onlinePlay) {
            try {
                FileReader reader = new FileReader("save.json");
                JSONParser jsonParser = new JSONParser();
                jsonFile = (JSONObject) jsonParser.parse(reader);

                boards = (JSONObject) jsonFile.get("boards");
                boardCount = boards.size();
            } catch (IOException | NullPointerException ignored) {
                defaultGame = true;
            }
        } else {
            String response = executeGet("http://127.0.0.1:8080/roborally/boards", "");

            JSONParser jsonParser = new JSONParser();
            boards = (JSONObject) jsonParser.parse(response);

            boardCount = boards.size();
        }


        if (boardCount == 0) {
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("No boards found");
            alert.setHeaderText("No boards found");
            String s ="You don't have any boards in your save.json file, you will play on the default board";
            alert.setContentText(s);
            alert.show();
            defaultGame = true;
        }

        loadedFile = null;

        if (defaultGame) {
            Board board = new Board(8, 8);
            gameController = new GameController(board, this);
            int no = result.get();
            for (int i = 0; i < no; i++) {
                Player player = new Player(board, PLAYER_COLORS.get(i), "Player " + (i + 1));
                board.addPlayer(player);
                player.setSpace(board.getSpace(i % board.width, i));
            }
        } else {
            ArrayList<String> BOARDS_LIST = new ArrayList<String>();

            for (Object o : boards.keySet()) {
                String key = o.toString();
                BOARDS_LIST.add(key);
            }

            ChoiceDialog<String> dialogList = new ChoiceDialog<>(BOARDS_LIST.get(0), BOARDS_LIST);
            dialogList.setTitle("Boards");
            dialogList.setHeaderText("Select board to play on");
            Optional<String> resultList = dialogList.showAndWait();

            if (resultList.isEmpty()) {
                gameController = null;
                return;
            }

            JSONObject boardConfig = new JSONObject();

            this.originBoard = resultList.get();

            if (onlinePlay) {
                String response = executeGet("http://127.0.0.1:8080/roborally/getGame", "name=" + resultList.get());

                JSONParser jsonParser = new JSONParser();
                boardConfig = (JSONObject) jsonParser.parse(response);
            } else {
                boardConfig = (JSONObject) boards.get(resultList.get());
            }

            JSONObject jsonBoard = (JSONObject) boardConfig.get("board");

            Board board = new Board(Integer.parseInt((String) jsonBoard.get("width")), Integer.parseInt((String) jsonBoard.get("height")));
            gameController = new GameController(board, this);
            gameController.setCheckpointPositions((JSONArray) boardConfig.get("checkpoints"));

            JSONArray jsonObstacles = (JSONArray) boardConfig.get("obstacles");

            /*
                    A GUI that shows amount of players joined and the player limit,
                    the "start game" button becomes available once the game is full.
             */

            int no = result.get();
            for (int i = 0; i < no; i++) {
                Player player = new Player(board, PLAYER_COLORS.get(i), "Player " + (i + 1));
                board.addPlayer(player);
                player.setSpace(board.getSpace(i % board.width, 0));
            }

            for (Object o : jsonObstacles) {
                JSONObject jsonObstacle = (JSONObject) o;
                JSONObject jsonSpace = (JSONObject) jsonObstacle.get("position");
                String type = (String) jsonObstacle.get("type");
                Heading heading = Heading.valueOf((String) jsonObstacle.get("heading"));

                Space space = board.getSpace(Integer.parseInt((String) jsonSpace.get("x")), Integer.parseInt((String) jsonSpace.get("y")));
                Obstacle obstacle = null;
                System.out.println(jsonSpace.toString());
                switch (type) {
                    case ("Conveyor") -> {
                        obstacle = new Conveyor(space, heading);
                    }
                    case ("Gear") -> {
                        obstacle = new Gear(space, heading);
                    }
                    case ("Wall") -> {
                        obstacle = new Wall(space, heading);
                    }
                    case ("Checkpoint") -> {
                        obstacle = new Checkpoint(space, heading);
                    }
                }
                space.setObstacle(obstacle);
                board.setObstacle();
            }
        }

        /*
                Send info to the server, about how the board is set up etc.
         */

        gameController.startProgrammingPhase();

        roboRally.createBoardView(gameController);
    }


    /**
     * Saves the current state of the game to a json file, that can be loaded later.
     *
     * @throws IOException
     *
     * @author Mads Hauberg
     */
    public void saveGame() throws IOException, ParseException {
        int saveCount = 0;

        JSONObject jsonFile = new JSONObject();
        JSONObject saves;

        if (!onlinePlay) {
            try {
                FileReader reader = new FileReader("save.json");
                JSONParser jsonParser = new JSONParser();
                jsonFile = (JSONObject) jsonParser.parse(reader);

                saves = (JSONObject) jsonFile.get("saves");
                saveCount = saves.size();
            } catch (IOException | NullPointerException ignored) {
                saves = new JSONObject();
            }
        } else {
            String response = executeGet("http://127.0.0.1:8080/roborally/saves", "");

            JSONParser jsonParser = new JSONParser();
            saves = (JSONObject) jsonParser.parse(response);

            saveCount = saves.size();
        }


        String saveName = (loadedFile != null) ? loadedFile :  "save" + (saveCount + 1);

        TextInputDialog textDialog = new TextInputDialog(saveName);
        textDialog.setTitle("Save");
        textDialog.setHeaderText("Enter save name");

        Optional<String> result = textDialog.showAndWait();

        if (result.isPresent()) {
            saveName = result.get();
        } else {
            return;
        }

        boolean overwrite = false;

        if (saves.get(saveName) != null) {
            Alert alert = new Alert(AlertType.CONFIRMATION);
            alert.setTitle("Save");
            String s = "A save with that name already exists, do you want do overwrite that save?";
            alert.setContentText(s);

            Optional<ButtonType> alertResult = alert.showAndWait();

            if ((result.isPresent()) && (alertResult.get() == ButtonType.CANCEL)) {
                saveGame();
                return;
            }
            overwrite = true;
        }

        loadedFile = saveName;

        Board board = gameController.board;

        List<Player> players = gameController.board.getPlayers();
        ArrayList<Space> spaces = board.getSpacesWithObstacles();

        JSONArray jsonPlayers = new JSONArray();
        JSONArray jsonObstacles = new JSONArray();

        for (int i = 0; i < players.size(); i++) {
            Player player = players.get(i);

            JSONObject jsonPlayer = new JSONObject();
            JSONObject jsonSpace = new JSONObject();

            ArrayList<String> playerCardsArray = new ArrayList<>();
            ArrayList<String> programCardsArray = new ArrayList<>();

            jsonSpace.put("x", Integer.toString(player.getSpace().x));
            jsonSpace.put("y", Integer.toString(player.getSpace().y));

            jsonPlayer.put("ID", Integer.toString(i));
            jsonPlayer.put("heading", player.getHeading().toString());
            jsonPlayer.put("position", jsonSpace);
            jsonPlayer.put("points", Integer.toString(player.getCheckpointCounter()));

            CommandCardField[] playerCards = player.getCardFields();
            CommandCardField[] programCards = player.getProgramFields();

            for (CommandCardField commandCardField : playerCards) {
                CommandCard commandCard = commandCardField.getCard();
                String cardName = null;
                if (commandCard != null) {
                    cardName = commandCard.command.toString();
                }
                playerCardsArray.add(cardName);
            }

            for (CommandCardField commandCardField : programCards) {
                CommandCard commandCard = commandCardField.getCard();
                String cardName = null;
                if (commandCard != null) {
                    cardName = commandCard.command.toString();
                }
                programCardsArray.add(cardName);

            }

            jsonPlayer.put("playerHand", playerCardsArray.toString());
            jsonPlayer.put("programHand", programCardsArray.toString());

            jsonPlayers.add(jsonPlayer);
        }

        for (Space space : spaces) {

            JSONObject jsonSpace = new JSONObject();

            jsonSpace.put("x", Integer.toString(space.x));
            jsonSpace.put("y", Integer.toString(space.y));
            ArrayList<Obstacle> obstacles = space.getObstacle();

            for (Obstacle obstacle : obstacles) {
                JSONObject jsonObstacle = new JSONObject();
                jsonObstacle.put("heading", obstacle.getHeading().toString());
                jsonObstacle.put("position", jsonSpace);

                if (obstacle instanceof Conveyor) {
                    jsonObstacle.put("type", "Conveyor");
                } else if (obstacle instanceof Gear) {
                    jsonObstacle.put("type", "Gear");
                } else if (obstacle instanceof Wall) {
                    jsonObstacle.put("type", "Wall");
                } else if (obstacle instanceof Checkpoint) {
                    jsonObstacle.put("type", "Checkpoint");
                }
                jsonObstacles.add(jsonObstacle);
            }
        }

        JSONArray jsonCheckpoints = new JSONArray();

        for (Space space : gameController.getCheckpointPositions()) {
            JSONObject jsonCheckpoint = new JSONObject();
            JSONObject jsonPosition = new JSONObject();

            jsonCheckpoint.put("x", Integer.toString(space.x));
            jsonCheckpoint.put("y", Integer.toString(space.y));

            jsonPosition.put("position", jsonCheckpoint);
            jsonCheckpoints.add(jsonPosition);
        }

        JSONObject jsonBoard = new JSONObject();

        jsonBoard.put("width", Integer.toString(gameController.board.width));
        jsonBoard.put("height", Integer.toString(gameController.board.height));
        jsonBoard.put("phase", board.getPhase().toString());
        jsonBoard.put("currentPlayer", Integer.toString(gameController.board.getPlayerNumber(gameController.board.getCurrentPlayer())));
        jsonBoard.put("currentStep", Integer.toString(gameController.board.getStep()));
        jsonBoard.put("stepMode", gameController.board.isStepMode());
        jsonBoard.put("originBoard", this.originBoard);

        JSONObject save = new JSONObject();
        save.put("board", jsonBoard);
        save.put("players", jsonPlayers);
        save.put("obstacles", jsonObstacles);
        save.put("checkpoints", jsonCheckpoints);

        if (onlinePlay && overwrite) {
            JSONObject postSave = new JSONObject();
            postSave.put(saveName, save);

            executePost("http://127.0.0.1:8080/roborally/overwriteGame", postSave.toJSONString());
            return;
        } else if (onlinePlay) {
            JSONObject postSave = new JSONObject();
            postSave.put(saveName, save);

            executePost("http://127.0.0.1:8080/roborally/saveGame", postSave.toJSONString());
            return;
        }

        saves.put(saveName, save);

        jsonFile.put("saves", saves);

        Files.write(Paths.get("save.json"), jsonFile.toJSONString().getBytes());
    }

    /**
     * Loads a saved json file and set up a new game with the data from that file.
     *
     * @throws IOException
     * @throws ParseException
     *
     * @author Mads Hauberg
     */
    public void loadGame() throws IOException, ParseException {
        int saveCount;

        JSONObject jsonFile;
        JSONObject saves;

        if (!onlinePlay) {
            try {
                    FileReader reader = new FileReader("save.json");
                    JSONParser jsonParser = new JSONParser();
                    jsonFile = (JSONObject) jsonParser.parse(reader);

                    saves = (JSONObject) jsonFile.get("saves");
                    saveCount = saves.size();
                } catch (IOException | NullPointerException ignored) {
                    Alert alert = new Alert(AlertType.INFORMATION);
                    alert.setTitle("No saves found");
                    alert.setHeaderText("No saves found");
                    String s ="You don't have any saved games in your save.json file";
                    alert.setContentText(s);
                    alert.show();
                    return;
                }
        } else {
            String response = executeGet("http://127.0.0.1:8080/roborally/saves", "");

            JSONParser jsonParser = new JSONParser();
            saves = (JSONObject) jsonParser.parse(response);

            saveCount = saves.size();
            System.out.println(response);
        }



        if (saveCount == 0) {
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("No saves found");
            alert.setHeaderText("No saves found");
            String s ="You don't have any saved games in your save.json file";
            alert.setContentText(s);
            alert.show();
            return;
        }
        ArrayList<String> SAVES_LIST = new ArrayList<String>();

        for (Object o : saves.keySet()) {
            String key = o.toString();
            SAVES_LIST.add(key);
        }

        ChoiceDialog<String> dialog = new ChoiceDialog<>(SAVES_LIST.get(0), SAVES_LIST);
        dialog.setTitle("Saves");
        dialog.setHeaderText("Select save to load");
        Optional<String> result = dialog.showAndWait();

        if (result.isEmpty()) {
            gameController = null;
            return;
        }
        if (gameController != null) {
            // The UI should not allow this, but in case this happens anyway.
            // give the user the option to save the game or abort this operation!
            if (!stopGame()) {
                return;
            }
        }
        JSONObject save = (JSONObject) saves.get(result.get());
        loadedFile = result.get();
        JSONObject jsonBoard = (JSONObject) save.get("board");

        this.originBoard = (String) jsonBoard.get("originBoard");

        Board board = new Board(Integer.parseInt((String) jsonBoard.get("width")), Integer.parseInt((String) jsonBoard.get("height")));
        gameController = new GameController(board, this);

        JSONArray jsonPlayers = (JSONArray) save.get("players");
        JSONArray jsonObstacles = (JSONArray) save.get("obstacles");

        for (int i = 0; i < jsonPlayers.size(); i++) {
            JSONObject jsonPlayer = (JSONObject) jsonPlayers.get(i);
            JSONObject jsonSpace = (JSONObject) jsonPlayer.get("position");
            Player player = new Player(board, PLAYER_COLORS.get(i), "Player " + (i + 1));
            board.addPlayer(player);
            player.setSpace(board.getSpace(Integer.parseInt((String) jsonSpace.get("x")), Integer.parseInt((String) jsonSpace.get("y"))));
            player.setHeading(Heading.valueOf((String) jsonPlayer.get("heading")));
            player.setCheckpointCounter(Integer.parseInt((String) jsonPlayer.get("points")));
        }

        for (Object o : jsonObstacles) {
            JSONObject jsonObstacle = (JSONObject) o;
            JSONObject jsonSpace = (JSONObject) jsonObstacle.get("position");
            String type = (String) jsonObstacle.get("type");
            Heading heading = Heading.valueOf((String) jsonObstacle.get("heading"));

            Space space = board.getSpace(Integer.parseInt((String) jsonSpace.get("x")), Integer.parseInt((String) jsonSpace.get("y")));
            Obstacle obstacle = null;

            switch (type) {
                case ("Conveyor") -> {
                    obstacle = new Conveyor(space, heading);
                }
                case ("Gear") -> {
                    obstacle = new Gear(space, heading);
                }
                case ("Wall") -> {
                    obstacle = new Wall(space, heading);
                }
                case ("Checkpoint") -> {
                    obstacle = new Checkpoint(space, heading);
                }
            }
            space.setObstacle(obstacle);
            board.setObstacle();
        }

        gameController.loadPhase(save);

        roboRally.createBoardView(gameController);
    }

    public void gameWon() {
        Alert alert = new Alert(AlertType.INFORMATION);
        List<Player> players = gameController.board.getPlayers();
        String winner = null;
        StringBuilder leaderboard = new StringBuilder();
        for (int i = 0; i < players.size(); i++) {
            Player player = players.get(i);
            int points = player.getCheckpointCounter();
            if (points == 4) {
               winner = player.getName();
            } else {
                if (i > 0 && points > players.get(i-1).getCheckpointCounter()) {
                    leaderboard.insert(0, player.getName() + ": " + points + "\n");
                } else {
                    leaderboard.append("\n").append(player.getName()).append(": ").append(points);
                }
            }
        }
        leaderboard.insert(0, winner + ": 4\n");

        alert.setTitle(winner + " wins!");
        alert.setHeaderText("The game is over\n" + winner + " won the game");
        alert.setContentText(String.valueOf(leaderboard));
        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent()) {
            gameController = null;
            roboRally.createBoardView(null);
        }
    }

    /**
     * Stop playing the current game, giving the user the option to save
     * the game or to cancel stopping the game. The method returns true
     * if the game was successfully stopped (with or without saving the
     * game); returns false, if the current game was not stopped. In case
     * there is no current game, false is returned.
     *
     * @return true if the current game was stopped, false otherwise
     */
    public boolean stopGame() throws IOException, ParseException {
        if (gameController != null) {

            // here we save the game (without asking the user).
            saveGame();

            gameController = null;
            roboRally.createBoardView(null);
            return true;
        }
        return false;
    }

    public void exit() throws IOException, ParseException {
        if (gameController != null) {
            Alert alert = new Alert(AlertType.CONFIRMATION);
            alert.setTitle("Exit RoboRally?");
            alert.setContentText("Are you sure you want to exit RoboRally?");
            Optional<ButtonType> result = alert.showAndWait();

            if (!result.isPresent() || result.get() != ButtonType.OK) {
                return; // return without exiting the application
            }
        }

        // If the user did not cancel, the RoboRally application will exit
        // after the option to save the game
        if (gameController == null || stopGame()) {
            Platform.exit();
        }
    }

    public void joinGame() throws ParseException {
        String response = executeGet("http://127.0.0.1:8080/roborally/games", "");

        JSONParser jsonParser = new JSONParser();
        JSONObject games = (JSONObject) jsonParser.parse(response);

        int gameCount = games.size();
        System.out.println(response);

        if (gameCount == 0) {
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("No games found");
            alert.setHeaderText("No games found");
            String s = "There are no games available on the server";
            alert.setContentText(s);
            alert.show();
            return;
        }
    }

    public void setOnlinePlay() {
        this.onlinePlay = true;
    }

    public boolean isOnlinePlay() {
        return this.onlinePlay;
    }

    public boolean isGameRunning() {
        return gameController != null;
    }

    public static String executeGet(String targetURL, String urlParameters) {
        HttpURLConnection connection = null;

        try {
            //Create connection
            URL yahoo = new URL(targetURL + "?" + urlParameters);
            URLConnection yc = yahoo.openConnection();
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(
                            yc.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            return response.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    public static String executePost(String targetURL, String body) {
        HttpURLConnection connection = null;

        System.out.println(body);

        try {
            //Create connection
            URL url = new URL(targetURL);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type",
                    "application/json");

            connection.setRequestProperty("Content-Length",
                    Integer.toString(body.getBytes().length));
            connection.setRequestProperty("Content-Language", "en-US");

            connection.setUseCaches(false);
            connection.setDoOutput(true);

            //Send request
            DataOutputStream wr = new DataOutputStream (
                    connection.getOutputStream());
            wr.writeBytes(body);
            wr.close();

            //Get Response
            InputStream is = connection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            StringBuilder response = new StringBuilder(); // or StringBuffer if Java version 5+
            String line;
            while ((line = rd.readLine()) != null) {
                response.append(line);
                response.append('\r');
            }
            rd.close();
            return response.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }


    @Override
    public void update(Subject subject) {
        roboRally.createBoardView(gameController);
    }

}
