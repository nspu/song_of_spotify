package com.nspu.songofspotify.views.fragments;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import com.nspu.songofspotify.R;
import com.nspu.songofspotify.controllers.SpotifyContainer;
import com.nspu.songofspotify.controllers.interfaces.SpotifyCustomInterface;
import com.nspu.songofspotify.databinding.FragmentControllerTrackBinding;
import com.nspu.songofspotify.utils.ChronoConverter;
import com.nspu.songofspotify.utils.MsToSec;
import com.spotify.sdk.android.player.Error;
import com.spotify.sdk.android.player.Metadata;
import com.spotify.sdk.android.player.PlaybackState;
import com.spotify.sdk.android.player.Player;


/**
 * Created by nspu on 02/03/18.
 * <p>
 * This fragment allows to control the tracks
 */
public class ControllerTrackFragment extends Fragment implements SpotifyCustomInterface {
    public static final String TAG = "ControllerTrackFragment";

    private FragmentControllerTrackBinding mBinding;
    private boolean mDisplayHour;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment ControllerTrackFragment.
     */
    public static ControllerTrackFragment newInstance() {
        return new ControllerTrackFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_controller_track, container, false);
        initButtons();
        initProgressBar();
        return mBinding.getRoot();
    }


    @Override
    public void onStart() {
        super.onStart();
        SpotifyContainer.getInstance().addListener(this);
        refreshData();

    }

    @Override
    public void onStop() {
        super.onStop();
        SpotifyContainer.getInstance().removeListener(this);
    }

    private void initButtons() {
        mBinding.btnPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SpotifyContainer.getInstance().previousTrack(new Player.OperationCallback() {
                    @Override
                    public void onSuccess() {
                        Log.d("ControllerTrackFragment", "btnPrevious success.");
                    }

                    @Override
                    public void onError(Error error) {
                        Log.d("ControllerTrackFragment", "btnPrevious Error : " + error.name());
                    }
                });
            }
        });

        mBinding.btnPlayPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBinding.getIsPlaying()) {
                    SpotifyContainer.getInstance().pauseTrack(new Player.OperationCallback() {
                        @Override
                        public void onSuccess() {
                            Log.d("ControllerTrackFragment", "btnPlayPause pauseTrack success.");
                        }

                        @Override
                        public void onError(Error error) {
                            Log.d("ControllerTrackFragment", "btnPlayPause pauseTrack Error : " + error.name());
                        }
                    });
                } else {
                    SpotifyContainer.getInstance().playCurrentTrack(new Player.OperationCallback() {
                        @Override
                        public void onSuccess() {
                            Log.d("ControllerTrackFragment", "btnPlayPause playCurrentTrack success.");
                        }

                        @Override
                        public void onError(Error error) {
                            Log.d("ControllerTrackFragment", "btnPlayPause playCurrentTrack Error : " + error.name());
                        }
                    });
                }

            }
        });

        mBinding.btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SpotifyContainer.getInstance().stopTrack(new Player.OperationCallback() {
                    @Override
                    public void onSuccess() {
                        Log.d("ControllerTrackFragment", "stopTrack success.");
                    }

                    @Override
                    public void onError(Error error) {
                        Log.d("ControllerTrackFragment", "stopTrack Error : " + error.name());
                    }
                });
            }
        });

        mBinding.btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SpotifyContainer.getInstance().nextTrack(new Player.OperationCallback() {
                    @Override
                    public void onSuccess() {
                        Log.d("ControllerTrackFragment", "nextTrack success.");
                    }

                    @Override
                    public void onError(Error error) {
                        Log.d("ControllerTrackFragment", "nextTrack Error : " + error.name());
                    }
                });
            }
        });
    }

    private void refreshData() {
        if (SpotifyContainer.getInstance().getPlayer() != null) {
            PlaybackState playback = SpotifyContainer.getInstance().getPlayer().getPlaybackState();
            mBinding.setIsPlaying(playback.isPlaying);
            mBinding.tvTimePostion.setText(ChronoConverter.SecToHHMMSS(MsToSec.convertMsToSec(playback.positionMs), mDisplayHour));

            Metadata.Track track = SpotifyContainer.getInstance().getPlayer().getMetadata().currentTrack;
            if (track != null) {
                mBinding.tvTitle.setText(track.name);
                int duration = MsToSec.convertMsToSec(track.durationMs);
                mBinding.tvTimeDuration.setText(ChronoConverter.SecToHHMMSS(duration));
                mDisplayHour = duration / 3600 > 0;
            }
        }
    }

    private void initProgressBar() {
        mBinding.sbTime.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

                SpotifyContainer.getInstance().seekToPosition(new Player.OperationCallback() {
                    @Override
                    public void onSuccess() {
                        Log.d("ControllerTrackFragment", "setOnSeekBarChangeListener success.");
                    }

                    @Override
                    public void onError(Error error) {
                        Log.d("ControllerTrackFragment", "setOnSeekBarChangeListener Error : " + error.name());
                    }
                }, seekBar.getProgress());

            }
        });
    }
    
    @Override
    public void trackChangeDurationSeconde(int sec) {
        mBinding.sbTime.setMax(sec);
        mBinding.tvTimeDuration.setText(ChronoConverter.SecToHHMMSS(sec));
        mDisplayHour = sec / 3600 > 0;
    }

    @Override
    public void trackChangePositionSeconde(int sec) {
        mBinding.sbTime.setProgress(sec);
        mBinding.tvTimePostion.setText(ChronoConverter.SecToHHMMSS(sec, mDisplayHour));
    }

    @Override
    public void trackChange(Metadata.Track track) {
        mBinding.tvTitle.setText(track.name);
    }

    @Override
    public void trackPause() {
        mBinding.setIsPlaying(false);
    }

    @Override
    public void trackPlay() {
        mBinding.setIsPlaying(true);
    }
}
