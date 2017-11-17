package com.youyou.uuelectric.renter.UI.order;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.rey.material.widget.Button;
import com.uu.facade.activity.pb.common.ActivityCommon;
import com.uu.facade.activity.pb.iface.ActivityInterface;
import com.uu.facade.base.cmd.Cmd;
import com.uu.facade.base.common.UuCommon;
import com.youyou.uuelectric.renter.Network.HttpResponse;
import com.youyou.uuelectric.renter.Network.NetworkTask;
import com.youyou.uuelectric.renter.Network.NetworkUtils;
import com.youyou.uuelectric.renter.Network.UUResponseData;
import com.youyou.uuelectric.renter.R;
import com.youyou.uuelectric.renter.UI.base.BaseActivity;
import com.youyou.uuelectric.renter.UUApp;
import com.youyou.uuelectric.renter.Utils.Support.Config;
import com.youyou.uuelectric.renter.Utils.Support.IntentConfig;
import com.youyou.uuelectric.renter.Utils.view.MSwipeRefreshLayout;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by liuchao on 2015/9/8.
 * 优惠券列表
 */
public class FavourSelectActivity extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener {
    /**
     * 下拉刷新控件
     */
    @InjectView(R.id.swipeRefreshLayout)
    public MSwipeRefreshLayout swipeRefreshLayout = null;
    /**
     * recyclerView列表项
     */
    @InjectView(R.id.favour_recyclerview)
    public RecyclerView favourRecyclerView = null;
    /**
     * 选择优惠券确定按钮
     */
    @InjectView(R.id.b3_button)
    public Button b3Button = null;
    /**
     * 选择优惠券text
     */
    @InjectView(R.id.favour_select_bottom_text)
    public TextView favourSelectBottomText = null;
    /**
     * 底部loading布局
     */
    @InjectView(R.id.favour_select_bottom_loadrela)
    public RelativeLayout favourSelectBottomLoadrela = null;
    /**
     * 优惠券列表
     */
    public List<FavourInfo> datas = new ArrayList<>();
    /**
     * 优惠券列表适配器
     */
    public FavourSelectAdapter favourSelectAdapter = null;
    /**
     * 分页对象
     */
    public UuCommon.PageNoRequest.Builder mPageBuilder = UuCommon.PageNoRequest.newBuilder();
    /**
     * 分页结果
     */
    public UuCommon.PageNoResult pageNoResult;
    /**
     * 当前页码
     */
    public int currentPage = 0;
    /**
     * 当前是否正在加载...
     */
    public boolean isLoading = false;
    /**
     * 优惠券ID
     */
    public int favourId;
    /**
     * 订单ID
     */
    public String orderId;

    /**
     * 选中的优惠券
     */
    public FavourInfo favourInfo = null;
    /**
     * 邀请入口文案展示
     */
    public String inviteFriendsDesc;


    /**
     * 空数据页面
     */
    @InjectView(R.id.favour_empty_rela1)
    public RelativeLayout favourEmptyRela = null;


    public String getInviteFriendsDesc() {
        return inviteFriendsDesc;
    }

    public void setInviteFriendsDesc(String inviteFriendsDesc) {
        this.inviteFriendsDesc = inviteFriendsDesc;
    }


    /**
     * adapter item点击事件
     */
    public FavourSelectAdapter.OnRadioClickListener onRadioClickListener = new FavourSelectAdapter.OnRadioClickListener() {
        @Override
        public void onItemClick(FavourInfo favourInfo) {
            FavourSelectActivity.this.favourInfo = favourInfo;
            setAnimalText(favourSelectBottomLoadrela, favourInfo);
        }
    };

    /**
     * 设置底部显示动画
     * @param favourInfo
     */
    private void setAnimalText(RelativeLayout favourSelectBottomLoadrela, FavourInfo favourInfo) {
        /*if (favourInfo == null) {
            startAnimal(favourSelectBottomLoadrela, favourInfo);
        } else {
            // 判断优惠券类型，打折优惠券还是金额优惠券
            // 优惠券类型：0-普通金额优惠券；1-折扣优惠券
            if (favourInfo.getCouponType() == 0) {
                startAnimal(favourSelectBottomLoadrela, favourInfo);
            } else if (favourInfo.getCouponType() == 1){
                startAnimal(favourSelectBottomLoadrela, favourInfo);
            }
        }*/
        startAnimal(favourSelectBottomLoadrela, favourInfo);
    }


