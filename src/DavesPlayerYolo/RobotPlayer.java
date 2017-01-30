package DavesPlayerYolo;
import battlecode.common.*;
import java.util.*;

/**
 * Totally not stolen from SamsTestPlayer
 */
public strictfp class RobotPlayer {
    static RobotController rc;
    static Random myRand;
    /**
     * run() is the method that is called when a robot is instantiated in the Battlecode world.
     * If this method returns, the robot dies!
    **/
    @SuppressWarnings("unused")

    //channels
    static int LEADER_TRACKING_CHANNEL = 3;
    static int GARDENER_COUNT_CHANNEL = 4;
    static int LUMBERJACK_COUNT_CHANNEL = 5;
    static int SHOULD_START_HARVESTING = 6;

    //will use channels upto ENEMY_ARCHON_LOG_START + 20
    static int ENEMY_ARCHON_LOG_START = 500;

    //variables
    static MapLocation[] EnemyArchonStart = null;
    static MapLocation TargetEnemyArchonStart = null;
    static MapLocation MY_LAST_LOCATION = null;

    static int AGE = 0;
    static boolean AM_LEADER = false;
    static boolean HARVEST_MODE = false;
    static Direction Direction_to_move = null;
    static Direction BUILD_DIRECTION = null;




    static int LEADER_TURNS = 0;

    static int GARDENER_MAX = 100;
    static int Num_of_Moves = 0;




    public enum ScoutTaskList{
        KillScout, KillGardener, Orbit, Shake, Search, None
    }

    public enum GardenerTaskList{
        moveawayfromarchon, goodplacetofarm, badplacetofarm, None
    }


    //public enum rcstate{none, chop, shake}

    public static void run(RobotController rc) throws GameActionException {

        // This is the RobotController object. You use it to perform actions from this robot,
        // and to get information on its current status.
        RobotPlayer.rc = rc;
        myRand = new Random(rc.getID());

        // Here, we've separated the controls into a different method for each RobotType.
        // You can add the missing ones or rewrite this into your own control structure.
        switch (rc.getType()) {
            case ARCHON:
                runArchon();
                break;
            case GARDENER:
                new Gardener(rc).run();
                break;
            case SOLDIER:
                runSoldier();
                break;
            case LUMBERJACK:
                runLumberjack();
                break;
            case SCOUT:
                runScout();
                break;
            case TANK:
                runTank();
                break;
        }
	}


    static void checkForLeader(int RoundNum, int broadcastRound, int turnDifference) throws GameActionException {

        //check if round 0 and round broadcast channel is 0 if so become leader
        if(broadcastRound == 0){
            AM_LEADER =  true;
            rc.broadcast(LEADER_TRACKING_CHANNEL,RoundNum);
        }
        //check if broadcastRound is turnDifference greater than RoundNum if so become leader
        if((RoundNum - broadcastRound) > turnDifference){
            AM_LEADER =  true;
            rc.broadcast(LEADER_TRACKING_CHANNEL,RoundNum);
        }
    }

    static void LeaderCode() throws GameActionException {

        TreeInfo[] trees = rc.senseNearbyTrees();
        int count = 0;
        for (TreeInfo t : trees) {
            if(!t.getTeam().isPlayer()){
                count++;
            }
        }
        if(count < 5){
            rc.broadcast(SHOULD_START_HARVESTING,1);
        }


        //Victory point code
        float bulls = rc.getTeamBullets();
        int vp = rc.getTeamVictoryPoints();
        if((bulls/10) + vp > 1000){
            rc.donate(bulls);
        }

        if(rc.getTeamBullets() > 1000){
            rc.donate(500);
        }


    }

    static void runArchon() throws GameActionException {
        System.out.println("I'm an archon!");
        Direction Archondir = Direction.getNorth();
        // The code you want your robot to perform every round should be in this loop
        while (true) {

            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode
            try {
                Archondir = Archondir.rotateRightDegrees(45);
                tryMove(Archondir);
                checkForLeader(rc.getRoundNum(), rc.readBroadcast(LEADER_TRACKING_CHANNEL), 10);
                if(AM_LEADER == true){
                    LeaderCode();
                }


                // Generate a random direction
                Direction dir = randomDirection();

                // Randomly attempt to build a gardener in this direction
                if(rc.getBuildCooldownTurns() == 0){
                    if (rc.getTeamBullets() > 250 && rc.canBuildRobot(RobotType.GARDENER, dir)) {
                        rc.hireGardener(dir);

                    }
                }

                //wander();

                // Broadcast archon's location for other robots on the team to know
                MapLocation myLocation = rc.getLocation();
                rc.broadcast(0,(int)myLocation.x);
                rc.broadcast(1,(int)myLocation.y);


                // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
                Clock.yield();

            } catch (Exception e) {
                System.out.println("Archon Exception");
                e.printStackTrace();
            }
        }
    }

	static void runGardener() throws GameActionException {
        System.out.println("I'm a gardener!");
        rc.broadcast(GARDENER_COUNT_CHANNEL,rc.readBroadcast(GARDENER_COUNT_CHANNEL) + 1);

        Team enemy = rc.getTeam().opponent();
        // The code you want your robot to perform every round should be in this loop
        while (true) {
            AGE++;
            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode
            try {
                checkForLeader(rc.getRoundNum(), rc.readBroadcast(LEADER_TRACKING_CHANNEL), 15);
                if(AM_LEADER == true){
                    LeaderCode();
                }

                if(HARVEST_MODE){
                    HarvestMode();
                }else{
                    if(AGE == 1){
                        if (rc.getTeamBullets() > 50 && rc.getBuildCooldownTurns() == 0) {
                            for (float i = 0; i < 6.2; i = i + (float) 0.2) {
                                Direction TempDir = new Direction(i);
                                if (rc.canBuildRobot(RobotType.LUMBERJACK, TempDir)) {
                                    rc.buildRobot(RobotType.LUMBERJACK, TempDir);
                                    break;
                                }
                            }
                        }
                    }

                    SearchForLocation();


                }


                // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
                Clock.yield();

            } catch (Exception e) {
                System.out.println("Gardener Exception");
                e.printStackTrace();
            }
        }
    }

    public static void SearchForLocation() throws GameActionException {
        RobotInfo[] robots = rc.senseNearbyRobots();

        Team enemy = rc.getTeam().opponent();

        int NUM_OF_ENEMY_ARCHON = 0;
        int NUM_OF_ENEMY_GARDENER = 0;
        int NUM_OF_ENEMY_SOLDIER = 0;
        int NUM_OF_ENEMY_LUMBERJACK = 0;
        int NUM_OF_ENEMY_SCOUT = 0;
        int NUM_OF_ENEMY_TANK = 0;

        RobotInfo FRIEND_ARCHON = null;

        GardenerTaskList RobotTask = GardenerTaskList.None; //no task picked yet this turn

        for (RobotInfo r : robots) {
            switch (r.getType()) {
                case ARCHON:
                    //broadcast archon location
                    if(r.getTeam() == enemy){
                        NUM_OF_ENEMY_ARCHON++;
                        LogEnemyArchonLocation(r);
                    }else{
                        RobotTask = GardenerTaskList.moveawayfromarchon;
                        FRIEND_ARCHON = r;
                    }
                    break;
                case GARDENER:
                    if(r.getTeam() == enemy){
                        NUM_OF_ENEMY_GARDENER++;
                    }else{

                    }
                    break;
                case SOLDIER:
                    if(r.getTeam() == enemy){
                        NUM_OF_ENEMY_SOLDIER++;
                    }else{

                    }
                    break;
                case LUMBERJACK:
                    if(r.getTeam() == enemy){
                        NUM_OF_ENEMY_LUMBERJACK++;
                    }else{

                    }
                    break;
                case SCOUT:
                    if(r.getTeam() == enemy){
                        NUM_OF_ENEMY_SCOUT++;
                    }else{

                    }
                    break;
                case TANK:
                    if(r.getTeam() == enemy){
                        NUM_OF_ENEMY_TANK++;
                    }else{

                    }
                    break;
            }

        }

        //2: check if here is a good spot for a garden
        if(RobotTask == GardenerTaskList.None) {

            boolean IS_THERE_ROOM = true;

            EnemyArchonStart = rc.getInitialArchonLocations(enemy);
            if(EnemyArchonStart.length == 1){
                TargetEnemyArchonStart = EnemyArchonStart[0];
            }else{
                int randomNum = myRand.nextInt(EnemyArchonStart.length - 1);
                TargetEnemyArchonStart = EnemyArchonStart[randomNum];
            }
            BUILD_DIRECTION = rc.getLocation().directionTo(TargetEnemyArchonStart);


            Direction TREE2 = BUILD_DIRECTION.rotateRightDegrees(72);
            Direction TREE3 = BUILD_DIRECTION.rotateLeftDegrees(72);
            Direction TREE4 = BUILD_DIRECTION.rotateRightDegrees(144);
            Direction TREE5 = BUILD_DIRECTION.rotateLeftDegrees(144);

            if(rc.canPlantTree(BUILD_DIRECTION)){

                rc.setIndicatorDot(rc.getLocation().add(BUILD_DIRECTION), 0, 255, 0);

            }else{
                rc.setIndicatorDot(rc.getLocation().add(BUILD_DIRECTION), 0, 0, 255);
                IS_THERE_ROOM = false;
            }

            if(rc.canPlantTree(TREE2)){
                rc.setIndicatorDot(rc.getLocation().add(TREE2), 255, 0, 0);

            }else{
                rc.setIndicatorDot(rc.getLocation().add(TREE2), 0, 0, 255);
                IS_THERE_ROOM = false;
            }

            if(rc.canPlantTree(TREE3)){
                rc.setIndicatorDot(rc.getLocation().add(TREE3), 255, 0, 0);

            }else{
                rc.setIndicatorDot(rc.getLocation().add(TREE3), 0, 0, 255);
                IS_THERE_ROOM = false;
            }

            if(rc.canPlantTree(TREE4)){
                rc.setIndicatorDot(rc.getLocation().add(TREE4), 255, 0, 0);

            }else{
                rc.setIndicatorDot(rc.getLocation().add(TREE4), 0, 0, 255);
                IS_THERE_ROOM = false;
            }

            if(rc.canPlantTree(TREE5)){
                rc.setIndicatorDot(rc.getLocation().add(TREE5), 255, 0, 0);

            }else{
                rc.setIndicatorDot(rc.getLocation().add(TREE5), 0, 0, 255);
                IS_THERE_ROOM = false;
            }

            if(IS_THERE_ROOM){
                System.out.println("Is Room");
                RobotTask = GardenerTaskList.goodplacetofarm;

            }else{
                System.out.println("No Room");
                RobotTask = GardenerTaskList.badplacetofarm;
            }

        }


        switch (RobotTask) {
            case moveawayfromarchon:
                Direction AwayFromArchon = rc.getLocation().directionTo(FRIEND_ARCHON.getLocation()).opposite();
                tryMove(AwayFromArchon, 15, 12);
                Direction_to_move = AwayFromArchon;
                break;
            case goodplacetofarm:
                HARVEST_MODE = true;
                if (rc.getTeamBullets() > 50 && rc.getBuildCooldownTurns() == 0) {
                if (rc.canBuildRobot(RobotType.SCOUT, BUILD_DIRECTION)) {
                    rc.buildRobot(RobotType.SCOUT, BUILD_DIRECTION);
                }
            }
                break;
            case badplacetofarm:
                if(rc.canMove(Direction_to_move)){
                    tryMove(Direction_to_move, 15, 12);
                }else{
                    Direction_to_move = randomDirection();
                    tryMove(Direction_to_move, 15, 12);
                }
                break;
        }
    }

    public static void HarvestMode() throws GameActionException {
        try {

            int guardeners = rc.readBroadcast(GARDENER_COUNT_CHANNEL);
            int halfguard = guardeners/2;
            int lumbers = rc.readBroadcast(LUMBERJACK_COUNT_CHANNEL);


            if( halfguard > lumbers){
                if (rc.getTeamBullets() > 50 && rc.getBuildCooldownTurns() == 0)
                    if (rc.canBuildRobot(RobotType.LUMBERJACK, BUILD_DIRECTION)) {
                        rc.buildRobot(RobotType.LUMBERJACK, BUILD_DIRECTION);
                    }
            }


            TreeInfo[] trees = rc.senseNearbyTrees((float)1.5,rc.getTeam());
            if (trees.length < 4) {
                Direction TREE2 = BUILD_DIRECTION.rotateRightDegrees(72);
                Direction TREE3 = BUILD_DIRECTION.rotateLeftDegrees(72);
                Direction TREE4 = BUILD_DIRECTION.rotateRightDegrees(144);
                Direction TREE5 = BUILD_DIRECTION.rotateLeftDegrees(144);

                if(rc.canPlantTree(TREE2)){
                    rc.setIndicatorDot(rc.getLocation().add(TREE2), 255, 0, 0);
                    rc.plantTree(TREE2);
                }

                if(rc.canPlantTree(TREE3)){
                    rc.setIndicatorDot(rc.getLocation().add(TREE3), 255, 0, 0);
                    rc.plantTree(TREE3);
                }

                if(rc.canPlantTree(TREE4)){
                    rc.setIndicatorDot(rc.getLocation().add(TREE4), 255, 0, 0);
                    rc.plantTree(TREE4);
                }

                if(rc.canPlantTree(TREE5)){
                    rc.setIndicatorDot(rc.getLocation().add(TREE5), 255, 0, 0);
                    rc.plantTree(TREE5);
                }

            }
            //water the lowest HP tree in range

            float LowestTreeHP = 50;
            TreeInfo TargetWaterTree = null;

            for (TreeInfo t : trees) {
                if (t.health < LowestTreeHP) {
                    LowestTreeHP = t.health;
                    TargetWaterTree = t;
                }
            }

            if (TargetWaterTree != null) {
                if (rc.canWater(TargetWaterTree.getID())) {
                    rc.water(TargetWaterTree.getID());
                }
            }

            //look for trees to shake
            TreeInfo TargetShakeTree = null;
            int NumOfBull = 0;
            for (TreeInfo t : trees) {
                if (t.containedBullets > NumOfBull) {
                    NumOfBull = t.containedBullets;
                    TargetShakeTree = t;
                }
            }

            if (TargetShakeTree != null) {
                if (rc.canShake(TargetShakeTree.getID())) {
                    rc.shake(TargetShakeTree.getID());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static void runSoldier() throws GameActionException {
        System.out.println("I'm an soldier!");
        Team enemy = rc.getTeam().opponent();

        // The code you want your robot to perform every round should be in this loop
        while (true) {

            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode
            try {
                MapLocation myLocation = rc.getLocation();

                // See if there are any nearby enemy robots
                RobotInfo[] robots = rc.senseNearbyRobots(-1, enemy);

                // If there are some...
                if (robots.length > 0) {
                    // And we have enough bullets, and haven't attacked yet this turn...
                    if (rc.canFireSingleShot()) {
                        // ...Then fire a bullet in the direction of the enemy.
                        rc.fireSingleShot(rc.getLocation().directionTo(robots[0].location));
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
    }

    static void runLumberjack() throws GameActionException {
        System.out.println("I'm a lumberjack!");
        Team enemy = rc.getTeam().opponent();
        rc.broadcast(LUMBERJACK_COUNT_CHANNEL,rc.readBroadcast(LUMBERJACK_COUNT_CHANNEL) + 1);
        // The code you want your robot to perform every round should be in this loop
        while (true) {

            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode
            try {



                // See if there are any enemy robots within striking range (distance 1 from lumberjack's radius)
                RobotInfo[] robots = rc.senseNearbyRobots(RobotType.LUMBERJACK.bodyRadius+GameConstants.LUMBERJACK_STRIKE_RADIUS, enemy);

                //check for nearby trees and attempt to chop them down. ~Movian
                TreeInfo[] Trees = rc.senseNearbyTrees();

                MapLocation myLocation = rc.getLocation();

                int action = 0;
                int noofbull = 4;

                TreeInfo TargetTree = null;
                if(Trees.length > 0) {
                    //check for robots or bullets in trees
                    for (int i = 0; i < Trees.length; i++) {
                        //Check if Tree Contains a Robot

                        if (Trees[i].containedBullets > noofbull) {
                            action = 2;
                            noofbull = Trees[i].containedBullets;
                            TargetTree = Trees[i];
                            break;
                        }else if (Trees[i].containedRobot != null || Trees[i].getTeam() != rc.getTeam()) {
                            //Tree contains a robot check if tree in range to chop.
                            action = 1;
                            TargetTree = Trees[i];
                            break;
                        }
                    }
                }

                System.out.println(action);

                //do something based on action
                switch (action) {
                    case 1:

                        if (rc.canChop(TargetTree.getID())) {
                            rc.chop(TargetTree.getID());
                            System.out.println("I am at loaction:" + myLocation + " Tried to Chop tree with ID:" + TargetTree.getID() + "at location:" + TargetTree.getLocation());
                        } else {
                            //Tree is not in range, move towards tree
                            Direction toTree = myLocation.directionTo(TargetTree.getLocation());
                            tryMove(toTree);
                            System.out.println("I am at loaction:" + myLocation + " Couldn't chop trying to move to tree ID:" + TargetTree.getID() + " at location:" + TargetTree.getLocation());

                        }
                        break;
                    case 2:

                        if (rc.canShake(TargetTree.getID())) {
                            rc.shake(TargetTree.getID());
                        } else {
                            Direction toTree = myLocation.directionTo(TargetTree.getLocation());
                            tryMove(toTree);
                            break;
                        }
                        break;
                    case 0:

                        tryMove(randomDirection());
                        break;
                    default:

                        tryMove(randomDirection());
                        break;
                }



                // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
                Clock.yield();

            } catch (Exception e) {
                System.out.println("Lumberjack Exception");
                e.printStackTrace();
            }
        }
    }

    static void runScout() throws GameActionException {
        System.out.println("I'm an scout!");
        Team enemy = rc.getTeam().opponent();
        EnemyArchonStart = rc.getInitialArchonLocations(enemy); //TargetEnemyArchonStart
        if(EnemyArchonStart.length == 1){
            TargetEnemyArchonStart = EnemyArchonStart[0];
        }else{
            int randomNum = myRand.nextInt(EnemyArchonStart.length - 1);
            TargetEnemyArchonStart = EnemyArchonStart[randomNum];
        }


        // The code you want your robot to perform every round should be in this loop
        while (true) {

            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode
            try {

                ScoutTaskList RobotTask = ScoutTaskList.None; //no task picked yet this turn

                MapLocation myLocation = rc.getLocation();

                // See if there are any nearby enemy robots
                RobotInfo[] robots = rc.senseNearbyRobots(-1, enemy);

                TreeInfo[] trees = rc.senseNearbyTrees();

                RobotInfo TargetRobot = null;
                RobotInfo ENEMY_ARCHON = null;
                RobotInfo ENEMY_JACK_TO_AVOID = null;

                TreeInfo TargetShakeTree = null;

                int NUM_OF_ENEMY_ARCHON = 0;
                int NUM_OF_ENEMY_GARDENER = 0;
                int NUM_OF_ENEMY_SOLDIER = 0;
                int NUM_OF_ENEMY_LUMBERJACK = 0;
                int NUM_OF_ENEMY_SCOUT = 0;
                int NUM_OF_ENEMY_TANK = 0;



                //prioritys:
                //1: enemy gardner or scout in range, kill it
                for (RobotInfo r : robots) {
                    switch (r.getType()) {
                        case ARCHON:
                            //broadcast archon location
                            NUM_OF_ENEMY_ARCHON++;
                            ENEMY_ARCHON = r;
                            LogEnemyArchonLocation(r);
                            break;
                        case GARDENER:
                            NUM_OF_ENEMY_GARDENER++;
                            if(RobotTask == ScoutTaskList.None){
                                RobotTask = ScoutTaskList.KillGardener;
                                TargetRobot = r;
                            }
                            break;
                        case SOLDIER:
                            NUM_OF_ENEMY_SOLDIER++;
                            break;
                        case LUMBERJACK:
                            NUM_OF_ENEMY_LUMBERJACK++;
                            System.out.println("Enemy lumberjack detected at range: " + getRange(r.getLocation()));
                            if(getRange(r.getLocation()) < 5){
                                ENEMY_JACK_TO_AVOID = r;
                            }
                            break;
                        case SCOUT:
                            NUM_OF_ENEMY_SCOUT++;
                            if (NUM_OF_ENEMY_SCOUT == 1) {
                                RobotTask = ScoutTaskList.KillScout;
                                TargetRobot = r;
                            }
                            break;
                        case TANK:
                            NUM_OF_ENEMY_TANK++;
                            break;
                    }

                }

                //2: enemy archon in range orbit it:
                if(RobotTask == ScoutTaskList.None){
                    if(ENEMY_ARCHON != null){
                        RobotTask = ScoutTaskList.Orbit;
                    }
                }

                //3: tree with bullets in range shake it
                if(RobotTask == ScoutTaskList.None){
                    //look for trees to shake
                    for (TreeInfo t : trees) {
                        if (t.containedBullets > 0) {
                            TargetShakeTree = t;
                            RobotTask = ScoutTaskList.Shake;
                            break;
                        }
                    }
                }

                //4: head toward enemy archon starting location(search mode)
                if(RobotTask == ScoutTaskList.None){
                    RobotTask = ScoutTaskList.Search;
                }

                switch (RobotTask) {
                    case Search:
                        System.out.println("Search");
                        ArrayList Archons = EnemyArchonLocations();
                        System.out.println("NUmber of known arcons: " + Archons.size());

                        if(Archons.isEmpty()){
                            Direction toTarget = myLocation.directionTo(TargetEnemyArchonStart);
                            tryMove(toTarget, 15, 12);
                        }else if(Archons.size() == 1){
                            MapLocation ArcLoc = (MapLocation) Archons.get(0);
                            Direction toTarget = myLocation.directionTo(ArcLoc);
                            tryMove(toTarget, 15, 12);
                        }else if(Archons.size() > 1){
                            int number = myRand.nextInt(Archons.size());
                            System.out.println("Random: " + number);

                            MapLocation ArcLoc = (MapLocation) Archons.get(number);
                            Direction toTarget = myLocation.directionTo(ArcLoc);
                            tryMove(toTarget, 15, 12);
                        }
                        break;
                    case Shake:
                        System.out.println("Shake");
                        if (TargetShakeTree != null) {
                            if (rc.canShake(TargetShakeTree.getID())) {
                                rc.shake(TargetShakeTree.getID());
                            }else{
                                //move toward tree
                                Direction toTree = myLocation.directionTo(TargetShakeTree.getLocation());
                                tryMove(toTree);
                            }
                        }
                        break;
                    case Orbit:
                        System.out.println("Orbit");
                        Direction toTargetArchon = myLocation.directionTo(ENEMY_ARCHON.getLocation());
                        tryMove(toTargetArchon, 15, 12);
                        break;
                    case KillScout:
                        System.out.println("KillScout");
                        Direction ToKillScout = myLocation.directionTo(TargetRobot.getLocation());

                        System.out.println("Range to target: " + getRange(TargetRobot.getLocation()));

                        if(getRange(TargetRobot.getLocation()) > 2.5){
                            tryMove(ToKillScout);
                            ToKillScout = myLocation.directionTo(TargetRobot.getLocation());
                        }

                        if (rc.canFireSingleShot()) {
                            // ...Then fire a bullet in the direction of the enemy.
                            rc.fireSingleShot(ToKillScout);
                        }
                        break;
                    case KillGardener:
                        System.out.println("KillGardener");
                        Direction ToKillGardener = myLocation.directionTo(TargetRobot.getLocation());

                        System.out.println("Range to target: " + getRange(TargetRobot.getLocation()));

                        if(getRange(TargetRobot.getLocation()) > 2.5){
                            tryMove(ToKillGardener);
                            ToKillGardener = myLocation.directionTo(TargetRobot.getLocation());
                        }

                        if (rc.canFireSingleShot()) {
                            // ...Then fire a bullet in the direction of the enemy.
                            rc.fireSingleShot(ToKillGardener);
                        }
                        break;
                }

                if (ENEMY_JACK_TO_AVOID != null & rc.hasMoved() == false){
                    Direction toTarget = myLocation.directionTo(ENEMY_JACK_TO_AVOID.getLocation());
                    tryMove(toTarget.rotateLeftDegrees(90), 15, 12);
                }

                // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
                Clock.yield();

            } catch (Exception e) {
                System.out.println("Scout Exception");
                e.printStackTrace();
            }
        }
    }

    public static void LogEnemyArchonLocation(RobotInfo robot) throws GameActionException {
        try {
            for (int i = ENEMY_ARCHON_LOG_START; i < ENEMY_ARCHON_LOG_START + 20; i = i + 3) {
                if (rc.readBroadcast(i) == 0) {
                    rc.broadcast(i, robot.getID());
                    rc.broadcast(i + 1, (int) robot.getLocation().x);
                    rc.broadcast(i + 2, (int) robot.getLocation().y);
                    break;
                } else if (rc.readBroadcast(i) == robot.getID()) {
                    rc.broadcast(i + 1, (int) robot.getLocation().x);
                    rc.broadcast(i + 2, (int) robot.getLocation().y);
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static ArrayList EnemyArchonLocations() throws GameActionException {

        ArrayList Locations = new ArrayList(0);

        for (int i = ENEMY_ARCHON_LOG_START; i < ENEMY_ARCHON_LOG_START + 20; i = i + 3) {
            if (rc.readBroadcast(i) == 0) {
                return Locations;
            } else if (rc.readBroadcast(i) > 0) {
                MapLocation ArcLoc = new MapLocation((float)rc.readBroadcast(i + 1),(float)rc.readBroadcast(i + 2));
                Locations.add(ArcLoc);
            }
        }

        return Locations;
    }

    static void runTank() throws GameActionException {
        System.out.println("I'm an tank!");
        Team enemy = rc.getTeam().opponent();

        // The code you want your robot to perform every round should be in this loop
        while (true) {

            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode
            try {
                MapLocation myLocation = rc.getLocation();

                // See if there are any nearby enemy robots
                RobotInfo[] robots = rc.senseNearbyRobots(-1, enemy);

                // If there are some...
                if (robots.length > 0) {
                    // And we have enough bullets, and haven't attacked yet this turn...
                    if (rc.canFireSingleShot()) {
                        // ...Then fire a bullet in the direction of the enemy.
                        rc.fireSingleShot(rc.getLocation().directionTo(robots[0].location));
                    }
                }

                // Move randomly
                tryMove(randomDirection());

                // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
                Clock.yield();

            } catch (Exception e) {
                System.out.println("Tank Exception");
                e.printStackTrace();
            }
        }
    }

    public static Direction randomDirection() {
        return(new Direction(myRand.nextFloat()*2*(float)Math.PI));
    }
    /**
     * Attempts to move in a given direction, while avoiding small obstacles directly in the path.
     *
     * @param dir The intended direction of movement
     * @return true if a move was performed
     * @throws GameActionException
     */
    static boolean tryMove(Direction dir) throws GameActionException {
        return tryMove(dir,15,12);
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
    static boolean tryMove(Direction dir, float degreeOffset, int checksPerSide) throws GameActionException {

        // First, try intended direction
        if (rc.canMove(dir)) {
            rc.move(dir);
            return true;
        }

        // Now try a bunch of similar angles
        boolean moved = false;
        int currentCheck = 1;

        while(currentCheck<=checksPerSide) {
            // Try the offset of the left side
            if(rc.canMove(dir.rotateLeftDegrees(degreeOffset*currentCheck))) {
                if(rc.getLocation().add(dir.rotateLeftDegrees(degreeOffset*currentCheck)).equals(MY_LAST_LOCATION)){
                    //dont move back to last location
                }else {
                    MY_LAST_LOCATION = rc.getLocation();
                    rc.move(dir.rotateLeftDegrees(degreeOffset * currentCheck));
                    return true;
                }
            }
            // Try the offset on the right side
            if(rc.canMove(dir.rotateRightDegrees(degreeOffset*currentCheck))) {
                if(rc.getLocation().add(dir.rotateRightDegrees(degreeOffset*currentCheck)).equals(MY_LAST_LOCATION)){
                    //dont move back to last location
                }else{
                    MY_LAST_LOCATION = rc.getLocation();
                    rc.move(dir.rotateRightDegrees(degreeOffset*currentCheck));
                    return true;
                }
            }
            // No move performed, try slightly further
            currentCheck++;
        }

        // A move never happened, so return false.
        return false;
    }

    static float getRange(MapLocation Target) {
        return rc.getLocation().distanceTo(Target);
    }
    /**
     * A slightly more complicated example function, this returns true if the given bullet is on a collision
     * course with the current robot. Doesn't take into account objects between the bullet and this robot.
     *
     * @param bullet The bullet in question
     * @return True if the line of the bullet's path intersects with this robot's current position.
     */
    static boolean willCollideWithMe(BulletInfo bullet) {
        MapLocation myLocation = rc.getLocation();

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

        return (perpendicularDist <= rc.getType().bodyRadius);
    }





    }



