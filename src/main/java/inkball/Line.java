package inkball;

import java.util.*;
import processing.core.PApplet;
import processing.core.PVector;

/**
 * Represents a line that can be drawn in the Inkball game. A line consists of multiple points
 * connected together, and can detect collisions with balls.
 */
public class Line {

    private ArrayList<PVector> points;
    private PApplet app;

    /**
     * Constructs a Line object starting at the specified coordinates.
     *
     * @param startX the x-coordinate of the starting point.
     * @param startY the y-coordinate of the starting point.
     * @param app    the PApplet instance used for rendering.
     */
    public Line(float startX, float startY, PApplet app) {
        this.points = new ArrayList<>();
        this.app = app;
        points.add(new PVector(startX, startY));  // Add starting point
    }

    /**
     * Adds a new point to the line at the specified coordinates.
     *
     * @param x the x-coordinate of the new point.
     * @param y the y-coordinate of the new point.
     */
    public void addPoint(float x, float y) {
        points.add(new PVector(x, y));  // Add the new point to the points list
    }

    /**
     * Draws the line by connecting consecutive points.
     */
    public void draw() {
        app.stroke(0);
        app.strokeWeight(10);
        
        // Draw a line between each consecutive pair of points
        for (int i = 0; i < points.size() - 1; i++) {
            PVector p1 = points.get(i);
            PVector p2 = points.get(i + 1);
            app.line(p1.x, p1.y, p2.x, p2.y);
        }
    }

    /**
     * Checks if the ball collides with a line segment between two points.
     *
     * @param ball the ball to check for collision.
     * @param p1   the first point of the line segment.
     * @param p2   the second point of the line segment.
     * @return true if a collision is detected, false otherwise.
     */
    public boolean checkCollide(Ball ball, PVector p1, PVector p2) {
        PVector nextBallPos = new PVector(ball.getX() + ball.getVelocityX(), ball.getY() + ball.getVelocityY());
        float disP1ToBall = PVector.dist(p1, nextBallPos);
        float disP2ToBall = PVector.dist(p2, nextBallPos);
        float disP1ToP2 = PVector.dist(p1, p2);

        return disP1ToBall + disP2ToBall < disP1ToP2 + ball.getRadius();
    }

    /**
     * Returns the normal vector (perpendicular vector) of the line segment at the collision point.
     *
     * @param ball the ball involved in the collision.
     * @param p1   the first point of the line segment.
     * @param p2   the second point of the line segment.
     * @return the normal vector to the line segment.
     */
    public PVector getNPVector(Ball ball, PVector p1, PVector p2) {
        float dx = p2.x - p1.x;
        float dy = p2.y - p1.y;

        PVector n1 = new PVector(-dy, dx);
        PVector n2 = new PVector(dy, -dx);

        n1.normalize();
        n2.normalize();

        PVector ballPos = new PVector(ball.getX(), ball.getY());
        PVector midPoint = new PVector((p1.x + p2.x) / 2, (p1.y + p2.y) / 2);
        float disP1ToBall = PVector.dist(PVector.add(midPoint, n1), ballPos);
        float disP2ToBall = PVector.dist(PVector.add(midPoint, n2), ballPos);

        return (disP1ToBall < disP2ToBall) ? n1 : n2;
    }

    /**
     * Reflects the ball's velocity based on the normal vector of the line.
     *
     * @param ball the ball to reflect.
     * @param n    the normal vector of the line.
     */
    public void reflectBall(Ball ball, PVector n) {
        PVector v = new PVector(ball.getVelocityX(), ball.getVelocityY());

        PVector newVel = PVector.sub(v, PVector.mult(n, 2 * PVector.dot(v, n)));
        float originalSpeed = v.mag();
        newVel.normalize();
        newVel.mult(originalSpeed);
        ball.setVelocity(newVel.x, newVel.y);
    }

    /**
     * Checks if the ball collides with any part of the line and reflects it if a collision occurs.
     *
     * @param ball the ball to check for collision.
     * @return true if a collision occurs, false otherwise.
     */
    public boolean collideAndReflect(Ball ball) {
        for (int i = 0; i < points.size() - 1; i++) {
            PVector p1 = points.get(i);
            PVector p2 = points.get(i + 1);

            if (checkCollide(ball, p1, p2)) {
                PVector n = getNPVector(ball, p1, p2);
                reflectBall(ball, n);
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the mouse is near any part of the line.
     *
     * @param mouseX the x-coordinate of the mouse.
     * @param mouseY the y-coordinate of the mouse.
     * @return true if the mouse is near the line, false otherwise.
     */
    public boolean isNearLine(float mouseX, float mouseY) {
        for (int i = 0; i < points.size() - 1; i++) {
            PVector p1 = points.get(i);
            PVector p2 = points.get(i + 1);

            if (distToSegment(mouseX, mouseY, p1, p2) < 10) {
                return true;
            }
        }
        return false;
    }

    /**
     * Calculates the shortest distance from a point to a line segment.
     *
     * @param x  the x-coordinate of the point.
     * @param y  the y-coordinate of the point.
     * @param p1 the first point of the line segment.
     * @param p2 the second point of the line segment.
     * @return the shortest distance from the point to the line segment.
     */
    private float distToSegment(float x, float y, PVector p1, PVector p2) {
        PVector v = new PVector(x, y);
        PVector lineVec = PVector.sub(p2, p1);
        PVector pointVec = PVector.sub(v, p1);
        float lineLength = lineVec.mag();
        lineVec.normalize();
        float projection = PVector.dot(pointVec, lineVec);

        projection = app.constrain(projection, 0, lineLength);

        PVector closestPoint = PVector.add(p1, PVector.mult(lineVec, projection));
        return PVector.dist(v, closestPoint);
    }


    /**
     * Gets the list of points in the line.
     *
     * @return the list of points.
     */
    public ArrayList<PVector> getPoints() {
        return points;
    }
}
