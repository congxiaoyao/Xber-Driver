package com.congxiaoyao.xber_driver.main;


import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.congxiaoyao.httplib.request.gson.GsonHelper;
import com.congxiaoyao.httplib.response.TaskRsp;
import com.congxiaoyao.xber_driver.R;
import com.congxiaoyao.xber_driver.databinding.FragmentTravelBinding;
import com.congxiaoyao.xber_driver.location.LocationService;

import java.text.SimpleDateFormat;

public class TravelFragment extends Fragment {

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

        binding.setFormat(format);
        binding.setTask(taskRsp);
        binding.setPresenter(new Presenter());
        return binding.getRoot();
    }

    public class Presenter {

        public void onStartTravelClick(View view) {
            LocationService.startServiceForUpload(getContext(), taskRsp.getTaskId());
            ((TextView) view).setText("结束此任务");
        }

        public void onTaskDetailClick(View view) {
            Intent intent = new Intent(getContext(), CurrentTaskActivity.class);
            intent.putExtra(KEY_TASK_RSP, json);
            startActivity(intent);
        }

    }

}
