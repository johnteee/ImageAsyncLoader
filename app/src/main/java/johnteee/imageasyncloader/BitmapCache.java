package johnteee.imageasyncloader;

import android.support.v4.util.LruCache;

import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by teee on 2017/9/12.
 */

class BitmapCache<T extends CacheableBitmapDrawable> extends LruCache<String, T> {

    private AtomicBoolean isTerminating = new AtomicBoolean(false);

    private WeakHashMap<String, T> drawableWeakHashMap = new WeakHashMap<>();

    /**
     * @param maxSize for caches that do not override {@link #sizeOf}, this is
     *                the maximum number of entries in the cache. For all other caches,
     *                this is the maximum sum of the sizes of the entries in this cache.
     */
    public BitmapCache(int maxSize) {
        super(maxSize);
    }


    @Override
    protected int sizeOf(String key, T value) {
        return ImageUtils.getBitmapByteCount(value.getBitmap());
    }

    public void putWithCachedStatus(String key, T value) {
        value.setCached(true);
        drawableWeakHashMap.put(key, value);

        put(key, value);

        checkTerminating();
    }

    @Override
    protected void entryRemoved(boolean evicted, String key, T oldValue, T newValue) {
        super.entryRemoved(evicted, key, oldValue, newValue);

        if (evicted && (! ImageUtils.isBitmapDrawableEmptyOrRecycled(oldValue))) {
            oldValue.setCached(false);

            checkTerminating();
        }
    }

    private void recycleAllDrawableWeakHashMap() {
        for (T value : drawableWeakHashMap.values()) {
            if (value != null) {
                ImageUtils.recycleDrawable(value);
            }
        }
    }

    public void terminate() {
        synchronized (isTerminating) {
            isTerminating.set(true);
            recycleAllResources();
        }
    }

    public void checkTerminating() {
        synchronized (isTerminating) {
            if (isTerminating.get()) {
                recycleAllResources();
            }
        }
    }

    private void recycleAllResources() {
        evictAll();
        recycleAllDrawableWeakHashMap();
        System.gc();
    }
}
