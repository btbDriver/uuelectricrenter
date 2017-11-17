package com.youyou.uuelectric.renter.UI.main;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.flyco.pageindicator.indicator.FlycoPageIndicaor;
import com.google.protobuf.InvalidProtocolBufferException;
import com.umeng.analytics.MobclickAgent;
import com.uu.facade.advertisement.protobuf.bean.AdvCommon;
import com.uu.facade.advertisement.protobuf.iface.AdvInterface;
import com.uu.facade.base.cmd.Cmd;
import com.youyou.uuelectric.renter.Network.HttpResponse;
import com.youyou.uuelectric.renter.Network.LruImageCache;
import com.youyou.uuelectric.renter.Network.NetworkTask;
import com.youyou.uuelectric.renter.Network.NetworkUtils;
import com.youyou.uuelectric.renter.Network.UUResponseData;
import com.youyou.uuelectric.renter.Network.user.UserConfig;
import com.youyou.uuelectric.renter.R;
import com.youyou.uuelectric.renter.UI.web.H5Activity;
import com.youyou.uuelectric.renter.UI.web.H5Constant;
import com.youyou.uuelectric.renter.UUApp;
import com.youyou.uuelectric.renter.Utils.AnimDialogUtils;
import com.youyou.uuelectric.renter.Utils.DisplayUtil;
import com.youyou.uuelectric.renter.Utils.Support.L;
import com.youyou.uuelectric.renter.Utils.UMCountConstant;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Administrator on 2015/10/20 0020.
 * 首页广告管理类
 */
public class MainMapAdManager {

    private Activity context;

    private DisplayMetrics displayMetrics = new DisplayMetrics();
    private View contentView;
    private ViewPager viewPager;
    private RelativeLayout adRootContent;
    private int padding = 32;
    private AdAdapter adAdapter;
    private FlycoPageIndicaor mIndicator;
    private AnimDialogUtils animDialogUtils;

    List<AdvCommon.AdvInfo> advInfoListList;

    /**
     * 记录曝光的广告集合，用于过滤，避免重复上报
     */
    List<String> reportAdList = new ArrayList<>();

    private boolean isShowing = false;

    private static final String SP_NAME = "adRecordSp";

    public MainMapAdManager(Activity context, List<AdvCommon.AdvInfo> advInfoListList) {
        this.context = context;
        this.advInfoListList = advInfoListList;

    }

