package com.youyou.uuelectric.renter.Utils.map;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.amap.api.navi.model.NaviLatLng;
import com.uu.facade.order.pb.common.OrderCommon;
import com.youyou.uuelectric.renter.R;
import com.youyou.uuelectric.renter.UI.main.StartNaviActivity;
import com.youyou.uuelectric.renter.Utils.DialogUtil;
import com.youyou.uuelectric.renter.Utils.Support.Config;
import com.youyou.uuelectric.renter.Utils.Support.L;
import com.youyou.uuelectric.renter.Utils.Support.LocationListener;
import com.youyou.uuelectric.renter.Utils.view.RippleView;

import java.util.List;

/**
 * Created by liuchao on 2015/12/30.
 * 导航工具类，用于打开百度地图导航或者是高德地图
 */
public class NavMapUtil {

    public static Dialog dialog = null;

    /**
     * 百度地图和高德地图安装包包名
     */
    public static final String BAIDU_PACKAGE = "com.baidu.BaiduMap";
    public static final String GAODE_PACKAGE = "com.autonavi.minimap";
    /**
     * 导航类型:1:百度导航，2：高德导航
     */
    public static final int NAV_TYPE_BAIDU = 1;
    public static final int NAV_TYPE_GAODE = 2;
    /**
     * 导航方式：0：步行导航， 1：驾车导航
     */
    public static final int TYPE_WALK = 0;
    public static final int TYPE_DRIVE = 1;
    public static final int TYPE_BUS = 2;

    /**
     * 显示选择导航弹出框
     * @param mContext
     * @param title 弹窗标题
     * @param isBaiduOk 是否安装百度地图应用
     * @param isGaodeOk 是否安装高德地图应用
     * @param navType 导航类型： 步行导航：TYPE_WALK， 驾车导航：TYPE_DRIVE， 公交导航： TYPE_BUS
     */
    public static void showNavSelectDialog(final Activity mContext, OrderCommon.ParkingInfo parkingInfo, String title, boolean isBaiduOk, boolean isGaodeOk, int navType) {
        if (mContext == null) {
            return;
        }
        if (!isBaiduOk && !isGaodeOk) {
            return;
        }

        if (TextUtils.isEmpty(title)) {
            title = "选择导航";
        }

        String lat = parkingInfo.getLatlon().getLat();
        String lon = parkingInfo.getLatlon().getLon();
        NaviLatLng endLatLng = new NaviLatLng(Double.parseDouble(lat), Double.parseDouble(lon));
        String endAddress = parkingInfo.getParkingAddress();


        // 直接跳转百度导航
        if (isBaiduOk && !isGaodeOk) {
            buildIntentFromScheme(mContext, NAV_TYPE_BAIDU, endLatLng, endAddress, navType);
            return;
        }
        // 直接跳转高德导航
        if (!isBaiduOk && isGaodeOk) {
            buildIntentFromScheme(mContext, NAV_TYPE_GAODE, endLatLng, endAddress, navType);
            return;
        }

        View view = inflateView(mContext, title, isBaiduOk, isGaodeOk, endLatLng, endAddress, navType);
        dialog = DialogUtil.getInstance(mContext).showMaterialCustomDialog(view, false);
    }

    /**
     * 加载布局文件
     * @return
     */
    public static View inflateView(final Activity mContext, String title, boolean isBaiduOk, boolean isGaodeOk, NaviLatLng endLatLng, String endAddress, int navType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.nav_select, null);

        TextView textTitle = (TextView) view.findViewById(R.id.navselect_text_title);
        textTitle.setText(title);

        RippleView baiduRela = (RippleView) view.findViewById(R.id.navselect_rela_baidu);
        ImageView baiduIcon = (ImageView) view.findViewById(R.id.navselect_icon_baidu);
        TextView baiduName = (TextView) view.findViewById(R.id.navselect_name_baidu);

        RippleView gaodeRela = (RippleView) view.findViewById(R.id.navselect_rela_gaode);
        ImageView gaodeIcon = (ImageView) view.findViewById(R.id.navselect_icon_gaode);
        TextView gaodeName = (TextView) view.findViewById(R.id.navselect_name_gaode);

