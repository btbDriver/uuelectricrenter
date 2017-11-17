package com.youyou.uuelectric.renter.UI.web.url;

import android.content.Context;
import android.text.TextUtils;

import com.uu.facade.base.common.UuCommon;
import com.uu.facade.user.protobuf.bean.UserInterface;
import com.youyou.uuelectric.renter.Network.user.GsonUtils;
import com.youyou.uuelectric.renter.Network.user.SPConstant;
import com.youyou.uuelectric.renter.Service.LoopService;
import com.youyou.uuelectric.renter.UUApp;
import com.youyou.uuelectric.renter.Utils.Support.L;
import com.youyou.uuelectric.renter.Utils.Support.SysConfig;

/**
 * Created by liuchao on 2015/9/15.
 * URL工具操作类
 */
public class URLConfig {
    // 测试环境URL 地址
    public static String TEST_URLIP = "uuyc.test.uuzuche.com.cn:81";
    // public static String TEST_URLIP = "192.168.255.130:81";
    // 正式环境URL地址
    public static String NORMAL_URLID = "w.yc.uuzuche.com.cn:81";

    public static final String URL_PRE = "http://";
    /**
     * 用于保存URL信息，获取urlInfo对象通过get方法获得
     */
    public static URLInfo urlInfo = new URLInfo();

    /**
     * 初始化WEBVIEW URL信息
     *
     * @param context
     */
    public static void init(Context context) {
        if (context == null)
            return;
        // 是否测试环境
        String BASE_IP = TEST_URLIP;
        if (SysConfig.DEVELOP_MODE) {
            BASE_IP = TEST_URLIP;
        } else {
            BASE_IP = NORMAL_URLID;
        }
        if (urlInfo == null) {
            urlInfo = new URLInfo();
        }
        urlInfo.setRegister(new WebUrl(new StringBuffer().append(URL_PRE).append(BASE_IP)
                .append("/user/faq?option=hyxy").toString(), "友友用车会员协议"));
        urlInfo.setShare(new WebUrl(new StringBuffer(URL_PRE).append(BASE_IP)
                .append("/user/shareCode").toString(), "邀请好友"));
        urlInfo.setActiveList(new WebUrl(new StringBuffer(URL_PRE).append(BASE_IP)
                .append("/advert/activeList").toString(), "活动"));
        urlInfo.setBalanceList(new WebUrl(new StringBuffer(URL_PRE).append(BASE_IP)
                .append("/finance/balanceList").toString(), "余额"));
        urlInfo.setBillingRule(new WebUrl(new StringBuffer(URL_PRE).append(BASE_IP)
                .append("/user/faq?option=jfgz").toString(), "计费规则"));
        urlInfo.setHelpCenter(new WebUrl(new StringBuffer(URL_PRE).append(BASE_IP)
                .append("/user/faq").toString(), "帮助中心"));
        urlInfo.setPlatRule(new WebUrl(new StringBuffer(URL_PRE).append(BASE_IP)
                .append("/user/faq?option=ptgz").toString(), "平台规则"));
        urlInfo.setPaymentNotice(new WebUrl(new StringBuffer(URL_PRE).append(BASE_IP)
                .append("/finance/paymentNotice").toString(), "待支付费用"));
        urlInfo.setPeccancyList(new WebUrl(new StringBuffer(URL_PRE).append(BASE_IP)
                .append("/finance/peccancyList").toString(), "待处理违章"));

        urlInfo.setFaq(new WebUrl(new StringBuffer(URL_PRE).append(BASE_IP)
                .append("/user/faq?option=yczzd").toString(), "用车早知道"));

        // 无需初始化发票url
        // 服务器下发则显示发票按钮，不下发则不显示
        URLSPUtils.setParam(context, SPConstant.SPNAME_WEBVIEW_URL, SPConstant.SPKEY_WEBVIEW_URL, urlInfo);
    }

