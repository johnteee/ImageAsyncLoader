package johnteee.imageasyncloader;

import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.util.LruCache;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by teee on 2017/9/12.
 */

class BitmapCache extends LruCache<String, CacheableBitmapDrawable> {

    private AtomicBoolean isTerminating = new AtomicBoolean(false);

    /**
     * @param maxSize for caches that do not override {@link #sizeOf}, this is
     *                the maximum number of entries in the cache. For all other caches,
     *                this is the maximum sum of the sizes of the entries in this cache.
     */
    public BitmapCache(int maxSize) {
        super(maxSize);
    }


    @Override
    protected int sizeOf(String key, CacheableBitmapDrawable value) {
        return ImageUtils.getBitmapByteCount(value.getBitmap());
    }

    public BitmapDrawable putWithCachedStatus(String key, CacheableBitmapDrawable value) {

        value.setCached(true);

        return put(key, value);
    }

    @Override
    protected void entryRemoved(boolean evicted, String key, CacheableBitmapDrawable oldValue, CacheableBitmapDrawable newValue) {
        super.entryRemoved(evicted, key, oldValue, newValue);

        if (evicted && (! ImageUtils.isBitmapDrawableEmptyOrRecycled(oldValue))) {
            oldValue.setCached(false);

            if (isTerminating.get()) {
                ImageUtils.recycleDrawable(oldValue);
            }
        }
    }

    public void terminate() {
        isTerminating.set(true);
        evictAll();
        System.gc();
    }

    public void checkDrawableRecycleable(Drawable drawable) {
        if (drawable instanceof CacheableBitmapDrawable) {
            final CacheableBitmapDrawable cacheableBitmapDrawable = (CacheableBitmapDrawable) drawable;
            cacheableBitmapDrawable.doThingsAboutAccessCachedStatus(new Runnable() {
                @Override
                public void run() {
                    if (! cacheableBitmapDrawable.isCached()) {
                        ImageUtils.recycleDrawable(cacheableBitmapDrawable);
                    }
                }
            });
        }
    }
}
