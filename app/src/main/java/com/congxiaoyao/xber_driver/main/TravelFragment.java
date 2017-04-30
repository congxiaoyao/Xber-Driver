package com.congxiaoyao.xber_driver.main;


import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.congxiaoyao.httplib.request.gson.GsonHelper;
import com.congxiaoyao.httplib.response.Task;
import com.congxiaoyao.httplib.response.TaskRsp;
import com.congxiaoyao.xber_driver.R;
import com.congxiaoyao.xber_driver.databinding.FragmentTravelBinding;
import com.congxiaoyao.xber_driver.location.LocationService;
import com.congxiaoyao.xber_driver.mvpbase.view.LoadableViewImpl;

import java.text.SimpleDateFormat;

public class TravelFragment extends LoadableViewImpl<TravelContract.Presenter> implements TravelContract.View{

    public static final String KEY_TASK_RSP = "KEY_TASK_RSP";
    private SimpleDateFormat format = new SimpleDateFormat("yyyy年MM月dd日");
    private FragmentTravelBinding binding;
    private String json;
    private TaskRsp taskRsp;

    public static TravelFragment getInstance(TaskRsp taskRsp) {
        TravelFragment fragment = new TravelFragment();
        Bundle bundle = new Bundle();
        bundle.putString(KEY_TASK_RSP, GsonHelper.getInstance().toJson(taskRsp));
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        json = getArguments().getString(KEY_TASK_RSP);
        taskRsp = GsonHelper.getInstance().fromJson(json, TaskRsp.class);
        binding = DataBindingUtil.inflate(getLayoutInflater(savedInstanceState),
                R.layout.fragment_travel, container, false);
        super.progressBar = (ContentLoadingProgressBar) binding.getRoot().findViewById(R.id.content_progress_bar);
        binding.setFormat(format);
        binding.setTask(taskRsp);
        if (Integer.valueOf(Task.STATUS_EXECUTING).equals(taskRsp.getStatus())) {
            binding.button.setText("继 续 执 行");
        }
        binding.setPresenter(new Presenter());
        return binding.getRoot();
    }

    @Override
    public void showFinishWarning() {
        new AlertDialog.Builder(getContext())
                .setTitle("警告！")
                .setMessage("尚未到达任务点附近，确认提交吗?")
                .setPositiveButton("嗯嗯", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        presenter.finishTask(taskRsp, true);
                    }
                })
                .setNegativeButton("算了", null)
                .show();
    }

    @Override
    public void showLoading() {
        super.showLoading();
        binding.button.setEnabled(false);
    }

    @Override
    public void hideLoading() {
        super.hideLoading();
        binding.button.setEnabled(true);
    }

    @Override
    public void showFinishButton() {
        binding.button.setText("长按结束此任务");
        binding.button.setOnClickListener(null);
        binding.button.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                presenter.finishTask(taskRsp, false);
                return true;
            }
        });
    }

    public class Presenter {

        public void onStartTravelClick(View view) {
            if (taskRsp.getStatus() == Task.STATUS_DELIVERED) {
                presenter.startTask(taskRsp);
            } else {
                presenter.startService(taskRsp);
            }
        }

        public void onTaskDetailClick(View view) {
            Intent intent = new Intent(getContext(), CurrentTaskActivity.class);
            intent.putExtra(KEY_TASK_RSP, json);
            startActivity(intent);
        }
    }
}
