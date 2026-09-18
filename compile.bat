@echo off
setlocal

if exist out rmdir /s /q out
mkdir out

set "SOURCE_LIST=%TEMP%\cpu_scheduler_sources.txt"
if exist "%SOURCE_LIST%" del "%SOURCE_LIST%"

for /r "src\main\java" %%f in (*.java) do echo "%%f">>"%SOURCE_LIST%"

javac --release 17 -encoding UTF-8 -d out @"%SOURCE_LIST%"

if errorlevel 1 (
    del "%SOURCE_LIST%" >nul 2>&1
    exit /b 1
)

del "%SOURCE_LIST%" >nul 2>&1
echo.
echo Compilation successful.
endlocal