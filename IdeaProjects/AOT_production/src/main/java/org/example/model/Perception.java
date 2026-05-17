package org.example.model;

import org.example.enums.ActionType;

import java.util.Map;

public class Perception {
    private CellPerception currentCell;
    private Map<ActionType,CellPerception> neighbouringCells;
    public Perception(CellPerception currentCell, Map<ActionType, CellPerception> neighbors) {
        this.currentCell = currentCell;
        this.neighbouringCells = neighbors;
    }

    public CellPerception getCurrentCell() { return currentCell; }
    public Map<ActionType, CellPerception> getNeighbouringCells() { return neighbouringCells; }
}
