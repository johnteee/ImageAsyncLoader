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
import android.widget.ImageView;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by teee on 2017/9/12.
 */

public class ImageAsyncUtils {

    public static final int REQ_WIDTH = 100;
    public static final int REQ_HEIGHT = 100;
    private static Handler uiHandler = new Handler(Looper.getMainLooper());
    private static ConcurrentHashMap<String, Long> viewTimeOrderMap = new ConcurrentHashMap<>();

    private static int cacheSize = 30 * 1024 * 1024; // 30MiB
    private static BitmapCacheWithARC resBitmapCache = new BitmapCacheWithARC(cacheSize);

    public static void loadImageResAsync(final Context context, final ImageView imageView, final int resId) {
        final String keyOfView = getKeyOfObject(imageView);

        final long myOperatingExactTimestamp = System.currentTimeMillis();
        viewTimeOrderMap.put(keyOfView, myOperatingExactTimestamp);

        BitmapDrawable bitmapDrawable = resBitmapCache.get(getDrawableKeyByResId(resId));
        if (!(ImageUtils.isBitmapDrawableEmptyOrRecycled(bitmapDrawable))) {
            setImageBitmapOnUiThread(imageView, bitmapDrawable, myOperatingExactTimestamp);
            return;
        }

        // Avoid recycling images displayed wrong images.
        setImageBitmapOnUiThread(imageView, null, myOperatingExactTimestamp);
        AsyncTask<Void, Void, Void> asyncTask = new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... voids) {

                Bitmap newBitmap = ImageUtils.decodeSampledBitmapFromResource(context.getResources(), resId, REQ_WIDTH, REQ_HEIGHT);
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
    private static String getDrawableKeyByResId(int resId) {
        return "drawable_" + resId;
    }

    private static String getKeyOfObject(Object object) {
        String value = Objects.toString(object);
        Log.d("test", value);

        return value;
    }

    /**
     *
     * @param imageView
     * @param bitmapDrawable
     * @param myOperatingExactTimestamp To avoid the disorder problems of imageview updating.
     */
    private static void setImageBitmapOnUiThread(final ImageView imageView, final BitmapDrawable bitmapDrawable, final long myOperatingExactTimestamp) {
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
                            }
                        }
                    });
                }
            }
        });
    }
}
