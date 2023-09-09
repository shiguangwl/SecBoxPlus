package com.xxhoz.secbox.bean;

public class EpsodeEntity {
    private String videoUrl;
    private String videoName;
    /**
     * 是否被选中播放(默认没有播放)
     */
    private boolean isPlay = false;

    public EpsodeEntity(String videoName, String videoUrl) {
        this.videoUrl = videoUrl;
        this.videoName = videoName;
    }

    public boolean isPlay() {
        return isPlay;
    }

    public void setPlay(boolean play) {
        isPlay = play;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public String getVideoName() {
        return videoName;
    }

    public void setVideoName(String videoName) {
        this.videoName = videoName;
    }
}
