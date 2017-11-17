package com.youyou.uuelectric.renter.UI.main.rentcar;

import android.app.Activity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.google.protobuf.InvalidProtocolBufferException;
import com.uu.facade.base.cmd.Cmd;
import com.uu.facade.usecar.protobuf.iface.UsercarInterface;
import com.youyou.uuelectric.renter.Network.HttpResponse;
import com.youyou.uuelectric.renter.Network.NetworkTask;
import com.youyou.uuelectric.renter.Network.NetworkUtils;
import com.youyou.uuelectric.renter.Network.UUResponseData;
import com.youyou.uuelectric.renter.R;
import com.youyou.uuelectric.renter.UI.base.BaseFragment;
import com.youyou.uuelectric.renter.Utils.AnimDialogUtils;
import com.youyou.uuelectric.renter.Utils.SharedPreferencesUtil;
import com.youyou.uuelectric.renter.Utils.Support.Config;
import com.youyou.uuelectric.renter.Utils.Support.L;
import com.youyou.uuelectric.renter.Utils.recycler.OnItemClickListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dcq on 2016/1/20 0020.
 * 显示取消订单原因选择弹窗
 */
public class CommentCarDialog {
    private List<UsercarInterface.EvaluateTag> evaluateTages = new ArrayList<>();
    private List<UsercarInterface.EvaluateTag> tempEts = new ArrayList<>();

    private Activity mActivity;
    private BaseFragment fragment;
    private RecyclerView mRecyclerView;
    private ReasonAdapter mAdapter;
    private Button mOk;
    private String cancelWarning;
    private AnimDialogUtils animDialogUtils;
    private LinearLayout llCommentTopBg;
    private LinearLayout nagtiveComment;
    private LinearLayout normalComment;
    private LinearLayout nagtiveWeight, normalWeight;
    private ImageView ivNormalIc;
    private LinearLayout llCommentResultWithSubmitButton;
    private View viewLine;
    private String orderId;
    private TextView tvCommentTip;

    public CommentCarDialog(BaseFragment fragment) {
        this.fragment = fragment;
        mActivity = fragment.getActivity();
    }

    public CommentCarDialog(Activity mActivity) {
        this.mActivity = mActivity;
    }

