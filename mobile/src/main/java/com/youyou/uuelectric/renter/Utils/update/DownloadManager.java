package com.youyou.uuelectric.renter.Utils.update;

import android.app.Activity;
import android.net.Uri;
import android.text.TextUtils;

import com.thin.downloadmanager.DefaultRetryPolicy;
import com.thin.downloadmanager.DownloadRequest;
import com.thin.downloadmanager.DownloadStatusListenerV1;
import com.thin.downloadmanager.ThinDownloadManager;
import com.youyou.uuelectric.renter.Utils.Support.L;
import com.youyou.uuelectric.renter.Utils.Support.SysConfig;

import java.io.File;

/**
 * Created by aaron on 16/5/4.
 * 文件下载管理类
 */
public class DownloadManager {
    private static Object lockObj = new Object();

    private static ThinDownloadManager thinDownloadManager = null;
    // 通过get方法获取
    public static String apkPath = null;
    public static String downApk = "update.apk";
    public static int downloadId = -1;

    /**
     * 获取下载管理对象
     * @return
     */
    public static com.thin.downloadmanager.DownloadManager getInstance() {
        synchronized (lockObj) {
            if (thinDownloadManager == null) {
                thinDownloadManager = new ThinDownloadManager();
            }
        }
        return thinDownloadManager;
    }



    /**
     * 获取下载apk文件的url
     * @param downLoadUrl
     * @return
     */
    public static DownloadRequest getDownLoadRequest(Activity mContext, String downLoadUrl, DownloadStatusListenerV1 downloadStatusListenerV1) {
        Uri downLoadUri = Uri.parse(downLoadUrl);
        L.i("downLoad path:" + getApkPath(mContext));
        Uri destionUri = Uri.parse(getApkPath(mContext));
        DownloadRequest downloadRequest = new DownloadRequest(downLoadUri)
                .setRetryPolicy(new DefaultRetryPolicy())
                .setDestinationURI(destionUri).setPriority(DownloadRequest.Priority.HIGH)
                .setDownloadContext(mContext)
                .setStatusListener(downloadStatusListenerV1);

        return downloadRequest;
    }


    /**
     * 获取apk下载路径
     * @param mContext
     * @return
     */
    public static String getApkPath(Activity mContext) {
        if (TextUtils.isEmpty(apkPath)) {
            File file = new File(SysConfig.DOWN_APP_PATH);
            if (!file.exists()) {
                file.mkdirs();
            }
            apkPath = SysConfig.DOWN_APP_PATH + downApk;
        }

        return apkPath;
    }
}
