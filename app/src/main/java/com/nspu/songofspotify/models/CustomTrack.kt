package com.nspu.songofspotify.models

import kaaes.spotify.webapi.android.models.Image
import java.io.Serializable

/**
 * Created by nspu on 03/03/18.
 */
class CustomTrack(
        var name: String,
        var artists: String,
        var covers: MutableList<out Image>?,
        var uri: String,
        var albumName: String
) : Serializable
