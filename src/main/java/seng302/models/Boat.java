package seng302.models;


import javafx.util.Pair;
import seng302.controllers.RaceViewController;
import seng302.utilities.PolarReader;

import java.util.ArrayList;

/**
 * Class to encapsulate properties associated with a boat.
 */

public class Boat implements Comparable<Boat>{

    private String name;
    private String nickName;
    private double speed;
    private int finishingPlace;

    private Coordinate currentPosition;

    private int lastPassedMark;
    private int lastTackMarkPassed;
    private int lastGybeMarkPassed;
    private boolean finished;
    private double heading;
    private double maxSpeed;
    private ArrayList<Coordinate> pathCoords;
    private double totalSpeed = 0;
    private double speedCounter = 0;
    private double VMGofBoat;
    private double TWAofBoat;
    private double gybeVMGofBoat;
    private double gybeTWAofBoat;
    private String lastTurnTaken = "Right";

    public Boat(String name, String nickName, double speed) {
        this.name = name;
        this.nickName = nickName;
        this.maxSpeed = speed;
        this.finished = false;
        this.lastPassedMark = 0;
        this.pathCoords = new ArrayList<>();
        this.currentPosition = new Coordinate(0,0);
        try {
            PolarReader.polars();
        } catch (Exception e) {
            e.printStackTrace();
        }
        ArrayList<Integer> TWSList = PolarReader.getTWS();
        ArrayList<ArrayList<Pair<Double, Double>>> polars = PolarReader.getPolars();
        Pair<Double,Double> tackingInfo = tacking(20,TWSList,polars, true);
        Pair<Double,Double> gybingInfo = tacking(20, TWSList,polars,false);
        gybeVMGofBoat = gybingInfo.getKey();
        gybeTWAofBoat = gybingInfo.getValue();
        VMGofBoat = tackingInfo.getKey();
        TWAofBoat = tackingInfo.getValue();
    }

    /**
     * Sets the latitude and longitude of the boat
     * @param lat the latitude of the boat
     * @param lon the longitude of the boat
     */
    public void setPosition(double lat, double lon){
        currentPosition.setLat(lat);
        currentPosition.setLon(lon);
    }

    public Coordinate getCurrentPosition() {
        return currentPosition;
    }

    public double averageSpeed(double currentSpeed){
        totalSpeed += currentSpeed;
        speedCounter++;
        //System.out.println("my speed " + (totalSpeed/speedCounter));
        return totalSpeed/speedCounter;
    }

    /**
     * Updates the boat's coordinates by how much it moved in timePassed hours on the course
     * @param timePassed the amount of race hours since the last update
     * @param course the course the boat is racing on
     */
    public void updateLocation(double timePassed, Course course) {
        if(finished){
            return;
        }
        ArrayList<CompoundMark> courseOrder = course.getCourseOrder();
        CompoundMark nextMark = courseOrder.get(lastPassedMark+1);
        double currentSpeed = speed;
//        if(nextMark.getName().equals("Windward Gate")){
//            currentSpeed = VMGofBoat;
//        } else if(nextMark.getName().equals("Leeward Gate")){
//            currentSpeed = gybeVMGofBoat * (-1.0);
//        }
        System.out.println(currentSpeed);
        double distanceGained = timePassed * averageSpeed(currentSpeed);
        double distanceLeftInLeg = currentPosition.greaterCircleDistance(nextMark.getPosition());


        //If boat moves more than the remaining distance in the leg
        while(distanceGained > distanceLeftInLeg && lastPassedMark < courseOrder.size()-1){
            distanceGained -= distanceLeftInLeg;
            //Set boat position to next mark
            currentPosition.setLat(nextMark.getLat());
            currentPosition.setLon(nextMark.getLon());
            lastPassedMark++;

            if(lastPassedMark < courseOrder.size()-1){
                setHeading(course.headingsBetweenMarks(lastPassedMark, lastPassedMark + 1));
                nextMark = courseOrder.get(lastPassedMark+1);
                distanceLeftInLeg = currentPosition.greaterCircleDistance(nextMark.getPosition());
            }
        }

        //Check if boat has finished
        if(lastPassedMark == courseOrder.size()-1){
            finished = true;
            speed = 0;
        } if(nextMark.getName().equals("Windward Gate")){
                lastGybeMarkPassed = 0;
                Coordinate tackingPosition = tackingUpdateLocation(distanceGained, courseOrder, course, true);
                //Move the remaining distance in leg
                currentPosition.update(tackingPosition.getLat(), tackingPosition.getLon());
        } else if(nextMark.getName().equals("Leeward Gate")){
            lastTackMarkPassed = 0;
            Coordinate tackingPosition = tackingUpdateLocation(distanceGained, courseOrder, course, false);
            //Move the remaining distance in leg
            currentPosition.update(tackingPosition.getLat(), tackingPosition.getLon());
        } else{
            lastTackMarkPassed = 0;
            lastGybeMarkPassed = 0;
            //Move the remaining distance in leg
            double percentGained = (distanceGained / distanceLeftInLeg);
            double newLat = getCurrentLat() + percentGained * (nextMark.getLat() - getCurrentLat());
            double newLon = getCurrentLon() + percentGained * (nextMark.getLon() - getCurrentLon());
            currentPosition.update(newLat, newLon);
        }
    }

