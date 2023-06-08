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
        Obstacle obstacle = space.getObstacle();
        if (obstacle instanceof Conveyor) {
            Polygon conveyor = new Polygon(0.0, 0.0,
                    20.0, 0.0,
                    20.0, 40.0,
                    0.0, 40.0);
            try {
                conveyor.setFill(Color.valueOf(obstacle.getColor()));
            } catch (Exception e) {
                conveyor.setFill(Color.MEDIUMPURPLE);
            }

            conveyor.setRotate((90*obstacle.getHeading().ordinal())%360);
            this.getChildren().add(conveyor);
        } else if (obstacle instanceof Wall) {
            Polygon wall = new Polygon(-30.0, 0.0,
                    30.0, 0.0,
                    30.0, 10.0,
                    -30.0, 10.0);
            try {
                wall.setFill(Color.valueOf(obstacle.getColor()));
            } catch (Exception e) {
                wall.setFill(Color.MEDIUMPURPLE);
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
        } else if (obstacle instanceof Gear) {
            Polygon gear = new Polygon(-30.0, 0.0,
                    30.0, 0.0,
                    30.0, 10.0,
                    -30.0, 10.0);
            try {
                gear.setFill(Color.valueOf(obstacle.getColor()));
            } catch (Exception e) {
                gear.setFill(Color.MEDIUMPURPLE);
            }
            this.getChildren().add(gear);
        }
        else if (obstacle instanceof Checkpoint){
            Circle checkpoint = new Circle(20);
            try {
                checkpoint.setFill(Color.valueOf(obstacle.getColor()));
            }catch (Exception e){
                checkpoint.setFill(Color.MEDIUMPURPLE);
            }
            this.getChildren().add(checkpoint);
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
