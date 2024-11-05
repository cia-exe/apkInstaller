// PATH=%PATH%;C:\CIA.data\Workspace\temp\apkInstaller\out\artifacts\apkInstaller_jar
// cd C:\CIA.data\Workspace\temp\apkInstaller\test\apk

// package ken.apkinstaller

import java.io.File


fun main(args: Array<String>) {

    val workDir = File(".")
    val apks = mutableListOf<File>()
    val extensions = mutableListOf<File>()

    workDir.listFiles()?.forEach {
        if (it.isFile) {
            if (it.name.endsWith(".apk")) apks.add(it) else extensions.add(it)
        }
    }

    val apk = if (apks.size == 1) apks[0]
    else {
        if (apks.isEmpty()) println("ERROR! APK not found!")
        else println("ERROR! Too many APKs: [${apks.joinToString()}]")
        null
    }

    val packageName = apk?.getPackageNameFromApk()

    println(
        """
        workDir     : ${workDir.absolutePath}
        apks        : ${apks.joinToString()}
        extensions  : ${extensions.joinToString()}
        apk         : ${apk?.name}
        packageName : $packageName
        """
    )

    if (args.isEmpty()) {
        printHelp()
        return
    }

    if (apk == null) {
        println("APK file Error! exit...")
        return
    }

    if (packageName == null) {
        println("Failed to extract package name from APK! exit...")
        return
    }

    //---------------------------------------------

    fun verify() {
        // Check if the package is installed
        val r = adb("shell", "pm", "list", "packages", packageName)
        if (r.failed) {
            println("Package $packageName is not installed.")
            return
        }

        // Verify OBB file existence and sizes
        val obbPath = "/storage/emulated/0/Android/obb/$packageName/"
        //val obbFiles = listOf("main.1.$packageName.obb", "patch.1.$packageName.obb")

        extensions.forEach { obbFile ->
            val r1 = adb("shell", "ls", "$obbPath$obbFile")
            if (r1.failed) println("OBB file $obbFile not found on device.")
            else println("OBB file $obbFile is present on device.")
        }
    }

    fun install() {
        // Install the APK
        if (adb("install", "-r", "-d", apk.absolutePath).failed) {
            println("Error installing APK.")
            return
        }

        // Copy OBB files to the device
        val obbPath = "/storage/emulated/0/Android/obb/$packageName/"
        if (adb("shell", "ls", obbPath).failed) {
            val r = adb("shell", "mkdir", obbPath)
            println("make dir $obbPath -> $r")
        }

        //val obbFiles = listOf("main.1.$packageName.obb", "patch.1.$packageName.obb")

        extensions.forEach {
            if (adb("push", it.name, obbPath).failed) println("Error copying $it to device.")
        }

        verify()
    }

    fun verifyUninstall() {
        val r = adb("shell", "pm", "list", "packages", packageName)
        if (r.failed) println("Error: Package $packageName is still installed.")
        else println("Package $packageName is uninstalled.")

        val obbPath = "/storage/emulated/0/Android/obb/$packageName/"
        if (adb("shell", "ls", obbPath).failed) println("Error: OBB directory $obbPath still exists.")
        else println("OBB directory $obbPath is removed.")
    }

    fun remove() {
        // Uninstall the APK
        runCommand("adb", "uninstall", packageName)

        // Remove OBB directory if it exists
        val obbPath = "/storage/emulated/0/Android/obb/$packageName/"
        if (adb("shell", "ls", obbPath).failed) {
            println("Warning: OBB directory exists. Deleting...")
            if (adb("shell", "rm", "-rf", obbPath).failed) println("OK")
            else println("Error deleting OBB directory.")
        }

        verifyUninstall()
    }

    //---------------------------------------------


    when (args[0]) {
        "-v" -> verify()
        "-i" -> install()
        "-u" -> remove()
        "-ui" -> {
            remove()
            install()
        }

        else -> printHelp()
    }
}