    public Coordinate tackingUpdateLocation(double distanceGained, ArrayList<CompoundMark> courseOrder, Course course, Boolean onTack){
        double TrueWindAngle;
        if(onTack){
            TrueWindAngle = TWAofBoat;
        } else {
            TrueWindAngle = gybeTWAofBoat;
        }

        CompoundMark nextMark = courseOrder.get(lastPassedMark+1);
        double lengthOfLeg = courseOrder.get(lastPassedMark).getPosition().greaterCircleDistance(nextMark.getPosition());
        double lengthOfTack = ((lengthOfLeg*10) / Math.floor((lengthOfLeg*10)))/10;
        int numOfTacks = (int)(lengthOfLeg / lengthOfTack);
        ArrayList<CompoundMark> tackingMarks = new ArrayList<>();
        tackingMarks.add(courseOrder.get(lastPassedMark));
        CompoundMark currentMark = courseOrder.get(lastPassedMark);
        String previousTackLat = lastTurnTaken;
        for(int i = 0; i < numOfTacks; i++){
            double distance = (lengthOfTack/Math.cos(Math.toRadians(TrueWindAngle)));
            Coordinate temp1 = tackingMarks.get(i).getPosition();
            double bearing = currentMark.getPosition().headingToCoordinate(nextMark.getPosition());
            if(previousTackLat == "Right"){
                bearing -= TrueWindAngle;
                previousTackLat = "Left";
            } else {
                bearing += TrueWindAngle;
                previousTackLat = "Right";
            }

            Coordinate answer = temp1.CoordFrom(distance,bearing);
            double newLat = answer.getLat();
            double newLong = answer.getLon();
            String name = "tack" + (i+1);
            CompoundMark temp = new CompoundMark(name, newLat, newLong);
            tackingMarks.add(temp);
        }
        tackingMarks.add(nextMark);
        double lengthOfTackingLeg = 0;
//        for(CompoundMark mark: tackingMarks){
//            System.out.println(mark.getLat());
//            System.out.println(mark.getLon());
//        }
        for(int j = 1; j < tackingMarks.size(); j++){
            lengthOfTackingLeg += tackingMarks.get(j-1).getPosition().greaterCircleDistance(tackingMarks.get(j).getPosition());
        }
        int lastMarkPassed = 0;
        if(onTack){
            lastMarkPassed = lastTackMarkPassed;
        } else {
            lastMarkPassed = lastGybeMarkPassed;
        }
        CompoundMark nextTackMark = tackingMarks.get(lastMarkPassed+1);
        double distanceLeftinTack = currentPosition.greaterCircleDistance(nextTackMark.getPosition());
        if(lastMarkPassed == 0){
            double newHeading = tackingMarks.get(lastMarkPassed).getPosition().headingToCoordinate(tackingMarks.get(lastMarkPassed + 1).getPosition());;
            setHeading(newHeading);
            //nextTackMark = tackingMarks.get(lastMarkPassed+1);
            //distanceLeftinTack = currentPosition.greaterCircleDistance(nextTackMark.getPosition());
        }
        //If boat moves more than the remaining distance in the leg
        while(distanceGained > distanceLeftinTack && lastMarkPassed < tackingMarks.size()-1){
            distanceGained -= distanceLeftinTack;
            //Set boat position to next mark
            currentPosition.setLat(nextTackMark.getLat());
            currentPosition.setLon(nextTackMark.getLon());
            if(onTack){
                lastTackMarkPassed++;
            } else {
                lastGybeMarkPassed++;
            }
            if(lastMarkPassed == 0){

            }
            lastMarkPassed++;

            if(lastMarkPassed < tackingMarks.size()-1){
                double newHeading = tackingMarks.get(lastMarkPassed).getPosition().headingToCoordinate(tackingMarks.get(lastMarkPassed + 1).getPosition());;
                setHeading(newHeading);
                nextTackMark = tackingMarks.get(lastMarkPassed+1);
                distanceLeftinTack = currentPosition.greaterCircleDistance(nextTackMark.getPosition());
            }
        }

        double percentGained = (distanceGained / distanceLeftinTack);
        double newLat = getCurrentLat() + percentGained * (nextTackMark.getLat() - getCurrentLat());
        double newLon = getCurrentLon() + percentGained * (nextTackMark.getLon() - getCurrentLon());
        Coordinate tackPosition = new Coordinate(newLat, newLon);
        return tackPosition;
    }



