package com.iqiyi.fzf.mediaplayerdemo;


import android.text.TextUtils;
import android.util.Log;
import android.view.Surface;

import com.tvguo.iqiyi.PSCallbackInfoManager;
import com.tvguo.iqiyi.PSMessageListener;
import com.tvguo.iqiyi.qimo.QimoExecutionResult;
import com.tvguo.iqiyi.util.MediaInfo;

import java.io.ByteArrayOutputStream;
import java.util.List;

public class PSListenerImpl implements PSMessageListener {
    private static final String TAG = PSListenerImpl.class.getSimpleName();

    private PlayerCallback mCallback;
    private String session;
    private int mediaType;
    public final static Object mVideoLock = new Object();

    public PSListenerImpl(PlayerCallback callback) {
        mCallback = callback;
    }

    @Override
    public QimoExecutionResult onStart(MediaInfo mediaInfo) {
        Log.d(TAG, "onStart mediainfo=" + mediaInfo.session);
        QimoExecutionResult result = new QimoExecutionResult();
        session = mediaInfo.session;
        mediaType = mediaInfo.mediaType;
        if (mediaInfo.mediaType == MediaInfo.MEDIA_TYPE_VIDEO) {
            synchronized (mVideoLock) {
                if (!TextUtils.isEmpty(session) && mediaType == MediaInfo.MEDIA_TYPE_VIDEO) {
                    stopPreviousVideo(session);
                }
                PSCallbackInfoManager.getInstance().updateMediaInfo(mediaInfo);
                mCallback.startPlay(mediaInfo.session, mediaInfo.videoInfo.uri, mediaInfo.videoInfo.history);
            }
        } else if (mediaInfo.mediaType == MediaInfo.MEDIA_TYPE_AUDIO) {
            Log.d(TAG, "start play audio");
            PSCallbackInfoManager.getInstance().updateMediaInfo(mediaInfo);
            mCallback.startAudio(mediaInfo.session);
        } else if (mediaInfo.mediaType == MediaInfo.MEDIA_TYPE_PICTURE) {
            Log.d(TAG, "start play picture");
            PSCallbackInfoManager.getInstance().updateMediaInfo(mediaInfo);
            mCallback.startPicture(mediaInfo.session);
        } else if (mediaInfo.mediaType == MediaInfo.MEDIA_TYPE_MIRROR) {
            Log.d(TAG, "start play mirror");
            PSCallbackInfoManager.getInstance().updateMediaInfo(mediaInfo);
            PSCallbackInfoManager.getInstance().setMeidaPlay(mediaInfo.session, 0);
        } else {
            Log.d(TAG, "unknown media type");
            result.result = false;
            return result;
        }

        result.result = true;
        return result;
    }

    private void stopPreviousVideo(String session) {
        mCallback.stopPlay(session);
    }

    @Override
    public void onStop() {
        Log.d(TAG, "onStop");
        mCallback.stopPlay(session);
    }

    @Override
    public void onPause() {
        Log.d(TAG, "onPause");
        mCallback.pausePlay(session);
    }

    @Override
    public void onSeekTo(int i) {
        Log.d(TAG, "onSeekTo position=" + i);
        mCallback.seekTo(i);
    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume");
        mCallback.resumePlay(session);
    }

    @Override
    public int onGetDuration() {
        Log.d(TAG, "onGetDuration");
        return mCallback.getDuration(session);
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

    public interface PlayerCallback{
        void startPlay(String session, String url, long history);
        void stopPlay(String session);
        void pausePlay(String session);
        void seekTo(int position);

        void resumePlay(String session);

        int getDuration(String session);

        int getPosition();

        boolean setVolume(int volume);

        int getVolume();

        void playback();

        void startAudio(String session);

        void startPicture(String session);

        float getRate();

        void setRate(float rate);

        String saveAudioPicture(String s, ByteArrayOutputStream byteArrayOutputStream);

        Surface getSurface();

        void changeMirrorSize(int width, int height);
    }
}
