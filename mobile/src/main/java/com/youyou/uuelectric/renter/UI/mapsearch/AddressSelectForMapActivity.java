package com.youyou.uuelectric.renter.UI.mapsearch;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.amap.api.maps.model.LatLng;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.poisearch.PoiItemDetail;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.google.gson.Gson;
import com.youyou.uuelectric.renter.Network.user.SPConstant;
import com.youyou.uuelectric.renter.R;
import com.youyou.uuelectric.renter.UI.base.BaseActivity;
import com.youyou.uuelectric.renter.UI.main.rentcar.LocalPointItem;
import com.youyou.uuelectric.renter.UI.nearstation.NearDotMode;
import com.youyou.uuelectric.renter.Utils.Support.Config;
import com.youyou.uuelectric.renter.Utils.Support.IntentConfig;
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
public class AddressSelectForMapActivity extends BaseActivity implements PoiSearch.OnPoiSearchListener {

    @InjectView(R.id.et_address)
    EditText mEtAddress;

    @InjectView(R.id.recyclerView)
    RecyclerView recyclerView;
    @InjectView(R.id.rv_cancel)
    RippleView mRvCancel;
    @InjectView(R.id.iv_del_icon)
    ImageView mIvDelIcon;
    @InjectView(R.id.ll_always_near_address)
    LinearLayout llAlwaysNearAddress;
    @InjectView(R.id.viewpager)
    ViewPager viewPager;
    @InjectView(R.id.btn_always_dot)
    android.widget.Button btnAlwaysDot;
    @InjectView(R.id.btn_near_dot)
    android.widget.Button btnNearDot;
    @InjectView(R.id.cursor)
    ImageView ivCursor;


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

    private boolean isEnd;
    private int item_width = 0;// 动画图片偏移量
    private int beginPosition, endPosition, currentFragmentIndex;
    private MyFragmentPagerAdapter fragmentPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address_select_for_map);
        removeDefaultToolbar();
        ButterKnife.inject(this);

        sharedPreferences = getSharedPreferences(SPConstant.SPNAME_SEARCH_HISTORY, Context.MODE_PRIVATE);

        initView();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (getIntent().getIntExtra(IntentConfig.GET_DOT_TYPE, 1) == 1) {
                    viewPager.setCurrentItem(0);
                } else {
                    viewPager.setCurrentItem(1);
                }
            }
        }, 200);


//        readSearchHistory();
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
                    llAlwaysNearAddress.setVisibility(View.GONE);
                    poiItems.clear();
                    adapter.notifyDataSetChanged();
                    recyclerView.setVisibility(View.VISIBLE);
                } else {
                    llAlwaysNearAddress.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);

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


        //高德地图检索地点的adapter
        adapter = new SearchAdapter();
        adapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                LocalPointItem localPointItem = poiItems.get(position);

