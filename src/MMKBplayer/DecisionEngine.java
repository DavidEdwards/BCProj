package MMKBplayer;

import battlecode.common.*;
import com.sun.xml.internal.ws.api.pipe.FiberContextSwitchInterceptor;

import java.awt.*;

/**
 * Created by movian on 1/16/2017.
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

    static int RobotsBuilt = 0;

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
        Task RobotTask = null;
        try {
            BaseLineArray(rc.getType());

            //Process Bullets (Same for all bots)
            WeightDodge(Bullets);
            WeightCombat(Robots);
            WeightTreeShake(Trees);
            if(rc.getType() == RobotType.LUMBERJACK){ WeightChop(Trees);}
            if(rc.getType() == RobotType.GARDENER){WeightPlant(Trees); WeightWater(Trees);}
            if(rc.getType() == RobotType.SCOUT){WeightLure(rc); }
            if(rc.getType() == RobotType.GARDENER){WeightBuild(rc); }

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
        } catch (Exception e) {
            System.out.println("GetTaskDecision Exception");
            e.printStackTrace();
        }
        return RobotTask;
    }

    static void WeightDodge(BulletInfo[] Bullets)
    {
        if (Bullets.length > 0) {WorkingWeights[0] += Bullets.length * 10;}
    }

    static void WeightCombat(RobotInfo[] Robots)
    {
        //logic adjusting the value of Combat
        if (Robots.length > 0)
        {
            WorkingWeights[1] += Robots.length *10;
        }
    }

    static void WeightTreeShake(TreeInfo[] Trees)
    {
        //logic adjusting the value of Shake
        if (Trees.length > 0)
        {
            for(int i = 0; i < Trees.length;i++) {
                if (Trees[i].containedBullets > 2 ){ WorkingWeights[2] += + 10;}
            }
        }
    }

    static void WeightChop(TreeInfo[] Trees)
    {
        //logic adjusting the value of Shake
        if (Trees.length > 0)
        {
            for(int i = 0; i < Trees.length;i++) {
                if (Trees[i].containedRobot != null){ WorkingWeights[3] += + 10;}
            }
        }
    }

    static void WeightPlant(TreeInfo[] Trees)
    {
        WorkingWeights[4] += Trees.length * -4;
    }

    static void WeightWater(TreeInfo[] Trees)
    {
        for (int i=0;i < Trees.length;i++)
        {
            if (Trees[i].health < 50){WorkingWeights[5] += 10;}
        }
    }

    static void WeightLure(RobotController rc)
    {
        //logic adjusting the value of Shake
    }

    static void WeightBuild(RobotController rc)
    {
        if( RobotsBuilt < rc.getRoundNum() / 100 ){
            WorkingWeights[7] += rc.getRoundNum() / 100;
        }
    }

}
