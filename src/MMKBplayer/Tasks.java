package MMKBplayer;

import battlecode.common.*;


/**
 * Created by movia on 1/16/2017.
 */
public class Tasks {


    /**
     * General task to dodge a bullet with the passed Bullet information
     *
     * @param rc the robot that needs to dodge.
     * @return True if the line of the bullet's path intersects with this robot's current position.
     */

    static void PerformTask( RobotController rc, DecisionEngine.RobotTaskList RobotTask,  BulletInfo[] Bullets, TreeInfo[] Trees, RobotInfo[] Robots)throws GameActionException
    {
        //main routine for calling tasks
        switch (RobotTask)
        {
            case Dodge:
                for(int i=0; i < Bullets.length;i++) {
                    DodgeBullet(rc, Bullets[i]);
                }
                break;
            case Build:
                BuildRobot(rc, DecisionEngine.BuildType);
                break;
            case Shake:
                Shaking(rc, Trees);
                break;
        }
    }

    static void DodgeBullet(RobotController rc, BulletInfo bi) throws GameActionException {
        //while there are bullets still on track to hit robot attempt to sidestep
        while (SharedSubs.willCollideWithMe(rc, bi)) {
            SharedSubs.trySidestep(rc, bi);
        }
        System.out.println("Dodging!");
    }

    static void Combat(RobotController rc) throws GameActionException
    {

        System.out.println("Combat!");
    }

    static void Shaking(RobotController rc, TreeInfo[] Trees) throws GameActionException
    {
        //locate nearest tree with bullets and shake it
        MapLocation myLocation = rc.getLocation();
        for (int i=0; i < Trees.length;i++)
        {
            if(Trees[i].containedBullets > 1)
            {
                if (rc.canShake(Trees[i].getID()))
                {
                    rc.canShake(Trees[i].getID());
                    System.out.println("Shaking!");
                }else{
                    Direction toTree = myLocation.directionTo(Trees[i].getLocation());
                    SharedSubs.tryMove(rc, toTree);
                    System.out.println("Move to Tree!");
                }
            }
        }

    }

    static void Chopping(RobotController rc, TreeInfo[] Trees) throws GameActionException
    {
        //locate nearest tree with robot and chop it
        MapLocation myLocation = rc.getLocation();
        for (int i=0; i < Trees.length;i++)
        {
            if(Trees[i].containedRobot != null)
            {
                if (rc.canChop(Trees[i].getID()))
                {
                    rc.chop(Trees[i].getID());
                }else{
                    Direction toTree = myLocation.directionTo(Trees[i].getLocation());
                    SharedSubs.tryMove(rc, toTree);
                }
            }
        }
        System.out.println("Chopping!");
    }

    static void TreePlanting(RobotController rc) throws GameActionException
    {
        //rotate and plant trees

        System.out.println("Planting!");
    }

    static void TreeWatering(RobotController rc, TreeInfo[] Trees)throws GameActionException
    {
        for (int i =0; i < Trees.length; i++)
        {
            if (Trees[i].health < 50){ rc.water(Trees[i].location);}
        }
        System.out.println("Watering!");
    }

    static void BuildRobot(RobotController rc, RobotType Rtype) throws GameActionException
    {
        try {
            // Generate a random direction
            Direction dir = SharedSubs.randomDirection();
            switch (DecisionEngine.BuildType) {
                case GARDENER:

                    //Attempt to hire gardner in this direction
                    if (rc.canHireGardener(dir) && Math.random() < .01) {
                        rc.hireGardener(dir);
                        DecisionEngine.RobotsBuilt[0]++;
                    }
                    break;
                default:
                    if (rc.canBuildRobot(DecisionEngine.BuildType, dir)) { rc.buildRobot(DecisionEngine.BuildType, dir);}
                    break;
            }
        } catch (Exception e) {
            System.out.println("BuildRobot Exception");
            e.printStackTrace();
        }
        System.out.println("Building!");
    }
}
