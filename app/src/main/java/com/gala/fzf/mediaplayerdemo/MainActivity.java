package com.gala.fzf.mediaplayerdemo;

import android.app.Service;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.VideoView;


import com.tvguo.gala.PSConfigInfo;
import com.tvguo.gala.PSServiceManager;

import java.io.ByteArrayOutputStream;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

public class MainActivity extends AppCompatActivity implements PSListenerImpl.PushScreenCallback {
    private static final String TAG = MainActivity.class.getSimpleName();
    public static final int QIMO_VIDEO_PUSH = 0x000001, QIMO_PICTURE_PUSH = 0x000002, QIMO_NETVIDEO_PUSH = 0x000008, DEVICE_RENAME = 0x000010,
            DEVICE_UPDATE_CHECK = 0x000020, FEEDBACK = 0x000040, EARPHONE = 0x000080, SUBTITLE = 0x000100, CEC = 0x000200, IGNORE_WIFI = 0x000400,
            HDMI_OUTPUT_ZOOM = 0x000800, REMOTE_FORBID = 0x001000, DEVICE_REBOOT = 0x002000, REMOTE_FORBID_INDIVIDUAL = 0x004000, SCREEN_CAPTURE =
            0x008000, QIMO_OFFLINE = 0x010000, MIRROR_QUALITY = 0x020000, PICTURE_ZOOM = 0x040000, PLAYBACK_SPEED = 0x080000, DELAY_EXIT_VIDEO =
            0x100000, WIFI_DISPLAY = 0x200000, OFFLINE_CACHE = 0x400000;

    private Button mStart;
    private Button mStop;
    private TextView mQimoState;
    private TextView mAirplayState;
    private Button mPlayback;
    private Button mExit;
    private VideoView mPlayer;
    private AudioManager audioManager;
    private TextView mIMState;

    private int volumeMax = 0;
    private float rate = 0;
    private String mFeature = String.valueOf(QIMO_VIDEO_PUSH + QIMO_PICTURE_PUSH + QIMO_NETVIDEO_PUSH + DEVICE_RENAME + DEVICE_UPDATE_CHECK + FEEDBACK + EARPHONE + SUBTITLE + CEC + IGNORE_WIFI + HDMI_OUTPUT_ZOOM + REMOTE_FORBID + DEVICE_REBOOT + REMOTE_FORBID_INDIVIDUAL + SCREEN_CAPTURE + QIMO_OFFLINE + MIRROR_QUALITY + PICTURE_ZOOM + PLAYBACK_SPEED + DELAY_EXIT_VIDEO + WIFI_DISPLAY + OFFLINE_CACHE);
    private int curposition = 0;
    private PlayerCallback mPlayerCallback = null;
    private StatusReceiver mStatusReceiver;
    private Button mInit;
    private Button mClear;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initService();
        initReceiver();
        audioManager = (AudioManager) getSystemService(Service.AUDIO_SERVICE);
        if (audioManager != null) {
            volumeMax = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        }

        mStart = findViewById(R.id.btn_start);
        mStop = findViewById(R.id.btn_end);
        mInit = findViewById(R.id.btn_init);
        mClear = findViewById(R.id.btn_clear);
        mQimoState = findViewById(R.id.qimo_state);
        mAirplayState = findViewById(R.id.airplay_state);
        mIMState = findViewById(R.id.im_state);
        createPlayer();

        mStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PSServiceManager.getInstance().startService();
            }
        });
        mStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PSServiceManager.getInstance().stopService();
            }
        });
        mInit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initService();
            }
        });
        mClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PSServiceManager.getInstance().clear();
            }
        });
        mStart.performClick();
    }

    private void initReceiver() {
        mStatusReceiver = new StatusReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mStatusReceiver, filter);
    }

    private void initService() {
        final PSConfigInfo configInfo = new PSConfigInfo();
        configInfo.cachePath = getCacheDir().getAbsolutePath() + "/airplay";
        configInfo.deviceId = "QD10011027718A100008";
        configInfo.uuid = "707a-a167-1526-e144";
        configInfo.deviceName = "LeaveMeAlone";
        configInfo.hardOper = 0;
        configInfo.hardVersion = "4";
        configInfo.targetInterface = "wlan0";
        configInfo.appVersion = "521035";
        configInfo.featureBitmap = mFeature;
        configInfo.mGalaDevice = 2;
        configInfo.mGalaVersion = 3;
        PSListenerImpl psListener = new PSListenerImpl(this);
        mPlayerCallback = psListener.getPlayerCallback();
//        PSServiceManager.getInstance().setLibPath("/data/pushscreen");
        PSServiceManager.getInstance().init(this, configInfo, new PushScreenServiceState(), psListener);
    }

    private void createPlayer() {
        WindowManager wm1 = this.getWindowManager();
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm1.getDefaultDisplay().getMetrics(outMetrics);
        int winWidth = outMetrics.widthPixels;
        mPlayer = findViewById(R.id.video_player);
        RelativeLayout.LayoutParams rlParams = (RelativeLayout.LayoutParams) mPlayer.getLayoutParams();
        rlParams.width = winWidth;
        rlParams.height = winWidth * 9 / 16;
        mPlayer.setLayoutParams(rlParams);
        mPlayback = findViewById(R.id.control_playback);
        mExit = findViewById(R.id.control_stop);

        mPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mPlayerCallback.onPlayerPrepared();
            }
        });
        mPlayback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.equals(mPlayback.getText(), getString(R.string.control_pause))) {
                    curposition = mPlayer.getCurrentPosition();
                    mPlayer.pause();
                    mPlayback.setText(R.string.control_resume);
                    rate = 0;
                    mPlayerCallback.onPlayerPaused();
                } else if (TextUtils.equals(mPlayback.getText(), getString(R.string.control_resume))) {
                    mPlayer.seekTo(curposition);
                    mPlayer.start();
                    mPlayback.setText(R.string.control_pause);
                    rate = 1;
                    mPlayerCallback.onPlayerResumed();
                }
            }
        });
        mExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "click exit");
                mPlayer.stopPlayback();
                mPlayerCallback.onPlayerStopped();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PSServiceManager.getInstance().clear();
        mPlayer = null;
        mPlayerCallback = null;
        unregisterReceiver(mStatusReceiver);
    }

    @Override
    public boolean startPlay(final String url, final long history) {
        Log.d(TAG, "startPlay url=" + url + ", history=" + history);
        boolean ret = false;
        FutureTask<Boolean> futureTask = new FutureTask<>(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                if (TextUtils.isEmpty(url) || mPlayer == null) {
                    Log.d(TAG, "url is empty");
                    return false;
                }
                Log.d(TAG, "url is " + url);
                Uri uri = Uri.parse(url);
                mPlayer.setVideoURI(uri);
                mPlayer.seekTo((int) history);
                mPlayer.start();
                return true;
            }
        });
        runOnUiThread(futureTask);
        try {
            ret = futureTask.get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    @Override
    public boolean stopPlay() {
        boolean ret = false;
        FutureTask<Boolean> futureTask = new FutureTask<>(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                mPlayer.stopPlayback();
                return true;
            }
        });
        runOnUiThread(futureTask);
        try {
            ret = futureTask.get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    @Override
    public boolean pausePlay() {
        boolean ret = false;
        FutureTask<Boolean> futureTask = new FutureTask<>(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                mPlayer.pause();
                curposition = mPlayer.getCurrentPosition();
                mPlayback.setText(R.string.control_resume);
                return true;
            }
        });
        runOnUiThread(futureTask);
        try {
            ret = futureTask.get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    @Override
    public void seekTo(final int position) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "seekTo..............." + position);
                curposition = position;
                mPlayer.pause();
                mPlayer.seekTo(position);
                mPlayer.start();
            }
        });
    }

    @Override
    public boolean resumePlay() {
        boolean ret = false;
        FutureTask<Boolean> futureTask = new FutureTask<>(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                Log.d(TAG, "resumeplay..............." + curposition);
                mPlayer.seekTo(curposition);
                mPlayer.start();
                mPlayback.setText(R.string.control_pause);
                return true;
            }
        });

        runOnUiThread(futureTask);
        try {
            ret = futureTask.get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    @Override
    public int getDuration() {
        return mPlayer.getDuration();
    }

    @Override
    public int getPosition() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                curposition = mPlayer.getCurrentPosition();
            }
        });
        return curposition;
    }

    @Override
    public boolean setVolume(final int volume) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, (volume / 100) * volumeMax, 0);
            }
        });
        return true;
    }

    @Override
    public int getVolume() {
        return audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
    }

    @Override
    public void playback() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mPlayback != null) {
                    mPlayback.performClick();
                }
            }
        });
    }

    @Override
    public float getRate() {
        return rate;
    }

    @Override
    public void setRate(float rate) {
        this.rate = rate;
    }

    @Override
    public String saveAudioPicture(String s, ByteArrayOutputStream byteArrayOutputStream) {
        return null;
    }

    @Override
    public Surface getSurface() {
        return null;
    }

    @Override
    public void changeMirrorSize(final int width, final int height) {
    }


    // 投屏service的运行状态监听
    class PushScreenServiceState implements PSServiceManager.ServiceStateCallback {
        @Override
        public void onQimoResult(final boolean b) {
            Log.d(TAG, "start qimo " + (b ? "success" : "fail"));
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (b && mQimoState != null) {
                        mQimoState.setText(R.string.qimo_alive);
                    }
                }
            });
        }

        @Override
        public void onAirplayResult(final boolean b) {
            Log.d(TAG, "start airplay " + (b ? "success" : "fail"));
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (b && mAirplayState != null) {
                        mAirplayState.setText(R.string.airplay_alive);
                    }
                }
            });

        }

        @Override
        public void onPushServiceResult(final boolean b) {
            Log.d(TAG, "start pushservice " + (b ? "success" : "fail"));
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (b && mIMState != null) {
                        mIMState.setText(R.string.im_alive);
                    }
                }
            });

        }

        @Override
        public void onQimoServicePublished(String s) {
            Log.d(TAG, "start qimo on" + s);
        }

        @Override
        public void onAirplayServicePublished(String s) {
            Log.d(TAG, "start airplay on" + s);
        }

        @Override
        public void onServiceStopped() {
            Log.d(TAG, "service stopped");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mQimoState != null) {
                        mQimoState.setText(R.string.qimo_dead);
                    }
                    if (mAirplayState != null) {
                        mAirplayState.setText(R.string.airplay_dead);
                    }
                    if (mIMState != null) {
                        mIMState.setText(R.string.im_dead);
                    }
                }
            });

        }
    }
}
