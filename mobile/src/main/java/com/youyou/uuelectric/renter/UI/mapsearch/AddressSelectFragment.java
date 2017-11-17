package com.youyou.uuelectric.renter.UI.mapsearch;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.VolleyError;
import com.google.protobuf.InvalidProtocolBufferException;
import com.uu.facade.base.cmd.Cmd;
import com.uu.facade.dot.protobuf.iface.DotInterface;
import com.youyou.uuelectric.renter.Network.HttpResponse;
import com.youyou.uuelectric.renter.Network.NetworkTask;
import com.youyou.uuelectric.renter.Network.NetworkUtils;
import com.youyou.uuelectric.renter.Network.UUResponseData;
import com.youyou.uuelectric.renter.Network.user.UserConfig;
import com.youyou.uuelectric.renter.R;
import com.youyou.uuelectric.renter.UI.base.BaseActivity;
import com.youyou.uuelectric.renter.UI.base.BaseFragment;
import com.youyou.uuelectric.renter.UI.nearstation.NearDotMode;
import com.youyou.uuelectric.renter.Utils.Support.Config;
import com.youyou.uuelectric.renter.Utils.Support.IntentConfig;
import com.youyou.uuelectric.renter.Utils.Support.LocationListener;
import com.youyou.uuelectric.renter.Utils.eventbus.BaseEvent;
import com.youyou.uuelectric.renter.Utils.eventbus.EventBusConstant;
import com.youyou.uuelectric.renter.Utils.recycler.OnItemClickListener;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.greenrobot.event.EventBus;

/**
 * 检索网点 常用网点，附近网点
 */
public class AddressSelectFragment extends BaseFragment {


    @InjectView(R.id.near_station_list)
    RecyclerView recyclerView;
    private DotAdapter adapter;
    private String recordType;
    private ArrayList<DotInterface.DotInfo> dotInfoList = new ArrayList<DotInterface.DotInfo>();
    private NearDotMode mDotMode;
    //行程规划中传输到附近网点时的初始网点id
    private String dotId = "";

    public static AddressSelectFragment getInstance() {
        return new AddressSelectFragment();
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getArguments();
        recordType = bundle.getString(IntentConfig.DOT_AWLAYS_NEAR_RECORD_TYPE);
    }


    @Override
    public View setView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        recyclerView = new RecyclerView(this.mContext);
        View rootView = inflater.inflate(R.layout.activity_near_station, null);
        ButterKnife.inject(this, rootView);
//        recyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
        initView();

