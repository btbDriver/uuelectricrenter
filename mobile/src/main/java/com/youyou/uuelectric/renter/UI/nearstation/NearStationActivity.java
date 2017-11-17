package com.youyou.uuelectric.renter.UI.nearstation;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.google.protobuf.InvalidProtocolBufferException;
import com.umeng.analytics.MobclickAgent;
import com.uu.facade.base.cmd.Cmd;
import com.uu.facade.dot.protobuf.iface.DotInterface;
import com.youyou.uuelectric.renter.Network.HttpResponse;
import com.youyou.uuelectric.renter.Network.NetworkTask;
import com.youyou.uuelectric.renter.Network.NetworkUtils;
import com.youyou.uuelectric.renter.Network.UUResponseData;
import com.youyou.uuelectric.renter.R;
import com.youyou.uuelectric.renter.UI.base.BaseActivity;
import com.youyou.uuelectric.renter.UI.main.MainActivity;
import com.youyou.uuelectric.renter.Utils.Support.Config;
import com.youyou.uuelectric.renter.Utils.Support.IntentConfig;
import com.youyou.uuelectric.renter.Utils.Support.L;
import com.youyou.uuelectric.renter.Utils.UMCountConstant;
import com.youyou.uuelectric.renter.Utils.eventbus.BaseEvent;
import com.youyou.uuelectric.renter.Utils.eventbus.EventBusConstant;
import com.youyou.uuelectric.renter.Utils.recycler.OnItemClickListener;
import com.youyou.uuelectric.renter.Utils.view.RippleView;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.greenrobot.event.EventBus;
import me.grantland.widget.AutofitTextView;

/**
 * 附近网点展示列表Activity
 */
public class NearStationActivity extends BaseActivity {

    @InjectView(R.id.near_station_list)
    RecyclerView mNearStationList;

    private List<DotInterface.DotInfo> dotInfoList = new ArrayList<>();

    private DotAdapter adapter;

