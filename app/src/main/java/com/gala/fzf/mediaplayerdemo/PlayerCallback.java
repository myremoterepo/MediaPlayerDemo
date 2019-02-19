package com.gala.fzf.mediaplayerdemo;
/**
 * 播放器的播放状态监听接口
 * */
public interface PlayerCallback {
    public void onPlayerPrepared();
    public void onPlayerPaused();
    public void onPlayerResumed();
    public void onPlayerStopped();
}
