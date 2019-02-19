package com.gala.fzf.mediaplayerdemo;

import android.text.TextUtils;
import android.util.Log;
import android.view.Surface;

import com.tvguo.gala.PSCallbackInfoManager;
import com.tvguo.gala.PSMessageListener;
import com.tvguo.gala.qimo.QimoExecutionResult;
import com.tvguo.gala.util.MediaInfo;

import java.io.ByteArrayOutputStream;
import java.util.List;

public class PSListenerImpl implements PSMessageListener {
    private static final String TAG = PSListenerImpl.class.getSimpleName();

    private PushScreenCallback mCallback;
    private String session = null;

    public PSListenerImpl(PushScreenCallback callback) {
        mCallback = callback;
    }

    @Override
    public QimoExecutionResult onStart(MediaInfo mediaInfo) {
        Log.d(TAG, "onStart mediainfo=" + mediaInfo.toString());
        QimoExecutionResult result = new QimoExecutionResult();
        if (!TextUtils.isEmpty(session)) {
            boolean ret = stopPreviousVideo(session);
            if (ret) {
                session = null;
            } else {
                Log.d(TAG, "stop previous video fail!");
            }
        }
        session = mediaInfo.session;
        if (mediaInfo.mediaType == MediaInfo.MEDIA_TYPE_VIDEO) {
            Log.d(TAG, "start play video");
            if (mediaInfo.session.startsWith("gala")) {
                Log.d(TAG, "qimo video:");
                Log.d(TAG, "albumId=" + mediaInfo.videoInfo.albumId);
                Log.d(TAG, "tvid=" + mediaInfo.videoInfo.tvId);
                result.result = false;
            }
            PSCallbackInfoManager.getInstance().updateMediaInfo(mediaInfo);
            boolean ret = mCallback.startPlay(mediaInfo.videoInfo.uri, mediaInfo.videoInfo.history);
            if (ret) {
                PSCallbackInfoManager.getInstance().setMeidaPlay(session, mCallback.getPosition());
            }
            result.result = ret;
        } else {
            Log.d(TAG, "not support mediaType=" + mediaInfo.mediaType);
            PSCallbackInfoManager.getInstance().setMediaStop(session);
            result.result = false;
        }

        return result;
    }

    private boolean stopPreviousVideo(String session) {
        Log.d(TAG, "stopPreviousVideo session=" + session);
        boolean ret = mCallback.stopPlay();
        if (ret) {
            PSCallbackInfoManager.getInstance().setMediaStop(session);
        }
        return ret;
    }

    @Override
    public void onStop() {
        Log.d(TAG, "onStop");
        boolean ret = mCallback.stopPlay();
        if (ret) {
            PSCallbackInfoManager.getInstance().setMediaStop(session);
            session = null;
        } else {
            Log.d(TAG, "stop fail session=" + session);
        }
    }

    @Override
    public void onPause() {
        Log.d(TAG, "onPause");
        boolean ret = mCallback.pausePlay();
        if (ret) {
            PSCallbackInfoManager.getInstance().setMediaPause(session, mCallback.getPosition());
        } else {
            Log.d(TAG, "pause fail session=" + session);
        }
    }

    @Override
    public void onSeekTo(int i) {
        Log.d(TAG, "onSeekTo position=" + i);
        mCallback.seekTo(i);
    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume");
        boolean ret = mCallback.resumePlay();
        if (ret) {
            PSCallbackInfoManager.getInstance().setMeidaPlay(session, mCallback.getPosition());
        } else {
            Log.d(TAG, "resume fail session=" + session);
        }
    }

    @Override
    public int onGetDuration() {
        Log.d(TAG, "onGetDuration");
        return mCallback.getDuration();
    }

    @Override
    public int onGetPosition() {
        Log.d(TAG, "onGetPosition");
        return mCallback.getPosition();
    }

    @Override
    public boolean onSetPlayList(List<MediaInfo> list, int i) {
        return false;
    }

    @Override
    public boolean onSetVolume(int i) {
        Log.d(TAG, "onSetVolume volume=" + i);
        return mCallback.setVolume(i);
    }

    @Override
    public boolean onPlayNext() {
        Log.d(TAG, "onPlayNext");
        return false;
    }

    @Override
    public boolean onPlayLast() {
        Log.d(TAG, "onPlayLast");
        return false;
    }

    @Override
    public void onSetEpisodeExit(String s, String s1) {
        Log.d(TAG, "onSetEpisodeExit episode=" + s + ", picture=" + s1);
    }

    @Override
    public void onSetDelayExit(long l, String s) {
        Log.d(TAG, "onSetDelayExit delay=" + l + ", picture=" + s);
    }

    @Override
    public int onGetStopDelay() {
        Log.d(TAG, "onGetStopDelay");
        return 0;
    }

    @Override
    public void onSetVideoSpeed(String s) {
        Log.d(TAG, "onSetVideoSpeed speed=" + s);
    }

