package com.congxiaoyao.xber_driver.mvpbase.view;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.congxiaoyao.xber_driver.R;
import com.congxiaoyao.xber_driver.mvpbase.presenter.ListLoadablePresenter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by congxiaoyao on 2017/3/16.
 */

public abstract class ListLoadableViewImpl<T extends ListLoadablePresenter, D> extends LoadableViewImpl<T> implements ListLoadableView<T, D>  {

    private ViewGroup container;
    private LayoutInflater inflater;
    private BaseQuickAdapter<D,BaseViewHolder> adapter;
    private List<D> data;

    private View emptyView;
    private View networkErrorView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        this.container = container;
        this.inflater = inflater;
        data = new ArrayList<>();
        adapter = new DefaultAdapter(getItemLayoutResId(), data);

        //底部加载更多
        bindAdapterAndDataSet(adapter, data);

        //下拉刷新
        if (isSupportSwipeRefresh()) {
            SwipeRefreshLayout swipeRefreshLayout = getSwipeRefreshLayout();
            swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    presenter.refreshData();
                }
            });
        }

        if (isSupportToolbarDoubleClick()) {
            listenToolbarDoubleClick();
        }
        return null;
    }

    /**
     * 将自定义的适配器和数据绑定设置进来 因为本来是有个默认的
     * 如果不设置进来会导致大部分接口中的功能失效
     *
     * @param adapter
     * @param data
     */
    protected void bindAdapterAndDataSet(final BaseQuickAdapter<D, BaseViewHolder> adapter, List<D> data) {
        if (adapter == null || data == null) return;
        this.data = data;
        this.adapter = adapter;
    }

    protected BaseQuickAdapter<D,BaseViewHolder> getAdapter() {
        if (adapter == null) {
            throw new RuntimeException("adapter为空 " +
                    "请确认在调用super.onCreateView()方法之后调用此方法");
        }
        return adapter;
    }

    protected List<D> getData() {
        return data;
    }

    /**
     * @return 创建一个当没有数据的时候显示的view
     */
    protected View createEmptyView() {
        return inflater.inflate(R.layout.view_empty, container, false);
    }

    /**
     * @return 创建一个当没有网络的时候显示的内容
     */
    protected View createNetworkErrorView() {
        View view = inflater.inflate(R.layout.view_network_error, container, false);
        View button = view.findViewById(R.id.btn_reload);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.subscribe();
            }
        });
        return view;
    }

    @Override
    protected void onReLoginSuccess() {
        super.onReLoginSuccess();
        presenter.subscribe();
    }

    @Override
    protected void onReLoginFailed() {
        super.onReLoginFailed();
        Toast.makeText(getContext(), "未登录", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showDataEmpty() {
        if (emptyView == null) emptyView = createEmptyView();
        adapter.setEmptyView(emptyView);
        data.clear();
        adapter.notifyDataSetChanged();
    }

    @Override
    public void showNetworkError() {
        if (networkErrorView == null) {
            networkErrorView = createNetworkErrorView();
        }
        data.clear();
        adapter.setEmptyView(networkErrorView);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void addData(List<D> data) {
        //如果参数为null 相当于拿这个方法当刷新用
        if (data == null || data.size() == 0) {
            adapter.notifyDataSetChanged();
            return;
        }
        this.data.addAll(data);
        //第一次加载数据
        if (this.data.size() == data.size()) {
            adapter.notifyDataSetChanged();
        }
        //加载更多数据
        else {
            container.post(new Runnable() {
                @Override
                public void run() {
                    adapter.loadMoreComplete();
                }
            });
        }
    }

    @Override
    public void showNothing() {
        boolean changed = false;
        if (data.size() != 0) {
            data.clear();
            changed = true;
        }

        if (adapter.getFooterLayoutCount() != 0) {
            adapter.removeAllFooterView();
            changed = true;
        }

        if (adapter.getEmptyView() != null) {
            ((ViewGroup) adapter.getEmptyView()).removeAllViews();
            changed = true;
        }

        if (changed) adapter.notifyDataSetChanged();
    }

    @Override
    protected void onToolbarDoubleClick() {
        scrollToTop();
    }

    protected boolean isSupportToolbarDoubleClick() {
        return false;
    }

    /**
     * 如果使用默认adapter 请覆写此方法绑定数据
     *
     * @param viewHolder
     * @param data
     */
    protected void convert(BaseViewHolder viewHolder, D data) {
    }

    /**
     * 如果使用默认adapter 请覆写此方法返回item的布局文件id
     * @return
     */
    protected int getItemLayoutResId() {
        return -1;
    }

    /**
     * 如果开启了下拉刷新功能 请覆写此方法绑定一个拉刷新组件
     * @return
     */
    protected SwipeRefreshLayout getSwipeRefreshLayout() {
        if (isSupportSwipeRefresh()) {
            throw new RuntimeException("请覆写 getSwipeRefreshLayout 方法绑定一个下拉刷新组件");
        }
        return null;
    }

    /**
     * @return 如果希望开启下拉刷新 返回true 否则false
     */
    public abstract boolean isSupportSwipeRefresh();

    @Override
    public void setPresenter(T presenter) {
        this.presenter = presenter;
    }

    private class DefaultAdapter extends BaseQuickAdapter<D, BaseViewHolder> {

        public DefaultAdapter(int layoutResId, List<D> data) {
            super(layoutResId, data);
        }

        @Override
        protected void convert(BaseViewHolder baseViewHolder, D d) {
            ListLoadableViewImpl.this.convert(baseViewHolder, d);
        }
    }
}
