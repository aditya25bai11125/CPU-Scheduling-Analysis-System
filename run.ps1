$ErrorActionPreference = "Stop"

if (-not (Test-Path "out/com/vit/cpusimulator/Main.class")) {
    & ".\compile.ps1"
}

java -cp out com.vit.cpusimulator.Main @args
