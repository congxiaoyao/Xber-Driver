package com.congxiaoyao.xber_driver.simulatepost;

import org.litepal.crud.DataSupport;

/**
 * Created by congxiaoyao on 2017/3/2.
 */

public class UploadState extends DataSupport {

    private long carId;
    private int updated;
    private long taskId;
    private long endSpotId;

    public UploadState() {
    }

    public UploadState(long carId, int updated, long taskId) {
        this.carId = carId;
        this.updated = updated;
        this.taskId = taskId;
    }

    public long getCarId() {
        return carId;
    }

    public void setCarId(long carId) {
        this.carId = carId;
    }

    public int getUpdated() {
        return updated;
    }

    public void setUpdated(int updated) {
        this.updated = updated;
    }

    public void setTaskId(long taskId) {
        this.taskId = taskId;
    }

    public long getTaskId() {
        return taskId;
    }

    public long getEndSpotId() {
        return endSpotId;
    }

    public void setEndSpotId(long endSpotId) {
        this.endSpotId = endSpotId;
    }

    @Override
    public String toString() {
        return "UploadState{" +
                "carId=" + carId +
                ", updated=" + updated +
                ", taskId=" + taskId +
                ", endSpotId=" + endSpotId +
                '}';
    }
}
