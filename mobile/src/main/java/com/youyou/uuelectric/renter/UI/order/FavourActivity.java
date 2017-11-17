package com.youyou.uuelectric.renter.UI.order;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.rey.material.widget.EditText;
import com.umeng.analytics.MobclickAgent;
import com.uu.facade.activity.pb.common.ActivityCommon;
import com.uu.facade.activity.pb.iface.ActivityInterface;
import com.uu.facade.base.cmd.Cmd;
import com.uu.facade.base.common.UuCommon;
import com.youyou.uuelectric.renter.Network.HttpResponse;
import com.youyou.uuelectric.renter.Network.NetworkTask;
import com.youyou.uuelectric.renter.Network.NetworkUtils;
import com.youyou.uuelectric.renter.Network.UUResponseData;
import com.youyou.uuelectric.renter.Network.listen.OnClickNetworkListener;
import com.youyou.uuelectric.renter.Network.listen.TextWatcherAdapter;
import com.youyou.uuelectric.renter.R;
import com.youyou.uuelectric.renter.UI.base.BaseActivity;
import com.youyou.uuelectric.renter.UI.web.H5Activity;
import com.youyou.uuelectric.renter.UI.web.H5Constant;
import com.youyou.uuelectric.renter.UI.web.url.URLConfig;
import com.youyou.uuelectric.renter.Utils.Support.Config;
import com.youyou.uuelectric.renter.Utils.UMCountConstant;
import com.youyou.uuelectric.renter.Utils.view.MSwipeRefreshLayout;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import jp.wasabeef.recyclerview.animators.adapters.AlphaInAnimationAdapter;
import jp.wasabeef.recyclerview.animators.adapters.ScaleInAnimationAdapter;

/**
 * Created by liuchao on 2015/9/8.
 * 优惠券列表
 */
public class FavourActivity extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener {
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
     * 兑换按钮
     */
    @InjectView(R.id.favour_exchange_view)
    public View favourExchangeView = null;
    /**
     * 无优惠券时邀请好友入口
     */
    @InjectView(R.id.layout_call_friend0)
    public RelativeLayout favourAddFriend=null;
    /**
     * 邀请好友入口文案
     */
    @InjectView(R.id.tv_addfriend_cuppons)
    public TextView favourAddFriendDesc;
    /**
     * 顶部按钮模块
     */
    @InjectView(R.id.favour_top_rela)
    public RelativeLayout favourTopRela = null;
    /**
     * 优惠券编辑框
     */
    @InjectView(R.id.favour_input_edit)
    public EditText favourInputEdit = null;
    /**
     * 空数据页面
     */
    @InjectView(R.id.favour_empty_rela)
    public RelativeLayout favourEmptyRela = null;
    /**
     * 清空优惠券输入图标
     */
    @InjectView(R.id.validate_favour_delete)
    public ImageView validateFavourDelete = null;
    /**
     * 优惠券列表
     */
    public List<ActivityCommon.UserCouponInfo> datas = new ArrayList<>();

    /**
     * 优惠券列表适配器
     */
    public FavourAdapter favourAdapter = null;
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
     * 邀请入口文案展示
     */
    public String inviteFriendsDesc;



