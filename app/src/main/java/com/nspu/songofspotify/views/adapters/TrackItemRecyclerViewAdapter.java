package com.nspu.songofspotify.views.adapters;

import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.nspu.songofspotify.R;
import com.nspu.songofspotify.controllers.SpotifyContainer;
import com.nspu.songofspotify.controllers.interfaces.SpotifyCustomInterface;
import com.nspu.songofspotify.databinding.FragmentTrackItemBinding;
import com.nspu.songofspotify.models.CustomTrack;
import com.nspu.songofspotify.views.callbacks.CustomTrackClickCallback;
import com.spotify.sdk.android.player.Metadata;

import java.util.List;

import javax.security.auth.DestroyFailedException;
import javax.security.auth.Destroyable;

/**
 * Created by nspu on 02/03/18.
 * Bridge between the data and the view.git
 */
public class TrackItemRecyclerViewAdapter extends RecyclerView.Adapter<TrackItemRecyclerViewAdapter.TrackViewHolder> {

    private final List<CustomTrack> mCustomTrackList;
    private final CustomTrackClickCallback mCustomTrackClickCallback;

    public TrackItemRecyclerViewAdapter(CustomTrackClickCallback mCustomTrackClickCallback, List<CustomTrack> customTrackList) {
        this.mCustomTrackClickCallback = mCustomTrackClickCallback;
        this.mCustomTrackList = customTrackList;
    }

    @NonNull
    @Override
    public TrackViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        FragmentTrackItemBinding binding = DataBindingUtil.inflate(
                LayoutInflater.from(parent.getContext()),
                R.layout.fragment_track_item,
                parent, false);

        return new TrackViewHolder(binding);

    }

    @Override
    public void onBindViewHolder(@NonNull final TrackViewHolder holder, final int position) {
        final CustomTrack track = mCustomTrackList.get(position);
        holder.binding.setTrack(track);
        holder.binding.setImageUrl(track.getCovers().get(1).url);
        holder.binding.setPosition(position);
        holder.binding.setCallback(mCustomTrackClickCallback);

        Metadata.Track currentTrack = null;
        try {
            currentTrack = SpotifyContainer.getInstance().getCurrentTrack();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        if ((currentTrack != null && currentTrack.name.equals(track.getName()))
                && currentTrack.albumName.equals(track.getAlbumName())) {
            holder.binding.setIsCurrent(true);
        } else {
            holder.binding.setIsCurrent(false);
        }
    }


    @Override
    public int getItemCount() {
        return mCustomTrackList == null ? 0 : mCustomTrackList.size();
    }

    public class TrackViewHolder extends RecyclerView.ViewHolder implements SpotifyCustomInterface, Destroyable {
        final FragmentTrackItemBinding binding;

        TrackViewHolder(FragmentTrackItemBinding binding){
            super(binding.getRoot());
            this.binding = binding;
            try {
                SpotifyContainer.getInstance().addListener(this);
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }

        @Override
        public void destroy() throws DestroyFailedException {
            try {
                SpotifyContainer.getInstance().removeListener(this);
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }

        @Override
        public void trackChangeDurationSeconde(int sec) {

        }

        @Override
        public void trackChangePositionSeconde(int sec) {

        }

        @Override
        public void trackChange(Metadata.Track track) {
            if (track.name.equals(binding.getTrack().getName())
                    && track.albumName.equals(binding.getTrack().getAlbumName())) {
                binding.setIsCurrent(true);
            } else {
                binding.setIsCurrent(false);
            }
        }

        @Override
        public void trackPause() {

        }

        @Override
        public void trackPlay() {

        }
    }
}
