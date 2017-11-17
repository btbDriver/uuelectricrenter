package com.youyou.uuelectric.renter.Network.listen;

import android.app.Activity;
import android.view.View;

import com.youyou.uuelectric.renter.Network.user.UserConfig;
import com.youyou.uuelectric.renter.UI.main.MainActivity;

/**
 * Created by liuchao on 2015/9/15.
 * 判断当前用户是否已登录
 */
public abstract class OnClickLoginedListener extends BaseClickListener {

    private Activity context = null;

    public OnClickLoginedListener(Activity context) {
        this.context = context;
    }

    @Override
    public void onClick(View view) {
        super.onClick(view);
        if (context == null)
            return;
        // 判断当前用户是否已经登录，若未登陆则弹出登陆提示框
        if (context instanceof MainActivity) {
            ((MainActivity) context).closeDrawer();
        }
        if (!UserConfig.isPassLogined()) {
            UserConfig.goToLoginDialog(context);
            return;
        }

        onLoginedClick(view);
    }

    public abstract void onLoginedClick(View v);
}
