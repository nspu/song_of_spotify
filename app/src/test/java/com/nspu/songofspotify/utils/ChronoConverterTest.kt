package com.nspu.songofspotify.utils

import org.junit.Test

import org.junit.Assert.*

/**
* Created by nspu on 02/03/18.
*/
class ChronoConverterTest {
    @Test
    @Throws(Exception::class)
    fun secToHHMMSS() {
        assertEquals(ChronoConverter.SecToHHMMSS(5000, true), "01:23:20")
        assertEquals(ChronoConverter.SecToHHMMSS(1000, true), "00:16:40")
        assertEquals(ChronoConverter.SecToHHMMSS(50, true), "00:00:50")
        assertEquals(ChronoConverter.SecToHHMMSS(5, true), "00:00:05")

        assertEquals(ChronoConverter.SecToHHMMSS(5000, false), "83:20")
        assertEquals(ChronoConverter.SecToHHMMSS(1000, false), "16:40")
        assertEquals(ChronoConverter.SecToHHMMSS(50, false), "00:50")
        assertEquals(ChronoConverter.SecToHHMMSS(5, false), "00:05")

        assertEquals(ChronoConverter.SecToHHMMSS(5000), "01:23:20")
        assertEquals(ChronoConverter.SecToHHMMSS(1000), "16:40")
        assertEquals(ChronoConverter.SecToHHMMSS(50), "00:50")
        assertEquals(ChronoConverter.SecToHHMMSS(5), "00:05")
    }
}