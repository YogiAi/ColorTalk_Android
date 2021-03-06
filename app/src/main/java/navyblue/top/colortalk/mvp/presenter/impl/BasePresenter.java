
package navyblue.top.colortalk.mvp.presenter.impl;

import android.app.Activity;

import navyblue.top.colortalk.mvp.presenter.abs.IBasePresenter;
import navyblue.top.colortalk.mvp.view.abs.IBaseView;
import navyblue.top.colortalk.rest.ServiceFactory;
import navyblue.top.colortalk.rest.services.ColorTalkService;
import rx.Subscription;
import rx.subscriptions.CompositeSubscription;

/**
 * Description：BasePresenter
 * <p>
 * Base class that implements the Presenter interface and provides a base implementation for
 * attachView() and detachView(). It also handles keeping a reference to the mvpView that
 * can be accessed from the children classes by calling getMvpView().
 * <p>
 * Created by：Yogi Ai
 */
public class BasePresenter<T extends IBaseView> implements IBasePresenter<T> {
    public static final ColorTalkService sColorTalkService = ServiceFactory.getColorTalkSingleton();

    protected T mBaseView;
    public CompositeSubscription mCompositeSubscription;
    protected Activity mActivity;
//    public DataManager mDataManager;


    @Override
    public void attachView(T mvpView) {
        this.mBaseView = mvpView;
        this.mCompositeSubscription = new CompositeSubscription();
        if(mvpView instanceof  Activity){
            this.mActivity = (Activity) mvpView;
        }
//        this.mDataManager = DataManager.getInstance();
    }

    @Override
    public void attachView(T baseView, Activity activity) {
        this.mBaseView = baseView;
        this.mCompositeSubscription = new CompositeSubscription();
        mActivity = activity;
    }

    @Override
    public void detachView() {
        this.mBaseView = null;
        this.mCompositeSubscription.unsubscribe();
        this.mCompositeSubscription = null;
        this.mActivity = null;
//        this.mDataManager = null;
    }

    public boolean isViewAttached() {
        return mBaseView != null;
    }

    public T getBaseView() {
        return mBaseView;
    }

    public void checkViewAttached() {
        if (!isViewAttached()) throw new MvpViewNotAttachedException();
    }

    public static class MvpViewNotAttachedException extends RuntimeException {
        public MvpViewNotAttachedException() {
            super("Please call Presenter.attachView(MvpView) before" +
                    " requesting data to the Presenter");
        }
    }

    public void addSubscription(Subscription s) {
        if (this.mCompositeSubscription == null) {
            this.mCompositeSubscription = new CompositeSubscription();
        }

        this.mCompositeSubscription.add(s);
    }

}