    /**
     * 当前是否正在加载...
     */
    public boolean isLoading = false;
    /**
     * 是否从兑换优惠券进入优惠券列表
     */
    public boolean isCouponCode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favour);

        ButterKnife.inject(this);
        initView();

        // 初始化数据执行网络请求优惠券列表
        initData();
    }

    /**
     * 初始化组件
     */
    private void initView() {
        // 优惠券列表
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setRecyclerView(favourRecyclerView);
        swipeRefreshLayout.setColorSchemeResources(R.color.c1, R.color.c1, R.color.c1);
        favourRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        favourRecyclerView.addItemDecoration(new RecyclerItemDivider(this));
        favourAdapter = new FavourAdapter(this, datas);
        AlphaInAnimationAdapter alphaAdapter = new AlphaInAnimationAdapter(favourAdapter);
        ScaleInAnimationAdapter scaleAdapter = new ScaleInAnimationAdapter(alphaAdapter);
        favourRecyclerView.setAdapter(scaleAdapter);
        favourRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                // 是否满足加载更多
                if (favourAdapter.isShowLoadMore()) {
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
         * 兑换按钮，请求网络激活优惠码
         */
        favourExchangeView.setOnClickListener(new OnClickNetworkListener() {
            @Override
            public void onNetworkClick(View v) {
                String couponCode = favourInputEdit.getText().toString();
                // 请求网络、激活优惠码
                requestCouponCode(couponCode);
            }
        });
        /**
         * 邀请好友入口点击事件
         */

        favourAddFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, H5Activity.class);
                intent.putExtra(H5Constant.TITLE, URLConfig.getUrlInfo().getShare().getTitle());
                intent.putExtra(H5Constant.MURL, URLConfig.getUrlInfo().getShare().getUrl());
                mContext.startActivity(intent);
                MobclickAgent.onEvent(mContext, UMCountConstant.COUPON_LIST_INVITATION);

            }
        });
        /**
         * 优惠码输入框，监听输入显示删除图标
         */
        favourInputEdit.addTextChangedListener(new TextWatcherAdapter() {
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (!TextUtils.isEmpty(charSequence)) {
                    validateFavourDelete.setVisibility(View.VISIBLE);
                } else {
                    validateFavourDelete.setVisibility(View.GONE);
                }
            }
        });
        /**
         * 删除图标，清空输入内容
         */
        validateFavourDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                favourInputEdit.setText("");
            }
        });
    }

    /**
     * 修复友盟BUG
     * @param savedInstanceState
     */
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        try {
            super.onRestoreInstanceState(savedInstanceState);
        } catch (Exception e) {
            savedInstanceState = null;
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
     * 下拉刷新执行的操作
     */
    @Override
    public void onRefresh() {
        // 请求优惠券列表
        if (Config.isNetworkConnected(mContext)) {
            currentPage = 1;
            isCouponCode = true;
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
        requestCouponList();
    }

    /**
     * 请求网络获取优惠券列表
     */
    private void requestCouponList() {
        if (Config.isNetworkConnected(mContext)) {
            ActivityInterface.QueryUserCouponsList.Request.Builder request = ActivityInterface.QueryUserCouponsList.Request.newBuilder();
            request.setPageRequest(mPageBuilder);
            NetworkTask task = new NetworkTask(Cmd.CmdCode.QueryUserCouponsList_NL_VALUE);
            task.setBusiData(request.build().toByteArray());
            NetworkUtils.executeNetwork(task, new HttpResponse.NetWorkResponse<UUResponseData>() {
                @Override
                public void onSuccessResponse(UUResponseData responseData) {
                    if (responseData.getRet() == 0) {
                        try {
                            showResponseCommonMsg(responseData.getResponseCommonMsg());
                            ActivityInterface.QueryUserCouponsList.Response response = ActivityInterface.QueryUserCouponsList.Response.parseFrom(responseData.getBusiData());
                            // 成功
                            if (response.getRet() == 0) {
                                if (currentPage == 1) {
                                    datas.clear();

                                }
                                datas.addAll(response.getUserCouponListList());
                                //获取邀请好友文案
                                inviteFriendsDesc = response.getInviteFriendsDesc();
                                favourAddFriend.setVisibility(View.GONE);
                                favourEmptyRela.setVisibility(View.GONE);
                                favourAdapter.setDatas(datas);
                                // 若没有优惠券信息
                               if (datas.size() == 0) {
                                   favourRecyclerView.setVisibility(View.GONE);
                                   favourEmptyRela.setVisibility(View.VISIBLE);
                                   favourAddFriend.setVisibility(View.VISIBLE);
                                    favourAddFriendDesc.setText(inviteFriendsDesc);
                                    mProgressLayout.showContent();
                                    return;
                                } else {
                                    favourEmptyRela.setVisibility(View.GONE);
                                    favourRecyclerView.setVisibility(View.VISIBLE);
                                }
                                pageNoResult = response.getPageResult();
                                favourAdapter.setIsShowLoadMore(pageNoResult.getHasMore());

                                // 重新刷新listview，主要调用其requestLayout方法
                                favourRecyclerView.invalidateItemDecorations();
                                if (isCouponCode) {
                                    isCouponCode = false;
                                    favourRecyclerView.setAdapter(favourAdapter);
                                }
                                mProgressLayout.showContent();
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
     * 请求网络激活优惠码数据
     */
    private void requestCouponCode(String couponCode) {
        showProgress(false);
        ActivityInterface.ActivateCouponCode.Request.Builder request = ActivityInterface.ActivateCouponCode.Request.newBuilder();
        request.setCode(couponCode);
        NetworkTask task = new NetworkTask(Cmd.CmdCode.ActivateCouponCode_NL_VALUE);
        task.setBusiData(request.build().toByteArray());
        NetworkUtils.executeNetwork(task, new HttpResponse.NetWorkResponse<UUResponseData>() {
            @Override
            public void onSuccessResponse(UUResponseData responseData) {
                if (responseData.getRet() == 0) {
                    try {
                        showResponseCommonMsg(responseData.getResponseCommonMsg());
                        ActivityInterface.ActivateCouponCode.Response response = ActivityInterface.ActivateCouponCode.Response.parseFrom(responseData.getBusiData());
                        // 成功
                        if (response.getRet() == 0) {
                            currentPage = 1;
                            mPageBuilder.setPageNo(currentPage);

                            isCouponCode = true;
                            loadData();
                        }
                        // 失败
                        else if (response.getRet() == -1) {
                            // -1 失败
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        showDefaultNetworkSnackBar();
                    }
                }
            }

            @Override
            public void onError(VolleyError errorResponse) {
                showDefaultNetworkSnackBar();
            }

            @Override
            public void networkFinish() {
                dismissProgress();
            }
        });
    }


}
