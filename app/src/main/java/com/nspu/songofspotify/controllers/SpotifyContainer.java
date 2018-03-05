package com.nspu.songofspotify.controllers;

import android.app.Activity;
import android.arch.lifecycle.MutableLiveData;
import android.util.Log;

import com.nspu.songofspotify.controllers.interfaces.SpotifyCustomInterface;
import com.nspu.songofspotify.controllers.interfaces.SpotifyLoginInterface;
import com.nspu.songofspotify.utils.MsToSec;
import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.Error;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.PlayerEvent;
import com.spotify.sdk.android.player.Spotify;
import com.spotify.sdk.android.player.SpotifyPlayer;

import java.util.ArrayList;
import java.util.List;

import javax.security.auth.DestroyFailedException;
import javax.security.auth.Destroyable;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.models.Playlist;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

import static com.spotify.sdk.android.player.Metadata.Track;

/**
 * Created by nspu on 02/03/18.
 * <p>
 * SpotifyContainer class use the spotify sdk(Beta) for android and spotify-web-api-android(library to call REST API).
 * This allows to cover many situations easily.
 * Keep all the buisness logic here to communicate with spotify is probably the best way because the SDK is still in beta
 * and it will be the only file(or package) to change when it will evolve.
 */
public class SpotifyContainer implements SpotifyPlayer.NotificationCallback, ConnectionStateCallback, Destroyable {
    private static SpotifyContainer ourInstance;

    //Top50 Canada
    private static final String PLAYLIST = "spotify:user:spotify:playlist:37i9dQZF1DWXT8uSSn6PRy";
    public static final String USER_ID = "spotify";
    public static final String PLAYLIST_ID = "37i9dQZF1DWXT8uSSn6PRy";


    private Player mPlayer;
    private final kaaes.spotify.webapi.android.SpotifyService mSpotify;
    private final Activity mActivity;
    private final List<SpotifyCustomInterface> mListeners = new ArrayList<>();
    private final String mToken;
    private ProgressTrackRunnable mProgressTrackRunnable;

    //LiveData
    private final MutableLiveData<Playlist> mObservableCurrentPlayList = new MutableLiveData<>();
    private final MutableLiveData<Track> mObservableCurrentTrack = new MutableLiveData<>();

    public static SpotifyContainer getInstance() {
        return ourInstance;
    }

    /**
     * Initialise the singleton only the first time this method is called.
     *
     * @param activity activity is use by spotify for authentication
     * @param token    api token for spotify authorization
     * @param clientId give by spotify for identification
     */
    public static void init(Activity activity, String token, String clientId) throws Throwable {
        if (activity == null) throw new Throwable("activity is null");
        if (!(activity instanceof SpotifyLoginInterface)) throw
                new Throwable("activity does not implement SpotifyLoginInterface");
        if (token.isEmpty()) throw
                new Throwable("clientId not declared. Impossible to instantiate SpotifyContainer");

        if (ourInstance == null) {
            ourInstance = new SpotifyContainer(activity, token, clientId);
        }
    }

    private SpotifyContainer(Activity activity, String token, String clientID) {
        mActivity = activity;
        final Config playerConfig = new Config(activity, token, clientID);
        SpotifyApi api = new SpotifyApi();
        mToken = playerConfig.oauthToken;
        api.setAccessToken(playerConfig.oauthToken);
        mSpotify = api.getService();
        Spotify.getPlayer(playerConfig, this, new SpotifyPlayer.InitializationObserver() {
            @Override
            public void onInitialized(SpotifyPlayer spotifyPlayer) {
                mPlayer = spotifyPlayer;
                mPlayer.addConnectionStateCallback(SpotifyContainer.this);
                mPlayer.addNotificationCallback(SpotifyContainer.this);
            }

            @Override
            public void onError(Throwable throwable) {
                Log.e("SpotifyContainer", "Could not initialize player: " + throwable.getMessage());
            }
        });
    }

    /**
     * Add an object implementing the SpotifyCustomInterface interface to the listener
     *
     * @param toAdd object implementing the SpotifyCustomInterface
     */
    public void addListener(SpotifyCustomInterface toAdd) {
        if (!mListeners.contains(toAdd)) {
            mListeners.add(toAdd);
        }
    }

    /**
     * Remove an object implementing the SpotifyCustomInterface interface to the listener
     *
     * @param toRemove object implementing the SpotifyCustomInterface
     */
    public void removeListener(SpotifyCustomInterface toRemove) {
        if (mListeners.contains(toRemove)) {
            mListeners.remove(toRemove);
        }
    }

    @Override
    public void destroy() throws DestroyFailedException {
        Spotify.destroyPlayer(this);
    }

    @Override
    public void onPlaybackEvent(PlayerEvent playerEvent) {
        Log.d("SpotifyContainer", "Playback event received: " + playerEvent.name());

        switch (playerEvent) {
            case kSpPlaybackNotifyPlay:
                notifPlay();
                break;

            case kSpPlaybackNotifyPause:
                emmitTrackPause();
                break;

            case kSpPlaybackNotifyTrackChanged:
                notifTrackChanged();
                break;

            case kSpPlaybackNotifyMetadataChanged:
                break;

            default:
                break;
        }
    }

    /**
     * It is called when a song is play. Emmit the information about the track
     */
    private void notifPlay() {
        int duration;
        if (mPlayer.getMetadata().currentTrack != null) {
            emmitTrackChange(mPlayer.getMetadata().currentTrack);
            duration = MsToSec.convertMsToSec(mPlayer.getMetadata().currentTrack.durationMs);
            emmitTrackChangeDurationSeconde(duration);
            emmitTrackPlay();
            if (mProgressTrackRunnable == null || mProgressTrackRunnable.getState() == Thread.State.TERMINATED) {
                mProgressTrackRunnable = new ProgressTrackRunnable();
                mProgressTrackRunnable.start();
            }
        }
    }

