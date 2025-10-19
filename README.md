# A* Pathfinding Visualizer

A clean and interactive **A\*** pathfinding visualizer built with **JavaFX**.  
This project demonstrates how the A* algorithm efficiently finds the shortest path between two points while navigating around obstacles on a grid.

---

## Features

- **Interactive grid:** Click to add or remove walls  
- **Start/End selection:** Right-click to set or remove start and end nodes  
- **A\*** algorithm visualization with smooth animation  
- **Random maze generation** for testing  
- **Reset and clear options** to quickly experiment  
- Real-time color-coded visualization of open, closed, and path nodes  

---

## Demo

| Color | Meaning |
|-------|----------|
| Green | Start node |
| Red | End node |
| Black | Wall/obstacle |
| Blue | Visited/open node |
| Gold | Final path |

---

## How It Works

The visualizer implements the **A\*** (A-star) search algorithm, which combines:
- **G cost:** distance from the start node  
- **H cost:** heuristic (Manhattan distance to the end node)  
- **F = G + H:** total estimated cost  

The algorithm continuously expands the node with the lowest `F` score until it reaches the goal or determines that no path exists.

---

## Technologies Used

- **Language:** Java  
- **Framework:** JavaFX  
- **Algorithm:** A\* (A-star) Search  
- **Tools:** IntelliJ IDEA / VS Code  

---

## How to Run

1. Clone the repository:
   ```bash
   git clone https://github.com/<your-username>/AStar-Pathfinding-Visualizer.git
   ```
2. Open the project in your IDE
3. Make sure JavaFX is configured (Java 17+ recommended)
4. Run:
   ```bash
   PathfindingVisualizer.java
   ```
---

## Author

# Chloe Nuzillat
- cnuzillat@gmail.com
- www.linkedin.com/in/chloe-nuzillat
- www.github.com/cnuzillat
