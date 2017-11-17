package com.youyou.uuelectric.renter.UI.main.rentcar;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.amap.api.services.core.PoiItem;
import com.amap.api.services.poisearch.PoiItemDetail;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.google.gson.Gson;
import com.youyou.uuelectric.renter.Network.user.SPConstant;
import com.youyou.uuelectric.renter.R;
import com.youyou.uuelectric.renter.UI.base.BaseActivity;
import com.youyou.uuelectric.renter.Utils.Support.Config;
import com.youyou.uuelectric.renter.Utils.Support.L;
import com.youyou.uuelectric.renter.Utils.recycler.OnItemClickListener;
import com.youyou.uuelectric.renter.Utils.view.RippleView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * POI地址搜索选择Activity
 */
public class  AddressSelectActivity extends BaseActivity implements PoiSearch.OnPoiSearchListener {

    @InjectView(R.id.et_address)
    EditText mEtAddress;
    @InjectView(R.id.recyclerView)
    RecyclerView recyclerView;
    @InjectView(R.id.rv_cancel)
    RippleView mRvCancel;
    @InjectView(R.id.iv_del_icon)
    ImageView mIvDelIcon;


    private String keyWord = "";// 要输入的poi搜索关键字
    private PoiResult poiResult; // poi返回的结果
    private int currentPage = 0;// 当前页面，从0开始计数
    private PoiSearch.Query query;// Poi查询条件类
    private PoiSearch poiSearch;// POI搜索
    /**
     * 搜索到的兴趣点集合
     */
    private List<LocalPointItem> poiItems = new ArrayList<>();
    private SearchAdapter adapter;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address_select);
        removeDefaultToolbar();
        ButterKnife.inject(this);

        sharedPreferences = getSharedPreferences(SPConstant.SPNAME_SEARCH_HISTORY, Context.MODE_PRIVATE);

        initView();

        readSearchHistory();
    }

    private void initView() {
        mEtAddress.addTextChangedListener(new TextWatcher() {
            String before;

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                before = mEtAddress.getText().toString();
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {

                String address = editable.toString().trim();
                if (address != null && address.length() > 0 && !address.equals(before)) {
                    keyWord = address;
                    doSearchQuery();
                }

                if (address != null && address.length() > 0) {
                    mIvDelIcon.setVisibility(View.VISIBLE);
                } else {
                    mIvDelIcon.setVisibility(View.INVISIBLE);
                    poiItems.clear();
                    if (localDatas.getDatas() != null) {

                        ArrayList<LocalPointItem> tempDatas = new ArrayList<LocalPointItem>();
                        for (int i = 0; i < localDatas.getDatas().size(); i++) {
                            tempDatas.add(localDatas.getDatas().get(i));
                        }
                        poiItems.addAll(tempDatas);
                    }
                    adapter.notifyDataSetChanged();
                }
            }
        });


        adapter = new SearchAdapter();
        adapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                LocalPointItem localPointItem = poiItems.get(position);

                saveSearchHistory(localPointItem);

                Intent intent = new Intent();
                intent.putExtra("localPointItem", localPointItem);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        mRvCancel.setOnRippleCompleteListener(new RippleView.OnRippleCompleteListener() {
            @Override
            public void onComplete(RippleView rippleView) {
                finish();
            }
        });

    }

    private List<LocalPointItem> localPointItems = new ArrayList<>();

    class LocalDatas {
        List<LocalPointItem> datas;

        public List<LocalPointItem> getDatas() {
            return datas;
        }

        public void setDatas(List<LocalPointItem> datas) {
            this.datas = datas;
        }
    }

    LocalDatas localDatas = new LocalDatas();

    /**
     * 保存搜索历史到本地
     */
    private void saveSearchHistory(LocalPointItem localPointItem) {
        Gson gson = new Gson();
        List<LocalPointItem> datas = localDatas.getDatas();
        boolean isSave = false;
        if (datas == null) {
            isSave = true;
            datas = new ArrayList<>();
        } else {
            if (!datas.contains(localPointItem)) {
                isSave = true;
            }
        }
        if (isSave) {
            datas.add(localPointItem);
            localDatas.setDatas(datas);
            String json = gson.toJson(localDatas);
            L.i("saveSearchHistory---" + json);
            sharedPreferences.edit().putString(SPConstant.SPKEY_HISTORY, json).commit();
        }
    }

    private void readSearchHistory() {
        if (sharedPreferences.contains(SPConstant.SPKEY_HISTORY)) {
            String json = sharedPreferences.getString(SPConstant.SPKEY_HISTORY, "");
            L.i("readSearchHistory----" + json);
            Gson gson = new Gson();
            localDatas = gson.fromJson(json, LocalDatas.class);
            if (localDatas != null && localDatas.getDatas() != null) {
                ArrayList<LocalPointItem> tempDatas = new ArrayList<LocalPointItem>();
                for (int i = 0; i < localDatas.getDatas().size(); i++) {
                    tempDatas.add(localDatas.getDatas().get(i));
                }
                Collections.reverse(tempDatas);
                poiItems.addAll(tempDatas);
                adapter.notifyDataSetChanged();
            }
        }
    }

    /**
     * 执行POI查询
     */
    private void doSearchQuery() {
        if (!Config.isNetworkConnected(mContext)) {
            showDefaultNetworkSnackBar();
            return;
        }
        currentPage = 0;
        query = new PoiSearch.Query(keyWord, "", "北京");
        query.setPageSize(20);// 设置每页最多返回多少条poiItem
        query.setPageNum(currentPage);// 设置查第一页

        poiSearch = new PoiSearch(this, query);
        poiSearch.setOnPoiSearchListener(this);
        poiSearch.searchPOIAsyn();

    }


    @Override
    public void onPoiSearched(PoiResult result, int rCode) {
        if (rCode == 0) {
            if (result != null && result.getQuery() != null) {
                if (result.getQuery().equals(query)) {// 是否是同一条
                    poiResult = result;
                    ArrayList<PoiItem> pois = poiResult.getPois();
                    poiItems.clear();
                    if (pois != null && pois.size() > 0) {
                        for (PoiItem poiItem : pois) {
                            LocalPointItem localPointItem = new LocalPointItem(poiItem.getTitle(),
                                    poiItem.getSnippet(),
                                    poiItem.getLatLonPoint().getLatitude(),
                                    poiItem.getLatLonPoint().getLongitude());
                            poiItems.add(localPointItem);
                        }
                    } else {
                        showSnackBarMsg("对不起，没有搜索到相关数据!");
                    }
                    adapter.notifyDataSetChanged();
                }
            }
        } else {
            showSnackBarMsg("对不起，没有搜索到相关数据!");
        }
    }

    @Override
    public void onPoiItemDetailSearched(PoiItemDetail poiItemDetail, int i) {

    }

    private class SearchHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView address;
        RippleView rvRoot;

        public SearchHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.tv_title);
            address = (TextView) itemView.findViewById(R.id.tv_sub_title);
            rvRoot = (RippleView) itemView.findViewById(R.id.rv_root);
        }
    }

    /**
     * 隐藏软键盘
     */
    private void hideInput() {
        ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
                .hideSoftInputFromWindow(AddressSelectActivity.this.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    /**
     * 输入框中的X号点击逻辑
     */
    @OnClick(R.id.iv_del_icon)
    public void delIconClick() {
        mEtAddress.setText("");
        poiItems.clear();
        if (localDatas.getDatas() != null) {
            poiItems.addAll(localDatas.getDatas());
        }
        adapter.notifyDataSetChanged();
    }

    /**
     * 展示搜索到的列表数据Adapter
     */
    private class SearchAdapter extends RecyclerView.Adapter<SearchHolder> {

        OnItemClickListener onItemClickListener;

        public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
            this.onItemClickListener = onItemClickListener;
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
    }

}
