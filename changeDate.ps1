param (
    [string]$date,  # Format attendu : dd/MM/yyyy
    [string]$time   # Format attendu : HH:mm
)

# Vérifier si les arguments sont fournis
if (-not $date -or -not $time) {
    Write-Host "Veuillez fournir une date (format dd/MM/yyyy) et une heure (format HH:mm)."
    exit
}

# Parse la date et l'heure
try {
    $parsedDate = [datetime]::ParseExact($date, 'dd/MM/yyyy', $null)
    $parsedTime = [datetime]::ParseExact($time, 'HH:mm', $null)
} catch {
    Write-Host "Erreur: La date ou l'heure n'est pas dans le bon format."
    exit
}

# Extraire les composants de la date et de l'heure
$day = $parsedDate.ToString("dd")
$month = $parsedDate.ToString("MM")
$year = $parsedDate.ToString("yyyy")

$hour = $parsedTime.ToString("HH")
$minute = $parsedTime.ToString("mm")

# Formater la date et l'heure pour la commande ADB
$adbDateTime = "${month}${day}${hour}${minute}${year}.00"

# Exécuter la commande ADB pour changer la date et l'heure
$adbCommand = "adb shell ""date $adbDateTime"""
Write-Host "Exécution de la commande ADB : $adbCommand"
Invoke-Expression $adbCommand

# Vérifier si la date et l'heure ont été correctement mises à jour
$checkDateCommand = "adb shell date"
Write-Host "Vérification de la date et de l'heure :"
Invoke-Expression $checkDateCommand
