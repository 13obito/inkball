package inkball;

import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PVector;
import processing.data.JSONArray;
import processing.data.JSONObject;
import processing.event.KeyEvent;
import processing.event.MouseEvent;

import java.util.*;

public class App extends PApplet {

    public static final int CELLSIZE = 32; //8;
    public static final int CELLHEIGHT = 32;

    public static final int CELLAVG = 32;
    public static final int TOPBAR = 64;
    public static int WIDTH = 576; //CELLSIZE*BOARD_WIDTH;
    public static int HEIGHT = 640; //BOARD_HEIGHT*CELLSIZE+TOPBAR;
    public static final int BOARD_WIDTH = WIDTH/CELLSIZE;
    public static final int BOARD_HEIGHT = 20;

    public static final int INITIAL_PARACHUTES = 1;

    public static final int FPS = 30;

    public String configPath;

    public static Random random = new Random();
	
	// Feel free to add any additional methods or attributes you want. Please put classes in different files.

    private ArrayList<Line> lines;  // List to store lines (each line is a list of points)
    private ArrayList<PVector> currentLine;   
    private Level currentLevel;
    private boolean isPaused = false;
    private boolean isGameEnd = false;

    private int currentLevelIndex = 0;
    private JSONArray levelList;
    private JSONObject config;


    /**
     * Constructor initializes the App object with the default config.json path.
     */
    public App() {
        this.configPath = "config.json";
    }

    /**
     * Initializes the settings of the window size.
     */
    @Override
    public void settings() {
        size(WIDTH, HEIGHT);
    }

    /**
     * Load all resources such as images and initialize game elements like the level and player-drawn lines.
     */
    @Override
    public void setup() {
        frameRate(FPS);
        config = loadJSONObject(configPath);
        levelList = config.getJSONArray("levels");

        currentLevel = new Level(this, false);
        currentLevel.loadLevel(levelList.getJSONObject(currentLevelIndex), config);

        lines = new ArrayList<>();
        currentLine = null;
    }

    /**
     * Handles keyboard input when a key is pressed.
     * @param event The KeyEvent triggered by a key press.
     */
    @Override
    public void keyPressed(KeyEvent event) {
        if (isGameEnd) {
            if (key == 'r' || key == 'R') {
                resetGame();
            }
        } else if (!currentLevel.isTimeUp() && !currentLevel.isLevelEnd()) {
            if (event.getKey() == ' ') {
                isPaused = !isPaused;
            }
        }

        if (!currentLevel.isLevelEnd()) {
            if (key == 'r' || key == 'R') {
                currentLevel.reset();
                lines.clear();
            }
        }
    }

    /**
     * Placeholder method for handling key release events.
     */
    @Override
    public void keyReleased() {
    }

    /**
     * Handles mouse press events to create or remove lines.
     * Left click starts a new player-drawn line, and right click (or control + left click) deletes a line.
     * @param e The MouseEvent triggered by a mouse press.
     */
    @Override
    public void mousePressed(MouseEvent e) {
        if (e.getButton() == LEFT && !keyPressed) {
            currentLine = new ArrayList<>();
            currentLine.add(new PVector(e.getX(), e.getY() - TOPBAR));
        } else if (e.getButton() == RIGHT || (e.getButton() == LEFT && e.isControlDown())) {
            for (int i = 0; i < lines.size(); i++) {
                Line line = lines.get(i);
                if (line.isNearLine(e.getX(), e.getY() - TOPBAR)) {
                    lines.remove(i);
                    break;
                }
            }
        }
    }

    /**
     * Adds points to the current line when the mouse is dragged.
     * @param e The MouseEvent triggered by mouse dragging.
     */
    @Override
    public void mouseDragged(MouseEvent e) {
        if (currentLine != null) {
            currentLine.add(new PVector(e.getX(), e.getY() - TOPBAR));
        }
    }

    /**
     * Finalizes the line when the mouse is released.
     * @param e The MouseEvent triggered by releasing the mouse button.
     */
    @Override
    public void mouseReleased(MouseEvent e) {
        if (currentLine != null) {
            Line line = new Line(currentLine.get(0).x, currentLine.get(0).y, this);
            for (int i = 1; i < currentLine.size(); i++) {
                line.addPoint(currentLine.get(i).x, currentLine.get(i).y);
            }
            lines.add(line);
            currentLine = null;
        }
    }

    /**
     * Main game loop that handles drawing all game elements.
     */
    @Override
    public void draw() {
        background(200);
        drawTopBar();
        translate(0, TOPBAR);
        currentLevel.draw();
        drawLines();

        if (currentLevel.checkLevelEnd() && !isGameEnd) {
            currentLevel.endLevel();
        }

        if (currentLevel.isTimeUp()) {
            return;
        }

        if (isPaused || isGameEnd) {
            return;
        }

        updateGame();
    }

