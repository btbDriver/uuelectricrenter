package com.youyou.uuelectric.renter.UI.mapsearch;

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
import com.youyou.uuelectric.renter.R;
import com.youyou.uuelectric.renter.UI.base.BaseActivity;
import com.youyou.uuelectric.renter.UI.base.BaseFragment;
import com.youyou.uuelectric.renter.UI.main.rentcar.LocalPointItem;
import com.youyou.uuelectric.renter.Utils.Support.Config;
import com.youyou.uuelectric.renter.Utils.Support.L;
import com.youyou.uuelectric.renter.Utils.eventbus.BaseEvent;
import com.youyou.uuelectric.renter.Utils.eventbus.EventBusConstant;
import com.youyou.uuelectric.renter.Utils.recycler.OnItemClickListener;
import com.youyou.uuelectric.renter.Utils.task.ActivityUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.greenrobot.event.EventBus;

/**
 * location 附近的网点 在map上加marker
 */
public class MapAdressNearDotFragment extends BaseFragment {


    @InjectView(R.id.near_station_list)
    RecyclerView recyclerView;


    private ArrayList<DotInterface.DotInfo> dotInfoList = new ArrayList<DotInterface.DotInfo>();
    private DotAdapter adapter;
    private View rootView;
    private String dotId;

    public static MapAdressNearDotFragment getInstance() {
        return new MapAdressNearDotFragment();
    }

    private String noDataTip = "";
    private LocalPointItem localPointItem;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        noDataTip = getResources().getString(R.string.near_no_station_tip);
        Bundle bundle = getArguments();
        localPointItem = ((MapSearchActivity) getActivity()).localPointItem;
        dotId = ((MapSearchActivity) getActivity()).dotId;

    }

    @Override
    public View setView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.activity_near_station, null);
        ButterKnife.inject(this, rootView);
        initView();


        return rootView;
    }

    private void initView() {

        adapter = new DotAdapter(mContext, dotInfoList, true);
        adapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                if (!Config.isNetworkConnected(mContext)) {
                    ((BaseActivity) mContext).showDefaultNetworkSnackBar();
                    return;
                }

                DotInterface.DotInfo dotInfo = dotInfoList.get(position);
                EventBus.getDefault().post(new BaseEvent(EventBusConstant.EVENT_TYPE_SELECTED_DOTINFO2, dotInfo));

                ActivityUtil.closeNumberActivities(2);
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        recyclerView.setAdapter(adapter);


        requestMarkerByLocation();
    }

    /**
     * 请求服务器获取位置附近的网点信息
     *
     * @param
     */
    private void requestMarkerByLocation() {

        mProgressLayout.showLoading("努力拉取附近网点中...");

        if (TextUtils.isEmpty(Config.cityCode)) {
            L.i("cityId为null，不能发送网络请求..");
            return;
        }

        if (Config.isNetworkConnected(mContext)) {

            DotInterface.FindNearA2BDot.Request.Builder builder = DotInterface.FindNearA2BDot.Request.newBuilder();
            builder.setCityId(Config.cityCode);
            builder.setCurrentPositionLat(localPointItem.getLat());//经纬度调整
            builder.setCurrentPositionLon(localPointItem.getLng());

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
                                List<DotInterface.DotInfo> nearDotsList = response.getDotsList();
                                if (nearDotsList != null && nearDotsList.size() > 0) {
                                    dotInfoList.clear();
                                    dotInfoList.addAll(nearDotsList);
                                    adapter.notifyDataSetChanged();
                                    mProgressLayout.showContent();
                                    if (getActivity() != null) {
                                        ((MapSearchActivity) getActivity()).addMarkersToMap(dotInfoList);
                                    }
                                } else {
                                    if (getActivity() != null) {
                                        ((MapSearchActivity) getActivity()).gotoLocationAnim();
                                    }
                                    mProgressLayout.setEmptyView(noDataTip);
                                }
                            } else {
                                mProgressLayout.setEmptyView(noDataTip);
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
            mProgressLayout.showLoading("努力拉取附近网点中...");
            requestMarkerByLocation();
        }
    };

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }
}
