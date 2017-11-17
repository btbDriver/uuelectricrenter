package com.youyou.uuelectric.renter.UI.license;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;

import com.youyou.uuelectric.renter.R;
import com.youyou.uuelectric.renter.UI.base.BaseActivity;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * Created by chudaijiang on 2016/5/16.
 */
public class PhotoActivity extends BaseActivity {

    @InjectView(R.id.iv_license_photo)
    ImageView ivLicensePhoto;
    @InjectView(R.id.bt_take_photo_again)
    Button btTakePhotoAgain;
    @InjectView(R.id.bt_use_photo)
    Button btUsePhoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_license_photo);
        removeDefaultToolbar();

        ButterKnife.inject(this);

        initView();

    }

    private void initView() {
        ivLicensePhoto.setImageURI(this.getIntent().getData());
    }

    @OnClick(R.id.bt_take_photo_again)
    public void a(){
        finish();
    }

    @OnClick(R.id.bt_use_photo)
    public void b() {
        setResult(RESULT_OK);
        finish();
    }
}
