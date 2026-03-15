param(
    [Parameter(Mandatory = $true)]
    [int]$Port
)

$env:PORT = "$Port"
& "$PSScriptRoot\..\gradlew.bat" bootRun
