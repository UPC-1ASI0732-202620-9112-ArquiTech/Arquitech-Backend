$java17 = $null

# Buscar Temurin / Adoptium
$adoptium = Get-ChildItem "C:\Program Files\Eclipse Adoptium" `
    -Directory `
    -Filter "jdk-17*" `
    -ErrorAction SilentlyContinue |
    Sort-Object Name -Descending |
    Select-Object -First 1

if ($adoptium) {
    $java17 = $adoptium.FullName
}

# Si no apareció, buscar instalaciones Java normales
if (-not $java17) {
    $java = Get-ChildItem "C:\Program Files\Java" `
        -Directory `
        -Filter "jdk-17*" `
        -ErrorAction SilentlyContinue |
        Sort-Object Name -Descending |
        Select-Object -First 1

    if ($java) {
        $java17 = $java.FullName
    }
}

# Si no encuentra Java 17
if (-not $java17) {
    Write-Host "No se encontro JDK 17."
    Write-Host "Instala Java 17 antes de ejecutar ArquiTech."
    exit 1
}

$env:JAVA_HOME = $java17
$env:Path = "$env:JAVA_HOME\bin;$env:Path"

Write-Host "Usando:"
java -version

.\mvnw.cmd spring-boot:run