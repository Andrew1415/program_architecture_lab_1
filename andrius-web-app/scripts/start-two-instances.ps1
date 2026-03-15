$projectRoot = Split-Path -Parent $PSScriptRoot

Start-Process cmd.exe -ArgumentList "/k", "set PORT=25256 && gradlew.bat bootRun" -WorkingDirectory $projectRoot
Start-Sleep -Seconds 2
Start-Process cmd.exe -ArgumentList "/k", "set PORT=25257 && gradlew.bat bootRun" -WorkingDirectory $projectRoot
