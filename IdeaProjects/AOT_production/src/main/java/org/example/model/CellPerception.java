package org.example.model;

public class CellPerception {
    private boolean isNest;
    private boolean hasFood;
    private double nestPheromone;
    private double foodPheromone;

    public CellPerception(boolean isNest, boolean hasFood, double nestPheromone, double foodPheromone) {
        this.isNest = isNest;
        this.hasFood = hasFood;
        this.nestPheromone = nestPheromone;
        this.foodPheromone = foodPheromone;
    }

    public boolean isNest() {
        return isNest;
    }

    public boolean isHasFood() {
        return hasFood;
    }

    public double getNestPheromone() {
        return nestPheromone;
    }

    public double getFoodPheromone() {
        return foodPheromone;
    }
}
