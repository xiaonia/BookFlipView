package demo.xuqingqi.flipview;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created on 2016/9/27.
 */
public class BookFlipView extends FlipView {

    private static final String TAG = "BookFlipView";

    private boolean mFlipEnable = true;
    private FlipDragHelper mDragHelper;

    private static final int X_VEL_THRESHOLD = 800;
    private static final int X_DISTANCE_THRESHOLD = 300;

    public BookFlipView(Context context) {
        super(context);
        init();
    }

    public BookFlipView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BookFlipView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public BookFlipView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        mDragHelper = FlipDragHelper.create(this, new DragCallback());
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (!mFlipEnable) {
            return false;
        }

        boolean shouldIntercept = mDragHelper.shouldInterceptTouchEvent(ev);
        if (mDragHelper.getViewDragState() != FlipDragHelper.STATE_SETTLING) {
            mDragHelper.abort();
        }

        return super.onInterceptTouchEvent(ev) || shouldIntercept;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        try {
            mDragHelper.processTouchEvent(event);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return true;
    }

    public class DragCallback extends FlipDragHelper.Callback {

        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            return child != null;
        }

        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            if ((changedView.getRight() <= getBounds().left && dx < 0)
                    || (changedView.getLeft() >= getBounds().left && dx > 0)) {

                requestLayout();
            }
        }

        @Override
        public void onViewReleased(View child, float xvel, float yvel) {
            animToSide(child, xvel, yvel);
        }

        public int getViewHorizontalDragRange(View child) {
            return 1;
        }

        public int getViewVerticalDragRange(View child) {
            return 0;
        }

        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            if (left > getBounds().left) {
                return getBounds().left;
            }
            if (left < getBounds().left - child.getWidth()) {
                return  getBounds().left - child.getWidth();
            }
            return left;
        }

        @Override
        public int clampViewPositionVertical(View child, int top, int dy) {
            return top - dy;
        }

    }

    @Override
    public void computeScroll() {
        if (mDragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    private void animToSide(View changedView, float xvel, float yvel) {
        int finalX = getPaddingLeft();
        int finalY = getPaddingTop();
        int dx = changedView.getLeft() - getPaddingLeft();

        if (xvel > X_VEL_THRESHOLD) {// x正方向的速度足够大，向右滑动
            finalX = getPaddingLeft();
        } else if (xvel < -X_VEL_THRESHOLD) {// x负方向的速度足够大，向左滑动
            finalX = - changedView.getWidth() - getPaddingRight();
        } else if (dx > X_DISTANCE_THRESHOLD) {// x正方向的位移足够大，向右滑动
            finalX = getPaddingLeft();
        } else if (dx < -X_DISTANCE_THRESHOLD) {// x负方向的位移足够大，向左滑动
            finalX = - changedView.getWidth() - getPaddingRight();
        }

        if (mDragHelper.smoothSlideViewTo(changedView, finalX, finalY)) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

}
