package SamsTestPlayer;

import battlecode.common.*;

import java.util.ArrayList;

public class Scout extends Robot {

    public enum TaskList {
        KillScout, KillGardener, Orbit, Shake, Search, None
    }

    public Scout(RobotController rc) {
        super(rc);
        whatAmI = SCOUT;
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

                    TaskList RobotTask = TaskList.None; //no task picked yet this turn

                    MapLocation myLocation = getRc().getLocation();

                    // See if there are any nearby enemy robots
                    RobotInfo[] robots = getRc().senseNearbyRobots(-1, enemy);

                    TreeInfo[] trees = getRc().senseNearbyTrees();

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
                                if(RobotTask == TaskList.None){
                                    RobotTask = TaskList.KillGardener;
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
                                    RobotTask = TaskList.KillScout;
                                    TargetRobot = r;
                                }
                                break;
                            case TANK:
                                NUM_OF_ENEMY_TANK++;
                                break;
                        }

                    }

                    //2: enemy archon in range orbit it:
                    if(RobotTask == TaskList.None){
                        if(ENEMY_ARCHON != null){
                            RobotTask = TaskList.Orbit;
                        }
                    }

                    //3: tree with bullets in range shake it
                    if(RobotTask == TaskList.None){
                        //look for trees to shake
                        for (TreeInfo t : trees) {
                            if (t.containedBullets > 0) {
                                TargetShakeTree = t;
                                RobotTask = TaskList.Shake;
                                break;
                            }
                        }
                    }

                    //4: head toward enemy archon starting location(search mode)
                    if(RobotTask == TaskList.None){
                        RobotTask = TaskList.Search;
                    }

                    switch (RobotTask) {
                        case Search:
                            System.out.println("Search");
                            ArrayList Archons = EnemyArchonLocations();
                            System.out.println("NUmber of known arcons: " + Archons.size());

                            if(Archons.isEmpty()){
                                Direction toTarget = myLocation.directionTo(targetEnemyArchonStart);
                                tryMove(toTarget, 15, 12);
                            }else if(Archons.size() == 1){
                                MapLocation ArcLoc = (MapLocation) Archons.get(0);
                                Direction toTarget = myLocation.directionTo(ArcLoc);
                                tryMove(toTarget, 15, 12);
                            }else if(Archons.size() > 1){
                                int number = rand.nextInt(Archons.size());
                                System.out.println("Random: " + number);

                                MapLocation ArcLoc = (MapLocation) Archons.get(number);
                                Direction toTarget = myLocation.directionTo(ArcLoc);
                                tryMove(toTarget, 15, 12);
                            }
                            break;
                        case Shake:
                            System.out.println("Shake");
                            if (TargetShakeTree != null) {
                                if (getRc().canShake(TargetShakeTree.getID())) {
                                    getRc().shake(TargetShakeTree.getID());
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

                            if (getRc().canFireSingleShot()) {
                                // ...Then fire a bullet in the direction of the enemy.
                                getRc().fireSingleShot(ToKillScout);
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

                            if (getRc().canFireSingleShot()) {
                                // ...Then fire a bullet in the direction of the enemy.
                                getRc().fireSingleShot(ToKillGardener);
                            }
                            break;
                    }

                    if (ENEMY_JACK_TO_AVOID != null & getRc().hasMoved() == false){
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
