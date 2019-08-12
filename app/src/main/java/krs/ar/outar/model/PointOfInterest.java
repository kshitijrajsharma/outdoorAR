package krs.ar.outar.model;

public class PointOfInterest {
    private double lat;
    private double lon;
    private String label;

    public PointOfInterest(double lat, double lon, String label) {
        this.lat = lat;
        this.lon = lon;
        this.label = label;
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

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }


}
