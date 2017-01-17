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

    static void PerformTask( RobotController rc, DecisionEngine.Task RobotTask)
    {

    }

    static void DodgeBullet(RobotController rc, BulletInfo bi) throws GameActionException {

        while (SharedSubs.willCollideWithMe(rc, bi)) {
            SharedSubs.trySidestep(rc, bi);
        }
    }

    static boolean Combat(RobotController rc)
    {

        return false;
    }

    static boolean Shakeing(RobotController rc)
    {

        return false;
    }

    static boolean Chopping(RobotController rc)
    {

        return false;
    }

    static boolean TreePlanting(RobotController rc)
    {

        return false;
    }

    static boolean TreeWatering(RobotController rc)
    {

        return false;
    }

    static boolean BuildRobot(RobotController rc, RobotType Rtype) throws GameActionException
    {
        try {
            switch (Rtype) {
                case GARDENER:
                    // Generate a random direction
                    Direction dir = SharedSubs.randomDirection();
                    //Attempt to hire gardner in this direction
                    if (rc.canHireGardener(dir) && Math.random() < .01) {
                        rc.hireGardener(dir);
                    }
                    break;
            }
            DecisionEngine.RobotsBuilt++;
        } catch (Exception e) {
            System.out.println("BuildRobot Exception");
            e.printStackTrace();
        }
        return false;
    }
}
