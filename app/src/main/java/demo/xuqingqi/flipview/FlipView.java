package demo.xuqingqi.flipview;

import android.annotation.TargetApi;
import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

/**
 * create at most 3 child view
 */
public class FlipView extends ViewGroup {

    private static final String TAG = "FlipView";

    private static final int ELEVATION = 10;
    private static final int DEFAULT_POSITION = 0;

    protected boolean mInLayout;
    protected FlipRecycler mRecycler;
    protected int mFirstVisiablePosition;
    protected FlipAdapter mAdapter;
    private Rect mBounds;
    private DataSetObserver mObserver;
    protected boolean mDataChanged;
    private int mElevation;

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
        mObserver = new FlipDataSetObserver();
        //开启硬件加速，否则阴影效果会失效
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setLayerType(LAYER_TYPE_HARDWARE, null);
        }
    }

    public void setAdapter(FlipAdapter adapter) {
        if (mAdapter != null && mObserver != null) {
            mAdapter.unregisterDataSetObserver(mObserver);
        }
        this.mAdapter = adapter;
        this.mAdapter.registerDataSetObserver(this.mObserver);

        resetList();
        this.mRecycler.setAdapter(this.mAdapter);

        requestLayout();
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (mAdapter == null || mInLayout) {
            return;
        }

        mInLayout = true;
        layoutChildren();
        invalidate();
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
        } else {
            mFirstVisiablePosition = DEFAULT_POSITION;
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

        //removeAllViewsInLayout();
        detachAllViewsFromParent();

        mElevation = 0;
        if (mFirstVisiablePosition >= -1
                && mFirstVisiablePosition < mAdapter.getItemCount() - 1) {

            makeAndAddChild(mFirstVisiablePosition + 1, true, mElevation);
        }

        mElevation += ELEVATION;
        if (mFirstVisiablePosition >= 0) {
            makeAndAddChild(mFirstVisiablePosition, true, mElevation);
        }

        mElevation += ELEVATION;
        if (mFirstVisiablePosition > 0) {
            makeAndAddChild(mFirstVisiablePosition - 1, false, mElevation);
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

    private void makeAndAddChild (int position, boolean isBelow, int elevation) {
        boolean recycled = true;
        ViewHolder viewHolder = mRecycler.getActiveView(position);
        if (viewHolder == null) {
            viewHolder = mRecycler.getRecycleView(position);
            if (viewHolder == null) {
                viewHolder = mAdapter.onCreateViewHolder(this, 0);
            }

            mAdapter.bindViewHolder(viewHolder, position);
            recycled = false;
        }

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

            int childWidthSpec = MeasureSpec.makeMeasureSpec(getBounds().width(), MeasureSpec.EXACTLY);
            int childHeightSpec;
            final int lpHeight = lp.height;
            if (lpHeight > 0) {
                childHeightSpec = MeasureSpec.makeMeasureSpec(lpHeight, MeasureSpec.EXACTLY);
            } else {
                childHeightSpec = MeasureSpec.makeMeasureSpec(getMeasuredHeight(), MeasureSpec.EXACTLY);
            }
            child.measure(childWidthSpec, childHeightSpec);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            child.setElevation(elevation);
        }

        if (isBelow) {
            child.layout(getBounds().left, getBounds().top,
                    getBounds().right, getBounds().bottom);
        } else {
            child.layout(getBounds().left - getBounds().width(), getBounds().top,
                    getBounds().left, getBounds().bottom);
        }
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

    public class FlipDataSetObserver extends DataSetObserver {

        public void onChanged() {

            recycleAllViews();

            requestLayout();
            invalidate();
        }

        public void onInvalidated() {
            // Do nothing
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        recycleAllViews();
    }

    private void resetList() {
        removeAllViewsInLayout();
        mFirstVisiablePosition = 0;
        mDataChanged = false;
    }

    private void recycleAllViews() {
        View view = findFirstVisiableView();
        if (view != null) {
            LayoutParams layoutParams = (LayoutParams) view.getLayoutParams();
            mFirstVisiablePosition = layoutParams.getViewAdapterPosition();
        }

        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            LayoutParams layoutParams = (LayoutParams) child.getLayoutParams();
            ViewHolder viewHolder = layoutParams.mViewHolder;
            mRecycler.setRecycleView(viewHolder);
        }

        if (mFirstVisiablePosition >= mAdapter.getItemCount()
                || mFirstVisiablePosition < 0) {

            mFirstVisiablePosition = 0;
        }
    }

}
