package org.example.environment;

import org.example.enums.ActionType;
import org.example.model.CellPerception;
import org.example.model.Perception;

import java.util.HashMap;
import java.util.Map;

public class Grid {
    private  Cell[][] cells;
    private  int width;
    private  int height;

    public Grid(int width, int height, int defaultCapacity) {
        this.width = width;
        this.height = height;
        this.cells = new Cell[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                cells[x][y] = new Cell(defaultCapacity, 0, false);
            }
        }
    }

    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public boolean isValid(int x, int y) {
        return x >= 0 && x < width && y >= 0 && y < height;
    }
    public void setCell(int x, int y, Cell cell) {
        if (isValid(x, y)) cells[x][y] = cell;
    }
    public Cell getCell(int x, int y) {
        return isValid(x, y) ? cells[x][y] : null;
    }

    private void addNeighborPerception(Map<ActionType, CellPerception> map, ActionType dir, int nx, int ny) {
        if (isValid(nx, ny) && !cells[nx][ny].isObstacle() && !cells[nx][ny].isFull()) {
            map.put(dir, cells[nx][ny].toPerception());
        }
    }

    public Perception getPerceptionForPosition(int x, int y) {
        CellPerception current = cells[x][y].toPerception();
        Map<ActionType, CellPerception> neighbors = new HashMap<>();
        addNeighborPerception(neighbors, ActionType.UP, x, y - 1);
        addNeighborPerception(neighbors, ActionType.DOWN, x, y + 1);
        addNeighborPerception(neighbors, ActionType.LEFT, x - 1, y);
        addNeighborPerception(neighbors, ActionType.RIGHT, x + 1, y);
        return new Perception(current, neighbors);
    }

    public void evaporatePheromones(double rate) {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                cells[x][y].evaporate(rate);
            }
        }
    }
}
