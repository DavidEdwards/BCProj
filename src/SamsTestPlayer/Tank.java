package SamsTestPlayer;

import battlecode.common.*;

import java.util.ArrayList;

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
                    Soldier.TaskList RobotTask = Soldier.TaskList.None; //no task picked yet this turn
                    MapLocation myLocation = getRc().getLocation();

                    // See if there are any nearby enemy robots
                    RobotInfo[] robots = getRc().senseNearbyRobots(-1, enemy);

                    RobotInfo TargetRobot = null;


                    //log an archons you see
                    for (RobotInfo r : robots) {
                        switch (r.getType()) {
                            case ARCHON:
                                LogEnemyArchonLocation(r);
                                break;
                        }
                    }

                    //prioritys:
                    //1, shoot soldier
                    if(RobotTask == Soldier.TaskList.None) {
                        if (FindRobotType(robots, RobotType.SOLDIER) != null) {
                            TargetRobot = FindRobotType(robots, RobotType.SOLDIER);
                            RobotTask = Soldier.TaskList.KillSolider;
                        }
                    }
                    //2, shoot tank
                    if(RobotTask == Soldier.TaskList.None) {
                        if (FindRobotType(robots, RobotType.TANK) != null) {
                            TargetRobot = FindRobotType(robots, RobotType.TANK);
                            RobotTask = Soldier.TaskList.KillTank;
                        }
                    }
                    //3, shoot scout
                    if(RobotTask == Soldier.TaskList.None) {
                        if (FindRobotType(robots, RobotType.SCOUT) != null) {
                            TargetRobot = FindRobotType(robots, RobotType.SCOUT);
                            RobotTask = Soldier.TaskList.KillScout;
                        }
                    }
                    //4, shoot lumberjack
                    if(RobotTask == Soldier.TaskList.None) {
                        if (FindRobotType(robots, RobotType.LUMBERJACK) != null) {
                            TargetRobot = FindRobotType(robots, RobotType.LUMBERJACK);
                            RobotTask = Soldier.TaskList.KillLumberjack;
                        }
                    }
                    //5, shoot archon
                    if(RobotTask == Soldier.TaskList.None) {
                        if (FindRobotType(robots, RobotType.ARCHON) != null) {
                            TargetRobot = FindRobotType(robots, RobotType.ARCHON);
                            RobotTask = Soldier.TaskList.KillArchon;
                        }
                    }
                    if(RobotTask == Soldier.TaskList.None) {
                        if (FindRobotType(robots, RobotType.GARDENER) != null) {
                            TargetRobot = FindRobotType(robots, RobotType.GARDENER);
                            RobotTask = Soldier.TaskList.KillGardener;
                        }
                    }
                    //6, search
                    if(RobotTask == Soldier.TaskList.None) {
                        RobotTask = Soldier.TaskList.Search;
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
                        case KillSolider:
                            System.out.println("KillSolider");
                            Direction ToKillSolider = myLocation.directionTo(TargetRobot.getLocation());

                            System.out.println("Range to target: " + getRange(TargetRobot.getLocation()));

                            if(getRange(TargetRobot.getLocation()) > 2.5){
                                tryMove(ToKillSolider);
                                ToKillSolider = myLocation.directionTo(TargetRobot.getLocation());
                            }

                            if (getRc().canFirePentadShot()) {
                                // ...Then fire a bullet in the direction of the enemy.
                                getRc().firePentadShot(ToKillSolider);
                            }
                            break;
                        case KillTank:
                            System.out.println("KillTank");
                            Direction ToKillTank = myLocation.directionTo(TargetRobot.getLocation());

                            System.out.println("Range to target: " + getRange(TargetRobot.getLocation()));

                            if(getRange(TargetRobot.getLocation()) > 2.5){
                                tryMove(ToKillTank);
                                ToKillTank = myLocation.directionTo(TargetRobot.getLocation());
                            }

                            if (getRc().canFirePentadShot()) {
                                // ...Then fire a bullet in the direction of the enemy.
                                getRc().firePentadShot(ToKillTank);
                            }
                            break;
                        case KillScout:
                            System.out.println("KillScout");
                            Direction ToKillScout = myLocation.directionTo(TargetRobot.getLocation());

                            System.out.println("Range to target: " + getRange(TargetRobot.getLocation()));

                            if(getRange(TargetRobot.getLocation()) > 2.5){
                                tryMove(ToKillScout);
                                ToKillScout = myLocation.directionTo(TargetRobot.getLocation());
                            }

                            if (getRc().canFirePentadShot()) {
                                // ...Then fire a bullet in the direction of the enemy.
                                getRc().firePentadShot(ToKillScout);
                            }
                            break;
                        case KillLumberjack:
                            System.out.println("KillLumberjack");
                            Direction ToKillLumberjack = myLocation.directionTo(TargetRobot.getLocation());

                            System.out.println("Range to target: " + getRange(TargetRobot.getLocation()));

                            if(getRange(TargetRobot.getLocation()) > 2.5){
                                tryMove(ToKillLumberjack);
                                ToKillLumberjack = myLocation.directionTo(TargetRobot.getLocation());
                            }

                            if (getRc().canFirePentadShot()) {
                                // ...Then fire a bullet in the direction of the enemy.
                                getRc().firePentadShot(ToKillLumberjack);
                            }
                            break;
                        case KillArchon:
                            System.out.println("KillArchon");
                            Direction ToKillArchon = myLocation.directionTo(TargetRobot.getLocation());

                            System.out.println("Range to target: " + getRange(TargetRobot.getLocation()));

                            if(getRange(TargetRobot.getLocation()) > 2.5){
                                tryMove(ToKillArchon);
                                ToKillArchon = myLocation.directionTo(TargetRobot.getLocation());
                            }

                            if (getRc().canFirePentadShot()) {
                                // ...Then fire a bullet in the direction of the enemy.
                                getRc().firePentadShot(ToKillArchon);
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

                            if (getRc().canFirePentadShot()) {
                                // ...Then fire a bullet in the direction of the enemy.
                                getRc().firePentadShot(ToKillGardener);
                            }
                            break;
                    }


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
