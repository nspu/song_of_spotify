package com.nspu.songofspotify.views.fragments;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nspu.songofspotify.R;
import com.nspu.songofspotify.controllers.SpotifyContainer;
import com.nspu.songofspotify.databinding.FragmentTrackListBinding;
import com.nspu.songofspotify.models.CustomTrack;
import com.nspu.songofspotify.utils.ArrayToString;
import com.nspu.songofspotify.views.dialog.WaitingDialog;
import com.nspu.songofspotify.views.adapters.TrackItemRecyclerViewAdapter;
import com.nspu.songofspotify.views.callbacks.CustomTrackClickCallback;
import com.spotify.sdk.android.player.Error;
import com.spotify.sdk.android.player.Player;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.models.Playlist;
import kaaes.spotify.webapi.android.models.PlaylistTrack;
import kaaes.spotify.webapi.android.models.Track;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by nspu on 02/03/18.
 *
 * A fragment representing a list of tracks.
 * Pattern : MVC
 */
public class TrackListFragment extends Fragment {
    public static final String TAG = "TrackListFragment";

    private FragmentTrackListBinding mBinding;
    private List<CustomTrack> mCustomTrackList;

    public static TrackListFragment newInstance() {
        return new TrackListFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_track_list, container, false);
        try {
            SpotifyContainer.getInstance().getCurrentPlaylist(new Callback<Playlist>() {
                @Override
                public void success(final Playlist playlist, Response response) {
                    LoadPlayListAsync async = new LoadPlayListAsync(
                            mCustomTrackList,
                            playlist.tracks.items,
                            getContext(),
                            mBinding,
                            mCustomTrackClickCallback);
                    async.execute();
                }

                @Override
                public void failure(RetrofitError error) {

                }
            });
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }

        return mBinding.getRoot();
    }

    /**
     * Change the track when an item is clicked on the list.
     */
    private final CustomTrackClickCallback mCustomTrackClickCallback = new CustomTrackClickCallback() {
        @Override
        public void onClick(CustomTrack customTrack, int position) {

            SpotifyContainer.getInstance().changeTrack(new Player.OperationCallback() {
                @Override
                public void onSuccess() {
                    Log.d("MyTrackNspuRecyclerView", "onClick success");

                }

                @Override
                public void onError(Error error) {
                    Log.d("MyTrackNspuRecyclerView", "onClick Error : " + error.name());

                }
            }, position);
        }
    };

    /**
     * Load the playlist. When it's done, diplay it.
     */
    static class LoadPlayListAsync extends AsyncTask<Void, Void, Void> {
        final String TAG = "LoadPlayListAsync";
        private int imageDone = 0;

        private List<CustomTrack> mCustomTrackList;
        List<PlaylistTrack> mPlaylist;
        Context context;
        private FragmentTrackListBinding mBinding;
        private CustomTrackClickCallback mCustomTrackClickCallback;

        public LoadPlayListAsync(List<CustomTrack> mCustomTrackList, List<PlaylistTrack> mPlaylist, Context context, FragmentTrackListBinding mBinding, CustomTrackClickCallback mCustomTrackClickCallback) {
            this.mCustomTrackList = mCustomTrackList;
            this.mPlaylist = mPlaylist;
            this.context = context;
            this.mBinding = mBinding;
            this.mCustomTrackClickCallback = mCustomTrackClickCallback;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            WaitingDialog.getInstance().display(TAG);
        }

        @SafeVarargs
        @Override
        protected final Void doInBackground(Void... avoid) {
            imageDone = 0;
            mCustomTrackList = new ArrayList<>();
            for (PlaylistTrack playlistTrack : mPlaylist) {
                Track track = playlistTrack.track;
                CustomTrack customTrack = new CustomTrack(
                        track.name,
                        ArrayToString.JoinArtistsToString(track.artists),
                        new ArrayList(track.album.images),
                        track.uri,
                        track.album.name);
                mCustomTrackList.add(customTrack);

                //Preload image in memory (async, need to use the callback with loading ui)
                Picasso.with(context)
                        .load(track.album.images.get(1).url)
                        .placeholder(R.drawable.placeholder)
                        .fetch(new com.squareup.picasso.Callback() {
                            @Override
                            public void onSuccess() {
                                imageDone++;
                            }

                            @Override
                            public void onError() {
                                imageDone++;
                            }
                        });
            }

            //waiting for all images are load
            while (imageDone < mPlaylist.size()) ;
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            TrackItemRecyclerViewAdapter mAdapter = new TrackItemRecyclerViewAdapter(mCustomTrackClickCallback, mCustomTrackList);
            mBinding.rvTrack.setAdapter(mAdapter);
            mAdapter.notifyDataSetChanged();
            WaitingDialog.getInstance().hide(TAG);
        }
    }
}
