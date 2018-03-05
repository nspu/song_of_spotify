package com.nspu.songofspotify.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import com.nspu.songofspotify.controllers.SpotifyRestApi;
import com.nspu.songofspotify.controllers.SpotifyContainer;
import com.nspu.songofspotify.models.CustomTrack;
import com.spotify.sdk.android.player.Metadata;

import java.util.List;

/**
 * Created by nspu on 04/03/18.
 */

public class TrackTabbedViewModel extends AndroidViewModel {

    private final LiveData<List<CustomTrack>> mObservablePlaylist;
    private final LiveData<Metadata.Track> mObservableTrack;

    public TrackTabbedViewModel(@NonNull Application application) {
        super(application);

        this.mObservablePlaylist = SpotifyRestApi.getInstance().getObservableCurrentPlayList();
        this.mObservableTrack = SpotifyContainer.getInstance().getObservableCurrentTrack();
    }

    /**
     * Expose the livedata track, so the UI can observe it.
     */
    public LiveData<List<CustomTrack>> getObervablePlaylist() {
        return mObservablePlaylist;
    }
    public LiveData<Metadata.Track> getObervableTrack() {
        return mObservableTrack;
    }
}
