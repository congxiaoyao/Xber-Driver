package com.congxiaoyao.xber_driver.debug;

import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;

import com.congxiaoyao.httplib.NetWorkConfig;
import com.congxiaoyao.location.utils.GPSEncoding;
import com.congxiaoyao.xber_driver.R;
import com.congxiaoyao.xber_driver.TAG;
import com.congxiaoyao.xber_driver.databinding.ActivityUploadBinding;
import com.congxiaoyao.xber_driver.debug.StompActivity;

import java.util.Arrays;
import java.util.Random;

import static com.congxiaoyao.location.model.GpsSampleOuterClass.GpsSample;

public class UploadActivity extends StompActivity {

    private ActivityUploadBinding binding;
    private Handler handler = new Handler();

    private static final String SEND_TO = "/app/gpsSample/upload";
    private static final String WS_URL = NetWorkConfig.WS_URL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ViewDataBinding viewDataBinding = DataBindingUtil.setContentView(this, R.layout.activity_upload);
        binding = (ActivityUploadBinding) viewDataBinding;
        binding.setPresenter(new Presenter());
        binding.seekBar.setOnSeekBarChangeListener(seekBarListener);

        initStompClient(WS_URL);
    }

    public class Presenter {

        public void onUploadClick(View view) {
            Random random = new Random();
            byte[] bytes = new byte[100];
            random.nextBytes(bytes);
            for (int i = 0; i < bytes.length; i++) {
                if(bytes[i] == 0) bytes[i] = 1;
            }
            Log.d(com.congxiaoyao.xber_driver.TAG.ME, "onUploadClick: send " + Arrays.toString(bytes));
            stompClient.send(SEND_TO, bytes).subscribe();
//            onUpload();
        }
    }

    public void onUpload() {
        if (!stompClient.isConnected()) return;
        String text = binding.editText.getText().toString();
        if (text.equals("")) return;

        final long carId = Long.parseLong(text);
        final int interval = binding.seekBar.getProgress() * 1000;

        final double[] location = {100, 100};
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (stompClient == null || !stompClient.isConnected()) return;
                GpsSample.Builder builder = GpsSample.newBuilder();
                builder.setLat(location[0] += 0.00006f);
                builder.setLng(location[1] += 0.00006f);
                builder.setCarId(carId);
                builder.setVlat(0.00006f);
                builder.setVlng(0.00006f);
                builder.setTaskId(10);
                builder.setTime(System.currentTimeMillis());
                GpsSample gpsSample = builder.build();
                Log.d(TAG.ME, "sending:" + gpsSample);
                byte[] encode = GPSEncoding.encode(gpsSample.toByteArray());
                stompClient.send(SEND_TO, new String(encode)).subscribe();
                handler.postDelayed(this, interval);
            }
        });
    }

    private SeekBar.OnSeekBarChangeListener seekBarListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            binding.tvJiange.setText("时间间隔:" + progress);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };
}
