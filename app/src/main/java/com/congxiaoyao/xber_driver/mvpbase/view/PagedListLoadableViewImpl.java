package com.congxiaoyao.xber_driver.mvpbase.view;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.congxiaoyao.xber_driver.R;
import com.congxiaoyao.xber_driver.mvpbase.presenter.PagedListLoadablePresenter;

import java.util.ArrayList;
import java.util.List;

/**
 * 实现了SegmentFault中内容展示的一个标准套路
 * RecyclerView+ContentLoadingProgressBar+SwipeRefreshLayout+底部加载更多
 *
 * 同时实现了网络连接失败时的展示页面
 * 没有数据时候的展示页面
 * 加载到最后一条的提示
 *
 * 关于列表数据展示的载体 这里约定使用{@link android.support.v7.widget.RecyclerView}并使用
 * {@link BaseQuickAdapter}作为适配器 所以此类已经默认空实现了适配器并将适配器中的转换(convert)方法
 * 直接交由此类的{@link PagedListLoadableViewImpl#convert(BaseViewHolder, Object)}实现
 * 所以具体的业务逻辑类可以覆写此方法完成view的适配
 * 同时必须要覆写{@link PagedListLoadableViewImpl#getItemLayoutResId()}返回item的布局文件的id
 *
 * 如果需要自定义adapter完成更为复杂的操作 需要将自己实现的adapter绑定进来 绑定使用
 * {@link PagedListLoadableViewImpl#bindAdapterAndDataSet(BaseQuickAdapter, List)}
 * 在自定义adapter的过程中你不需要亲自实现底部加载更多的功能 即使你做了实现 我也会覆盖你的实现
 * 如果这样对你有影响 请在绑定之后重新做底部加载实现即可
 *
 * 前面两段的意思是想说 我为了实现接口中的功能实现了一个默认的adapter 如果你不嫌弃 覆写下方法
 * 便可以通过{@link PagedListLoadableViewImpl#getAdapter()}直接拿来使用 比如设置给你自己的RecyclerView
 * 如果你嫌弃呢 就自己实现一个传进来 否则我没办法帮你实现接口中规定的功能
 *
 * 关于底部加载 需要实现抽象方法{@link PagedListLoadableViewImpl#isSupportLoadMore()}才能开启
 * 如果需要开启底部加载 还需要覆写{@link PagedListLoadableViewImpl#getPageSize()}
 *
 * 关于下拉刷新 需要实现抽象方法{@link PagedListLoadableViewImpl#isSupportSwipeRefresh()}开启
 * 如果需要开启下拉刷新 还需要覆写{@link PagedListLoadableViewImpl#getSwipeRefreshLayout()}
 *
 * 好！这一大段确实有点啰嗦 总结下 正常情况下你只需要实现
 * {@link PagedListLoadableViewImpl#getItemLayoutResId()}
 * {@link PagedListLoadableViewImpl#convert(BaseViewHolder, Object)}
 * {@link PagedListLoadableViewImpl#isSupportLoadMore()}
 * {@link PagedListLoadableViewImpl#getPageSize()}
 * {@link PagedListLoadableViewImpl#isSupportSwipeRefresh()}
 * {@link PagedListLoadableViewImpl#getSwipeRefreshLayout()}
 * {@link PagedListLoadableViewImpl#scrollToTop()}
 * {@link PagedListLoadableViewImpl#hideSwipeRefreshLoading()}
 * 这八个方法就可以啦！！
 *
 * Created by congxiaoyao on 2016/8/26.
 */
public abstract class PagedListLoadableViewImpl<T extends PagedListLoadablePresenter, D> extends
        LoadableViewImpl<T> implements ListLoadableView<T, D> {

    private static final int PAGE_SIZE = 20;

    private ViewGroup container;
    private LayoutInflater inflater;
    private BaseQuickAdapter<D,BaseViewHolder> adapter;
    private List<D> data;

    private View emptyView;
    private View networkErrorView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
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
        if (!isSupportLoadMore()) return;
        //滑到底部加载更多
        adapter.setLoadMoreView(new MyLoadMoreView());
        adapter.setOnLoadMoreListener(new BaseQuickAdapter.RequestLoadMoreListener() {
            @Override
            public void onLoadMoreRequested() {
                container.post(new Runnable() {@Override public void run() {
                    if (!presenter.hasMoreData()) {
                        adapter.loadMoreEnd();
                    }else {
                        presenter.loadMoreData();
                    }
                }});
            }
        });
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
    public void addData(final List<D> data) {
        //如果参数为null 相当于拿这个方法当刷新用
        if (data == null) {
            adapter.notifyDataSetChanged();
            return;
        }
        if (data.size() == 0) {
            container.post(new Runnable() {
                @Override
                public void run() {
                    adapter.loadMoreEnd();
                }
            });
            return;
        }
        //第一次加载数据
        if (adapter.getData().size() == 0) {
            adapter.addData(data);
        }
        //加载更多数据
        else {
            container.post(new Runnable() {
                @Override
                public void run() {
                    adapter.addData(data);
                    adapter.loadMoreComplete();
                }
            });
        }
        //如果没有更多数据了 就设置下footer
        if (!presenter.hasMoreData()) {
            container.post(new Runnable() {
                @Override
                public void run() {
                    adapter.loadMoreEnd();
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
     * 如果开启了底部加载更多 请覆写此方法返回精准的pageSize 默认{@link PagedListLoadableViewImpl#PAGE_SIZE}
     * @return 每一页的item的数量
     */
    protected int getPageSize() {
        return PAGE_SIZE;
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
     * @return 如果希望开启底部加载更多 返回true 否则false
     */
    public abstract boolean isSupportLoadMore();

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
            PagedListLoadableViewImpl.this.convert(baseViewHolder, d);
        }
    }
}