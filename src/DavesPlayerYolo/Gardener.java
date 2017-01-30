package DavesPlayerYolo;

import battlecode.common.*;

public class Gardener extends Robot {

    private static int gardenerCount = 0;

    public Gardener(RobotController rc) {
        super(rc);
        gardenerCount++;
        System.out.println("Make a new gardener: count: "+gardenerCount);
    }

    @Override
    public void run() {
        try {
            setMaxHealth(getRc().getHealth());
            while (isRunning()) {
                if(getRc().getHealth() < (getMaxHealth() / 10)) {
                    reportDeath();
                }

                // Listen for home archon's location
                int xPos = getRc().readBroadcast(0);
                int yPos = getRc().readBroadcast(1);
                MapLocation archonLoc = new MapLocation(xPos,yPos);

                // Generate a random direction
                Direction dir = randomDirection();

                // Randomly attempt to build a soldier or lumberjack in this direction
                if (getRc().canBuildRobot(RobotType.GARDENER, dir)) {
                    getRc().buildRobot(RobotType.GARDENER, dir);
                }

                // Move randomly
                tryMove(randomDirection());

                // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
                Clock.yield();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            reportDeath();
        }
    }

    @Override
    public void reportDeath() {
        if(isDeathReported) return;

        isDeathReported = true;
        gardenerCount--;
        System.out.println("Gardener dead: #YOLO: count: " + gardenerCount);
    }

    public static int getGardenerCount() {
        return gardenerCount;
    }
}
