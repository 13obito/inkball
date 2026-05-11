package inkball;

import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PVector;

/**
 * Represents a hole in the Inkball game. A hole can attract nearby balls and capture them.
 */
public class Hole {
    private int x, y, color;
    private int radius = 64;
    private PApplet app;
    private boolean loadImages;
    private PImage[] holeImages;

    /**
     * Constructs a Hole object with a given position, color, and app context.
     *
     * @param x     the X position of the hole.
     * @param y     the Y position of the hole.
     * @param color the color index of the hole.
     * @param app   the PApplet instance for rendering and processing.
     * @param loadImages set to true if images should be loaded.
     */
    public Hole(int x, int y, int color, PApplet app, boolean loadImages) {
        this.x = x;
        this.y = y;
        this.color = color;
        this.loadImages = loadImages;
        this.app = app;

        if (loadImages) {
            loadImages();  // Load the images for the ball (only if not in test mode)
        }
    }

    /**
     * Loads the images for the hole based on its color.
     * This method is used internally and should not be accessed externally.
     */
    private void loadImages() {
        holeImages = new PImage[5];
        for (int i = 0; i < 5; i++) {
            holeImages[i] = ResourceImages.load(app, "hole" + i + ".png");
        }
    }

    /**
     * Attracts a ball toward the hole if it is close enough. If the ball gets close enough, it is captured.
     *
     * @param ball the ball to attract towards the hole.
     */
    public void attractBall(Ball ball) {
        // Calculate the center of the hole and the ball
        float holeCenterX = x + radius / 2;
        float holeCenterY = y + radius / 2;
        float ballCenterX = ball.getX();
        float ballCenterY = ball.getY();

        // Calculate the distance between the ball and the hole
        float distance = PApplet.dist(holeCenterX, holeCenterY, ballCenterX, ballCenterY);

        // Attract the ball if it is close enough and not captured
        if (distance < 32 && !ball.isCaptured()) {
            float attractionFactor = 0.005f;
            PVector directionToHole = new PVector(holeCenterX - ballCenterX, holeCenterY - ballCenterY);
            directionToHole.mult(attractionFactor);

            // Adjust ball's velocity towards the hole
            ball.setVelocity(ball.getVelocityX() + directionToHole.x, ball.getVelocityY() + directionToHole.y);

            // Shrink the ball's radius as it approaches the hole
            float shrinkFactor = PApplet.map(distance, 12, 32, 0.5f, 1.0f);
            ball.setRadius(ball.getRadius() * shrinkFactor);

            // If the ball is very close, mark it as captured
            if (distance < 12) {
                ball.setCaptured(true);
            }
        }
        // If the ball is too far, reset its size (and capture flag if it drifted out of range)
        else if (distance >= 32) {
            ball.setRadius(12);
            ball.setCaptured(false);
        }
    }

    /**
     * Draws the hole on the screen.
     */
    public void draw() {
        app.image(holeImages[color], x, y, radius, radius);
    }

    /**
     * Gets the X position of the hole.
     *
     * @return the X position of the hole.
     */
    public int getX() {
        return x;
    }

    /**
     * Gets the Y position of the hole.
     *
     * @return the Y position of the hole.
     */
    public int getY() {
        return y;
    }

    /**
     * Gets the color of the hole.
     *
     * @return the color index of the hole.
     */
    public int getColor() {
        return color;
    }

    /**
     * Gets the radius of the hole.
     *
     * @return the radius of the hole.
     */
    public int getRadius() {
        return radius;
    }
}
