package com.congxiaoyao.xber_driver.debug;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.congxiaoyao.httplib.NetWorkConfig;
import com.congxiaoyao.stopmlib.LifecycleEvent;
import com.congxiaoyao.stopmlib.Stomp;
import com.congxiaoyao.stopmlib.client.StompClient;
import com.congxiaoyao.stopmlib.client.StompMessage;
import com.congxiaoyao.xber_driver.R;
import com.congxiaoyao.xber_driver.TAG;
import com.congxiaoyao.xber_driver.utils.Token;

import org.java_websocket.WebSocket;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Action2;

public class StompDebugActivity extends AppCompatActivity {

    private StompClient stompClient;
    private List<MessageBox> messageBoxes;
    private ViewPager viewPager;
    private MessageBoxPager messageBoxPager;

    private static final String HEADER_KEY = "Authorization";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stomp_debug);

        setPreference("def_receive", "/user/37/system");
        setPreference("def_send", "/app/helloUser");

        messageBoxes = new ArrayList<>();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.not_connected);

        BottomBarPager bottomBarPager = new BottomBarPager();
        ((ViewPager) findViewById(R.id.vp_send)).setAdapter(bottomBarPager);
        bottomBarPager.onSendMsg = new Action2<String, String>() {
            @Override
            public void call(final String path, String msg) {
                if (stompClient == null) return;
                Log.d(TAG.ME, "onSend:" + msg);
                final MessageBox messageBox = getCurrentMessageBox();

                stompClient.send(path, msg)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Action1<Void>() {
                            @Override
                            public void call(Void aVoid) {
                                if (messageBox != null) {
                                    messageBox.println(getString(R.string.send_fail));
                                }
                            }
                        }, new Action1<Throwable>() {
                            @Override
                            public void call(Throwable throwable) {
                                if (messageBox != null) {
                                    messageBox.println(getString(R.string.send_success));
                                    Log.d(TAG.ME, "send finished path = " + path);
                                }
                            }
                        });
            }
        };

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);

        messageBoxPager = new MessageBoxPager();
        viewPager = (ViewPager) findViewById(R.id.vp_message_box);
        viewPager.setAdapter(messageBoxPager);
        messageBoxPager.onRequestTopic = new Action2<String, MessageBox>() {
            @Override
            public void call(String topic, final MessageBox messageBox) {
                if (stompClient == null) return;
                stompClient.topic(topic)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Action1<StompMessage>() {
                            @Override
                            public void call(final StompMessage message) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        messageBox.println(message.getPayload());
                                    }
                                });
                            }
                        });
                messageBox.println("listening topic:" + topic);
            }
        };
        tabLayout.setupWithViewPager(viewPager, true);
    }

    public void initStompClient() {
        Map<String, String> header = new HashMap<>();
        header.put(HEADER_KEY, Token.value);
        stompClient = Stomp.over(WebSocket.class, NetWorkConfig.WS_URL, header);
        stompClient.lifecycle()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<LifecycleEvent>() {
                    @Override
                    public void call(LifecycleEvent lifecycleEvent) {
                        MessageBox messageBox = getCurrentMessageBox();
                        String hint = "";
                        switch (lifecycleEvent.getType()) {
                            case OPENED:
                                hint = "OPENED";
                                getSupportActionBar().setTitle(R.string.connected);
                                break;
                            case CLOSED:
                                hint = "CLOSED";
                                getSupportActionBar().setTitle(R.string.not_connected);
                                break;
                            case ERROR:
                                getSupportActionBar().setTitle(R.string.not_connected);
                                hint = "ERROR";
                                break;
                        }
                        if (messageBox != null) messageBox.println(hint);
                        else Toast.makeText(StompDebugActivity.this, hint, Toast.LENGTH_SHORT).show();
                    }
                });
        stompClient.connect();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add("连接");
        menu.getItem(0).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (stompClient != null) {
            item.setTitle(R.string.connect);
            stompClient.disconnect();
            stompClient = null;
            return true;
        }
        getSupportActionBar().setTitle(R.string.connecting);
        initStompClient();
        item.setTitle(R.string.cutdown);
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (stompClient != null) {
            stompClient.disconnect();
            stompClient = null;
            messageBoxes.clear();
        }
    }

    public MessageBox getCurrentMessageBox() {
        int position = viewPager.getCurrentItem();
        if (position >= messageBoxes.size()) return null;
        return messageBoxes.get(position);
    }

    public MessageBox getMessageBoxByTopic(String topic) {
        if (messageBoxPager == null) return null;
        int position = messageBoxPager.getPositionByTopic(topic);
        if (position == -1) return null;
        return position >= messageBoxes.size() ? null : messageBoxes.get(position);
    }

    class MessageBoxPager extends PagerAdapter {

        private List<View> viewList;
        private List<String> titles;
        private List<String> topics;

        Action2<String, MessageBox> onRequestTopic;

        MessageBoxPager() {
            this.titles = new ArrayList<>();
            this.viewList = new ArrayList<>();
            topics = new ArrayList<>();
            View view = View.inflate(StompDebugActivity.this, R.layout.message_box, null);
            initBar(view);
            viewList.add(view);
            titles.add(getResources().getString(R.string.please_add));
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView(viewList.get(position));
        }

        @Override
        public int getCount() {
            return viewList.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return object == view;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View view = viewList.get(position);
            container.addView(view);
            return view;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return titles.get(position);
        }

        private void initBar(final View view) {
            (view.findViewById(R.id.swipe_refresh)).setEnabled(false);
            Button button = (Button) view.findViewById(R.id.btn_ok);
            final EditText editText = (EditText) view.findViewById(R.id.et_topic);
            String defTopic = getPreference("def_receive");
            editText.setText(defTopic);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    InputMethodManager imm = (InputMethodManager)
                            getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
                    if (onRequestTopic == null) return;
                    String topic = editText.getText().toString().trim();
                    topics.add(topic);
                    if (topic.equals("")) return;
                    String[] split = topic.split("/");
                    topic = split[split.length - 1];
                    titles.set(titles.size() - 1, topic);
                    view.findViewById(R.id.ll_topic).setVisibility(View.GONE);
                    addView();
                    final SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout)
                            findViewById(R.id.swipe_refresh);
                    swipeRefreshLayout.setEnabled(true);
                    final MessageBox messageBox = new MessageBox(
                            (TextView) view.findViewById(R.id.tv_content),
                            (ScrollView) view.findViewById(R.id.scrollView));
                    messageBoxes.add(messageBox);
                    swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                        @Override
                        public void onRefresh() {
                            messageBox.clear();
                            swipeRefreshLayout.setRefreshing(false);
                        }
                    });
                    onRequestTopic.call(topics.get(topics.size()-1), messageBox);
                }
            });
        }

        private void addView() {
            View view = View.inflate(StompDebugActivity.this, R.layout.message_box, null);
            viewList.add(view);
            titles.add(getString(R.string.please_add));
            initBar(view);
            notifyDataSetChanged();
        }

        public String getTopicByPosition(int position) {
            if (position >= topics.size()) return null;
            return topics.get(position);
        }

        public int getPositionByTopic(String topic) {
            int position = topics.indexOf(topic);
            return position;
        }
    }

    private String getPreference(String key) {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(StompDebugActivity.this);
        return sharedPreferences.getString(key, null);
    }

    private void setPreference(String key, String value) {
        PreferenceManager
                .getDefaultSharedPreferences(StompDebugActivity.this)
                .edit().putString(key, value).apply();
    }

    class BottomBarPager extends PagerAdapter {

        private List<View> viewList;
        Action2<String, String> onSendMsg;
        View addMoreView;

        BottomBarPager() {
            viewList = new ArrayList<>();
            addMoreView = View.inflate(StompDebugActivity.this, R.layout.bottom_bar, null);
            EditText editText = (EditText) addMoreView.findViewById(R.id.et_send);
            editText.setText(getPreference("def_send"));
            viewList.add(addMoreView);
            initAddButton((Button) addMoreView.findViewById(R.id.btn_send));
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            if (viewList.size() <= position) return;
            container.removeView(viewList.get(position));
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View child = viewList.get(position);
            if (child.getParent() == null) {
                container.addView(child);
            }
            return child;
        }

        @Override
        public int getCount() {
            return viewList.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        public View addView() {
            final View view = View.inflate(StompDebugActivity.this, R.layout.bottom_bar, null);
            Button button = (Button) addMoreView.findViewById(R.id.btn_send);
            final EditText editText = (EditText) addMoreView.findViewById(R.id.et_send);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String msg = editText.getText().toString().trim();
                    if (msg.equals("")) return;
                    String path = (String) view.getTag();
                    if (onSendMsg != null) onSendMsg.call(path, msg);
                }
            });
            String path = editText.getText().toString();
            view.setTag(path);
            editText.setHint(path);
            editText.getBackground().setColorFilter(
                    ContextCompat.getColor(StompDebugActivity.this, R.color.colorPrimary),
                    PorterDuff.Mode.SRC_IN);
            button.getBackground().setColorFilter(
                    ContextCompat.getColor(StompDebugActivity.this, R.color.colorPrimary),
                    PorterDuff.Mode.SRC_IN);
            button.setAlpha(0.8f);
            button.setTextColor(Color.WHITE);
            editText.setText("");
            button.setText(R.string.send);
            addMoreView = view;
            initAddButton((Button) view.findViewById(R.id.btn_send));
            viewList.add(view);
            return view;
        }

        public void initAddButton(Button button) {
            button.setText("ADD");
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    EditText editText = (EditText) addMoreView.findViewById(R.id.et_send);
                    String path = editText.getText().toString().trim();
                    if (path.equals("")) return;
                    addView();
                    notifyDataSetChanged();
                }
            });
        }
    }

    class MessageBox {

        private TextView textView;
        private ScrollView scrollView;

        public MessageBox(TextView textView, ScrollView scrollView) {
            this.scrollView = scrollView;
            this.textView = textView;
        }

        public void println(Object object) {
            print(object);
            textView.append("\n");
            scrollView.post(new Runnable() {
                @Override
                public void run() {
                    scrollView.fullScroll(View.FOCUS_DOWN);
                }
            });
        }

        public void print(Object object) {
            if (object == null) {
                textView.append("null");
            } else textView.append(object.toString());
            scrollView.post(new Runnable() {
                @Override
                public void run() {
                    scrollView.fullScroll(View.FOCUS_DOWN);
                }
            });
        }

        public void clear() {
            textView.setText("");
        }
    }

}
