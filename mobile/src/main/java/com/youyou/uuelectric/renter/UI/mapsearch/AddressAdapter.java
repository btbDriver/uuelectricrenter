package com.youyou.uuelectric.renter.UI.mapsearch;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.youyou.uuelectric.renter.R;
import com.youyou.uuelectric.renter.UI.main.rentcar.LocalPointItem;
import com.youyou.uuelectric.renter.Utils.recycler.OnItemClickListener;
import com.youyou.uuelectric.renter.Utils.view.RippleView;

import java.util.List;

/**
 * Created by Administrator on 2016/3/9.
 */
public class AddressAdapter extends RecyclerView.Adapter<AddressAdapter.SearchHolder> {

    OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    private List<LocalPointItem> poiItems;
    private Context context;

    public AddressAdapter(Context context , List<LocalPointItem> poiItems) {
        super();
        this.context = context;
        this.poiItems = poiItems;
    }
    @Override
    public SearchHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.search_address_list_item, parent, false);
        return new SearchHolder(itemView);
    }

    @Override
    public void onBindViewHolder(SearchHolder holder, final int position) {
        LocalPointItem poiItem = poiItems.get(position);
        holder.title.setText(poiItem.getAddress());
        holder.address.setText(poiItem.getSnippet());
        holder.remotePrice.setText("12.00元");
        holder.remotePrice.setVisibility(View.VISIBLE);

        holder.rvRoot.setOnRippleCompleteListener(new RippleView.OnRippleCompleteListener() {
            @Override
            public void onComplete(RippleView rippleView) {
                if (onItemClickListener != null) {
                    hideInput();
                    onItemClickListener.onItemClick(position);
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return poiItems.size();
    }

    /**
     * 隐藏软键盘
     */
    private void hideInput() {
        ((InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE))
                .hideSoftInputFromWindow(((Activity)context).getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    class SearchHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView address, remotePrice;
        RippleView rvRoot;

        public SearchHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.tv_title);
            address = (TextView) itemView.findViewById(R.id.tv_sub_title);
            rvRoot = (RippleView) itemView.findViewById(R.id.rv_root);
            remotePrice = (TextView) itemView.findViewById(R.id.tv_remote_price);
        }
    }

}