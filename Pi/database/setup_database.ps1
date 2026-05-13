# Script PowerShell pour créer la base de données BioSync
# Encodage UTF-8

Write-Host ""
Write-Host "╔════════════════════════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host "║         Configuration BioSync - Base de Données           ║" -ForegroundColor Cyan
Write-Host "╚════════════════════════════════════════════════════════════╝" -ForegroundColor Cyan
Write-Host ""

# Vérifier si MySQL est accessible
$mysqlPath = Get-Command mysql -ErrorAction SilentlyContinue

if (-not $mysqlPath) {
    Write-Host "❌ MySQL n'est pas trouvé dans le PATH système." -ForegroundColor Red
    Write-Host ""
    Write-Host "💡 Solutions possibles :" -ForegroundColor Yellow
    Write-Host "   1. Ajoutez MySQL au PATH" -ForegroundColor White
    Write-Host "   2. Ou entrez le chemin complet vers mysql.exe" -ForegroundColor White
    Write-Host ""
    
    $customPath = Read-Host "Entrez le chemin complet vers mysql.exe (ou laissez vide pour annuler)"
    
    if ([string]::IsNullOrWhiteSpace($customPath)) {
        Write-Host ""
        Write-Host "⚠️  Installation annulée." -ForegroundColor Yellow
        Read-Host "Appuyez sur Entrée pour quitter"
        exit 1
    }
    
    $mysqlPath = $customPath
} else {
    $mysqlPath = "mysql"
}

Write-Host ""
Write-Host "🔐 Configuration MySQL" -ForegroundColor Cyan
Write-Host ""

$mysqlUser = Read-Host "Nom d'utilisateur MySQL [root]"
if ([string]::IsNullOrWhiteSpace($mysqlUser)) {
    $mysqlUser = "root"
}

$mysqlPass = Read-Host "Mot de passe MySQL (laissez vide si aucun)" -AsSecureString
$mysqlPassPlain = [Runtime.InteropServices.Marshal]::PtrToStringAuto(
    [Runtime.InteropServices.Marshal]::SecureStringToBSTR($mysqlPass)
)

Write-Host ""
Write-Host "📦 Création de la base de données..." -ForegroundColor Cyan
Write-Host ""

# Construire la commande
$scriptPath = Join-Path $PSScriptRoot "create_database.sql"

try {
    if ([string]::IsNullOrWhiteSpace($mysqlPassPlain)) {
        & $mysqlPath -u $mysqlUser -e "source $scriptPath" 2>&1 | Out-Null
    } else {
        & $mysqlPath -u $mysqlUser "-p$mysqlPassPlain" -e "source $scriptPath" 2>&1 | Out-Null
    }
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host ""
        Write-Host "╔════════════════════════════════════════════════════════════╗" -ForegroundColor Green
        Write-Host "║              ✅ INSTALLATION RÉUSSIE !                     ║" -ForegroundColor Green
        Write-Host "╚════════════════════════════════════════════════════════════╝" -ForegroundColor Green
        Write-Host ""
        Write-Host "📊 Base de données créée : " -NoNewline -ForegroundColor White
        Write-Host "biosync" -ForegroundColor Cyan
        Write-Host "👥 Utilisateurs de test :" -ForegroundColor White
        Write-Host ""
        Write-Host "   ┌─────────────┬───────────────────────┬──────────────┐" -ForegroundColor Gray
        Write-Host "   │    Rôle     │        Email          │ Mot de passe │" -ForegroundColor Gray
        Write-Host "   ├─────────────┼───────────────────────┼──────────────┤" -ForegroundColor Gray
        Write-Host "   │ 👑 Admin    │ admin@biosync.com     │ admin123     │" -ForegroundColor White
        Write-Host "   │ 🏋️ Coach    │ coach@biosync.com     │ coach123     │" -ForegroundColor White
        Write-Host "   │ 👤 User     │ user@biosync.com      │ user123      │" -ForegroundColor White
        Write-Host "   └─────────────┴───────────────────────┴──────────────┘" -ForegroundColor Gray
        Write-Host ""
        Write-Host "🚀 Vous pouvez maintenant lancer l'application !" -ForegroundColor Green
        Write-Host ""
    } else {
        throw "Erreur lors de l'exécution du script SQL"
    }
} catch {
    Write-Host ""
    Write-Host "╔════════════════════════════════════════════════════════════╗" -ForegroundColor Red
    Write-Host "║                ❌ ERREUR D'INSTALLATION                    ║" -ForegroundColor Red
    Write-Host "╚════════════════════════════════════════════════════════════╝" -ForegroundColor Red
    Write-Host ""
    Write-Host "🔍 Vérifications à faire :" -ForegroundColor Yellow
    Write-Host "   1. MySQL est installé et démarré" -ForegroundColor White
    Write-Host "   2. Le mot de passe est correct" -ForegroundColor White
    Write-Host "   3. Vous avez les droits nécessaires" -ForegroundColor White
    Write-Host ""
    Write-Host "💡 Pour démarrer MySQL :" -ForegroundColor Yellow
    Write-Host "   net start MySQL80" -ForegroundColor White
    Write-Host ""
    Write-Host "Erreur : $_" -ForegroundColor Red
    Write-Host ""
}

Write-Host ""
Read-Host "Appuyez sur Entrée pour quitter"
