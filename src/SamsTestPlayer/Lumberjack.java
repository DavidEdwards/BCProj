package SamsTestPlayer;

import battlecode.common.*;

import java.util.*;

public class Lumberjack extends Robot {

    public enum TaskList {
        MoveToEnemyArchon, Shake, Chop, Strike, None
    }

    public Lumberjack(RobotController rc) {
        super(rc);
        whatAmI = LUMBERJACK;
        reportAlive();
    }

    @Override
    public void run() {
        try {
            Team enemy = getRc().getTeam().opponent();

            enemyArchonStart = getRc().getInitialArchonLocations(enemy); //targetEnemyArchonStart
            if(enemyArchonStart.length == 1){
                targetEnemyArchonStart = enemyArchonStart[0];
            }else{
                int randomNum = rand.nextInt(enemyArchonStart.length - 1);
                targetEnemyArchonStart = enemyArchonStart[randomNum];
            }
            
            while (isRunning()) {
                if(getRc().getHealth() < (getMaxHealth() / 10)) {
                    reportDeath();
                }

                incrementAge();

                // Try/catch blocks stop unhandled exceptions, which cause your robot to explode
                try {

                    // See if there are any enemy robots within striking range (distance 1 from lumberjack's radius)
                    RobotInfo[] EnemyRobots = getRc().senseNearbyRobots(RobotType.LUMBERJACK.bodyRadius+GameConstants.LUMBERJACK_STRIKE_RADIUS, enemy);
                    RobotInfo[] FriendlyRobots = getRc().senseNearbyRobots(RobotType.LUMBERJACK.bodyRadius+GameConstants.LUMBERJACK_STRIKE_RADIUS, getRc().getTeam());

                    TreeInfo[] Trees = getRc().senseNearbyTrees();
                    List<TreeInfo> treesList = new ArrayList<>();
                    for(TreeInfo tree : Trees) {
                        if(!tree.getTeam().isPlayer()) treesList.add(tree);
                    }
                    Trees = treesList.toArray(new TreeInfo[treesList.size()]);

                    TaskList RobotTask = TaskList.None;
                    RobotInfo TargetRobot = null;
                    TreeInfo TargetTree = null;

                    MapLocation myLocation = getRc().getLocation();

                    RobotInfo ENEMY_ARCHON = null;

                    int NUM_OF_ENEMY_ARCHON = 0;
                    int NUM_OF_ENEMY_GARDENER = 0;
                    int NUM_OF_ENEMY_SOLDIER = 0;
                    int NUM_OF_ENEMY_LUMBERJACK = 0;
                    int NUM_OF_ENEMY_SCOUT = 0;
                    int NUM_OF_ENEMY_TANK = 0;

                    //Task 1 Kill Scouts
                    for (RobotInfo r : EnemyRobots) {
                        switch (r.getType()) {
                            case ARCHON:
                                //broadcast archon location
                                NUM_OF_ENEMY_ARCHON++;
                                ENEMY_ARCHON = r;
                                LogEnemyArchonLocation(r);
                                break;
                            case GARDENER:
                                NUM_OF_ENEMY_GARDENER++;
                                break;
                            case SOLDIER:
                                //NUM_OF_ENEMY_SOLDIER++;
                                break;
                            case LUMBERJACK:
                                NUM_OF_ENEMY_LUMBERJACK++;
                                System.out.println("Enemy lumberjack detected at range: " + getRange(r.getLocation()));
                                break;
                            case SCOUT:
                                if (RobotTask == TaskList.None){
                                    RobotTask = TaskList.Strike;
                                    TargetRobot = r;
                                }
                                NUM_OF_ENEMY_SCOUT++;
                                break;
                            case TANK:
                                NUM_OF_ENEMY_TANK++;
                                break;
                        }

                    }

                    //Task 2 check for trees with interest

                    if(RobotTask == TaskList.None)
                    {
                        if(Trees.length > 0) {
                            TargetTree = Trees[0];
                            RobotTask = TaskList.Chop;
                        }
                    }

                    //3: tree with bullets in range shake it
                    if(RobotTask == TaskList.None){
                        //look for trees to shake
                        for (TreeInfo t : Trees) {
                            if (t.containedBullets > 0) {
                                TargetTree = t;
                                RobotTask = TaskList.Shake;
                                break;
                            }
                        }
                    }

                    //task 4 move towards enemy archon.

                    if(RobotTask == TaskList.None){
                        RobotTask = TaskList.MoveToEnemyArchon;
                    }

                    switch (RobotTask) {
                        case MoveToEnemyArchon:
                            System.out.println("Search");
                            ArrayList Archons = EnemyArchonLocations();
                            System.out.println("NUmber of known arcons: " + Archons.size());

                            if(Archons.isEmpty()){
                                Direction toTarget = myLocation.directionTo(targetEnemyArchonStart);
                                moveToTarget(targetEnemyArchonStart);
                            }else if(Archons.size() == 1){
                                MapLocation ArcLoc = (MapLocation) Archons.get(0);
                                Direction toTarget = myLocation.directionTo(ArcLoc);
                                moveToTarget(ArcLoc);
                            }else if(Archons.size() > 1){
                                int number = rand.nextInt(Archons.size());
                                System.out.println("Random: " + number);

                                MapLocation ArcLoc = (MapLocation) Archons.get(number);
                                Direction toTarget = myLocation.directionTo(ArcLoc);
                                moveToTarget(ArcLoc);
                            }
                            break;
                        case Shake:
                            System.out.println("Shake");
                            if (TargetTree != null) {
                                if (getRc().canShake(TargetTree.getID())) {
                                    getRc().shake(TargetTree.getID());
                                }else{
                                    //move toward tree
                                    Direction toTree = myLocation.directionTo(TargetTree.getLocation());
                                    moveToTarget(TargetTree.getLocation());
                                }
                            }
                            break;
                        case Chop:
                            System.out.println("Chop");
                            getRc().setIndicatorDot(TargetTree.getLocation(), 255, 255, 255);

                            if (getRc().canChop(TargetTree.getID())) {
                                getRc().chop(TargetTree.getID());
                            }else{
                                Direction ToTargetTree = myLocation.directionTo(TargetTree.getLocation());
                                moveToTarget(TargetTree.getLocation());
                            }
                            break;
                        case Strike:
                            System.out.println("Strike");
                            Direction ToKillScout = myLocation.directionTo(TargetRobot.getLocation());

                            System.out.println("Range to target: " + getRange(TargetRobot.getLocation()));

                            if(getRange(TargetRobot.getLocation()) > 2.5){
                                moveToTarget(TargetRobot.getLocation());
                                ToKillScout = myLocation.directionTo(TargetRobot.getLocation());
                            }

                            if (getRc().canStrike()) {
                                // ...Then fire a bullet in the direction of the enemy.
                                if (EnemyRobots.length > FriendlyRobots.length)
                                {
                                    getRc().strike();
                                }
                            }
                            break;
                    }

                    // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
                    Clock.yield();
                } catch (Exception e) {
                    System.out.println("Lumberjack Exception");
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
