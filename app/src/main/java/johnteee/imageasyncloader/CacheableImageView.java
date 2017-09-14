package johnteee.imageasyncloader;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;

/**
 * Created by teee on 2017/9/14.
 */

public class CacheableImageView extends AppCompatImageView {

    public CacheableImageView(Context context) {
        super(context);
    }

    public CacheableImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CacheableImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setImageDrawable(Drawable drawable) {

        Drawable oldDrawable = getDrawable();

        super.setImageDrawable(drawable);

        notifyDrawable(drawable, true);
        notifyDrawable(oldDrawable, false);
    }

    @Override
    public void setImageResource(int resId) {
        Drawable oldDrawable = getDrawable();

        super.setImageResource(resId);

        notifyDrawable(oldDrawable, false);
    }

    private static void notifyDrawable(Drawable drawable, final boolean isDisplayed) {
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
}
