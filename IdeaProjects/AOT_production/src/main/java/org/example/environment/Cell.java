package org.example.environment;

import org.example.model.CellPerception;

public class
Cell {
    private int capacity;
    private int foodAmount;
    private double nestPheromone;
    private double foodPheromone = 0.0;
    private boolean isNest;
    private int currentAntCount = 0;

    public Cell(int capacity, int foodAmount, boolean isNest) {
        this.capacity = capacity;
        this.foodAmount = foodAmount;
        this.isNest = isNest;
        this.nestPheromone = isNest ? 100.0 : 0.0;
    }

    public boolean isObstacle() { return capacity == 0; }
    public boolean isFull() { return capacity > 0 && currentAntCount >= capacity; }

    public int getFoodAmount() { return foodAmount; }
    public boolean hasFood() { return foodAmount > 0; }
    public boolean isNest() { return isNest; }

    public double getNestPheromone() { return nestPheromone; }
    public double getFoodPheromone() { return foodPheromone; }

    public void addFood(int amount) { this.foodAmount += amount; }
    public void removeFood(int amount) { this.foodAmount = Math.max(0, this.foodAmount - amount); }

    public void depositNestPheromone(double amount) { if (!isNest) this.nestPheromone += amount; }
    public void depositFoodPheromone(double amount) { this.foodPheromone += amount; }
    public void evaporate(double rate) {
        if (!isNest) this.nestPheromone = Math.max(0, this.nestPheromone - rate);
        this.foodPheromone = Math.max(0, this.foodPheromone - rate);
    }
    public void incrementAnts() { this.currentAntCount++; }
    public void decrementAnts() { this.currentAntCount = Math.max(0, this.currentAntCount - 1); }
    public CellPerception toPerception(){return new CellPerception(isNest, hasFood(), nestPheromone, foodPheromone);}
}
