package johnteee.imageasyncloader;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;

import java.io.InputStream;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by teee on 2017/9/13.
 */

public class CacheableBitmapDrawable extends BitmapDrawable {

    private AtomicInteger isCached = new AtomicInteger(0);
    private AtomicInteger isDisplayed = new AtomicInteger(0);

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
                if (isCached.get() <= 0 && isDisplayed.get() <= 0) {
                    ImageUtils.recycleDrawable(CacheableBitmapDrawable.this);
                }
            }
        });
    }
}
