package org.example.config;

import java.util.List;

public record ModelConfig(SimulationConfig simulation, GridConfig grid, Coordinate nest, List<Coordinate> obstacles, List<FoodSource> foodSources, int ants, PheromonesConfig pheromones, EnergyCostsConfig energyCosts) {
    public record SimulationConfig(int maxTicks, int tickDelayMs, int randomSeed) {}

    public record GridConfig(int width, int height, int defaultCapacity) {}

    public record Coordinate(int x, int y) {}

    public record FoodSource(int x, int y, int amount) {}

    public record PheromonesConfig(double evaporationPerTick, double depositAmount, double randomnessFactor) {}

    public record EnergyCostsConfig(int move, int pickUp, int drop) {}
}