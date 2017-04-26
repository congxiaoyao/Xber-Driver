package com.congxiaoyao.location.model;

/**
 * Created by Jaycejia on 2017/2/12.
 */
public class NearestNCarsQueryMessage {
    private Long userId;//用户id
    private Long queryId;//查询id
    private Double latitude;//纬度
    private Double longitude;//经度
    private Integer number;//查询数量
    private Double radius;//半径

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getQueryId() {
        return queryId;
    }

    public void setQueryId(Long queryId) {
        this.queryId = queryId;
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

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public Double getRadius() {
        return radius;
    }

    public void setRadius(Double radius) {
        this.radius = radius;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NearestNCarsQueryMessage that = (NearestNCarsQueryMessage) o;

        if (userId != null ? !userId.equals(that.userId) : that.userId != null) return false;
        if (queryId != null ? !queryId.equals(that.queryId) : that.queryId != null) return false;
        if (latitude != null ? !latitude.equals(that.latitude) : that.latitude != null)
            return false;
        if (longitude != null ? !longitude.equals(that.longitude) : that.longitude != null)
            return false;
        if (number != null ? !number.equals(that.number) : that.number != null) return false;
        return radius != null ? radius.equals(that.radius) : that.radius == null;

    }

    @Override
    public int hashCode() {
        int result = userId != null ? userId.hashCode() : 0;
        result = 31 * result + (queryId != null ? queryId.hashCode() : 0);
        result = 31 * result + (latitude != null ? latitude.hashCode() : 0);
        result = 31 * result + (longitude != null ? longitude.hashCode() : 0);
        result = 31 * result + (number != null ? number.hashCode() : 0);
        result = 31 * result + (radius != null ? radius.hashCode() : 0);
        return result;
    }
}
