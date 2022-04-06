package cn.beiye.myapplication2

import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun lang_test() {
        val length = 6
        val v = " ".repeat(length)
        println(v)
        assertEquals(length, v.length)
    }
}