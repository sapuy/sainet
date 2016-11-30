package xyz.leosap.multiplace.objects;

/**
 * Created by LeoSap on 30/11/2016.
 */

public class Place {
    private String name;
    private double lat,lng;
    private String image;

    public Place(String name, double lat, double lng, String image) {
        this.name = name;
        this.lat = lat;
        this.lng = lng;
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
