package com.nspu.songofspotify.views;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.nspu.songofspotify.R;
import com.nspu.songofspotify.controllers.SpotifyRestApi;
import com.nspu.songofspotify.controllers.SpotifyContainer;
import com.nspu.songofspotify.controllers.interfaces.SpotifyLoginInterface;

import com.nspu.songofspotify.views.dialog.WaitingDialog;
import com.nspu.songofspotify.views.fragments.ControllerTrackFragment;
import com.nspu.songofspotify.views.fragments.TrackListFragment;
import com.nspu.songofspotify.views.fragments.TrackTabbedFragment;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;

/**
 * Created by nspu on 02/03/18.
 */

public class MainActivity extends AppCompatActivity implements SpotifyLoginInterface {

    private static final String CLIENT_ID = "b2dcfb9360eb40baad9da7f7ea2c6a74";
    private static final String REDIRECT_URI = "song-of-spotify://callback";

    private TrackListFragment mTrackListFragment;
    private TrackTabbedFragment mTrackTabbedFragment;
    private static final int REQUEST_CODE = 1337;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(CLIENT_ID,
                AuthenticationResponse.Type.TOKEN,
                REDIRECT_URI);
        builder.setScopes(new String[]{"streaming"});
        AuthenticationRequest request = builder.build();
        AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request);
        WaitingDialog.init(this);

    }

    private void initButton(){
        FloatingActionButton button = findViewById(R.id.btn_change_fragment);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mTrackListFragment.isVisible()){
                    changeForTabbedFragment();
                }else {
                    changeForListFragment();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        // Check if result comes from the correct activity
        if (requestCode == REQUEST_CODE) {
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);
            if (response.getType() == AuthenticationResponse.Type.TOKEN) {
                try {
                    SpotifyContainer.init(this, response.getAccessToken(), CLIENT_ID);
                    SpotifyRestApi.init(SpotifyContainer.getInstance().getToken());
                } catch (Throwable throwable) {
                    Log.d("MainActivity", throwable.getMessage());
                }
            }
        }
    }

    @Override
    public void isLogging() {
        initController();
        initButton();
        mTrackListFragment = TrackListFragment.newInstance();
        mTrackTabbedFragment = TrackTabbedFragment.newInstance();
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.fl_display_tracks, mTrackListFragment, TrackListFragment.TAG).commit();

    }

    private void initController() {
        ControllerTrackFragment trackNspuFragment = ControllerTrackFragment.newInstance();

        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.fl_controller, trackNspuFragment, ControllerTrackFragment.TAG)
                .commit();
    }


    private void changeForListFragment() {
        getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(
                        R.animator.card_flip_left_in,
                        R.animator.card_flip_left_out,
                        R.animator.card_flip_right_in,
                        R.animator.card_flip_right_out)
                .replace(R.id.fl_display_tracks, mTrackListFragment, TrackListFragment.TAG)
                .commit();
    }

    private void changeForTabbedFragment() {
        getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(
                        R.animator.card_flip_right_in,
                        R.animator.card_flip_right_out,
                        R.animator.card_flip_left_in,
                        R.animator.card_flip_left_out)
                .replace(R.id.fl_display_tracks, mTrackTabbedFragment, TrackTabbedFragment.TAG)
                .commit();
    }
}
