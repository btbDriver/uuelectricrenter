package com.youyou.uuelectric.renter.UI.order;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;

import com.android.volley.VolleyError;
import com.uu.facade.base.cmd.Cmd;
import com.uu.facade.base.common.UuCommon;
import com.uu.facade.order.pb.common.OrderCommon;
import com.uu.facade.trip.protobuf.bean.TripInterface;
import com.uu.facade.trip.protobuf.common.TripCommon;
import com.youyou.uuelectric.renter.Network.HttpResponse;
import com.youyou.uuelectric.renter.Network.NetworkTask;
import com.youyou.uuelectric.renter.Network.NetworkUtils;
import com.youyou.uuelectric.renter.Network.UUResponseData;
import com.youyou.uuelectric.renter.R;
import com.youyou.uuelectric.renter.UI.base.BaseActivity;
import com.youyou.uuelectric.renter.UI.main.MainActivity;
import com.youyou.uuelectric.renter.UI.order.RouteAdapter.OnItemClickListener;
import com.youyou.uuelectric.renter.Utils.Support.Config;
import com.youyou.uuelectric.renter.Utils.Support.IntentConfig;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import jp.wasabeef.recyclerview.animators.FadeInAnimator;
import jp.wasabeef.recyclerview.animators.adapters.AlphaInAnimationAdapter;
import jp.wasabeef.recyclerview.animators.adapters.ScaleInAnimationAdapter;

/**
 * Created by liuchao on 2015/9/12.
 * 行程列表
 */
public class RouteActivity extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener {
    /**
     * 行程列表
     */
    @InjectView(R.id.route_recyclerview)
    public RecyclerView routeRecyclerview = null;
    /**
     * 下拉刷新列表
     */
    @InjectView(R.id.swipeRefreshLayout)
    public SwipeRefreshLayout swipeRefreshLayout = null;

    /**
     * 行程adapter
     */
    public RouteAdapter routeAdapter = null;
    /**
     * 行程列表
     */
    public List<TripCommon.TripListInfo> datas = new ArrayList<>();
    /**
     * 分页对象
     */
    public UuCommon.PageNoRequest.Builder mPageBuilder = UuCommon.PageNoRequest.newBuilder();
    /**
     * 分页结果
     */
    public UuCommon.PageResultNew pageNoResult;
    /**
     * 当前页码
     */
    public int currentPage = 0;
    /**
     * 当前是否正在加载...
     */
    public boolean isLoading = false;

