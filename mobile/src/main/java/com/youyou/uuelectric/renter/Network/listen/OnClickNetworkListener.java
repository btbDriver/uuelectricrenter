package com.youyou.uuelectric.renter.Network.listen;

import android.view.View;

import com.youyou.uuelectric.renter.R;
import com.youyou.uuelectric.renter.Utils.Support.Config;


/**
 * Created by taurusxi on 14-9-17.
 */
public abstract class OnClickNetworkListener extends BaseClickListener {

    @Override
    public void onClick(View v) {
        boolean isNetworkOk = Config.isNetworkConnected(v.getContext());

        if (isNetworkOk) {
            onNetworkClick(v);
        } else {
            Config.showToast(v.getContext(), v.getContext().getResources().getString(R.string.network_error_tip));
        }
    }

    public abstract void onNetworkClick(View v);
}
