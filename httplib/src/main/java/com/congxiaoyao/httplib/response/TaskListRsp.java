package com.congxiaoyao.httplib.response;

import java.util.Date;
import java.util.List;

/**
 * Created by Jaycejia on 2017/2/18.
 */
public class TaskListRsp implements Pageable<TaskRsp> {
    private Date timestamp;
    private List<TaskRsp> taskList;
    private Page page;

    public TaskListRsp() {
    }

    public TaskListRsp(Date timestamp, List<TaskRsp> taskList) {
        this.timestamp = timestamp;
        this.taskList = taskList;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public List<TaskRsp> getTaskList() {
        return taskList;
    }

    public void setTaskList(List<TaskRsp> taskList) {
        this.taskList = taskList;
    }

    @Override
    public Page getPage() {
        return page;
    }

    @Override
    public List<TaskRsp> getCurrentPageData() {
        return getTaskList();
    }

    @Override
    public Date getTimeStamp() {
        return timestamp;
    }
}
