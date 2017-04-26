package com.congxiaoyao.xber_driver.register;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.congxiaoyao.httplib.request.CarRequest;
import com.congxiaoyao.httplib.request.LoginRequest;
import com.congxiaoyao.httplib.request.UserRequest;
import com.congxiaoyao.httplib.request.body.CarDriverReq;
import com.congxiaoyao.httplib.request.body.LoginBody;
import com.congxiaoyao.httplib.request.body.NewCar;
import com.congxiaoyao.httplib.request.body.User;
import com.congxiaoyao.httplib.request.retrofit2.XberRetrofit;
import com.congxiaoyao.httplib.response.Car;
import com.congxiaoyao.httplib.response.LoginInfoResponse;
import com.congxiaoyao.xber_driver.R;
import com.congxiaoyao.xber_driver.TAG;
import com.congxiaoyao.xber_driver.databinding.ActivityRegisterBinding;
import com.congxiaoyao.xber_driver.utils.Token;

import java.util.List;

import me.imid.swipebacklayout.lib.app.SwipeBackActivity;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class RegisterActivity extends SwipeBackActivity implements View.OnClickListener {

    private ActivityRegisterBinding binding;
    private ContentLoadingProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_register);
        binding.btnRegister.setOnClickListener(this);
        progressBar = (ContentLoadingProgressBar) binding.getRoot().findViewById(R.id.content_progress_bar);
        binding.etPlate.append((int) (Math.random() * 100000) + "");
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
    }

    @Override
    public void onClick(View v) {
        if (!checkInfo()) return;
        final User user = getUserByViews();
        final String[] token = {""};
        final Long[] userId = new Long[1];
        progressBar.setVisibility(View.VISIBLE);
        XberRetrofit.create(UserRequest.class).registerDriver(user, ""
        ).flatMap(new Func1<String, Observable<LoginInfoResponse>>() {
            @Override
            public Observable<LoginInfoResponse> call(String s) {
                return XberRetrofit.create(LoginRequest.class).login(new LoginBody(user.getUsername(),
                        user.getPassword(), Token.getClientId(RegisterActivity.this)));
            }
        }).flatMap(new Func1<LoginInfoResponse, Observable<String>>() {
            @Override
            public Observable<String> call(LoginInfoResponse loginInfoResponse) {
                userId[0] = loginInfoResponse.getUserId();
                return XberRetrofit.create(CarRequest.class).addCar(getCarByViews(),
                        token[0] = loginInfoResponse.getAuthToken());
            }
        }).flatMap(new Func1<String, Observable<List<Car>>>() {
            @Override
            public Observable<List<Car>> call(String s) {
                return XberRetrofit.create(CarRequest.class).getCarsWithoutDriver(token[0]);
            }
        }).flatMap(new Func1<List<Car>, Observable<String>>() {
            @Override
            public Observable<String> call(List<Car> cars) {
                for (Car car : cars) {
                    if (car.getPlate().equals(getCarByViews().getPlate())) {
                        CarDriverReq req = new CarDriverReq(car.getCarId(), userId[0]);
                        return XberRetrofit.create(CarRequest.class).changeCarDriver(req, token[0]);
                    }
                }
                throw new RuntimeException();
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<String>() {
            @Override
            public void call(String s) {
                Toast.makeText(RegisterActivity.this, s, Toast.LENGTH_LONG).show();
                finish();
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                Log.e(TAG.ME, "call: ", throwable);
                progressBar.hide();
                Toast.makeText(RegisterActivity.this, "注册失败 请重试", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private boolean checkInfo() {
        if (!checkNullAndToast(binding.etUserId, "账号")) {
            return false;
        }
        if (!checkNullAndToast(binding.etPassword, "密码")) {
            return false;
        }
        if (!checkNullAndToast(binding.etUsername, "姓名")) {
            return false;
        }
        if (!checkNullAndToast(binding.etOld, "年龄")) {
            return false;
        }else {
            try {
                Integer.parseInt(binding.etOld.getText().toString());
            } catch (Exception e) {
                return false;
            }
        }
        if (!checkNullAndToast(binding.etPlate, "车牌")) {
            return false;
        }
        if (!checkNullAndToast(binding.etSpec, "车型")) {
            return false;
        }
        return true;
    }

    public boolean checkNullAndToast(EditText editText, String type) {
        if (editText.getText().toString().equals("")) {
            Toast.makeText(this, type + "不允许为空", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    public User getUserByViews() {
        User user = new User();
        user.setUsername(binding.etUserId.getText().toString());
        user.setPassword(binding.etPassword.getText().toString());
        user.setName(binding.etUsername.getText().toString());
        user.setAge(Integer.valueOf(binding.etOld.getText().toString()));
        user.setUserType((byte) 1);
        user.setGender((byte) 1);
        return user;
    }

    public NewCar getCarByViews() {
        NewCar newCar = new NewCar();
        newCar.setPlate(binding.etPlate.getText().toString());
        newCar.setSpec(binding.etSpec.getText().toString());
        return newCar;
    }
}
