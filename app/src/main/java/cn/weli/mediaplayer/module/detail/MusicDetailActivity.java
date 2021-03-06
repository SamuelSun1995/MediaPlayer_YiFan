package cn.weli.mediaplayer.module.detail;


import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.weli.mediaplayer.R;
import cn.weli.mediaplayer.base.BaseActivity;
import cn.weli.mediaplayer.constant.SongsConstant;
import cn.weli.mediaplayer.helper.MediaHelper;
import cn.weli.mediaplayer.manager.CallbackListener;
import cn.weli.mediaplayer.manager.MediaManager;
import cn.weli.mediaplayer.utils.ImageUtil;
import cn.weli.mediaplayer.utils.ThreadPoolUtil;


public class MusicDetailActivity extends BaseActivity<MusicDetailPresenter, IMusicDetailView> implements IMusicDetailView, CallbackListener, View.OnClickListener {

    private MediaManager mMediaManager;
    private boolean hasInitThread = false;
    private Animation animation;

    @BindView(R.id.detail_back)
    ImageView mBackImg;
    @BindView(R.id.detail_img)
    ImageView mDetailImg;
    @BindView(R.id.detail_song_name)
    TextView mSongName;
    @BindView(R.id.detail_singer)
    TextView mSinger;
    @BindView(R.id.detail_img_fav)
    ImageView mFavImg;
    @BindView(R.id.detail_lastsong)
    ImageView mLastImg;
    @BindView(R.id.detail_status_play)
    ImageView mPlayImg;
    @BindView(R.id.detail_status_pause)
    ImageView mPauseImg;
    @BindView(R.id.detail_nextsong)
    ImageView mNextImg;
    @BindView(R.id.detail_progress)
    ProgressBar mProgress;


    private Handler sHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 0x123) {
                mProgress.setProgress((Integer) msg.obj);
            }
        }
    };


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bindView();
        //?????????
        initMediaManager();
        initData();
        setListener();
        //???????????????
        initAnim();
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setData();
    }

    private void initData() {
        setData();
    }

    /**
     * ????????????
     */
    private void initMediaManager() {
        mMediaManager = MediaManager.getInstance();
        mMediaManager.registCallbackListener(this);
    }


    /**
     * ???????????????
     */
    private void initAnim() {
        animation = AnimationUtils.loadAnimation(this, R.anim.rotate);
        LinearInterpolator interpolator = new LinearInterpolator();     //???????????????
        animation.setInterpolator(interpolator);
        if (animation != null) {
            mDetailImg.startAnimation(animation);
        }
    }


    private void setListener() {

        mBackImg.setOnClickListener(this);
        mFavImg.setOnClickListener(this);
        mLastImg.setOnClickListener(this);
        mPlayImg.setOnClickListener(this);
        mPauseImg.setOnClickListener(this);
        mNextImg.setOnClickListener(this);

    }

    /**
     * ????????????
     */
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.detail_back:      //??????
                finishActivity();
                break;
            case R.id.detail_img_fav:       //??????????????????????????????????????????
                mPresenter.dealFavItemClick();
                break;
            case R.id.detail_lastsong:
                mMediaManager.last();
                break;
            case R.id.detail_status_play:
                mMediaManager.play(MediaHelper.isPlaySongData);
                break;
            case R.id.detail_status_pause:
                mMediaManager.pause();
                break;
            case R.id.detail_nextsong:
                mMediaManager.next();
                break;
        }
    }

    /**
     * ?????????????????????????????????
     */
    private void setData() {

        if (mMediaManager.mMediaPlayer.isPlaying()) {
            //????????????
            mPlayImg.setVisibility(View.GONE);
            mPauseImg.setVisibility(View.VISIBLE);
        } else {
            //????????????
            mPlayImg.setVisibility(View.VISIBLE);
            mPauseImg.setVisibility(View.GONE);
        }

        mProgress.setMax(MediaHelper.isPlaySongData.duration);

        if (MediaHelper.isPlaySongData.isFavorite == SongsConstant.IS_FAV) {
            mFavImg.setImageResource(R.drawable.hasfav);
        } else if (MediaHelper.isPlaySongData.isFavorite == SongsConstant.NOT_FAV) {
            mFavImg.setImageResource(R.drawable.wait_fav);
        }
        mSongName.setText(MediaHelper.isPlaySongData.songName);
        mSinger.setText(MediaHelper.isPlaySongData.singer);
        if (MediaHelper.isPlaySongData != null) {
            mDetailImg.setImageBitmap(ImageUtil.makeRoundCorner(MediaHelper.getSongAlbumBitmap(MediaHelper.isPlaySongData.songMediaId
                    , MediaHelper.isPlaySongData.albumId)));
        }
        //?????????????????????
        initProgressThread();
    }

    /**
     * ??????????????????
     */
    @Override
    public void setIsOrNotFav() {
        if (MediaHelper.isPlaySongData.isFavorite == SongsConstant.IS_FAV) {
            mFavImg.setImageResource(R.drawable.hasfav);
        } else if (MediaHelper.isPlaySongData.isFavorite == SongsConstant.NOT_FAV) {
            mFavImg.setImageResource(R.drawable.wait_fav);
        }
    }


    @Override
    public void play(int oldId, int nowId) {
        if (animation != null) {
            mDetailImg.startAnimation(animation);
        }

        setData();
    }

    @Override
    public void pause(int id) {
        if (animation != null) {
            mDetailImg.clearAnimation();
        }
        setData();
    }

    @Override
    public void stop(int id) {
        if (animation != null) {
            mDetailImg.clearAnimation();
        }
        setData();
    }

    /**
     * ??????????????????????????????
     */
    private void initProgressThread() {

        if (!hasInitThread) {
            ThreadPoolUtil.getInstance().execute(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        int currentPosition = mMediaManager.mMediaPlayer.getCurrentPosition();
                        try {
                            Thread.sleep(300);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        Message message = new Message();
                        message.what = 0x123;
                        message.obj = currentPosition;
                        sHandler.sendMessage(message);
                    }
                }
            });
            hasInitThread = true;
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected int getLayout() {
        return R.layout.music_detail_layout;
    }

    @Override
    protected void bindView() {
        ButterKnife.bind(this);
    }

    @Override
    protected Class<IMusicDetailView> getViewClass() {
        return IMusicDetailView.class;
    }

    @Override
    protected Class<MusicDetailPresenter> getPresenterClass() {
        return MusicDetailPresenter.class;
    }
}
