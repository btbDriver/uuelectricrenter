package com.youyou.uuelectric.renter.UI.main;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.widget.EditText;

import com.android.volley.VolleyError;
import com.google.protobuf.InvalidProtocolBufferException;
import com.rey.material.widget.Button;
import com.uu.facade.base.cmd.Cmd;
import com.uu.facade.ext.protobuf.bean.ExtInterface;
import com.youyou.uuelectric.renter.Network.HttpResponse;
import com.youyou.uuelectric.renter.Network.NetworkTask;
import com.youyou.uuelectric.renter.Network.NetworkUtils;
import com.youyou.uuelectric.renter.Network.UUResponseData;
import com.youyou.uuelectric.renter.R;
import com.youyou.uuelectric.renter.UI.base.BaseActivity;
import com.youyou.uuelectric.renter.Utils.Support.Config;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * Created by liuchao on 2015/9/15.
 * 意见反馈
 */
public class FeedbackActivity extends BaseActivity {

    @InjectView(R.id.feedback_content_edit)
    EditText feedContent; // 意见反馈功能

    @InjectView(R.id.feedback_contact_edit)
    EditText feedContact; // 联系方式

    @InjectView(R.id.b3_button)
    Button feedOk; // 提交按钮

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);
        ButterKnife.inject(this);

        initView();
    }

    /**
     * 初始化组件
     */
    private void initView() {
        feedOk.setText("提交");
        //判断提交按钮是否可用
        setButton();
        //为意见反馈信息添加内容更改监听事件
        feedContent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!TextUtils.isEmpty(s)) {
                    Config.setB3ViewEnable(feedOk, true);
                } else {
                    Config.setB3ViewEnable(feedOk, false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    @OnClick(R.id.b3_button)
    public void requestFeedback() {
        showProgress(false);
        ExtInterface.Feedback.Request.Builder request = ExtInterface.Feedback.Request.newBuilder();
        String feedContentStr = feedContent.getText().toString().trim();
        String feedContactStr = feedContact.getText().toString().trim();
        request.setContentDesc(feedContentStr);
        request.setContactInformation(feedContactStr);
        NetworkTask task = new NetworkTask(Cmd.CmdCode.Feedback_NL_VALUE);
        task.setBusiData(request.build().toByteArray());
        NetworkUtils.executeNetwork(task, new HttpResponse.NetWorkResponse<UUResponseData>() {
            @Override
            public void onSuccessResponse(UUResponseData responseData) {
                if (responseData.getRet() == 0) {
                    try {
                        showResponseCommonMsg(responseData.getResponseCommonMsg());
                        ExtInterface.Feedback.Response response = ExtInterface.Feedback.Response.parseFrom(responseData.getBusiData());
                        if (response.getRet() == 0) {
                            FeedbackActivity.this.finish();
                        }
                    } catch (InvalidProtocolBufferException e) {
                        e.printStackTrace();
                        showDefaultNetworkSnackBar();
                    }
                }
            }

            @Override
            public void onError(VolleyError errorResponse) {
                showDefaultNetworkSnackBar();
            }

            @Override
            public void networkFinish() {
                dismissProgress();
            }
        });
    }

    /**
     * desc:判断提交按钮是否可用
     */
    public void setButton() {
        if (!TextUtils.isEmpty(feedContent.getText().toString())) {
            Config.setB3ViewEnable(feedOk, true);
        } else {
            Config.setB3ViewEnable(feedOk, false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item == null)
            return true;
        if (item.getItemId() == android.R.id.home || item.getItemId() == 0) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);

    }
}
