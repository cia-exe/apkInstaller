import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Assertions.*

import ken.apkinstaller.*

internal class MainKtTest {

    @BeforeEach
    fun setUp() {
        println("--- setUp(${Thread.currentThread().id})")
    }

    @AfterEach
    fun tearDown() {
        println("--- tearDown(${Thread.currentThread().id})")
    }

    //@Disabled
    @Test
    fun testMain() {
        println("*** main(${Thread.currentThread().id})")
        main(emptyArray())
    }

    //@Test
    fun testPrintHelp() {
        println("*** printHelp(${Thread.currentThread().id})")
        printHelp()
    }

    //@Test
    fun testRunCommand() {
        println("*** runCommand(${Thread.currentThread().id})")
    }

    //@Test
    fun testGetPackageNameFromApk() {
        println("*** getPackageNameFromApk(${Thread.currentThread().id})")
    }

    //@Test
    fun testAdb() {
        println("*** adb(${Thread.currentThread().id})")
    }
}