package com.congxiaoyao.xber_driver.simulatepost;

import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.congxiaoyao.httplib.request.CarRequest;
import com.congxiaoyao.httplib.request.TaskRequest;
import com.congxiaoyao.httplib.request.body.LaunchTaskRequest;
import com.congxiaoyao.httplib.request.body.StatusChangeRequest;
import com.congxiaoyao.httplib.request.gson.GsonHelper;
import com.congxiaoyao.httplib.request.retrofit2.XberRetrofit;
import com.congxiaoyao.httplib.request.retrofit2.adapter.rxjava.HttpException;
import com.congxiaoyao.httplib.response.CarDetail;
import com.congxiaoyao.httplib.response.ErrorInfo;
import com.congxiaoyao.httplib.response.Spot;
import com.congxiaoyao.httplib.response.TaskListRsp;
import com.congxiaoyao.httplib.response.TaskRsp;
import com.congxiaoyao.xber_driver.R;
import com.congxiaoyao.xber_driver.TAG;
import com.congxiaoyao.xber_driver.databinding.DialogAddtaskBinding;
import com.congxiaoyao.xber_driver.databinding.ItemUploadBinding;
import com.congxiaoyao.xber_driver.debug.StompActivity;
import com.congxiaoyao.xber_driver.widget.BottomDialog;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class SimulatePostActivity extends StompActivity {

    private DialogAddtaskBinding dialogBinding = null;
    private BottomDialog dialog;

    public static final Map<Long, Spot> spots = new HashMap<>();

    static {
        spots.put(1L, new Spot(1L, "天津", 39.067659254941, 117.15266523691));
        spots.put(5L, new Spot(5L, "济南", 36.802028182562, 117.21337371141));
        spots.put(7L, new Spot(7L, "青岛", 36.069648537928, 120.31782601656));
        spots.put(10L, new Spot(10L, "北京", 39.913922, 116.400102));
        spots.put(11L, new Spot(11L, "保定", 38.919706, 115.49222));
        spots.put(12L, new Spot(12L, "唐山", 39.631641, 118.123096));
        spots.put(13L, new Spot(13L, "承德", 40.970423, 117.960717));
        spots.put(14L, new Spot(14L, "衡水", 37.551351, 115.619774));
    }

    public static final Map<Long, String> latlngFileNames = new HashMap<>();

    static {
        latlngFileNames.put(5L, "jinan.latlngs");
        latlngFileNames.put(7L, "qingdao.latlngs");
        latlngFileNames.put(10L, "beijing.latlngs");
        latlngFileNames.put(11L, "baoding.latlngs");
        latlngFileNames.put(12L, "tangshan.latlngs");
        latlngFileNames.put(13L, "chengde.latlngs");
        latlngFileNames.put(14L, "hengshui.latlngs");
    }

    private List<CarThread> threads = new ArrayList<>();
    private UploadAdapter uploadAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simulate_post_activity);
        initStompClient();
    }

    @Override
    protected void onConnected() {
        super.onConnected();
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        uploadAdapter = new UploadAdapter();
        recyclerView.setAdapter(uploadAdapter);
        List<UploadState> states = DataSupport.findAll(UploadState.class);
        for (UploadState state : states) {
            Log.d(TAG.ME, "onConnected: unUpload = " + state);
            updatePoints(state.getCarId(), state.getTaskId(), state.getEndSpotId());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.meun_simulation, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_pushTask) {
            dialogBinding = DataBindingUtil.inflate(getLayoutInflater(),
                    R.layout.dialog_addtask, null, false);
            RadioGroup radioGroup = dialogBinding.radioGroup;
            int index = 0;
            for (Long pId : latlngFileNames.keySet()) {
                Spot spot = spots.get(pId);
                String spotName = spot.getSpotName();
                RadioButton child = new RadioButton(this);
                child.setText(spotName);
                child.setTag(String.valueOf(pId));
                radioGroup.addView(child, index, new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            }
            dialogBinding.setPresenter(new Presenter());
            getFreeCars(new Action1<List<CarDetail>>() {
                @Override
                public void call(List<CarDetail> carDetails) {
                    for (CarDetail carDetail : carDetails) {
                        Log.d(TAG.ME, "freeCars-->" + carDetail);
                    }
                    if (carDetails.size() > 1) {
                        while (true) {
                            int index = (int) (Math.random() * carDetails.size());
                            CarDetail carDetail = carDetails.get(index);
                            if (carDetail.getUserInfo().getUserId() == 1) continue;
                            dialogBinding.setCarDetail(carDetail);
                            dialogBinding.setDriverName(carDetail.getUserInfo().getName());
                            dialogBinding.pushButton.setEnabled(true);
                            break;
                        }
                    } else {
                        dialogBinding.setDriverName("无可用司机");
                    }
                }
            });
            dialog = new BottomDialog(this);
            dialog.setContentView(dialogBinding.getRoot());
            dialogBinding.pushButton.setEnabled(false);
            dialog.show();
            return true;
        }
        return false;
    }

    public void getYourJobAndGo(final Long userId, final Long carId) {
        getTaskByDriverId(userId, new Action1<TaskRsp>() {
            @Override
            public void call(final TaskRsp taskRsp) {
                startTask(taskRsp.getTaskId(), new Action1<String>() {
                    @Override
                    public void call(String s) {
                        Toast.makeText(SimulatePostActivity.this, "领取任务:" + s,
                                Toast.LENGTH_SHORT).show();
                        updatePoints(carId, taskRsp.getTaskId(),
                                taskRsp.getEndSpot().getSpotId());
                    }
                });
            }
        });
    }

    public void updatePoints(final Long carId, final Long taskId, Long endSpotId) {
        final CarThread thread = new CarThread(carId, taskId, endSpotId, stompClient, this);
        thread.setProgressListener(new CarThread.ProgressListener() {
            @Override
            public void onUpload(float progress) {
                if (isFinishing()) {
                    Log.d(TAG.ME, "activity is finished so you hit it");
                    thread.quit();
                    return;
                }
                for (int i = 0; i < threads.size(); i++) {
                    CarThread t = threads.get(i);
                    if (t.getCarId().equals(carId)) {
                        uploadAdapter.notifyItemChanged(i);
                        break;
                    }
                }
            }

            @Override
            public void onFinished() {
                if (isFinishing()) {
                    Log.d(TAG.ME, "activity is finished so you hit it");
                    thread.quit();
                    return;
                }
                finishTask(taskId, new Action1<String>() {
                    @Override
                    public void call(String s) {
                        Toast.makeText(SimulatePostActivity.this, "任务完成 " + s,
                                Toast.LENGTH_SHORT).show();
                        DataSupport.deleteAll(UploadState.class, "carid = ?", String.valueOf(carId));
                        for (int i = 0; i < threads.size(); i++) {
                            CarThread t = threads.get(i);
                            if (t.getCarId().equals(thread.getCarId())) {
                                threads.remove(i);
                                uploadAdapter.notifyItemRemoved(i);
                                break;
                            }
                        }
                    }
                });
            }
        });
        threads.add(thread);
        Log.d(TAG.ME, "updatePoints: threads size = " + threads.size());
        uploadAdapter.notifyDataSetChanged();
        thread.start();
    }

    /**
     * @param userId
     * @param callback 你要的taskId在里面
     */
    public void getTaskByDriverId(long userId, final Action1<TaskRsp> callback) {

        XberRetrofit.create(TaskRequest.class)
                .getTask(userId, 0, 10, 0, null, null, TOKEN)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(new Func1<TaskListRsp, List<TaskRsp>>() {
                    @Override
                    public List<TaskRsp> call(TaskListRsp taskListRsp) {
                        return taskListRsp.getTaskList();
                    }
                }).map(new Func1<List<TaskRsp>, TaskRsp>() {
            @Override
            public TaskRsp call(List<TaskRsp> taskRsps) {
                return taskRsps.get(0);
            }
        }).subscribe(new Action1<TaskRsp>() {
            @Override
            public void call(TaskRsp taskRsp) {
                callback.call(taskRsp);
            }
        });

    }

    private void startTask(long taskId, final Action1<String> callback) {
        StatusChangeRequest requestBody = new StatusChangeRequest();
        requestBody.setStatus(1);
        requestBody.setTaskId(taskId);
        XberRetrofit.create(TaskRequest.class)
                .changeTaskStatus(requestBody, TOKEN)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        callback.call(s);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Log.e(TAG.ME, "call: ", throwable);
                    }
                });
    }

    private void finishTask(long taskId, final Action1<String> callback) {
        StatusChangeRequest requestBody = new StatusChangeRequest();
        requestBody.setStatus(2);
        requestBody.setTaskId(taskId);
        XberRetrofit.create(TaskRequest.class)
                .changeTaskStatus(requestBody, TOKEN)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        callback.call(s);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
//                        handleException(throwable);
                        Log.e(TAG.ME, "call: ", throwable);
                    }
                });
    }

    private void pushTask(final LaunchTaskRequest requestBody, final Action1<String> callback) {
        XberRetrofit.create(TaskRequest.class)
                .generateTask(requestBody, TOKEN)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        callback.call(s);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Log.e(TAG.ME, "push task call: ", throwable);
                    }
                });
    }

    private void getFreeCars(final Action1<List<CarDetail>> call) {
        XberRetrofit.create(CarRequest.class).getFreeCars(now(), towDaysLater(), TOKEN)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<CarDetail>>() {
                    @Override
                    public void call(List<CarDetail> carDetail) {
                        call.call(carDetail);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        handleException(throwable);
                    }
                });
    }


    public void handleException(Throwable throwable) {
        System.out.println("handle Exception");
        if (throwable instanceof HttpException) {
            HttpException exception = (HttpException) throwable;
            System.out.println("code = " + exception.code());
            try {
                String errorbody = exception.response().errorBody().string();
                ErrorInfo errorInfo = GsonHelper.getInstance().fromJson(errorbody, ErrorInfo.class);
                Log.d(TAG.ME, "handleException: msg = " + errorInfo.getErrorMessage());
                Log.d(TAG.ME, "handleException: errorbody = " + errorbody);
                Log.d(TAG.ME, "handleException: " + errorInfo.getExceptionName());
                Log.e(TAG.ME, "handleException: ", errorInfo.getException());
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            throwable.printStackTrace();
        }
    }

    public static long now() {
        return System.currentTimeMillis();
    }

    public static long towDaysLater() {
        return nDaysLater(2);
    }

    public static long nDaysLater(int n) {
        return System.currentTimeMillis() + (n * 24 * 60 * 60 * 1000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        for (CarThread thread : threads) {
            if (thread != null) {
                thread.quit();
            }
        }
        threads.clear();
        threads = null;
        if (dialogBinding != null) {
            dialogBinding.unbind();
            dialogBinding = null;
        }
    }

    public class Presenter {

        public void onPushTask(View view) {
            LaunchTaskRequest requestBody = new LaunchTaskRequest();
            long carId = dialogBinding.getCarDetail().getCarId();
            int radioButtonId = dialogBinding.radioGroup.getCheckedRadioButtonId();
            View radioButton = dialogBinding.radioGroup.findViewById(radioButtonId);
            long endSpotId = Long.parseLong((String) radioButton.getTag());
            requestBody.setCarId(carId);
            requestBody.setContent("input content here");
            requestBody.setNote("input note here");
            requestBody.setStartSpot(1L);
            requestBody.setEndSpot(endSpotId);
            requestBody.setStartTime(now());
            requestBody.setEndTime(towDaysLater());
            pushTask(requestBody, new Action1<String>() {
                @Override
                public void call(String s) {
                    Toast.makeText(SimulatePostActivity.this, "发布任务：" + s, Toast.LENGTH_SHORT).show();
                    getYourJobAndGo(dialogBinding.getCarDetail().getUserInfo().getUserId(),
                            dialogBinding.getCarDetail().getCarId());
                }
            });
            dialog.dismiss();
        }
    }

    public static class LocBean {
        public double lat;
        public double lng;

        @Override
        public String toString() {
            return "LocBean{" +
                    "lat=" + lat +
                    ", lng=" + lng +
                    '}';
        }
    }

    class UploadAdapter extends RecyclerView.Adapter<DataBindingHolder> {

        @Override
        public DataBindingHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            ViewDataBinding binding = DataBindingUtil.inflate(getLayoutInflater(),
                    R.layout.item_upload,
                    parent, false);
            return new DataBindingHolder(binding);
        }

        @Override
        public void onBindViewHolder(DataBindingHolder holder, int position) {
            ItemUploadBinding binding = (ItemUploadBinding) holder.binding;
            CarThread thread = threads.get(position);
            binding.setCarId(String.valueOf(thread.getCarId()));
            binding.setProgress((int) (thread.getProgress() * 100));
            Log.d(TAG.ME, "onBindViewHolder: progress = " + thread.getProgress());
            binding.setEndSpotName(spots.get(thread.getEndSpotId()).getSpotName());
        }

        @Override
        public int getItemCount() {
            return threads.size();
        }
    }

    static class DataBindingHolder extends RecyclerView.ViewHolder {

        private ViewDataBinding binding;

        public DataBindingHolder(ViewDataBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
