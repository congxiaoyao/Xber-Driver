package com.congxiaoyao.xber_driver.driverdetail;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseViewHolder;
import com.congxiaoyao.httplib.request.gson.GsonHelper;
import com.congxiaoyao.httplib.response.Spot;
import com.congxiaoyao.httplib.response.TaskRsp;
import com.congxiaoyao.xber_driver.Driver;
import com.congxiaoyao.xber_driver.R;
import com.congxiaoyao.xber_driver.main.CurrentTaskActivity;
import com.congxiaoyao.xber_driver.main.TravelFragment;
import com.congxiaoyao.xber_driver.mvpbase.view.PagedListLoadableViewImpl;

import java.text.SimpleDateFormat;

/**
 * Created by guo on 2017/3/29.
 */

public class HistoryTaskFragment
        extends PagedListLoadableViewImpl<HistoryTaskContract.Presenter, TaskRsp>
        implements HistoryTaskContract.View {

    private RecyclerView recyclerView;
    private SimpleDateFormat format = new SimpleDateFormat("yyyy年MM月dd日");
    private SwipeRefreshLayout swipeRefreshLayout;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history_task, container, false);
        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        progressBar = (ContentLoadingProgressBar) view.findViewById(R.id.content_progress_bar);
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(getContext(), R.color.colorPrimary));
        super.onCreateView(inflater, container, savedInstanceState);
        recyclerView.setAdapter(getAdapter());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        if (presenter != null) presenter.subscribe();
        return view;
    }

    @Override
    public boolean isSupportLoadMore() {
        return true;
    }

    @Override
    public boolean isSupportSwipeRefresh() {
        return true;
    }

    @Override
    protected SwipeRefreshLayout getSwipeRefreshLayout() {
        return swipeRefreshLayout;
    }

    @Override
    protected boolean isSupportToolbarDoubleClick() {
        return true;
    }

    @Override
    public void scrollToTop() {
        recyclerView.smoothScrollToPosition(0);
    }

    @Override
    protected int getPageSize() {
        return HistoryTaskPresenterImpl.PAGE_SIZE;
    }

    @Override
    public void addExecutingTask(final TaskRsp taskRsp) {
        recyclerView.post(new Runnable() {
            @Override
            public void run() {
                getAdapter().addHeaderView(createHeaderTask(taskRsp));
            }
        });
    }

    @Override
    public Long getDriverId() {
        return Driver.fromSharedPreference(getContext()).getUserId();
    }

    @Override
    public void clearHeader() {
        getAdapter().removeAllHeaderView();
    }

    @Override
    protected int getItemLayoutResId() {
        return R.layout.item_history_task;
    }

    @Override
    protected void convert(final BaseViewHolder viewHolder, TaskRsp taskRsp) {
        super.convert(viewHolder, taskRsp);
        viewHolder.setText(R.id.tv_start_spot, taskRsp.getStartSpot().getSpotName());
        viewHolder.setText(R.id.tv_end_spot, taskRsp.getEndSpot().getSpotName());
        viewHolder.setText(R.id.tv_start_time, format.format(taskRsp.getRealStartTime()));
        viewHolder.setText(R.id.tv_transport_content, taskRsp.getContent());
        viewHolder.getView(R.id.btn_more).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onTimeButtonClick(getData().get(viewHolder.getAdapterPosition()));
            }
        });
        viewHolder.getView(R.id.ll_container).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TaskRsp taskRsp = getData().get(viewHolder.getAdapterPosition());
                String json = GsonHelper.getInstance().toJson(taskRsp);
                Intent intent = new Intent(getContext(), CurrentTaskActivity.class);
                intent.putExtra(TravelFragment.KEY_TASK_RSP, json);
                startActivity(intent);
            }
        });
    }

    public View createHeaderTask(TaskRsp taskRsp) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.item_history_task,
                recyclerView, false);
        ((TextView) view.findViewById(R.id.tv_start_spot))
                .setText(taskRsp.getStartSpot().getSpotName());

        ((TextView) view.findViewById(R.id.tv_end_spot))
                .setText(taskRsp.getEndSpot().getSpotName());

        ((TextView) view.findViewById(R.id.tv_start_time))
                .setText(format.format(taskRsp.getStartTime()));

        ((TextView) view.findViewById(R.id.tv_transport_content))
                .setText(taskRsp.getContent());

        TextView state = (TextView) view.findViewById(R.id.tv_state);
        state.setText("运送中");
        state.setTextColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));

        view.findViewById(R.id.btn_more).setVisibility(View.GONE);
        return view;
    }

    private SimpleDateFormat format2 = null;
    public void onTimeButtonClick(TaskRsp taskRsp) {
        if (format2 == null) {
            format2 = new SimpleDateFormat("yyyy年MM月dd日 HH点mm分");
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setItems(new String[]{format2.format(taskRsp.getRealStartTime())
                , "至"
                , format2.format(taskRsp.getRealEndTime())}, null).show();
    }

    @Override
    public void hideSwipeRefreshLoading() {
        swipeRefreshLayout.setRefreshing(false);
    }
}
