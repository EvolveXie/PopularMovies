package com.evolvexie.popularmovies.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by hand on 2018/3/26.
 */

public class MovieTrailer {

    private int id;
    @SerializedName("results")
    private List<Video> videos;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<Video> getVideos() {
        return videos;
    }

    public void setVideos(List<Video> videos) {
        this.videos = videos;
    }
}
