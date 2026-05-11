package inkball;

import processing.core.PApplet;
import processing.core.PImage;

import java.io.File;

/**
 * Resolves inkball sprite paths for Gradle runs (project dir) and processed resources (build/).
 */
public final class ResourceImages {

    private ResourceImages() {
    }

    /**
     * @param filename file name only, e.g. {@code ball0.png}
     */
    public static PImage load(PApplet app, String filename) {
        String[] candidates = new String[] {
            "src/main/resources/inkball/" + filename,
            "build/resources/main/inkball/" + filename,
        };
        for (String path : candidates) {
            if (new File(path).isFile()) {
                return app.loadImage(path);
            }
        }
        return app.loadImage("src/main/resources/inkball/" + filename);
    }
}
