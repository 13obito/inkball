package inkball;

import processing.core.PApplet;
import processing.core.PImage;

/**
 * Represents a wall in the Inkball game. A wall can have different colors and
 * sizes, and it interacts with balls and the game world.
 */
public class Wall {
    private int x, y, color;
    private int size = 32;
    private boolean loadImages;
    private PApplet app;
    private PImage[] wallImages;
    private PImage currentImage; 
    private PImage originalImage;

    /**
     * Constructs a Wall object with a specified position, color, and Processing app context.
     *
     * @param x     the X-coordinate of the wall's position.
     * @param y     the Y-coordinate of the wall's position.
     * @param color the color index of the wall.
     * @param app   the PApplet instance used for rendering the wall.
     * @param loadImages set to true if images should be loaded.
     */
    public Wall(int x, int y, int color, PApplet app, boolean loadImages) {
        this.x = x;
        this.y = y;
        this.color = color;
        this.loadImages = loadImages;
        this.app = app;

        if (loadImages) {
            loadImages();  // Load wall images
            this.originalImage = wallImages[color];  // Set the original image based on color
            this.currentImage = originalImage;  // Set the current image to the original image
        }
    }

    /**
     * Loads the wall images for different colors.
     * This method is used internally during initialization.
     */
    private void loadImages() {
        wallImages = new PImage[5];
        for (int i = 0; i < wallImages.length; i++) {
            wallImages[i] = ResourceImages.load(app, "wall" + i + ".png");
        }
    }

    /**
     * Sets a new image for the wall.
     *
     * @param newImage the new PImage to set for the wall.
     */
    public void setImage(PImage newImage) {
        this.currentImage = newImage;
    }

    /**
     * Resets the wall's image to its original state.
     */
    public void resetImage() {
        this.currentImage = originalImage;
    }

    /**
     * Checks whether the wall has a color. A wall is considered colored if its color index is not 0.
     *
     * @return true if the wall has a color, false otherwise.
     */
    public boolean hasColor() {
        return color != 0;
    }

    /**
     * Draws the wall at its current position using the current image.
     */
    public void draw() {
        app.image(currentImage, x, y, size, size);
    }

    /**
     * Returns the X-coordinate of the wall.
     *
     * @return the X-coordinate of the wall.
     */
    public int getX() {
        return x;
    }

    /**
     * Returns the Y-coordinate of the wall.
     *
     * @return the Y-coordinate of the wall.
     */
    public int getY() {
        return y;
    }

    /**
     * Returns the color index of the wall.
     *
     * @return the color index of the wall.
     */
    public int getColor() {
        return color;
    }

    /**
     * Returns the size of the wall, which is a square with equal width and height.
     *
     * @return the size of the wall (default is 32x32).
     */
    public int getSize() {
        return size;
    }
}
