package com.congxiaoyao.httplib.response;

import com.congxiaoyao.httplib.response.exception.EmptyDataException;

import java.util.Date;
import java.util.List;

import rx.Observable;
import rx.functions.Action1;
import rx.functions.Action2;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by congxiaoyao on 2017/3/14.
 */

public class ResponsePreProcess {

    public static <T> Observable.Transformer
            <Pageable<T>, List<T>> pagedListDataToBeanList(final Action2<Page,Date> savePage) {
        return new Observable.Transformer<Pageable<T>, List<T>>() {
            @Override
            public Observable<List<T>> call(Observable<Pageable<T>> pageableObservable) {
                return pageableObservable.doOnNext(new Action1<Pageable<T>>() {
                    @Override
                    public void call(Pageable<T> pageable) {
                        //判断list是否为空
                        checkListNull(pageable);
                        //经过flat之后page就被舍弃了，所以在这里做下保存操作
                        if (savePage != null)
                            savePage.call(pageable.getPage(), pageable.getTimeStamp());
                    }
                }).map(new Func1<Pageable<T>, List<T>>() {
                    @Override
                    public List<T> call(Pageable<T> tPageable) {
                        return tPageable.getCurrentPageData();
                    }
                }).subscribeOn(Schedulers.io());
            }
        };
    }

    private static void checkListNull(Pageable pageable) {
        if (pageable == null) {
            throw new EmptyDataException();
        }
        List currentPageData = pageable.getCurrentPageData();
        if (currentPageData == null || currentPageData.size() == 0) {
            throw new EmptyDataException();
        }
    }
}
