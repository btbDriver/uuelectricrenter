package com.youyou.uuelectric.renter.UI.mapsearch;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.uu.facade.dot.protobuf.iface.DotInterface;
import com.youyou.uuelectric.renter.R;
import com.youyou.uuelectric.renter.Utils.recycler.OnItemClickListener;
import com.youyou.uuelectric.renter.Utils.view.RippleView;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import me.grantland.widget.AutofitTextView;

/**
 * Created by Administrator on 2016/3/10.
 */
public class DotAdapter extends RecyclerView.Adapter<DotAdapter.DotViewHolder> {

    OnItemClickListener onItemClickListener;
    private List<DotInterface.DotInfo> myDotInfoList;
    //传入状态值，用来做基础判断，用来判断是否要添加第一行的本次网点还车 默认为0不添加，1添加
    private int type = 0;
    //是否显示左侧的图标
    private boolean isShowIcon;
    private Context context;

    public DotAdapter(Context context, List<DotInterface.DotInfo> myDotInfoList) {
        super();
        this.context = context;
        this.myDotInfoList = myDotInfoList;
    }

    public DotAdapter(Context context, List<DotInterface.DotInfo> myDotInfoList, int type) {
        this.context = context;
        this.myDotInfoList = myDotInfoList;
        this.type = type;
    }

    public DotAdapter(Context context, List<DotInterface.DotInfo> myDotInfoList, boolean isShowIcon) {
        this.context = context;
        this.myDotInfoList = myDotInfoList;
        this.isShowIcon = isShowIcon;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    @Override
    public DotViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.near_map_station_item, parent, false);
        return new DotViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(DotViewHolder holder, final int position) {

        // 网络请求未通，暂时注释
        DotInterface.DotInfo dotInfo = myDotInfoList.get(position);

        if (type == 1) {
            if (position == 0) {
                holder.mTvTitle.setTextColor(context.getResources().getColor(R.color.c1));
                holder.mTvDistance.setVisibility(View.GONE);
                holder.mTvTitle.setText("本次取车网点");
                holder.mTvSubTitle.setText(dotInfo.getDotName());
            } else {
                holder.mTvTitle.setTextColor(context.getResources().getColor(R.color.c3));
                holder.mTvDistance.setVisibility(View.GONE);
                holder.mTvTitle.setText(dotInfo.getDotName());
                holder.mTvSubTitle.setText(dotInfo.getDotDesc());
            }
        } else {
            holder.mTvTitle.setTextColor(context.getResources().getColor(R.color.c3));
            holder.mTvDistance.setVisibility(View.VISIBLE);
            float distance = dotInfo.getDistance() / 1000f;
            String result;
            if (distance >= 1) {
                result = String.format("%.2f", distance) + "km";
            } else {
                result = dotInfo.getDistance() + "m";
            }
            holder.mTvDistance.setText(result);
            holder.mTvTitle.setText(dotInfo.getDotName());
            holder.mTvSubTitle.setText(dotInfo.getDotDesc());
        }

        float backCarFee = dotInfo.getBackCarFee();
        if (backCarFee > 0) {
            holder.mTvRemotePrice.setVisibility(View.VISIBLE);
            holder.mTvRemotePrice.setText("+ " + (int) Math.ceil(backCarFee) + "元");
        } else {
            holder.mTvRemotePrice.setVisibility(View.GONE);
        }


        if (isShowIcon) {
            holder.mIvIcon.setVisibility(View.VISIBLE);
            holder.mTvNumber.setText((position + 1) + "");
        } else {
            holder.mIvIcon.setVisibility(View.GONE);

        }

        holder.mRvRoot.setOnRippleCompleteListener(new RippleView.OnRippleCompleteListener() {
            @Override
            public void onComplete(RippleView rippleView) {
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(position);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return myDotInfoList.size();
    }

    class DotViewHolder extends RecyclerView.ViewHolder {
        @InjectView(R.id.rv_root)
        RippleView mRvRoot;
        @InjectView(R.id.tv_title)
        AutofitTextView mTvTitle;
        @InjectView(R.id.tv_distance)
        TextView mTvDistance;
        @InjectView(R.id.tv_remote_price)
        TextView mTvRemotePrice;
        @InjectView(R.id.tv_sub_title)
        TextView mTvSubTitle;
        @InjectView(R.id.tv_marker_number)
        TextView mTvNumber;
        @InjectView(R.id.iv_icon)
        RelativeLayout mIvIcon;

        public DotViewHolder(View itemView) {
            super(itemView);
            ButterKnife.inject(this, itemView);
        }
    }

}
