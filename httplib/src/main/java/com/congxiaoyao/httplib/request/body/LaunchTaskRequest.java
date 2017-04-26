package com.congxiaoyao.httplib.request.body;

/**
 * Created by Jaycejia on 2017/2/18.
 */
public class LaunchTaskRequest {
    private Long startTime;
    private Long startSpot;
    private Long endTime;
    private Long endSpot;
    private Long carId;
    private String content;
    private String note;

    public Long getStartTime() {
        return startTime;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    public Long getStartSpot() {
        return startSpot;
    }

    public void setStartSpot(Long startSpot) {
        this.startSpot = startSpot;
    }

    public Long getEndTime() {
        return endTime;
    }

    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }

    public Long getEndSpot() {
        return endSpot;
    }

    public void setEndSpot(Long endSpot) {
        this.endSpot = endSpot;
    }

    public Long getCarId() {
        return carId;
    }

    public void setCarId(Long carId) {
        this.carId = carId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    @Override
    public String toString() {
        return "LaunchTaskRequest{" +
                "startTime=" + startTime +
                ",\n startSpot=" + startSpot +
                ",\n endTime=" + endTime +
                ",\n endSpot=" + endSpot +
                ",\n carId=" + carId +
                ",\n content='" + content + '\'' +
                ",\n note='" + note + '\'' +
                '}';
    }
}
