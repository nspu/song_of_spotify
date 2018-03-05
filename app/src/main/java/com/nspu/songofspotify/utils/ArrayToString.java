package com.nspu.songofspotify.utils;

import java.util.List;

import kaaes.spotify.webapi.android.models.ArtistSimple;

/**
 * Created by nspu on 03/03/18.
 */

public class ArrayToString {
    public static String JoinArtistsToString(List<ArtistSimple> artists) {
        if (artists == null) return "";

        StringBuilder nameArtists = new StringBuilder();
        for (int i = 0; i < artists.size(); i++) {
            nameArtists.append(artists.get(i).name);
            if (i + 1 < artists.size()) nameArtists.append(", ");
        }
        return nameArtists.toString();
    }
}