    private View.OnClickListener imageOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            AdvCommon.AdvInfo advInfo = (AdvCommon.AdvInfo) view.getTag();
            if (advInfo != null) {
                popWindowStat(advInfo.getAdvId(), "2");

                Intent intent = new Intent(context, H5Activity.class);
                intent.putExtra(H5Constant.TITLE, advInfo.getAdvTitle());
                intent.putExtra(H5Constant.MURL, advInfo.getUrl());
                context.startActivity(intent);
            }
        }
    };

    private ViewPager.OnPageChangeListener onPageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {

            popWindowStat(advInfoListList.get(position).getAdvId(), "1");

        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };

    View.OnClickListener closeClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            isShowing = false;
            recyclerImageView();
        }
    };

    public boolean isShowing() {
        return isShowing;
    }

    public void showAdDialog() {

        L.i("开始展现广告...");

        isShowing = true;

        contentView = LayoutInflater.from(context).inflate(R.layout.ad_dialog_content_layout, null);
        adRootContent = (RelativeLayout) contentView.findViewById(R.id.ad_root_content);

        viewPager = (ViewPager) contentView.findViewById(R.id.viewPager);
        mIndicator = (FlycoPageIndicaor) contentView.findViewById(R.id.indicator);

        adAdapter = new AdAdapter();
        viewPager.setAdapter(adAdapter);
        viewPager.addOnPageChangeListener(onPageChangeListener);
        mIndicator.setViewPager(viewPager);
        isShowIndicator();

        animDialogUtils = AnimDialogUtils.getInstance(context).initView(contentView, closeClickListener);
        setRootContainerHeight();

        // 延迟1s展示，为了避免ImageLoader还为加载完缓存图片时就展示了弹窗的情况
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                animDialogUtils.show();

                // 保存广告记录，确保一天只弹一次
                saveShowRecord(context);

                // 默认曝光第一个广告
                popWindowStat(advInfoListList.get(0).getAdvId(), "1");
            }
        }, 1000);
    }

    public void dismissAdDialog() {
        animDialogUtils.dismiss();
    }

    private static final String DATEKEY = "ad_date";
    /**
     * 保存广告已经展现的记录，根据当前时间，确保无论什么状态，一天只会展示一次广告弹窗
     */
    private void saveShowRecord(Context context) {
        SharedPreferences sp = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String date = sdf.format(new Date());
        sp.edit().putString(DATEKEY, date).commit();
    }

    /**
     * 判断是否已经展现了广告
     *
     * @return true：已经展现    false：未展现
     */
    public static boolean isSaveRecord(Context context) {
        SharedPreferences sp = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String date = sdf.format(new Date());
        String saveDate = sp.getString(DATEKEY, "");
        if (date.equals(saveDate)) {
            return true;
        }
        return false;
    }

    /**
     * 直接删除地图页的广告弹窗，此方法用在账户异常切换后，另一个账户首页加载的界面不是地图页，而切换之前的用户在地图页，且已经弹出了广告弹窗且未关闭
     * 这种场景，使用该方法删除地图页广告弹窗
     *
     * @param context
     */
    public static void removeAdDialog(Activity context) {
        if (context != null && context instanceof Activity) {
            ViewGroup androidContentView = (ViewGroup) context.getWindow().findViewById(Window.ID_ANDROID_CONTENT);
            View rootView = androidContentView.findViewWithTag(AnimDialogUtils.ANIM_DIALOG_TAG);
            if (rootView != null) {
                androidContentView.removeView(rootView);
            }
        }
    }

    private void setRootContainerHeight() {

        context.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int widthPixels = displayMetrics.widthPixels;
        int totalPadding = DisplayUtil.dip2px(context, padding * 2);
        int width = widthPixels - totalPadding;
        final int height = (int) (width / 3.0f * 4.0f);
        ViewGroup.LayoutParams params = adRootContent.getLayoutParams();
        params.height = height;
    }

    /**
     * 根据页面数量，判断是否显示Indicator
     */
    private void isShowIndicator() {
        if (advInfoListList.size() > 1) {
            mIndicator.setVisibility(View.VISIBLE);
        } else {
            mIndicator.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * 广告的曝光、点击统计
     *
     * @param advId
     * @param eventType 1：曝光类型  2：点击类型
     */
    private void popWindowStat(String advId, String eventType) {
        // 未登录时，不进行上报统计
        if (!UserConfig.isPassLogined()) {
            return;
        }
        if (TextUtils.isEmpty(advId)) {
            L.i("广告统计--->advId为null");
            return;
        }
        if (TextUtils.isEmpty(eventType)) {
            L.i("广告统计--->eventType为null");
            return;
        }
        String type = "";
        if (eventType.equals("1")) {
            if (reportAdList.contains(advId)) {
                return;
            }
            reportAdList.add(advId);
            MobclickAgent.onEvent(context, UMCountConstant.AD_VIEW, advId);
            type = "曝光";
        } else if (eventType.equals("2")) {
            MobclickAgent.onEvent(context, UMCountConstant.AD_CLICK, advId);
            type = "点击";
        }
        L.i("开始执行曝光操作，广告ID:" + advId + "\t 类型:" + type);
        AdvInterface.PopWindowStat.Request.Builder builder = AdvInterface.PopWindowStat.Request.newBuilder();
        builder.setAdvId(advId);
        builder.setEventType(eventType);
        NetworkTask task = new NetworkTask(Cmd.CmdCode.PopWindowStat_VALUE);
        task.setBusiData(builder.build().toByteArray());
        NetworkUtils.executeNetwork(task, new HttpResponse.NetWorkResponse<UUResponseData>() {

            @Override
            public void onSuccessResponse(UUResponseData responseData) {

                if (responseData.getRet() == 0) {
                    try {
                        AdvInterface.PopWindowStat.Response response = AdvInterface.PopWindowStat.Response.parseFrom(responseData.getBusiData());
                        if (response.getRet() == 0) {

                            L.i("广告统计上报成功...");
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

    class AdAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return advInfoListList.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            AdvCommon.AdvInfo advInfo = advInfoListList.get(position);
            /*NetworkImageView imageView = new NetworkImageView(context);
            UUApp.getInstance().display(advInfo.getActivityImg(), imageView);
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            container.addView(imageView, params);
            imageView.setTag(advInfo);
            imageView.setOnClickListener(imageOnClickListener);*/

            final ImageView imageView = new ImageView(context);
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            container.addView(imageView, params);
            imageView.setTag(advInfo);
            imageView.setOnClickListener(imageOnClickListener);

            UUApp.getInstance().getImageLoaderInstance().get(advInfo.getActivityImg(), new ImageLoader.ImageListener() {
                @Override
                public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                    if (response.getBitmap() != null) {
                        imageView.setImageBitmap(response.getBitmap());
                    }
                }

                @Override
                public void onErrorResponse(VolleyError error) {

                }
            });

            return imageView;
        }
    }

    /**
     * 回收LruCache中缓存的图片
     */
    public void recyclerImageView() {
        for (AdvCommon.AdvInfo advInfo : advInfoListList) {
            LruImageCache.getInstance().removeBitmap(LruImageCache.getCacheKey(advInfo.getActivityImg()));
        }
    }


}
