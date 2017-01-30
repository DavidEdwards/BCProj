package SamsTestPlayer;

import battlecode.common.*;

public class Archon extends Robot {

    public Archon(RobotController rc) {
        super(rc);
        whatAmI = ARCHON;
        reportAlive();
    }

    @Override
    public void run() {
        try {
            Direction Archondir = Direction.getNorth();

            while (isRunning()) {
                if (getRc().getHealth() < (getMaxHealth() / 10)) {
                    reportDeath();
                }

                // Try/catch blocks stop unhandled exceptions, which cause your robot to explode
                try {
                    Archondir = Archondir.rotateRightDegrees(45);
                    tryMove(Archondir);
                    checkForLeader(getRc().getRoundNum(), getRc().readBroadcast(LEADER), 10);
                    if (isLeader()) {
                        LeaderCode();
                    }


                    // Generate a random direction
                    Direction dir = randomDirection();

                    // Randomly attempt to build a gardener in this direction
                    if (getRc().getBuildCooldownTurns() == 0) {
                        if (getRc().getTeamBullets() > 250 && getRc().canBuildRobot(RobotType.GARDENER, dir)) {
                            getRc().hireGardener(dir);

                        }
                    }

                    //wander();

                    // Broadcast archon's location for other robots on the team to know
                    MapLocation myLocation = getRc().getLocation();
                    getRc().broadcast(0, (int) myLocation.x);
                    getRc().broadcast(1, (int) myLocation.y);

                    // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
                    Clock.yield();
                } catch (Exception e) {
                    System.out.println("Archon Exception");
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
        System.out.println("Archon alive.");
    }

    @Override
    public void reportDeath() {
        super.reportDeath();
        System.out.println("Archon dead: #YOLO.");
    }
}
