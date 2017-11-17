package com.youyou.uuelectric.renter.UI.main.rentcar;

import android.app.Activity;
import android.graphics.Color;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.google.protobuf.InvalidProtocolBufferException;
import com.uu.facade.base.cmd.Cmd;
import com.uu.facade.order.pb.bean.OrderInterface;
import com.youyou.uuelectric.renter.Network.HttpResponse;
import com.youyou.uuelectric.renter.Network.NetworkTask;
import com.youyou.uuelectric.renter.Network.NetworkUtils;
import com.youyou.uuelectric.renter.Network.UUResponseData;
import com.youyou.uuelectric.renter.R;
import com.youyou.uuelectric.renter.Utils.AnimDialogUtils;
import com.youyou.uuelectric.renter.Utils.Support.Config;
import com.youyou.uuelectric.renter.Utils.eventbus.BaseEvent;
import com.youyou.uuelectric.renter.Utils.eventbus.EventBusConstant;
import com.youyou.uuelectric.renter.Utils.recycler.OnItemClickListener;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by dcq on 2016/1/20 0020.
 * 显示取消订单原因选择弹窗
 */
public class CancelOrderDialog {

    private List<ChoiceReasonBean> choiceReasonBeanList = new ArrayList<>();
    private Activity mActivity;
    private RecyclerView mRecyclerView;
    private ReasonAdapter mAdapter;
    private Button mOk;
    private String cancelWarning;
    private TextView mCancelTip;
    private AnimDialogUtils animDialogUtils;

    public CancelOrderDialog(Activity activity) {
        mActivity = activity;
    }

    public void showCancelOrderDialog() {
        if (!Config.isNetworkConnected(mActivity)) {
            Config.showToast(mActivity, mActivity.getString(R.string.network_error_tip));
            return;
        }
        Config.showProgressDialog(mActivity, false, null);
        OrderInterface.CancelOrderReasonList.Request.Builder request = OrderInterface.CancelOrderReasonList.Request.newBuilder();
        request.setCurrentTime(System.currentTimeMillis());
        NetworkTask task = new NetworkTask(Cmd.CmdCode.CancelOrderReasonList_NL_VALUE);
        task.setBusiData(request.build().toByteArray());
        NetworkUtils.executeNetwork(task, new HttpResponse.NetWorkResponse<UUResponseData>() {
            @Override
            public void onSuccessResponse(UUResponseData responseData) {

                if (responseData.getRet() == 0) {
                    try {
                        OrderInterface.CancelOrderReasonList.Response response = OrderInterface.CancelOrderReasonList.Response.parseFrom(responseData.getBusiData());
                        if (response.getRet() == 0) {
                            List<String> reasonList = response.getReasonList();
                            choiceReasonBeanList.clear();

                            int len = reasonList.size();
                            int j;
                            for (int i = 0; i < len; i++) {
                                if (i % 2 == 0) {
                                    j = (int) i / 2;
                                } else {
                                    j = len / 2 + (int) i / 2;
                                }
                                String reason = reasonList.get(j);
                                ChoiceReasonBean bean = new ChoiceReasonBean(reason, false);
                                choiceReasonBeanList.add(bean);
                            }
                            cancelWarning = response.getCancelWarning();

                            createCancelDialogView(response.getCancelWarningRGBValue());
                        }

                    } catch (InvalidProtocolBufferException e) {
                        e.printStackTrace();
                    }
                }

            }

            @Override
            public void onError(VolleyError errorResponse) {
                Config.showToast(mActivity, mActivity.getString(R.string.network_error_tip));
            }

            @Override
            public void networkFinish() {
                Config.dismissProgress();
            }
        });

    }

    public void dismissCancelOrderDialog() {
        if (animDialogUtils != null && animDialogUtils.isShowing()) {
            animDialogUtils.dismiss();
        }
    }

    /**
     * 创建取消订单的视图View
     */
    public void createCancelDialogView(String cancelWarningRGBValue) {

        View rootView = LayoutInflater.from(mActivity).inflate(R.layout.cancel_order_dialog_layout, null);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.rv_reason);
        mRecyclerView.setLayoutManager(new GridLayoutManager(mActivity, 2, LinearLayoutManager.VERTICAL, false));
        mOk = (Button) rootView.findViewById(R.id.ok_btn);
        mOk.setOnClickListener(okBtnClickListener);
        mCancelTip = (TextView) rootView.findViewById(R.id.tv_cancel_tip);
        mCancelTip.setText(cancelWarning);
        // 解析服务器下发msg颜色
        if (!TextUtils.isEmpty(cancelWarningRGBValue) && cancelWarningRGBValue.startsWith("#")) {
            mCancelTip.setTextColor(Color.parseColor(cancelWarningRGBValue));
        }

        mAdapter = new ReasonAdapter();
        mAdapter.setOnItemClickListener(onItemClickListener);
        mRecyclerView.setAdapter(mAdapter);

        animDialogUtils = AnimDialogUtils.getInstance(mActivity);
        animDialogUtils.initView(rootView, null);
        animDialogUtils.show();
    }


    /**
     * 确定按钮点击后发送事件，取消订单
     */
    View.OnClickListener okBtnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            List<String> reasonList = new ArrayList<>();
            for (ChoiceReasonBean bean : choiceReasonBeanList) {
                if (bean.isSelect()) {
                    reasonList.add(bean.getReason());
                    break;
                }
            }
            animDialogUtils.dismiss();
            EventBus.getDefault().post(new BaseEvent(EventBusConstant.EVENT_TYPE_CANCEL_ORDER, reasonList));
        }
    };


    /**
     * 取消原因点击处理
     */
    OnItemClickListener onItemClickListener = new OnItemClickListener() {
        @Override
        public void onItemClick(int position) {
            for (int i = 0; i < choiceReasonBeanList.size(); i++) {
                ChoiceReasonBean bean = choiceReasonBeanList.get(i);
                if (i == position) {
                    bean.setSelect(true);
                } else {
                    bean.setSelect(false);
                }
            }
            mAdapter.notifyDataSetChanged();
        }
    };

    class ReasonAdapter extends RecyclerView.Adapter<ReasonViewHolder> {

        OnItemClickListener onItemClickListener;

        public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
            this.onItemClickListener = onItemClickListener;
        }

        @Override
        public ReasonViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cancel_reason_item_layout, parent, false);
            return new ReasonViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ReasonViewHolder holder, final int position) {

            ChoiceReasonBean bean = choiceReasonBeanList.get(position);
            holder.mReason.setText(bean.getReason());
            holder.mReason.setSelected(bean.isSelect());
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onItemClickListener != null) {
                        onItemClickListener.onItemClick(position);
                    }
                }
            });

        }

        @Override
        public int getItemCount() {
            return choiceReasonBeanList.size();
        }
    }

    class ReasonViewHolder extends RecyclerView.ViewHolder {
        TextView mReason;

        public ReasonViewHolder(View itemView) {
            super(itemView);
            mReason = (TextView) itemView.findViewById(R.id.tv_reason);
        }
    }


    class ChoiceReasonBean {
        public String reason;
        public boolean isSelect;

        public ChoiceReasonBean(String reason, boolean isSelect) {
            this.reason = reason;
            this.isSelect = isSelect;
        }

        public String getReason() {
            return reason;
        }

        public boolean isSelect() {
            return isSelect;
        }

        public void setSelect(boolean select) {
            isSelect = select;
        }
    }
}
