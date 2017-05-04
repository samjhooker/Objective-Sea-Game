package seng302.models;

/**
 * Coordinate class to encapsulates the latitude longitude coordinates.
 */
public class Coordinate {
    protected double lat, lon;

    private static final double EARTH_RADIUS_IN_NAUTICAL_MILES = 3437.74677;

    public Coordinate(double lat, double lon){
        this.lat = lat;
        this.lon = lon;
    }

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    /**
     * Simplifier for setting lat and lon in one call
     * @param lat the latitude to be set
     * @param lon the longitude to be set
     */
    public void update(double lat, double lon) {
        setLat(lat);
        setLon(lon);
    }

    /**
     * Calculates distance from this coordinate to another
     * @param other the other Coordinate to compare distance to
     * @return distance from this to other in nautical miles
     */
    public double greaterCircleDistance(Coordinate other){
        double lat1 = Math.toRadians(lat);
        double lat2 = Math.toRadians(other.getLat());
        double lon1 = Math.toRadians(lon);
        double lon2 = Math.toRadians(other.getLon());

        return EARTH_RADIUS_IN_NAUTICAL_MILES * Math.acos(Math.sin(lat1) * Math.sin(lat2) +
                Math.cos(lat1) * Math.cos(lat2) * Math.cos(lon2 - lon1));
    }

    /**
     * Calculates the heading from this coordinate to another
     * @param other the other Coordinate to calculate heading to
     * @return the heading from this to other
     */
    public double headingToCoordinate(Coordinate other){
        double Ldelta = Math.toRadians(other.getLon()) - Math.toRadians(getLon());
        double X = Math.cos(Math.toRadians(other.getLat())) * Math.sin(Ldelta);
        double Y = Math.cos(Math.toRadians(getLat())) * Math.sin(Math.toRadians(other.getLat()))
                - Math.sin(Math.toRadians(getLat())) * Math.cos(Math.toRadians(other.getLat())) * Math.cos(Ldelta);
        double heading = Math.toDegrees(Math.atan2(X, Y));
        if(heading < 0){
            heading += 360;
        }
        return heading;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Coordinate that = (Coordinate) o;

        if (Double.compare(that.lat, lat) != 0) return false;
        return Double.compare(that.lon, lon) == 0;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(lat);
        result = (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(lon);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    /**
     * Calculates the lat and long based on given distance and bearing from current lat,long
     * @param distance the distance between the two coordinates
     * @param bearing the bearing between the two coordinates
     * @return the new coordinate
     */
    public Coordinate coordAt(double distance, double bearing){
        bearing = Math.toRadians(bearing);
        double lat1 = Math.toRadians(lat);
        double long1 = Math.toRadians(lon);

        double lat2 = Math.asin( Math.sin(lat1)*Math.cos(distance/EARTH_RADIUS_IN_NAUTICAL_MILES) + Math.cos(lat1)*Math.sin(distance/EARTH_RADIUS_IN_NAUTICAL_MILES)*Math.cos(bearing));

        double long2 = long1 + Math.atan2(Math.sin(bearing)*Math.sin(distance/EARTH_RADIUS_IN_NAUTICAL_MILES)*Math.cos(lat1), Math.cos(distance/EARTH_RADIUS_IN_NAUTICAL_MILES)-Math.sin(lat1)*Math.sin(lat2));

        lat2 = Math.toDegrees(lat2);
        long2 = Math.toDegrees(long2);
        return new Coordinate(lat2,long2);

    }
}
