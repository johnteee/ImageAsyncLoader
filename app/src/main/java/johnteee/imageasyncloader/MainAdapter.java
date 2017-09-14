package johnteee.imageasyncloader;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by teee on 2017/9/11.
 */

public class MainAdapter extends RecyclerView.Adapter<VH> {

    private final Context context;
    private final ArrayList<Integer> resList;
    private final ImageAsyncHelper imageAsyncHelper;

    private int lastPosition;

    MainAdapter(Context context, ImageAsyncHelper imageAsyncHelper) {
        this.context = context;

        this.imageAsyncHelper = imageAsyncHelper;

        ArrayList<Integer> drawableList = new ArrayList<>();
        for (int i = 67; i <= 96; i++) {
            int drawableId = context.getResources().getIdentifier("imag11" + i, "drawable", "johnteee.imageasyncloader");
            drawableList.add(drawableId);
        }

        resList = drawableList;
    }

    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        return new VH(VH.generateView(context));
    }

    @Override
    public void onBindViewHolder(VH holder, int position) {
//        holder.image.setImageBitmap(ImageUtils.decodeSampledBitmapFromResource(context.getResources(), resList.get(position), 100, 100));

        Integer resId = resList.get(position);
        imageAsyncHelper.loadImageResAsync(context, holder.image, resId, 100, 100);
        holder.title.setText(context.getResources().getResourceName(resId));
        holder.description.setText("Description of " + context.getResources().getResourceName(resId));

        setAnimation(holder, position);
    }

    @Override
    public int getItemCount() {
        return resList.size();
    }

    @Override
    public void onViewDetachedFromWindow(VH holder)
    {
        holder.clearAnimation();
    }

    private void setAnimation(VH holder, int position)
    {
        View view = holder.itemView;

        // If the bound view wasn't previously displayed on screen, it's animated
        if (position > lastPosition)
        {
            view.clearAnimation();
            Animation animation = AnimationUtils.loadAnimation(context, android.R.anim.slide_in_left);
            view.startAnimation(animation);
            lastPosition = position;
        }
    }
}

class VH extends RecyclerView.ViewHolder {

    public CacheableImageView image;
    public TextView title;
    public TextView description;

    public VH(View itemView) {
        super(itemView);

        image = itemView.findViewById(R.id.image);
        title = itemView.findViewById(R.id.title);
        description = itemView.findViewById(R.id.description);
    }

    public void clearAnimation() {
        if (itemView != null) {
            itemView.clearAnimation();
        }
    }

    public static View generateView(Context context) {
        return View.inflate(context, R.layout.view_item, null);
    }
}