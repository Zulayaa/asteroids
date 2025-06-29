package asteroids;

import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class AsteroidsApplication extends Application {

    public static int WIDTH = 1920;
    public static int HEIGHT = 1020;
    private int points;

    @Override
    public void start(Stage stage) throws Exception {
        //Creating a HashMap for uninterrupted, continuous movement
        Map<KeyCode, Boolean> pressedKeys = new HashMap<>();
        boolean[] canShoot = {true};

        //Creating the layout
        Pane pane = new Pane();
        pane.setPrefSize(WIDTH, HEIGHT);
        Text text = new Text(10, 20, "Points: 0");
        pane.getChildren().add(text);

        //Creating the ship, asteroids and projectiles
        Ship ship = new Ship(WIDTH / 2, HEIGHT / 2);
        List<Asteroid> asteroids = new ArrayList<>();
        List<Projectile> projectiles = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            Random rnd = new Random();
            Asteroid asteroid = new Asteroid(rnd.nextInt(3500), rnd.nextInt(900));
            asteroids.add(asteroid);
        }

        //Adding the ship and asteroid to the layout
        pane.getChildren().add(ship.getCharacter());
        asteroids.forEach(asteroid -> pane.getChildren().add(asteroid.getCharacter()));

        //Creating the window
        Scene scene = new Scene(pane);

        //Movement of the ship
        scene.setOnKeyPressed(event -> {
            pressedKeys.put(event.getCode(), Boolean.TRUE);
        });

        scene.setOnKeyReleased(event -> {
            pressedKeys.put(event.getCode(), Boolean.FALSE);
        });

        new AnimationTimer() {

            @Override
            public void handle(long now) {
                if (pressedKeys.getOrDefault(KeyCode.LEFT, false)) {
                    ship.turnLeft();
                }
                if (pressedKeys.getOrDefault(KeyCode.RIGHT, false)) {
                    ship.turnRight();
                }
                if (pressedKeys.getOrDefault(KeyCode.UP, false)) {
                    ship.accelerate();
                }
                if (pressedKeys.getOrDefault(KeyCode.SPACE, false) && projectiles.size() < 20) {
                    scene.setOnKeyPressed(event -> {
                        pressedKeys.put(event.getCode(), Boolean.TRUE);
                        if (event.getCode() == KeyCode.SPACE && canShoot[0] && projectiles.size() < 10) {
                            Projectile projectile = new Projectile((int) ship.getCharacter().getTranslateX(), (int) ship.getCharacter().getTranslateY());
                            projectile.getCharacter().setRotate(ship.getCharacter().getRotate());
                            projectiles.add(projectile);

                            projectile.accelerate();
                            projectile.setMovement(projectile.getMovement().normalize().multiply(3));

                            pane.getChildren().add(projectile.getCharacter());

                            canShoot[0] = false;
                        }
                    });

                    scene.setOnKeyReleased(event -> {
                        pressedKeys.put(event.getCode(), Boolean.FALSE);

                        if (event.getCode() == KeyCode.SPACE) {
                            canShoot[0] = true;
                        }
                    });

                }

                ship.move();
                asteroids.forEach(asteroid -> asteroid.move());
                projectiles.forEach(projectile -> projectile.move());

                asteroids.forEach(asteroid -> {
                    if (ship.collide(asteroid)) {
                        stop();
                    }
                });

                List<Projectile> projectilesToRemove = new ArrayList<>();

                for (Projectile projectile : projectiles) {
                    List<Asteroid> collisions = asteroids.stream()
                            .filter(asteroid -> asteroid.collide(projectile))
                            .collect(Collectors.toList());

                    if (!collisions.isEmpty()) {
                        for (Asteroid collided : collisions) {
                            asteroids.remove(collided);
                            pane.getChildren().remove(collided.getCharacter());
                        }

                        projectilesToRemove.add(projectile);

                        points++;
                        text.setText("Points: " + points);
                    }
                }

                projectilesToRemove.forEach(projectile -> {
                    pane.getChildren().remove(projectile.getCharacter());
                });
                projectiles.removeAll(projectilesToRemove);

                if (Math.random() < 0.005) {
                    Asteroid asteroid = new Asteroid(WIDTH, HEIGHT);
                    if (!asteroid.collide(ship)) {
                        asteroids.add(asteroid);
                        pane.getChildren().add(asteroid.getCharacter());
                    }
                }
            }
        }.start();

        //Naming and showing the window
        stage.setTitle("Asteroids!");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(AsteroidsApplication.class);
    }

    public static int partsCompleted() {
        // State how many parts you have completed using the return value of this method
        return 4;
    }

}