    public double VMG(double boatSpeed, double TWA){
        double VMG = boatSpeed * Math.cos(Math.toRadians(TWA));
        return VMG;
    }

    public double lagrangeInterpolation(Pair<Double, Double> A, Pair<Double, Double> B, Pair<Double, Double> C, double x) {
        double answer = (((x - B.getKey()) * (x - C.getKey())) / ((A.getKey() - B.getKey()) * (A.getKey() - C.getKey()))) * A.getValue() + (((x - A.getKey()) * (x - C.getKey())) / ((B.getKey() - A.getKey()) * (B.getKey() - C.getKey()))) * B.getValue() + (((x - A.getKey()) * (x - B.getKey())) / ((C.getKey() - A.getKey()) * (C.getKey() - B.getKey()))) * C.getValue();
        return answer;
    }

    public Pair<Double,Double> tacking(int TWS, ArrayList<Integer> trueWindSpeeds, ArrayList<ArrayList<Pair<Double, Double>>> polars, Boolean onTack){
//        double TWS = course.getTrueWindSpeed();
        Pair<Double,Double> boatsTack;
        int index = 0;
        double TWSDiff = 1000000000;
        //Find the 3 values closest to the TWS to interpolate with
        for(int i = 0; i < trueWindSpeeds.size(); i++){
            if(Math.abs(TWS - trueWindSpeeds.get(i)) < TWSDiff){
                index = i;
                TWSDiff = Math.abs(TWS - trueWindSpeeds.get(i));
            }
        }
        //Check that these values aren't 0 or the size of the list as this will cause an error
        if(index == 0){index ++;}
        if(index == trueWindSpeeds.size() - 1){index -= 1;}
        double TWS1 = trueWindSpeeds.get(index-1);
        double TWS2 = trueWindSpeeds.get(index);
        double TWS3 = trueWindSpeeds.get(index+1);
        double VMG1 = 0;
        double TWA1 = 0;
        double VMG2 = 0;
        double TWA2 = 0;
        double VMG3 = 0;
        double TWA3 = 0;
        double gybeVMG1 = 1000000;
        double gybeVMG2 = 1000000;
        double gybeVMG3 = 1000000;

        Pair<Double,Double> pair4;
        Pair<Double,Double> pair5;
        Pair<Double,Double> pair6;
        double TrueVMG;
        //Interpolate between closet TWS to get highest VMG of each
        //if gybing 5,6,7 if tacking 1,3,5
        if(onTack){
        for(double k = 0; k < 91; k++){
            double BSP1 = lagrangeInterpolation(polars.get(index - 1).get(1), polars.get(index - 1).get(3), polars.get(index - 1).get(5), k);
            if(VMG(BSP1, k) > VMG1){VMG1 = VMG(BSP1, k); TWA1 = k;}
            double BSP2 = lagrangeInterpolation(polars.get(index).get(1), polars.get(index).get(3), polars.get(index).get(5), k);
            if(VMG(BSP2, k) > VMG2){VMG2 = VMG(BSP2, k); TWA2 = k;}
            double BSP3 = lagrangeInterpolation(polars.get(index + 1).get(1), polars.get(index + 1).get(3), polars.get(index + 1).get(5), k);
            if(VMG(BSP3, k) > VMG3){VMG3 = VMG(BSP3, k);TWA3 = k;}
        }
            Pair<Double,Double> pair1 = new Pair<>(TWS1, VMG1);
            Pair<Double,Double> pair2 = new Pair<>(TWS2, VMG2);
            Pair<Double,Double> pair3 =  new Pair<>(TWS3, VMG3);
            TrueVMG = lagrangeInterpolation(pair1,pair2, pair3, TWS);
            //interpolate back to get TWA based on found VMG
            pair4 = new Pair<>(VMG1, TWA1);
            pair5 = new Pair<>(VMG2, TWA2);
            pair6 = new Pair<>(VMG3, TWA3);
        } else {
            for(double k = 90; k < 181; k++){
                double BSP1 = lagrangeInterpolation(polars.get(index - 1).get(5), polars.get(index - 1).get(6), polars.get(index - 1).get(7), k);
                if(VMG(BSP1, k) < gybeVMG1){gybeVMG1 = VMG(BSP1, k); TWA1 = k;}
                double BSP2 = lagrangeInterpolation(polars.get(index).get(5), polars.get(index).get(6), polars.get(index).get(7), k);
                if(VMG(BSP2, k) < gybeVMG2){gybeVMG2 = VMG(BSP2, k); TWA2 = k;}
                double BSP3 = lagrangeInterpolation(polars.get(index + 1).get(5), polars.get(index + 1).get(6), polars.get(index + 1).get(7), k);
                if(VMG(BSP3, k) < gybeVMG3){gybeVMG3 = VMG(BSP3, k);TWA3 = k;}
                }
            Pair<Double,Double> pair1 = new Pair<>(TWS1, gybeVMG1);
            Pair<Double,Double> pair2 = new Pair<>(TWS2, gybeVMG2);
            Pair<Double,Double> pair3 =  new Pair<>(TWS3, gybeVMG3);
            TrueVMG = lagrangeInterpolation(pair1,pair2, pair3, TWS);
            //interpolate back to get TWA based on found VMG
            pair4 = new Pair<>(gybeVMG1, TWA1);
            pair5 = new Pair<>(gybeVMG2, TWA2);
            pair6 = new Pair<>(gybeVMG3, TWA3);

        }
        double TWA = lagrangeInterpolation(pair4,pair5,pair6,TrueVMG);
        boatsTack = new Pair<>(TrueVMG, TWA);
        return boatsTack;
    }




