package MMKBplayer;

import battlecode.common.*;

/**
 * Created by movia on 1/16/2017.
 */
public class DecisionEngine {
    // Task Baselines
    int Dodging = 100;
    int Combat = 50;
    int Shaking = 60;
    int chopping = 40;
    int Planting = 60;
    int watering = 70;
    int Luring = 20;

    public enum Task{
        Build, Dodge, Combat, Shake, Chop, Plant, Water, Lure
    }

    static Task GetTaskDecision(RobotController rc)
    {
        try
        {
            switch (rc.getType()) {
            case ARCHON:
                return DecisionEngine.runArchon();
            }
        } catch (Exception e) {
            System.out.println("GetTaskDecision Exception");
            e.printStackTrace();
        }
        return null;
    }

    static  Task runArchon() throws GameActionException {
        Task ArchonTask = Task.Build;
        try
        {
            // Add code here to determin best current task for this Archon
        } catch (Exception e) {
            System.out.println("Archon Exception");
            e.printStackTrace();
        }
        return ArchonTask;
    }
}
