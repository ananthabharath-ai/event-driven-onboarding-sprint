@echo off
    echo Setting JAVA_HOME to JDK 17...
    set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.17.10-hotspot
    set Path=%JAVA_HOME%\bin;%Path%
    echo Java 17 environment is ready.
    
    REM This line starts a new command prompt
    cmd.exe