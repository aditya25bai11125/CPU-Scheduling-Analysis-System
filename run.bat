@echo off

if not exist out\com\vit\cpusimulator\Main.class (
    call compile.bat
    if errorlevel 1 exit /b 1
)

java -cp out com.vit.cpusimulator.Main %*
