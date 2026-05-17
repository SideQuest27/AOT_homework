package org.example.model;

import org.example.agent.Ant;
import org.example.enums.ActionType;

public class Action {

    private ActionType actionType;
    private Ant ant;


    public Action(ActionType actionType, Ant ant) {
        this.actionType = actionType;
        this.ant = ant;
    }

    public ActionType getActionType() {
        return actionType;
    }

    public void setActionType(ActionType actionType) {
        this.actionType = actionType;
    }


    public Ant getAnt() {
        return ant;
    }

    public void setAnt(Ant ant) {
        this.ant = ant;
    }
}
