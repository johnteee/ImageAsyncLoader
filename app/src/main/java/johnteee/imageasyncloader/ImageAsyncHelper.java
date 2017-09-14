package johnteee.imageasyncloader;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by teee on 2017/9/12.
 */

class ImageAsyncHelper {

    private static final int MIB_IN_BYTES = 1024 * 1024;

    private Handler uiHandler = new Handler(Looper.getMainLooper());

    private ConcurrentHashMap<String, Long> viewTimeOrderMap;

    private int cacheSize;
    private BitmapCache resBitmapCache;

    ImageAsyncHelper() {
        this(30);
    }

    ImageAsyncHelper(float cacheSizeInMiB) {
        this.cacheSize = (int) (cacheSizeInMiB * MIB_IN_BYTES);

        init();
    }

    private void init() {
        resBitmapCache = new BitmapCache(cacheSize);
        viewTimeOrderMap = new ConcurrentHashMap<>();
    }

    public void loadImageResAsync(final Context context, final ImageView imageView, final int resId, final int req_width, final int req_height) {
        final String keyOfView = getKeyOfObject(imageView);

        final long myOperatingExactTimestamp = System.currentTimeMillis();
        viewTimeOrderMap.put(keyOfView, myOperatingExactTimestamp);

        CacheableBitmapDrawable bitmapDrawable = resBitmapCache.get(getDrawableKeyByResId(resId));
        if (!(ImageUtils.isBitmapDrawableEmptyOrRecycled(bitmapDrawable))) {
            setImageBitmapOnUiThread(imageView, bitmapDrawable, myOperatingExactTimestamp, false);
            return;
        }

        // Avoid recycling ImageViews displayed wrong images.
        setImageBitmapOnUiThread(imageView, null, myOperatingExactTimestamp, false);
        AsyncTask<Void, Void, Void> asyncTask = new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... voids) {
                CacheableBitmapDrawable existingBitmapDrawable = resBitmapCache.get(getDrawableKeyByResId(resId));
                if (! ImageUtils.isBitmapDrawableEmptyOrRecycled(existingBitmapDrawable)) {
                    return null;
                }

                Bitmap newBitmap = ImageUtils.decodeSampledBitmapFromResource(context.getResources(), resId, req_width, req_height);
                CacheableBitmapDrawable newBitmapDrawable = new CacheableBitmapDrawable(context.getResources(), newBitmap);

                resBitmapCache.putWithCachedStatus(getDrawableKeyByResId(resId), newBitmapDrawable);
                setImageBitmapOnUiThread(imageView, newBitmapDrawable, myOperatingExactTimestamp);

                return null;
            }
        };
        asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @NonNull
    private String getDrawableKeyByResId(int resId) {
        return "drawable_" + resId;
    }

    private String getKeyOfObject(Object object) {
        String value = Objects.toString(object);
        Log.d("test", value);

        return value;
    }

    /**
     *
     */
    private void setImageBitmapOnUiThread(final ImageView imageView, final CacheableBitmapDrawable bitmapDrawable, final long myOperatingExactTimestamp) {
        setImageBitmapOnUiThread(imageView, bitmapDrawable, myOperatingExactTimestamp, true);
    }

    /**
     *
     * @param imageView
     * @param bitmapDrawable
     * @param myOperatingExactTimestamp To avoid the disorder problems of imageview updating.
     */
    private void setImageBitmapOnUiThread(final ImageView imageView, final CacheableBitmapDrawable bitmapDrawable, final long myOperatingExactTimestamp, final boolean withAnimation) {
        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                Drawable oldDrawable = imageView.getDrawable();
                if (bitmapDrawable == oldDrawable) {
                    return;
                }

                boolean myTurn = isMyTurn(imageView, myOperatingExactTimestamp);
                if (myTurn) {
                    if (bitmapDrawable == null || (! ImageUtils.isBitmapDrawableEmptyOrRecycled(bitmapDrawable))) {
                        CacheableBitmapDrawable.setImageDrawableWithRecyleableCheck(imageView, bitmapDrawable);

                        if (withAnimation) {
                            imageView.startAnimation(AnimationUtils.loadAnimation(imageView.getContext(), android.R.anim.fade_in));
                        }
                    }
                    else {
                        Log.d("test", "RECYCLED" + getKeyOfObject(bitmapDrawable));
                    }
                }
            }
        });
    }

    private boolean isMyTurn(ImageView imageView, long myOperatingExactTimestamp) {
        String keyOfView = getKeyOfObject(imageView);
        Long lastTimestamp = viewTimeOrderMap.get(keyOfView);
        return lastTimestamp == null || myOperatingExactTimestamp >= lastTimestamp;
    }

    public void terminate() {
        resBitmapCache.terminate();
        viewTimeOrderMap.clear();
    }
}
