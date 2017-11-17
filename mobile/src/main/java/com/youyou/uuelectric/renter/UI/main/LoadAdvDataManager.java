package com.youyou.uuelectric.renter.UI.main;

import android.content.Context;
import android.text.TextUtils;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.google.protobuf.InvalidProtocolBufferException;
import com.uu.facade.advertisement.protobuf.bean.AdvCommon;
import com.uu.facade.advertisement.protobuf.iface.AdvInterface;
import com.uu.facade.base.cmd.Cmd;
import com.youyou.uuelectric.renter.Network.HttpResponse;
import com.youyou.uuelectric.renter.Network.NetworkTask;
import com.youyou.uuelectric.renter.Network.NetworkUtils;
import com.youyou.uuelectric.renter.Network.UUResponseData;
import com.youyou.uuelectric.renter.UUApp;
import com.youyou.uuelectric.renter.Utils.Support.Config;
import com.youyou.uuelectric.renter.Utils.Support.L;
import com.youyou.uuelectric.renter.Utils.Support.LocationListener;
import com.youyou.uuelectric.renter.Utils.eventbus.BaseEvent;
import com.youyou.uuelectric.renter.Utils.eventbus.EventBusConstant;

import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * 拉取最新的广告数据管理类
 */
public class LoadAdvDataManager {

    private Context mContext;
    private int loadCount = 0;
    private List<AdvCommon.AdvInfo> advInfoListList;

    public LoadAdvDataManager(Context context) {
        this.mContext = context;
    }


    /**
     * 获取服务端需要下发的广告数据
     */
    public void queryAdv() {
        if (Config.isNetworkConnected(mContext)) {
            if (Config.cityCode == null || Config.cityCode.equals("")) {
                Config.getCoordinates(mContext, new LocationListener() {
                    @Override
                    public void locationSuccess(double lat, double lng, String addr) {
                        startQueryAdv();
                    }
                });
            } else {
                startQueryAdv();
            }
        }
    }

    private void startQueryAdv() {
        if (TextUtils.isEmpty(Config.cityCode)) {
            L.i("cityCode为null,不能进行广告数据拉取操作");
            return;
        }
        AdvInterface.QueryAdv.Request.Builder builder = AdvInterface.QueryAdv.Request.newBuilder();
        builder.setCityCode(Config.cityCode);
        NetworkTask task = new NetworkTask(Cmd.CmdCode.QueryAdvertisement_VALUE);
        task.setBusiData(builder.build().toByteArray());
        NetworkUtils.executeNetwork(task, new HttpResponse.NetWorkResponse<UUResponseData>() {
            @Override
            public void onSuccessResponse(UUResponseData responseData) {
                if (responseData.getRet() == 0) {
                    try {
                        AdvInterface.QueryAdv.Response response = AdvInterface.QueryAdv.Response.parseFrom(responseData.getBusiData());
                        if (response.getRet() == 0) {
                            advInfoListList = response.getAdvInfoListList();

                            if (advInfoListList != null && advInfoListList.size() > 0) {
                                L.i("拉取到最新广告数据，需要展示，广告数量为:" + advInfoListList.size());
                                for (final AdvCommon.AdvInfo advInfo : advInfoListList) {
                                    UUApp.getImageLoaderInstance().get(advInfo.getActivityImg(), new ImageLoader.ImageListener() {
                                        @Override
                                        public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                                            if (response.getBitmap() != null) {
                                                L.i("广告" + advInfo.getAdvId() + "的图片已加载成功...");
                                                loadCount++;
                                                if (loadCount >= advInfoListList.size()) {
                                                    L.i("广告图片已全部加载，发送展示广告事件");
                                                    EventBus.getDefault().post(new BaseEvent(EventBusConstant.EVENT_TYPE_QUERY_ADV, advInfoListList));
                                                }
                                            }
                                        }

                                        @Override
                                        public void onErrorResponse(VolleyError error) {

                                            L.i("广告" + advInfo.getAdvId() + "的图片已加载失败...");

                                        }
                                    });
                                }
                            } else {
                                EventBus.getDefault().post(new BaseEvent(EventBusConstant.EVENT_TYPE_QUERY_ADV, null));
                                L.i("拉取到最新广告数据为null");
                            }
                        }
                    } catch (InvalidProtocolBufferException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onError(VolleyError errorResponse) {
            }

            @Override
            public void networkFinish() {

            }
        });
    }

}
