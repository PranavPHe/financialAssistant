Clear-Host # Clears the screen

javac -cp ".\lib\snakeyaml-2.5.jar;." -d . .\src\Main.java

if ($LASTEXITCODE -ne 0) {
    Write-Host "Compilation failed"
    exit 1
}

java -cp ".\lib\snakeyaml-2.5.jar;." src.Main