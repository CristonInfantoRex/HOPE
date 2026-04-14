@echo off
echo ============================================================
echo  Java Multi-threaded Download Manager -- Build ^& Run
echo ============================================================

:: Compile
if not exist out mkdir out
echo Compiling...
javac -d out src\*.java
if errorlevel 1 (
    echo Compilation FAILED. Check errors above.
    pause
    exit /b 1
)
echo Compilation OK.

:: Run (pass extra args as URLs if provided, else demo mode)
echo.
echo Running Download Manager...
echo.
java -cp out Main %*

pause
