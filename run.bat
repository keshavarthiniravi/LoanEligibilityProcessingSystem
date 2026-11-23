@echo off
cd /d "%~dp0"
if not exist out mkdir out
dir /s /b src\*.java > sources.txt
javac -d out @sources.txt
if errorlevel 1 (
  echo Compilation failed. Make sure JDK 17+ is installed.
  pause
  exit /b 1
)
del sources.txt
java -cp out com.loansystem.Main
pause