    /**
     * 执行选中动画
     * @param
     */
    private void startAnimal(final RelativeLayout favourSelectBottomLoadrela, final FavourInfo favourInfo) {
        favourSelectBottomLoadrela.setVisibility(View.VISIBLE);
        // 请求网络获取优惠券优惠金额
        if (favourInfo != null) {
            requestCouponMessage(orderId, favourInfo.getCouponId() + "", favourSelectBottomLoadrela);
        } else {
            favourSelectBottomLoadrela.setVisibility(View.GONE);
            favourSelectBottomText.setText("未选择优惠券");
        }

    }

    /**
     * 请求服务器获取优惠券金额
     * @param orderId
     * @param couponId
     */
    private void requestCouponMessage(String orderId, String couponId, final RelativeLayout favourSelectBottomLoadrela) {
        if (!Config.isNetworkConnected(mContext)) {
            requestCouponFailure();
            return;
        }
        // 在请求之前取消之前的请求
        UUApp.getInstance().getRequestQueue().cancelAll("GetCouponDiscountInfo");

        ActivityInterface.GetCouponDiscountInfo.Request.Builder request = ActivityInterface.GetCouponDiscountInfo.Request.newBuilder();
        request.setCouponId(couponId);
        request.setOrderId(orderId);
        NetworkTask task = new NetworkTask(Cmd.CmdCode.GetCouponDiscountInfo_NL_VALUE);
        task.setBusiData(request.build().toByteArray());
        task.setTag("GetCouponDiscountInfo");
        NetworkUtils.executeNetwork(task, new HttpResponse.NetWorkResponse<UUResponseData>() {
            @Override
            public void onSuccessResponse(UUResponseData responseData) {
                if (responseData.getRet() == 0) {
                    try {
                        ActivityInterface.GetCouponDiscountInfo.Response response = ActivityInterface.GetCouponDiscountInfo.Response.parseFrom(responseData.getBusiData());
                        if (response.getRet() == 0) {
                            favourSelectBottomText.setText(response.getDiscountTips());
                            favourSelectBottomLoadrela.setVisibility(View.GONE);
                        } else {
                            requestCouponFailure();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        requestCouponFailure();
                    }
                } else {
                    requestCouponFailure();
                }
            }

            @Override
            public void onError(VolleyError errorResponse) {
                requestCouponFailure();
            }

            @Override
            public void networkFinish() {
            }
        });
    }

    /**
     * 查询优惠券优惠金额失败
     */
    private void requestCouponFailure() {

        favourSelectBottomLoadrela.setVisibility(View.GONE);
        favourSelectBottomText.setText("");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favour_select);

        ButterKnife.inject(this);
        initView();
    }

