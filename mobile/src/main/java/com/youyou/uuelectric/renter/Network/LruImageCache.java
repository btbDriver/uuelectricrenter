package com.youyou.uuelectric.renter.Network;

/**
 * Created by taurusxi on 14-9-23.
 */
import android.graphics.Bitmap;
import android.support.v4.util.LruCache;
import android.widget.ImageView;

import com.android.volley.toolbox.ImageLoader.ImageCache;

public class LruImageCache implements ImageCache
{

    private static LruCache<String, Bitmap> mMemoryCache;

    private static LruImageCache lruImageCache;

    private LruImageCache(){
        // Get the Max available memory
        int maxMemory = (int) Runtime.getRuntime().maxMemory();
        int cacheSize = maxMemory / 8;
        mMemoryCache = new LruCache<String, Bitmap>(cacheSize){
            @Override
            protected int sizeOf(String key, Bitmap bitmap){
                return bitmap.getRowBytes() * bitmap.getHeight();
            }
        };
    }

    public static LruImageCache getInstance(){
        if(lruImageCache == null){
            lruImageCache = new LruImageCache();
        }
        return lruImageCache;
    }

    @Override
    public Bitmap getBitmap(String key) {
        return mMemoryCache.get(key);
    }

    @Override
    public void putBitmap(String key, Bitmap bitmap) {
        if (getBitmap(key) == null) {
            mMemoryCache.put(key, bitmap);
        }
    }

    public void removeBitmap(String key) {
        if (getBitmap(key) != null) {
            mMemoryCache.remove(key);
        }
    }


    public static String getCacheKey(String url) {
        return new StringBuilder(url.length() + 12).append("#W").append(0)
                .append("#H").append(0).append("#S").append(ImageView.ScaleType.CENTER_INSIDE.ordinal()).append(url)
                .toString();
    }

}
