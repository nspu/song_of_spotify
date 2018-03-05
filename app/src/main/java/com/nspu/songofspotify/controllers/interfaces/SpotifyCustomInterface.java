package com.nspu.songofspotify.controllers.interfaces;

import com.spotify.sdk.android.player.Metadata.Track;

/**
 * Created by nspu on 02/03/18.
 */

public interface SpotifyCustomInterface {
    /**
     * Send the duration of a track in second
     *
     * @param sec
     */
    void trackChangeDurationSeconde(int sec);

    /**
     * Send the new time of the current track in second
     *
     * @param sec
     */
    void trackChangePositionSeconde(int sec);

    /**
     * Send the new track
     *
     * @param track
     */
    void trackChange(Track track);

    /**
     * Send that the current track is in pause
     */
    void trackPause();

    /**
     * Send that the current track is play
     */
    void trackPlay();
}
