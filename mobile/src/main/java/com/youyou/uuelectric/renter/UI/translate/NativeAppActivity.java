package com.youyou.uuelectric.renter.UI.translate;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;


import com.youyou.uuelectric.renter.Network.user.UserConfig;
import com.youyou.uuelectric.renter.UI.login.LoginPhoneActivity;
import com.youyou.uuelectric.renter.UI.license.ValidateLicenseActivity;
import com.youyou.uuelectric.renter.UI.main.MainActivity;
import com.youyou.uuelectric.renter.UI.main.SettingActivity;
import com.youyou.uuelectric.renter.UI.main.user.UserInfoActivity;
import com.youyou.uuelectric.renter.UI.order.FavourActivity;
import com.youyou.uuelectric.renter.UI.order.TripOrderDetailActivity;
import com.youyou.uuelectric.renter.UI.web.H5Activity;
import com.youyou.uuelectric.renter.UI.web.H5Constant;
import com.youyou.uuelectric.renter.Utils.Support.Config;
import com.youyou.uuelectric.renter.Utils.Support.IntentConfig;
import com.youyou.uuelectric.renter.Utils.Support.L;
import com.youyou.uuelectric.renter.Utils.UrlBase64;
import com.youyou.uuelectric.renter.Utils.eventbus.BaseEvent;
import com.youyou.uuelectric.renter.Utils.eventbus.EventBusConstant;
import com.youyou.uuelectric.renter.Utils.task.ActivityUtil;

import java.util.List;
import java.util.Map;

import de.greenrobot.event.EventBus;

/**
 * Created by 16515_000 on 2014/8/12.
 */
public class NativeAppActivity extends Activity
{
    public String tag = "NativeAppActivity";
    public Activity mContext = null;

