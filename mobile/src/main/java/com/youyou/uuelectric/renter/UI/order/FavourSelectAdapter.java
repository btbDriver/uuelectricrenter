package com.youyou.uuelectric.renter.UI.order;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
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
import android.widget.Button;
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
import com.youyou.uuelectric.renter.Utils.view.RippleView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import me.grantland.widget.AutofitTextView;

/**
 * Created by liuchao on 2015/9/8.
 * 选择优惠券adapter
 */
public class FavourSelectAdapter extends RecyclerView.Adapter<FavourSelectAdapter.ViewHolder> implements OnItemClickListener {
    /**
    * 全局上下文
    */
    public Activity mContext;
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
    public int itemLayout = R.layout.activity_favour_item_select;
    /**
     * 数据源
     */
    public List<FavourInfo> datas = null;
    /**
     * 点击每一项
     */
    public OnRadioClickListener onRadioClickListener = null;

    /**
     * 选中的优惠券ID
     */
    public int favourId;

    /**
     * 邀请入口文案展示
     */
    public String inviteFriendsDesc;

    public FavourSelectAdapter(Activity context, List<FavourInfo> datas, OnRadioClickListener onRadioClickListener) {
        mContext = context;
        this.datas = datas;
        this.onRadioClickListener = onRadioClickListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(viewType, parent, false);
        return new ViewHolder(mContext, v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder,  int position) {
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
            }
            return;
        }

        initViewBg(holder);

        if (datas != null && datas.size() >= position) {
            // 优惠券类型：0-普通金额优惠券；1-折扣优惠券
            if (datas.get(position).getCouponType() == 0) {
                holder.favourItemAccountRela.setVisibility(View.INVISIBLE);
                holder.favourItemPriceRela.setVisibility(View.VISIBLE);

                if (!TextUtils.isEmpty(datas.get(position).getCouponAmount())) {
                    String couponAmunt = datas.get(position).getCouponAmount();
                    // 优惠券金额为三位数时，更改字体大小
                    if (couponAmunt.length() >= 3) {
                        holder.favourItemPriceUnit.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
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
            holder.favourItemContentTitle.setText(datas.get(position).getCouponName());
            holder.favourItemContentStart.setText(getMDFromInt(datas.get(position).getStartUseTime()));
            holder.favourItemContentEnd.setText(getMDFromInt(datas.get(position).getEndUseTime()));
            holder.favourItemContentDesc.setText(datas.get(position).getOrderUseMsg());
            holder.favourItemButtomDesc.setText(datas.get(position).getCouponDes());
            if (datas.get(position).isSelected()) {
                // 执行选中动画
                selectAnimal(holder.favourItemRadiobutton, R.mipmap.ic_checkbox_coupon2);
            } else {
                holder.favourItemRadiobutton.setBackgroundResource(R.mipmap.ic_checkbox_coupon1);
            }
        }
        // 根据当前订单的状态显示不同UI
        showStatus(holder, datas.get(position).getCouponState().getNumber());

        final int cposition = position;
        // 添加水波纹效果
        holder.favourItemRootRela.setOnRippleCompleteListener(new RippleView.OnRippleCompleteListener() {
            @Override
            public void onComplete(RippleView rippleView) {
                itemClick(holder, cposition);
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

    @Override
    public void onItemClick(int position) {
        Intent intent = new Intent(mContext, H5Activity.class);
        intent.putExtra(H5Constant.TITLE, URLConfig.getUrlInfo().getShare().getTitle());
        intent.putExtra(H5Constant.MURL, URLConfig.getUrlInfo().getShare().getUrl());
        mContext.startActivity(intent);
        MobclickAgent.onEvent(mContext, UMCountConstant.COUPON_LIST_INVITATION);
    }


    class ViewHolder extends RecyclerView.ViewHolder {
        public RippleView favourItemRootRela;
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
        public Button favourItemRadiobutton;// 是否选中

        public ViewHolder(final Context mContext, final View itemView) {
            super(itemView);
            favourItemRootRela = (RippleView) itemView.findViewById(R.id.favour_item_root_rela);
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
            favourItemContentDesc = (TextView) itemView.findViewById(R.id.favour_item_content_desc);

            favourItemContentStatusRela = (RelativeLayout) itemView.findViewById(R.id.favour_item_content_status_rela);
            favourItemButtomDesc = (TextView) itemView.findViewById(R.id.favour_item_buttom_desc);
            favourItemRadiobutton = (Button) itemView.findViewById(R.id.favour_item_radiobutton);
        }
    }


    // ########################## get set ################################################

    interface OnRadioClickListener {
        public abstract void onItemClick(FavourInfo favourInfo);
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

    /**
     * 点击处理事件
     * @param holder
     * @param position
     */
    public void itemClick(FavourSelectAdapter.ViewHolder holder, int position) {
        if (datas.get(position).isSelected()) {
            datas.get(position).setIsSelected(false);
            // 执行选中动画
            selectAnimal(holder.favourItemRadiobutton, R.mipmap.ic_checkbox_coupon1);
            onRadioClickListener.onItemClick(null);
        } else {
            // 执行选中动画
            // selectAnimal(holder.favourItemRadiobutton, R.mipmap.ic_checkbox_coupon2);
            // 遍历data数据
            if (datas != null && datas.size() > 0) {
                for (int i = 0; i < datas.size(); i++) {
                    datas.get(i).setIsSelected(false);
                }
                datas.get(position).setIsSelected(true);
                FavourSelectAdapter.this.notifyDataSetChanged();
                notifyItemRangeChanged(0, datas.size());
            }

            onRadioClickListener.onItemClick(datas.get(position));
        }
    }


    /**
     * 3:当前订单可用：不置灰 显示中间那句话
     * 2:已过期：置灰，不显示中间那句话
     * 0:未使用：不置灰 不显示中间那句话
     *
     * @param holder
     * @param status
     */
    public void showStatus(ViewHolder holder, int status) {

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

    /**
     * 执行选中动画
     * @param animalView
     */
    private void selectAnimal(final View animalView, final int resourceId) {
        ObjectAnimator objectAnimatorQ1 = ObjectAnimator.ofFloat(animalView, "alpha", 1.0f, 0.0f);
        ObjectAnimator objectAnimatorQ2 = ObjectAnimator.ofFloat(animalView, "scaleX", 1.0f, 0.0f);
        ObjectAnimator objectAnimatorQ3 = ObjectAnimator.ofFloat(animalView, "scaleY", 1.0f, 0.0f);

        final ObjectAnimator objectAnimatorH1 = ObjectAnimator.ofFloat(animalView, "alpha", 0.0f, 1.0f);
        final ObjectAnimator objectAnimatorH2 = ObjectAnimator.ofFloat(animalView, "scaleX", 0.0f, 1.0f);
        final ObjectAnimator objectAnimatorH3 = ObjectAnimator.ofFloat(animalView, "scaleY", 0.0f, 1.0f);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(objectAnimatorQ1).with(objectAnimatorQ2).with(objectAnimatorQ3);
        animatorSet.setDuration(200);
        animatorSet.start();
        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }
            @Override
            public void onAnimationEnd(Animator animation) {
                animalView.setBackgroundResource(resourceId);
                AnimatorSet animatorSet = new AnimatorSet();
                animatorSet.play(objectAnimatorH1).with(objectAnimatorH2).with(objectAnimatorH3);
                animatorSet.setDuration(200);
                animatorSet.start();
            }
            @Override
            public void onAnimationCancel(Animator animation) {
            }
            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
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