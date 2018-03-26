package com.evolvexie.popularmovies.model;

import java.util.List;

/**
 * Created by hand on 2018/3/26.
 */

public class MovieDetail {

    private int id;
    //电影时长
    private int runtime;
    //电影标语
    private String tagline;
    private List<Genres> genres;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getRuntime() {
        return runtime;
    }

    public void setRuntime(int runtime) {
        this.runtime = runtime;
    }

    public String getTagline() {
        return tagline;
    }

    public void setTagline(String tagline) {
        this.tagline = tagline;
    }

    public List<Genres> getGenres() {
        return genres;
    }

    /**
     * 获取所有类别拼接的字符串
     * @return type1 / type2 ...
     */
    public String getGenresStr(){
        if (this.genres == null || this.genres.size() ==0){
            return "";
        }else{
            StringBuffer genres = new StringBuffer();
            for (Genres genre:this.genres) {
                genres.append(genre.getName()).append(" / ");
            }
            return genres.toString();
        }
    }

    public void setGenres(List<Genres> genres) {
        this.genres = genres;
    }
}