    /**
     * item 点击事件
     */
    public OnItemClickListener onItemClickListener = new OnItemClickListener() {
        @Override
        public void onItemClick(TripCommon.TripListInfo tripListInfo) {
            if (tripListInfo == null) {
                showDefaultNetworkSnackBar();
                return;
            }
            /**
             * 0：已取消
             * 1：待取车
             * 2：用车中
             * 4：待支付
             * 8：支付完成
             */
            // 已取消
            if (tripListInfo.getStatus() == OrderCommon.OrderFormStatus.renter_cancel_VALUE) {
                Intent intent = new Intent(mContext, TripOrderDetailActivity.class);
                intent.putExtra(IntentConfig.ORDER_ID, tripListInfo.getOrderId());
                intent.putExtra(IntentConfig.KEY_FROM_TRIP, true);
                intent.putExtra(IntentConfig.KEY_PAGE_TYPE, TripOrderDetailActivity.PAGE_TYPE_ORDER_DETAIL);
                startActivity(intent);
            }
            // 待取车
            else if (tripListInfo.getStatus() == OrderCommon.OrderFormStatus.wait_get_car_VALUE) {
                Intent intent = new Intent(mContext, MainActivity.class);
                intent.putExtra("goto", MainActivity.GOTO_GET_CAR);
                intent.putExtra(IntentConfig.ORDER_ID, tripListInfo.getOrderId());

                Config.setOrderId(mContext, tripListInfo.getOrderId());
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            }
            // 用车中
            else if (tripListInfo.getStatus() == OrderCommon.OrderFormStatus.using_car_VALUE) {
                Intent intent = new Intent(mContext, MainActivity.class);
                intent.putExtra("goto", MainActivity.GOTO_CURRENT_STROKE);

                Config.setOrderId(mContext, tripListInfo.getOrderId());
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            }
            // 待支付
            else if (tripListInfo.getStatus() == OrderCommon.OrderFormStatus.wait_pay_VALUE) {
                Intent intent = new Intent(mContext, MainActivity.class);
                intent.putExtra("goto", MainActivity.GOTO_NEED_PAY_ORDER);

                Config.setOrderId(mContext, tripListInfo.getOrderId());
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            }
            // 完成
            else if (tripListInfo.getStatus() == OrderCommon.OrderFormStatus.order_finish_VALUE) {
                Intent intent = new Intent(mContext, TripOrderDetailActivity.class);
                intent.putExtra(IntentConfig.ORDER_ID, tripListInfo.getOrderId());
                intent.putExtra(IntentConfig.KEY_FROM_TRIP, true);
                intent.putExtra(IntentConfig.KEY_PAGE_TYPE, TripOrderDetailActivity.PAGE_TYPE_TRIP_DETAIL);
                startActivity(intent);
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route);

        ButterKnife.inject(this);
        initView();

        // 初始化数据
        initData();
    }

    /**
     * 下拉刷新执行的操作
     */
    @Override
    public void onRefresh() {
        // 请求优惠券列表
        if (Config.isNetworkConnected(mContext)) {
            currentPage = 1;
            mPageBuilder.setPageNo(currentPage);
            loadData();
        } else {
            swipeRefreshLayout.setRefreshing(false);
            showDefaultNetworkSnackBar();
        }
    }

    /**
     * 初始化数据
     */
    private void initData() {
        mProgressLayout.showLoading();
        currentPage = 1;
        mPageBuilder.setPageNo(currentPage);
        swipeRefreshLayout.setRefreshing(true);
        loadData();
    }

    /**
     * 初始化组件
     */
    private void initView() {
        // 行程列表
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeResources(R.color.c1, R.color.c1, R.color.c1);
        routeRecyclerview.setLayoutManager(new LinearLayoutManager(this));
        routeRecyclerview.setItemAnimator(new FadeInAnimator());
        routeRecyclerview.addItemDecoration(new RecyclerItemDivider(this));
        routeAdapter = new RouteAdapter(this, datas, onItemClickListener);
        AlphaInAnimationAdapter alphaAdapter = new AlphaInAnimationAdapter(routeAdapter);
        ScaleInAnimationAdapter scaleAdapter = new ScaleInAnimationAdapter(alphaAdapter);
        routeRecyclerview.setAdapter(scaleAdapter);
        routeRecyclerview.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                // 是否满足加载更多
                if (routeAdapter.isShowLoadMore()) {
                    LinearLayoutManager layoutManager = (LinearLayoutManager) routeRecyclerview.getLayoutManager();
                    int totalItemCount = layoutManager.getItemCount();
                    int visibleItem = layoutManager.getChildCount();
                    if (visibleItem > 0 && layoutManager.findLastVisibleItemPosition() >= totalItemCount - 1) {
                        if (!isLoading) {
                            isLoading = true;
                            if (Config.isNetworkConnected(mContext)) {
                                if (pageNoResult != null && pageNoResult.getHasMore()) {
                                    currentPage++;
                                    mPageBuilder.setPageNo(currentPage);
                                    loadData();
                                }
                            } else {
                                isLoading = false;
                            }
                        }
                    }
                }
            }
        });
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item == null)
            return true;
        if (item.getItemId() == android.R.id.home || item.getItemId() == 0) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 请求网络获取订单列表
     */
    private void loadData() {
        TripInterface.OrderTripList.Request.Builder request = TripInterface.OrderTripList.Request.newBuilder();
        request.setPageRequest(mPageBuilder);
        NetworkTask task = new NetworkTask(Cmd.CmdCode.TripList_NL_VALUE);
        task.setBusiData(request.build().toByteArray());
        NetworkUtils.executeNetwork(task, new HttpResponse.NetWorkResponse<UUResponseData>() {
            @Override
            public void onSuccessResponse(UUResponseData responseData) {
                if (responseData.getRet() == 0) {
                    try {
                        showResponseCommonMsg(responseData.getResponseCommonMsg());
                        TripInterface.OrderTripList.Response response = TripInterface.OrderTripList.Response.parseFrom(responseData.getBusiData());
                        // 成功
                        if (response.getRet() == 0) {
                            if (currentPage == 1) {
                                datas.clear();
                            }
                            datas.addAll(response.getOrderInfosList());
                            if (datas.size() == 0) {
                                mProgressLayout.setEmptyView("暂无行程");
                            } else {
                                pageNoResult = response.getPageResult();
                                routeAdapter.setIsShowLoadMore(pageNoResult.getHasMore());
                                routeAdapter.notifyDataSetChanged();
                                routeRecyclerview.invalidateItemDecorations();
                                // routeRecyclerview.setAdapter(routeAdapter);
                                mProgressLayout.showContent();
                            }
                        }
                        // 失败
                        else if (response.getRet() == -1) {
                            // -1 失败
                            mProgressLayout.showError(onErrorClickNetworkListener);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        mProgressLayout.showError(onErrorClickNetworkListener);
                    }
                }
            }

            @Override
            public void onError(VolleyError errorResponse) {
                mProgressLayout.showError(onErrorClickNetworkListener);
            }

            @Override
            public void networkFinish() {
                isLoading = false;
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    /**
     * 错误信息点击事件
     */
    View.OnClickListener onErrorClickNetworkListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!Config.isNetworkConnected(mContext)) {
                return;
            }
            mProgressLayout.showLoading();
            loadData();
        }
    };
}
