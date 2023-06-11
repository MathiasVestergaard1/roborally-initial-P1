package dk.dtu.compute.se.pisd.roborally.view;

import dk.dtu.compute.se.pisd.designpatterns.observer.Subject;
import dk.dtu.compute.se.pisd.roborally.model.Space;
import dk.dtu.compute.se.pisd.roborally.model.obstacles.*;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import org.jetbrains.annotations.NotNull;
import javafx.scene.image.Image;

import java.awt.*;
import java.io.File;
import java.util.ArrayList;

public class ObstacleView extends SpaceView implements ViewObserver {

    public ObstacleView(@NotNull Space space) {
        super(space);

        // XXX the following styling should better be done with styles
        this.setPrefWidth(SPACE_WIDTH);
        this.setMinWidth(SPACE_WIDTH);
        this.setMaxWidth(SPACE_WIDTH);

        this.setPrefHeight(SPACE_HEIGHT);
        this.setMinHeight(SPACE_HEIGHT);
        this.setMaxHeight(SPACE_HEIGHT);
        ImageView backgroundImage = new ImageView();
        getChildren().add(backgroundImage);


        // This space view should listen to changes of the space
        space.attach(this);
        update(space);
    }

    private void updateObstacles() {
        getChildren().removeIf(node -> node instanceof ImageView);

        ArrayList<Obstacle> obstacles = space.getObstacle();
        for (Obstacle obstacle : obstacles) {
            if (obstacle instanceof Conveyor) {
                ImageView conveyor = new ImageView();
                try {
                    String imageFilePath = new File("images/conveyor.JPG").toURI().toString();
                    Image image = new Image(imageFilePath);
                    conveyor.setImage(image);
                    conveyor.setFitHeight(50);
                    conveyor.setFitWidth(50);

                } catch (Exception e) {
                }

                conveyor.setRotate((90*obstacle.getHeading().ordinal())%360);
                this.getChildren().add(conveyor);
            } else if (obstacle instanceof Wall) {
                ImageView wall = new ImageView();
                try {
                    String imageFilePath = new File("images/newWall.JPG").toURI().toString();
                    Image image = new Image(imageFilePath);
                    wall.setImage(image);
                    wall.setFitWidth(10);
                    wall.setFitHeight(60);
                } catch (Exception e) {
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
                wall.setRotate((90*obstacle.getHeading().ordinal())%360+90);
                this.getChildren().add(wall);
            } else if (obstacle instanceof Gear) {
                ImageView gear = new ImageView();
                try {
                    String imageFilePath = new File("images/gear.JPG").toURI().toString();
                    Image image = new Image(imageFilePath);
                    gear.setImage(image);
                    gear.setFitWidth(60);
                    gear.setFitHeight(60);
                } catch (Exception e) {
                    // Handle exception
                }
                this.getChildren().add(gear);
            }
            else if (obstacle instanceof Checkpoint) {
                ImageView checkpoint = new ImageView();
                try {
                    String imageFilePath = new File("images/checkpoint.JPG").toURI().toString();
                    Image image = new Image(imageFilePath);
                    checkpoint.setImage(image);
                    checkpoint.setFitWidth(40);
                    checkpoint.setFitHeight(40);

                } catch (Exception e) {
                    // Handle exception
                }
                this.getChildren().add(checkpoint);
            }
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

    @Override
    public void updateView(Subject subject) {
        if (subject == this.space) {
            updateBackgroundImage();
            updateObstacles();
        }
    }
}
