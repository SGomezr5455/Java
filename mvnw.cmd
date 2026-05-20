@REM ----------------------------------------------------------------------------
@REM Maven Start Up Batch script
@REM ----------------------------------------------------------------------------
@echo off
set MAVEN_PROJECTBASEDIR=%~dp0
set MVNW_REPOURL=https://repo.maven.apache.org/maven2

if not exist "%USERPROFILE%\.m2\wrapper\dists\apache-maven-3.9.9-bin" (
    echo Downloading Maven 3.9.9...
    mkdir "%USERPROFILE%\.m2\wrapper\dists" 2>nul
)

set MVN_CMD=%USERPROFILE%\.m2\wrapper\dists\apache-maven-3.9.9-bin\apache-maven-3.9.9\bin\mvn.cmd

if not exist "%MVN_CMD%" (
    echo Maven not found. Please open this project in IntelliJ IDEA which will manage Maven automatically.
    echo Or install Maven from https://maven.apache.org/download.cgi and add it to PATH.
    exit /b 1
)

"%MVN_CMD%" %*
