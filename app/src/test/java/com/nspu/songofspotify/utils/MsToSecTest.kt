package com.nspu.songofspotify.utils

import org.junit.Assert.assertEquals
import org.junit.Test

/**
* Created by nspu on 02/03/18.
*/
class MsToSecTest {
    @Test
    @Throws(Exception::class)
    fun convertMsToSec() {
        assertEquals(MsToSec.convertMsToSec(50000L).toLong(), 50)
        assertEquals(MsToSec.convertMsToSec(5411L).toLong(), 5)
        assertEquals(MsToSec.convertMsToSec(5911L).toLong(), 6)
        assertEquals(MsToSec.convertMsToSec(700L).toLong(), 1)
        assertEquals(MsToSec.convertMsToSec(400L).toLong(), 0)
    }

    @Test
    @Throws(Exception::class)
    fun convertSecToMs() {
        assertEquals(MsToSec.convertSecToMs(50).toLong(), 50000)
        assertEquals(MsToSec.convertMsToSec(0).toLong(), 0)
    }

}