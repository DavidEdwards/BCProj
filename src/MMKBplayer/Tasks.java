package MMKBplayer;

import battlecode.common.*;
import java.awt.*;

/**
 * Created by movia on 1/16/2017.
 */
public class Tasks {
    /**
     * General task to dodge a bullet with the passed Bullet information
     *
     * @param rc the robot that needs to dodge.
     * @param bi the bullet that needs to be dodged.
     * @return True if the line of the bullet's path intersects with this robot's current position.
     */

    static boolean DodgeBullet(RobotController rc, BulletInfo bi)
    {

        return false;
    }

    static boolean Combat(RobotController rc, BulletInfo bi)
    {

        return false;
    }

    static boolean Shakeing(RobotController rc, BulletInfo bi)
    {

        return false;
    }

    static boolean Chopping(RobotController rc, BulletInfo bi)
    {

        return false;
    }

    static boolean TreePlanting(RobotController rc, BulletInfo bi)
    {

        return false;
    }

    static boolean TreeWatering(RobotController rc, BulletInfo bi)
    {

        return false;
    }

    static boolean BuildRobot(RobotController rc) throws GameActionException
    {
        try {
            // Generate a random direction
            Direction dir = SharedSubs.randomDirection();

            // Randomly attempt to build a gardener in this direction
            if (rc.canHireGardener(dir) && Math.random() < .01) {
                rc.hireGardener(dir);
            }
        } catch (Exception e) {
            System.out.println("BuildRobot Exception");
            e.printStackTrace();
        }
        return false;
    }
}
