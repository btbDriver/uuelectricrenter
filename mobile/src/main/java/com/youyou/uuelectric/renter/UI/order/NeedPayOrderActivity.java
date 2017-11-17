package com.youyou.uuelectric.renter.UI.order;

import android.os.Bundle;

import com.youyou.uuelectric.renter.R;
import com.youyou.uuelectric.renter.UI.base.BaseActivity;
import com.youyou.uuelectric.renter.Utils.eventbus.BaseEvent;
import com.youyou.uuelectric.renter.Utils.eventbus.EventBusConstant;

import de.greenrobot.event.EventBus;

public class NeedPayOrderActivity extends BaseActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_need_pay_order);
        getSupportFragmentManager().beginTransaction().replace(R.id.fl_content_container, new NeedPayOrderFragment()).commit();
    }

}
