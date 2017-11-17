package com.youyou.uuelectric.renter.UI.main.rentcar;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import com.youyou.uuelectric.renter.R;
import com.youyou.uuelectric.renter.UI.base.BaseActivity;
import com.youyou.uuelectric.renter.UI.main.MsgHandler;
import com.youyou.uuelectric.renter.UI.web.url.WebUrl;
import com.youyou.uuelectric.renter.Utils.Support.Config;
import com.youyou.uuelectric.renter.Utils.Support.IntentConfig;
import com.youyou.uuelectric.renter.Utils.Support.L;
import com.youyou.uuelectric.renter.Utils.eventbus.BaseEvent;
import com.youyou.uuelectric.renter.Utils.eventbus.EventBusConstant;
import com.youyou.uuelectric.renter.Utils.popupwindow.PopupMenuManager;

import java.util.Calendar;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class ReturnCarActivity extends BaseActivity {
    @InjectView(R.id.close_car_door)
    LinearLayout closeCarDoor;
    //网点ID,如果传空说明是第一次点击锁车门，需要校验并提示客户端还车费，如果有值需要判断当前车辆可还车网点是否与网点ID一致，不一致再次弹窗提示，一致执行还车逻辑
    public String parkingId ="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_return_car);
        ButterKnife.inject(this);
        // 初始化weburl
        initWebUri();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!Config.locationIsSuccess()) {
            Config.getCoordinates(mContext, null);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (lockCarDoorDialog != null && lockCarDoorDialog.timeOverThread != null) {
            lockCarDoorDialog.timeOverThread = null;
            lockCarDoorDialog.handler.removeMessages(1);
            lockCarDoorDialog.handler = null;
            lockCarDoorDialog = null;
        }
        // 关闭menu菜单选择
        PopupMenuManager.dismiss();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Config.SETTINGGPS) {
            Config.checkLocationInfo(mContext, "锁车门需要您的准确定位，请打开“GPS”开关。");
        }
    }

    @Override
    public void onEventMainThread(BaseEvent event) {
        super.onEventMainThread(event);
        if (event == null)
            return;
        if (EventBusConstant.EVENT_TYPE_ASYNC_RESULT.equals(event.getType())) {
            int result = (int) event.getExtraData();
            if (result == EventBusConstant.EVENT_TYPE_RESULT_FAILIE) {
                Config.showToast(mContext, "操作失败，请重试");
            }
            // 拉取消息成功之后超时标示设置为false（开关车门等异步操作需要此逻辑）
            Config.timeout = false;
            resetButtonEnabled();
        }
    }


    @OnClick(R.id.call_center)
    public void callCenterClick() {
        Config.callCenter(mContext);
    }


    LockCarDoorDialog lockCarDoorDialog = null;

    @OnClick(R.id.close_car_door)
    public void closeCarDoorClick() {

        closeCarDoor.setEnabled(false);
        if (Config.checkLocationInfo(mContext, "锁车门需要您的准确定位，请打开“GPS”开关。")) {

            //锁车门弹窗的操作
            lockCarDoorDialog = new LockCarDoorDialog(this);
            lockCarDoorDialog.showLockCarDialog(parkingId);

        } else {
            closeCarDoor.setEnabled(true);
        }

    }

    /**
     * 重置关车门按钮状态
     */
    public void resetButtonEnabled() {
        dismissProgress();
        closeCarDoor.setEnabled(true);
    }

    /**
     * 开关车门寻车等异步操作是否超时
     */
    public void isOverTime() {
        Config.timeout = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                L.i("当前时间：" + Calendar.getInstance().getTime());
                SystemClock.sleep(Config.timeouttime);

                // 如果已经超时
                if (Config.timeout && Config.loadingDialog != null && Config.loadingDialog.isShowing()) {
                    L.i("超时30秒发送handler消息");
                    handler.sendEmptyMessage(1);
                }
            }
        }).start();
    }

    /**
     * 开关车门寻车等异步操作是否超时
     */
    public Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            L.i("重新请求服务器获取消息");
            // 轮询去掉，超时后自动重新拉取，不走轮询
            MsgHandler.sendLoopRequest(mContext);
        }
    };


    private void initWebUri() {
        if (getIntent() == null) {
            return;
        }
        if (getIntent().hasExtra(IntentConfig.CAR_GUIDE_URL) && getIntent().hasExtra(IntentConfig.CAR_FEEDBACK_URL)) {
            carConditionUrl = new WebUrl();
            carConditionUrl.setTitle(getIntent().getStringExtra(IntentConfig.CAR_FEEDBACK_TITLE));
            carConditionUrl.setUrl(getIntent().getStringExtra(IntentConfig.CAR_FEEDBACK_URL));
            carGuideUrl = new WebUrl();
            carGuideUrl.setTitle(getIntent().getStringExtra(IntentConfig.CAR_GUIDE_TITLE));
            carGuideUrl.setUrl(getIntent().getStringExtra(IntentConfig.CAR_GUIDE_URL));
            carSafeUrl = new WebUrl();
            carSafeUrl.setTitle(getIntent().getStringExtra(IntentConfig.CAR_SAFE_TITLE));
            carSafeUrl.setUrl(getIntent().getStringExtra(IntentConfig.CAR_SAFE_URL));
            if (menuItem != null) {
                menuItem.setVisible(true);
            }
        } else {
            if (menuItem != null) {
                menuItem.setVisible(false);
            }
        }
    }

    // menu 菜单
    MenuItem menuItem = null;
    WebUrl carConditionUrl = null;// 车况反馈
    WebUrl carGuideUrl = null;// 用车指南
    WebUrl carSafeUrl = null;// 保险故障

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_return_car, menu);
        menuItem = menu.findItem(R.id.returncar_menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem mItem = menu.findItem(R.id.returncar_menu);
        if (mItem != null) {
            if (carConditionUrl != null) {
                mItem.setVisible(true);
            } else {
                mItem.setVisible(false);
            }
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == 0 || item.getItemId() == android.R.id.home) {
            onBackPressed();
        } else if (item.getItemId() == R.id.returncar_menu) {
            View view = mContext.findViewById(R.id.returncar_menu);
            // 显示popupwindow菜单
            PopupMenuManager.initPupopWindow(mContext, PopupMenuManager.RETURN_SHOW, view, carConditionUrl, carGuideUrl, carSafeUrl, null);
        }
        return super.onOptionsItemSelected(item);
    }

}
