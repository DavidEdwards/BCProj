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

        //Get general info for robot.
        BulletInfo[] Bullets = rc.senseNearbyBullets();
        TreeInfo[] Tress = rc.senseNearbyTrees();
        RobotInfo[] Robots = rc.senseNearbyRobots();

        RunRobot(Bullets, Tress, Robots);

	}

    static void RunRobot(BulletInfo[] Bullets, TreeInfo[] Trees, RobotInfo[] Robots) throws GameActionException {
        System.out.println("I'm a " + rc.getType().toString() + "!");
        DecisionEngine.RobotTaskList PreviousTask = null;
        // The code you want your robot to perform every round should be in this loop
        while (true) {

            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode
            try {

                DecisionEngine.RobotTaskList CurrentTask = DecisionEngine.GetTaskDecision(rc, Bullets, Trees, Robots);
                Tasks.PerformTask(rc, CurrentTask, Bullets, Trees, Robots);

                if (CurrentTask != PreviousTask){System.out.println(CurrentTask.toString());}
                PreviousTask = CurrentTask;

                // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
                Clock.yield();

            } catch (Exception e) {
                System.out.println(rc.getType().toString() +" Exception");
                e.printStackTrace();
            }
        }
    }
}
