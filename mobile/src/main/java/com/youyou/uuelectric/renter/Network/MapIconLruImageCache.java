package com.youyou.uuelectric.renter.Network;

/**
 * 首页地图Marker图标缓存类
 */

import android.graphics.Bitmap;
import android.support.v4.util.LruCache;
import android.widget.ImageView;

import com.android.volley.toolbox.ImageLoader.ImageCache;

public class MapIconLruImageCache implements ImageCache {

    private static LruCache<String, Bitmap> mMemoryCache;

    private static MapIconLruImageCache lruImageCache;

    private MapIconLruImageCache() {
        // Get the Max available memory
        int maxMemory = (int) Runtime.getRuntime().maxMemory();
        int cacheSize = maxMemory / 8;
        mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                return bitmap.getRowBytes() * bitmap.getHeight();
            }
        };
    }

    public static MapIconLruImageCache getInstance() {
        if (lruImageCache == null) {
            lruImageCache = new MapIconLruImageCache();
        }
        return lruImageCache;
    }

    @Override
    public Bitmap getBitmap(String arg0) {
        return mMemoryCache.get(arg0);
    }

    @Override
    public void putBitmap(String arg0, Bitmap arg1) {
        if (getBitmap(arg0) == null) {
            mMemoryCache.put(arg0, arg1);
        }
    }

    /**
     * Creates a cache key for use with the L1 cache.
     *
     * @param url       The URL of the request.
     */
    public static String getCacheKey(String url) {
        return new StringBuilder(url.length() + 12).append("#W").append(0)
                .append("#H").append(0).append("#S").append(ImageView.ScaleType.CENTER_INSIDE.ordinal()).append(url)
                .toString();
    }

}
