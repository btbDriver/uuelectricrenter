package com.youyou.uuelectric.renter.Utils.share;

/**
 * Created by liuchao on 2015/9/16.
 */
public class ShareInfo {
    public String shareTitle = null;// 分享标题
    public String context = null;// 分享文案
    public String shareUrlWechart = null;// 微信分享的URL地址
    public String shareUrlFriend = null;// 朋友圈分享的URL地址
    public String url = null;// 基本URL地址，若其他URL地址为空的时候天传URL

    public void setShareTitle(String shareTitle) {
        this.shareTitle = shareTitle;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public void setShareUrlWechart(String shareUrlWechart) {
        this.shareUrlWechart = shareUrlWechart;
    }

    public void setShareUrlFriend(String shareUrlFriend) {
        this.shareUrlFriend = shareUrlFriend;
    }

    public String getShareTitle() {

        return shareTitle;
    }

    public String getContext() {
        return context;
    }


    public String getShareUrlWechart() {
        return shareUrlWechart;
    }

    public String getShareUrlFriend() {
        return shareUrlFriend;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUrl() {

        return url;
    }

    String imgUrl;

    public void setImage(String imgUrl) {
        this.imgUrl = imgUrl;
    }
}
