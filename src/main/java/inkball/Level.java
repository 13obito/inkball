package inkball;

import processing.core.PApplet;
import processing.core.PImage;
import processing.data.JSONArray;
import processing.data.JSONObject;
import java.util.*;

/**
 * The Level class represents a game level in the Inkball game. 
 * It manages the walls, balls, holes, spawners, and game logic such as scoring and ball spawning.
 */
public class Level {
    
    private ArrayList<Wall> wallList;
    private ArrayList<Ball> ballList;
    private ArrayList<Ball> currentBalls;
    private ArrayList<Ball> initialBalls;
    private ArrayList<Ball> initialCurrentBalls;
    private ArrayList<Hole> holeList;
    private ArrayList<int[]> spawnerList;

    private PApplet app;
    private PImage tileImage;
    private PImage spawnerImage;
    private int timer;
    private int timeLimit;
    private int timeRemaining;
    private int spawnInterval;
    private int initialScore;
    private int score;

    private boolean isTimeUp = false;
    private boolean isLevelEnd = false;
    private boolean isTesting = false;

    private JSONObject scoreIncreaseFromHoleCapture;
    private JSONObject scoreDecreaseFromWrongHole;
    private float scoreIncreaseModifier;
    private float scoreDecreaseModifier;

    private int frameCounter = 0;
    private int yellowTileCounter = 0;
    /** Frames remaining until the next ball spawns from the queue. */
    private int spawnCountdown = 0;

    private Random random = new Random();

    /**
     * Constructs a new Level object.
     *
     * @param app the PApplet instance used to render the game.
     */
    public Level(PApplet app, boolean isTesting) {
        this.wallList = new ArrayList<>();
        this.holeList = new ArrayList<>();
        this.ballList = new ArrayList<>();
        this.currentBalls = new ArrayList<>();
        this.initialBalls = new ArrayList<>();
        this.initialCurrentBalls = new ArrayList<>();
        this.spawnerList = new ArrayList<>();
        this.app = app;
        this.isTesting = isTesting;
        this.timer = 0;
        this.timeRemaining = 0;
        this.score = 0;
        this.initialScore = 0;

        if(!isTesting) {
            tileImage = ResourceImages.load(app, "tile.png");
            spawnerImage = ResourceImages.load(app, "entrypoint.png");
        }

    }


