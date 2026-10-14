@echo off
chcp 65001 >nul
title Emergency Broadcast System - 1-click run
cd /d "%~dp0"
echo ==================================================
echo  HE THONG CANH BAO KHAN CAP - CHAY 1-CLICK
echo ==================================================
where java >nul 2>nul
if errorlevel 1 (
  echo [LOI] Chua cai Java! Tai JDK 17+ roi chay lai.
  pause
  exit /b 1
)
java -version
if not exist "lib\mysql-connector-j-8.3.0.jar" (
  echo [LOI] Thieu lib\mysql-connector-j-8.3.0.jar
  pause
  exit /b 1
)
if not exist "build\classes-manual" mkdir "build\classes-manual"
echo [1/2] Bien dich...
javac -encoding UTF-8 -cp "lib/mysql-connector-j-8.3.0.jar" -d "build/classes-manual" src/emergencybroadcast/*.java src/emergencybroadcast/*/*.java
if errorlevel 1 (
  echo [LOI] Bien dich that bai!
  pause
  exit /b 1
)
echo [2/2] Chay app...
echo  - Nho bat XAMPP -^> Start MySQL truoc!
echo  - Tai khoan mau: admin/admin123 ^| operator/op123 ^| receiver1/rec123
java -cp "build/classes-manual;lib/mysql-connector-j-8.3.0.jar;." emergencybroadcast.Main
pause
