package org.example.simulation;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.Main;
import org.example.agent.Ant;
import org.example.enums.ActionType;
import org.example.enums.AntState;
import org.example.enums.MessageType;
import org.example.environment.Cell;
import org.example.environment.Grid;
import org.example.model.Action;
import org.example.model.Perception;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.example.Main.*;

public class Manager {
    private final Grid grid;
    private final List<Ant> ants;
    private final Map<Ant, int[]> antPositions;

    private int currentTick = 0;
    private static final Logger logger = LogManager.getLogger(Manager.class);

    public Manager(Grid grid) {
        this.grid = grid;
        this.ants = new ArrayList<>();
        this.antPositions = new HashMap<>();
    }

    public void registerAnt(Ant ant, int startX, int startY){
        ants.add(ant);
        antPositions.put(ant, new int[]{startX, startY});
        grid.getCell(startX,startY).incrementAnts();
    }

    private boolean moveAnt(Ant ant, int[] currentPos, int targetX, int targetY){
        if (!grid.isValid(targetX, targetY)) return false;

        Cell targetCell = grid.getCell(targetX, targetY);
        if (targetCell.isObstacle() || targetCell.isFull()) return false;

        grid.getCell(currentPos[0], currentPos[1]).decrementAnts();

        if (ant.getState() == AntState.SEARCHING) {
            grid.getCell(currentPos[0], currentPos[1]).depositNestPheromone(PheromonesDepositAmount);
        } else {
            grid.getCell(currentPos[0], currentPos[1]).depositFoodPheromone(PheromonesDepositAmount);
        }

        currentPos[0] = targetX;
        currentPos[1] = targetY;
        targetCell.incrementAnts();

        if (targetCell.isNest() || targetCell.hasFood()) {
            ant.refillEnergy();
        }

        return true;
    }

    private boolean processAction(Ant ant, Action action, int[] currentPosition){
        Cell currentCell = grid.getCell(currentPosition[0], currentPosition[1]);
        switch (action.getActionType()){
            case PICK_FOOD:
                if (currentCell.hasFood() && !currentCell.isNest() && ant.getState() == AntState.RETURNING && !ant.isCarryingFood()) {
                    System.out.println(ant.getId()+" pick food");
                    currentCell.removeFood(1);
                    ant.refillEnergy(); // Entering food refills energy completely
                    ant.setCarryingFood(true);
                    return true;
                }
                return false;

            case DROP_FOOD:
                if (currentCell.isNest() && ant.getState() == AntState.SEARCHING && ant.isCarryingFood() ) {
                    System.out.println(ant.getId()+" drop food");
                    currentCell.addFood(1);
                    ant.refillEnergy(); // Entering nest refills energy completely
                    ant.setCarryingFood(false);
                    return true;
                }
                return false;

            case UP:    return moveAnt(ant, currentPosition, currentPosition[0], currentPosition[1] - 1);
            case DOWN:  return moveAnt(ant, currentPosition, currentPosition[0], currentPosition[1] + 1);
            case LEFT:  return moveAnt(ant, currentPosition, currentPosition[0] - 1, currentPosition[1]);
            case RIGHT: return moveAnt(ant, currentPosition, currentPosition[0] + 1, currentPosition[1]);
        }
        return false;
    }

    private boolean anyAntAlive() {
        return ants.stream().anyMatch(Ant::isAlive);
    }

    private int firstFoodDiscoveredTick = -1;

    private void logSystemState() {
        // 1. Core Population State Counters
        long antsAlive = ants.stream().filter(Ant::isAlive).count();
        long antsSearching = ants.stream().filter(a -> a.isAlive() && a.getState() == AntState.SEARCHING).count();
        long antsReturning = ants.stream().filter(a -> a.isAlive() && a.getState() == AntState.RETURNING).count();

        // 2. Nest Inventory Tracking
        int totalFoodInNest = grid.getCell(NestX, NestY).getFoodAmount();

        // 3. Capture Discovery Speed (Fires only once upon first delivery)
        if (firstFoodDiscoveredTick == -1 && totalFoodInNest > 0) {
            firstFoodDiscoveredTick = currentTick;
        }

        // TODO: 17/05/2026 make the foodSource logging dynamic

        // 4. Source Exploitation Bin Counters (Sensing individual coordinates from setup)
        int foodRemainingSrc1 = grid.getCell(2, 2).getFoodAmount();   // Source 1 [cite: 278]
        int foodRemainingSrc2 = grid.getCell(18, 3).getFoodAmount();  // Source 2 [cite: 279]
        int foodRemainingSrc3 = grid.getCell(15, 17).getFoodAmount(); // Source 3 [cite: 280]

        // 5. Active Pheromone Path Counter [cite: 255]
        // Loops through the map to count how many cells have active scent markers left
        int pheromoneTrailsActive = 0;
        for (int x = 0; x < grid.getWidth(); x++) {
            for (int y = 0; y < grid.getHeight(); y++) {
                Cell cell = grid.getCell(x, y);
                if (cell != null) {
                    // A trail cell is active if food pheromone > 0, or nest pheromone > 0 (ignoring the permanent nest hub)
                    if (cell.getFoodPheromone() > 0.0 || (cell.getNestPheromone() > 0.0 && !cell.isNest())) {
                        pheromoneTrailsActive++;
                    }
                }
            }
        }

        // 6. CSV Header Injection [cite: 247]
        if (currentTick == 0) {
            logger.info("tick,totalFoodInNest,antsAlive,antsSearching,antsReturning,foodSrc1Remaining,foodSrc2Remaining,foodSrc3Remaining,firstFoodDiscoveredTick,pheromoneTrailsActive");
        }

        // 7. Format Metrics Line and Print to Log4j Target File [cite: 245-247]
        String csvRow = String.format("%d,%d,%d,%d,%d,%d,%d,%d,%d,%d",
                currentTick,
                totalFoodInNest,
                antsAlive,
                antsSearching,
                antsReturning,
                foodRemainingSrc1,
                foodRemainingSrc2,
                foodRemainingSrc3,
                firstFoodDiscoveredTick,
                pheromoneTrailsActive
        );

        logger.info(csvRow);
    }

    public void runSimulationLoop(){
        while (currentTick < SimulationMaxTicks && anyAntAlive()){

            for (Ant ant : ants){
                if(!ant.isAlive()) continue;

                int[] currentAntPos = antPositions.get(ant);

                Perception perception = grid.getPerceptionForPosition(currentAntPos[0], currentAntPos[1]);
                ant.sense(perception);

                ant.reason();

                Action action = ant.act();
                if (action == null) continue;

                boolean success = processAction(ant, action, currentAntPos);

                if (success){
                    ant.receiveMessage(MessageType.SUCCESS);

                    if (action.getActionType() != ActionType.PICK_FOOD && action.getActionType() != ActionType.DROP_FOOD){
                        ant.consumeEnergy(1);
                        if (ant.getEnergy() == 0){
                            System.err.println(ant.getId()+" Died");
                        }
                    }
                } else ant.receiveMessage(MessageType.FAILED);

            }

            grid.evaporatePheromones(PheromonesEvaporationPerTick);

            logSystemState();

            currentTick++;
        }
    }


}
