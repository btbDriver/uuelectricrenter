package com.youyou.uuelectric.renter.UI.web;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;

import com.youyou.uuelectric.renter.R;
import com.youyou.uuelectric.renter.UI.base.BaseActivity;
import com.youyou.uuelectric.renter.Utils.BitmapUtils;
import com.youyou.uuelectric.renter.Utils.ReadImgToBinary2;
import com.youyou.uuelectric.renter.Utils.Support.SysConfig;

/**
 * Created by liuchao on 2015/8/31.
 * 客户端H5页面
 */
public class H5Activity extends BaseActivity {

    public H5Fragment h5Fragment = null;
    public Thread cameraThread = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_h5);
        h5Fragment = new H5Fragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.mfl_content_container, h5Fragment).commit();
    }

    /**
     * 重写onSaveInstanceState方法，保证H5Activity在后台销毁时，H5Fragment也同时销毁
     *   否则H5Fragment中调用getActivity可能会出现空指针异常
     * @param outState
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        /*if (cameraThread != null) {
            cameraThread.interrupt();
            cameraThread = null;
        }*/
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // 打开相机解析图片
        if (requestCode == H5Constant.h5CameraCode && resultCode == RESULT_OK) {
            cameraThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    Bitmap bitmapTemp = null;
                    if (TextUtils.isEmpty(H5Constant.bigPicPath)) {
                        H5Constant.bigPicPath = SysConfig.SD_IMAGE_PATH + H5Constant.tempImage;
                    }
                    if (H5Constant.bigPicPath != null) {
                        // 压缩图片的宽高
                        int photoWidth = 320, photoHeight = 480;
                        bitmapTemp = BitmapUtils.getInSampleBitmap(H5Constant.bigPicPath, photoWidth, photoHeight);
                    }
                    if (bitmapTemp != null) {
                        // 判断图片是否需要旋转角度，若需要的话则旋转
                        bitmapTemp = BitmapUtils.rotateBitmapByDegree(bitmapTemp, BitmapUtils.getBitmapDegree(H5Constant.bigPicPath));
                        if (bitmapTemp != null) {
                            if (TextUtils.isEmpty(H5Constant.picPath)) {
                                H5Constant.picPath = SysConfig.SD_IMAGE_PATH + H5Constant.compressImage;
                            }
                            BitmapUtils.saveImage(bitmapTemp, H5Constant.picPath);
                            try {
                                if (!TextUtils.isEmpty(H5Constant.cameraCallback)) {
                                    // 将图片转换为Base64字符串
                                    final String base64Str = ReadImgToBinary2.imgToBase64(H5Constant.picPath, bitmapTemp, "");
                                    // 发送请求，主线程中调用webview的js方法，传输base64字符串（图片信息）
                                    // h5Fragment.mWebView.loadUrl("javascript:" + H5Constant.cameraCallback + "('" + base64Str + "')");
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            h5Fragment.mWebView.loadUrl("javascript:" + H5Constant.cameraCallback + "('" + base64Str + "')");
                                        }
                                    });
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            if (bitmapTemp != null && !bitmapTemp.isRecycled()) {
                                bitmapTemp.recycle();
                            }
                        }
                    }
                }
            });
            cameraThread.start();

        }
    }


}
