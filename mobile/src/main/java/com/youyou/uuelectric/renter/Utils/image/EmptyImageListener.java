package com.youyou.uuelectric.renter.Utils.image;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;

/**
 * ImageLoader中的ImageListener的空实现
 */
public class EmptyImageListener implements ImageLoader.ImageListener {

    @Override
    public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {

    }

    @Override
    public void onErrorResponse(VolleyError error) {

    }
}
