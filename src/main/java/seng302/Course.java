package seng302;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by cjd137 on 7/03/17.
 * Class to figure out the mark locations and degrees.
 */

public class Course {

    private ArrayList<Mark> courseOrder;
    private ArrayList<Double> minMaxLatLon = new ArrayList<>();
    private HashMap<String, Mark> marks;

    private static final double EARTH_RADIUS_IN_NAUTICAL_MILES = 3437.74677;

    public Course() {
        this.marks = new HashMap<>();
        this.courseOrder = new ArrayList<>();
    }

    public void addNewMark(Mark mark){
        marks.put(mark.getName(), mark);
    }

    public void addMarkInOrder(String markName){
        courseOrder.add(marks.get(markName));
    }

    public double distanceBetweenMarks(int markIndex1, int markIndex2){
        Mark mark1 = this.courseOrder.get(markIndex1);
        Mark mark2 = this.courseOrder.get(markIndex2);
        double distance = greaterCircleDistance(mark1.getLat(), mark2.getLat(), mark1.getLon(), mark2.getLon());
        return distance;
    }

    public static double greaterCircleDistance(double lat1, double lat2, double lon1, double lon2){
        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);
        lon1 = Math.toRadians(lon1);
        lon2 = Math.toRadians(lon2);

        return EARTH_RADIUS_IN_NAUTICAL_MILES * Math.acos(Math.sin(lat1) * Math.sin(lat2) +
                Math.cos(lat1) * Math.cos(lat2) * Math.cos(lon2 - lon1));
    }

    public double headingsBetweenMarks(int markIndex1, int markIndex2){
        Mark mark1 = this.courseOrder.get(markIndex1);
        Mark mark2 = this.courseOrder.get(markIndex2);
        double Ldelta = Math.toRadians(mark2.getLon()) - Math.toRadians(mark1.getLon());
        double X = Math.cos(Math.toRadians(mark2.getLat())) * Math.sin(Ldelta);
        double Y = Math.cos(Math.toRadians(mark1.getLat())) * Math.sin(Math.toRadians(mark2.getLat()))
                - Math.sin(Math.toRadians(mark1.getLat())) * Math.cos(Math.toRadians(mark2.getLat())) * Math.cos(Ldelta);
        double heading = Math.toDegrees(Math.atan2(X, Y));
        if(heading < 0){
            heading += 360;
        }
        return heading;
    }

    /**
     * This function grabs all the latitude and longitude points for each mark/gate
     * The furthest most points to the North and East are found and made the max latitude and longitude
     * The furthest most points to the South and West are also found and made the min latitude and longitude.
     * This is then added to an ArrayList for future use.
     */
    public void getCourseSize() {
        double latMax, latMin;
        latMax = latMin = this.courseOrder.get(0).getLat();
        double lonMax, lonMin;
        lonMax = lonMin = this.courseOrder.get(0).getLon();

        for(int i = 0; i < this.courseOrder.size(); i++) {

            if(this.courseOrder.get(i).getLat() > latMax) {
                latMax = this.courseOrder.get(i).getLat();
            } else if(this.courseOrder.get(i).getLat() < latMin) {
                latMin = this.courseOrder.get(i).getLat();
            }

            if(this.courseOrder.get(i).getLon() > lonMax) {
                lonMax = this.courseOrder.get(i).getLon();
            } else if(this.courseOrder.get(i).getLon() < lonMin) {
                lonMin = this.courseOrder.get(i).getLon();
            }
        }
        //Adding padding of 0.004 to each coordinate to make sure the visual area is large enough
        latMin -= 0.004; lonMin -= 0.004; latMax += 0.004; lonMax += 0.004;

        this.minMaxLatLon.add(latMin);
        this.minMaxLatLon.add(lonMin);
        this.minMaxLatLon.add(latMax);
        this.minMaxLatLon.add(lonMax);
    }

    public ArrayList<Mark> getCourseOrder(){
        return this.courseOrder;
    }
    public ArrayList<Double> getBoundaries() {return this.minMaxLatLon; }



}
