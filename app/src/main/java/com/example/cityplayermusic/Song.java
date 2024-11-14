package com.example.cityplayermusic;

import android.net.Uri;

public class Song {
// Members
    String title;
    Uri uri;
    Uri artwork;
    int size;
    int duration;

    // Constructor


    public Song(String title, Uri uri, Uri artwork, int size, int duration) {
        this.title = title;
        this.uri = uri;
        this.artwork = artwork;
        this.size = size;
        this.duration = duration;
    }

    // getters

    public String getTitle() {
        return title;
    }

    public Uri getUri() {
        return uri;
    }

    public Uri getArtwork() {
        return artwork;
    }

    public int getSize() {
        return size;
    }

    public int getDuration() {
        return duration;
    }

    // setters

    public void setTitle(String title) {
        this.title = title;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }

    public void setArtwork(Uri artwork) {
        this.artwork = artwork;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }
}
