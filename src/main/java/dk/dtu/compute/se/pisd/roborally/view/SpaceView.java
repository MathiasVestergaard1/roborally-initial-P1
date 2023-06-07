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
import dk.dtu.compute.se.pisd.roborally.model.Heading;
import dk.dtu.compute.se.pisd.roborally.model.Player;
import dk.dtu.compute.se.pisd.roborally.model.Space;
import dk.dtu.compute.se.pisd.roborally.model.obstacles.Conveyor;
import dk.dtu.compute.se.pisd.roborally.model.obstacles.Checkpoint;
import dk.dtu.compute.se.pisd.roborally.model.obstacles.Obstacle;
import dk.dtu.compute.se.pisd.roborally.model.obstacles.Gear;
import dk.dtu.compute.se.pisd.roborally.model.obstacles.Wall;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.StrokeLineCap;
import org.jetbrains.annotations.NotNull;

/**
 * ...
 *
 * @author Ekkart Kindler, ekki@dtu.dk
 *
 */
public class SpaceView extends StackPane implements ViewObserver {

    final public static int SPACE_HEIGHT = 75; // 60; // 75;
    final public static int SPACE_WIDTH = 75;  // 60; // 75;

    public final Space space;


    public SpaceView(@NotNull Space space) {
        this.space = space;

        // XXX the following styling should better be done with styles
        this.setPrefWidth(SPACE_WIDTH);
        this.setMinWidth(SPACE_WIDTH);
        this.setMaxWidth(SPACE_WIDTH);

        this.setPrefHeight(SPACE_HEIGHT);
        this.setMinHeight(SPACE_HEIGHT);
        this.setMaxHeight(SPACE_HEIGHT);

        if ((space.x + space.y) % 2 == 0) {
            this.setStyle("-fx-background-color: white;");
        } else {
            this.setStyle("-fx-background-color: black;");
        }

        // updatePlayer();

        // This space view should listen to changes of the space
        space.attach(this);
        updateBackgroundImage();
        update(space);
    }

    private void updatePlayer() {
        this.getChildren().clear();

        // Add the background image first
        this.getChildren().add(backgroundImage);

        // Add the checkpoint if it exists
        drawCheckpoint();
        drawConveyor();
        drawGear();
        drawWall();

        Player player = space.getPlayer();
        if (player != null) {
            Polygon arrow = new Polygon(0.0, 0.0,
                    10.0, 20.0,
                    20.0, 0.0 );
            try {
                arrow.setFill(Color.valueOf(player.getColor()));
            } catch (Exception e) {
                arrow.setFill(Color.MEDIUMPURPLE);
            }

            arrow.setRotate((90*player.getHeading().ordinal())%360);
            this.getChildren().add(arrow);
        }
    }

    private void drawCheckpoint() {

        Obstacle obstacle = space.getObstacle();
        if (obstacle instanceof Checkpoint) {
            this.getChildren().removeIf(node -> node instanceof Circle);

            Circle checkpoint = new Circle(20.0);
            try {
                checkpoint.setFill(Color.valueOf(obstacle.getColor()));
            } catch (Exception e) {
                checkpoint.setFill(Color.MEDIUMPURPLE);
            }

            checkpoint.setRotate((90 * obstacle.getHeading().ordinal()) % 360);
            this.getChildren().add(checkpoint);
        }
    }

    private void drawConveyor() {

        Obstacle obstacle = space.getObstacle();
        if (obstacle instanceof  Conveyor) {
            this.getChildren().removeIf(node -> node instanceof Polygon);

            Polygon conveyor = new Polygon(0.0, 0.0,
                    20.0, 0.0,
                    20.0, 40.0,
                    0.0, 40.0);
            try {
                conveyor.setFill((Color.valueOf(obstacle.getColor())));
            } catch (Exception e) {
                conveyor.setFill(Color.MEDIUMPURPLE);
            }
            conveyor.setRotate((90 * obstacle.getHeading().ordinal()) % 360);
            this.getChildren().add(conveyor);

        }
    }

    private void drawGear() {

        Obstacle obstacle = space.getObstacle();
        if (obstacle instanceof Gear) {
            this.getChildren().removeIf(node -> node instanceof Polygon);

            Polygon gear = new Polygon(-15.0, 5.0,
                    -15.0, 5.0,
                    15.0, 10.0,
                    -15.0, 10.0);
            try {
                gear.setFill(((Color.valueOf(obstacle.getColor()))));
            } catch (Exception e) {
                gear.setFill(Color.MEDIUMPURPLE);
            }
            gear.setRotate((90 * obstacle.getHeading().ordinal()) % 360);
            this.getChildren().add(gear);
        }
    }

    private void drawWall() {

        Obstacle obstacle = space.getObstacle();
        if (obstacle instanceof  Wall) {
            this.getChildren().removeIf(node -> node instanceof Polygon);

            Polygon wall = new Polygon(-30.0, 0.0,
                    30.0, 0.0,
                    30.0, 10.0,
                    -30.0, 10.0);
            try {
                wall.setFill(((Color.valueOf(obstacle.getColor()))));
            } catch (Exception e) {
                wall.setFill(Color.BLACK);
            }
            switch (obstacle.getHeading()) {
                case NORTH -> {
                    wall.setTranslateY(-30);
                }
                case SOUTH -> {
                    wall.setTranslateY(30);
                }
                case EAST -> {
                    wall.setTranslateX(30);
                }
                case WEST -> {
                    wall.setTranslateX(-30);
                }
            }
            wall.setRotate((90*obstacle.getHeading().ordinal())%360);
            this.getChildren().add(wall);
        }
    }



    @Override
    public void updateView(Subject subject) {
        if (subject == this.space) {
            updatePlayer();
        }
    }

}