    /**
     * 初始化组件
     */
    private void initView() {
        b3Button.setText("确定");
        // 优惠券列表
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setRecyclerView(favourRecyclerView);
        swipeRefreshLayout.setColorSchemeResources(R.color.c1, R.color.c1, R.color.c1);
        favourRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        favourRecyclerView.addItemDecoration(new RecyclerItemDivider(this));

        favourSelectAdapter = new FavourSelectAdapter(this, datas, onRadioClickListener);
        favourRecyclerView.setAdapter(favourSelectAdapter);
        favourRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                // 是否满足加载更多
                if (favourSelectAdapter.isShowLoadMore()) {
                    LinearLayoutManager layoutManager = (LinearLayoutManager) favourRecyclerView.getLayoutManager();
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

        /**
         * 点击确定按钮
         */
        b3Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!Config.isNetworkConnected(mContext)) {
                    showDefaultNetworkSnackBar();
                    return;
                }

                // 若没有优惠券的话则传递-1
                if (favourInfo == null) {
                    Intent intent = new Intent();
                    intent.putExtra(IntentConfig.COUPON_ID, -1);
                    setResult(RESULT_OK, intent);
                    finish();
                } else {
                    Intent intent = new Intent();
                    intent.putExtra(IntentConfig.COUPON_ID, favourInfo.getCouponId());
                    setResult(RESULT_OK, intent);
                    finish();
                }

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 获取优惠券ID
        if (getIntent() == null)
            return;
        if (getIntent().hasExtra(IntentConfig.COUPON_ID)) {
            favourId = getIntent().getIntExtra(IntentConfig.COUPON_ID, -1);
        }
        if (getIntent().hasExtra(IntentConfig.ORDER_ID)) {
            orderId = getIntent().getStringExtra(IntentConfig.ORDER_ID);
        }
        if (TextUtils.isEmpty(orderId)) {
            return;
        }

        // 初始化数据执行网络请求优惠券列表
        initData();
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

    /**
     * 请求网络获取优惠券列表
     */
    private void loadData() {
        requestCouponCanUseList();
    }


    /**
     * 请求网络获取可用优惠券列表
     */
    private void requestCouponCanUseList() {

        if (Config.isNetworkConnected(mContext)) {
            ActivityInterface.QueryCanUseCoupons.Request.Builder request = ActivityInterface.QueryCanUseCoupons.Request.newBuilder();
            request.setOrderId(orderId);// 设置订单ID
            request.setPageRequest(mPageBuilder);
            NetworkTask task = new NetworkTask(Cmd.CmdCode.QueryCanUseCoupons_NL_VALUE);
            task.setBusiData(request.build().toByteArray());
            NetworkUtils.executeNetwork(task, new HttpResponse.NetWorkResponse<UUResponseData>() {
                @Override
                public void onSuccessResponse(UUResponseData responseData) {
                    if (responseData.getRet() == 0) {
                        try {
                            showResponseCommonMsg(responseData.getResponseCommonMsg());
                            ActivityInterface.QueryCanUseCoupons.Response response = ActivityInterface.QueryCanUseCoupons.Response.parseFrom(responseData.getBusiData());
                            // 成功
                            if (response.getRet() == 0) {
                                if (currentPage == 1) {
                                    datas.clear();
                                }
                                datas.addAll(changePb2Info(response.getUserCouponListList(), favourId));
                                inviteFriendsDesc = response.getInviteFriendsDesc();
                                // 若存在默认的选择优惠券则反选在textview中
                                setAnimalText(favourSelectBottomLoadrela, favourInfo);
                                pageNoResult = response.getPageResult();
                                favourSelectAdapter.setIsShowLoadMore(pageNoResult.getHasMore());
                                favourSelectAdapter.notifyDataSetChanged();
                                favourRecyclerView.invalidateItemDecorations();
                                favourEmptyRela.setVisibility(View.GONE);
                                mProgressLayout.showContent();
                            }
                            if(datas.size() == 0){
                                favourRecyclerView.setVisibility(View.GONE);
                                favourEmptyRela.setVisibility(View.VISIBLE);
                                return;
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
        } else {
            isLoading = false;
            mProgressLayout.showError(onErrorClickNetworkListener);
        }
    }


    /**
     * 将PB对象转换为info对象
     *
     * @return
     */
    private List<FavourInfo> changePb2Info(List<ActivityCommon.UserCouponInfo> datas, int favourId) {
        List<FavourInfo> favourList = new ArrayList<>();
        if (datas != null && datas.size() > 0) {
            for (ActivityCommon.UserCouponInfo userCouponInfo : datas) {
                FavourInfo favourInfo = new FavourInfo();
                favourInfo.setCouponAmount(userCouponInfo.getCouponAmount());
                favourInfo.setCouponDes(userCouponInfo.getCouponDes());
                favourInfo.setCouponId(userCouponInfo.getCouponId());
                favourInfo.setCouponName(userCouponInfo.getCouponName());
                favourInfo.setCouponState(userCouponInfo.getCouponState());
                favourInfo.setEndUseTime(userCouponInfo.getEndUseTime());
                favourInfo.setOrderUseMsg(userCouponInfo.getOrderUseMsg());
                favourInfo.setStartUseTime(userCouponInfo.getStartUseTime());
                // 折扣部分
                favourInfo.setCouponDiscount(userCouponInfo.getCouponDiscount());
                favourInfo.setCouponMaxReduce(userCouponInfo.getCouponMaxReduce());
                favourInfo.setCouponType(userCouponInfo.getCouponType());
                if (favourId != -1 && favourId == favourInfo.getCouponId()) {
                    favourInfo.setIsSelected(true);
                    this.favourInfo = favourInfo;
                } else {
                    favourInfo.setIsSelected(false);
                }
                favourList.add(favourInfo);
            }
        }

        return favourList;
    }
}
