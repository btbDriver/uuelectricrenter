package com.youyou.uuelectric.renter.UI.web.url;

/**
 * Created by liuchao on 2015/9/15.
 */
public class WebUrl {

    public String url = null;
    public String title = null;

    public WebUrl() {
    }

    public WebUrl(String url, String title) {
        this.url = url;
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public String getTitle() {
        return title;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
