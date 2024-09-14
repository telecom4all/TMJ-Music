# Fonction pour convertir les millisecondes depuis l'époque Unix en date et heure locale
function Convert-UnixTime {
    param (
        [long]$unixTime
    )

    # Obtenir la date et heure locale de l'émulateur en prenant en compte le fuseau horaire et l'heure d'été
    return (Get-Date "1970-01-01 00:00:00Z").AddMilliseconds($unixTime).ToLocalTime()
}

# Récupérer les informations du téléphone
$phoneModel = adb shell getprop ro.product.model
$phoneManufacturer = adb shell getprop ro.product.manufacturer
$timeZone = adb shell getprop persist.sys.timezone

# Afficher les informations du téléphone
Write-Host "Nom du téléphone : $phoneManufacturer $phoneModel"
Write-Host "Fuseau horaire : $timeZone"
Write-Host "--------------------" -ForegroundColor Yellow

# Exécuter la commande adb et filtrer les alarmes pour be.telecom4all.tmjmusic
$alarms = adb shell dumpsys alarm | Select-String "be.telecom4all.tmjmusic"

# Lister toutes les alarmes liées à be.telecom4all.tmjmusic
$alarms | ForEach-Object {
    # Vérifier si la ligne contient l'heure RTC en millisecondes "when"
    if ($_ -match "when (\d+)") {
        $timestamp = [long]$matches[1]

        # Convertir les millisecondes en date et heure lisible
        $alarmTime = Convert-UnixTime -unixTime $timestamp

        # Afficher l'heure de l'alarme
        Write-Host "Alarme pour TMJ-Music à : $alarmTime"
    }
    # Vérifier s'il s'agit d'un tag d'alarme avec un message
    elseif ($_ -match "tag=\*walarm\*:(.*)") {
        $tag = $matches[1]
        Write-Host "Type d'alarme : $tag"
    }
    # Extraire le message de l'alarme si disponible
    elseif ($_ -match "operation=PendingIntent\{.*\}: PendingIntentRecord\{.*broadcastIntent.*extras=.*msg='(.*)'") {
        $alarmMessage = $matches[1]
        Write-Host "Message de l'alarme : $alarmMessage"
    }
}

# Affichage d'une ligne séparatrice après chaque alarme
Write-Host "--------------------" -ForegroundColor Yellow
