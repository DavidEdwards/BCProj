package SamsTestPlayer;

import battlecode.common.*;

import java.util.ArrayList;
import java.util.Random;

public abstract class Robot implements Runnable {
    public static int MAX_GARDENERS = 5;
    public static int MAX_LUMBERJACKS = 5;
    public static int MAX_SOLDIERS = 5;

    public static int GARDENER = 3;
    public static int LUMBERJACK = 4;
    public static int ARCHON = 5;
    public static int LEADER = 6;
    public static int SHOULD_START_HARVESTING = 7;
    public static int SCOUT = 8;
    public static int SOLDIER = 9;
    public static int TANK = 10;
    public static int MAX_GARDENERS_CHANNEL = 11;
    public static int MAX_LUMBERJACKS_CHANNEL = 12;
    public static int MAX_SOLDIERS_CHANNEL = 13;


    protected MapLocation[] enemyArchonStart = null;
    protected MapLocation targetEnemyArchonStart = null;
    protected MapLocation lastLocation = null;

    private RobotController rc = null;

    protected Random rand = new Random();
    protected boolean isDeathReported = false;
    private boolean running = true;
    private float maxHealth = 0f;
    private boolean isLeader = false;
    protected int whatAmI = 0;
    private int age = 0;

    static int ENEMY_ARCHON_LOG_START = 500;

    public Robot(RobotController rc) {
        this.rc = rc;
        setMaxHealth(getRc().getHealth());
    }

    @Override
    public abstract void run();

    public static int getTotalAlive(RobotController rc, int type) {
        try {
            return rc.readBroadcast(type);
        } catch (GameActionException e) {
            e.printStackTrace();
        }
        return 0;
    }

    protected void checkForLeader(int RoundNum, int broadcastRound, int turnDifference) throws GameActionException {
        //check if round 0 and round broadcast channel is 0 if so become leader
        if(broadcastRound == 0){
            isLeader = true;
            getRc().broadcast(LEADER, RoundNum);
        }

        //check if broadcastRound is turnDifference greater than RoundNum if so become leader
        if((RoundNum - broadcastRound) > turnDifference){
            isLeader = true;
            getRc().broadcast(LEADER, RoundNum);
        }
    }

    protected void LeaderCode() throws GameActionException {

        if(getMaxGardeners() == 0)
        {
            getRc().broadcast(MAX_GARDENERS_CHANNEL, 1);
        }

        if(getRc().getRoundNum() > 100){
            getRc().broadcast(MAX_GARDENERS_CHANNEL, 3);
        }

        if(getRc().getRoundNum() > 300){
            getRc().broadcast(MAX_GARDENERS_CHANNEL, 5);
        }

        TreeInfo[] trees = getRc().senseNearbyTrees();
        int count = 0;
        for (TreeInfo t : trees) {
            if(!t.getTeam().isPlayer()){
                count++;
            }
        }
        if(count < 5){
            getRc().broadcast(SHOULD_START_HARVESTING,1);
        }

        //Victory point code
        float bulls = getRc().getTeamBullets();
        int vp = getRc().getTeamVictoryPoints();
        if((bulls/10) + vp > 1000){
            getRc().donate(bulls);
        }

        if(getRc().getTeamBullets() > 1000){
            getRc().donate(500);
        }
    }

    protected ArrayList EnemyArchonLocations() throws GameActionException {
        ArrayList Locations = new ArrayList(0);

        for (int i = ENEMY_ARCHON_LOG_START; i < ENEMY_ARCHON_LOG_START + 20; i = i + 3) {
            if (getRc().readBroadcast(i) == 0) {
                return Locations;
            } else if (getRc().readBroadcast(i) > 0) {
                MapLocation ArcLoc = new MapLocation((float)getRc().readBroadcast(i + 1),(float)getRc().readBroadcast(i + 2));
                Locations.add(ArcLoc);
            }
        }

        return Locations;
    }

