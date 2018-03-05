package com.nspu.songofspotify.views;

import android.databinding.BindingAdapter;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.nspu.songofspotify.R;
import com.squareup.picasso.Picasso;

/**
 * Created by nspu on 02/03/18.
 */

public class BindingAdapters {
    @BindingAdapter("playPause")
    public static void changeImagePlayPause(View view, boolean isPlaying) {
        ImageButton btn = (ImageButton)view;
        if(isPlaying){
            btn.setImageResource(android.R.drawable.ic_media_pause);
        }else{
            btn.setImageResource(android.R.drawable.ic_media_play);
        }
    }

    @BindingAdapter("visibleGone")
    public static void showHide(View view, boolean show) {
        view.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @BindingAdapter({"imageUrl"})
    public static void loadImage(ImageView view, String imageUrl) {
        Picasso.with(view.getContext())
                .load(imageUrl)
                .placeholder(R.drawable.placeholder).error(R.mipmap.ic_launcher_round)
                .into(view);
    }
}