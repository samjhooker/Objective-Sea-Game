package seng302.models;

import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import seng302.utilities.DisplayUtils;

/**
 * Created by Louis on 14-May-17.
 *
 */

public class DistanceLine {
    private Boat firstBoat;
    private Boat secondBoat;
    private CompoundMark mark;
    private Line line;
    private double distanceBetweenBoats;

    public void setFirstBoat(Boat firstBoat) {
        this.firstBoat = firstBoat;
    }

    public Boat getFirstBoat(){ return firstBoat;}

    public void setSecondBoat(Boat secondBoat) {
        this.secondBoat = secondBoat;
    }

    public Boat getSecondBoat(){ return secondBoat;}

    public void setMark(CompoundMark mark) {
        this.mark = mark;
    }

    public Line getLine() {
        return line;
    }

    public void reCalcLine() {
        if (mark != null){
            if (firstBoat != null && secondBoat != null) { // Line between two boats
                Coordinate midPoint = mark.getPosition();
                if (mark.hasTwoMarks()) {
                    midPoint = DisplayUtils.midPointFromTwoCoords(mark.getMark1().getPosition(), mark.getMark2().getPosition());
                }
                Coordinate boatMidPoint = DisplayUtils.midPointFromTwoCoords(firstBoat.getCurrentPosition(), secondBoat.getCurrentPosition());
                Coordinate trialCoord = new Coordinate(boatMidPoint.getLat(), boatMidPoint.getLon());
                createLine(midPoint, trialCoord);
            }
            Boat boatToUse = null;
            if (firstBoat == null){
                boatToUse = secondBoat;
            } else if (secondBoat == null) {
                boatToUse = firstBoat;
            }
            if (boatToUse != null) {
                Coordinate midPoint = mark.getPosition();
                if (mark.hasTwoMarks()) {
                    midPoint = DisplayUtils.midPointFromTwoCoords(mark.getMark1().getPosition(), mark.getMark2().getPosition());
                }
                createLine(midPoint, boatToUse.getCurrentPosition());
            }
        }
    }

    private void createLine(Coordinate markMidPoint, Coordinate boatMidPoint){
        CanvasCoordinate boatCanvasMidPoint = DisplayUtils.convertFromLatLon(boatMidPoint);
        CanvasCoordinate markPoint = DisplayUtils.convertFromLatLon(markMidPoint);
        line = new Line(
                markPoint.getX(), markPoint.getY(),
                boatCanvasMidPoint.getX(), boatCanvasMidPoint.getY()
        );
        line.setStroke(Color.web("#70aaa2"));
    }

//
//    private void updateDistanceBetweenBoats(){
//        Coordinate firstBoatOnLine;
//        Coordinate secondBoatOnLine;
//        distanceBetweenBoats = TimeUtils.calcDistance(firstBoatOnLine, secondBoatOnLine);
//    }
//
//    private double getDistanceBetweenBoats(){
//        return distanceBetweenBoats;
//    }
}
