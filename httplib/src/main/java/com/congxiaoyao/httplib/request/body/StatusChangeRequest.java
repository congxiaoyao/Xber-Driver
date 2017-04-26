package com.congxiaoyao.httplib.request.body;

/**
 * Created by Jaycejia on 2017/2/18.
 */
public class StatusChangeRequest {
    private Long taskId;
    private Integer status;

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
