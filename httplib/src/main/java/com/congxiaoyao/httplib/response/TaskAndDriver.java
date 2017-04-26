package com.congxiaoyao.httplib.response;

import java.util.Date;

/**
 * Created by congxiaoyao on 2017/4/7.
 */
public class TaskAndDriver {

    public static final int STATUS_DELIVERED = 0;//任务已分配
    public static final int STATUS_EXECUTING = 1;//正在执行
    public static final int STATUS_COMPLETED = 2;//已完成

    public TaskAndDriver() {
    }

    public TaskAndDriver(Long carId, Date startTime, Long startSpot, Date endTime, Long endSpot, String content, Long createUser, Date createTime, Date realStartTime, Date realEndTime, Integer status, String note) {
        this.carId = carId;
        this.startTime = startTime;
        this.startSpot = startSpot;
        this.endTime = endTime;
        this.endSpot = endSpot;
        this.content = content;
        this.createUser = createUser;
        this.createTime = createTime;
        this.realStartTime = realStartTime;
        this.realEndTime = realEndTime;
        this.status = status;
        this.note = note;
    }

    public TaskAndDriver(Task task, CarDetail carDetail) {
        this.carId = task.carId;
        this.startTime = task.startTime;
        this.startSpot = task.startSpot;
        this.endTime = task.endTime;
        this.endSpot = task.endSpot;
        this.content = task.content;
        this.createUser = task.createUser;
        this.createTime = task.createTime;
        this.realStartTime = task.realStartTime;
        this.realEndTime = task.realEndTime;
        this.status = task.status;
        this.note = task.note;
        this.carDetail = carDetail;
    }

    private CarDetail carDetail;

    private Long taskId;

    private Long carId;

    private Date startTime;

    private Long startSpot;

    private Date endTime;

    private Long endSpot;

    private String content;

    private Long createUser;

    private Date createTime;

    private Date realStartTime;

    private Date realEndTime;

    private Integer status;

    private String note;

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public Long getCarId() {
        return carId;
    }

    public void setCarId(Long carId) {
        this.carId = carId;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Long getStartSpot() {
        return startSpot;
    }

    public void setStartSpot(Long startSpot) {
        this.startSpot = startSpot;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public Long getEndSpot() {
        return endSpot;
    }

    public void setEndSpot(Long endSpot) {
        this.endSpot = endSpot;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content == null ? null : content.trim();
    }

    public Long getCreateUser() {
        return createUser;
    }

    public void setCreateUser(Long createUser) {
        this.createUser = createUser;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getRealStartTime() {
        return realStartTime;
    }

    public void setRealStartTime(Date realStartTime) {
        this.realStartTime = realStartTime;
    }

    public Date getRealEndTime() {
        return realEndTime;
    }

    public void setRealEndTime(Date realEndTime) {
        this.realEndTime = realEndTime;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note == null ? null : note.trim();
    }

    public CarDetail getCarDetail() {
        return carDetail;
    }

    public void setCarDetail(CarDetail carDetail) {
        this.carDetail = carDetail;
    }

    @Override
    public String toString() {
        return "Task{" +
                "taskId=" + taskId +
                ", carId=" + carId +
                ", startTime=" + startTime +
                ", startSpot=" + startSpot +
                ", endTime=" + endTime +
                ", endSpot=" + endSpot +
                ", content='" + content + '\'' +
                ", createUser=" + createUser +
                ", createTime=" + createTime +
                ", realStartTime=" + realStartTime +
                ", realEndTime=" + realEndTime +
                ", status=" + status +
                ", note='" + note + '\'' +
                '}';
    }
}
