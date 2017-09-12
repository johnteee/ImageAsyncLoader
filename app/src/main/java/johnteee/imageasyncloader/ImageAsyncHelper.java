package johnteee.imageasyncloader;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
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
    private BitmapCacheWithARC resBitmapCache;

    ImageAsyncHelper() {
        this(30);
    }

    ImageAsyncHelper(float cacheSizeInMiB) {
        this.cacheSize = (int) (cacheSizeInMiB * MIB_IN_BYTES);

        init();
    }

    private void init() {
        resBitmapCache = new BitmapCacheWithARC(cacheSize);
        viewTimeOrderMap = new ConcurrentHashMap<>();
    }

    public void loadImageResAsync(final Context context, final ImageView imageView, final int resId, final int req_width, final int req_height) {
        final String keyOfView = getKeyOfObject(imageView);

        final long myOperatingExactTimestamp = System.currentTimeMillis();
        viewTimeOrderMap.put(keyOfView, myOperatingExactTimestamp);

        BitmapDrawable bitmapDrawable = resBitmapCache.get(getDrawableKeyByResId(resId));
        if (!(ImageUtils.isBitmapDrawableEmptyOrRecycled(bitmapDrawable))) {
            setImageBitmapOnUiThread(imageView, bitmapDrawable, myOperatingExactTimestamp, false);
            return;
        }

        // Avoid recycling images displayed wrong images.
        setImageBitmapOnUiThread(imageView, null, myOperatingExactTimestamp, false);
        AsyncTask<Void, Void, Void> asyncTask = new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... voids) {

                Bitmap newBitmap = ImageUtils.decodeSampledBitmapFromResource(context.getResources(), resId, req_width, req_height);
                BitmapDrawable newBitmapDrawable = new BitmapDrawable(context.getResources(), newBitmap);

                BitmapDrawable existingBitmapDrawable = resBitmapCache.get(getDrawableKeyByResId(resId));
                if (ImageUtils.isBitmapDrawableEmptyOrRecycled(existingBitmapDrawable)) {
                    resBitmapCache.putWithARC(getDrawableKeyByResId(resId), newBitmapDrawable);
                }
                else {
                    ImageUtils.recycleDrawable(newBitmapDrawable);
                }

                setImageBitmapOnUiThread(imageView, newBitmapDrawable, myOperatingExactTimestamp);

                return null;
            }
        };
        asyncTask.execute();
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
    private void setImageBitmapOnUiThread(final ImageView imageView, final BitmapDrawable bitmapDrawable, final long myOperatingExactTimestamp) {
        setImageBitmapOnUiThread(imageView, bitmapDrawable, myOperatingExactTimestamp, true);
    }

    /**
     *
     * @param imageView
     * @param bitmapDrawable
     * @param myOperatingExactTimestamp To avoid the disorder problems of imageview updating.
     */
    private void setImageBitmapOnUiThread(final ImageView imageView, final BitmapDrawable bitmapDrawable, final long myOperatingExactTimestamp, final boolean withAnimation) {
        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                String keyOfView = getKeyOfObject(imageView);
                Long lastTimestamp = viewTimeOrderMap.get(keyOfView);
                if (lastTimestamp == null || myOperatingExactTimestamp >= lastTimestamp) {
                    resBitmapCache.doThingsWithARCSafe(new Runnable() {
                        @Override
                        public void run() {
                            Drawable oldDrawable = imageView.getDrawable();
                            resBitmapCache.changeDrawableARCAndCheck(oldDrawable, -1);
                            resBitmapCache.changeDrawableARCAndCheck(bitmapDrawable, 1);

                            if (bitmapDrawable == null || (! ImageUtils.isBitmapDrawableEmptyOrRecycled(bitmapDrawable))) {
                                imageView.setImageDrawable(bitmapDrawable);

                                if (withAnimation) {
                                    imageView.startAnimation(AnimationUtils.loadAnimation(imageView.getContext(), android.R.anim.fade_in));
                                }
                            }
                        }
                    });
                }
            }
        });
    }

    public void terminate() {
        resBitmapCache.terminate();
        viewTimeOrderMap.clear();
    }
}
