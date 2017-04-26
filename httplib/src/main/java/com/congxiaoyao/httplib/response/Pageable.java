package com.congxiaoyao.httplib.response;

import java.util.Date;
import java.util.List;

/**
 * Created by congxiaoyao on 2017/3/14.
 */

public interface Pageable<T> {

    Page getPage();

    List<T> getCurrentPageData();

    Date getTimeStamp();
}
