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

import dk.dtu.compute.se.pisd.designpatterns.observer.Subject;
import dk.dtu.compute.se.pisd.roborally.model.Board;
import dk.dtu.compute.se.pisd.roborally.model.Heading;
import dk.dtu.compute.se.pisd.roborally.model.Player;
import dk.dtu.compute.se.pisd.roborally.model.Space;
import dk.dtu.compute.se.pisd.roborally.model.obstacles.Checkpoint;
import dk.dtu.compute.se.pisd.roborally.model.obstacles.Obstacle;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.StrokeLineCap;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.io.File;

/**
 * ...
 *
 * @author Ekkart Kindler, ekki@dtu.dk
 *
 */
public class SpaceView extends StackPane implements ViewObserver {

    final public static int SPACE_HEIGHT = 60; // 75;
    final public static int SPACE_WIDTH = 60; // 75;

    public final Space space;
    private final ImageView backgroundImage;

    public SpaceView(@NotNull Space space) {
        this.space = space;
        backgroundImage = new ImageView();

        // XXX the following styling should better be done with styles
        this.setPrefWidth(SPACE_WIDTH);
        this.setMinWidth(SPACE_WIDTH);
        this.setMaxWidth(SPACE_WIDTH);

        this.setPrefHeight(SPACE_HEIGHT);
        this.setMinHeight(SPACE_HEIGHT);
        this.setMaxHeight(SPACE_HEIGHT);

        getChildren().add(backgroundImage);

        // This space view should listen to changes of the space
        space.attach(this);
        update(space);
    }

    private void updatePlayer() {
        this.getChildren().clear();

        // Add the background image first
        this.getChildren().add(backgroundImage);

        // Add the checkpoint if it exists
        drawCheckpoint();

        Player player = space.getPlayer();
        if (player != null) {
            Polygon arrow = new Polygon(0.0, 0.0,
                    10.0, 20.0,
                    20.0, 0.0);
            try {
                arrow.setFill(Color.valueOf(player.getColor()));
            } catch (Exception e) {
                arrow.setFill(Color.MEDIUMPURPLE);
            }

            arrow.setRotate((90 * player.getHeading().ordinal()) % 360);
            this.getChildren().add(arrow);
        }
    }

    private void drawCheckpoint() {
        this.getChildren().removeIf(node -> node instanceof Polygon);

        Obstacle obstacle = space.getObstacle();
        if (obstacle instanceof Checkpoint) {
            Polygon checkpoint = new Polygon(0.0, 0.0,
                    20.0, 0.0,
                    20.0, 20.0,
                    0.0, 20.0);
            try {
                checkpoint.setFill(Color.valueOf(obstacle.getColor()));
            } catch (Exception e) {
                checkpoint.setFill(Color.MEDIUMPURPLE);
            }

            checkpoint.setRotate((90 * obstacle.getHeading().ordinal()) % 360);
            this.getChildren().add(checkpoint);
        }
    }

    @Override
    public void updateView(Subject subject) {
        if (subject == this.space) {
            updateBackgroundImage();
            updatePlayer();
        }
    }

    private void updateBackgroundImage() {
        String filepath = new File("images/floor_tile.JPG").toURI().toString();
        Image fieldImage = new Image(filepath);

        BackgroundImage backgroundImage = new BackgroundImage(
                fieldImage,
                BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.DEFAULT,
                new BackgroundSize(SPACE_WIDTH, SPACE_HEIGHT, false, false, false, false)
        );
        Background background = new Background(backgroundImage);
        setBackground(background);
    }
}