        // 数组中[0]为百度地图，[1]为高德地图
        PackageInfo[] mPackageInfo = getAppInfoByPackage(mContext);
        if (isBaiduOk) {
            baiduRela.setVisibility(View.VISIBLE);
            if (mPackageInfo != null && mPackageInfo[0] != null) {
                String appName = mPackageInfo[0].applicationInfo.loadLabel(mContext.getPackageManager()).toString();
                Drawable appIcon = mPackageInfo[0].applicationInfo.loadIcon(mContext.getPackageManager());
                if (!TextUtils.isEmpty(appName)) {
                    // 若存在特殊空格字符，则替换掉
                    baiduName.setText(appName.trim().replaceAll("\\s*", ""));
                }
                baiduIcon.setBackgroundDrawable(appIcon);
            }
        } else {
            baiduRela.setVisibility(View.GONE);
        }
        if (isGaodeOk) {
            gaodeRela.setVisibility(View.VISIBLE);
            if (mPackageInfo != null && mPackageInfo[1] != null) {
                String appName = mPackageInfo[1].applicationInfo.loadLabel(mContext.getPackageManager()).toString();
                Drawable appIcon = mPackageInfo[1].applicationInfo.loadIcon(mContext.getPackageManager());
                if (!TextUtils.isEmpty(appName)) {
                    // 若存在特殊空格字符，则替换掉
                    gaodeName.setText(appName.trim().replaceAll("\\s*", ""));
                }
                gaodeIcon.setBackgroundDrawable(appIcon);
            }
        } else {
            gaodeRela.setVisibility(View.GONE);
        }
        // 初始化按钮监听
        initRippleListener(mContext, baiduRela, gaodeRela, endLatLng, endAddress, navType);
        return view;
    }

    /**
     * 初始化按钮监听
     * @param mContext
     * @param baiduRela
     * @param gaodeRela
     */
    public static void initRippleListener(final Activity mContext, RippleView baiduRela, RippleView gaodeRela, final NaviLatLng endLatLng, final String endAddress, final int navType) {
        baiduRela.setOnRippleCompleteListener(new RippleView.OnRippleCompleteListener() {
            @Override
            public void onComplete(RippleView rippleView) {
                DialogUtil.getInstance(mContext).closeDialog();
                buildIntentFromScheme(mContext, NAV_TYPE_BAIDU, endLatLng, endAddress, navType);
            }
        });
        gaodeRela.setOnRippleCompleteListener(new RippleView.OnRippleCompleteListener() {
            @Override
            public void onComplete(RippleView rippleView) {
                DialogUtil.getInstance(mContext).closeDialog();
                buildIntentFromScheme(mContext, NAV_TYPE_GAODE, endLatLng, endAddress, navType);
            }
        });
    }


    /**
     * 解析百度地图和高德地图的sheme，返回Intent对象
     */
    public static void buildIntentFromScheme(final Activity mContext, final int navKind, final NaviLatLng endLatLng, final String endAddress, final int navType) {
        if (navKind == NAV_TYPE_BAIDU) {
            Config.getCoordinates(mContext, new LocationListener() {
                @Override
                public void locationSuccess(double lat, double lng, String addr) {
                    try {
                        StringBuffer uriBuffer = new StringBuffer();
                        uriBuffer.append(NavMapConstant.SCHEME_BAIDU_PRE)
                                .append(NavMapConstant.SCHEME_BAIDU_ORIGIN).append("latlng:").append(Config.lat).append(",").append(Config.lng).append("|name:").append("当前位置")
                                .append("&").append(NavMapConstant.SCHEME_BAIDU_DESTINATION).append("latlng:").append(endLatLng.getLatitude()).append(",").append(endLatLng.getLongitude()).append("|name:").append(endAddress)
                                .append("&").append(NavMapConstant.SCHEME_BAIDU_MODE).append(changeBaiMode(navType))
                                .append("&").append(NavMapConstant.SCHEME_BAIDU_REGION).append(Config.currentCity)
                                .append("&").append(NavMapConstant.SCHEME_BAIDU_TYPE).append("gcj02")
                                .append("&").append(NavMapConstant.SCHEME_BAIDU_SRC).append(getApplicationName(mContext))
                                .append(NavMapConstant.SCHEME_BAIDU_LAST);
                        L.i("调起百度地图导航SCHEME：" + uriBuffer.toString());
                        Intent intent = Intent.parseUri(uriBuffer.toString(), Intent.URI_INTENT_SCHEME);

                        int calculateMode = StartNaviActivity.CALCULATE_MODE_FOOT;
                        if (navType == TYPE_WALK) {
                            calculateMode = StartNaviActivity.CALCULATE_MODE_FOOT;
                        } else if (navType == TYPE_DRIVE) {
                            calculateMode = StartNaviActivity.TYPE_DRIVE;
                        }
                        // 数据统计
                        Config.reportLog(calculateMode, Config.getOrderId(mContext) + "\\t" + 0);
                        mContext.startActivity(intent);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } else if (navKind == NAV_TYPE_GAODE) {
            Config.getCoordinates(mContext, new LocationListener() {
                @Override
                public void locationSuccess(double lat, double lng, String addr) {
                    StringBuffer uriBuffer = new StringBuffer();
                    uriBuffer.append(NavMapConstant.SCHEME_GAODE_PRE)
                            .append(NavMapConstant.SCHEME_GAODE_SOURCEAPPLICATION).append(getApplicationName(mContext))
                            .append("&").append(NavMapConstant.SCHEME_GAODE_SLAT).append(Config.lat)
                            .append("&").append(NavMapConstant.SCHEME_GAODE_SLON).append(Config.lng)
                            .append("&").append(NavMapConstant.SCHEME_GAODE_SNAME).append(Config.currentAddress)
                            .append("&").append(NavMapConstant.SCHEME_GAODE_DLAT).append(endLatLng.getLatitude())
                            .append("&").append(NavMapConstant.SCHEME_GAODE_DLON).append(endLatLng.getLongitude())
                            .append("&").append(NavMapConstant.SCHEME_GAODE_DNAME).append(endAddress)
                            .append("&").append(NavMapConstant.SCHEME_GAODE_DEV).append("0")
                            .append("&").append(NavMapConstant.SCHEME_GAODE_M).append("3")
                            .append("&").append(NavMapConstant.SCHEME_GAODE_T).append(changeGaoMode(navType));
                    L.i("调起高德地图导航SCHEME：" + uriBuffer.toString());
                    Intent intent = new Intent();
                    intent.addCategory(Intent.CATEGORY_DEFAULT);
                    intent.setData(Uri.parse(uriBuffer.toString()));
                    intent.setPackage(GAODE_PACKAGE);

                    int calculateMode = StartNaviActivity.CALCULATE_MODE_FOOT;
                    if (navType == TYPE_WALK) {
                        calculateMode = StartNaviActivity.CALCULATE_MODE_FOOT;
                    } else if (navType == TYPE_DRIVE) {
                        calculateMode = StartNaviActivity.TYPE_DRIVE;
                    }
                    // 数据统计
                    Config.reportLog(calculateMode, Config.getOrderId(mContext) + "\\t" + 0);
                    mContext.startActivity(intent);
                }
            });

        }
    }


    /**
     * 获取高德地图的导航方式
     * @param navType
     * @return
     */
    public static int changeGaoMode(int navType) {
        int result = 4;
        if (navType == TYPE_WALK) {
            result = 4;
        } else if (navType == TYPE_DRIVE) {
            result = 2;
        } else if (navType == TYPE_BUS) {
            result = 1;
        }

        return result;
    }


    /**
     * 获取百度地图的导航方式
     * @param navType
     * @return
     */
    public static String changeBaiMode(int navType) {
        String result = "";
        if (navType == TYPE_WALK) {
            result = "walking";
        } else if (navType == TYPE_DRIVE) {
            result = "driving";
        } else if (navType == TYPE_BUS) {
            result = "transit";
        }

        return result;
    }

    /**
     * 获取当前应用名称
     * @param mContext
     * @return
     */
    public static String getApplicationName(final Activity mContext) {
        PackageManager packageManager = null;
        ApplicationInfo applicationInfo = null;
        try {
            packageManager = mContext.getApplicationContext().getPackageManager();
            applicationInfo = packageManager.getApplicationInfo(mContext.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            applicationInfo = null;
        }
        String applicationName = (String) packageManager.getApplicationLabel(applicationInfo);
        return applicationName;
    }

    /**
     * 获取packageInfo数组，第一个元素为
     * @param mContext
     * @return
     */
    public static PackageInfo[] getAppInfoByPackage(Activity mContext) {
        PackageInfo[] mPackageInfo = new PackageInfo[2];
        List<PackageInfo> packages = mContext.getPackageManager().getInstalledPackages(0);
        L.i("packages Size:" + packages.size());
        for (int i = 0; i < packages.size(); i ++) {
            // L.i("执行：" + i);
            if (packages.get(i).packageName.equals(BAIDU_PACKAGE)) {
                L.i("执行：" + i + "  获取百度地图应用信息...");
                mPackageInfo[0] = packages.get(i);
            } else if (packages.get(i).packageName.equals(GAODE_PACKAGE)) {
                L.i("执行：" + i + "  获取高德地图应用信息...");
                mPackageInfo[1] = packages.get(i);
            }

            if (mPackageInfo[0] != null && mPackageInfo[1] != null) {
                return mPackageInfo;
            }
        }

        return mPackageInfo;
    }
}
