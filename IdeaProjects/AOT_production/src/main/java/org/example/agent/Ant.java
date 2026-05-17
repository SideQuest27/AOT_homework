package org.example.agent;

import org.example.enums.ActionType;
import org.example.enums.AntState;
import org.example.enums.MessageType;
import org.example.model.Action;
import org.example.model.CellPerception;
import org.example.model.Perception;

import java.util.*;

import static org.example.Main.*;

public class Ant {
    private int id;
    private int energy;
    private AntState state;
    private Queue<MessageType> inputQueue;
    private Perception currentPerception;
    private ActionType lastMoveAction = null;
    private boolean carryingFood = false;
    private ActionType failedDirection = null;



    public Ant(Integer id, int initialEnergy) {
        this.id = id;
        this.energy = initialEnergy;
        this.state = AntState.SEARCHING;
        this.inputQueue = new LinkedList<>();
    }

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public boolean isAlive() { return energy > 0; }
    public AntState getState() { return state; }
    public boolean isCarryingFood() { return carryingFood; }
    public int getEnergy() {
        return energy;
    }

    public void consumeEnergy(int amount) { this.energy = Math.max(0, this.energy - amount); }
    public void refillEnergy() { this.energy = 100; }
    public void receiveMessage(MessageType msg) { this.inputQueue.add(msg); }
    public void setCarryingFood(boolean carryingFood) {
        this.carryingFood = carryingFood;
    }

    private ActionType getOppositeDirection(ActionType dir) {
        if (dir == null) return null;
        switch (dir) {
            case UP: return ActionType.DOWN;
            case DOWN: return ActionType.UP;
            case LEFT: return ActionType.RIGHT;
            case RIGHT: return ActionType.LEFT;
            default: return null;
        }
    }

    public void sense(Perception perception){
        this.currentPerception = perception;

        while (!inputQueue.isEmpty())
        {
            MessageType msg = inputQueue.poll();

            if(msg == MessageType.FAILED){
                this.failedDirection = this.lastMoveAction;
                this.lastMoveAction = null;
            }else if(msg == MessageType.SUCCESS){
                this.failedDirection = null;
            }
        }
    }

    AntState intendedState;

    public void reason(){
        if(!isAlive()) return;
        // This is buggy and needs to be fixed
        if (state == AntState.SEARCHING && currentPerception.getCurrentCell().isHasFood() && !carryingFood && !currentPerception.getCurrentCell().isNest() ){
            // besides directly changing the state ill set a decided state variable and save the state there
             intendedState = AntState.RETURNING;
        }
        if (state == AntState.RETURNING && currentPerception.getCurrentCell().isNest() && isCarryingFood()){
            intendedState = AntState.SEARCHING;
        }
    }

    public Action act(){
        if(!isAlive()) return null;

        if (state == AntState.SEARCHING && currentPerception.getCurrentCell().isHasFood() && !carryingFood && !currentPerception.getCurrentCell().isNest() ){
            state = intendedState;
            return new Action(ActionType.PICK_FOOD, this);
        }
        if (state == AntState.RETURNING && currentPerception.getCurrentCell().isNest() && isCarryingFood()) {
            state = intendedState;
            return new Action(ActionType.DROP_FOOD, this);
        }

        Map<ActionType, CellPerception> neighbours = currentPerception.getNeighbouringCells();
        if(neighbours.isEmpty()){
            return new Action(getOppositeDirection(lastMoveAction), this);
        }

        Map<ActionType,Double> weights = new HashMap<>();
        double totalWeight = 0.0;

        for (Map.Entry<ActionType, CellPerception> entry : neighbours.entrySet()) {
            ActionType dir = entry.getKey();
            CellPerception cp = entry.getValue();

            double pheromoneValue = (state == AntState.SEARCHING) ? cp.getFoodPheromone() : cp.getNestPheromone();

            double weight = PheromonesRandomnessFactor + pheromoneValue;

            if (lastMoveAction != null && dir == getOppositeDirection(lastMoveAction)) {
                weight *= 0.1;
            }
            weights.put(dir,weight);
            totalWeight += weight;
        }

        double rand = random.nextDouble() * totalWeight;
        double runningSum = 0.0;
        ActionType chosenDir = null;

        for (Map.Entry<ActionType, Double> entry : weights.entrySet()) {
            runningSum += entry.getValue();
            if (rand <= runningSum) {
                chosenDir = entry.getKey();
                break;
            }
        }

        if (chosenDir == null) {
            chosenDir = neighbours.keySet().iterator().next();
        }

        lastMoveAction = chosenDir;
        return new Action(chosenDir,this);
    }

}
