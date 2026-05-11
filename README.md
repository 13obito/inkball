# Inkball

A small ball-and-hole puzzle game built with **Java 8**, **Processing** (`PApplet`), and **Gradle**. Balls move on a grid; you draw black lines to deflect them into matching holes.

> This project is a cleaned and modified version of a course project.  
> Some assessment-specific files and test cases have been removed.

## Requirements

- **JDK 8** or newer (the original course target was Java 8)

## Run

From the project root (same folder as `build.gradle`, `config.json`, and `level*.txt`):

```bash
# Windows
.\gradlew.bat run
```

Or double-click `run.bat` if present.

On macOS / Linux:

```bash
chmod +x gradlew
./gradlew run
```

If you use a global Gradle install:

```bash
gradle run
```

## Build a fat JAR

```bash
.\gradlew.bat jar
java -jar build/libs/inkball-1.0.jar
```

Run the JAR from the project root so `config.json` and level files resolve correctly.

## Configuration

- `config.json` — levels, timers, spawn intervals, scoring
- `level1.txt`, `level2.txt`, … — 18×18-style layouts (see comments in your course spec for tile codes)

Sprites live under `src/main/resources/inkball/`.

## License

No license is claimed for this educational demo; add one if you repurpose the code.
