package com.nspu.songofspotify.views.fragments;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nspu.songofspotify.R;
import com.nspu.songofspotify.controllers.SpotifyContainer;
import com.nspu.songofspotify.databinding.FragmentTrackTabbedItemBinding;
import com.nspu.songofspotify.databinding.FragmentTrackTabbedListBinding;
import com.nspu.songofspotify.models.CustomTrack;
import com.nspu.songofspotify.viewmodel.TrackTabbedViewModel;
import com.spotify.sdk.android.player.Metadata;

import java.util.List;

/**
 * Created by nspu on 02/03/18.
 *
 * This fragment display the tracks one by one. When you slide, emmit a signal to change the track with the new one.
 */
public class TrackTabbedFragment extends Fragment {

    public static final String TAG = "TrackTabbedFragment";

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private FragmentTrackTabbedListBinding mBinding;
    private List<CustomTrack> mCustomTrackList;
    private CustomTrack mCurrentTrack = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_track_tabbed_list, container, false);


        return mBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final TrackTabbedViewModel viewModel =
                ViewModelProviders.of(this).get(TrackTabbedViewModel.class);

        subscribe(viewModel);
    }

    private void subscribe(final TrackTabbedViewModel viewModel) {
        viewModel.getObervablePlaylist().observe(this, new Observer<List<CustomTrack>>() {
            @Override
            public void onChanged(@Nullable List<CustomTrack> playlist) {
                mCustomTrackList = playlist;
                mSectionsPagerAdapter = new SectionsPagerAdapter(getActivity().getSupportFragmentManager());
                mBinding.container.setAdapter(mSectionsPagerAdapter);
                updateCurrentTrack();
            }
        });

        viewModel.getObervableTrack().observe(this, new Observer<Metadata.Track>() {


            @Override
            public void onChanged(@Nullable Metadata.Track track) {
                mCurrentTrack = new CustomTrack(
                        track.name,
                        track.artistName,
                        null,
                        track.uri,
                        track.albumName);
                updateCurrentTrack();
            }
        });

        mBinding.container.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            boolean userScrollChange = false;
            int previousState = 0;

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if(userScrollChange){
                    SpotifyContainer.getInstance().changeTrack(null, position);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if (previousState == ViewPager.SCROLL_STATE_DRAGGING
                        && state == ViewPager.SCROLL_STATE_SETTLING) {
                    userScrollChange = true;
                } else if (previousState == ViewPager.SCROLL_STATE_SETTLING
                        && state == ViewPager.SCROLL_STATE_IDLE) {
                    userScrollChange = false;
                }

                previousState = state;
            }
        });
    }

    /**
     * Update the current track
     */
    private void updateCurrentTrack() {
        if (mCustomTrackList != null && mCurrentTrack != null) {
            for (int i = 0; i < mCustomTrackList.size(); i++) {
                if ((mCurrentTrack.getName().equals(mCustomTrackList.get(i).getName())
                        && mCustomTrackList.get(i).getAlbumName().contains(mCurrentTrack.getAlbumName()))
                        || mCurrentTrack.getUri().equals(mCustomTrackList.get(i).getUri())) {
                    mBinding.container.setCurrentItem(i);
                    break;
                }
            }
        }
    }

    public static TrackTabbedFragment newInstance() {
        return new TrackTabbedFragment();
    }

    @Override
    public void onStart() {
        super.onStart();
        if(mSectionsPagerAdapter != null){
            mBinding.container.setAdapter(mSectionsPagerAdapter);
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public final static String TRACK = "track";
        public final static String POSITION = "position";
        public final static String NUMBER_TRACK = "number_track";

        private FragmentTrackTabbedItemBinding mBinding;

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(CustomTrack track, int position, int numberTrack) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putSerializable(TRACK, track);
            args.putInt(POSITION, position);
            args.putInt(NUMBER_TRACK, numberTrack);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_track_tabbed_item, container, false);
            CustomTrack track = (CustomTrack) getArguments().getSerializable(TRACK);
            mBinding.tvTitle.setText(track.getName());
            mBinding.tvArtist.setText(track.getArtists());
            mBinding.setImageURI(track.getCovers().get(0).url);
            mBinding.setTrack(track);
            mBinding.setPosition(getArguments().getInt(POSITION) + " / " + getArguments().getInt(NUMBER_TRACK));
            return mBinding.getRoot();
        }
    }


    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentStatePagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return PlaceholderFragment.newInstance(mCustomTrackList.get(position), position + 1, mCustomTrackList.size());
        }


        @Override
        public int getItemPosition(@NonNull Object item) {
            PlaceholderFragment fragment = (PlaceholderFragment)item;
            CustomTrack customTrack = fragment.mBinding.getTrack();
            int position = mCustomTrackList.indexOf(customTrack);

            if (position >= 0) {
                return position;
            } else {
                return POSITION_NONE;
            }
        }

        @Override
        public int getCount() {
            return mCustomTrackList.size();
        }
    }
}
