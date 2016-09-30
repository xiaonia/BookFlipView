package demo.xuqingqi.flipview;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

/**
 * create at most 3 child view
 */
public class FlipView extends ViewGroup {

    private static final String TAG = "FlipView";
    protected boolean mInLayout;
    protected FlipRecycler mRecycler;
    protected int mFirstVisiablePosition;
    protected FlipAdapter mAdapter;
    private Rect mBounds;

    public FlipView(Context context) {
        super(context);
        initFlipView();
    }

    public FlipView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initFlipView();
    }

    public FlipView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initFlipView();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public FlipView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initFlipView();
    }

    private void initFlipView () {
        mRecycler = new FlipRecycler();
        mBounds = new Rect();
    }

    public void setAdapter(FlipAdapter adapter) {
        this.mAdapter = adapter;
        mRecycler.setAdapter(adapter);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        mInLayout = true;

        final int childCount = getChildCount();
        if (changed) {
            for (int i = 0; i < childCount; i++) {
                getChildAt(i).forceLayout();
            }
        }

        invalidate();
        layoutChildren();
        mInLayout = false;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    protected void layoutChildren(){
        View view = findFirstVisiableView();
        if (view != null) {
            LayoutParams layoutParams = (LayoutParams) view.getLayoutParams();
            mFirstVisiablePosition = layoutParams.getViewAdapterPosition();
        }

        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            LayoutParams layoutParams = (LayoutParams) child.getLayoutParams();
            ViewHolder viewHolder = layoutParams.mViewHolder;
            if (viewHolder.mPosition > mFirstVisiablePosition + 1
                    || viewHolder.mPosition < mFirstVisiablePosition - 1) {

                mRecycler.setRecycleView(viewHolder);
            } else {

                mRecycler.setActiveView(viewHolder);
            }
        }

        detachAllViewsFromParent();

        if (mFirstVisiablePosition < mAdapter.getItemCount() - 1) {
            makeAndAddChild(mFirstVisiablePosition + 1, true);
        }

        makeAndAddChild(mFirstVisiablePosition, true);

        if (mFirstVisiablePosition > 0) {
            makeAndAddChild(mFirstVisiablePosition - 1, false);
        }
    }

    public View findFirstVisiableView () {
        for (int i = getChildCount() - 1; i >= 0; i--) {
            View child = getChildAt(i);
            if (child.getVisibility() == VISIBLE) {
                if (child.getRight() <= getBounds().left
                        || child.getLeft() >= getBounds().right) {

                    continue;
                }

                return child;
            }
        }
        return null;
    }

    public View findLastVisiableView () {
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child.getVisibility() == VISIBLE) {
                if (child.getLeft() < getBounds().left) {
                    return child;
                }
            }
        }
        return null;
    }

    private void makeAndAddChild (int position, boolean isBelow) {
        boolean recycled = true;
        boolean needToMeasure = false;
        ViewHolder viewHolder = mRecycler.getActiveView(position);
        if (viewHolder == null) {
            viewHolder = mRecycler.getRecycleView(position);
            if (viewHolder == null) {
                viewHolder = mAdapter.onCreateViewHolder(this, 0);
                needToMeasure = true;
            }
            recycled = false;
        }
        mAdapter.bindViewHolder(viewHolder, position);

        View child = viewHolder.itemView;
        final ViewGroup.LayoutParams vlp = child.getLayoutParams();
        LayoutParams lp;
        if (vlp == null) {
            lp = generateDefaultLayoutParams();
        } else if (!checkLayoutParams(vlp)) {
            lp = generateLayoutParams(vlp);
        } else {
            lp = (LayoutParams) vlp;
        }
        lp.mViewHolder = viewHolder;
        if (lp != vlp) {
            child.setLayoutParams(lp);
        }

        if (recycled) {
            attachViewToParent(child, -1, lp);
        } else {
            addViewInLayout(child, -1, lp);

            if (needToMeasure) {
                final int childWidthSpec = MeasureSpec.makeMeasureSpec(getBounds().width(), MeasureSpec.EXACTLY);
                final int lpHeight = lp.height;
                final int childHeightSpec;
                if (lpHeight > 0) {
                    childHeightSpec = MeasureSpec.makeMeasureSpec(lpHeight, MeasureSpec.EXACTLY);
                } else {
                    childHeightSpec = MeasureSpec.makeMeasureSpec(getMeasuredHeight(), MeasureSpec.EXACTLY);
                }
                child.measure(childWidthSpec, childHeightSpec);
            }

            if (isBelow) {
                child.layout(getBounds().left, getBounds().top,
                        getBounds().right, getBounds().bottom);
            } else {
                child.layout(getBounds().left - getBounds().width(), getBounds().top,
                        getBounds().left, getBounds().bottom);
            }
        }

        Log.e(TAG, "child count is " + getChildCount());
    }

    public Rect getBounds() {
        mBounds.set(getPaddingLeft(), getPaddingTop(),
                getWidth() - getPaddingRight(), getHeight() - getPaddingBottom());
        return mBounds;
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
    }

    @Override
    protected LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new LayoutParams(p);
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams;
    }

    public static class LayoutParams extends MarginLayoutParams {

        ViewHolder mViewHolder;

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(MarginLayoutParams source) {
            super(source);
        }

        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }

        public LayoutParams(LayoutParams source) {
            super((ViewGroup.LayoutParams) source);
        }

        public int getViewAdapterPosition() {
            return mViewHolder.mPosition;
        }

    }

}
