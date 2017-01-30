package SamsTestPlayer;

import battlecode.common.*;

public class Tank extends Robot {

    private enum TaskList {

    }

    public Tank(RobotController rc) {
        super(rc);
        whatAmI = TANK;
        reportAlive();
    }

    @Override
    public void run() {
        try {
            Team enemy = getRc().getTeam().opponent();

            while (isRunning()) {
                if(getRc().getHealth() < (getMaxHealth() / 10)) {
                    reportDeath();
                }

                incrementAge();
                
                // Try/catch blocks stop unhandled exceptions, which cause your robot to explode
                try {
                    MapLocation myLocation = getRc().getLocation();

                    // See if there are any nearby enemy robots
                    RobotInfo[] robots = getRc().senseNearbyRobots(-1, enemy);

                    // If there are some...
                    if (robots.length > 0) {
                        // And we have enough bullets, and haven't attacked yet this turn...
                        if (getRc().canFireSingleShot()) {
                            // ...Then fire a bullet in the direction of the enemy.
                            getRc().fireSingleShot(getRc().getLocation().directionTo(robots[0].location));
                        }
                    }

                    // Move randomly
                    tryMove(randomDirection());

                    // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
                    Clock.yield();

                } catch (Exception e) {
                    System.out.println("Soldier Exception");
                    e.printStackTrace();
                }
                
                
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            reportDeath();
        }
    }

    @Override
    public void reportAlive() {
        super.reportAlive();
        System.out.println("Gardener alive.");
    }

    @Override
    public void reportDeath() {
        super.reportDeath();
        System.out.println("Gardener dead: #YOLO.");
    }
}
