package navyblue.top.colortalk.ui.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;
import navyblue.top.colortalk.R;
import navyblue.top.colortalk.mvp.models.Image;
import navyblue.top.colortalk.mvp.models.Moment;
import navyblue.top.colortalk.mvp.presenter.abs.IMainPresenter;
import navyblue.top.colortalk.mvp.presenter.impl.MainPresenter;
import navyblue.top.colortalk.mvp.view.abs.IMainView;
import navyblue.top.colortalk.ui.activities.MomentPostActivity;
import navyblue.top.colortalk.ui.adapters.MomentAdapter;
import navyblue.top.colortalk.ui.base.SwipeRefreshFragment;
import navyblue.top.colortalk.ui.listeners.OnMomentListener;

/**
 * Created by CIR on 16/1/19.
 */
public class MainFragment extends SwipeRefreshFragment implements IMainView {

    Activity mActivity;
    @Bind(R.id.rv_meizhi)
    RecyclerView mRecyclerView;
    private IMainPresenter mMainPresenter;
    private MomentAdapter mMomentAdapter;
    private List<Moment> mMomentList;
    private boolean mIsFirstTimeTouchBottom = true;
    private int mPage = 1;
    private boolean mMomentBeTouched;
    private static final int PRELOAD_SIZE = 6;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mMainPresenter = new MainPresenter();
        mMainPresenter.attachView(this);
        mMomentList = new ArrayList<>();

        setupRecyclerView();

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    private void setupRecyclerView() {
        final StaggeredGridLayoutManager layoutManager
                = new StaggeredGridLayoutManager(2,
                StaggeredGridLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);
        mMomentAdapter = new MomentAdapter(mActivity, mMomentList);
        mRecyclerView.setAdapter(mMomentAdapter);
        mRecyclerView.addOnScrollListener(getOnBottomListener(layoutManager));
        mMomentAdapter.setOnMomentClickListener(getOnMomentTouchListener());
    }


    RecyclerView.OnScrollListener getOnBottomListener(final StaggeredGridLayoutManager layoutManager) {
        return new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView rv, int dx, int dy) {
                boolean isBottom =
                        layoutManager.findLastCompletelyVisibleItemPositions(
                                new int[2])[1] >=
                                mMomentAdapter.getItemCount() -
                                        PRELOAD_SIZE;
                if (!mSwipeRefreshLayout.isRefreshing() && isBottom) {
                    if (!mIsFirstTimeTouchBottom) {
                        mSwipeRefreshLayout.setRefreshing(true);
                        mPage += 1;
                        mMainPresenter.loadMoments(false);
                    } else {
                        mIsFirstTimeTouchBottom = false;
                    }
                }
            }
        };
    }

    private OnMomentListener getOnMomentTouchListener() {
        return (v, imageView, card, moment) -> {
            Image image = moment.getImage();

            if (moment == null) return;
            if (v == imageView && !mMomentBeTouched) {
                mMomentBeTouched = true;
                Picasso.with(mActivity).load(image.getImageUrl()).fetch(new Callback() {

                    @Override
                    public void onSuccess() {
                        mMomentBeTouched = false;
                        mMainPresenter.showPicture(moment, imageView);
                    }

                    @Override
                    public void onError() {
                        mMomentBeTouched = false;
                    }
                });
            } else if (v == card) {
                Picasso.with(mActivity).load(image.getImageUrl()).fetch(new Callback() {

                    @Override
                    public void onSuccess() {
                        mMainPresenter.showMoment(moment, imageView);
                    }

                    @Override
                    public void onError() {
                        mMomentBeTouched = false;
                    }
                });
            }
        };
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = (Activity) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mActivity = null;
    }

    @Override
    public void requestDataRefresh() {
        super.requestDataRefresh();
        mPage = 1;
        mMainPresenter.loadMoments(true);
    }

    @OnClick(R.id.main_fab)
    public void onFab(View v) {
        Intent intent = new Intent(mActivity, MomentPostActivity.class);
        startActivity(intent);
    }

    @Override
    public void loadNextSuccess(List<Moment> moments) {
        mMomentList.addAll(moments);
        mMomentAdapter.notifyDataSetChanged();
        setRefreshing(false);
    }

    @Override
    public void onFailure(Throwable e) {

    }
}