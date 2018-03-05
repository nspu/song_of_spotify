package com.nspu.songofspotify.utils;

import java.util.Locale;

/**
 * Created by nspu on 02/03/18.
 */

public class ChronoConverter {
    public static String SecToHHMMSS(int sec, boolean displayHour) {
        String format;

        if (displayHour) {
            format = String.format(Locale.getDefault(), "%02d:%02d:%02d", sec / 3600, sec / 60 - sec / 3600 * 60, sec % 60);
        } else {
            format = String.format(Locale.getDefault(), "%02d:%02d", sec / 60, sec % 60);
        }

        return format;
    }

    public static String SecToHHMMSS(int sec) {
        String format;

        if (sec / 3600 > 0) {
            format = String.format(Locale.getDefault(), "%02d:%02d:%02d", sec / 3600, sec / 60 - sec / 3600 * 60, sec % 60);
        } else {
            format = String.format(Locale.getDefault(), "%02d:%02d", sec / 60, sec % 60);
        }

        return format;
    }
}
