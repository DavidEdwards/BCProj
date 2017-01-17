package MMKBplayer;
import battlecode.common.*;

public strictfp class RobotPlayer {

    static RobotController rc;

    /**
     * run() is the method that is called when a robot is instantiated in the Battlecode world.
     * If this method returns, the robot dies!
    **/
    @SuppressWarnings("unused")

    public static void run(RobotController rc) throws GameActionException {

        // This is the RobotController object. You use it to perform actions from this robot,
        // and to get information on its current status.
        RobotPlayer.rc = rc;

        BulletInfo[] Bullets = rc.senseNearbyBullets();
        TreeInfo[] Tress = rc.senseNearbyTrees();
        RobotInfo[] Robots = rc.senseNearbyRobots();
        // Here, we've separated the controls into a different method for each RobotType.
        // You can add the missing ones or rewrite this into your own control structure.

        RunRobot(Bullets, Tress, Robots);

	}

    static void RunRobot(BulletInfo[] Bullets, TreeInfo[] Trees, RobotInfo[] Robots) throws GameActionException {
        System.out.println("I'm an archon!");

        // The code you want your robot to perform every round should be in this loop
        while (true) {

            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode
            try {
                if (Clock.getBytecodesLeft() > 100) {

                    DecisionEngine.Task CurrentTask = DecisionEngine.GetTaskDecision(rc, Bullets, Trees, Robots);
                    Tasks.PerformTask(rc, CurrentTask);
                }
                // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
                Clock.yield();

            } catch (Exception e) {
                System.out.println("Archon Exception");
                e.printStackTrace();
            }
        }
    }
}
