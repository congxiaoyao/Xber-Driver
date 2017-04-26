package com.congxiaoyao.httplib.response;

public class Spot {
    private Long spotId;

    private String spotName;

    private Double latitude;

    private Double longitude;

    public Spot(Long spotId, String spotName, Double latitude, Double longitude) {
        this.spotId = spotId;
        this.spotName = spotName;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Long getSpotId() {
        return spotId;
    }

    public void setSpotId(Long spotId) {
        this.spotId = spotId;
    }

    public String getSpotName() {
        return spotName;
    }

    public void setSpotName(String spotName) {
        this.spotName = spotName == null ? null : spotName.trim();
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    @Override
    public String toString() {
        return "Spot{" +
                "spotId=" + spotId +
                ", spotName='" + spotName + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                '}';
    }
}