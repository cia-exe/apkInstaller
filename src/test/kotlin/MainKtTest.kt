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
        // doesn't work because they are different processes.
        //runCommand("cmd", "/c", "cd", "/testData/apk1")
        runCommand("cmd", "/c", "path")
        //runCommand("cmd", "/c", "path=c:/aaa")
        //runCommand("cmd", "/c", "path")
    }

    @AfterEach
    fun tearDown() {
        println("--- tearDown(${Thread.currentThread().id})")
    }

    //@Disabled
    @Test
    fun mainEmpty() {
        println("*** main(${Thread.currentThread().id})")
        main(emptyArray())
    }

    @Test
    fun mainVerify() {
        val arg = "-v"
        println("*** main(${Thread.currentThread().id}) $arg")
        main(arrayOf(arg))
    }

    @Test
    fun mainInstall() {
        val arg = "-i"
        println("*** main(${Thread.currentThread().id}) $arg")
        main(arrayOf(arg))
    }

    @Test
    fun mainRemove() {
        val arg = "-r"
        println("*** main(${Thread.currentThread().id}) $arg")
        main(arrayOf(arg))
    }

    @Test
    fun mainRemoveInstall() {
        val arg = "-ri"
        println("*** main(${Thread.currentThread().id}) $arg")
        main(arrayOf(arg))
    }


    // ---------------------------------------------------------------
    @Test
    fun testPrintHelp() {
        println("*** printHelp(${Thread.currentThread().id})")
        printHelp()
    }

    @Test
    fun testRunCommand() {
        println("*** runCommand(${Thread.currentThread().id})")
    }

    @Test
    fun testGetPackageNameFromApk() {
        println("*** getPackageNameFromApk(${Thread.currentThread().id})")

    }

    @Test
    fun testAdb() {
        println("*** adb(${Thread.currentThread().id})")
        adb("shell", "ls", "-la")
    }
}