    /**
     * 网络请求后显示弹窗
     */
    public void showCommentCarDialog() {
        if (!Config.isNetworkConnected(mActivity)) {
            Config.showToast(mActivity, mActivity.getString(R.string.network_error_tip));
            return;
        }
        final View rootView = initCommentDialogView();

        Config.showProgressDialog(mActivity, false, null);
        UsercarInterface.QueryUseCarEvaluate.Request.Builder request = UsercarInterface.QueryUseCarEvaluate.Request.newBuilder();
        request.setEvaluateScene(UsercarInterface.EvaluateScene.strokeing);
        NetworkTask task = new NetworkTask(Cmd.CmdCode.QueryUseCarEvaluate_VALUE);
        task.setBusiData(request.build().toByteArray());
        NetworkUtils.executeNetwork(task, new HttpResponse.NetWorkResponse<UUResponseData>() {
            @Override
            public void onSuccessResponse(UUResponseData responseData) {

                if (responseData.getRet() == 0) {
                    try {
                        UsercarInterface.QueryUseCarEvaluate.Response response = UsercarInterface.QueryUseCarEvaluate.Response.parseFrom(responseData.getBusiData());
                        if (response.getRet() == 0) {
                            for (UsercarInterface.EvaluateTag evaluateTage : response.getEvaluateTagList()) {
                                evaluateTages.add(evaluateTage);
                                tempEts.add(evaluateTage);
                            }

                            showView(rootView, 1);
                        } else {
                            Config.showToast(mActivity, response.getMsg());
                        }

                    } catch (InvalidProtocolBufferException e) {
                        e.printStackTrace();
                        L.i("评论Exception " + e.getStackTrace().toString());
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

    /**
     * 网络请求后显示弹窗
     *
     * @param orderId
     */
    public void showCommentCarDialog(String orderId) {
        if (!Config.isNetworkConnected(mActivity)) {
            Config.showToast(mActivity, mActivity.getString(R.string.network_error_tip));
            return;
        }

        this.orderId = orderId;
        final View rootView = initCommentDialogView();

        Config.showProgressDialog(mActivity, false, null);
        UsercarInterface.QueryOrderEvaluate.Request.Builder request = UsercarInterface.QueryOrderEvaluate.Request.newBuilder();
        request.setOrderId(orderId);
        NetworkTask task = new NetworkTask(Cmd.CmdCode.QueryOrderEvaluate_VALUE);
        task.setBusiData(request.build().toByteArray());
        NetworkUtils.executeNetwork(task, new HttpResponse.NetWorkResponse<UUResponseData>() {
            @Override
            public void onSuccessResponse(UUResponseData responseData) {

                if (responseData.getRet() == 0) {
                    try {
                        UsercarInterface.QueryOrderEvaluate.Response response = UsercarInterface.QueryOrderEvaluate.Response.parseFrom(responseData.getBusiData());
                        if (response.getRet() == 0) {
                            evaluateTages = response.getEvaluateTagList();

                            UsercarInterface.EvaluateType well = UsercarInterface.EvaluateType.well;
                            UsercarInterface.EvaluateType spitBad = UsercarInterface.EvaluateType.spit_bad;
                            UsercarInterface.EvaluateType userCancel = UsercarInterface.EvaluateType.user_cancel;
                            if (well.equals(response.getEvaluateType()) || userCancel.equals(response.getEvaluateType())) {
                                showView(rootView, 3);
                            } else if (spitBad.equals(response.getEvaluateType())) {
                                showView(rootView, 2);
                            }
                        } else if (response.getRet() == -2) {
                            Config.showToast(mActivity, "该订单暂无评价内容");
                        } else {
                            Config.showToast(mActivity, response.getMsg());
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
     * 初始化弹窗
     *
     * @return
     */
    private View initCommentDialogView() {
        View rootView = LayoutInflater.from(mActivity).inflate(R.layout.comment_car_dialog_layout, null);
        llCommentTopBg = (LinearLayout) rootView.findViewById(R.id.ll_comment_top_bg);

        nagtiveWeight = (LinearLayout) rootView.findViewById(R.id.ll_nagtive_weight);
        normalWeight = (LinearLayout) rootView.findViewById(R.id.ll_normal_weight);
        viewLine = (View) rootView.findViewById(R.id.view_line);

        nagtiveComment = (LinearLayout) rootView.findViewById(R.id.ll_comment_nagtive);
        normalComment = (LinearLayout) rootView.findViewById(R.id.ll_comment_normal);
        llCommentResultWithSubmitButton = (LinearLayout) rootView.findViewById(R.id.ll_comment_result_with_submit_button);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.rv_reason);
        mRecyclerView.setLayoutManager(new GridLayoutManager(mActivity, 2, LinearLayoutManager.VERTICAL, false));
        ivNormalIc = (ImageView) rootView.findViewById(R.id.iv_normal_ic);
        tvCommentTip = (TextView) rootView.findViewById(R.id.tv_comment_tip);
        mOk = (Button) rootView.findViewById(R.id.ok_btn);

        nagtiveComment.setOnClickListener(viewClickListener);
        normalComment.setOnClickListener(viewClickListener);
        mOk.setOnClickListener(viewClickListener);

        return rootView;
    }

    /**
     * 展示评论的视图View
     *
     * @param rootView
     * @param flag     根据不同的值来显示或隐藏view 1.显示评论上传，2显示吐槽，3显示还行
     */
    public void showView(View rootView, int flag) {
        mAdapter = new ReasonAdapter();

        mRecyclerView.setAdapter(mAdapter);

        View.OnClickListener cancleClick = null;
        switch (flag) {
            case 1:
                mAdapter.setOnItemClickListener(onItemClickListener);
                cancleClick = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!Config.isNetworkConnected(mActivity)) {
                            Config.showToast(mActivity, mActivity.getString(R.string.network_error_tip));
                            return;
                        }
                        ((CurrentStrokeFragment) fragment).submitUseCarEvaluate(UsercarInterface.EvaluateType.user_cancel, null);

                        String orderid = Config.getOrderId(mActivity);
                        if (!TextUtils.isEmpty(orderid)) {
                            SharedPreferencesUtil.getSharedPreferences(mActivity).putBoolean("current_stroke_" + orderid, true);
                        }
                    }
                };

                break;
            case 2:
                nagtiveWeight.setVisibility(View.VISIBLE);
                normalWeight.setVisibility(View.GONE);
                viewLine.setVisibility(View.GONE);
                viewClickListener.onClick(nagtiveComment);
                mOk.setVisibility(View.GONE);
                break;
            case 3:
                nagtiveWeight.setVisibility(View.GONE);
                normalWeight.setVisibility(View.VISIBLE);
                viewLine.setVisibility(View.GONE);
                viewClickListener.onClick(normalComment);
                mOk.setVisibility(View.GONE);
                break;
        }

        animDialogUtils = AnimDialogUtils.getInstance(mActivity);
        animDialogUtils.initView(rootView, cancleClick);
        animDialogUtils.show();
    }


    /**
     * 确定按钮点击后发送事件，取消订单
     */
    View.OnClickListener viewClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.ok_btn:

                    if (!Config.isNetworkConnected(mActivity)) {
                        Config.showToast(mActivity, mActivity.getString(R.string.network_error_tip));
                        return;
                    }
                    animDialogUtils.dismiss();
                    if (nagtiveComment.getBackground() != null) {
                        ((CurrentStrokeFragment) fragment).submitUseCarEvaluate(UsercarInterface.EvaluateType.spit_bad, chooseReasonList);
                    } else if (normalComment.getBackground() != null) {
                        ((CurrentStrokeFragment) fragment).submitUseCarEvaluate(UsercarInterface.EvaluateType.well, null);
                    }

                    String orderid = Config.getOrderId(mActivity);
                    if (!TextUtils.isEmpty(orderid)) {
                        SharedPreferencesUtil.getSharedPreferences(mActivity).putBoolean("current_stroke_" + orderid, true);
                    }
                    break;

                case R.id.ll_comment_nagtive:
                    tvCommentTip.setText(R.string.comment_text_nagtive);
                    nagtiveComment.setBackgroundResource(R.mipmap.ic_selected_comment);
                    normalComment.setBackgroundResource(0);
                    if (llCommentResultWithSubmitButton.getVisibility() != View.VISIBLE) {
                        llCommentResultWithSubmitButton.setVisibility(View.VISIBLE);
                        llCommentTopBg.setBackgroundResource(R.drawable.comment_car_top_round_bg);
                    }

                    mRecyclerView.setVisibility(View.VISIBLE);
                    ivNormalIc.setVisibility(View.GONE);
                    break;
                case R.id.ll_comment_normal:
                    tvCommentTip.setText(R.string.comment_text_normal);
                    nagtiveComment.setBackgroundResource(0);
                    normalComment.setBackgroundResource(R.mipmap.ic_selected_comment);
                    if (llCommentResultWithSubmitButton.getVisibility() != View.VISIBLE) {
                        llCommentResultWithSubmitButton.setVisibility(View.VISIBLE);
                        llCommentTopBg.setBackgroundResource(R.drawable.comment_car_top_round_bg);
                    }

                    mRecyclerView.setVisibility(View.GONE);
                    ivNormalIc.setVisibility(View.VISIBLE);
                    break;

            }


        }
    };


    //选择的吐槽原因集合
    private List<UsercarInterface.EvaluateTag> chooseReasonList = new ArrayList<>();

    /**
     * 吐槽原因点击处理（可多选）
     */
    OnItemClickListener onItemClickListener = new OnItemClickListener() {
        @Override
        public void onItemClick(int position) {

            UsercarInterface.EvaluateTag bean = tempEts.get(position);
            if (chooseReasonList.contains(bean)) {
                chooseReasonList.remove(bean);
            } else {
                chooseReasonList.add(bean);
            }

            for (int i = 0; i < tempEts.size(); i++) {
                UsercarInterface.EvaluateTag evaluateTage = tempEts.get(i);
                if (chooseReasonList.contains(evaluateTage)) {

                    UsercarInterface.EvaluateTag et = evaluateTages.remove(i).toBuilder().setSelect(true).build();
                    evaluateTages.add(i, et);
                } else {
                    UsercarInterface.EvaluateTag et = evaluateTages.remove(i).toBuilder().setSelect(false).build();
                    evaluateTages.add(i, et);
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

            UsercarInterface.EvaluateTag evaluateTage = evaluateTages.get(position);
            holder.mReason.setText(evaluateTage.getDesc());
            holder.mReason.setSelected(evaluateTage.getSelect());
            if (!TextUtils.isEmpty(orderId)) {
                if (!evaluateTage.getSelect()) {
                    holder.mReason.setTextColor(mActivity.getResources().getColor(R.color.c5));
                }
            }
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
            return evaluateTages.size();
        }
    }

    class ReasonViewHolder extends RecyclerView.ViewHolder {
        TextView mReason;

        public ReasonViewHolder(View itemView) {
            super(itemView);
            mReason = (TextView) itemView.findViewById(R.id.tv_reason);
        }
    }
}
