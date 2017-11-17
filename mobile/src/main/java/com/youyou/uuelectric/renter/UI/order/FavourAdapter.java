package com.youyou.uuelectric.renter.UI.order;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.umeng.analytics.MobclickAgent;
import com.uu.facade.activity.pb.common.ActivityCommon;
import com.youyou.uuelectric.renter.R;
import com.youyou.uuelectric.renter.UI.web.H5Activity;
import com.youyou.uuelectric.renter.UI.web.H5Constant;
import com.youyou.uuelectric.renter.UI.web.url.URLConfig;
import com.youyou.uuelectric.renter.Utils.Support.Config;
import com.youyou.uuelectric.renter.Utils.UMCountConstant;
import com.youyou.uuelectric.renter.Utils.recycler.OnItemClickListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import me.grantland.widget.AutofitTextView;

/**
 * Created by liuchao on 2015/9/8.
 * 优惠券adapter
 */
public class FavourAdapter extends RecyclerView.Adapter<FavourAdapter.ViewHolder> implements OnItemClickListener {

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
    public int itemLayout = R.layout.activity_favour_item;
    /**
     * 第一项邀请好友布局
     */
    public int addFriendLayou = R.layout.addfriend_cupons;

    /**
     * 邀请入口文案展示
     */
    public String inviteFriendsDesc;

    /**
     * 数据源
     */

    public List<ActivityCommon.UserCouponInfo> datas = null;

    public FavourAdapter(Context context, List<ActivityCommon.UserCouponInfo> datas ) {
        mContext = context;
        this.datas = datas;

    }

