package navyblue.top.colortalk.ui.base;

import android.os.Bundle;
import android.view.View;

import navyblue.top.colortalk.R;
import navyblue.top.colortalk.ui.widgets.MultiSwipeRefreshLayout;

public class SwipeRefreshFragment extends BaseFragment {

    public MultiSwipeRefreshLayout mSwipeRefreshLayout;


    @Override public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        trySetupSwipeRefresh(view);
    }


    void trySetupSwipeRefresh(View root) {
        mSwipeRefreshLayout = (MultiSwipeRefreshLayout) root.findViewById(
                R.id.swipe_refresh_layout);
        if (mSwipeRefreshLayout != null) {
            mSwipeRefreshLayout.setColorSchemeResources(R.color.blue,
                    R.color.red, R.color.purple);
            mSwipeRefreshLayout.setOnRefreshListener(() -> requestDataRefresh());
        }
    }


    public void requestDataRefresh() {
    }


    public void setRefreshing(boolean refreshing) {
        if (mSwipeRefreshLayout == null) {
            return;
        }
        if (!refreshing) {
            // 防止刷新消失太快，让子弹飞一会儿
            mSwipeRefreshLayout.postDelayed(
                    () -> mSwipeRefreshLayout.setRefreshing(false), 1000);
        }
        else {
            mSwipeRefreshLayout.setRefreshing(true);
        }
    }


    public void setProgressViewOffset(boolean scale, int start, int end) {
        mSwipeRefreshLayout.setProgressViewOffset(scale, start, end);
    }


    public void setSwipeableChildren(MultiSwipeRefreshLayout.CanChildScrollUpCallback canChildScrollUpCallback) {
        mSwipeRefreshLayout.setCanChildScrollUpCallback(canChildScrollUpCallback);
    }
}
