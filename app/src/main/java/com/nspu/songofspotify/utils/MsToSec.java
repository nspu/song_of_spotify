package com.nspu.songofspotify.utils;

/**
 * Created by nspu on 02/03/18.
 */

public class MsToSec {
    public static int convertMsToSec(long ms) {
        return (int) Math.round(((double) ms) / 1000);
    }

    public static int convertSecToMs(int sec) {
        return sec * 1000;
    }
}