        return rootView;
    }

    private void initView() {

        /*
        recyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));*/
        if (IntentConfig.DOT_AWLAYS.equals(recordType)) {
            adapter = new DotAdapter(mContext, dotInfoList, 1);


            mDotMode = getArguments().getParcelable(IntentConfig.DOT_A2A);
            if (UserConfig.isPassLogined()) {

                requestFindCommonA2BDotNL();
            } else {

                if (mDotMode != null) {
                    //根据传入的网点数据创建一个dotinfo
                    DotInterface.DotInfo.Builder builder = DotInterface.DotInfo.newBuilder();
                    builder.setDotName(mDotMode.getDotName());
                    builder.setDotDesc(mDotMode.getDotDesc());
                    builder.setDotId(mDotMode.getDotId());
                    builder.setDotLat(mDotMode.getDotLat());
                    builder.setDotLon(mDotMode.getDotLon());
                    DotInterface.DotInfo aToAdotInfo = builder.build();
                    dotInfoList.add(aToAdotInfo);
                    adapter.notifyDataSetChanged();
                }
            }
        } else {
            adapter = new DotAdapter(mContext, dotInfoList);
            dotId = getArguments().getString(IntentConfig.DOT_ID);
        }


        //网点选择地adapter，常用网点，附近网点
        adapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                if (!Config.isNetworkConnected(mContext)) {
                    ((BaseActivity) mContext).showDefaultNetworkSnackBar();
                    return;
                }

                DotInterface.DotInfo dotInfo = dotInfoList.get(position);
                EventBus.getDefault().post(new BaseEvent(EventBusConstant.EVENT_TYPE_SELECTED_DOTINFO2, dotInfo));

                mContext.finish();
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        recyclerView.setAdapter(adapter);
    }

    /**
     * 常用网点-登录态
     */
    private void requestFindCommonA2BDotNL() {
        mProgressLayout.showLoading("努力拉取网点中...");
        if (Config.isNetworkConnected(mContext)) {
            DotInterface.FindCommonA2BDotNL.Request.Builder builder = DotInterface.FindCommonA2BDotNL.Request.newBuilder();
            builder.setCityId(Config.cityCode);
            //不为null 说明是从行程规划进入，否则是从行程中进入
            if (mDotMode != null) {
                builder.setParkingId(mDotMode.getDotId());
            } else {
                String orderId = Config.getOrderId(mContext);
                if (!TextUtils.isEmpty(orderId)) {
                    builder.setOrderId(orderId);
                }
            }

            NetworkTask task = new NetworkTask(Cmd.CmdCode.FindCommonA2BDotNL_VALUE);

            task.setBusiData(builder.build().toByteArray());
            NetworkUtils.executeNetwork(task, new HttpResponse.NetWorkResponse<UUResponseData>() {
                @Override
                public void onSuccessResponse(UUResponseData responseData) {
                    if (responseData.getRet() == 0) {
                        try {
                            showResponseCommonMsg(responseData.getResponseCommonMsg());
                            DotInterface.FindCommonA2BDotNL.Response response = DotInterface.FindCommonA2BDotNL.Response.parseFrom(responseData.getBusiData());

                            if (response.getRet() == 0) {
                                List<DotInterface.DotInfo> dotsList = response.getDotsList();
                                DotInterface.DotInfo currentDot = response.getCurrentDot();

                                mProgressLayout.showContent();
                                if (dotsList != null && dotsList.size() > 0) {
                                    dotInfoList.clear();
                                    dotInfoList.add(currentDot);
                                    dotInfoList.addAll(dotsList);
                                    adapter.notifyDataSetChanged();

                                } else {
                                    dotInfoList.add(currentDot);
                                    adapter.notifyDataSetChanged();
                                }
                            } else {
                                mProgressLayout.showErrorSmall(onClickListener, "咦，与总部联系不上了...", "点击重试");
                            } /*else {
                                if (responseData.getResponseCommonMsg() != null && !TextUtils.isEmpty(responseData.getResponseCommonMsg().getMsg())) {
                                    showResponseCommonMsg(responseData.getResponseCommonMsg());
                                } else {
                                    Config.showFiledToast(mContext);
                                }
                            }*/
                        } catch (InvalidProtocolBufferException e) {
                            e.printStackTrace();
                            mProgressLayout.showErrorSmall(onClickListener, "咦，与总部联系不上了...", "点击重试");
                        }

                    } else {
                        mProgressLayout.showErrorSmall(onClickListener, "咦，与总部联系不上了...", "点击重试");
                    }
                }

                @Override
                public void onError(VolleyError errorResponse) {
                    mProgressLayout.showErrorSmall(onClickListener, "咦，与总部联系不上了...", "点击重试");
                }

                @Override
                public void networkFinish() {
                }
            });

        } else {
            mProgressLayout.showErrorSmall(onClickListener, "网络不给力哦，请刷新重试", "点击重试");
        }
    }


    public void locationAndGetNearA2BDot() {
        mProgressLayout.showLoading("努力拉取网点中...");
        Config.getCoordinates(mContext, new LocationListener() {
            @Override
            public void locationSuccess(double lat, double lng, String addr) {
//                showToast(addr + lat + "," + lng);
                requestFindNearA2BDot(lat, lng);
            }
        });
    }

    /**
     * 附近网点(非登录态)
     *
     * @param lat
     * @param lng
     */
    private void requestFindNearA2BDot(double lat, double lng) {
        if (Config.isNetworkConnected(mContext)) {
            DotInterface.FindNearA2BDot.Request.Builder builder = DotInterface.FindNearA2BDot.Request.newBuilder();
            builder.setCityId(Config.cityCode);
            builder.setCurrentPositionLat(lat);//经纬度调整
            builder.setCurrentPositionLon(lng);

            String orderId = Config.getOrderId(mContext);
            if (!TextUtils.isEmpty(orderId)) {
                builder.setOrderId(orderId);
            } else {
                if (!TextUtils.isEmpty(dotId)) {
                    builder.setParking(dotId);
                }
            }

            NetworkTask task = new NetworkTask(Cmd.CmdCode.FindNearA2BDot_VALUE);
            task.setBusiData(builder.build().toByteArray());
            NetworkUtils.executeNetwork(task, new HttpResponse.NetWorkResponse<UUResponseData>() {
                @Override
                public void onSuccessResponse(UUResponseData responseData) {
                    if (responseData.getRet() == 0) {
                        try {
                            showResponseCommonMsg(responseData.getResponseCommonMsg());
                            DotInterface.FindNearA2BDot.Response response = DotInterface.FindNearA2BDot.Response.parseFrom(responseData.getBusiData());

                            if (response.getRet() == 0) {
                                List<DotInterface.DotInfo> dotsList = response.getDotsList();
                                if (dotsList != null && dotsList.size() > 0) {
                                    mProgressLayout.showContent();
                                    dotInfoList.clear();
                                    dotInfoList.addAll(dotsList);
                                    adapter.notifyDataSetChanged();

                                } else {
                                    mProgressLayout.setEmptyView(getResources().getString(R.string.near_no_station_tip));
                                }
                            } else {
                                mProgressLayout.showErrorSmall(onClickListener, "咦，与总部联系不上了...", "点击重试");
                            }
                        } catch (InvalidProtocolBufferException e) {
                            e.printStackTrace();
                            mProgressLayout.showErrorSmall(onClickListener, "咦，与总部联系不上了...", "点击重试");
                        }

                    } else {
                        mProgressLayout.showErrorSmall(onClickListener, "咦，与总部联系不上了...", "点击重试");
                    }
                }

                @Override
                public void onError(VolleyError errorResponse) {
                    mProgressLayout.showErrorSmall(onClickListener, "咦，与总部联系不上了...", "点击重试");
                }

                @Override
                public void networkFinish() {
                }
            });

        } else {
            mProgressLayout.showErrorSmall(onClickListener, "网络不给力哦，请刷新重试", "点击重试");
        }

    }

    /**
     * 加载错误时点击重试处理逻辑
     */
    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            if (IntentConfig.DOT_AWLAYS.equals(recordType)) {
                requestFindCommonA2BDotNL();
            } else {
                locationAndGetNearA2BDot();
            }
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }
}