    @Override
    public boolean onChangeWatchTa(String s, String s1) {
        return false;
    }

    @Override
    public void onUpdateLyric(String s) {

    }

    @Override
    public boolean onGetMuteState() {
        return false;
    }

    @Override
    public void onSeekLeft() {
        Log.d(TAG, "seekLeft");
    }

    @Override
    public void onSeekRight() {
        Log.d(TAG, "seekRight");
    }

    @Override
    public void onStoreImage(String s, ByteArrayOutputStream byteArrayOutputStream) {
        Log.d(TAG, "onStoreImage key=" + s);
    }

    @Override
    public float onGetRate() {
        Log.d(TAG, "getrate");
        return mCallback.getRate();
    }

    @Override
    public void onSetRate(float rate) {
        Log.d(TAG, "setRate rate=" + rate);
        mCallback.setRate(rate);
    }

    @Override
    public void onChangePictureSize(double scale, double x, double y) {
        Log.d(TAG, "onChangePictureSize scale=" + scale + ", x=" + x + ", y=" + y);
    }

    @Override
    public int onGetVolume() {
        Log.d(TAG, "getVolume");
        return mCallback.getVolume();
    }

    @Override
    public Surface onGetMirrorSurface() {
        Log.d(TAG, "onGetMirrorSurface");
        return mCallback.getSurface();
    }

    @Override
    public void onChangeMirrorSurfaceSize(int i, int i1) {
        Log.d(TAG, "changeMirrorSize width=" + i + ", height=" + i1);
        mCallback.changeMirrorSize(i, i1);
    }

    @Override
    public void onVolumeUp() {
        Log.d(TAG, "volumeUp");
    }

    @Override
    public void onVolumeDown() {
        Log.d(TAG, "volumeDown");
    }

    @Override
    public void onUpdateAudioInfo(MediaInfo.AudioInfo audioInfo) {
        Log.d(TAG, "onUpdateAudioInfo " + audioInfo.type);
        Log.d(TAG, "onUpdateAudioInfo " + audioInfo.album);
        Log.d(TAG, "onUpdateAudioInfo " + audioInfo.albumUri);
        Log.d(TAG, "onUpdateAudioInfo " + audioInfo.artist);
        Log.d(TAG, "onUpdateAudioInfo " + audioInfo.name);
        Log.d(TAG, "onUpdateAudioInfo " + audioInfo.songId);
        Log.d(TAG, "onUpdateAudioInfo " + audioInfo.source);
        Log.d(TAG, "onUpdateAudioInfo " + audioInfo.uri);
    }

    @Override
    public String onSaveAudioAlbum(String s, ByteArrayOutputStream byteArrayOutputStream) {
        Log.d(TAG, "onSaveAudioAlbum key=" + s);
        return mCallback.saveAudioPicture(s, byteArrayOutputStream);
    }

    @Override
    public void onSeekLeftContinuousStart() {

    }

    @Override
    public void onSeekRightContinuousStart() {

    }

    @Override
    public void onSeekContinuousEnd() {

    }

    @Override
    public boolean onSetDolby(boolean b) {
        return false;
    }


    @Override
    public void onClickPlayback() {
        Log.d(TAG, "playback");
        mCallback.playback();
    }

    @Override
    public boolean onSetPlayMode(int i) {
        Log.d(TAG, "onSetPlayMode mode=" + i);
        return false;
    }

    @Override
    public QimoExecutionResult onSetRes(String s) {
        return new QimoExecutionResult();
    }

    @Override
    public Object controlCommand(String s, Object... objects) throws Exception {
        return null;
    }

    public PlayerCallback getPlayerCallback() {
        return new PlayerCallBackImpl();
    }

    class PlayerCallBackImpl implements PlayerCallback {

        @Override
        public void onPlayerPrepared() {
            PSCallbackInfoManager.getInstance().changeDuration(session, mCallback.getPosition(), mCallback.getDuration());
        }

        @Override
        public void onPlayerPaused() {
            PSCallbackInfoManager.getInstance().setMediaPause(session, mCallback.getPosition());
        }

        @Override
        public void onPlayerResumed() {
            PSCallbackInfoManager.getInstance().setMeidaPlay(session, mCallback.getPosition());
        }

        @Override
        public void onPlayerStopped() {
            PSCallbackInfoManager.getInstance().setMediaStop(session);
        }
    }

    public interface PushScreenCallback {
        boolean startPlay(String url, long history);

        boolean stopPlay();

        boolean pausePlay();

        void seekTo(int position);

        boolean resumePlay();

        int getDuration();

        int getPosition();

        boolean setVolume(int volume);

        int getVolume();

        void playback();

        float getRate();

        void setRate(float rate);

        String saveAudioPicture(String s, ByteArrayOutputStream byteArrayOutputStream);

        Surface getSurface();

        void changeMirrorSize(int width, int height);
    }
}
