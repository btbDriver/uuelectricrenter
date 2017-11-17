package com.youyou.uuelectric.renter.UI.main;

import android.app.Activity;
import android.content.Intent;

import com.youyou.uuelectric.renter.Network.user.UserConfig;
import com.youyou.uuelectric.renter.UI.web.H5Constant;
import com.youyou.uuelectric.renter.UI.web.url.URLConfig;

import it.neokree.materialnavigationdrawer.elements.MaterialSection;
import it.neokree.materialnavigationdrawer.elements.listeners.MaterialSectionListener;

/**
 * Created by liuchao on 2015/12/2.
 */
public class MSectionListener implements MaterialSectionListener {

    /**
     * ### 侧导跳转的时候intent的title，url等参数在onClick内部赋值，这是因为这些参数需要请求网络
     * 若直接赋值的话那么可能来不及更新网络获取的参数值
     * 侧导跳转H5的时候传递参数，判断是否跳转的是H5页面
     */
    public static final String isGotoH5Args = "isGotoH5Args";
    /**
     * 判断跳转的是那个H5页面（当前H5侧导页面：余额，活动，邀请好友）
     */
    public static final String GotoH5 = "gotoh5";
    /**
     * 跳转H5页面：余额页面，活动页面，邀请好友页面,用车早知道页面
     */
    public static final int GotoH5balance = 1;
    public static final int GotoH5active = 2;
    public static final int GotoH5share = 3;
    public static final int GotoH5Faq = 4;

    public Intent intent;
    public Activity mContext;

    public MSectionListener(Intent intent, Activity mContext) {
        this.intent = intent;
        this.mContext = mContext;
    }

    @Override
    public void onClick(MaterialSection section) {
        // 若用户点击用车早知道按钮，则不必验证是否登录
        if (intent.hasExtra(isGotoH5Args) && intent.getBooleanExtra(isGotoH5Args, false)) {
            // 判断是否跳转FAQ页面
            if (intent.hasExtra(GotoH5) && intent.getIntExtra(GotoH5, -1) == GotoH5Faq) {
                intent.putExtra(H5Constant.TITLE, URLConfig.getUrlInfo().getFaq().getTitle());
                intent.putExtra(H5Constant.MURL, URLConfig.getUrlInfo().getFaq().getUrl());
                mContext.startActivity(intent);
                return;
            }
        }

        // 判断当前是否登陆，若未登陆则显示登陆界面
        if (!UserConfig.isPassLogined()) {
            UserConfig.goToLoginDialog(mContext);
            return;
        }

        // 判断是否跳转的为H5页面
        if (intent.hasExtra(isGotoH5Args) && intent.getBooleanExtra(isGotoH5Args, false)) {
            // 判断跳转的是哪个H5页面
            // 跳转余额页面
            if (intent.hasExtra(GotoH5) && intent.getIntExtra(GotoH5, -1) == GotoH5balance) {
                intent.putExtra(H5Constant.TITLE, URLConfig.getUrlInfo().getBalanceList().getTitle());
                intent.putExtra(H5Constant.MURL, URLConfig.getUrlInfo().getBalanceList().getUrl());
            }
            // 跳转活动页面
            else if (intent.hasExtra(GotoH5) && intent.getIntExtra(GotoH5, -1) == GotoH5active) {
                intent.putExtra(H5Constant.TITLE, URLConfig.getUrlInfo().getActiveList().getTitle());
                intent.putExtra(H5Constant.MURL, URLConfig.getUrlInfo().getActiveList().getUrl());
            }
            // 跳转邀请好友页面
            else if (intent.hasExtra(GotoH5) && intent.getIntExtra(GotoH5, -1) == GotoH5share) {
                intent.putExtra(H5Constant.TITLE, URLConfig.getUrlInfo().getShare().getTitle());
                intent.putExtra(H5Constant.MURL, URLConfig.getUrlInfo().getShare().getUrl());
                // 邀请好友添加H5页面可复制参数
                intent.putExtra(H5Constant.OPENLONGCLICK, true);
            }
            // 参数错误
            if (!intent.hasExtra(H5Constant.MURL)) {
                return;
            }
        }
        // 执行其他跳转逻辑
        mContext.startActivity(intent);
    }

}
