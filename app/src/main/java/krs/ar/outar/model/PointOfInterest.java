package krs.ar.outar.model;

public class PointOfInterest {
    private double lat;
    private double lon;
    private double alt;
    private String name;

    public PointOfInterest(String name,double lat, double lon,double alt) {
        this.name = name;
        this.lat = lat;
        this.lon = lon;
        this.alt=alt;

    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public String getname() {
        return name;
    }

    public void setname(String name) {
        this.name = name;
    }


}
