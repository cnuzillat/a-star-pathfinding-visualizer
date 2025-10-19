import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.util.*;

/**
 * A* Pathfinding Visualizer
 * -------------------------
 * A JavaFX application that visualizes the A* pathfinding algorithm.
 * -------------------------
 * Features:
 * - Click to toggle walls.
 * - Right-click to set Start and End cells.
 * - Random maze generation.
 * - Step-by-step animation of A* search.
 * -------------------------
 * This project demonstrates GUI programming, multithreading, and algorithm visualization.
 */
public class PathfindingVisualizer extends Application {

    private static final int ROWS = 20;
    private static final int COLS = 30;
    private static final int CELL_SIZE = 28;

    private final Cell[][] grid = new Cell[ROWS][COLS];
    private final GridPane gridPane = new GridPane();

    private Cell startCell = null;
    private Cell endCell = null;

    private volatile boolean running = false;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("A* Pathfinding Visualizer");

        buildGrid();

        Button runBtn = new Button("Run A*");
        Button clearPathBtn = new Button("Clear Path");
        Button resetBtn = new Button("Reset Grid");
        Button randomMazeBtn = new Button("Random Maze");

        runBtn.setTooltip(new Tooltip("Run A* algorithm (requires start & end)"));
        clearPathBtn.setTooltip(new Tooltip("Clear the found path & visited markers"));
        resetBtn.setTooltip(new Tooltip("Clear everything"));
        randomMazeBtn.setTooltip(new Tooltip("Randomly place walls"));

        runBtn.setOnAction(e -> {
            if (running) return;
            if (startCell == null || endCell == null) {
                System.out.println("Set start and end (right-click).");
                return;
            }
            runAStar();
        });

        clearPathBtn.setOnAction(e -> {
            if (running) return;
            clearVisitedAndPath();
        });

        resetBtn.setOnAction(e -> {
            if (running) return;
            resetGrid();
        });

        randomMazeBtn.setOnAction(e -> {
            if (running) return;
            randomMaze();
        });

        HBox controls = new HBox(8, runBtn, clearPathBtn, randomMazeBtn, resetBtn);
        controls.setPadding(new Insets(8));

        BorderPane root = new BorderPane();
        root.setCenter(gridPane);
        root.setTop(controls);

