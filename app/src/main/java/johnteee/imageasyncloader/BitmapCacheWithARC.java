package johnteee.imageasyncloader;

import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.util.LruCache;
import android.util.Log;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by teee on 2017/9/12.
 */

class BitmapCacheWithARC extends LruCache<String, BitmapDrawable> {

    private static ConcurrentHashMap<String, Integer> drawableARCMap = new ConcurrentHashMap<>();

    /**
     * @param maxSize for caches that do not override {@link #sizeOf}, this is
     *                the maximum number of entries in the cache. For all other caches,
     *                this is the maximum sum of the sizes of the entries in this cache.
     */
    public BitmapCacheWithARC(int maxSize) {
        super(maxSize);
    }


    @Override
    protected int sizeOf(String key, BitmapDrawable value) {
        return ImageUtils.getBitmapByteCount(value.getBitmap());
    }

    public BitmapDrawable putWithARC(String key, BitmapDrawable value) {

        changeDrawableARC(value, 1);

        return put(key, value);
    }

    @Override
    protected void entryRemoved(boolean evicted, String key, BitmapDrawable oldValue, BitmapDrawable newValue) {
        super.entryRemoved(evicted, key, oldValue, newValue);

        if (evicted && (! ImageUtils.isBitmapDrawableEmptyOrRecycled(oldValue))) {
            changeDrawableARC(oldValue, -1);
        }
    }

    public void changeDrawableARCAndCheck(Drawable drawable, int i) {
        if (drawable == null) {
            return;
        }

        synchronized (drawableARCMap) {
            String key = getKeyOfObject(drawable);
            changeDrawableARC(drawable, i);
            checkARCAndRemove(drawable, key);
        }
    }

    private void changeDrawableARC(Drawable drawable, int i) {
        synchronized (drawableARCMap) {
            String key = getKeyOfObject(drawable);
            Integer value = drawableARCMap.get(key);
            value = value != null ? value : 0;

            int result = value + i;

            Log.d("test", "REMAIN" + (maxSize() - size()));
            Log.d("test", "ARC" + result);
            drawableARCMap.put(key, result);
        }
    }

    private void checkARCAndRemove(Drawable drawable, String key) {
        synchronized (drawableARCMap) {
            if (drawableARCMap.get(key) <= 0) {
                Log.d("test", "Remove by ARC");
                drawableARCMap.remove(key);
                ImageUtils.recycleDrawable(drawable);
            }
        }
    }

    private static String getKeyOfObject(Object object) {
        String value = Objects.toString(object);
        Log.d("test", value);

        return value;
    }
}
