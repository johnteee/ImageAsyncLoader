package johnteee.imageasyncloader;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.widget.ImageView;

import java.io.InputStream;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by teee on 2017/9/13.
 */

public class CacheableBitmapDrawable extends BitmapDrawable {

    private AtomicInteger isCached = new AtomicInteger(0);
    private AtomicInteger isDisplayed = new AtomicInteger(0);

    private AtomicBoolean hasBeenDisplayed = new AtomicBoolean(false);

    public CacheableBitmapDrawable(Resources res, Bitmap bitmap) {
        super(res, bitmap);
    }

    public CacheableBitmapDrawable(Resources res, String filepath) {
        super(res, filepath);
    }

    public CacheableBitmapDrawable(Resources res, InputStream is) {
        super(res, is);
    }

    public void setCached(final boolean cached) {
        doThingsAboutAccessCachedStatus(new Runnable() {
            @Override
            public void run() {
                if (cached) {
                    isCached.incrementAndGet();
                }
                else {
                    isCached.decrementAndGet();
                }

                checkDrawableRecycleable();
            }
        });
    }

    public void setDisplayed(final boolean displayed) {
        doThingsAboutAccessCachedStatus(new Runnable() {
            @Override
            public void run() {
                if (displayed) {
                    isDisplayed.incrementAndGet();
                    hasBeenDisplayed.set(true);
                }
                else {
                    isDisplayed.decrementAndGet();
                }

                checkDrawableRecycleable();
            }
        });
    }

    public synchronized void doThingsAboutAccessCachedStatus(Runnable runnable) {
        if (runnable != null) {
            runnable.run();
        }
    }

    public void checkDrawableRecycleable() {
        doThingsAboutAccessCachedStatus(new Runnable() {
            @Override
            public void run() {
                if (isCached.get() <= 0 && isDisplayed.get() <= 0 && hasBeenDisplayed.get()) {
                    ImageUtils.recycleDrawable(CacheableBitmapDrawable.this);
                }
            }
        });
    }

    public static void notifyDrawable(Drawable drawable, final boolean isDisplayed) {
        if (drawable instanceof CacheableBitmapDrawable) {

            ((CacheableBitmapDrawable) drawable).setDisplayed(isDisplayed);

        } else if (drawable instanceof LayerDrawable) {

            // The drawable is a LayerDrawable, so recurse on each layer
            LayerDrawable layerDrawable = (LayerDrawable) drawable;
            for (int i = 0, z = layerDrawable.getNumberOfLayers(); i < z; i++) {
                notifyDrawable(layerDrawable.getDrawable(i), isDisplayed);
            }
        }
    }

    public static void setImageDrawableWithRecyleableCheck(ImageView imageView, Drawable drawable) {

        if (imageView instanceof CacheableImageView) {
            imageView.setImageDrawable(drawable);
        }
        else {
            Drawable oldDrawable = imageView.getDrawable();

            imageView.setImageDrawable(drawable);

            CacheableBitmapDrawable.notifyDrawable(drawable, true);
            CacheableBitmapDrawable.notifyDrawable(oldDrawable, false);
        }
    }
}