fun printHelp() {

    val src = ::printHelp.javaClass.protectionDomain.codeSource.location.path
    //val src = object {}.javaClass.protectionDomain.codeSource.location.path
    //val path = object { val path = javaClass.protectionDomain.codeSource.location.path }.path

    // val path = Unit.javaClass.protectionDomain.codeSource.location.path
    //C:/Users/cia.exe/.m2/repository/org/jetbrains/kotlin/kotlin-stdlib/1.7.10/kotlin-stdlib-1.7.10.jar

    // val c = MethodHandles.lookup().lookupClass()
    // println("$c ${c.name} ${c.simpleName} ${c.protectionDomain.codeSource.location}")
    // e.g. PATH=%PATH%;C:\CIA.data\Workspace\temp\apkInstaller\out\artifacts\apkInstaller_jar

    val path = "Add the path in CMD by: PATH=%PATH%;${File(src).run { if (isFile) parentFile else this }.absolutePath}"
    val line = "-".repeat(path.length)

    println(
        """
        $line
        $path
        $line

        Usage:

        java -jar apkInstaller.jar [-v | -i | -r | -ri]
        
        -v    : verify the installation of APK and Extensions.
        -i    : install APK and Extensions, then verify.
        -u    : uninstall APK and Extensions.
        -ui   : uninstall, install, and verify installation.
        """//.trimIndent()
    )
}

//fun getRemoteFiles(dir: String) =
//    adb("shell", "ls", "-l", dir).message.lines()
//        .filter { it.startsWith("-") }
//        .mapNotNull {
//            it.split("\\s+".toRegex()).let { parts ->
//                if (parts.size >= 5) parts.lastOrNull()?.let { name -> name to parts[4].toLongOrNull() } else null
//            }
//        }.toMap()
//
//fun verifyInstallation(packageName: String, obbFiles: List<File>) {
//    if (!isPackageInstalled(packageName)) return exitWithError("Package $packageName is not installed.")
//
//    val remoteObbFiles = getRemoteObbFiles("/storage/emulated/0/Android/obb/$packageName/")
//    if (obbFiles.size != remoteObbFiles.size) exitWithError("Mismatch in OBB files.")
//
//    obbFiles.forEach { local ->
//        remoteObbFiles[local.name]?.let { if (it != local.length()) exitWithError("Size mismatch for ${local.name}") }
//            ?: exitWithError("Missing OBB file: ${local.name}")
//    }
//
//    println("Verification successful.")
//}


//fun runCommand(vararg commands: String, showOutput: Boolean = true): Boolean {
//    val command = commands.asList()
//    println("***** ${command.joinToString(" ")}")
//    return try {
//        val process = ProcessBuilder(command)
//            .redirectErrorStream(true) // Merge stderr with stdout
//            .start()
//
//        if (showOutput) {
//            // Read and print output and error messages in real time
//            val reader = process.inputStream.bufferedReader()
//            reader.lines().forEach { line ->
//                println(line) // Print each line of output
//            }
//        }
//
//        val exitCode = process.waitFor()
//        if (exitCode == 0) true // Command was successful
//        else {
//            println("Error! ${command.joinToString(" ")} -> $exitCode")
//            false // Command failed
//        }
//    } catch (e: IOException) {
//        println("Error executing command: ${e.message}")
//        false // Exception occurred
//    }
//}

data class CommandResult(private val code: Int, val message: String) {
    val success = code == 0
    val failed = !success
    override fun toString() = "Cmd($code, $message)"
}

fun runCommand(vararg commands: String, showOutput: Boolean = true): CommandResult {
    val commandList = commands.asList()
    return try {
        val process = ProcessBuilder(commandList).redirectErrorStream(true).start() // Merge stderr with stdout

        // Read and print output and error messages in real time
//        if (showOutput) process.inputStream.bufferedReader().use {
//            it.lines().forEach { line -> println(line) } // Print each line of output
//        }

        val result = process.inputStream.bufferedReader().use { it.readText().trim() }
        //if (showOutput) println(result)

        val exitCode = process.waitFor()
        //if (exitCode != 0) println("!!! runCommand Error.")
        CommandResult(exitCode, result)
    } catch (e: Exception) {
        println("!!! runCommand Exception: ${e.message}")
        CommandResult(-999, e.message.orEmpty())
    }.also { println("@@ runCommand[ ${commandList.joinToString(" ")} ]-> $it") }
}

fun File.getPackageNameFromApk(): String? {
    val r = runCommand("aapt2", "dump", "packagename", absolutePath)
    return if (r.success && r.message.isNotEmpty()) r.message // Successfully retrieved package name
    else {
        println("Failed to retrieve package name: $r")
        null
    }
}

fun adb(vararg commands: String, showOutput: Boolean = true) = runCommand("adb", *commands, showOutput = showOutput)


