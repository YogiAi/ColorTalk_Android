package navyblue.top.colortalk.ui.activities;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.umeng.socialize.UMAuthListener;
import com.umeng.socialize.UMShareAPI;
import com.umeng.socialize.bean.SHARE_MEDIA;

import java.io.File;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import navyblue.top.colortalk.R;
import navyblue.top.colortalk.app.Constants;
import navyblue.top.colortalk.mvp.presenter.abs.ILoginPresenter;
import navyblue.top.colortalk.mvp.presenter.impl.LoginPresenter;
import navyblue.top.colortalk.mvp.view.abs.ILoginView;
import navyblue.top.colortalk.ui.base.BaseLoginActivity;

public class LoginActivity extends BaseLoginActivity
        implements View.OnClickListener, ILoginView {

    private static final String TAG = "LoginActivity";

    @Bind(R.id.vv_login)
    VideoView mVideoView;
    @Bind(R.id.login_progress)
    ProgressBar mLoginProgress;
    @Bind(R.id.txt_email)
    AutoCompleteTextView mTvEmail;
    @Bind(R.id.txt_password)
    EditText mEtPassword;
    @Bind(R.id.txt_forgot)
    TextView mTvForgot;
    @Bind(R.id.txt_create)
    TextView mTvRegister;
    @Bind(R.id.email_sign_in_button)
    Button mBtnSignIn;
    @Bind(R.id.iv_weibo_login)
    ImageView mIvWeiboLogin;
    @Bind(R.id.iv_wechat_login)
    ImageView mIvWechatLogin;
    @Bind(R.id.iv_douban_login)
    ImageView mIvDoubanLogin;
    @Bind(R.id.iv_qq_login)
    ImageView mIvQqLogin;

    private UMShareAPI mShareAPI;
    /** auth callback interface**/
    private UMAuthListener umAuthListener;
    private ILoginPresenter mLoginPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        initStatus();
        initVideo();
        initUmeng();
        setListeners();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mVideoView.stopPlayback();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mShareAPI.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onClick(View view) {
        SHARE_MEDIA platform = null;
        if (view.getId() == R.id.iv_weibo_login){
            platform = SHARE_MEDIA.SINA;
        }else if (view.getId() == R.id.iv_wechat_login){
            platform = SHARE_MEDIA.WEIXIN;
        }else if (view.getId() == R.id.iv_douban_login){
            platform = SHARE_MEDIA.DOUBAN;
        }else if (view.getId() == R.id.iv_qq_login){
            platform = SHARE_MEDIA.QQ;
        }
        /**begin invoke umeng api**/
        mShareAPI.doOauthVerify(LoginActivity.this, platform, umAuthListener);
    }

    @Override
    public void showProcess() {
        mLoginProgress.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideProcess() {
        mLoginProgress.setVisibility(View.GONE);
    }

    @Override
    public void gotoMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onFailure(Throwable e) {

    }

    private void initStatus() {
        mLoginPresenter = new LoginPresenter();
        mLoginPresenter.attachView(this);

        mLoginPresenter.loginCheck();
    }

    private void initUmeng() {
        mShareAPI = UMShareAPI.get(this);

        umAuthListener = new UMAuthListener() {
            @Override
            public void onComplete(SHARE_MEDIA platform, int action, Map<String, String> data) {
                Toast.makeText(LoginActivity.this, "Login succeed", Toast.LENGTH_SHORT).show();
                mLoginPresenter.thirdLogin(data.get("uid"));
            }

            @Override
            public void onError(SHARE_MEDIA platform, int action, Throwable t) {
                Toast.makeText(LoginActivity.this, "Authorize fail", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancel(SHARE_MEDIA platform, int action) {
                Toast.makeText(LoginActivity.this , "Authorize cancel", Toast.LENGTH_SHORT).show();
            }
        };
    }

    private void initVideo() {
        File videoFile = getFileStreamPath(Constants.VIDEO_NAME);
        if (!videoFile.exists()) {
            videoFile = mLoginPresenter.copyVideoFile();
        }

        mVideoView.setVideoPath(videoFile.getPath());
        mVideoView.setLayoutParams(new RelativeLayout.LayoutParams(-1, -1));
        mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                mediaPlayer.setLooping(true);
                mediaPlayer.start();
            }
        });
    }

    private void setListeners() {
        mIvWeiboLogin.setOnClickListener(this);
        mIvDoubanLogin.setOnClickListener(this);
        mIvWechatLogin.setOnClickListener(this);
        mIvQqLogin.setOnClickListener(this);
        mBtnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = mTvEmail.getText().toString();
                String password = mEtPassword.getText().toString();
                // TODO: 邮箱密码为空预先提示
                // TODO: 密码错误或超时提示

                showProcess();
                mLoginPresenter.localLogin(email, password);
            }
        });
    }

}
