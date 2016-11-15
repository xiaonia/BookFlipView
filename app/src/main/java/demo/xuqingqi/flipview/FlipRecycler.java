package demo.xuqingqi.flipview;

import java.util.ArrayList;
import java.util.List;

/**
 * Created on 2016/9/28.
 */
public class FlipRecycler {

    private List<ViewHolder> mActiveViews;
    private List<ViewHolder> mRecycleViews;
    private FlipAdapter mAdapter;

    public FlipRecycler () {
        mRecycleViews = new ArrayList<>();
        mActiveViews = new ArrayList<>();
    }

    public FlipRecycler setAdapter(FlipAdapter adapter) {
        this.mAdapter = adapter;
        this.clear();
        return this;
    }

    public ViewHolder getActiveView(int position) {
        if (mActiveViews.size() > 0) {
            for (int i = 0; i < mActiveViews.size(); i++) {
                ViewHolder viewHolder = mActiveViews.get(i);
                if (mAdapter != null) {
                    if (mAdapter.hasStableIds()) {
                        if (viewHolder.mItemId == mAdapter.getItemId(position)) {
                            mActiveViews.remove(viewHolder);
                            return viewHolder;
                        }
                    }
                }
                if (viewHolder.mPosition == position) {
                    mActiveViews.remove(viewHolder);
                    return viewHolder;
                }
            }
        }
        return null;
    }


    public ViewHolder getRecycleView(int position) {
        if (mRecycleViews.size() > 0) {
            for (int i = 0; i < mRecycleViews.size(); i++) {
                ViewHolder viewHolder = mRecycleViews.get(i);
                if (mAdapter != null) {
                    if (mAdapter.hasStableIds()) {
                        if (viewHolder.mItemId == mAdapter.getItemId(position)) {
                            mRecycleViews.remove(viewHolder);
                            return viewHolder;
                        }
                    }
                }
                if (viewHolder.mPosition == position) {
                    mRecycleViews.remove(viewHolder);
                    return viewHolder;
                }
            }
            return mRecycleViews.remove(0);
        }
        return null;
    }

    public void setRecycleView(ViewHolder viewHolder) {
        if (viewHolder != null) {
            if (mRecycleViews.contains(viewHolder)) {
                mRecycleViews.remove(viewHolder);
            }
            mRecycleViews.add(viewHolder);
        }
    }

    public void setActiveView(ViewHolder viewHolder) {
        if (viewHolder != null) {
            if (mActiveViews.contains(viewHolder)) {
                mActiveViews.remove(viewHolder);
            }
            mActiveViews.add(viewHolder);
        }
    }

    public void clear() {
        this.mActiveViews.clear();
        this.mRecycleViews.clear();
    }

}
