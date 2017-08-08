package android.support.v7.widget;

/**
 * Created by mariotaku on 2016/12/5.
 */

public class RecyclerViewAccessor {
    public static void setLayoutPosition(RecyclerView.ViewHolder holder, int position) {
        holder.mPreLayoutPosition = position;
    }
}
