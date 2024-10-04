@echo off
rem This batch file runs apkInstaller.jar located in the same folder with arguments

set JAR_PATH=%~dp0apkInstaller.jar

rem If no arguments are passed, display usage help
if "%*"=="" (
    java -jar "%JAR_PATH%"
    exit /b
)

rem Run the jar with the passed arguments
java -jar "%JAR_PATH%" %*
