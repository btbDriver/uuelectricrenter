package com.youyou.uuelectric.renter.UI.license;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.rey.material.widget.Button;
import com.rey.material.widget.EditText;
import com.uu.facade.auth.pb.iface.UserAuthInterface;
import com.uu.facade.base.cmd.Cmd;
import com.uu.facade.user.protobuf.bean.UserInterface;
import com.youyou.uuelectric.renter.Network.HttpResponse;
import com.youyou.uuelectric.renter.Network.NetworkTask;
import com.youyou.uuelectric.renter.Network.NetworkUtils;
import com.youyou.uuelectric.renter.Network.UUParams;
import com.youyou.uuelectric.renter.Network.UUResponseData;
import com.youyou.uuelectric.renter.Network.listen.OnClickFastListener;
import com.youyou.uuelectric.renter.Network.listen.OnClickNormalListener;
import com.youyou.uuelectric.renter.Network.listen.TextWatcherAdapter;
import com.youyou.uuelectric.renter.Network.user.UserConfig;
import com.youyou.uuelectric.renter.R;
import com.youyou.uuelectric.renter.UI.base.BaseActivity;
import com.youyou.uuelectric.renter.UI.main.MainActivity;
import com.youyou.uuelectric.renter.Utils.BitmapUtils;
import com.youyou.uuelectric.renter.Utils.RegexUtils;
import com.youyou.uuelectric.renter.Utils.Support.Config;
import com.youyou.uuelectric.renter.Utils.Support.L;
import com.youyou.uuelectric.renter.Utils.Support.SysConfig;
import com.youyou.uuelectric.renter.Utils.animation.AnimationUtils;
import com.youyou.uuelectric.renter.Utils.eventbus.BaseEvent;
import com.youyou.uuelectric.renter.Utils.eventbus.EventBusConstant;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;

/**
 * Created by liuchao on 2015/9/6.
 * 驾驶证认证
 */
public class ValidateLicenseActivity extends BaseActivity {
    /**
     * 提交按钮
     */
    @InjectView(R.id.b3_button)
    public Button validateLicenseButton = null;
    /**
     * 驾驶证号输入框
     */
    @InjectView(R.id.validate_license_edit)
    public EditText validateLicenseEdit = null;
    /**
     * 档案编号输入框
     */
    @InjectView(R.id.validate_record_edit)
    public EditText validateRecordEdit = null;
    @InjectView(R.id.expand_license_root)
    LinearLayout expandLincenseRoot;
    /**
     * 驾驶证说明（驳回原因）
     */
    @InjectView(R.id.validate_license_backdesc)
    public TextView validateLicenseBackdesc = null;
    @InjectView(R.id.validate_license_backdesc_root)
    RelativeLayout validate_license_backdesc_root;
    @InjectView(R.id.license_img)
    ImageView licenseImg;
    @InjectView(R.id.icon)
    ImageView icon;
    @InjectView(R.id.error_icon)
    ImageView errorIcon;
    @InjectView(R.id.ic_notice_license)
    ImageView icNoticeLicense;
    @InjectView(R.id.arrow)
    ImageView arrow;
    @InjectView(R.id.validate_license_rela1)
    RelativeLayout validateLicenseRela1;
    @InjectView(R.id.validate_license_rela2)
    RelativeLayout validateLicenseRela2;
    @InjectView(R.id.scroll_view)
    ScrollView mScrollView;
    @InjectView(R.id.arrow)
    ImageView mClick;
    @InjectView(R.id.license_img_root)
    RelativeLayout licenseImgRoot;

    @InjectView(R.id.tv_license_tip)
    TextView tv_license_tip;
    @InjectView(R.id.validate_license_desc)
    TextView validateLicenseDesc;
    public static int TAKE_PHOTO_CODE = 998;

    @OnClick(R.id.license_img_root)
    public void imgClick() {
//        getImageFromCamera();
//        startActivity(new Intent(mContext, CameraActivity.class));
        startActivityForResult(new Intent(mContext, CameraActivity.class), TAKE_PHOTO_CODE);
    }

