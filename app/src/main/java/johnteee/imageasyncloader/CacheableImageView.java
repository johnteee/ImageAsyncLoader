package johnteee.imageasyncloader;

import android.content.Context;
import android.graphics.drawable.Drawable;
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

        CacheableBitmapDrawable.notifyDrawable(drawable, true);
        CacheableBitmapDrawable.notifyDrawable(oldDrawable, false);
    }

    @Override
    public void setImageResource(int resId) {
        Drawable oldDrawable = getDrawable();

        super.setImageResource(resId);

        CacheableBitmapDrawable.notifyDrawable(oldDrawable, false);
    }
}