    public void setDatas(List<ActivityCommon.UserCouponInfo> datas) {
        this.datas = datas;
    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(viewType, parent, false);
        return new ViewHolder(mContext, v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        int viewType = getItemViewType(position);


        if (viewType != itemLayout) {
            // 设置加载更多布局是否显示
            if (viewType == loadMoreLayout) {

                if (Config.isNetworkConnected(mContext)) {
                    holder.itemView.setVisibility(View.VISIBLE);
                } else {
                    holder.itemView.setVisibility(View.GONE);
                    Config.showFiledToast((Activity) mContext);
                }
            }else if(viewType == addFriendLayou){
              //更改要求好友入口文案
                inviteFriendsDesc = ((FavourActivity)mContext).inviteFriendsDesc;
                holder.favourinviteFriendsDesc.setText(inviteFriendsDesc);
                holder.itemView.setVisibility(View.VISIBLE);
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        FavourAdapter.this.onItemClick(0);

                    }
                });

            }
            return;
        }

        initViewBg(holder);

        if (datas != null && datas.size() >= position) {
            position = position -1;
            // 优惠券类型：0-普通金额优惠券；1-折扣优惠券
            if (datas.get(position).getCouponType() == 0) {
                holder.favourItemAccountRela.setVisibility(View.INVISIBLE);
                holder.favourItemPriceRela.setVisibility(View.VISIBLE);
                if (!TextUtils.isEmpty(datas.get(position).getCouponAmount())) {
                    String couponAmunt = datas.get(position).getCouponAmount();
                    // 优惠券金额为三位数时，更改字体大小
                    if (couponAmunt.length() >= 3) {
                        holder.favourItemPriceUnit.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
                        holder.favourItemPrice.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 30);
                    } else {
                        holder.favourItemPrice.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 36);
                        holder.favourItemPriceUnit.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
                    }
                    holder.favourItemPrice.setText(couponAmunt);
                } else {
                    holder.favourItemPrice.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 36);
                    holder.favourItemPriceUnit.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
                    holder.favourItemPrice.setText("");
                }

            } else if (datas.get(position).getCouponType() == 1) {
                holder.favourItemAccountRela.setVisibility(View.VISIBLE);
                holder.favourItemPriceRela.setVisibility(View.INVISIBLE);
                if (!TextUtils.isEmpty(datas.get(position).getCouponDiscount())) {
                    String discount = datas.get(position).getCouponDiscount();
                    if (discount.contains(".")) {
                        String[] discounts =discount.split("\\.");
                        holder.favourItemAccount.setText(discounts[0]);
                        holder.favourItemAccountUnit.setText("." + discounts[1]);
                    }
                    // 不包含小数点，为整数折
                    else {
                        if (discount.length() == 2) {
                            holder.favourItemAccount.setText(discount.substring(0, 1));
                            holder.favourItemAccountUnit.setText(discount.substring(1, 2));
                        } else {
                            holder.favourItemAccount.setText("");
                            holder.favourItemAccountUnit.setText("");
                        }
                    }
                } else {
                    holder.favourItemAccount.setText("");
                    holder.favourItemAccountUnit.setText("");
                }
            }

            holder.favourItemContentTitle.setText(datas.get(position).getCouponName().trim());
            holder.favourItemContentStart.setText(getMDFromInt(datas.get(position).getStartUseTime()));
            holder.favourItemContentEnd.setText(getMDFromInt(datas.get(position).getEndUseTime()));
            holder.favourItemContentDesc.setText(datas.get(position).getOrderUseMsg().trim());
            holder.favourItemButtomDesc.setText(datas.get(position).getCouponDes().trim());


        }
        // 根据当前订单的状态显示不同UI
        showStatus(holder, datas.get(position).getCouponState().getNumber());


        holder.itemView.setTag(datas.get(position));
    }

    @Override
    public int getItemCount() {
        if (datas != null && datas.size() > 0) {
            if (isShowLoadMore)
                return datas.size() + 2;
            else
                return datas.size() + 1;
        }
        return 0;
    }

    @Override
    public int getItemViewType(int position) {

        int viewType = itemLayout;
        if (position == 0) {
            viewType = addFriendLayou;
        }

        if (position == getItemCount() - 1 && isShowLoadMore) {
            viewType = loadMoreLayout;
        }

        return viewType;
    }

    /**
     *    优惠券邀请好友点击事件
     * @param position
     */
    @Override
    public void onItemClick(int position) {
        if(position == 0){
            Intent intent = new Intent(mContext, H5Activity.class);
            intent.putExtra(H5Constant.TITLE, URLConfig.getUrlInfo().getShare().getTitle());
            intent.putExtra(H5Constant.MURL, URLConfig.getUrlInfo().getShare().getUrl());
            mContext.startActivity(intent);
            MobclickAgent.onEvent(mContext, UMCountConstant.COUPON_LIST_INVITATION);

        }
    }


    class ViewHolder extends RecyclerView.ViewHolder  {

        public RelativeLayout favourItemRootRela;
        public RelativeLayout favourlayoutcallfriend;
        public View favourItemBgLeft;
        public View favourItemBgRight;

        public RelativeLayout favourItemPriceRela;// 价格布局文件
        public TextView favourItemPriceUnit;// 美元符
        public TextView favourItemPrice;// 价格

        // 折扣部分的UI布局
        public RelativeLayout favourItemAccountRela;// 折扣布局文件
        public TextView favourItemAccount;// 折扣
        public TextView favourItemAccountUnit;// 折扣小数点后面部分

        public AutofitTextView favourItemContentTitle;// 标题
        public TextView favourItemContentDate;// 有效期
        public TextView favourItemContentStart;// 有效期 开始时间
        public TextView favourItemContentLine;//
        public TextView favourItemContentEnd; // 有效期 结束时间

        public RelativeLayout favourItemContentStatusRela;// 说明布局
        public TextView favourItemContentDesc;// 说明
        public TextView favourItemButtomDesc;// 底部文案
        public TextView favourinviteFriendsDesc;//邀请好友文案

        public ViewHolder(final Context mContext, final View itemView) {
            super(itemView);
            favourlayoutcallfriend = (RelativeLayout) itemView.findViewById(R.id.layout_call_friend);
            favourItemRootRela = (RelativeLayout) itemView.findViewById(R.id.favour_item_root_rela);
            favourItemBgLeft = (View) itemView.findViewById(R.id.favour_item_bg_left);
            favourItemBgRight = (View) itemView.findViewById(R.id.favour_item_bg_right);

            favourItemPriceRela = (RelativeLayout) itemView.findViewById(R.id.favour_price_rela);
            favourItemPriceUnit = (TextView) itemView.findViewById(R.id.favour_item_price_unit);
            favourItemPrice = (TextView) itemView.findViewById(R.id.favour_item_price);

            // 折扣部分UI
            favourItemAccountRela = (RelativeLayout) itemView.findViewById(R.id.favour_account_rela);
            favourItemAccount = (TextView) itemView.findViewById(R.id.favour_item_account);
            favourItemAccountUnit = (TextView) itemView.findViewById(R.id.favour_item_account_unit);

            favourItemContentTitle = (AutofitTextView) itemView.findViewById(R.id.favour_item_content_title);
            favourItemContentDate = (TextView) itemView.findViewById(R.id.favour_item_content_date);
            favourItemContentStart = (TextView) itemView.findViewById(R.id.favour_item_content_start);
            favourItemContentLine = (TextView) itemView.findViewById(R.id.favour_item_content_line);
            favourItemContentEnd = (TextView) itemView.findViewById(R.id.favour_item_content_end);

            favourItemContentStatusRela = (RelativeLayout) itemView.findViewById(R.id.favour_item_content_status_rela);
            favourItemContentDesc = (TextView) itemView.findViewById(R.id.favour_item_content_desc);
            favourItemButtomDesc = (TextView) itemView.findViewById(R.id.favour_item_buttom_desc);

            //邀请好友文案
            favourinviteFriendsDesc = (TextView) itemView.findViewById(R.id.tv_addfriend_cuppons);
        }





    }

    /**
     * 初始化锯齿背景
     * @param holder
     */
    private void initViewBg(ViewHolder holder) {
        // 设置内容区域平铺的小圆角背景
        Bitmap topBitmap = BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.ic_border_cupons_left);
        BitmapDrawable leftDrawable = new BitmapDrawable(mContext.getResources(), topBitmap);
        leftDrawable.setTileModeY(Shader.TileMode.REPEAT);

        Bitmap bottomBitmap = BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.ic_border_cupons);
        BitmapDrawable rightDrawable = new BitmapDrawable(mContext.getResources(), bottomBitmap);
        rightDrawable.setTileModeY(Shader.TileMode.REPEAT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            holder.favourItemBgLeft.setBackground(leftDrawable);
            holder.favourItemBgRight.setBackground(rightDrawable);
        } else {
            holder.favourItemBgLeft.setBackgroundDrawable(leftDrawable);
            holder.favourItemBgRight.setBackgroundDrawable(rightDrawable);
        }

    }


    // ########################## get set ################################################


    /**
     * 3:当前订单可用：不置灰 显示中间那句话
     * 2:已过期：置灰，不显示中间那句话
     * 0:未使用：不置灰 不显示中间那句话
     *
     * @param holder
     * @param status
     */
    private void showStatus(ViewHolder holder, int status) {

        if (status == ActivityCommon.CouponState.IS_ORDER_USE_VALUE || status == ActivityCommon.CouponState.IS_NOT_USE_VALUE) {
            holder.favourItemPriceUnit.setTextColor(Color.parseColor("#fe9223"));
            holder.favourItemPrice.setTextColor(mContext.getResources().getColor(R.color.c8));
            holder.favourItemAccount.setTextColor(mContext.getResources().getColor(R.color.c8));
            holder.favourItemAccountUnit.setTextColor(Color.parseColor("#fe9223"));

            holder.favourItemContentTitle.setTextColor(mContext.getResources().getColor(R.color.c3));
            holder.favourItemContentDate.setTextColor(mContext.getResources().getColor(R.color.c3));
            holder.favourItemContentStart.setTextColor(mContext.getResources().getColor(R.color.c3));
            holder.favourItemContentLine.setTextColor(mContext.getResources().getColor(R.color.c3));
            holder.favourItemContentEnd.setTextColor(mContext.getResources().getColor(R.color.c3));
            holder.favourItemContentDesc.setTextColor(mContext.getResources().getColor(R.color.c1));
            holder.favourItemButtomDesc.setTextColor(mContext.getResources().getColor(R.color.c4));
        } else {
            holder.favourItemPriceUnit.setTextColor(mContext.getResources().getColor(R.color.c5));
            holder.favourItemPrice.setTextColor(mContext.getResources().getColor(R.color.c5));
            holder.favourItemAccount.setTextColor(mContext.getResources().getColor(R.color.c5));
            holder.favourItemAccountUnit.setTextColor(mContext.getResources().getColor(R.color.c5));

            holder.favourItemContentTitle.setTextColor(mContext.getResources().getColor(R.color.c5));
            holder.favourItemContentDate.setTextColor(mContext.getResources().getColor(R.color.c5));
            holder.favourItemContentStart.setTextColor(mContext.getResources().getColor(R.color.c5));
            holder.favourItemContentLine.setTextColor(mContext.getResources().getColor(R.color.c5));
            holder.favourItemContentEnd.setTextColor(mContext.getResources().getColor(R.color.c5));
            holder.favourItemContentDesc.setTextColor(mContext.getResources().getColor(R.color.c5));
            holder.favourItemButtomDesc.setTextColor(mContext.getResources().getColor(R.color.c5));
        }
        // 判断是否显示状态信息
        if (status == ActivityCommon.CouponState.IS_ORDER_USE_VALUE) {
            holder.favourItemContentStatusRela.setVisibility(View.VISIBLE);
            holder.favourItemContentDesc.setVisibility(View.VISIBLE);
        } else {
            holder.favourItemContentStatusRela.setVisibility(View.GONE);
            holder.favourItemContentDesc.setVisibility(View.INVISIBLE);
        }
    }


    // ########################## get set ################################################


    public void setIsShowLoadMore(boolean isShowLoadMore) {
        this.isShowLoadMore = isShowLoadMore;
    }

    public boolean isShowLoadMore() {
        return isShowLoadMore;
    }

    /**
     * 将int型时间数据转换为：月份-日期，格式字符串
     *
     * @param time
     * @return
     */
    public static final String getMDFromInt(int time) {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("MM月dd日");
        calendar.setTimeInMillis(((long) time) * 1000L);
        return sdf.format(calendar.getTime());
    }
}