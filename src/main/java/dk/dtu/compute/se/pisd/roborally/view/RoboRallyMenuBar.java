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
package dk.dtu.compute.se.pisd.roborally.view;

import dk.dtu.compute.se.pisd.roborally.controller.AppController;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import org.json.simple.parser.ParseException;

import java.io.IOException;

/**
 * ...
 *
 * @author Ekkart Kindler, ekki@dtu.dk
 *
 */
public class RoboRallyMenuBar extends MenuBar {

    private AppController appController;

    private Menu controlMenu;

    private MenuItem saveGame;

    private MenuItem newGame;

    private MenuItem loadGame;

    private MenuItem stopGame;

    private MenuItem exitApp;

    private MenuItem joinGame;

    public RoboRallyMenuBar(AppController appController) {
        this.appController = appController;

        controlMenu = new Menu("File");
        this.getMenus().add(controlMenu);

        newGame = new MenuItem("New Game");
        newGame.setOnAction( e -> {
            try {
                this.appController.newGame();
            } catch (IOException | ParseException ex) {
                throw new RuntimeException(ex);
            }
        });
        controlMenu.getItems().add(newGame);

        stopGame = new MenuItem("Stop Game");
        stopGame.setOnAction( e -> {
            try {
                this.appController.stopGame();
            } catch (IOException | ParseException ex) {
                throw new RuntimeException(ex);
            }
        });
        controlMenu.getItems().add(stopGame);

        saveGame = new MenuItem("Save Game");
        saveGame.setOnAction( e -> {
            try {
                this.appController.saveGame();
            } catch (IOException | ParseException ex) {
                throw new RuntimeException(ex);
            }
        });
        controlMenu.getItems().add(saveGame);

        loadGame = new MenuItem("Load Game");
        loadGame.setOnAction( e -> {
            try {
                this.appController.loadGame();
            } catch (IOException | ParseException ex) {
                throw new RuntimeException(ex);
            }
        });
        controlMenu.getItems().add(joinGame);

        joinGame = new MenuItem("Join Game");
        joinGame.setOnAction( e -> {
            try {
                this.appController.joinGame();
            } catch (IOException | ParseException ex) {
                throw new RuntimeException(ex);
            }
        });
        controlMenu.getItems().add(loadGame);

        exitApp = new MenuItem("Exit");
        exitApp.setOnAction( e -> {
            try {
                this.appController.exit();
            } catch (IOException | ParseException ex) {
                throw new RuntimeException(ex);
            }
        });
        controlMenu.getItems().add(exitApp);

        controlMenu.setOnShowing(e -> update());
        controlMenu.setOnShown(e -> this.updateBounds());
        update();
    }

    public void update() {
        if (appController.isGameRunning()) {
            if (appController.isOnlinePlay()) {
                newGame.setVisible(false);
                stopGame.setVisible(true);
                saveGame.setVisible(true);
                loadGame.setVisible(false);
                joinGame.setVisible(false);
            } else {
                newGame.setVisible(false);
                stopGame.setVisible(true);
                saveGame.setVisible(true);
                loadGame.setVisible(false);
                joinGame.setVisible(false);
            }
        } else if (appController.isOnlinePlay()) {
            newGame.setVisible(true);
            stopGame.setVisible(false);
            saveGame.setVisible(false);
            loadGame.setVisible(true);
            joinGame.setVisible(true);
        } else {
                newGame.setVisible(true);
                stopGame.setVisible(false);
                saveGame.setVisible(false);
                loadGame.setVisible(true);
                joinGame.setVisible(false);
            }
        }
    }

