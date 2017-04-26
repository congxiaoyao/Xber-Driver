package com.congxiaoyao.httplib.response;

import java.util.Date;

/**
 * Created by Jaycejia on 2017/2/18.
 */
public class TaskRsp {
    private Long taskId;

    private Long carId;

    private Date startTime;

    private Spot startSpot;

    private Date endTime;

    private Spot endSpot;

    private String content;

    private Long createUser;

    private Date createTime;

    private Date realStartTime;

    private Date realEndTime;

    private Integer status;

    private String note;

    public TaskRsp(Task task) {
        this.taskId = task.getTaskId();
        this.startTime = task.getStartTime();
        this.endTime = task.getEndTime();
        this.content = task.getContent();
        this.createUser = task.getCreateUser();
        this.createTime = task.getCreateTime();
        this.realStartTime = task.getRealStartTime();
        this.realEndTime = task.getRealEndTime();
        this.status = task.getStatus();
        this.note = task.getNote();
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Spot getStartSpot() {
        return startSpot;
    }

    public void setStartSpot(Spot startSpot) {
        this.startSpot = startSpot;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public Spot getEndSpot() {
        return endSpot;
    }

    public void setEndSpot(Spot endSpot) {
        this.endSpot = endSpot;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
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
        this.note = note;
    }

    public Long getCarId() {
        return carId;
    }

    public void setCarId(Long carId) {
        this.carId = carId;
    }

    @Override
    public String toString() {
        return "TaskRsp{" +
                "taskId=" + taskId +
                ", content='" + content + '\'' +
                ", status=" + status +
                ", note='" + note + '\'' +
                '}';
    }
}
