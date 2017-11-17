package com.youyou.uuelectric.renter.UI.main;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.os.Vibrator;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.youyou.uuelectric.renter.Network.NetworkTask;
import com.youyou.uuelectric.renter.Network.user.UserConfig;
import com.youyou.uuelectric.renter.UI.start.StartQueryRequest;
import com.youyou.uuelectric.renter.Utils.Support.Config;

/**
 * 传感监听：用于监听摇一摇切换环境
 */
public class SensorListener implements SensorEventListener {

    private Activity mContext;
    private Vibrator vibrator;
    boolean isShowDialog = false;
    private MaterialDialog materialDialog;

    //速度阀值
    private int mSpeed = 8000;
    //时间间隔
    private int mInterval = 50;
    //上一次摇晃的时间
    private long LastTime;
    //上一次的x、y、z坐标
    private float LastX, LastY, LastZ;

    public SensorListener(Activity activity) {
        this.mContext = activity;
        vibrator = (Vibrator) activity.getSystemService(Context.VIBRATOR_SERVICE);
    }

    /**
     * 感应改变后的回调方法
     *
     * @param event
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        long NowTime = System.currentTimeMillis();
        if ((NowTime - LastTime) < mInterval)
            return;
        //将NowTime赋给LastTime
        LastTime = NowTime;
        //获取x,y,z
        float NowX = event.values[0];
        float NowY = event.values[1];
        float NowZ = event.values[2];
        //计算x,y,z变化量
        float DeltaX = NowX - LastX;
        float DeltaY = NowY - LastY;
        float DeltaZ = NowZ - LastZ;
        //赋值
        LastX = NowX;
        LastY = NowY;
        LastZ = NowZ;
        //计算
        double NowSpeed = Math.sqrt(DeltaX * DeltaX + DeltaY * DeltaY + DeltaZ * DeltaZ) / mInterval * 10000;
        //判断
        if (NowSpeed >= mSpeed) {
            if (!isShowDialog) {
                showDialog();
                vibrator.vibrate(80);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }


    private void showDialog() {
        if (mContext != null) {
            isShowDialog = true;
            final SharedPreferences networkInfo = mContext.getSharedPreferences("network", 0);
            boolean check = networkInfo.getBoolean("check", true);
            int choiceItem = 0;
            if (!check) {
                String ip = networkInfo.getString("ip", NetworkTask.TEST_IP);
                if (NetworkTask.TEST_IP.equals(ip)) {
                    choiceItem = 1;
                } else {
                    choiceItem = 2;
                }
            }
            String[] ips = {NetworkTask.NORMAL_IP, NetworkTask.TEST_IP, NetworkTask.TEST_IP_40};
            materialDialog = new MaterialDialog.Builder(mContext)
                    .theme(Theme.DARK)
                    .title("选择环境：")
                    .items(ips)
                    .itemsCallbackSingleChoice(choiceItem, new MaterialDialog.ListCallbackSingleChoice() {
                        @Override
                        public boolean onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {

                            SharedPreferences.Editor editor = networkInfo.edit();
                            if (which == 0) {
                                NetworkTask.setBASEURL(NetworkTask.NORMAL_IP);         //正式环境
                                editor.putBoolean("check", true).apply();
                                Config.showToast(mContext, "你已切换至正式环境,请退出后,重新启动APP");
                            } else if (which == 1) {
                                NetworkTask.setBASEURL(NetworkTask.TEST_IP);         //160环境
                                editor.putBoolean("check", false);
                                editor.putString("ip", NetworkTask.TEST_IP);
                                editor.commit();
                                Config.showToast(mContext, "你已切换至测试环境(160),请退出后,重新启动APP");
                            } else if (which == 2) {
                                NetworkTask.setBASEURL(NetworkTask.TEST_IP_40);         //40环境
                                editor.putBoolean("check", false);
                                editor.putString("ip", NetworkTask.TEST_IP_40);
                                editor.commit();
                                Config.showToast(mContext, "你已切换至测试环境(40),请退出后,重新启动APP");
                            }
                            dialog.dismiss();

                            // 清空用户数据
                            UserConfig.clearUserInfo(mContext);

                            // 调用startQuery接口
                            StartQueryRequest.isStartQuerySuccess = false;
                            StartQueryRequest.startQueryRequest(mContext);
                            /*// 重新启动应用
                            Intent intent = mContext.getPackageManager().getLaunchIntentForPackage(mContext.getPackageName());
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            mContext.startActivity(intent);*/

                            isShowDialog = false;
                            return true;
                        }
                    })
                    .show();

            materialDialog.setCanceledOnTouchOutside(true);
            materialDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    isShowDialog = false;
                }
            });
        }
    }
}