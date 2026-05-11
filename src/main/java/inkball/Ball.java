package inkball;

import processing.core.PApplet;
import processing.core.PImage;

/**
 * Represents a ball in the Inkball game.
 */
public class Ball {

    private int x, y, color, initialX, initialY, initialColor;
    private float radius;
    private float velocityX, velocityY;
    private boolean isCaptured;
    private boolean loadImages;
    private PImage[] ballImages;
    private PApplet app;

    /**
     * Constructs a Ball object with an initial position, color, and velocity.
     *
     * @param x     the initial x position of the ball.
     * @param y     the initial y position of the ball.
     * @param color the color index of the ball.
     * @param app   the PApplet instance for rendering and processing.
     * @param loadImages set to true if images should be loaded.
     */
    public Ball(int x, int y, int color, PApplet app, boolean loadImages) {
        this.x = x;
        this.y = y;
        this.color = color;
        this.initialX = x;
        this.initialY = y;
        this.initialColor = color;

        this.radius = 12;
        this.isCaptured = false;
        this.loadImages = loadImages;
        this.app = app;

        setRandomVelocity();  // Set random velocity for X and Y directions

        if (loadImages) {
            loadImages();  // Load the images for the ball (only if not in test mode)
        }
    }

    /**
     * Loads ball images into the ballImages array.
     */
    private void loadImages() {
        ballImages = new PImage[5];
        for (int i = 0; i < 5; i++) {
            ballImages[i] = ResourceImages.load(app, "ball" + i + ".png");
        }
    }

    /**
     * Moves the ball by adding its velocity to its position.
     * Reverses direction if it hits the boundaries of the app window.
     */
    public void move() {
        x += velocityX;
        y += velocityY;

        int boardW = App.WIDTH;
        int boardH = App.HEIGHT - App.TOPBAR;

        if (x - radius < 0 || x + radius > boardW) {
            velocityX *= -1;  // Reverse X velocity if ball hits left or right boundary
        }

        if (y - radius < 0 || y + radius > boardH) {
            velocityY *= -1;  // Reverse Y velocity if ball hits top or bottom boundary
        }
    }

    /**
     * Handles collision between the ball and a wall.
     * Reverses velocity and adjusts position if a collision is detected.
     *
     * @param wall the wall object to check for collision.
     */
    public void collide(Wall wall) {
        int nextX = x + (int) velocityX;
        int nextY = y + (int) velocityY;

        boolean willCollideHorizontally = nextX - radius < wall.getX() + wall.getSize() && nextX + radius > wall.getX();
        boolean willCollideVertically = nextY - radius < wall.getY() + wall.getSize() && nextY + radius > wall.getY();

        if (willCollideHorizontally && willCollideVertically) {
            // Handle horizontal bounce
            if (willCollideHorizontally && y + radius > wall.getY() && y - radius < wall.getY() + wall.getSize()) {
                velocityX *= -1;
                if (x < wall.getX()) {
                    x = wall.getX() - (int) radius;  // Move the ball to the left of the wall
                }
                else {
                    x = wall.getX() + wall.getSize() + (int) radius;  // Move the ball to the right of the wall
                }
            }

            // Handle vertical bounce
            if (willCollideVertically && x + radius > wall.getX() && x - radius < wall.getX() + wall.getSize()) {
                velocityY *= -1;
                if (y < wall.getY()) {
                    y = wall.getY() - (int) radius;  // Move the ball above the wall
                }
                else {
                    y = wall.getY() + wall.getSize() + (int) radius;  // Move the ball below the wall
                }
            }

            // Change ball color if the wall has color
            if (wall.hasColor()) {
                this.color = wall.getColor();
            }
        }
    }

    /**
     * Sets the velocity of the ball.
     *
     * @param velocityX the horizontal velocity to set.
     * @param velocityY the vertical velocity to set.
     */
    public void setVelocity(float velocityX, float velocityY) {
        this.velocityX = velocityX;
        this.velocityY = velocityY;
    }

    /**
     * Sets the radius of the ball.
     *
     * @param r the radius to set.
     */
    public void setRadius(float r) {
        this.radius = r;
    }

    /**
     * Sets the position of the ball.
     *
     * @param x the x position to set.
     * @param y the y position to set.
     */
    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Gradually resets the radius of the ball back to 16.
     */
    public void resetRadius() {
        if (radius < 16) {
            radius += 0.5;
        }
        if (radius > 16) {
            radius = 16;
        }
    }

    /**
     * Resets the ball to its initial position, color, and velocity.
     */
    public void reset() {
        setPosition(initialX, initialY);
        setCaptured(false);
        setRandomVelocity();
        this.color = initialColor;
    }

    /**
     * Resets the ball to its initial position and velocity after wrong capture.
     */
    public void resetAfterWrongCaptured() {
        setPosition(initialX, initialY);
        setCaptured(false);
        setRandomVelocity();
    }

    /**
     * Randomly sets the velocity of the ball.
     */
    public void setRandomVelocity() {
        if (app.random(1) > 0.5) {
            this.velocityX = 2;
        } else {
            this.velocityX = -2;
        }

        if (app.random(1) > 0.5) {
            this.velocityY = 2;
        } else {
            this.velocityY = -2;
        }
    }

    /**
     * Sets the captured status of the ball.
     *
     * @param status the captured status to set.
     */
    public void setCaptured(boolean status) {
        this.isCaptured = status;
    }

    /**
     * Draws the ball on the screen using its color.
     */
    public void draw() {
        app.image(ballImages[color], x - radius, y - radius, radius * 2, radius * 2);
    }

    /**
     * Gets the X position of the ball.
     *
     * @return the X position of the ball.
     */
    public int getX() {
        return x;
    }

    /**
     * Gets the Y position of the ball.
     *
     * @return the Y position of the ball.
     */
    public int getY() {
        return y;
    }

    /**
     * Gets the radius of the ball.
     *
     * @return the radius of the ball.
     */
    public float getRadius() {
        return radius;
    }

    /**
     * Gets the color of the ball.
     *
     * @return the color index of the ball.
     */
    public int getColor() {
        return color;
    }

    /**
     * Gets the horizontal velocity of the ball.
     *
     * @return the horizontal velocity of the ball.
     */
    public float getVelocityX() {
        return velocityX;
    }

    /**
     * Gets the vertical velocity of the ball.
     *
     * @return the vertical velocity of the ball.
     */
    public float getVelocityY() {
        return velocityY;
    }

    /**
     * Checks if the ball is captured.
     *
     * @return true if the ball is captured, false otherwise.
     */
    public boolean isCaptured() {
        return isCaptured;
    }
}