//fun File.getPackageNameFromApk(): String? {
//    val command = listOf("aapt2", "dump", "packagename", absolutePath)
//    return try {
//        val process = ProcessBuilder(command).redirectErrorStream(true).start()
//        val result = process.inputStream.bufferedReader().readText().trim()
//        val exitCode = process.waitFor()
//
//        if (exitCode == 0 && result.isNotEmpty()) {
//            result // Successfully retrieved package name
//        } else {
//            println("Failed to retrieve package name($exitCode): $result")
//            null
//        }
//    } catch (e: IOException) {
//        println("Error executing aapt2: ${e.message}")
//        null
//    }
//}

//import java.io.File
//import java.io.IOException
//
//fun main(args: Array<String>) {
//    val currentDir = File("").absolutePath
//    val packageName = currentDir.substringAfterLast(File.separator)
//
//    if (args.isEmpty()) {
//        printHelp()
//        return
//    }
//
//    when (args[0]) {
//        "-v" -> verify(packageName)
//        "-i" -> install(packageName)
//        "-r" -> remove(packageName)
//        "-r-i" -> {
//            remove(packageName)
//            install(packageName)
//        }
//        else -> printHelp()
//    }
//}
//
//fun printHelp() {
//    println(
//        """
//        Usage:
//        java -jar apkInstaller.jar [-v | -i | -r | -r-i]
//        -v    : verify the installation of APK and OBB
//        -i    : install APK and OBB, then verify
//        -r    : uninstall APK and OBB
//        -r-i  : uninstall, install, and verify APK and OBB
//        """.trimIndent()
//    )
//}
//
//fun runCommand(command: List<String>): Boolean {
//    return try {
//        val process = ProcessBuilder(command)
//            .redirectErrorStream(true)
//            .start()
//
//        val exitCode = process.waitFor()
//        if (exitCode != 0) {
//            println("Command failed: ${command.joinToString(" ")}")
//            false
//        } else {
//            true
//        }
//    } catch (e: IOException) {
//        println("Error executing command: ${e.message}")
//        false
//    }
//}
//
//fun verify(packageName: String) {
//    // Check if the package is installed
//    val isInstalled = runCommand(listOf("adb", "shell", "pm", "list", "packages", packageName))
//    if (!isInstalled) {
//        println("Package $packageName is not installed.")
//        return
//    }
//
//    // Verify OBB file existence and sizes
//    val obbPath = "/storage/emulated/0/Android/obb/$packageName/"
//    val obbFiles = listOf("main.1.$packageName.obb", "patch.1.$packageName.obb")
//    obbFiles.forEach { obbFile ->
//        val command = listOf("adb", "shell", "ls", "$obbPath$obbFile")
//        val exists = runCommand(command)
//        if (!exists) {
//            println("OBB file $obbFile not found on device.")
//        } else {
//            println("OBB file $obbFile is present on device.")
//        }
//    }
//}
//
//fun install(packageName: String) {
//    // Find and install the APK file
//    val apkFiles = File(".").listFiles { _, name -> name.endsWith(".apk") }
//    if (apkFiles == null || apkFiles.size != 1) {
//        println("Error: There should be exactly one APK file in the directory.")
//        return
//    }
//    val apkFile = apkFiles[0].absolutePath
//    if (!runCommand(listOf("adb", "install", "-r", "-d", apkFile))) {
//        println("Error installing APK.")
//        return
//    }
//
//    // Copy OBB files to the device
//    val obbPath = "/storage/emulated/0/Android/obb/$packageName/"
//    val obbFiles = listOf("main.1.$packageName.obb", "patch.1.$packageName.obb")
//    obbFiles.forEach { obbFile ->
//        val command = listOf("adb", "push", obbFile, obbPath)
//        if (!runCommand(command)) {
//            println("Error copying $obbFile to device.")
//        }
//    }
//
//    verify(packageName)
//}
//
//fun remove(packageName: String) {
//    // Uninstall the APK
//    if (!runCommand(listOf("adb", "uninstall", packageName))) {
//        println("Error uninstalling package $packageName.")
//        return
//    }
//
//    // Remove OBB directory if it exists
//    val obbPath = "/storage/emulated/0/Android/obb/$packageName/"
//    if (runCommand(listOf("adb", "shell", "ls", obbPath))) {
//        println("Warning: OBB directory exists. Deleting...")
//        if (!runCommand(listOf("adb", "shell", "rm", "-rf", obbPath))) {
//            println("Error deleting OBB directory.")
//        }
//    }
//
//    verifyUninstall(packageName)
//}
//
//fun verifyUninstall(packageName: String) {
//    val isInstalled = runCommand(listOf("adb", "shell", "pm", "list", "packages", packageName))
//    if (isInstalled) {
//        println("Error: Package $packageName is still installed.")
//    } else {
//        println("Package $packageName is uninstalled.")
//    }
//
//    val obbPath = "/storage/emulated/0/Android/obb/$packageName/"
//    if (runCommand(listOf("adb", "shell", "ls", obbPath))) {
//        println("Error: OBB directory $obbPath still exists.")
//    } else {
//        println("OBB directory $obbPath is removed.")
//    }
//}