//                saveSearchHistory(localPointItem);
                Intent intent = new Intent(AddressSelectForMapActivity.this, MapSearchActivity.class);
                intent.putExtra("localPointItem", localPointItem);
                intent.putExtra(IntentConfig.DOT_ID, getIntent().getStringExtra(IntentConfig.DOT_ID));
                startActivityForResult(intent, 1);

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


        ArrayList<Fragment> fragments = new ArrayList<Fragment>();
        AddressSelectFragment alwaysFragment = new AddressSelectFragment();
        AddressSelectFragment nearFragment = new AddressSelectFragment();

        Bundle bundle1 = new Bundle();
        bundle1.putString(IntentConfig.DOT_AWLAYS_NEAR_RECORD_TYPE, IntentConfig.DOT_AWLAYS);
        if (getIntent().getIntExtra(IntentConfig.GET_DOT_TYPE, 0) == 1) {

            //测试数据 真实数据有上一个页面带过来
            NearDotMode dotMode = new NearDotMode();
            dotMode.setDotName(getIntent().getStringExtra(IntentConfig.KEY_DOTNAME));
            dotMode.setDotDesc(getIntent().getStringExtra(IntentConfig.KEY_DOTNAME));
            dotMode.setDotLon(getIntent().getDoubleExtra(IntentConfig.KEY_DOTLON, 0.0));
            dotMode.setDotLat(getIntent().getDoubleExtra(IntentConfig.KEY_DOTLAT, 0.0));
            dotMode.setDotId(getIntent().getStringExtra(IntentConfig.DOT_ID));

            bundle1.putParcelable(IntentConfig.DOT_A2A, dotMode);
        } else {

        }
        alwaysFragment.setArguments(bundle1);

        Bundle bundle2 = new Bundle();
        bundle2.putString(IntentConfig.DOT_AWLAYS_NEAR_RECORD_TYPE, IntentConfig.DOT_NEAR);
        bundle2.putString(IntentConfig.DOT_ID, getIntent().getStringExtra(IntentConfig.DOT_ID));
        nearFragment.setArguments(bundle2);


        fragments.add(alwaysFragment);
        fragments.add(nearFragment);
        fragmentPagerAdapter = new MyFragmentPagerAdapter(getSupportFragmentManager(), fragments);
        viewPager.setOffscreenPageLimit(1);
        viewPager.setAdapter(fragmentPagerAdapter);

        viewPager.setOnPageChangeListener(new MyOnPageChangeListener());


        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int screenW = displayMetrics.widthPixels;
        item_width = screenW / 2;
        Matrix matrix = new Matrix();
        matrix.postTranslate(screenW, 0);
        ivCursor.setImageMatrix(matrix);

    }

    private class MyFragmentPagerAdapter extends FragmentPagerAdapter {
        private ArrayList<Fragment> fragments;

        public MyFragmentPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        public MyFragmentPagerAdapter(FragmentManager fm, ArrayList<Fragment> fragments) {
            super(fm);
            this.fragments = fragments;
        }

        @Override
        public int getCount() { // getCount判空
            if (null != fragments) {
                return fragments.size();
            }
            return 0;
        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        @Override
        public Object instantiateItem(ViewGroup container, final int position) {
            Object obj = super.instantiateItem(container, position);
            return obj;
        }

    }

    public class MyOnPageChangeListener implements ViewPager.OnPageChangeListener {

        @Override
        public void onPageSelected(final int position) {
            Animation animation = new TranslateAnimation(endPosition, position * item_width, 0, 0);
            beginPosition = position * item_width;
            currentFragmentIndex = position;

            if (animation != null) {
                animation.setFillAfter(true);
                animation.setDuration(100);
                ivCursor.startAnimation(animation);
            }
            endPosition = beginPosition;
            if (0 == position) {
                btnAlwaysDot.setTextColor(getResources().getColor(R.color.c3));
                btnNearDot.setTextColor(getResources().getColor(R.color.c5));
            } else {
                btnNearDot.setTextColor(getResources().getColor(R.color.c3));
                btnAlwaysDot.setTextColor(getResources().getColor(R.color.c5));
            }
            AddressSelectFragment fragment = (AddressSelectFragment) fragmentPagerAdapter.getItem(position);
            if (position == 1) {
                fragment.locationAndGetNearA2BDot();
            }
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            if (!isEnd) {
                if (currentFragmentIndex == position) {
                    endPosition = item_width * currentFragmentIndex + (int) (item_width * positionOffset);
                }
                if (currentFragmentIndex == position + 1) {
                    endPosition = item_width * currentFragmentIndex - (int) (item_width * (1 - positionOffset));
                }

                Animation mAnimation = new TranslateAnimation(beginPosition, endPosition, 0, 0);
                mAnimation.setFillAfter(true);
                mAnimation.setDuration(100);
                ivCursor.startAnimation(mAnimation);
                beginPosition = endPosition;
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            if (state == ViewPager.SCROLL_STATE_DRAGGING) {
                isEnd = false;
            } else if (state == ViewPager.SCROLL_STATE_SETTLING) {
                isEnd = true;
                beginPosition = currentFragmentIndex * item_width;
                if (viewPager.getCurrentItem() == currentFragmentIndex) {
                    // 未跳入下一个页面
                    ivCursor.clearAnimation();
                    Animation animation = null;
                    // 恢复位置
                    animation = new TranslateAnimation(endPosition, currentFragmentIndex * item_width, 0, 0);
                    animation.setFillAfter(true);
                    animation.setDuration(100);
                    ivCursor.startAnimation(animation);
                    endPosition = currentFragmentIndex * item_width;
                }
            }
        }

    }

    @OnClick(R.id.btn_always_dot)
    public void btnAlwaysDot() {
        viewPager.setCurrentItem(0);

    }

    @OnClick(R.id.btn_near_dot)
    public void btnNearDot() {
        viewPager.setCurrentItem(1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == 1) {
                if (data != null) {
                    setResult(RESULT_OK, data);
                    LatLng latLng = data.getParcelableExtra("addressLatLng");
                    showToast("经纬度：" + latLng.latitude + " " + latLng.longitude);
                    finish();
                }
            }
        }

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

    @Override
    protected void onRestart() {
        super.onRestart();
        showInput();
    }

    /**
     * 隐藏软键盘
     */
    private void hideInput() {
        ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
                .hideSoftInputFromWindow(AddressSelectForMapActivity.this.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    /**
     * 显示软键盘
     */
    private void showInput() {
//        ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
//                .showSoftInput(mEtAddress, InputMethodManager.SHOW_IMPLICIT);
//        ((InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).showSoftInput(mEtAddress, 0);

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
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
