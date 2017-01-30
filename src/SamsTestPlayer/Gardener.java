package SamsTestPlayer;

import battlecode.common.*;

public class Gardener extends Robot {

    public enum TaskList {
        MoveAwayFromArchon, GoodPlaceToFarm, BadPlaceToFarm, None
    }

    private boolean isHarvestMode = false;

    //variables
    private Direction BUILD_DIRECTION = null;
    private Direction Direction_to_move = null;

    public Gardener(RobotController rc) {
        super(rc);
        whatAmI = GARDENER;
        reportAlive();
    }

    @Override
    public void run() {
        try {
            while (isRunning()) {
                if(getRc().getHealth() < (getMaxHealth() / 10)) {
                    reportDeath();
                }

                incrementAge();
                // Try/catch blocks stop unhandled exceptions, which cause your robot to explode
                try {
                    checkForLeader(getRc().getRoundNum(), getRc().readBroadcast(LEADER), 15);
                    if(isLeader()){
                        LeaderCode();
                    }

                    if(isHarvestMode){
                        HarvestMode();
                    }else{
                        if(getAge() == 1){
                            if (getRc().getTeamBullets() > 50 && getRc().getBuildCooldownTurns() == 0) {
                                for (float i = 0; i < 6.2; i = i + (float) 0.2) {
                                    Direction TempDir = new Direction(i);
                                    if (getRc().canBuildRobot(RobotType.LUMBERJACK, TempDir)) {
                                        getRc().buildRobot(RobotType.LUMBERJACK, TempDir);
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
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            reportDeath();
        }
    }


    private void SearchForLocation() throws GameActionException {
        RobotInfo[] robots = getRc().senseNearbyRobots();

        Team enemy = getRc().getTeam().opponent();

        int NUM_OF_ENEMY_ARCHON = 0;
        int NUM_OF_ENEMY_GARDENER = 0;
        int NUM_OF_ENEMY_SOLDIER = 0;
        int NUM_OF_ENEMY_LUMBERJACK = 0;
        int NUM_OF_ENEMY_SCOUT = 0;
        int NUM_OF_ENEMY_TANK = 0;

        RobotInfo FRIEND_ARCHON = null;

        TaskList RobotTask = TaskList.None; //no task picked yet this turn

        for (RobotInfo r : robots) {
            switch (r.getType()) {
                case ARCHON:
                    //broadcast archon location
                    if(r.getTeam() == enemy){
                        NUM_OF_ENEMY_ARCHON++;
                        LogEnemyArchonLocation(r);
                    }else{
                        if(getAge() < 50){
                            RobotTask = TaskList.MoveAwayFromArchon;
                        }

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
        if(RobotTask == TaskList.None) {

            boolean IS_THERE_ROOM = true;

            enemyArchonStart = getRc().getInitialArchonLocations(enemy);
            if(enemyArchonStart.length == 1){
                targetEnemyArchonStart = enemyArchonStart[0];
            }else{
                int randomNum = rand.nextInt(enemyArchonStart.length - 1);
                targetEnemyArchonStart = enemyArchonStart[randomNum];
            }
            BUILD_DIRECTION = getRc().getLocation().directionTo(targetEnemyArchonStart);


            Direction TREE2 = BUILD_DIRECTION.rotateRightDegrees(72);
            Direction TREE3 = BUILD_DIRECTION.rotateLeftDegrees(72);
            Direction TREE4 = BUILD_DIRECTION.rotateRightDegrees(144);
            Direction TREE5 = BUILD_DIRECTION.rotateLeftDegrees(144);

            if(getRc().canPlantTree(BUILD_DIRECTION)){

                getRc().setIndicatorDot(getRc().getLocation().add(BUILD_DIRECTION), 0, 255, 0);

            }else{
                getRc().setIndicatorDot(getRc().getLocation().add(BUILD_DIRECTION), 0, 0, 255);
                IS_THERE_ROOM = false;
            }

            if(getRc().canPlantTree(TREE2)){
                getRc().setIndicatorDot(getRc().getLocation().add(TREE2), 255, 0, 0);

            }else{
                getRc().setIndicatorDot(getRc().getLocation().add(TREE2), 0, 0, 255);
                IS_THERE_ROOM = false;
            }

            if(getRc().canPlantTree(TREE3)){
                getRc().setIndicatorDot(getRc().getLocation().add(TREE3), 255, 0, 0);

            }else{
                getRc().setIndicatorDot(getRc().getLocation().add(TREE3), 0, 0, 255);
                IS_THERE_ROOM = false;
            }

            if(getRc().canPlantTree(TREE4)){
                getRc().setIndicatorDot(getRc().getLocation().add(TREE4), 255, 0, 0);

            }else{
                getRc().setIndicatorDot(getRc().getLocation().add(TREE4), 0, 0, 255);
                IS_THERE_ROOM = false;
            }

            if(getRc().canPlantTree(TREE5)){
                getRc().setIndicatorDot(getRc().getLocation().add(TREE5), 255, 0, 0);

            }else{
                getRc().setIndicatorDot(getRc().getLocation().add(TREE5), 0, 0, 255);
                IS_THERE_ROOM = false;
            }

            if(IS_THERE_ROOM){
                System.out.println("Is Room");
                RobotTask = TaskList.GoodPlaceToFarm;

            }else{
                System.out.println("No Room");
                RobotTask = TaskList.BadPlaceToFarm;
            }

        }


        switch (RobotTask) {
            case MoveAwayFromArchon:
                Direction AwayFromArchon = getRc().getLocation().directionTo(FRIEND_ARCHON.getLocation()).opposite();
                tryMove(AwayFromArchon, 15, 12);
                Direction_to_move = AwayFromArchon;
                break;
            case GoodPlaceToFarm:
                isHarvestMode = true;
                if (getRc().getTeamBullets() > 50 && getRc().getBuildCooldownTurns() == 0) {
                    if (getRc().canBuildRobot(RobotType.SCOUT, BUILD_DIRECTION)) {
                        getRc().buildRobot(RobotType.SCOUT, BUILD_DIRECTION);
                    }
                }
                break;
            case BadPlaceToFarm:
                if(getRc().canMove(Direction_to_move)){
                    tryMove(Direction_to_move, 15, 12);
                }else{
                    Direction_to_move = randomDirection();
                    tryMove(Direction_to_move, 15, 12);
                }
                break;
        }
    }

    private void HarvestMode() throws GameActionException {
        try {


            if(getRc().readBroadcast(LUMBERJACK) < Robot.MAX_LUMBERJACKS){
                if (getRc().getTeamBullets() > 50 && getRc().getBuildCooldownTurns() == 0)
                    if (getRc().canBuildRobot(RobotType.LUMBERJACK, BUILD_DIRECTION)) {
                        getRc().buildRobot(RobotType.LUMBERJACK, BUILD_DIRECTION);
                    }
            }

            if(getRc().getRoundNum() > 100) {
                if (getRc().readBroadcast(SOLDIER) < Robot.MAX_SOLDIERS) {
                    if (getRc().getTeamBullets() > 50 && getRc().getBuildCooldownTurns() == 0)
                        if (getRc().canBuildRobot(RobotType.SOLDIER, BUILD_DIRECTION)) {
                            getRc().buildRobot(RobotType.SOLDIER, BUILD_DIRECTION);
                        }
                }
            }

            TreeInfo[] trees = getRc().senseNearbyTrees((float)1.5,getRc().getTeam());
            if (trees.length < 4) {
                Direction TREE2 = BUILD_DIRECTION.rotateRightDegrees(72);
                Direction TREE3 = BUILD_DIRECTION.rotateLeftDegrees(72);
                Direction TREE4 = BUILD_DIRECTION.rotateRightDegrees(144);
                Direction TREE5 = BUILD_DIRECTION.rotateLeftDegrees(144);

                if(getRc().canPlantTree(TREE2)){
                    getRc().setIndicatorDot(getRc().getLocation().add(TREE2), 255, 0, 0);
                    getRc().plantTree(TREE2);
                }

                if(getRc().canPlantTree(TREE3)){
                    getRc().setIndicatorDot(getRc().getLocation().add(TREE3), 255, 0, 0);
                    getRc().plantTree(TREE3);
                }

                if(getRc().canPlantTree(TREE4)){
                    getRc().setIndicatorDot(getRc().getLocation().add(TREE4), 255, 0, 0);
                    getRc().plantTree(TREE4);
                }

                if(getRc().canPlantTree(TREE5)){
                    getRc().setIndicatorDot(getRc().getLocation().add(TREE5), 255, 0, 0);
                    getRc().plantTree(TREE5);
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
                if (getRc().canWater(TargetWaterTree.getID())) {
                    getRc().water(TargetWaterTree.getID());
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
                if (getRc().canShake(TargetShakeTree.getID())) {
                    getRc().shake(TargetShakeTree.getID());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
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