        Scene scene = new Scene(root, COLS * (CELL_SIZE + 1) + 10, ROWS * (CELL_SIZE + 1) + 80);
        primaryStage.setFullScreen(true);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * Builds the initial grid of cells and sets up click behavior for walls and start/end cells.
     */
    private void buildGrid() {
        gridPane.getChildren().clear();
        gridPane.setPadding(new Insets(8));
        gridPane.setHgap(1);
        gridPane.setVgap(1);

        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                Rectangle rect = new Rectangle(CELL_SIZE, CELL_SIZE);
                rect.setStroke(Color.LIGHTGRAY);
                rect.setFill(Color.WHITE);

                Cell cell = new Cell(r, c, rect);
                grid[r][c] = cell;

                rect.setOnMouseClicked(ev -> {
                    if (running) return;
                    if (ev.getButton() == MouseButton.PRIMARY) {
                        cell.wall = !cell.wall;
                        updateCellColor(cell);
                    }
                    else if (ev.getButton() == MouseButton.SECONDARY) {
                        if (startCell == null && !cell.wall && cell != endCell) {
                            startCell = cell;
                        }
                        else if (endCell == null && !cell.wall && cell != startCell) {
                            endCell = cell;
                        }
                        else if (cell == startCell) {
                            startCell = null;
                        }
                        else if (cell == endCell) {
                            endCell = null;
                        }
                        updateAllColors();
                    }
                });

                gridPane.add(rect, c, r);
            }
        }
    }

    /**
     * Updates the color of a single cell based on its state.
     */
    private void updateCellColor(Cell cell) {
        Platform.runLater(() -> {
            if (cell == startCell) {
                cell.rect.setFill(Color.GREEN);
            }
            else if (cell == endCell) {
                cell.rect.setFill(Color.RED);
            }
            else if (cell.wall) {
                cell.rect.setFill(Color.BLACK);
            }
            else {
                cell.rect.setFill(Color.WHITE);
            }
        });
    }

    /**
     * Updates colors of all cells (used after mass updates like reset or random maze).
     */
    private void updateAllColors() {
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                updateCellColor(grid[r][c]);
            }
        }
    }

    /**
     * Clears visited and path states, keeping walls and start/end intact.
     */
    private void clearVisitedAndPath() {
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                Cell cell = grid[r][c];
                cell.visited = false;
                cell.inPath = false;
                cell.g = Double.POSITIVE_INFINITY;
                cell.h = 0;
                cell.f = Double.POSITIVE_INFINITY;
                cell.parent = null;
                if (!cell.wall && cell != startCell && cell != endCell) {
                    cell.rect.setFill(Color.WHITE);
                }
                else {
                    updateCellColor(cell);
                }
            }
        }
    }

    /**
     * Resets the grid to a clean state (no walls, no start/end, no path).
     */
    private void resetGrid() {
        startCell = null;
        endCell = null;
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                Cell cell = grid[r][c];
                cell.wall = false;
                cell.visited = false;
                cell.inPath = false;
                cell.parent = null;
                cell.g = Double.POSITIVE_INFINITY;
                cell.h = 0;
                cell.f = Double.POSITIVE_INFINITY;
                cell.rect.setFill(Color.WHITE);
            }
        }
    }

    /**
     * Randomly generates walls on the grid to create a maze-like layout.
     */
    private void randomMaze() {
        Random rnd = new Random();
        resetGrid();
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                if (rnd.nextDouble() < 0.25) {
                    grid[r][c].wall = true;
                }
            }
        }
        if (startCell != null) startCell.wall = false;
        if (endCell != null) endCell.wall = false;
        updateAllColors();
    }

    /**
     * Starts the A* algorithm in a background thread.
     */
    private void runAStar() {
        running = true;
        clearVisitedAndPath();

        Thread algoThread = new Thread(() -> {
            try {
                aStarSearch();
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            } finally {
                running = false;
            }
        });

        algoThread.setDaemon(true);
        algoThread.start();
    }

    /**
     * Executes the A* pathfinding algorithm with visual updates.
     */
    private void aStarSearch() throws InterruptedException {
        PriorityQueue<Cell> open = new PriorityQueue<>(Comparator.comparingDouble(c -> c.f));
        startCell.g = 0;
        startCell.h = manhattan(startCell, endCell);
        startCell.f = startCell.h;
        open.add(startCell);

        Set<Cell> closed = new HashSet<>();

        while (!open.isEmpty()) {
            Cell current = open.poll();

            if (current == endCell) {
                reconstructPath(current);
                return;
            }

            closed.add(current);
            current.visited = true;
            if (current != startCell) {
                colorCell(current, Color.LIGHTBLUE);
            }

            Thread.sleep(20);

            for (Cell neighbor : neighbors(current)) {
                if (closed.contains(neighbor) || neighbor.wall) continue;

                double tentativeG = current.g + 1;
                boolean better = false;
                if (!open.contains(neighbor)) {
                    neighbor.h = manhattan(neighbor, endCell);
                    better = true;
                } else if (tentativeG < neighbor.g) {
                    better = true;
                }

                if (better) {
                    neighbor.parent = current;
                    neighbor.g = tentativeG;
                    neighbor.f = neighbor.g + neighbor.h;
                    if (!open.contains(neighbor)) {
                        open.add(neighbor);
                        if (neighbor != startCell && neighbor != endCell) {
                            colorCell(neighbor, Color.CORNFLOWERBLUE.darker());
                        }
                    }
                }
            }
        }

        System.out.println("No path found.");
    }

    /**
     * Reconstructs and highlights the shortest path found by A*.
     */
    private void reconstructPath(Cell end) {
        Cell cur = end;
        while (cur != null) {
            cur.inPath = true;
            if (cur != startCell && cur != endCell) {
                colorCell(cur, Color.GOLD);
            }
            cur = cur.parent;
            try { Thread.sleep(40); } catch (InterruptedException ignored) {}
        }
    }

    /**
     * Returns the 4-connected neighbors (up, down, left, right) of a cell.
     */
    private List<Cell> neighbors(Cell c) {
        List<Cell> list = new ArrayList<>();
        int r = c.row, co = c.col;
        if (r > 0) list.add(grid[r - 1][co]);
        if (r < ROWS - 1) list.add(grid[r + 1][co]);
        if (co > 0) list.add(grid[r][co - 1]);
        if (co < COLS - 1) list.add(grid[r][co + 1]);
        return list;
    }

    /**
     * Calculates the Manhattan distance between two cells.
     */
    private double manhattan(Cell a, Cell b) {
        return Math.abs(a.row - b.row) + Math.abs(a.col - b.col);
    }

    /**
     * Updates a cell's color on the JavaFX thread safely.
     */
    private void colorCell(Cell cell, Color color) {
        Platform.runLater(() -> {
            if (cell == startCell) cell.rect.setFill(Color.GREEN);
            else if (cell == endCell) cell.rect.setFill(Color.RED);
            else if (cell.wall) cell.rect.setFill(Color.BLACK);
            else cell.rect.setFill(color);
        });
    }

    /**
     * Represents a single cell in the grid.
     */
    private static class Cell {
        int row, col;
        boolean wall = false;
        boolean visited = false;
        boolean inPath = false;
        double g = Double.POSITIVE_INFINITY;
        double h = 0;
        double f = Double.POSITIVE_INFINITY;
        Cell parent = null;
        Rectangle rect;

        Cell(int r, int c, Rectangle rect) {
            this.row = r;
            this.col = c;
            this.rect = rect;
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
