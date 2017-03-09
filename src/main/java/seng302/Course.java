package seng302;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by cjd137 on 7/03/17.
 * Class to figure out the mark locations and degrees.
 */

public class Course {

    private ArrayList<Mark> courseOrder;
    private HashMap<String, Mark> marks;

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
        System.out.println((mark1.getLat() - mark2.getLat()));
        System.out.println((mark1.getLon() - mark2.getLon()));
        double latDist = Math.pow((mark1.getLat() - mark2.getLat()), 2);
        double lonDist = Math.pow((mark1.getLon() - mark2.getLon()), 2);
        double totalDist = Math.pow((latDist + lonDist), 0.5);
        System.out.println(totalDist);
        return totalDist;
    }

    public ArrayList<Mark> getCourseOrder(){
        return this.courseOrder;
    }



}
