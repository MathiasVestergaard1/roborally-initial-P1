package dk.dtu.compute.se.pisd.roborally.view;

import dk.dtu.compute.se.pisd.designpatterns.observer.Subject;
import dk.dtu.compute.se.pisd.roborally.model.Space;
import dk.dtu.compute.se.pisd.roborally.model.obstacles.Conveyor;
import dk.dtu.compute.se.pisd.roborally.model.obstacles.Obstacle;
import dk.dtu.compute.se.pisd.roborally.model.obstacles.Wall;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import org.jetbrains.annotations.NotNull;

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

        if ((space.x + space.y) % 2 == 0) {
            this.setStyle("-fx-background-color: white;");
        } else {
            this.setStyle("-fx-background-color: black;");
        }

        // updatePlayer();

        // This space view should listen to changes of the space
        space.attach(this);
        update(space);
    }

    private void updateObstacles() {
        //This is temporary, it should not be an arrow
        Obstacle obstacle = space.getObstacle();
        if (obstacle instanceof Conveyor) {
            Polygon arrow = new Polygon(0.0, 0.0,
                    20.0, 0.0,
                    20.0, 40.0,
                    0.0, 40.0);
            try {
                arrow.setFill(Color.valueOf(obstacle.getColor()));
            } catch (Exception e) {
                arrow.setFill(Color.MEDIUMPURPLE);
            }

            arrow.setRotate((90*obstacle.getHeading().ordinal())%360);
            this.getChildren().add(arrow);
        } else if (obstacle instanceof Wall) {
            Polygon arrow = new Polygon(-30.0, 0.0,
                    30.0, 0.0,
                    30.0, 10.0,
                    -30.0, 10.0);
            try {
                arrow.setFill(Color.valueOf(obstacle.getColor()));
            } catch (Exception e) {
                arrow.setFill(Color.MEDIUMPURPLE);
            }
            arrow.setRotate((90*obstacle.getHeading().ordinal())%360);
            this.getChildren().add(arrow);
        }

    }

    @Override
    public void updateView(Subject subject) {
        if (subject == this.space) {
            updateObstacles();
        }
    }
}
