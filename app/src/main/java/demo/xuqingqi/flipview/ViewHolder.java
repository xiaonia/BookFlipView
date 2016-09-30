package demo.xuqingqi.flipview;

import android.view.View;

/**
 * Created on 2016/9/28.
 */
public abstract class ViewHolder {

    public static final int NO_POSITION = -1;
    public static final long NO_ID = -1;
    public static final int INVALID_TYPE = -1;

    public final View itemView;
    int mPosition = NO_POSITION;
    long mItemId = NO_ID;

    public ViewHolder(View itemView) {
        if (itemView == null) {
            throw new IllegalArgumentException("itemView may not be null");
        }
        this.itemView = itemView;
        this.itemView.setTag(this);
    }

    protected<V> V findView(int id) {
        return (V) itemView.findViewById(id);
    }

}
