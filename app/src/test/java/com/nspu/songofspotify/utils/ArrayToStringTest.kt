package com.nspu.songofspotify.utils

import kaaes.spotify.webapi.android.models.ArtistSimple
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.*

/**
 * Created by nspu on 04/03/18.
 */
class ArrayToStringTest {
    @Test
    @Throws(Exception::class)
    fun joinArtistsToString() {

        val artist1 = ArtistSimple()
        artist1.name = "artist1"
        val artist2 = ArtistSimple()
        artist2.name = "artist2"
        val artist3 = ArtistSimple()
        artist3.name = "artist3"

        val listManyArtists = ArrayList<ArtistSimple>()
        listManyArtists.add(artist1)
        listManyArtists.add(artist2)
        listManyArtists.add(artist3)

        val listOneArtist = ArrayList<ArtistSimple>()
        listOneArtist.add(artist1)

        val artistsNull = ArrayToString.JoinArtistsToString(null)
        val manyArtists = ArrayToString.JoinArtistsToString(listManyArtists)
        val oneArtist = ArrayToString.JoinArtistsToString(listOneArtist)

        assertEquals(artistsNull, "")
        assertEquals(manyArtists, "artist1, artist2, artist3")
        assertEquals(oneArtist, "artist1")
    }

}