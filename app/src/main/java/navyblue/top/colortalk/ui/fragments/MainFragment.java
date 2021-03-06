package navyblue.top.colortalk.ui.fragments;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
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
import io.rong.imkit.RongIM;
import navyblue.top.colortalk.R;
import navyblue.top.colortalk.app.ColorTalkApp;
import navyblue.top.colortalk.mvp.models.Image;
import navyblue.top.colortalk.mvp.models.Moment;
import navyblue.top.colortalk.mvp.presenter.abs.IMainPresenter;
import navyblue.top.colortalk.mvp.presenter.impl.MainPresenter;
import navyblue.top.colortalk.mvp.view.abs.IMainView;
import navyblue.top.colortalk.rest.ServiceFactory;
import navyblue.top.colortalk.rest.models.UserInfo;
import navyblue.top.colortalk.ui.adapters.MomentAdapter;
import navyblue.top.colortalk.ui.base.SwipeRefreshFragment;
import navyblue.top.colortalk.ui.listeners.OnMomentListener;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by CIR on 16/1/19.
 */
public class MainFragment extends SwipeRefreshFragment implements IMainView {

    @Bind(R.id.rv_meizhi)
    RecyclerView mRecyclerView;
    private IMainPresenter mMainPresenter;
    private MomentAdapter mMomentAdapter;
    private List<Moment> mMomentList;
    private boolean mIsFirstTimeTouchBottom = true;
    private int mPage = 1;
    private boolean mMomentBeTouched;
    private static final int PRELOAD_SIZE = 6;

    public static MainFragment newInstance() {
        return new MainFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflateAndBind(inflater, container, R.layout.fragment_main);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mMainPresenter = new MainPresenter();
        mMainPresenter.attachView(this, mActivity);

        ServiceFactory.getColorTalkSingleton().getUserInfo(ColorTalkApp.getUserID())
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<UserInfo>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(final UserInfo userInfo) {
                        RongIM.setUserInfoProvider(new RongIM.UserInfoProvider() {

                            @Override
                            public io.rong.imlib.model.UserInfo getUserInfo(String userId) {
                                return new io.rong.imlib.model.UserInfo(String.valueOf(userInfo.getUserId()), userInfo.getNickname(), Uri.parse(userInfo.getIconUrl()));
                            }

                        }, true);
                    }
                });

        mMomentList = new ArrayList<>();
        setupRecyclerView();
    }

    @Override
    public void onStart() {
        super.onStart();

        if(mMomentList.size() == 0){
            mPage = 1;
            new Handler().postDelayed(() -> setRefreshing(true), 358);
            requestDataRefresh();
        }

        if(ColorTalkApp.postedImage){
            ColorTalkApp.postedImage = false;
            mPage = 1;
            mMomentList.clear();
            new Handler().postDelayed(() -> setRefreshing(true), 358);
            requestDataRefresh();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();

        mMainPresenter.detachView();
        mMainPresenter = null;
    }

    @Override
    public void requestDataRefresh() {
        super.requestDataRefresh();
        mMainPresenter.loadMoments(true, mPage);
    }

    @Override
    public void loadNextSuccess(List<Moment> moments) {
        mPage++;
        mMomentList.addAll(moments);
        mMomentAdapter.notifyDataSetChanged();
        setRefreshing(false);
    }

    @Override
    public void loadNextFailed() {
        setRefreshing(false);
        // TODO: 换个更合理的提示
//        Toast.makeText(mActivity, "No more moments for now!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onFailure(Throwable e) {

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
                        mMainPresenter.loadMoments(false, mPage);
                    } else {
                        mIsFirstTimeTouchBottom = false;
                    }
                }
            }
        };
    }

    private OnMomentListener getOnMomentTouchListener() {
        return (v, imageView, card, moment, userIconImage) -> {
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
            } else if (v == userIconImage) {
                ServiceFactory.getColorTalkSingleton().getUserInfo(moment.getUserId())
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Subscriber<UserInfo>() {
                            @Override
                            public void onCompleted() {

                            }

                            @Override
                            public void onError(Throwable e) {

                            }

                            @Override
                            public void onNext(UserInfo userInfo) {
                                RongIM.setUserInfoProvider(new RongIM.UserInfoProvider() {

                                    @Override
                                    public io.rong.imlib.model.UserInfo getUserInfo(String userId) {

                                        return new io.rong.imlib.model.UserInfo(String.valueOf(userInfo.getUserId()), userInfo.getNickname(), Uri.parse(userInfo.getIconUrl()));
                                    }

                                }, true);

                                if (RongIM.getInstance() == null){
                                    return ;
                                }
                                RongIM.getInstance().startPrivateChat(mActivity, String.valueOf(moment.getUserId()), userInfo.getNickname());
                            }
                        });
            }
        };
    }
}