    public int compareTo(Boat otherBoat){
        return otherBoat.getLastPassedMark() - lastPassedMark;
    }

    public String getName() {
        return this.name;
    }

    public String getNickName() {return nickName;}

    public double getSpeed() {
        return this.speed;
    }

    public int getFinishingPlace() {
        return this.finishingPlace;
    }

    public void setFinishingPlace(int place) {
        this.finishingPlace = place;
    }

    public int getLastPassedMark() {
        return lastPassedMark;
    }

    public double getCurrentLat() {
        return currentPosition.getLat();
    }

    public double getCurrentLon() {
        return currentPosition.getLon();
    }

    public boolean isFinished() {
        return finished;
    }

    public double getHeading() {
        return heading;
    }

    /**
     * Sets the boats heading to the current value. If the heading has changed,
     * a new record is added the pathCoords list
     * @param heading the new heading
     * */
    public void setHeading(double heading) {
        if (this.heading != heading) {
            this.heading = heading;
            addPathCoord(new Coordinate(getCurrentLat(), getCurrentLon()));
        }
    }

    public ArrayList<Coordinate> getPathCoords() {return pathCoords;}

    public void addPathCoord(Coordinate newCoord){
        this.pathCoords.add(newCoord);
    }

    /**
     * Make speed be the max speed.
     */
    public void maximiseSpeed(){
        this.speed = maxSpeed;
        speedCounter = 0;
    }

    public double getMaxSpeed() {
        return maxSpeed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }
}