    /**
     * 更新URL信息
     *
     * @param response
     * @param urlInfo
     */
    public static void updateURLInfo(UserInterface.StartQueryInterface.Response response, URLInfo urlInfo) {
        if (response == null || urlInfo == null)
            return;
        if (!isEmpty(response.getActiveList())) {
            urlInfo.setActiveList(changePBWebviewTOWebview(response.getActiveList()));
        }
        if (!isEmpty(response.getBalanceList())) {
            urlInfo.setBalanceList(changePBWebviewTOWebview(response.getBalanceList()));
        }
        if (!isEmpty(response.getBillingRule())) {
            urlInfo.setBillingRule(changePBWebviewTOWebview(response.getBillingRule()));
        }
        if (!isEmpty(response.getHelpCenter())) {
            urlInfo.setHelpCenter(changePBWebviewTOWebview(response.getHelpCenter()));
        }
        if (!isEmpty(response.getInvoice())) {
            urlInfo.setInvoice(changePBWebviewTOWebview(response.getInvoice()));
        }
        if (!isEmpty(response.getShare())) {
            urlInfo.setShare(changePBWebviewTOWebview(response.getShare()));
        }
        if (!isEmpty(response.getPaymentNotice())) {
            urlInfo.setPaymentNotice(changePBWebviewTOWebview(response.getPaymentNotice()));
        }
        if (!isEmpty(response.getPeccancyList())) {
            urlInfo.setPeccancyList(changePBWebviewTOWebview(response.getPeccancyList()));
        }
        // 用户早知道
        if (!isEmpty(response.getCarEarlyKnow())) {
            urlInfo.setFaq(changePBWebviewTOWebview(response.getCarEarlyKnow()));
            L.i("url:" + urlInfo.getFaq().getUrl());
            L.i("title:" + urlInfo.getFaq().getTitle());
        }
        // 注册用户协议
        if (!isEmpty(response.getRegisteredAgreement())) {
            urlInfo.setRegister(changePBWebviewTOWebview(response.getRegisteredAgreement()));
        }
        // 平台规则
        if (!isEmpty(response.getPlatformRule())) {
            urlInfo.setPlatRule(changePBWebviewTOWebview(response.getPlatformRule()));
        }
        // 更新本地b4domain（登录时不再下发demain）
        urlInfo.setB4Domain(response.getB4Domain());

        // 更新侧边栏充值活动文案
        urlInfo.setRechargeDesc(response.getRechargeDesc());


        LoopService.LOOP_INTERVAL_SECS = response.getLongConnectionLoopMsgIntervalSecs();
        URLSPUtils.setParam(UUApp.getInstance().getContext(), SPConstant.SPNAME_WEBVIEW_URL, SPConstant.SPKEY_WEBVIEW_URL, urlInfo);
        urlInfo = new URLInfo();
    }


    /**
     * 判断weburl是否为空
     * @param webUrl
     * @return
     */
    public static boolean isEmpty(UuCommon.WebUrl webUrl) {
        if (webUrl == null || TextUtils.isEmpty(webUrl.getUrl())) {
            return true;
        }

        return false;
    }

    /**
     * 将WEBURL pb对象转换为本地对象
     *
     * @param webview
     * @return
     */
    public static WebUrl changePBWebviewTOWebview(UuCommon.WebUrl webview) {
        WebUrl webUrl = new WebUrl();
        webUrl.setTitle(webview.getTitle());
        webUrl.setUrl(webview.getUrl().trim());

        return webUrl;
    }


    /**
     * 获取URLInfo对象
     *
     * @return
     */
    public static URLInfo getUrlInfo() {
        // 若内存对象为空，则加载SP中内存
        if (urlInfo.getHelpCenter() == null || TextUtils.isEmpty(urlInfo.getHelpCenter().getUrl())) {
            urlInfo = GsonUtils.getInstance().fromJson(URLSPUtils.getParam(UUApp.getInstance().getContext(), SPConstant.SPNAME_WEBVIEW_URL, SPConstant.SPKEY_WEBVIEW_URL), URLInfo.class);
        }
        // 若SP中内存还是为空，则初始化方法
        if (urlInfo.getHelpCenter() == null || TextUtils.isEmpty(urlInfo.getHelpCenter().getUrl())) {
            if (UUApp.getInstance().getContext() != null) {
                init(UUApp.getInstance().getContext());
            }
        }
        return urlInfo;
    }

}
