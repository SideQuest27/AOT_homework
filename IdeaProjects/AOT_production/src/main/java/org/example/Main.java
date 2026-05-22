package org.example;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.agent.Ant;
import org.example.config.ModelConfig;
import org.example.environment.Cell;
import org.example.environment.Grid;
import org.example.simulation.Manager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Random;

public class Main {
    public static int SimulationMaxTicks;
    public static int SimulationTickDelayMs;
    public static int SimulationRandomSeed;
    public static int GridWidth;
    public static int GridHeight;
    public static int GridDefaultCapacity;
    public static int NestX;
    public static int NestY;
    public static int Ants;
    public static double PheromonesEvaporationPerTick;
    public static double PheromonesDepositAmount;
    public static double PheromonesRandomnessFactor;
    public static List<ModelConfig.Coordinate> Obstacles;
    public static List<ModelConfig.FoodSource> FoodSource;
    public static double EnergyCostMove;
    public static Random random = null; // TODO: 19/05/2026 add the randomness seed


    public static void main(String[] args) {


        ObjectMapper mapper = new ObjectMapper();
        String firstVar = "";
        if (!Paths.get("","").toAbsolutePath().toString().contains("IdeaProjects\\AOT_homework")){
               firstVar = "IdeaProjects/AOT_homework/";
        }

        Path relativePath = Paths.get( firstVar,"IdeaProjects/AOT_production/src/main/java/org/example/config/modelConfig.json");
        Path absolutePath = relativePath.toAbsolutePath();



        try {
            List<ModelConfig> configList = mapper.readValue(
                    new File(absolutePath.toString()),
                    new TypeReference<List<ModelConfig>>() {}
            );

            if (!configList.isEmpty()) {
                ModelConfig config = configList.get(0);

                SimulationMaxTicks = config.simulation().maxTicks();
                SimulationTickDelayMs = config.simulation().tickDelayMs();
                SimulationRandomSeed = config.simulation().randomSeed();


                random = new Random(SimulationRandomSeed);

                GridWidth = config.grid().width();
                GridHeight = config.grid().height();
                GridDefaultCapacity = config.grid().defaultCapacity();

                NestX = config.nest().x();
                NestY = config.nest().y();

                Ants = config.ants();

                PheromonesEvaporationPerTick = config.pheromones().evaporationPerTick();
                PheromonesDepositAmount = config.pheromones().depositAmount();
                PheromonesRandomnessFactor = config.pheromones().randomnessFactor();

                Obstacles = config.obstacles();

                FoodSource = config.foodSources();

            } else {
                System.out.println("The JSON array is empty.");
            }

        } catch (IOException e) {
            System.err.println("Failed to parse JSON file: " + e.getMessage());
            e.printStackTrace();
        }

        Grid grid = new Grid(GridWidth, GridHeight, GridDefaultCapacity);

        Cell nest = new Cell(GridDefaultCapacity,0,true);
        grid.setCell(NestX, NestY, nest);

        for(ModelConfig.Coordinate obstacleCoordinates : Obstacles){
            grid.setCell(obstacleCoordinates.x(), obstacleCoordinates.y(), new Cell(0, 0, false));
        }

        for(ModelConfig.FoodSource foodSource : FoodSource){
            grid.setCell(foodSource.x(), foodSource.y(), new Cell(GridDefaultCapacity, foodSource.amount(), false));
        }

        Manager manager = new Manager(grid);

        for (int i = 0; i < Ants; i++) {
            Ant worker = new Ant(i, 100);
            manager.registerAnt(worker, NestX, NestY);
        }

        manager.runSimulationLoop();
        manager.printAggregatedScore();
    }



}