    public void onCreate(Bundle b)
    {
        super.onCreate(b);
        mContext = this;
        // 尝试获取WebApp页面上过来的URL
        Uri uri = getIntent().getData();
        if (uri != null)
        {
            List<String> pathSegments = uri.getPathSegments();
            String uriQuery = uri.getQuery();
            // 若当前APP退到后台，则开启首页
            Intent intent;
            // 解析SCHEME
            if (pathSegments != null && pathSegments.size() > 0) {
                Map<String, String> queryMap = H5Constant.parseUriQuery(uriQuery);
                // 解析通用参数numberOfClosePre
                if (queryMap.containsKey(H5Constant.NUMBEROFCLOSEPRE) && !"".equals(queryMap.get(H5Constant.NUMBEROFCLOSEPRE))) {
                    int numberOfClosePre = Integer.valueOf(queryMap.get(H5Constant.NUMBEROFCLOSEPRE));
                    // 关闭N层activity
                    ActivityUtil.closeNumberActivities(numberOfClosePre);
                }
                // 解析webview
                if (pathSegments.get(0).equals("webview")) {
                    String h5Url = queryMap.get(H5Constant.MURL);
                    if (!TextUtils.isEmpty(h5Url)) {
                        intent = new Intent(NativeAppActivity.this, H5Activity.class);
                        h5Url = UrlBase64.decode(h5Url);
                        intent.putExtra(H5Constant.MURL, h5Url);
                        intent.putExtra(H5Constant.TITLE, queryMap.get(H5Constant.TITLE) == null ? "" : queryMap.get(H5Constant.TITLE));
                        // 添加H5页面是否可复制参数
                        String canSelect = queryMap.get(H5Constant.CANSELECT) == null ? "" : queryMap.get(H5Constant.CANSELECT);
                        boolean isCanSelect = false;
                        if ("0".equals(canSelect)) {
                            isCanSelect = false;
                        } else if ("1".equals(canSelect)) {
                            isCanSelect = true;
                        }
                        intent.putExtra(H5Constant.OPENLONGCLICK, isCanSelect);
                        startActivityForResult(intent, H5Constant.h5RequestCode);
                    }
                    // 跳转webview不能使用finish函数，否则scheme中back动作numberOfClosePre参数不正确
                    //finish();
                }
                // 跳转SCHEME-当前行程
                else if (pathSegments.get(0).equals("currentTrip")) {
                    intent = new Intent(mContext, MainActivity.class);
                    intent.putExtra("goto", MainActivity.GOTO_CURRENT_STROKE);

                    Config.setOrderId(mContext, queryMap.get("orderId"));
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    finish();
                }
                // 跳转SCHEME-行程详情和待支付页面
                else if (pathSegments.get(0).equals("tripDetail")) {
                    if (queryMap.containsKey("paid") && "1".equals(queryMap.get("paid"))) {
                        // 跳转行程详情页面
                        intent = new Intent(mContext, TripOrderDetailActivity.class);
                        intent.putExtra(IntentConfig.ORDER_ID, queryMap.get("orderId"));
                        intent.putExtra(IntentConfig.KEY_FROM_TRIP, true);
                        intent.putExtra(IntentConfig.KEY_PAGE_TYPE, TripOrderDetailActivity.PAGE_TYPE_TRIP_DETAIL);
                        startActivity(intent);
                        finish();
                    } else {
                        // 关闭当前形成页面的日结订单弹窗
                        L.i("跳转待支付页面,发送关闭日结弹窗的请求......");
                        BaseEvent baseEvent = new BaseEvent(EventBusConstant.EVENT_TYPE_CLOSE_CANCEL_DIALOG, "");
                        EventBus.getDefault().post(baseEvent);

                        // 跳转支付页面
                        intent = new Intent(mContext, MainActivity.class);
                        intent.putExtra("goto", MainActivity.GOTO_NEED_PAY_ORDER);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

                        Config.setOrderId(mContext, queryMap.get("orderId"));
                        startActivity(intent);
                        finish();
                    }

                }
                // 跳转SCHEME-主页地图页面
                else if (pathSegments.get(0).equals("map")) {
                    intent = new Intent(mContext, MainActivity.class);
                    intent.putExtra("goto", MainActivity.GOTO_MAP);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    finish();
                }
                // 跳转SCHEME-用户信息页面
                else if (pathSegments.get(0).equals("userReview")) {
                    intent = new Intent(mContext, UserInfoActivity.class);
                    startActivity(intent);
                    finish();
                }
                // 跳转SCHEME-上传驾照页面
                else if (pathSegments.get(0).equals("driverInfo")) {
                    intent = new Intent(mContext, ValidateLicenseActivity.class);
                    startActivity(intent);
                    finish();
                }
                // 跳转SCHEME-待取车页面
                else if (pathSegments.get(0).equals("getCar")) {
                    intent = new Intent(mContext, MainActivity.class);
                    intent.putExtra("goto", MainActivity.GOTO_GET_CAR);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    intent.putExtra(IntentConfig.ORDER_ID, queryMap.get("orderId"));

                    Config.setOrderId(mContext, queryMap.get("orderId"));
                    startActivity(intent);
                    finish();
                }
                // 跳转SCHEME-费用支付页面
                else if (pathSegments.get(0).equals("payForTrip")) {
                    intent = new Intent(mContext, MainActivity.class);
                    intent.putExtra("goto", MainActivity.GOTO_NEED_PAY_ORDER);

                    Config.setOrderId(mContext, queryMap.get("orderId"));
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    finish();
                }
                // 跳转SCHEME-优惠券列表
                else if (pathSegments.get(0).equals("couponList")) {
                    intent = new Intent(mContext, FavourActivity.class);
                    startActivity(intent);
                    finish();
                }
                // 跳转更多页面
                else if (pathSegments.get(0).equals("more")) {
                    if (!UserConfig.isPassLogined()) {
                        intent = new Intent(mContext, LoginPhoneActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        intent = new Intent(mContext, SettingActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }
                // 跳转登录页面
                else if (pathSegments.get(0).equals("login")) {
                    intent = new Intent(mContext, LoginPhoneActivity.class);
                    // 判断是否传递schemeURL
                    if (queryMap.containsKey(H5Constant.BACKTOSCHEMEURL) && !TextUtils.isEmpty(queryMap.get(H5Constant.BACKTOSCHEMEURL))) {
                        // base64解码schemeUrl
                        String schemeUrl = UrlBase64.decode(queryMap.get(H5Constant.BACKTOSCHEMEURL));
                        intent.putExtra(H5Constant.BACKTOSCHEMEURL, schemeUrl);
                    }
                    startActivity(intent);
                    finish();
                }
                else {
                    // 若解析不到SCHEME，则关闭NativeAppActivity；
                    finish();
                }
            } else {
                finish();
            }
        } else {
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == H5Constant.h5RequestCode) {
            setResult(H5Constant.h5ResultCode, data);
        }
        finish();
    }


}
