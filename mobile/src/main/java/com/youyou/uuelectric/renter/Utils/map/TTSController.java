package com.youyou.uuelectric.renter.Utils.map;

import android.content.Context;
import android.os.Bundle;

import com.amap.api.navi.AMapNaviListener;
import com.amap.api.navi.model.AMapNaviInfo;
import com.amap.api.navi.model.AMapNaviLocation;
import com.amap.api.navi.model.NaviInfo;
import com.iflytek.cloud.speech.SpeechConstant;
import com.iflytek.cloud.speech.SpeechError;
import com.iflytek.cloud.speech.SpeechListener;
import com.iflytek.cloud.speech.SpeechSynthesizer;
import com.iflytek.cloud.speech.SpeechUser;
import com.iflytek.cloud.speech.SynthesizerListener;
import com.youyou.uuelectric.renter.R;

/**
 * 语音播报组件
 */
public class TTSController implements SynthesizerListener, AMapNaviListener {

    public static TTSController ttsManager;
    private Context mContext;
    // 合成对象.
    private SpeechSynthesizer mSpeechSynthesizer;

    private String appId = "";

    TTSController(Context context) {
        mContext = context;
    }

    public static TTSController getInstance(Context context) {
        if (ttsManager == null) {
            ttsManager = new TTSController(context);
        }
        return ttsManager;
    }

    public void init() {
        appId = mContext.getResources().getString(R.string.app_id);
        SpeechUser.getUser().login(mContext, null, null,
                "appid=" + appId, listener);
        // 初始化合成对象.
        mSpeechSynthesizer = SpeechSynthesizer.createSynthesizer(mContext);
        initSpeechSynthesizer();
    }

    /**
     * 使用SpeechSynthesizer合成语音，不弹出合成Dialog.
     *
     * @param
     */
    public void playText(String playText) {
        if (!isfinish) {
            return;
        }
        if (null == mSpeechSynthesizer) {
            // 创建合成对象.
            mSpeechSynthesizer = SpeechSynthesizer.createSynthesizer(mContext);
            initSpeechSynthesizer();
        }
        // 进行语音合成.
        mSpeechSynthesizer.startSpeaking(playText, this);

    }

    public void stopSpeaking() {
        if (mSpeechSynthesizer != null)
            mSpeechSynthesizer.stopSpeaking();
    }

    public void startSpeaking() {
        isfinish = true;
    }

    private void initSpeechSynthesizer() {
        // 设置发音人
        mSpeechSynthesizer.setParameter(SpeechConstant.VOICE_NAME,
                mContext.getString(R.string.preference_default_tts_role));
        // 设置语速
        mSpeechSynthesizer.setParameter(SpeechConstant.SPEED,
                "" + mContext.getString(R.string.preference_key_tts_speed));
        // 设置音量
        mSpeechSynthesizer.setParameter(SpeechConstant.VOLUME,
                "" + mContext.getString(R.string.preference_key_tts_volume));
        // 设置语调
        mSpeechSynthesizer.setParameter(SpeechConstant.PITCH,
                "" + mContext.getString(R.string.preference_key_tts_pitch));

    }

    /**
     * 用户登录回调监听器.
     */
    private SpeechListener listener = new SpeechListener() {

        @Override
        public void onData(byte[] arg0) {
        }

        @Override
        public void onCompleted(SpeechError error) {
            if (error != null) {

            }
        }

        @Override
        public void onEvent(int arg0, Bundle arg1) {
        }
    };

    @Override
    public void onBufferProgress(int arg0, int arg1, int arg2, String arg3) {
        // 

    }

    boolean isfinish = true;

    @Override
    public void onCompleted(SpeechError arg0) {
        // 
        isfinish = true;
    }

    @Override
    public void onSpeakBegin() {
        // 
        isfinish = false;

    }

    @Override
    public void onSpeakPaused() {
        // 

    }

    @Override
    public void onSpeakProgress(int arg0, int arg1, int arg2) {
        // 

    }

    @Override
    public void onSpeakResumed() {
        // 

    }

    public void destroy() {
        if (mSpeechSynthesizer != null) {
            mSpeechSynthesizer.stopSpeaking();
        }
    }

    @Override
    public void onArriveDestination() {
        // 
        this.playText("到达目的地");
    }

    @Override
    public void onArrivedWayPoint(int arg0) {
        // 
    }

    @Override
    public void onCalculateRouteFailure(int arg0) {
        this.playText("路径计算失败，请检查网络或输入参数");
    }

    @Override
    public void onCalculateRouteSuccess() {
        String calculateResult = "路径计算就绪";

        this.playText(calculateResult);
    }

    @Override
    public void onEndEmulatorNavi() {
        this.playText("导航结束");

    }

    @Override
    public void onGetNavigationText(int arg0, String arg1) {
        // 
        this.playText(arg1);
    }

    @Override
    public void onInitNaviFailure() {
        // 

    }

    @Override
    public void onInitNaviSuccess() {
        // 

    }

    @Override
    public void onLocationChange(AMapNaviLocation arg0) {
        // 

    }

    @Override
    public void onReCalculateRouteForTrafficJam() {
        // 
        this.playText("前方路线拥堵，路线重新规划");
    }

    @Override
    public void onReCalculateRouteForYaw() {

        this.playText("您已偏航");
    }

    @Override
    public void onStartNavi(int arg0) {
        // 

    }

    @Override
    public void onTrafficStatusUpdate() {
        // 

    }

    @Override
    public void onGpsOpenStatus(boolean arg0) {
        // 

    }

    @Override
    public void onNaviInfoUpdated(AMapNaviInfo arg0) {
        // 

    }

    @Override
    public void onNaviInfoUpdate(NaviInfo arg0) {


    }
}