//import java.io.File
//import java.io.BufferedReader
//import kotlin.system.exitProcess
//
//fun main(args: Array<String>) {
//    val currentDir = File(".").canonicalPath
//    val apkFile = findFile(currentDir, "apk") ?: exitWithError("No APK file found.")
//    val obbFiles = findFiles(currentDir, "obb")
//    val packageName = findPackageName(currentDir) ?: exitWithError("No package name file found.")
//
//    when (args.getOrNull(0)) {
//        "-v" -> verifyInstallation(packageName, obbFiles)
//        "-i" -> installAndVerify(apkFile, obbFiles, packageName)
//        else -> printHelp()
//    }
//}
//
//fun printHelp() = println(
//    """Usage:
//    |java -jar apkInstaller.jar -v   // Verify installation
//    |java -jar apkInstaller.jar -i   // Install and verify files""".trimMargin()
//)
//
//fun exitWithError(message: String): Nothing {
//    println("Error: $message")
//    exitProcess(1)
//}
//
//fun findPackageName(dir: String) = File(dir).listFiles()?.firstOrNull { it.length() == 0L }?.name
//
//fun findFile(dir: String, extension: String) = File(dir).listFiles()?.firstOrNull { it.extension == extension }
//
//fun findFiles(dir: String, extension: String) =
//    File(dir).listFiles()?.filter { it.extension == extension } ?: emptyList()
//
//fun verifyInstallation(packageName: String, obbFiles: List<File>) {
//    if (!isPackageInstalled(packageName)) return exitWithError("Package $packageName is not installed.")
//
//    val remoteObbFiles = getRemoteObbFiles("/storage/emulated/0/Android/obb/$packageName/")
//    if (obbFiles.size != remoteObbFiles.size) exitWithError("Mismatch in OBB files.")
//
//    obbFiles.forEach { local ->
//        remoteObbFiles[local.name]?.let { if (it != local.length()) exitWithError("Size mismatch for ${local.name}") }
//            ?: exitWithError("Missing OBB file: ${local.name}")
//    }
//
//    println("Verification successful.")
//}
//
//fun installAndVerify(apkFile: File, obbFiles: List<File>, packageName: String) {
//    if (!installApk(apkFile)) return exitWithError("Failed to install APK.")
//
//    obbFiles.forEach { if (!pushObbFile(it, packageName)) exitWithError("Failed to copy OBB: ${it.name}") }
//    println("OBB files copied successfully.")
//
//    verifyInstallation(packageName, obbFiles)
//}
//
//fun isPackageInstalled(packageName: String) =
//    executeCommand("adb shell pm list packages $packageName").contains("package:$packageName")
//
//fun installApk(apkFile: File) =
//    executeCommand("adb install -r -d \"${apkFile.absolutePath}\"").contains("Success")
//
//fun pushObbFile(obbFile: File, packageName: String) =
//    executeCommand("adb push \"${obbFile.absolutePath}\" \"/storage/emulated/0/Android/obb/$packageName/\"").contains("file pushed")
//
//fun getRemoteObbFiles(obbDir: String) =
//    executeCommand("adb shell ls -l $obbDir").lines()
//        .filter { it.startsWith("-") }
//        .mapNotNull {
//            it.split("\\s+".toRegex()).let { parts ->
//                if (parts.size >= 5) parts.lastOrNull()?.let { name -> name to parts[4].toLongOrNull() } else null
//            }
//        }.toMap()
//
//fun executeCommand(command: String) = try {
//    Runtime.getRuntime().exec(command).inputStream.bufferedReader().use(BufferedReader::readText)
//} catch (e: Exception) {
//    println("Command failed: $command - ${e.message}")
//    ""
//}


//fun main(args: Array<String>) {
//    println("Hello World!")
//
//    // Try adding program arguments via Run/Debug configuration.
//    // Learn more about running applications: https://www.jetbrains.com/help/idea/running-applications.html.
//    println("Program arguments: ${args.joinToString()}")
//}