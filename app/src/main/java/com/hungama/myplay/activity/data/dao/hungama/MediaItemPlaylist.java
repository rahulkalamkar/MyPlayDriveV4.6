package com.hungama.myplay.activity.data.dao.hungama;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by hungama1 on 6/1/16.
 */
public class MediaItemPlaylist implements Serializable {
    public static final String KEY_SONG_ID = "song_id";
    public static final String KEY_SONG_NAME = "song_name";

    @Expose
    @SerializedName(KEY_SONG_ID)
    public long id;

    @Expose
    @SerializedName(KEY_SONG_NAME)
    public String title;

    public MediaItemPlaylist(long id, String title) {
        this.id = id;
        this.title = title;
    }
}