    private String noDataTip = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_near_station);
        ButterKnife.inject(this);

        noDataTip = getResources().getString(R.string.near_no_station_tip);

        initView();

        initArgs();

    }

    private void initArgs() {
        Intent intent = getIntent();
        // 根据该字段判断是否需要访问服务器获取数据
        boolean needLoadData = intent.getBooleanExtra(IntentConfig.NEAR_STATION_NEED_GET_DATA, false);
        if (needLoadData) {
            L.i("需要从服务器拉取最新数据");
            mProgressLayout.showLoading("努力拉取网点中...");
            loadData();
        } else {
            L.i("接收传递过来的网点数据");
            NearDotListMode mode = intent.getParcelableExtra(IntentConfig.KEY_NEAR_DOT_LIST);
            List<NearDotMode> nearDotModes = mode.getNearDotModes();
            List<DotInterface.DotInfo> dotInfos = transformNearDot2DotInfo(nearDotModes);
            if (dotInfos != null && dotInfos.size() > 0) {
                dotInfoList.addAll(dotInfos);
                adapter.notifyDataSetChanged();
            } else {
                mProgressLayout.setEmptyView(noDataTip);
            }
        }
    }

    /**
     * 将服务端返回的网点Dot转换成本地的附近网点Mode
     *
     * @param dotInfos
     */
    public static List<NearDotMode> transformDotInfo2NearDot(List<DotInterface.DotInfo> dotInfos) {
        List<NearDotMode> nearDotModes = new ArrayList<>();
        for (DotInterface.DotInfo dotInfo : dotInfos) {

            NearDotMode mode = new NearDotMode();
            mode.setCarTotal(dotInfo.getCarTotal());
            mode.setDistance(dotInfo.getDistance());
            mode.setDotDesc(dotInfo.getDotDesc());
            mode.setDotId(dotInfo.getDotId());
            mode.setDotLat(dotInfo.getDotLat());
            mode.setDotLon(dotInfo.getDotLon());
            mode.setDotName(dotInfo.getDotName());

            nearDotModes.add(mode);
        }

        return nearDotModes;
    }

    /**
     * 将本地的附近网点Mode转换成服务器端返回的DotInfo
     *
     * @param nearDotModes
     * @return
     */
    public static List<DotInterface.DotInfo> transformNearDot2DotInfo(List<NearDotMode> nearDotModes) {
        List<DotInterface.DotInfo> dotInfos = new ArrayList<>();
        for (NearDotMode mode : nearDotModes) {
            DotInterface.DotInfo.Builder builder = DotInterface.DotInfo.newBuilder();
            builder.setDotLon(mode.getDotLon());
            builder.setDotLat(mode.getDotLat());
            builder.setDotName(mode.getDotName());
            builder.setDotDesc(mode.getDotDesc());
            builder.setDistance(mode.getDistance());
            builder.setCarTotal(mode.getCarTotal());
            builder.setDotId(mode.getDotId());

            dotInfos.add(builder.build());
        }

        return dotInfos;
    }

    /**
     * 请求服务器，加载附近网点数据
     */
    private void loadData() {
        if (TextUtils.isEmpty(Config.cityCode)) {
            L.i("cityId为null，不能发送网络请求..");
            return;
        }
        if (Config.isNetworkConnected(mContext)) {
            DotInterface.MapSearchDotListRequest.Builder builder = DotInterface.MapSearchDotListRequest.newBuilder();
            builder.setCityId(Config.cityCode);
            builder.setCurrentPositionLat(Config.lat);
            builder.setCurrentPositionLon(Config.lng);

            NetworkTask task = new NetworkTask(Cmd.CmdCode.MapSearchDotList_VALUE);
            task.setBusiData(builder.build().toByteArray());
            NetworkUtils.executeNetwork(task, new HttpResponse.NetWorkResponse<UUResponseData>() {
                @Override
                public void onSuccessResponse(UUResponseData responseData) {
                    if (responseData.getRet() == 0) {
                        try {
                            showResponseCommonMsg(responseData.getResponseCommonMsg());
                            DotInterface.MapSearchDotListResponse response = DotInterface.MapSearchDotListResponse.parseFrom(responseData.getBusiData());

                            if (response.getRet() == 0) {
                                List<DotInterface.DotInfo> nearDotsList = response.getNearDotsList();
                                if (nearDotsList != null && nearDotsList.size() > 0) {
                                    dotInfoList.clear();
                                    dotInfoList.addAll(nearDotsList);
                                    adapter.notifyDataSetChanged();
                                    mProgressLayout.showContent();
                                } else {
                                    mProgressLayout.setEmptyView(noDataTip);
                                }


                            }
                        } catch (InvalidProtocolBufferException e) {
                            e.printStackTrace();
                            mProgressLayout.showError(errorClickListener);
                        }

                    } else {
                        mProgressLayout.showError(errorClickListener);
                    }
                }

                @Override
                public void onError(VolleyError errorResponse) {
                    mProgressLayout.showError(errorClickListener);
                }

                @Override
                public void networkFinish() {
                }
            });

        } else {
            mProgressLayout.showError(errorClickListener);
        }
    }

    //-------------------------------各种事件监听逻辑--------------------------------------

    /**
     * ToolBar右侧操作按钮点击事件
     */
    View.OnClickListener rightOptBtnClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            onBackPressed();
        }
    };

    /**
     * 加载失败时点击重新加载的逻辑处理
     */
    View.OnClickListener errorClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            mProgressLayout.showLoading();
            loadData();
        }
    };

    /**
     * RecyclerView的Item点击处理
     */
    OnItemClickListener itemClickListener = new OnItemClickListener() {
        @Override
        public void onItemClick(int position) {
            if (!Config.isNetworkConnected(mContext)) {
                showDefaultNetworkSnackBar();
                return;
            }

            DotInterface.DotInfo dotInfo = dotInfoList.get(position);
            EventBus.getDefault().post(new BaseEvent(EventBusConstant.EVENT_TYPE_SELECTED_DOTINFO, dotInfo));

            finish();
            //对附近网点列表点击事件统计
            MobclickAgent.onEvent(mContext, UMCountConstant.POINT_LIST_CLICK);
        }
    };

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        // 回地图页
        Intent intent = new Intent(mContext, MainActivity.class);
        intent.putExtra("goto", MainActivity.GOTO_MAP);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);

        // 发送刷新地图网点消息
        EventBus.getDefault().post(new BaseEvent(EventBusConstant.EVENT_TYPE_REFRESH_DOT_FORM_DOT_LIST));

        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == 0 || item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 初始化组件
     */
    public void initView() {
        // 设置ToolBar右侧操作按钮点击事件
//        setRightOptBtnInfo(getResources().getString(R.string.text_map), rightOptBtnClick);

        adapter = new DotAdapter();
        adapter.setOnItemClickListener(itemClickListener);
        mNearStationList.setLayoutManager(new LinearLayoutManager(this));
        mNearStationList.setAdapter(adapter);


    }


    class DotViewHolder extends RecyclerView.ViewHolder {
        @InjectView(R.id.rv_root)
        RippleView mRvRoot;
        @InjectView(R.id.tv_title)
        AutofitTextView mTvTitle;
        @InjectView(R.id.tv_distance)
        TextView mTvDistance;
        @InjectView(R.id.tv_sub_title)
        TextView mTvSubTitle;
        @InjectView(R.id.tv_number)
        TextView mTvNumber;

        public DotViewHolder(View itemView) {
            super(itemView);
            ButterKnife.inject(this, itemView);
        }
    }

    class DotAdapter extends RecyclerView.Adapter<DotViewHolder> {

        OnItemClickListener onItemClickListener;

        public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
            this.onItemClickListener = onItemClickListener;
        }

        @Override
        public DotViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.near_station_item, parent, false);
            return new DotViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(DotViewHolder holder, final int position) {

            // 网络请求未通，暂时注释
            DotInterface.DotInfo dotInfo = dotInfoList.get(position);

            if(dotInfo.getIsA2B() == 1){
                Drawable drawable= ContextCompat.getDrawable(getBaseContext(), R.mipmap.ic_location_return);
                drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
                holder.mTvTitle.setCompoundDrawablesWithIntrinsicBounds(null,null,drawable,null);//设置TextView的drawableright
                holder.mTvTitle.setCompoundDrawablePadding(getResources().getDimensionPixelSize(R.dimen.s1));
            }else if(dotInfo.getIsA2B() == 2){
                holder.mTvTitle.setCompoundDrawablesWithIntrinsicBounds(null,null,null,null);
            }
            holder.mTvTitle.setText(dotInfo.getDotName());

            holder.mTvSubTitle.setText(dotInfo.getDotDesc());
            float distance = dotInfo.getDistance() / 1000f;
            String result;
            if (distance >= 1) {
                result = String.format("%.2f", distance) + "km";
            } else {
                result = dotInfo.getDistance() + "m";
            }
            holder.mTvDistance.setText(result);
            holder.mTvNumber.setText(dotInfo.getCarTotal() + "");

            holder.mRvRoot.setOnRippleCompleteListener(new RippleView.OnRippleCompleteListener() {
                @Override
                public void onComplete(RippleView rippleView) {
                    if (onItemClickListener != null) {
                        onItemClickListener.onItemClick(position);
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return dotInfoList.size();
        }
    }


}