    protected void LogEnemyArchonLocation(RobotInfo robot) throws GameActionException {
        try {
            for (int i = ENEMY_ARCHON_LOG_START; i < ENEMY_ARCHON_LOG_START + 20; i = i + 3) {
                if (getRc().readBroadcast(i) == 0) {
                    getRc().broadcast(i, robot.getID());
                    getRc().broadcast(i + 1, (int) robot.getLocation().x);
                    getRc().broadcast(i + 2, (int) robot.getLocation().y);
                    break;
                } else if (getRc().readBroadcast(i) == robot.getID()) {
                    getRc().broadcast(i + 1, (int) robot.getLocation().x);
                    getRc().broadcast(i + 2, (int) robot.getLocation().y);
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getMaxGardeners() {
        try {
            return getRc().readBroadcast(MAX_GARDENERS_CHANNEL);
        } catch (GameActionException e) {
            e.printStackTrace();
        }
        return 0;
    }

    protected float getRange(MapLocation Target) {
        return getRc().getLocation().distanceTo(Target);
    }
    
    public void reportAlive() {
        try {
            getRc().broadcast(whatAmI,getRc().readBroadcast(whatAmI) + 1);
        } catch (GameActionException e) {
            e.printStackTrace();
        }
    }

    public void reportDeath() {
        if(isDeathReported) return;

        isDeathReported = true;
        try {
            getRc().broadcast(whatAmI,getRc().readBroadcast(whatAmI) - 1);
        } catch (GameActionException e) {
            e.printStackTrace();
        }
    }

    public RobotInfo FindRobotType(RobotInfo robots[] , RobotType type) {
        for (RobotInfo r : robots) {
            if(r.getType() == type) {
                return r;
            }
        }

        return null;
    }

    /**
     * Returns a random Direction
     * @return a random Direction
     */
    static Direction randomDirection() {
        return new Direction((float)Math.random() * 2 * (float)Math.PI);
    }

    /**
     * Attempts to move in a given direction, while avoiding small obstacles directly in the path.
     *
     * @param dir The intended direction of movement
     * @return true if a move was performed
     * @throws GameActionException
     */
    protected boolean tryMove(Direction dir) throws GameActionException {
        return tryMove(dir,20,3);
    }

    /**
     * Attempts to move in a given direction, while avoiding small obstacles direction in the path.
     *
     * @param dir The intended direction of movement
     * @param degreeOffset Spacing between checked directions (degrees)
     * @param checksPerSide Number of extra directions checked on each side, if intended direction was unavailable
     * @return true if a move was performed
     * @throws GameActionException
     */
    protected boolean tryMove(Direction dir, float degreeOffset, int checksPerSide) throws GameActionException {

        // First, try intended direction
        if (getRc().canMove(dir)) {
            getRc().move(dir);
            return true;
        }

        // Now try a bunch of similar angles
        boolean moved = false;
        int currentCheck = 1;

        while(currentCheck<=checksPerSide) {
            // Try the offset of the left side
            if(getRc().canMove(dir.rotateLeftDegrees(degreeOffset*currentCheck))) {
                lastLocation = rc.getLocation();
                getRc().move(dir.rotateLeftDegrees(degreeOffset*currentCheck));
                return true;
            }
            // Try the offset on the right side
            if(getRc().canMove(dir.rotateRightDegrees(degreeOffset*currentCheck))) {
                lastLocation = rc.getLocation();
                getRc().move(dir.rotateRightDegrees(degreeOffset*currentCheck));
                return true;
            }
            // No move performed, try slightly further
            currentCheck++;
        }

        // A move never happened, so return false.
        return false;
    }

    /**
     * A slightly more complicated example function, this returns true if the given bullet is on a collision
     * course with the current robot. Doesn't take into account objects between the bullet and this robot.
     *
     * @param bullet The bullet in question
     * @return True if the line of the bullet's path intersects with this robot's current position.
     */
    protected boolean willCollideWithMe(BulletInfo bullet) {
        MapLocation myLocation = getRc().getLocation();

        // Get relevant bullet information
        Direction propagationDirection = bullet.dir;
        MapLocation bulletLocation = bullet.location;

        // Calculate bullet relations to this robot
        Direction directionToRobot = bulletLocation.directionTo(myLocation);
        float distToRobot = bulletLocation.distanceTo(myLocation);
        float theta = propagationDirection.radiansBetween(directionToRobot);

        // If theta > 90 degrees, then the bullet is traveling away from us and we can break early
        if (Math.abs(theta) > Math.PI/2) {
            return false;
        }

        // distToRobot is our hypotenuse, theta is our angle, and we want to know this length of the opposite leg.
        // This is the distance of a line that goes from myLocation and intersects perpendicularly with propagationDirection.
        // This corresponds to the smallest radius circle centered at our location that would intersect with the
        // line that is the path of the bullet.
        float perpendicularDist = (float)Math.abs(distToRobot * Math.sin(theta)); // soh cah toa :)

        return (perpendicularDist <= getRc().getType().bodyRadius);
    }

    public RobotController getRc() {
        return rc;
    }

    public void setRc(RobotController rc) {
        this.rc = rc;
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public float getMaxHealth() {
        return maxHealth;
    }

    public void setMaxHealth(float maxHealth) {
        this.maxHealth = maxHealth;
    }

    public boolean isLeader() {
        return isLeader;
    }

    public int getAge() {
        return age;
    }

    public void incrementAge() {
        age++;
    }
}
