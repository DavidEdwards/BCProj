package MMKBplayer;

import battlecode.common.*;


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
    static int ArchonWeightlist[] = {0,0,0,0,0,0,0,0};
    static int GardnerWeightlist[] = {0,0,0,0,0,0,0,0};
    static int SoldierWeightlist[] = {0,0,0,0,0,0,0,0};
    static int TankWeightlist[] = {0,0,0,0,0,0,0,0};
    static int ScoutWeightlist[] = {0,0,0,0,0,0,0,0};
    static int LumberjackWeightlist[] = {0,0,0,0,0,0,0,0};
    static int WorkingWeights[];

    static int RobotsBuilt[] = {0,0,0,0,0};

    static RobotType BuildType = RobotType.GARDENER;

    static RobotTaskList[] TaskList = {RobotTaskList.Dodge, RobotTaskList.Combat, RobotTaskList.Shake, RobotTaskList.Chop, RobotTaskList.Plant, RobotTaskList.Water, RobotTaskList.Lure, RobotTaskList.Build} ;

    public enum RobotTaskList{
    Build, Dodge, Combat, Shake, Chop, Plant, Water, Lure
}

    //Sub that sets up array of base weights depending on robot type.
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

    static RobotTaskList GetTaskDecision(RobotController rc,  BulletInfo[] Bullets, TreeInfo[] Trees, RobotInfo[] Robots) {
        RobotTaskList RobotTask = RobotTaskList.Dodge;
        try {
            BaseLineArray(rc.getType());

            //Process each task for weighting
            WeightDodge(Bullets);
            WeightCombat(Robots);
            WeightTreeShake(Trees);
            if(rc.getType() == RobotType.LUMBERJACK){ WeightChop(Trees);}
            if(rc.getType() == RobotType.GARDENER){WeightPlant(Trees); WeightWater(Trees);}
            if(rc.getType() == RobotType.SCOUT){WeightLure(rc); }
            if(rc.getType() == RobotType.GARDENER || rc.getType() == RobotType.ARCHON ){WeightBuild(rc); }

            //Check final numbers from decision engine and return task with the highest weight
            int Decision = 0;
            int weight = 0;
            for (int i = 0; i < WorkingWeights.length;i++)
            {
                if (weight < WorkingWeights[i]) {
                    Decision = i;
                    weight = WorkingWeights[i];
                }
            }
            RobotTask = TaskList[Decision];
        } catch (Exception e) {
            System.out.println("Get TaskDecision Exception");
            e.printStackTrace();
        }
        return RobotTask;
    }

    static void WeightDodge(BulletInfo[] Bullets)
    {
        if (Bullets.length > 0) {WorkingWeights[0] += (Bullets.length * 20);}
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
                if (Trees[i].containedBullets > 2 ){ WorkingWeights[2] += 2;}
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
        if (RobotsBuilt[0] < (rc.getRoundNum() /100)) {
            WorkingWeights[7] += 10;
        }
    }

}
