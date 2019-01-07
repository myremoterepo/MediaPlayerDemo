package com.iqiyi.fzf.mediaplayerdemo;

import android.app.Service;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.VideoView;

import com.tvguo.iqiyi.PSCallbackInfoManager;
import com.tvguo.iqiyi.PSConfigInfo;
import com.tvguo.iqiyi.PSServiceManager;

import java.io.ByteArrayOutputStream;

public class MainActivity extends AppCompatActivity implements PSListenerImpl.PlayerCallback {
    private static final String QIMO_STATE_LIVING = "qimo运行中";
    private static final String QIMO_STATE_DEAD = "qimo已停止";
    private static final String AIRPLAY_STATE_LIVING = "airplay运行中";
    private static final String AIRPLAY_STATE_DEAD = "airplay已停止";
    private static final String PUSHSERVICE_STATE_LIVING = "pushservice运行中";
    private static final String PUSHSERVICE_STATE_DEAD= "pushservice已停止";
    private static final String CONTROL_PAUSE= "暂停";
    private static final String CONTROL_PLAY= "播放";
    private static final String TAG = "fzf";
    public static final int QIMO_VIDEO_PUSH = 0x000001,
            QIMO_PICTURE_PUSH = 0x000002,
            QIMO_NETVIDEO_PUSH = 0x000008,
            DEVICE_RENAME = 0x000010,
            DEVICE_UPDATE_CHECK = 0x000020,
            FEEDBACK = 0x000040,
            EARPHONE = 0x000080,
            SUBTITLE = 0x000100,
            CEC = 0x000200,
            IGNORE_WIFI = 0x000400,
            HDMI_OUTPUT_ZOOM = 0x000800,
            REMOTE_FORBID = 0x001000,
            DEVICE_REBOOT = 0x002000,
            REMOTE_FORBID_INDIVIDUAL = 0x004000,
            SCREEN_CAPTURE = 0x008000,
            QIMO_OFFLINE = 0x010000,
            MIRROR_QUALITY = 0x020000,
            PICTURE_ZOOM = 0x040000,
            PLAYBACK_SPEED = 0x080000,
            DELAY_EXIT_VIDEO = 0x100000,
            WIFI_DISPLAY = 0x200000,
            OFFLINE_CACHE = 0x400000;

    private Button mPlayback;
    private AudioManager audioManager;
    private Button mExit;
    private int volumeMax;
    private String audioSourcePath;
    private String session;
    private float rate = 0;
    private Button mStart;
    private Button mStop;
    private TextView mQimoState;
    private TextView mAirplayState;
    private TextView mPushServiceState;
    private VideoView mPlayer;
    private SurfaceView mSurfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initService();

        mStart = findViewById(R.id.btn_start);
        mStop = findViewById(R.id.btn_end);
        mQimoState = findViewById(R.id.qimo_state);
        mAirplayState = findViewById(R.id.airplay_state);
        mPushServiceState = findViewById(R.id.pushservice_state);
        mPlayer = findViewById(R.id.video_player);
        mPlayback = findViewById(R.id.control_playback);
        mExit = findViewById(R.id.control_stop);
        mSurfaceView = findViewById(R.id.video_surface);

        mPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                PSCallbackInfoManager.getInstance().changeDuration(session, mPlayer.getCurrentPosition(), mPlayer.getDuration());
            }
        });
        mPlayback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.equals(mPlayback.getText(), CONTROL_PAUSE)) {
                    curposition = mPlayer.getCurrentPosition();
                    mPlayer.pause();
                    mPlayback.setText(CONTROL_PLAY);
                    rate = 0;
                    PSCallbackInfoManager.getInstance().setMediaPause(session, curposition);
                } else if (TextUtils.equals(mPlayback.getText(), CONTROL_PLAY)) {
                    mPlayer.seekTo(curposition);
                    mPlayer.start();
                    mPlayback.setText(CONTROL_PAUSE);
                    rate = 1;
                    PSCallbackInfoManager.getInstance().setMeidaPlay(session, curposition);
                }
            }
        });
        mExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("fzf", "click exit");
                mPlayer.stopPlayback();
                PSCallbackInfoManager.getInstance().setMediaStop(session);
            }
        });

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

        mStart.performClick();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PSServiceManager.getInstance().clear();
    }

    private void initService() {
        audioSourcePath = getCacheDir().getAbsolutePath() + "/audio";
        audioManager = (AudioManager) getSystemService(Service.AUDIO_SERVICE);
        volumeMax = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        final PSConfigInfo configInfo = new PSConfigInfo();
        configInfo.cachePath = getCacheDir().getAbsolutePath() + "/airplay";
        configInfo.deviceId = "QD10011027718A100008";
        configInfo.uuid = "707a-a167-1526-e144";
        configInfo.deviceName = "test";
        configInfo.hardOper = 0;
        configInfo.hardVersion = "4";
        configInfo.targetInterface = "wlan0";
        configInfo.appVersion = "521035";
        configInfo.featureBitmap = String.valueOf(QIMO_VIDEO_PUSH
                + QIMO_PICTURE_PUSH
                + QIMO_NETVIDEO_PUSH
                + MIRROR_QUALITY
                + PICTURE_ZOOM
                + PLAYBACK_SPEED
                + DELAY_EXIT_VIDEO);
        PSListenerImpl psListener = new PSListenerImpl(this);
        PSServiceManager.ServiceStateCallback stateCallback = new PSServiceManager.ServiceStateCallback() {
            @Override
            public void onQimoResult(final boolean b) {
                Log.d(TAG, "start qimo " + (b ? "success" : "fail"));

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (b && mQimoState != null) {
                            mQimoState.setText(QIMO_STATE_LIVING);
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
                            mAirplayState.setText(AIRPLAY_STATE_LIVING);
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
                        if (b && mPushServiceState != null) {
                            mPushServiceState.setText(PUSHSERVICE_STATE_LIVING);
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
                            mQimoState.setText(QIMO_STATE_DEAD);
                        }
                        if (mAirplayState != null) {
                            mAirplayState.setText(AIRPLAY_STATE_DEAD);
                        }
                        if (mPushServiceState != null) {
                            mPushServiceState.setText(PUSHSERVICE_STATE_DEAD);
                        }
                    }
                });

            }
        };
        PSServiceManager.getInstance().init(this, configInfo, stateCallback, psListener);
        PSServiceManager.getInstance().setAudioEnable(true);
    }

    @Override
    public void startPlay(final String session, final String url, final long history) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                synchronized (PSListenerImpl.mVideoLock) {
                    if (TextUtils.isEmpty(url) || mPlayer == null) {
                        Log.d("fzf", "url is empty");
                        return;
                    }
                    Log.d("fzf", "url is " + url);
                    MainActivity.this.session = session;
                    Uri uri = Uri.parse(url);
                    mPlayer.setVideoURI(uri);
                    mPlayer.seekTo((int) history);
                    mPlayer.start();
                    PSCallbackInfoManager.getInstance().setMeidaPlay(session, curposition);
                }
            }
        });

    }

    @Override
    public void stopPlay(final String session) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                synchronized (PSListenerImpl.mVideoLock) {
                    mPlayer.stopPlayback();
                    PSCallbackInfoManager.getInstance().setMediaStop(session);
                }
            }
        });
    }

    private int curposition;
    @Override
    public void pausePlay(final String session) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                curposition = mPlayer.getCurrentPosition();
                mPlayer.pause();
                PSCallbackInfoManager.getInstance().setMediaPause(session, curposition);
            }
        });
    }

    @Override
    public void seekTo(final int position) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d("fzf", "seekTo..............." + position);
                curposition = position;
                mPlayer.seekTo(position);
            }
        });
    }

    @Override
    public void resumePlay(final String session) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d("fzf", "resumeplay..............." + curposition);
                mPlayer.seekTo(curposition);
                mPlayer.start();
                PSCallbackInfoManager.getInstance().setMeidaPlay(session, curposition);
            }
        });
    }

    @Override
    public int getDuration(final String session) {
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
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, (volume / 100) * volumeMax , 0);
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
    public void startAudio(String session) {

    }

    @Override
    public void startPicture(String session) {

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
        return mSurfaceView.getHolder().getSurface();
    }

    @Override
    public void changeMirrorSize(final int width, final int height) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                RelativeLayout.LayoutParams param = (RelativeLayout.LayoutParams) mSurfaceView.getLayoutParams();
                param.width = width;
                param.height = height;
                mSurfaceView.setLayoutParams(param);
            }
        });
    }
}