    /**
     * It is called when a track change. Emmit the information about the track
     */
    private void notifTrackChanged() {
        int duration;
        emmitTrackChange(mPlayer.getMetadata().currentTrack);
        duration = MsToSec.convertMsToSec(mPlayer.getMetadata().currentTrack.durationMs);
        emmitTrackChangeDurationSeconde(duration);
        emmitTrackChangePositionSeconde(0);
    }

    @Override
    public void onPlaybackError(Error error) {
        Log.d("SpotifyContainer", "Playback error received: " + error.name());
        switch (error) {
            // Handle error type as necessary
            default:
                break;
        }
    }

    @Override
    public void onLoggedIn() {
        Log.d("SpotifyContainer", "User logged in");

        ((SpotifyLoginInterface) mActivity).isLogging();

        //call the playlist
        mPlayer.playUri(new Player.OperationCallback() {
            @Override
            public void onSuccess() {
                Log.e("SpotifyContainer", "Success playUri ");

                pauseTrack(null);

                //get the playlist from the rest api
                //from SpotifyRestApi
                SpotifyRestApi.getInstance().getCurrentPlaylist();
                //from SpotifyApi (spotify-web-api-android)
                mSpotify.getPlaylist(USER_ID, PLAYLIST_ID, new Callback<Playlist>() {
                    @Override
                    public void success(Playlist playlist, Response response) {
                        mObservableCurrentPlayList.setValue(playlist);
                    }

                    @Override
                    public void failure(RetrofitError error) {
                    }
                });
            }

            @Override
            public void onError(Error error) {
                Log.e("SpotifyContainer", "Error playUri : " + error.name());
            }
        }, PLAYLIST, 0, 0);
    }

    @Override
    public void onLoggedOut() {
        Log.d("SpotifyContainer", "User logged out");
    }

    @Override
    public void onLoginFailed(Error error) {
        Log.d("SpotifyContainer", "Login failed");
    }

    @Override
    public void onTemporaryError() {
        Log.d("SpotifyContainer", "Temporary error occurred");
    }

    @Override
    public void onConnectionMessage(String message) {
        Log.d("SpotifyContainer", "Received connection message: " + message);
    }

    /**
     * GETTER
     */

    public String getToken() {
        return mToken;
    }

    public Player getPlayer() {
        return mPlayer;
    }

    public void getCurrentPlaylist(Callback<Playlist> callback) {
        mSpotify.getPlaylist(USER_ID, PLAYLIST_ID, callback);
    }

    public Track getCurrentTrack() {
        return mPlayer.getMetadata().currentTrack;
    }

    //livedata
    public MutableLiveData<Playlist> getObservableCurrentPlayList() {
        return mObservableCurrentPlayList;
    }

    public MutableLiveData<Track> getObservableCurrentTrack() {
        return mObservableCurrentTrack;
    }

    /**
     * EMMIT
     */
    private void emmitTrackChangeDurationSeconde(int sec) {
        for (SpotifyCustomInterface spotifyCustomInterface : mListeners) {
            spotifyCustomInterface.trackChangeDurationSeconde(sec);
        }
    }

    private void emmitTrackChangePositionSeconde(int sec) {
        for (SpotifyCustomInterface spotifyCustomInterface : mListeners) {
            spotifyCustomInterface.trackChangePositionSeconde(sec);
        }
    }

    private void emmitTrackChange(final Track track) {
        mObservableCurrentTrack.setValue(track);
        for (final SpotifyCustomInterface spotifyCustomInterface : mListeners) {
            spotifyCustomInterface.trackChange(track);
        }
    }

    private void emmitTrackPlay() {
        for (SpotifyCustomInterface spotifyCustomInterface : mListeners) {
            spotifyCustomInterface.trackPlay();
        }
    }

    private void emmitTrackPause() {
        for (SpotifyCustomInterface spotifyCustomInterface : mListeners) {
            spotifyCustomInterface.trackPause();
        }
    }


    /**
     * CONTROLLER
     */
    public void playCurrentTrack(Player.OperationCallback callback) {
        mPlayer.resume(callback);
    }

    public void nextTrack(Player.OperationCallback callback) {
        mPlayer.skipToNext(callback);
    }

    public void previousTrack(Player.OperationCallback callback) {
        mPlayer.skipToPrevious(callback);
    }

    public void pauseTrack(Player.OperationCallback callback) {
        mPlayer.pause(callback);
    }

    public void stopTrack(Player.OperationCallback callback) {
        mPlayer.pause(callback);
        mPlayer.seekToPosition(callback, 0);
        emmitTrackChangePositionSeconde(0);
    }

    public void seekToPosition(Player.OperationCallback callback, int positionSec) {
        mPlayer.seekToPosition(callback, MsToSec.convertSecToMs(positionSec));
        emmitTrackChangePositionSeconde(positionSec);
    }

    public void changeTrack(Player.OperationCallback callback, int position) {
        mPlayer.pause(null);
        mPlayer.playUri(callback, PLAYLIST, position, 0);
    }

    /**
     * Emmit a signal to the class who register to the listener each second when a track is playing.
     * Send the new progression
     */
    public class ProgressTrackRunnable extends Thread {
        @Override
        public void run() {
            while (mPlayer.getPlaybackState().isPlaying) {
                //Runs on the UI thread
                mActivity.runOnUiThread(new Runnable() {
                    public void run() {
                        int time_run = MsToSec.convertMsToSec(mPlayer.getPlaybackState().positionMs);
                        emmitTrackChangePositionSeconde(time_run);
                    }
                });
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