    /**
     * Updates the game state, checking for collisions between player-drawn lines and balls.
     */
    private void updateGame() {
        currentLevel.update();

        ArrayList<Line> linesHit = new ArrayList<>();
        for (Ball ball : currentLevel.getCurrentBalls()) {
            for (Line line : lines) {
                if (line.collideAndReflect(ball)) {
                    linesHit.add(line);
                    break;
                }
            }
        }
        lines.removeAll(linesHit);
    }

    /**
     * Draws all player-drawn lines.
     */
    private void drawLines() {
        stroke(0);
        strokeWeight(10);

        for (Line line : lines) {
            line.draw();
        }

        if (currentLine != null) {
            beginShape();
            noFill();
            for (PVector point : currentLine) {
                vertex(point.x, point.y);
            }
            endShape(OPEN);
        }

        strokeWeight(1);
        fill(255);
    }

    /**
     * Draws the top bar of the game, showing score, time, pause status, and the next ball preview.
     */
    private void drawTopBar() {
        fill(200);
        fill(0);
        textSize(20);

        String scoreText = "Score: " + currentLevel.getScore();
        String timeText;
        if (currentLevel.getTimeLimit() < 0) {
            timeText = "Time: --";
        } else if (currentLevel.isLevelEnd()) {
            timeText = "Time: " + currentLevel.getRemainingTime();
        } else {
            int tr = currentLevel.getTimeRemaining();
            timeText = "Time: " + (tr < 0 ? "--" : tr);
        }

        textAlign(RIGHT, CENTER);
        text(scoreText, WIDTH - 20, 20);
        text(timeText, WIDTH - 20, 50);
        textAlign(LEFT, BASELINE);

        if (isGameEnd) {
            fill(0);
            textSize(18);
            textAlign(CENTER, CENTER);
            text("=== ENDED ===", width / 2 + 50, TOPBAR / 2);
        }

        if (isPaused) {
            fill(0);
            textSize(18);
            textAlign(CENTER, CENTER);
            text("*** PAUSED ***", width / 2 + 50, TOPBAR / 2);
        }

        if (currentLevel.isTimeUp()) {
            fill(0);
            textSize(18);
            textAlign(CENTER, CENTER);
            text("=== TIME'S UP ===", width / 2 + 50, TOPBAR / 2);
        }

        drawNextBallPreview();
    }

    /**
     * Draws a preview of the next balls to be spawned.
     */
    private void drawNextBallPreview() {
        int previewX = 20;
        int previewY = 20;
        int boxSize = 30;
        int maxSlots = 5;

        fill(0);
        rect(previewX, previewY, boxSize * maxSlots, boxSize);

        ArrayList<Ball> ballList = currentLevel.getBallList();

        for (int i = 0; i < Math.min(ballList.size(), maxSlots); i++) {
            Ball ball = ballList.get(i);
            PImage ballImage = ResourceImages.load(this, "ball" + ball.getColor() + ".png");
            image(ballImage, previewX + 5 + (boxSize * i), previewY + 5, boxSize - 10, boxSize - 10);
        }

        fill(0);
        textSize(14);
        textAlign(LEFT);

        int countdownFrames = currentLevel.getSpawnCountdown();
        String countdownText = String.format("%.1f s", countdownFrames / (float) App.FPS);

        if (currentLevel.getBallList().size() > 0) {
            text(countdownText, previewX + (boxSize * maxSlots) + 10, previewY + 20);
        }
    }

    /**
     * Loads the next level in the game.
     * If there are no more levels, the game ends.
     */
    public void loadNextLevel() {
        currentLevelIndex++;
        lines.clear();

        if (currentLevelIndex < levelList.size()) {
            currentLevel.reset();
            JSONObject nextLevelData = levelList.getJSONObject(currentLevelIndex);
            currentLevel.loadLevel(nextLevelData, config);
        } else {
            isGameEnd = true;
        }
    }

    /**
     * Resets the game to the first level and resets the score and lines.
     */
    public void resetGame() {
        currentLevelIndex = 0;
        currentLevel.loadLevel(levelList.getJSONObject(currentLevelIndex), config);
        currentLevel.reset();
        currentLevel.setInitialScore(0);
        lines.clear();
        isGameEnd = false;
        isPaused = false;
    }

    /**
     * The main entry point to start the game.
     * @param args Command line arguments.
     */
    public static void main(String[] args) {
        PApplet.main("inkball.App");
    }
}
