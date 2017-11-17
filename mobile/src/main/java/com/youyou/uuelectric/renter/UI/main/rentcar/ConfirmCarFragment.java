package com.youyou.uuelectric.renter.UI.main.rentcar;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
import com.uu.facade.usecar.protobuf.iface.UsecarCommon;
import com.youyou.uuelectric.renter.R;
import com.youyou.uuelectric.renter.UI.base.BaseFragment;
import com.youyou.uuelectric.renter.UUApp;
import com.youyou.uuelectric.renter.Utils.Support.IntentConfig;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * 确认用车页面显示车辆信息的Fragment，供ViewPager加载显示
 */
public class ConfirmCarFragment extends BaseFragment {


    @InjectView(R.id.car_img)
    NetworkImageView mCarImg;
    @InjectView(R.id.car_name)
    TextView mCarName;
    @InjectView(R.id.car_number)
    TextView mCarNumber;
    @InjectView(R.id.car_mileage)
    TextView mCarMileage;
    @InjectView(R.id.price_mileage)
    TextView mPriceMileage;
    @InjectView(R.id.price_time)
    TextView mPriceTime;
    private UsecarCommon.CarBaseInfo carBaseInfo;

    public static ConfirmCarFragment getInstance() {
        return new ConfirmCarFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getArguments();
        if (bundle != null && bundle.getSerializable(IntentConfig.KEY_CARBASEINFO) != null)
            carBaseInfo = (UsecarCommon.CarBaseInfo) bundle.getSerializable(IntentConfig.KEY_CARBASEINFO);
    }

    @Override
    public View setView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_confirm_car, null);
        ButterKnife.inject(this, rootView);
        initView();

        return rootView;
    }

    private void initView() {
        if (carBaseInfo != null) {
            UUApp.getInstance().display(carBaseInfo.getCarImgUrl(), mCarImg, R.mipmap.ic_car_unload_details);
            mCarName.setText(carBaseInfo.getBrand() + carBaseInfo.getModel() + " ");
            mCarNumber.setText(carBaseInfo.getCarLicense());
            mCarMileage.setText(carBaseInfo.getEndurance() + "");
            mPriceMileage.setText("￥" + String.format("%.2f", carBaseInfo.getPricePerKm()));
            mPriceTime.setText("￥" + String.format("%.2f", carBaseInfo.getPricePerMinute()));
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }
}
