package johnteee.imageasyncloader;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;

import java.io.InputStream;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by teee on 2017/9/13.
 */

public class CacheableBitmapDrawable extends BitmapDrawable {

    private AtomicBoolean isCached = new AtomicBoolean(false);

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
                isCached.set(cached);
            }
        });
    }

    public boolean isCached() {
        return isCached.get();
    }

    public synchronized void doThingsAboutAccessCachedStatus(Runnable runnable) {
        if (runnable != null) {
            runnable.run();
        }
    }
}
