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
            Team enemy = getRc().getTeam().opponent();
            enemyArchonStart = getRc().getInitialArchonLocations(enemy); //targetEnemyArchonStart
            if(enemyArchonStart.length == 1){
                targetEnemyArchonStart = enemyArchonStart[0];
            }else{
                int randomNum = rand.nextInt(enemyArchonStart.length - 1);
                targetEnemyArchonStart = enemyArchonStart[randomNum];
            }

            while (isRunning()) {
                if (getRc().getHealth() < (getMaxHealth() / 10)) {
                    reportDeath();
                }

                // Try/catch blocks stop unhandled exceptions, which cause your robot to explode
                try {
                    Archondir = Archondir.rotateRightDegrees(45);
                    tryMove(Archondir, 15, 12);
                    checkForLeader(getRc().getRoundNum(), getRc().readBroadcast(LEADER), 10);
                    if (isLeader()) {
                        LeaderCode();
                    }

                    // Generate a random direction
                    Direction dir = getRc().getLocation().directionTo(targetEnemyArchonStart);

                    // Randomly attempt to build a gardener in this direction
                    if (getRc().getTeamBullets() > 100 && getRc().getBuildCooldownTurns() == 0 && getRc().readBroadcast(GARDENER) < getMaxGardeners()) {
                        System.out.println("Trying to build");
                        for(int l=0; l<359; l=l+10){
                        //int l = 0;
                            System.out.println("Adding degrees: " + l);
                            if (getRc().canBuildRobot(RobotType.GARDENER, dir.rotateLeftDegrees(l)) )
                            {
                                System.out.println("Can build");
                                getRc().hireGardener(dir.rotateLeftDegrees(l));
                                //break;
                            }else{
                                System.out.println("Can not build");
                            }
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
