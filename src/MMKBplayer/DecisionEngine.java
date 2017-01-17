package MMKBplayer;

import battlecode.common.*;
import com.sun.xml.internal.ws.api.pipe.FiberContextSwitchInterceptor;

import java.awt.*;

/**
 * Created by movia on 1/16/2017.
 */
public class DecisionEngine {
    // Task Baselines
    /*
    0 - Dodge
    1 - Combat
    2 - Shake
    3 - Chop
    4 - Plant
    5 - Water
    6 - Lure
    7 - Build
     */
    static int ArchonWeightlist[] = {100,0,60,0,0,0,0,40};
    static int GardnerWeightlist[] = {100,50,60,40,70,70,20,40};
    static int SoldierWeightlist[] = {100,50,60,40,70,70,20,40};
    static int TankWeightlist[] = {100,50,60,40,70,70,20,40};
    static int ScoutWeightlist[] = {100,50,60,40,70,70,20,40};
    static int LumberjackWeightlist[] = {100,50,60,40,70,70,20,40};
    static int WorkingWeights[];

    static Task[] TaskList = {Task.Dodge, Task.Combat, Task.Shake, Task.Chop, Task.Plant, Task.Water, Task.Lure, Task.Build} ;
    public enum Task{
        Build, Dodge, Combat, Shake, Chop, Plant, Water, Lure
    }

    static void BaseLineArray(RobotType Rtype)
    {
        switch (Rtype)
        {
            case ARCHON:
                WorkingWeights = ArchonWeightlist;
                break;
            case GARDENER:
                WorkingWeights = GardnerWeightlist;
                break;
            case SOLDIER:
                WorkingWeights = SoldierWeightlist;
                break;
            case TANK:
                WorkingWeights = TankWeightlist;
                break;
            case SCOUT:
                WorkingWeights = ScoutWeightlist;
                break;
            case LUMBERJACK:
                WorkingWeights = LumberjackWeightlist;
                break;
        }
    }

    static Task GetTaskDecision(RobotController rc,  BulletInfo[] Bullets, TreeInfo[] Trees, RobotInfo[] Robots) {
        try {
            BaseLineArray(rc.getType());
            switch (rc.getType()) {
                case ARCHON:
                    return GetArchonTask(rc, Bullets, Trees, Robots);
            }
        } catch (Exception e) {
            System.out.println("GetTaskDecision Exception");
            e.printStackTrace();
        }
        return null;
    }

    static Task GetArchonTask(RobotController rc, BulletInfo[] Bullets, TreeInfo[] Trees, RobotInfo[] Robots) throws GameActionException {
        Task ArchonTask = null;
        try {
            if (Bullets.length > 0) {WorkingWeights[0] += Bullets.length * 10;}
            if (Robots.length < 1) {WorkingWeights[7] += 30;}

            //Check final numbers from decision engine
            int Decision = 0;
            int weight = 0;
            for (int i = 0; i < WorkingWeights.length;i++)
            {
                if (weight < WorkingWeights[i]) {
                    Decision = i;
                    weight = WorkingWeights[i];
                }
            }
            ArchonTask = TaskList[Decision];
        } catch (Exception e) {
            System.out.println("Archon Exception");
            e.printStackTrace();
        }

        return ArchonTask;
    }
}
