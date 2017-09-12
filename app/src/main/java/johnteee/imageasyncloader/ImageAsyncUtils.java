package johnteee.imageasyncloader;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.util.LruCache;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by teee on 2017/9/12.
 */

public class ImageAsyncUtils {

    public static final int REQ_WIDTH = 200;
    public static final int REQ_HEIGHT = 200;
    private static Handler uiHandler = new Handler(Looper.getMainLooper());
    private static ConcurrentHashMap<String, Long> viewTimeOrderMap = new ConcurrentHashMap<>();

    private static int cacheSize = 4 * 1024 * 1024; // 4MiB
    private static LruCache<Integer, Bitmap> resBitmapCache = new LruCache<Integer, Bitmap>(cacheSize) {
        protected int sizeOf(String key, Bitmap value) {
            return value.getByteCount();
        }

        @Override
        protected void entryRemoved(boolean evicted, Integer key, Bitmap oldValue, Bitmap newValue) {
            synchronized (oldValue) {
                if (! oldValue.isRecycled()) {
                    oldValue.recycle();
                    System.gc();
                }

                super.entryRemoved(evicted, key, oldValue, newValue);
            }
        }
    };

    public static void loadImageResAsync(final Context context, final ImageView imageView, final int resId) {
        final String keyOfView = getKeyOfView(imageView);

        final long myOperatingExactTimestamp = System.currentTimeMillis();
        viewTimeOrderMap.put(keyOfView, myOperatingExactTimestamp);

        Bitmap oldBitmap = resBitmapCache.get(resId);
        if (oldBitmap != null && (! oldBitmap.isRecycled())) {
            setImageBitmapOnUiThread(imageView, oldBitmap, myOperatingExactTimestamp);
            return;
        }

        // Avoid recycling images displayed wrong images.
        setImageBitmapOnUiThread(imageView, null, myOperatingExactTimestamp);
        AsyncTask<Void, Void, Void> asyncTask = new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... voids) {

                Bitmap newBitmap = ImageUtils.decodeSampledBitmapFromResource(context.getResources(), resId, REQ_WIDTH, REQ_HEIGHT);
                Bitmap existingBitmap = resBitmapCache.get(resId);
                if (existingBitmap == null || existingBitmap.isRecycled()) {
                    resBitmapCache.put(resId, newBitmap);
                }

                setImageBitmapOnUiThread(imageView, newBitmap, myOperatingExactTimestamp);

                return null;
            }
        };
        asyncTask.execute();
    }

    private static String getKeyOfView(View view) {
        String value = Objects.toString(view);
        Log.d("test", value);

        return value;
    }

    /**
     *
     * @param imageView
     * @param bitmap
     * @param myOperatingExactTimestamp To avoid the disorder problems of imageview updating.
     */
    private static void setImageBitmapOnUiThread(final ImageView imageView, final Bitmap bitmap, final long myOperatingExactTimestamp) {
        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                String keyOfView = getKeyOfView(imageView);
                Long lastTimestamp = viewTimeOrderMap.get(keyOfView);
                if (lastTimestamp == null || myOperatingExactTimestamp >= lastTimestamp) {
                    imageView.setImageBitmap(bitmap);
                }
            }
        });
    }
}