    /**
     * Loads the level configuration from the provided JSON data.
     * Clears any existing level elements and sets up new ones such as walls, holes, balls, and spawners.
     *
     * @param levelData The JSON object containing the layout and ball information for the level.
     * @param config The JSON object containing configuration for scoring and other modifiers.
     */
    public void loadLevel(JSONObject levelData, JSONObject config) {
        // Clear existing lists to prepare for new level data
        wallList.clear();
        holeList.clear();
        ballList.clear();
        currentBalls.clear();
        initialBalls.clear();
        initialCurrentBalls.clear();
        spawnerList.clear();

        isTimeUp = false;
        isLevelEnd = false;

        // Reset score and time settings for the level
        this.score = initialScore;
        this.timeLimit = parseTimeLimitSeconds(levelData);
        int spawnSeconds = levelData.getInt("spawn_interval");
        this.spawnInterval = Math.max(1, spawnSeconds * App.FPS);
        this.spawnCountdown = this.spawnInterval;
        JSONArray ballsArray = levelData.getJSONArray("balls");

        // Load score modifiers from the config
        this.scoreIncreaseModifier = (float) levelData.getDouble("score_increase_from_hole_capture_modifier");
        this.scoreDecreaseModifier = (float) levelData.getDouble("score_decrease_from_wrong_hole_modifier");

        // Load score increment and decrement settings from the config
        this.scoreIncreaseFromHoleCapture = config.getJSONObject("score_increase_from_hole_capture");
        this.scoreDecreaseFromWrongHole = config.getJSONObject("score_decrease_from_wrong_hole");

        // Load ball information and initialize the ball list
        for (int i = 0; i < ballsArray.size(); i++) {
            String ballColorString = ballsArray.getString(i);
            int ballColor = getColorFromString(ballColorString);
            Ball newBall = new Ball(0, 0, ballColor, app, !isTesting);
            ballList.add(newBall); // Add to the list of balls to spawn
            initialBalls.add(newBall); // Store as part of the initial set of balls
        }

        // Load the layout of walls, spawners, and holes from the level's layout file
        String layoutFile = levelData.getString("layout");
        String[] lines = app.loadStrings(layoutFile);

        // Loop through each character in the layout and add walls, spawners, and holes as specified
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            for (int j = 0; j < line.length(); j++) {
                char type = line.charAt(j);
                int x = j * 32;
                int y = i * 32;

                // Add walls of various types
                if (type == 'X') {
                    this.wallList.add(new Wall(x, y, 0, app, !isTesting));
                } else if (type == '1' || type == '2' || type == '3' || type == '4') {
                    this.wallList.add(new Wall(x, y, Integer.parseInt(String.valueOf(type)), app, !isTesting));
                } 
                // Add spawners
                else if (type == 'S') {
                    this.spawnerList.add(new int[]{x, y});
                } 
                // Add holes
                else if (type == 'H') {
                    int holeColor = Character.getNumericValue(line.charAt(j + 1));
                    this.holeList.add(new Hole(x, y, holeColor, app, !isTesting));
                    j++; // Skip next character (hole color) as it's already processed
                } 
                // Add balls that are initially on the field
                else if (type == 'B') {
                    int ballColor = Character.getNumericValue(line.charAt(j + 1));
                    Ball newBall = new Ball(x, y, ballColor, app, !isTesting);
                    this.currentBalls.add(newBall); // Add to the list of currently active balls
                    this.initialCurrentBalls.add(newBall); // Store as part of the initial set of active balls
                    j++; // Skip next character (ball color) as it's already processed
                }
            }
        }
        this.timeRemaining = 0;
    }

    /**
     * Parses level time in seconds. Missing, negative, or non-integer values disable the level timer.
     */
    private int parseTimeLimitSeconds(JSONObject levelData) {
        try {
            Object raw = levelData.get("time");
            if (raw == null) {
                return -1;
            }
            int t;
            if (raw instanceof Integer) {
                t = (Integer) raw;
            } else if (raw instanceof Long) {
                t = ((Long) raw).intValue();
            } else if (raw instanceof Double) {
                t = ((Double) raw).intValue();
            } else if (raw instanceof String) {
                t = Integer.parseInt((String) raw);
            } else {
                t = levelData.getInt("time");
            }
            if (t < 0) {
                return -1;
            }
            return t;
        } catch (Exception e) {
            return -1;
        }
    }

    /**
     * Returns the remaining time for the level.
      *
      * @return the remaining time in the level.
      */
    public int getTimeRemaining() {
        if (timeLimit < 0) {
            return -1;
        }
        int timeElapsed = timer / App.FPS;
        int timeRemaining = timeLimit - timeElapsed;
        if (timeRemaining < 0) {
            timeRemaining = 0;
        }
        if (timeRemaining == 0 && timeLimit >= 0) {
            isTimeUp = true;
        }

        return timeRemaining;
    }

    /**
     * Draws the background grid of the level.
     */
    public void drawBackgroundGrid() {
        for (int i = 0; i < 18; i++) {
            for (int j = 0; j < 18; j++) {
                int x = j * 32;
                int y = i * 32;
                app.image(tileImage, x, y, 32, 32);
            }
        }

        for (int[] spawner : spawnerList) {
            drawSpawner(spawner[0], spawner[1]);
        }
    }

    /**
     * Draws the spawner at the given coordinates.
     *
     * @param x the X coordinate of the spawner.
     * @param y the Y coordinate of the spawner.
     */
    private void drawSpawner(int x, int y) {
        app.image(spawnerImage, x, y, App.CELLSIZE, App.CELLSIZE);
    }

    /**
     * Draws all elements (walls, holes, balls) in the level.
     */
    public void draw() {
        drawBackgroundGrid();
        for (Wall wall : wallList) {
            wall.draw();
        }
        for (Hole hole : holeList) {
            hole.draw();
        }
        for (Ball ball : currentBalls) {
            ball.draw();
        }
    }

    /**
     * Updates the state of the game for each frame.
     * Moves balls, checks for collisions with walls and holes, and handles ball captures.
     * Spawns new balls if needed.
     */
    public void update() {
        // Increment the timer for this frame
        timer++;

        // Iterate over the list of currently active balls
        Iterator<Ball> iterator = currentBalls.iterator();

        while (iterator.hasNext()) {
            Ball ball = iterator.next();
            ball.move(); // Move the ball according to its velocity

            // Check for collisions between the ball and any walls
            for (Wall wall : wallList) {
                ball.collide(wall); // Handle collision with the wall
            }

            Hole attractor = findAttractingHole(ball);
            if (attractor != null) {
                attractor.attractBall(ball);
            }

            // If the ball is captured by a hole
            if (ball.isCaptured()) {
                Hole capHole = findHoleForCapturedBall(ball);
                if (capHole == null) {
                    continue;
                }
                if (ball.getColor() == capHole.getColor() || ball.getColor() == 0 || capHole.getColor() == 0) {
                    increaseScore(getScoreIncrease(ball.getColor()));
                    iterator.remove();
                } else {
                    decreaseScore(getScoreDecrease(ball.getColor()));
                    ball.resetAfterWrongCaptured();
                    ballList.add(ball);
                    iterator.remove();
                }
            }
        }

        // Spawn new balls if needed
        spawnBall();
    }

    /**
     * Spawns a new ball at a spawner location.
     */
    private void spawnBall() {
        if (ballList.isEmpty() || spawnerList.isEmpty()) {
            return;
        }

        spawnCountdown--;
        if (spawnCountdown > 0) {
            return;
        }

        Ball ballToSpawn = ballList.remove(0);
        int[] spawnerCoords = spawnerList.get(random.nextInt(spawnerList.size()));
        int ballX = spawnerCoords[0] + App.CELLSIZE / 2;
        int ballY = spawnerCoords[1] + App.CELLSIZE / 2;
        ballToSpawn.setPosition(ballX, ballY);
        currentBalls.add(ballToSpawn);
        spawnCountdown = spawnInterval;
    }

    /**
     * Hole whose centre is within 32px that should attract the ball this frame (closest such hole).
     */
    private Hole findAttractingHole(Ball ball) {
        Hole best = null;
        float bestDistance = Float.MAX_VALUE;

        for (Hole hole : holeList) {
            float cx = hole.getX() + hole.getRadius() / 2f;
            float cy = hole.getY() + hole.getRadius() / 2f;
            float distance = PApplet.dist(ball.getX(), ball.getY(), cx, cy);
            if (distance < 32 && distance < bestDistance) {
                bestDistance = distance;
                best = hole;
            }
        }
        return best;
    }

    /**
     * Hole that captured the ball (centre within capture distance).
     */
    private Hole findHoleForCapturedBall(Ball ball) {
        for (Hole hole : holeList) {
            float cx = hole.getX() + hole.getRadius() / 2f;
            float cy = hole.getY() + hole.getRadius() / 2f;
            if (PApplet.dist(ball.getX(), ball.getY(), cx, cy) < 12) {
                return hole;
            }
        }
        return findClosestHoleByCentre(ball);
    }

    private Hole findClosestHoleByCentre(Ball ball) {
        Hole closestHole = null;
        float closestDistance = Float.MAX_VALUE;

        for (Hole hole : holeList) {
            float cx = hole.getX() + hole.getRadius() / 2f;
            float cy = hole.getY() + hole.getRadius() / 2f;
            float distance = PApplet.dist(ball.getX(), ball.getY(), cx, cy);
            if (distance < closestDistance) {
                closestDistance = distance;
                closestHole = hole;
            }
        }
        return closestHole;
    }

    /**
     * Converts a color code to a string.
     *
     * @param color the color code.
     * @return the string representation of the color.
     */
    public String getColorString(int color) {
        switch (color) {
            case 0:
                return "grey";
            case 1:
                return "orange";
            case 2:
                return "blue";
            case 3:
                return "green";
            case 4:
                return "yellow";
            default:
                return "grey";
        }
    }

    /**
     * Converts a color string to a color code.
     *
     * @param colorName the color name.
     * @return the color code.
     */
    public int getColorFromString(String colorName) {
        switch (colorName.toLowerCase()) {
            case "grey":
                return 0;
            case "orange":
                return 1;
            case "blue":
                return 2;
            case "green":
                return 3;
            case "yellow":
                return 4;
            default:
                return 0;
        }
    }

    /**
     * Gets the score increase based on the ball's color.
     *
     * @param color the color of the ball.
     * @return the score increase.
     */
    public int getScoreIncrease(int color) {
        String colorName = getColorString(color);
        int baseScore = scoreIncreaseFromHoleCapture.getInt(colorName);
        return (int) (baseScore * scoreIncreaseModifier);
    }

    /**
     * Gets the score decrease based on the ball's color.
     *
     * @param color the color of the ball.
     * @return the score decrease.
     */
    public int getScoreDecrease(int color) {
        String colorName = getColorString(color);
        int baseScore = scoreDecreaseFromWrongHole.getInt(colorName);
        return (int) (baseScore * scoreDecreaseModifier);
    }

    /**
     * Increases the score by a given amount.
     *
     * @param amount the amount to increase the score.
     */
    public void increaseScore(int amount) {
        this.score += amount;
    }

    /**
     * Decreases the score by a given amount.
     *
     * @param amount the amount to decrease the score.
     */
    public void decreaseScore(int amount) {
        this.score -= amount;
        if (this.score < 0) {
            this.score = 0;
        }
    }

    /**
     * Resets the level to its initial state.
     */
    public void reset() {
        ballList.clear();
        ballList.addAll(initialBalls);
        currentBalls.clear();
        currentBalls.addAll(initialCurrentBalls);
        setScore(initialScore);
        timer = 0;
        isTimeUp = false;
        isLevelEnd = false;
        yellowTileCounter = 0;
        frameCounter = 0;
        spawnCountdown = spawnInterval;
        for (Ball ball : initialCurrentBalls) {
            ball.reset();
        }
    }

    /**
     * Checks if the level has ended.
     *
     * @return true if the level has ended, false otherwise.
     */
    public boolean checkLevelEnd() {
        // Check if there are no more balls in the level
        if (ballList.isEmpty() && currentBalls.isEmpty()) {
            isLevelEnd = true;
            initialScore = score;
            return true;
        }
        return false;
    }

    /**
     * Ends the level and displays animations for the walls. 
     * When the level ends, walls light up in sequence to indicate level completion.
     * If the time runs out, the next level is loaded.
     */
    public void endLevel() {
        // Check if this is the first time ending the level (to avoid resetting the timeRemaining on each frame)
        if (timeRemaining == 0) {
            if (timeLimit < 0) {
                timeRemaining = 0;
            } else {
                timeRemaining = getTimeRemaining();
            }
        }

        // Lists to store the walls that form the edges of the board
        ArrayList<Wall> edgeWallsUpperLeft = new ArrayList<>();
        ArrayList<Wall> edgeWallsBottomRight = new ArrayList<>();
        int boardWidth = 17;
        int boardHeight = 17;

        // Add the upper-left walls (forming the left and top borders)
        for (int x = 0; x <= boardWidth; x++) {
            edgeWallsUpperLeft.add(findWallByCoords(x, 0));  // Top border walls
        }

        for (int y = 1; y <= boardHeight; y++) {
            edgeWallsUpperLeft.add(findWallByCoords(boardWidth, y));  // Right border walls
        }

        for (int x = boardWidth - 1; x >= 0; x--) {
            edgeWallsUpperLeft.add(findWallByCoords(x, boardHeight));  // Bottom border walls
        }

        for (int y = boardHeight - 1; y > 0; y--) {
            edgeWallsUpperLeft.add(findWallByCoords(0, y));  // Left border walls
        }

        // Add the bottom-right walls (forming the bottom and right borders)
        for (int x = boardWidth; x >= 0; x--) {
            edgeWallsBottomRight.add(findWallByCoords(x, boardHeight));  // Bottom border walls
        }

        for (int y = boardHeight - 1; y >= 0; y--) {
            edgeWallsBottomRight.add(findWallByCoords(0, y));  // Left border walls
        }

        for (int x = 1; x <= boardWidth; x++) {
            edgeWallsBottomRight.add(findWallByCoords(x, 0));  // Top border walls
        }

        for (int y = 1; y < boardHeight; y++) {
            edgeWallsBottomRight.add(findWallByCoords(boardWidth, y));  // Right border walls
        }

        // If the level has ended and time is remaining, animate the walls
        if (isLevelEnd && timeRemaining > 0) {
            frameCounter++;  // Increase frame counter to control animation speed

            // Only update the animation every 2 frames
            if (frameCounter % 2 == 0) {
                // Loop the yellowTileCounter to prevent index out of bounds
                if (yellowTileCounter >= edgeWallsUpperLeft.size()) {
                    yellowTileCounter = 0;
                }

                // Get the current walls to light up (yellow) from both upper-left and bottom-right corners
                Wall currentUpperLeftWall = edgeWallsUpperLeft.get(yellowTileCounter);
                Wall currentBottomRightWall = edgeWallsBottomRight.get(yellowTileCounter);

                // Load the yellow wall image and set it to the current walls
                PImage yellowWallImage = ResourceImages.load(app, "wall4.png");
                if (currentUpperLeftWall != null) {
                    currentUpperLeftWall.setImage(yellowWallImage);
                }
                if (currentBottomRightWall != null) {
                    currentBottomRightWall.setImage(yellowWallImage);
                }

                // Reset the image of the previous walls back to their original state
                if (yellowTileCounter > 0) {
                    Wall previousUpperLeftWall = edgeWallsUpperLeft.get(yellowTileCounter - 1);
                    Wall previousBottomRightWall = edgeWallsBottomRight.get(yellowTileCounter - 1);
                    if (previousUpperLeftWall != null) {
                        previousUpperLeftWall.resetImage();
                    }
                    if (previousBottomRightWall != null) {
                        previousBottomRightWall.resetImage();
                    }
                } 
                else {
                    Wall lastUpperLeftWall = edgeWallsUpperLeft.get(edgeWallsUpperLeft.size() - 1);
                    Wall lastBottomRightWall = edgeWallsBottomRight.get(edgeWallsBottomRight.size() - 1);
                    if (lastUpperLeftWall != null) {
                        lastUpperLeftWall.resetImage();
                    }
                    if (lastBottomRightWall != null) {
                        lastBottomRightWall.resetImage();
                    }
                }

                // Increase the score and decrement the remaining time for animation
                this.score += 1;
                timeRemaining -= 1;
                yellowTileCounter++;  // Move to the next tile in the sequence
            }
        }

        // When the time runs out, load the next level
        if (timeRemaining <= 0) {
            ((App) app).loadNextLevel();
        }
    }

    /**
     * Finds a wall by its coordinates.
     *
     * @param x the X coordinate.
     * @param y the Y coordinate.
     * @return the Wall object at the given coordinates, or null if none exists.
     */
    private Wall findWallByCoords(int x, int y) {
        for (Wall wall : wallList) {
            if (wall.getX() == x * App.CELLSIZE && wall.getY() == y * App.CELLSIZE) {
                return wall;
            }
        }
        return null;
    }

    /**
     * Gets the list of balls in the level.
     *
     * @return the list of balls.
     */
    public ArrayList<Ball> getBallList() {
        return ballList;
    }

    /**
     * Gets the list of currently active balls.
     *
     * @return the list of currently active balls.
     */
    public ArrayList<Ball> getCurrentBalls() {
        return currentBalls;
    }

    /**
     * Sets the current score.
     *
     * @param score the score to set.
     */
    public void setScore(int score) {
        this.score = score;
    }

    /**
     * Gets the current score.
     *
     * @return the current score.
     */
    public int getScore() {
        return score;
    }

    /**
     * Sets the initial score for the level.
     *
     * @param initialScore the initial score.
     */
    public void setInitialScore(int initialScore) {
        this.initialScore = initialScore;
    }

    /**
     * Gets the initial score for the level.
     *
     * @return the initial score.
     */
    public int getInitialScore() {
        return initialScore;
    }

    /**
     * Gets the time limit for the level.
     *
     * @return the time limit.
     */
    public int getTimeLimit() {
        return timeLimit;
    }

    /**
     * Gets the current timer value.
     *
     * @return the timer value.
     */
    public int getTimer() {
        return timer;
    }

    /**
     * Gets the remaining time in the level.
     *
     * @return the remaining time.
     */
    public int getRemainingTime() {
        return timeRemaining;
    }

    /**
     * Gets the spawn interval for new balls.
     *
     * @return the spawn interval in frames.
     */
    public int getSpawnInterval() {
        return spawnInterval;
    }

    /**
     * Frames until the next queued ball spawns (for UI).
     */
    public int getSpawnCountdown() {
        return spawnCountdown;
    }

    /**
     * Checks if the time is up in the level.
     *
     * @return true if the time is up, false otherwise.
     */
    public boolean isTimeUp() {
        return isTimeUp;
    }

    /**
     * Checks if the level has ended.
     *
     * @return true if the level has ended, false otherwise.
     */
    public boolean isLevelEnd() {
        return isLevelEnd;
    }
}
   
