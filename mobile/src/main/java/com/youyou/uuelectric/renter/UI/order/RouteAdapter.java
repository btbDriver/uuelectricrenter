package com.youyou.uuelectric.renter.UI.order;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.uu.facade.order.pb.common.OrderCommon;
import com.uu.facade.trip.protobuf.common.TripCommon;
import com.youyou.uuelectric.renter.R;
import com.youyou.uuelectric.renter.Utils.Support.Config;
import com.youyou.uuelectric.renter.Utils.view.RippleView;

import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Created by liuchao on 2015/9/8.
 * 行程管理adapter
 */
public class RouteAdapter extends RecyclerView.Adapter<RouteAdapter.ViewHolder> {

    /**
     * 全局上下文
     */
    public Context mContext;
    /**
     * 是否显示加载更多
     */
    public boolean isShowLoadMore = false;
    /**
     * 加载更多布局id
     */
    public int loadMoreLayout = R.layout.recycler_view_load_more;
    /**
     * 正常显示数据的item布局
     */
    public int itemLayout = R.layout.activity_route_item;
    /**
     * 数据源
     */
    public List<TripCommon.TripListInfo> datas = null;

    public OnItemClickListener onItemClickListener = null;


    public RouteAdapter(Context context, List<TripCommon.TripListInfo> datas, OnItemClickListener onItemClickListener) {
        this.mContext = context;
        this.datas = datas;
        this.onItemClickListener = onItemClickListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(viewType, parent, false);
        return new ViewHolder(mContext, v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        if (getItemViewType(position) != itemLayout) {
            // 设置加载更多布局是否显示
            if (getItemViewType(position) == loadMoreLayout) {
                if (Config.isNetworkConnected(mContext)) {
                    holder.itemView.setVisibility(View.VISIBLE);
                } else {
                    holder.itemView.setVisibility(View.GONE);
                    Config.showFiledToast((Activity) mContext);
                }
            }
            return;
        }

        // 设置显示内容
        holder.routeItemTitle.setText(datas.get(position).getStatusDesc());
        holder.routeItemTime.setText(tipformatter.format(datas.get(position).getCreateTime() * 1000L));
        holder.routeItemBook.setText(datas.get(position).getCar());
        holder.routeItemCar.setText(datas.get(position).getLicensePlateNumber());
        holder.routeItemAddress.setText(datas.get(position).getGetCarAddress());
        // 设置显示颜色
        showStatus(holder, datas.get(position).getStatus());

        holder.routeItemRootRela.setOnRippleCompleteListener(new RippleView.OnRippleCompleteListener() {
            @Override
            public void onComplete(RippleView rippleView) {
                if (datas != null && datas.size() > position) {
                    onItemClickListener.onItemClick(datas.get(position));
                }
            }
        });

        holder.itemView.setTag(datas.get(position));
    }

    @Override
    public int getItemCount() {
        if (datas != null && datas.size() > 0) {
            if (isShowLoadMore)
                return datas.size() + 1;
            else
                return datas.size();
        }
        return 0;
    }

    @Override
    public int getItemViewType(int position) {
        int viewType = itemLayout;
        if (position == getItemCount() - 1 && isShowLoadMore) {
            viewType = loadMoreLayout;
        }
        return viewType;
    }

    SimpleDateFormat tipformatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    class ViewHolder extends RecyclerView.ViewHolder {

        public RippleView routeItemRootRela;
        public TextView routeItemTitle;// 行程title
        public TextView routeItemTime;// 行程时间
        public TextView routeItemBook;// 预定车辆
        public TextView routeItemCar;// 车牌
        public TextView routeItemAddress;// 取车地点

        public ViewHolder(final Context mContext, final View itemView) {
            super(itemView);

            routeItemRootRela = (RippleView) itemView.findViewById(R.id.route_item_root_rela);
            routeItemTitle = (TextView) itemView.findViewById(R.id.route_item_title_text);
            routeItemTime = (TextView) itemView.findViewById(R.id.route_item_time_text);
            routeItemBook = (TextView) itemView.findViewById(R.id.route_item_book_content);
            routeItemCar = (TextView) itemView.findViewById(R.id.route_item_car_content);
            routeItemAddress = (TextView) itemView.findViewById(R.id.route_item_address_content);
        }
    }


    // ############################## get set方法 ############################################

    public void showStatus(ViewHolder holder, int status) {
        // 已取消
        if (status == OrderCommon.OrderFormStatus.renter_cancel_VALUE) {
            holder.routeItemTitle.setTextColor(mContext.getResources().getColor(R.color.c4));
        }
        // 已完成
        else if (status == OrderCommon.OrderFormStatus.order_finish_VALUE) {
            holder.routeItemTitle.setTextColor(mContext.getResources().getColor(R.color.c3));
        }
        // 待取车、在用车、待支付
        else if (status == OrderCommon.OrderFormStatus.wait_get_car_VALUE ||
                status == OrderCommon.OrderFormStatus.using_car_VALUE ||
                status == OrderCommon.OrderFormStatus.wait_pay_VALUE) {
            holder.routeItemTitle.setTextColor(mContext.getResources().getColor(R.color.c8));
        } else {
            holder.routeItemTitle.setTextColor(mContext.getResources().getColor(R.color.c8));
        }
    }

    /**
     * adapter item 点击事件
     */
    interface OnItemClickListener{

        public abstract void onItemClick(TripCommon.TripListInfo tripListInfo);
    }

    public void setmContext(Context mContext) {
        this.mContext = mContext;
    }

    public void setIsShowLoadMore(boolean isShowLoadMore) {
        this.isShowLoadMore = isShowLoadMore;
    }

    public Context getmContext() {
        return mContext;
    }

    public boolean isShowLoadMore() {
        return isShowLoadMore;
    }

}