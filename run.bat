@echo off
title Ombits Leave Management Backend
echo Starting Ombits Leave Management System (Spring Boot)...

if exist "C:\Program Files\JetBrains\IntelliJ IDEA 2025.3.2\jbr\bin\java.exe" (
    set "JAVA_HOME=C:\Program Files\JetBrains\IntelliJ IDEA 2025.3.2\jbr"
    set "PATH=C:\Program Files\JetBrains\IntelliJ IDEA 2025.3.2\jbr\bin;%PATH%"
)

call "%~dp0mvnw.cmd" spring-boot:run
pause
