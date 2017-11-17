package com.youyou.uuelectric.renter.UI.main.rentcar;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.uu.facade.base.cmd.Cmd;
import com.uu.facade.base.common.UuCommon;
import com.uu.facade.order.pb.bean.OrderInterface;
import com.youyou.uuelectric.renter.Network.HttpResponse;
import com.youyou.uuelectric.renter.Network.NetworkTask;
import com.youyou.uuelectric.renter.Network.NetworkUtils;
import com.youyou.uuelectric.renter.Network.UUResponseData;
import com.youyou.uuelectric.renter.R;
import com.youyou.uuelectric.renter.UI.main.MainActivity;
import com.youyou.uuelectric.renter.UI.main.MsgHandler;
import com.youyou.uuelectric.renter.Utils.AnimDialogUtils;
import com.youyou.uuelectric.renter.Utils.Support.Config;
import com.youyou.uuelectric.renter.Utils.Support.L;
import com.youyou.uuelectric.renter.Utils.Support.LocationListener;

import java.util.Calendar;

/**
 * Created by
 * 锁车门的dialog
 */
public class LockCarDoorDialog {

    private ReturnCarActivity mActivity;
    private Button mOk;
    private AnimDialogUtils animDialogUtils;
    private String mParkingId;

    public LockCarDoorDialog(ReturnCarActivity activity) {
        mActivity = activity;
    }

    public void showLockCarDialog(final String parkingId) {
        if (!Config.isNetworkConnected(mActivity)) {
            Config.showToast(mActivity, mActivity.getString(R.string.network_error_tip));
            mActivity.resetButtonEnabled();
            return;
        }
        Config.showProgressDialog(mActivity, false, null, "正在锁车门，请稍候");


//        mActivity.showProgress(false, "正在关车门，请稍候");
        Config.getCoordinates(mActivity, new LocationListener() {
            @Override
            public void locationSuccess(double lat, double lng, String addr) {
                if (lat != 0 && lng != 0) {
//                    Config.showToast(mActivity , addr + lat + "," + lng);;
                    OrderInterface.ReturnCar.Request.Builder request = OrderInterface.ReturnCar.Request.newBuilder();
                    request.setOrderId(Config.getOrderId(mActivity));
                    // cityCode有可能为空
                    if (TextUtils.isEmpty(Config.cityCode)) {
                        Config.cityCode = "010";
                    }
                    request.setCityCode(Config.cityCode);
                    UuCommon.LatLon.Builder builder = UuCommon.LatLon.newBuilder();
                    builder.setLon("" + lng);
                    builder.setLat("" + lat);
                    request.setLatLon(builder.build());
                    request.setParkingId(parkingId);
                    NetworkTask task = new NetworkTask(Cmd.CmdCode.ReturnCar_NL_VALUE);
                    task.setBusiData(request.build().toByteArray());
                    NetworkUtils.executeNetwork(task, new HttpResponse.NetWorkResponse<UUResponseData>() {
                        @Override
                        public void onSuccessResponse(UUResponseData responseData) {
                            if (responseData.getRet() == 0) {
                                try {
                                    mActivity.showResponseCommonMsg(responseData.getResponseCommonMsg());
                                    OrderInterface.ReturnCar.Response response = OrderInterface.ReturnCar.Response.parseFrom(responseData.getBusiData());
                                    if (response.getRet() == 0) {
                                        // 添加超时逻辑判断
                                        isOverTime();
                                    }
                                    //订单状态已改变
                                    else if (response.getRet() == 1) {
                                        mActivity.resetButtonEnabled();
                                        Intent intent = new Intent(mActivity, MainActivity.class);
                                        intent.putExtra("goto", MainActivity.GOTO_NEED_PAY_ORDER);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                        mActivity.startActivity(intent);
                                    }
                                    //弹窗提示还车网点及费用
                                    else if (response.getRet() == 2) {
                                        mActivity.resetButtonEnabled();
                                        mParkingId = response.getParkingId();
                                        mActivity.parkingId = mParkingId;
                                        String windowMsg = response.getWindowMsg();
                                        createLockDoorDialogView(windowMsg);
                                    } else {
                                        mActivity.resetButtonEnabled();
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    mActivity.resetButtonEnabled();
                                }
                            } else {
                                mActivity.resetButtonEnabled();
                            }
                        }

                        @Override
                        public void onError(VolleyError errorResponse) {
                            mActivity.showDefaultNetworkSnackBar();
                            mActivity.resetButtonEnabled();
                        }

                        @Override
                        public void networkFinish() {
                        }
                    });
                } else {
                    mActivity.showDefaultNetworkSnackBar();
                    mActivity.resetButtonEnabled();
                }
            }
        });

    }


    /**
     * 开关车门寻车等异步操作是否超时
     */
    public void isOverTime() {
        Config.timeout = true;
        timeOverThread = new Thread(new Runnable() {
            @Override
            public void run() {
                L.i("当前时间：" + Calendar.getInstance().getTime());
                SystemClock.sleep(Config.timeouttime);

                // 如果已经超时
                if (Config.timeout && Config.loadingDialog != null && Config.loadingDialog.isShowing()) {
                    L.i("超时30秒发送handler消息");
                    if (handler != null) {
                        handler.sendEmptyMessage(1);
                    }
                }
            }
        });
        timeOverThread.start();
    }

    Thread timeOverThread = null;
    /**
     * 开关车门寻车等异步操作是否超时
     */
    public Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            L.i("重新请求服务器获取消息");
            // 轮询去掉，超时后自动重新拉取，不走轮询
            MsgHandler.sendLoopRequest(mActivity);
        }
    };

    /**
     * 创建取消订单的视图View
     */
    public void createLockDoorDialogView(String windowMsg) {

        View rootView = LayoutInflater.from(mActivity).inflate(R.layout.lock_car_door_dialog_layout, null);
        mOk = (Button) rootView.findViewById(R.id.ok_btn);
        mOk.setOnClickListener(okBtnClickListener);
        TextView tvWindowMsg = (TextView) rootView.findViewById(R.id.tv_lock_car_window_msg);
        tvWindowMsg.setText(windowMsg);


        animDialogUtils = AnimDialogUtils.getInstance(mActivity);
        animDialogUtils.initView(rootView, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mParkingId = "";
                mActivity.parkingId = mParkingId;
            }
        });
        animDialogUtils.show();
    }


    /**
     * 好的锁车门 点击
     */
    View.OnClickListener okBtnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {


            animDialogUtils.dismiss();
            showLockCarDialog(mParkingId);
//            EventBus.getDefault().post(new BaseEvent(EventBusConstant.EVENT_TYPE_CANCEL_ORDER, reasonList));
        }
    };


}