    @InjectView(R.id.expand_license)
    LinearLayout expand_license;

    @OnClick(R.id.expand_license)
    public void expandLicenseClick() {
        if (isShow) {

            AnimationUtils.rotateIndicatingArrow180(mClick, false);
            validateLicenseRela1.setVisibility(View.GONE);
            validateLicenseRela2.setVisibility(View.GONE);
            validateLicenseEdit.setEnabled(false);
            validateRecordEdit.setEnabled(false);
            validateLicenseDesc.setVisibility(View.GONE);
        } else {
            AnimationUtils.rotateIndicatingArrow180(mClick, true);
            validateLicenseRela1.setVisibility(View.VISIBLE);
            validateLicenseRela2.setVisibility(View.VISIBLE);
            validateLicenseEdit.setEnabled(true);
            validateRecordEdit.setEnabled(true);
            validateLicenseDesc.setVisibility(View.VISIBLE);
            mScrollView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
                }
            },300);
        }
        isShow = !isShow;
    }

    @InjectView(R.id.validate_license_rela1_delete)
    ImageView license_rela1_delete;
    @InjectView(R.id.validate_license_rela2_delete)
    ImageView license_rela2_delete;
    boolean isShow = false;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_validate_license);
        TAG = "Camera";
        sp = getSharedPreferences(TAG, Context.MODE_PRIVATE);
        ButterKnife.inject(this);
        initView();
        if (sp.getBoolean(KEY_ACTIVITY_IS_DESTORY, false)) {
            if (b == null) {
                b = new Bundle();
            }
            b.putString(KEY_BIGPICPATH, sp.getString(KEY_BIGPICPATH, ""));
            b.putInt(KEY_PHOTOHEIGHT, sp.getInt(KEY_PHOTOHEIGHT, 0));
        }
        /**
         * 恢复Activity被销毁时保存的必要的字段值
         */
        if (b != null) {
            L.d("----------onCreate----------" + b);
            bigPicPath = b.getString(KEY_BIGPICPATH);
            photoHeight = b.getInt(KEY_PHOTOHEIGHT);
            L.d("从onSaveInstanceState方法中恢复图片路径" + bigPicPath);
            //startTackPhoto();
            getPhoto();
            clearSpRecord();
        }
        // 请求服务器获取驾驶证驳回状态
        requestValidateDesc();
    }


    /**
     * 初始化组件
     */
    public void initView() {

        validate_license_backdesc_root.setVisibility(View.GONE);
        tv_license_tip.setText(R.string.upload_license_tip1);
        validateLicenseButton.setText("提交");
        if (!sp.getString(UserConfig.getUserInfo().getPhone() + "pic_path", "").equals("") && !sp.getString(UserConfig.getUserInfo().getPhone() + "imgSessionKey", "").equals("")) {
            //如果拍照距当前大于1小时就不显示图片了
            if (System.currentTimeMillis() - sp.getLong("time", 0) < 1000 * 60 * 60) {
                picPath = sp.getString(UserConfig.getUserInfo().getPhone() + "pic_path", "");
                bitmap = BitmapUtils.getInSampleBitmap(picPath, photoWidth, photoHeight);
                licenseImg.setImageBitmap(bitmap);
                imgSessionKey = sp.getString(UserConfig.getUserInfo().getPhone() + "imgSessionKey", "");
                expandLincenseRoot.setVisibility(View.VISIBLE);
                isImageSuccess = true;
            }
        } else {
            isImageSuccess = false;
            expandLincenseRoot.setVisibility(View.GONE);
        }
        // 初始化设置提交按钮不可用
        Config.setB3ViewEnable(validateLicenseButton, false);
        // 添加输入监听
        validateLicenseEdit.addTextChangedListener(new TextWatcherAdapter() {
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                setButtonEnabled();
                if (!validateLicenseEdit.getText().toString().isEmpty())
                    license_rela1_delete.setVisibility(View.VISIBLE);
                else
                    license_rela1_delete.setVisibility(View.GONE);

            }
        });
        // 添加输入监听
        validateRecordEdit.addTextChangedListener(new TextWatcherAdapter() {
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                setButtonEnabled();
                if (!validateRecordEdit.getText().toString().isEmpty())
                    license_rela2_delete.setVisibility(View.VISIBLE);
                else
                    license_rela2_delete.setVisibility(View.GONE);
            }
        });
        validateLicenseEdit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    if (!validateLicenseEdit.getText().toString().isEmpty())
                        license_rela1_delete.setVisibility(View.VISIBLE);
                } else {
                    license_rela1_delete.setVisibility(View.GONE);
                }
            }
        });
        validateRecordEdit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    if (!validateRecordEdit.getText().toString().isEmpty())
                        license_rela2_delete.setVisibility(View.VISIBLE);
                } else {
                    license_rela2_delete.setVisibility(View.GONE);
                }
            }
        });

        /**
         * 清空按钮点击事件
         */
        license_rela1_delete.setOnClickListener(new OnClickNormalListener() {
            @Override
            public void onNormalClick(View v) {
                validateLicenseEdit.setText("");
            }
        });
        license_rela2_delete.setOnClickListener(new OnClickNormalListener() {
            @Override
            public void onNormalClick(View v) {
                validateRecordEdit.setText("");
            }
        });
        /**
         * 点击测试按钮
         */
        validateLicenseButton.setOnClickListener(new OnClickFastListener() {
            @Override
            public void onFastClick(View v) {
                if (!Config.isNetworkConnected(mContext)) {
                    showDefaultNetworkSnackBar();
                    return;
                }
                String license = validateLicenseEdit.getText().toString();
                String record = validateRecordEdit.getText().toString();
                if (!license.isEmpty() || !record.isEmpty()) {
                    if (!RegexUtils.regexLicense(license) || !RegexUtils.regexRecord(record)) {
                        showToast("请填写完整的驾驶证号和档案编号");
                        return;
                    }
                }

                requestValidateLicense(license, record);
            }
        });
        setButtonEnabled();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private String picPath;
    private String bigPicPath;
    private SharedPreferences sp;

    // 拍照时，当前Activity是否被系统回收
    public static String KEY_ACTIVITY_IS_DESTORY = "activity_is_destory";
    private static final String KEY_BIGPICPATH = "bigPicPath";
    private static final String KEY_PHOTOHEIGHT = "photoHeight";


    /**
     * 使用拍照获取图片
     */
    protected void getImageFromCamera() {
        String state = Environment.getExternalStorageState();
        if (state.equals(Environment.MEDIA_MOUNTED)) {
            bigPicPath = SysConfig.SD_IMAGE_PATH + getPhotoBigFileName();

            SharedPreferences.Editor edit = sp.edit();
            edit.putBoolean(KEY_ACTIVITY_IS_DESTORY, true);
            edit.putString(KEY_BIGPICPATH, bigPicPath);
//            edit.putInt(KEY_PHOTOHEIGHT, photoHeight);
            edit.commit();

            // 部分机型更改了action的值，这里尽量使用系统定义的常量
            Intent getImageByCamera = new Intent(/*"android.media.action.IMAGE_CAPTURE"*/MediaStore.ACTION_IMAGE_CAPTURE);
            L.d("---------------getImageFromCamera:" + bigPicPath + "------------------");
            getImageByCamera.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(bigPicPath)));
            // 添加隐士intent跳转的判断，若没有对应的activity，则不做处理
            if (getImageByCamera.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(getImageByCamera, 1);
            }
        } else {
            Toast.makeText(getApplicationContext(), "请确认已经插入SD卡", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * 获取大尺寸图片名称
     *
     * @return
     */
    private String getPhotoBigFileName() {
        StringBuilder fileName = new StringBuilder();
        long time = System.currentTimeMillis();

        String phone = UserConfig.getUserInfo().getPhone();
        try {
            phone = phone.replace("*", "1");

        } catch (Exception e) {
            phone = "";
        }
        fileName.append(phone + "bigvalidate" + time + ".jpg");
        return fileName.toString();
    }

    /**
     * 默认请求服务器，获取驾驶证驳回状态
     */
    public void requestValidateDesc() {
        if (!Config.isNetworkConnected(mContext)) {
            mProgressLayout.showError(onErrorClickNetworkListener);
            return;
        }
        mProgressLayout.showLoading();
        UserInterface.UserVerifyInfo.Request.Builder request = UserInterface.UserVerifyInfo.Request.newBuilder();
        request.setR((int) (System.currentTimeMillis() / 1000));
        NetworkTask task = new NetworkTask(Cmd.CmdCode.UserVerifyInfo_NL_VALUE);
        task.setBusiData(request.build().toByteArray());
        NetworkUtils.executeNetwork(task, new HttpResponse.NetWorkResponse<UUResponseData>() {
            @Override
            public void onSuccessResponse(UUResponseData responseData) {
                if (responseData.getRet() == 0) {
                    try {
                        showResponseCommonMsg(responseData.getResponseCommonMsg());
                        UserInterface.UserVerifyInfo.Response response = UserInterface.UserVerifyInfo.Response.parseFrom(responseData.getBusiData());
                        if (response.getRet() == 0) {
                            // 添加驳回原因title
                            if (!TextUtils.isEmpty(response.getVerifyNotice())) {
                                validateLicenseBackdesc.setText(response.getVerifyNotice());
                                validate_license_backdesc_root.setVisibility(View.VISIBLE);
                                tv_license_tip.setText(R.string.upload_license_tip2);
                                errorIcon.setVisibility(View.VISIBLE);
                                expandLincenseRoot.setVisibility(View.GONE);
                                isImageSuccess = false;
                            } else {
                                validate_license_backdesc_root.setVisibility(View.GONE);
                                errorIcon.setVisibility(View.GONE);
//                                expand_license.setVisibility(View.VISIBLE);
                            }
                            setButtonEnabled();
                            mProgressLayout.showContent();
                        } else if (response.getRet() == -1) {
                            mProgressLayout.showError(onErrorClickNetworkListener);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        mProgressLayout.showError(onErrorClickNetworkListener);
                    }
                }
            }

            @Override
            public void onError(VolleyError errorResponse) {
                mProgressLayout.showError(onErrorClickNetworkListener);
            }

            @Override
            public void networkFinish() {
            }
        });
    }

    @Override
    public void onRestoreInstanceState(Bundle state) {
        try {
            super.onRestoreInstanceState(state);
        } catch (Exception e) {
        }
        state = null;
    }

    View.OnClickListener onErrorClickNetworkListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!Config.isNetworkConnected(mContext)) {
                return;
            }
            mProgressLayout.showLoading();
            requestValidateDesc();
        }
    };


    /**
     * 网络请求--驾驶证认证
     *
     * @param license 18位驾驶证编号
     * @param record  12位档案编号
     */
    public void requestValidateLicense(String license, String record) {
        showProgress(false);
        UserAuthInterface.LicenseAuthMessage.Request.Builder request = UserAuthInterface.LicenseAuthMessage.Request.newBuilder();
        if (!license.isEmpty() && !record.isEmpty()) {

            request.setLicense(license);
            request.setLicenseId(record);
        }
        request.setImageKey(imgSessionKey);
        NetworkTask task = new NetworkTask(Cmd.CmdCode.LicenseAuthMessage_NL_VALUE);
        task.setBusiData(request.build().toByteArray());
        NetworkUtils.executeNetwork(task, new HttpResponse.NetWorkResponse<UUResponseData>() {
            @Override
            public void onSuccessResponse(UUResponseData responseData) {
                if (responseData.getRet() == 0) {
                    try {
                        showResponseCommonMsg(responseData.getResponseCommonMsg());
                        UserAuthInterface.LicenseAuthMessage.Response response = UserAuthInterface.LicenseAuthMessage.Response.parseFrom(responseData.getBusiData());
                        if (response.getRet() == 0) {
                            finish();
                            if (MainActivity.driverLicenseActivityDescResult != null) {
                                //上传驾照后，去掉首页的上传驾照引导提示。
                                EventBus.getDefault().post(new BaseEvent(EventBusConstant.EVENT_TYPE_CLOSE_CANCEL_DIALOG));
                            }
                        } else if (response.getRet() == -1) {
//                            showDefaultNetworkSnackBar();
                            // 上传失败
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        showDefaultNetworkSnackBar();
                    }
                }
            }

            @Override
            public void onError(VolleyError errorResponse) {
                showDefaultNetworkSnackBar();
            }

            @Override
            public void networkFinish() {
                dismissProgress();
            }
        });
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item == null)
            return true;
        if (item.getItemId() == android.R.id.home || item.getItemId() == 0) {
            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * 设置按钮是否可用
     */
    public void setButtonEnabled() {
//        String license = validateLicenseEdit.getText().toString();
//        String record = validateRecordEdit.getText().toString();
//        if (license.isEmpty() && record.isEmpty() && isImageSuccess) {
//            Config.setB3ViewEnable(validateLicenseButton, true);
//        } else if (RegexUtils.regexLicense(license) && RegexUtils.regexRecord(record)) {
//            Config.setB3ViewEnable(validateLicenseButton, true);
//        } else {
//            Config.setB3ViewEnable(validateLicenseButton, false);
//        }
        if (isImageSuccess) {
            Config.setB3ViewEnable(validateLicenseButton, true);
        } else {
            Config.setB3ViewEnable(validateLicenseButton, false);
        }

    }

    /**
     * 清空在拍照时记录的恢复该Activity的字段
     */
    private void clearSpRecord() {
        SharedPreferences.Editor edit = sp.edit();
        edit.putBoolean(KEY_ACTIVITY_IS_DESTORY, false);
        edit.putString(KEY_BIGPICPATH, "");
        edit.putInt(KEY_PHOTOHEIGHT, 0);
        edit.commit();
    }

    private String getPhotoFileName() {
        StringBuilder fileName = new StringBuilder();
        String phone = UserConfig.getUserInfo().getPhone();
        try {
            phone = phone.replace("*", "1");

        } catch (Exception e) {
            phone = "";
        }
        fileName.append(phone + "validate" + ".jpg");
        return fileName.toString();
    }

    /**
     * 保存并压缩图片
     *
     * @param photo
     * @param spath
     * @return
     */
    public static boolean saveImage(Bitmap photo, String spath) {
        try {
            File mPhotoFile = new File(spath);
            if (!mPhotoFile.exists()) {
                mPhotoFile.createNewFile();
            }
            photo = BitmapUtils.getInSampleBitmapByBitmap(photo);
            BufferedOutputStream bos = new BufferedOutputStream(
                    new FileOutputStream(spath, false));
            photo.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            bos.flush();
            bos.close();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    Bitmap bitmap;

    // 压缩图片的宽高
    private int photoWidth = 320, photoHeight = 480;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(KEY_BIGPICPATH, bigPicPath);
        outState.putInt(KEY_PHOTOHEIGHT, photoHeight);
        try {
            super.onSaveInstanceState(outState);
        } catch (Exception e) {
            outState = null;
        }
    }


    private void getPhoto() {
        picPath = sp.getString(UserConfig.getUserInfo().getPhone() + "pic_path", "");
        if (!"".equals(picPath)) {
            try {
                bitmap = BitmapUtils.getInSampleBitmap(picPath, photoWidth, photoHeight);
                setImageViewByVolley(bitmap);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * 开始执行拍照操作
     */
    private void startTackPhoto() {
        Bitmap bitmapTemp = null;
        if (bigPicPath != null) {
            bitmapTemp = BitmapUtils.getInSampleBitmapByFile(bigPicPath);
        }
        if (bitmapTemp != null) {
            bitmapTemp = rotateBitmapByDegree(bitmapTemp, getBitmapDegree(bigPicPath));
            picPath = SysConfig.SD_IMAGE_PATH + getPhotoFileName();
            sp.edit().putString(UserConfig.getUserInfo().getPhone() + "pic_path", picPath).commit();
            if (bitmapTemp != null) {
                saveImage(bitmapTemp, picPath);
                try {
                    bitmap = BitmapUtils.getInSampleBitmap(picPath, photoWidth, photoHeight);
                    setImageViewByVolley(bitmap);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                bitmapTemp.recycle();
            }
        }
    }

    public String imgSessionKey = "";
    public boolean isImageSuccess = false;

    private void setImageViewByVolley(final Bitmap bitmap) {
        if (Config.isNetworkConnected(this)) {
            showProgress(false);
            UUParams params = new UUParams();
            // 记录上传中状态之前的状态
            params.put("name", new File(picPath));
            NetworkTask networkTask = new NetworkTask(params);
            networkTask.setTag("upload");
            NetworkUtils.executeNetwork(networkTask, new HttpResponse.NetWorkResponse<JSONObject>() {
                @Override
                public void onSuccessResponse(JSONObject json) {
                    try {
                        int ret = json.getInt("code");
                        if (ret == 0) {
                            String responseData = json.getString("message");
                            if (responseData != null) {
                                if (responseData.equals("-1")) {
                                    showDefaultNetworkSnackBar();
                                } else {
                                    imgSessionKey = json.getString("message");
                                    SharedPreferences.Editor edit = sp.edit();
                                    edit.putString(UserConfig.getUserInfo().getPhone() + "imgSessionKey", imgSessionKey);
                                    edit.putLong("time", System.currentTimeMillis());
                                    edit.commit();
                                    licenseImg.setImageBitmap(bitmap);
                                    isImageSuccess = true;
                                    validate_license_backdesc_root.setVisibility(View.GONE);
                                    errorIcon.setVisibility(View.GONE);
                                    expandLincenseRoot.setVisibility(View.VISIBLE);
                                    setButtonEnabled();
                                }
                            }
                        } else {
                            showDefaultNetworkSnackBar();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onError(VolleyError errorResponse) {
                    showDefaultNetworkSnackBar();
                }

                @Override
                public void networkFinish() {
                    dismissProgress();
                }
            });

        }
    }

    /**
     * 将图片按照某个角度进行旋转
     *
     * @param bm     需要旋转的图片
     * @param degree 旋转角度
     * @return 旋转后的图片
     */
    public static Bitmap rotateBitmapByDegree(Bitmap bm, int degree) {
        Bitmap returnBm = null;

        // 根据旋转角度，生成旋转矩阵
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        try {
            // 将原始图片按照旋转矩阵进行旋转，并得到新的图片
            returnBm = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);
        } catch (OutOfMemoryError e) {
        }
        if (returnBm == null) {
            returnBm = bm;
        }
        if (bm != returnBm) {
            bm.recycle();
        }
        return returnBm;
    }

    /**
     * 读取图片的旋转的角度
     *
     * @param path 图片绝对路径
     * @return 图片的旋转角度
     */
    private int getBitmapDegree(String path) {
        int degree = 0;
        try {
            // 从指定路径下读取图片，并获取其EXIF信息
            ExifInterface exifInterface = new ExifInterface(path);
            // 获取图片的旋转信息
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return degree;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        clearSpRecord();
        if (resultCode == RESULT_OK && requestCode == ValidateLicenseActivity.TAKE_PHOTO_CODE) {
//            startTackPhoto();
            getPhoto();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

}
