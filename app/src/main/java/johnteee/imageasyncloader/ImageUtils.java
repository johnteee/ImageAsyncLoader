package johnteee.imageasyncloader;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;

/**
 * Created by teee on 2017/9/12.
 */

public class ImageUtils {

    public static BitmapFactory.Options getImageResDimension(Resources resources, int resId) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(resources, resId, options);
        int imageHeight = options.outHeight;
        int imageWidth = options.outWidth;
        String imageType = options.outMimeType;

        return options;
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId,
                                                         int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }

    public static void recycleDrawable(Drawable drawable) {
        synchronized (drawable) {
            if (drawable instanceof BitmapDrawable) {
                BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
                Bitmap bitmap = bitmapDrawable.getBitmap();
                if (bitmap != null) {
                    recycleBitmap(bitmap);
                }
            }
        }
    }

    public static void recycleBitmap(Bitmap bitmap) {
        synchronized (bitmap) {
            if (! bitmap.isRecycled()) {
                bitmap.recycle();
                System.gc();
            }
        }
    }

    public static boolean isBitmapDrawableEmptyOrRecycled(BitmapDrawable bitmapDrawable) {
        return bitmapDrawable == null || bitmapDrawable.getBitmap().isRecycled();
    }

    public static int getBitmapByteCount(Bitmap bitmap) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB_MR1)
            return bitmap.getRowBytes() * bitmap.getHeight();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT)
            return bitmap.getByteCount();
        return bitmap.getAllocationByteCount();
    }

}
