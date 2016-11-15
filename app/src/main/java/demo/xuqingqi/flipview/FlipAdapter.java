package demo.xuqingqi.flipview;

import android.database.DataSetObservable;
import android.database.DataSetObserver;
import android.view.ViewGroup;

/**
 * Created on 2016/9/27.
 */
public abstract class FlipAdapter<VH extends ViewHolder> {

    private final DataSetObservable mObservable = new DataSetObservable();

    public long getItemId(int position) {
        return ViewHolder.NO_ID;
    }

    public abstract int getItemCount();

    public abstract Object getItem(int position);

    public abstract boolean hasStableIds();

    public abstract VH onCreateViewHolder(ViewGroup parent, int viewType);

    public abstract void onBindViewHolder(VH holder, int position);

    public final VH createViewHolder(ViewGroup parent, int viewType) {
        final VH holder = onCreateViewHolder(parent, viewType);
        return holder;
    }

    public final void bindViewHolder(VH holder, int position) {
        holder.mPosition = position;
        holder.mItemId = getItemId(position);

        onBindViewHolder(holder, position);
    }

    public void notifyDataSetChanged() {
        mObservable.notifyChanged();
    }

    void registerDataSetObserver(DataSetObserver observer) {
        this.mObservable.registerObserver(observer);
    }

    void unregisterDataSetObserver(DataSetObserver observer){
        this.mObservable.unregisterObserver(observer);
    }

}

