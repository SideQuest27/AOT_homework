package org.example.simulation;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.Main;
import org.example.agent.Ant;
import org.example.config.ModelConfig;
import org.example.enums.ActionType;
import org.example.enums.AntState;
import org.example.enums.MessageType;
import org.example.environment.Cell;
import org.example.environment.Grid;
import org.example.model.Action;
import org.example.model.Perception;

import java.util.*;

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
        // Core Population State Counters [cite: 250, 251, 252]
        long antsAlive = ants.stream().filter(Ant::isAlive).count();
        long antsSearching = ants.stream().filter(a -> a.isAlive() && a.getState() == AntState.SEARCHING).count();
        long antsReturning = ants.stream().filter(a -> a.isAlive() && a.getState() == AntState.RETURNING).count();

        // Nest Inventory Tracking (Nest at 10,10) [cite: 249, 270]
        int totalFoodInNest = grid.getCell(10, 10).getFoodAmount();

        // Capture Discovery Speed [cite: 254]
        if (firstFoodDiscoveredTick == -1 && totalFoodInNest > 0) {
            firstFoodDiscoveredTick = currentTick;
        }

        // 3. Dynamic CSV Header Injection using the config list size [cite: 247]
        if (currentTick == 0) {
            StringJoiner header = new StringJoiner(",");
            header.add("tick").add("totalFoodInNest").add("antsAlive").add("antsSearching").add("antsReturning");

            // Loop using the configuration count
            for (int i = 0; i < FoodSource.size(); i++) {
                header.add("foodSrc" + (i + 1) + "Remaining");
            }

            header.add("firstFoodDiscoveredTick").add("pheromoneTrailsActive");
            logger.info(header.toString());
        }

        // 4. Build CSV Data Row
        StringJoiner csvRow = new StringJoiner(",");
        csvRow.add(String.valueOf(currentTick));
        csvRow.add(String.valueOf(totalFoodInNest));
        csvRow.add(String.valueOf(antsAlive));
        csvRow.add(String.valueOf(antsSearching));
        csvRow.add(String.valueOf(antsReturning));




        // 5. THE SAME TYPE OF LOGIC: Iterate directly over your ModelConfig sources! [cite: 253]
        for (ModelConfig.FoodSource foodSource : FoodSource) {
            Cell cell = grid.getCell(foodSource.x(), foodSource.y());
            int amount = (cell != null) ? cell.getFoodAmount() : 0;
            csvRow.add(String.valueOf(amount));
        }

        // Calculate Active Pheromone Paths [cite: 255]
        int pheromoneTrailsActive = 0;
        for (int x = 0; x < grid.getWidth(); x++) {
            for (int y = 0; y < grid.getHeight(); y++) {
                Cell cell = grid.getCell(x, y);
                if (cell != null && !cell.isNest()) {
                    if (cell.getFoodPheromone() > 0.0 || cell.getNestPheromone() > 0.0) {
                        pheromoneTrailsActive++;
                    }
                }
            }
        }

        // Append remaining metrics and log row [cite: 245, 246]
        csvRow.add(String.valueOf(firstFoodDiscoveredTick));
        csvRow.add(String.valueOf(pheromoneTrailsActive));

        logger.info(csvRow.toString());
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
