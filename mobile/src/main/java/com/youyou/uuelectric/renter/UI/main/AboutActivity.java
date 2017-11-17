package com.youyou.uuelectric.renter.UI.main;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.uu.facade.base.cmd.Cmd;
import com.uu.facade.user.protobuf.bean.UserInterface;
import com.youyou.uuelectric.renter.Network.HttpResponse;
import com.youyou.uuelectric.renter.Network.NetworkTask;
import com.youyou.uuelectric.renter.Network.NetworkUtils;
import com.youyou.uuelectric.renter.Network.UUResponseData;
import com.youyou.uuelectric.renter.Network.listen.OnClickFastListener;
import com.youyou.uuelectric.renter.Network.user.UserConfig;
import com.youyou.uuelectric.renter.R;
import com.youyou.uuelectric.renter.UI.base.BaseActivity;
import com.youyou.uuelectric.renter.UI.start.StartQueryRequest;
import com.youyou.uuelectric.renter.UI.web.H5Activity;
import com.youyou.uuelectric.renter.UI.web.H5Constant;
import com.youyou.uuelectric.renter.UI.web.url.URLConfig;
import com.youyou.uuelectric.renter.Utils.Support.Config;
import com.youyou.uuelectric.renter.Utils.Support.SysConfig;
import com.youyou.uuelectric.renter.Utils.VersionUtils;
import com.youyou.uuelectric.renter.Utils.update.UpdateManager;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * 更多--关于
 */
public class AboutActivity extends BaseActivity {

    Dialog dialog;
    @InjectView(R.id.version)
    TextView version;
    @InjectView(R.id.icon)
    ImageView mIcon;
    @InjectView(R.id.name)
    TextView mName;
    @InjectView(R.id.get_version)
    RelativeLayout mGetVersion;
    @InjectView(R.id.line)
    View mLine;
    @InjectView(R.id.rule)
    RelativeLayout mRule;

    @OnClick(R.id.call_center)
    public void phoneClick() {
        Config.callCenter(mContext);
    }

    private long firstTime = 0;
    private long interval = 500;
    private int count = 0;
    Toast toast;


    @OnClick(R.id.rule)
    public void ruleClick() {
        // 平台规则换成H5页面
        if (!Config.isNetworkConnected(mContext)) {
            showDefaultNetworkSnackBar();
            return;
        }
        Intent intentRule = new Intent(mContext, H5Activity.class);
        intentRule.putExtra(H5Constant.MURL, URLConfig.getUrlInfo().getPlatRule().getUrl());
        intentRule.putExtra(H5Constant.TITLE, URLConfig.getUrlInfo().getPlatRule().getTitle());
        intentRule.putExtra(H5Constant.CARFLUSH, false);
        startActivity(intentRule);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        ButterKnife.inject(this);
        try {
            mName.setText("友友用车");
            version.setText("版本:" + VersionUtils.getVersionName(mContext));
        } catch (Exception e) {
            e.printStackTrace();
        }
        toast = new Toast(mContext);

        // 检查新版本
        mGetVersion.setOnClickListener(new OnClickFastListener() {
            @Override
            public void onFastClick(View v) {
                if (!Config.isNetworkConnected(mContext)) {
                    showDefaultNetworkSnackBar();
                    return;
                }

                // 根据用户订单状态判断是否检查更新
                loadUserInfo();
            }
        });

        mIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SysConfig.DEVELOP_MODE) {
                    long secondTime = System.currentTimeMillis();
                    // 判断每次点击的事件间隔是否符合连击的有效范围
                    // 不符合时，有可能是连击的开始，否则就仅仅是单击
                    if (secondTime - firstTime <= interval) {
                        ++count;
                        toast.cancel();
                        toast.makeText(mContext, "还差" + (1 - count) + "下切换环境.快快快,时间不够了", Toast.LENGTH_SHORT).show();
                    } else {
                        count = 1;
                    }
                    // 延迟，用于判断用户的点击操作是否结束
//                    delay();
                    firstTime = secondTime;
                    if (count == 1) {
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
                        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                        builder.setTitle("选择环境：");
                        builder.setSingleChoiceItems(ips, choiceItem, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                SharedPreferences.Editor editor = networkInfo.edit();
                                if (i == 0) {
                                    NetworkTask.setBASEURL(NetworkTask.NORMAL_IP);         //正式环境
                                    editor.putBoolean("check", true).apply();
                                    Config.showToast(mContext, "你已切换至正式环境,请退出后,重新启动APP");
                                } else if (i == 1) {
                                    NetworkTask.setBASEURL(NetworkTask.TEST_IP);         //160环境
                                    editor.putBoolean("check", false);
                                    editor.putString("ip", NetworkTask.TEST_IP);
                                    editor.commit();
                                    Config.showToast(mContext, "你已切换至测试环境(160),请退出后,重新启动APP");
                                } else if (i == 2) {
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
                                Intent intent = getPackageManager().getLaunchIntentForPackage(getPackageName());
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);*/
                            }
                        });
                        dialog = builder.create();
                        dialog.setCanceledOnTouchOutside(true);
                        dialog.show();
                    }
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home || item.getItemId() == 0) {
            onBackPressed();
            return false;
        }
        return true;
    }


    /**
     * 拉取用户状态
     */
    private void loadUserInfo() {
        // 请求之前取消所有的拉去信息
        showProgress(false);

        UserInterface.UserInfo.Request.Builder request = UserInterface.UserInfo.Request.newBuilder();
        request.setR((int) (System.currentTimeMillis() / 1000));
        NetworkTask task = new NetworkTask(Cmd.CmdCode.UserInfo_NL_VALUE);
        task.setBusiData(request.build().toByteArray());
        task.setTag("about_userinfo");
        NetworkUtils.executeNetwork(task, new HttpResponse.NetWorkResponse<UUResponseData>() {
            @Override
            public void onSuccessResponse(UUResponseData responseData) {
                if (responseData.getRet() == 0) {
                    try {
                        UserInterface.UserInfo.Response response = UserInterface.UserInfo.Response.parseFrom(responseData.getBusiData());
                        if (response.getRet() == 0) {
                            int orderStatus = response.getOrderStatus();
                            if (orderStatus != MainActivity.WAIT_GET_CAR && orderStatus != MainActivity.CURRENT_STROKE
                                    && orderStatus != MainActivity.WAIT_PAY && orderStatus != MainActivity.OTHER_PAY) {
                                // 若用户没有待取车，进行中，待支付订单，则检测是否是要版本更新
                                UpdateManager.queryAppBaseVersionInfo(mContext, false, true);
                            } else {
                                dismissProgress();
                                Config.showToast(mContext, mContext.getResources().getString(R.string.about_new));
                            }
                        } else {
                            dismissProgress();
                            showDefaultNetworkSnackBar();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        dismissProgress();
                        showDefaultNetworkSnackBar();
                    }
                }
            }

            @Override
            public void onError(VolleyError errorResponse) {
                showDefaultNetworkSnackBar();
                dismissProgress();
            }

            @Override
            public void networkFinish() {
            }
        });
    }
}
