package demo.xuqingqi.flipview;

import android.view.View;

/**
 * Created on 2016/10/16.
 */
public interface OnViewChangeListener {

    void onViewPositionChanged(View changedView, int left, int top, int dx, int dy);

    void onViewSelected(int position);

    void onLastViewOverDraged(View changedView, boolean horizontal, int left, int dx);

    void onFirstViewOverDraged(View changedView, boolean horizontal, int left, int dx);

}
