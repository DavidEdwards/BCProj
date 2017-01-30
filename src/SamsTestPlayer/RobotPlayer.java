package SamsTestPlayer;
import battlecode.common.*;

import java.awt.*;
import java.util.*;

public strictfp class RobotPlayer {
    static RobotController rc;
    static Random myRand;

    /**
     * run() is the method that is called when a robot is instantiated in the Battlecode world.
     * If this method returns, the robot dies!
     **/
    public static void run(RobotController rc) throws GameActionException {

        // This is the RobotController object. You use it to perform actions from this robot,
        // and to get information on its current status.
        RobotPlayer.rc = rc;
        myRand = new Random(rc.getID());

        // Here, we've separated the controls into a different method for each RobotType.
        // You can add the missing ones or rewrite this into your own control structure.
        switch (rc.getType()) {
            case ARCHON:
                new Archon(rc).run();
                break;
            case GARDENER:
                new Gardener(rc).run();
                break;
            case SOLDIER:
                new Soldier(rc).run();
                break;
            case LUMBERJACK:
                new Lumberjack(rc).run();
                break;
            case SCOUT:
                new Scout(rc).run();
                break;
            case TANK:
                new Tank(rc).run();
                break;
        }
	}
}




