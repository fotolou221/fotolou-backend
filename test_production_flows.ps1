$ErrorActionPreference = "Stop"

Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host "🚀 DEBUT DU TEST D'INTEGRATION COMPLET FOTOLOU EN PROD" -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan

$BaseUrl = "http://localhost:8080/api"

# 1. Admin Authentication
Write-Host "`n[1/9] Test Authentification Admin..." -ForegroundColor Yellow
$AdminAuthPayload = @{
    username = "admin@fotolou.sn"
    password = "admin_fotolou_2026"
    rememberMe = $true
} | ConvertTo-Json

$AdminLoginRes = Invoke-RestMethod -Uri "$BaseUrl/authenticate" -Method Post -Body $AdminAuthPayload -ContentType "application/json"
$AdminToken = $AdminLoginRes.id_token
if (-not $AdminToken) { throw "Impossible de récupérer le token Admin" }
Write-Host "✅ Admin authentifié avec succès (Token reçu)" -ForegroundColor Green

$AdminHeaders = @{
    Authorization = "Bearer $AdminToken"
}

# 2. Création de Salon + Vérification Rôle Coiffeur Propriétaire
Write-Host "`n[2/9] Test Création Salon & Attribution Rôle ROLE_COIFFEUR..." -ForegroundColor Yellow
$UniqueSuffix = Get-Random -Minimum 1000 -Maximum 9999
$CoiffeurPhone = "+22177$($UniqueSuffix)88"
$SalonPayload = @{
    name = "Salon Royal Barbier $UniqueSuffix"
    slug = "royal-barbier-$UniqueSuffix"
    location = "Almadies, Dakar"
    district = "Dakar Ouest"
    status = "OPEN"
    phone = $CoiffeurPhone
    avatarUrl = "images/salons/king-barber-avatar.png"
    coverUrl = "images/salons/king-barber-cover.png"
    latitude = 14.716677
    longitude = -17.467686
    peopleWaiting = 0
    estimatedWaitMinutes = 0
    active = $true
} | ConvertTo-Json

$CreatedSalon = Invoke-RestMethod -Uri "$BaseUrl/salons" -Method Post -Body $SalonPayload -ContentType "application/json" -Headers $AdminHeaders
$SalonId = $CreatedSalon.id
Write-Host "✅ Salon créé ID=$SalonId, Nom=$($CreatedSalon.name)" -ForegroundColor Green

# Vérifier que le coiffeur propriétaire a le rôle ROLE_COIFFEUR et NON ROLE_CLIENT
$CleanPhone = $CoiffeurPhone.Replace(" ", "")
$OwnerUser = Invoke-RestMethod -Uri "$BaseUrl/admin/users/$CleanPhone" -Method Get -Headers $AdminHeaders
$HasCoiffeur = $OwnerUser.authorities -contains "ROLE_COIFFEUR"
$HasClient = $OwnerUser.authorities -contains "ROLE_CLIENT"

if (-not $HasCoiffeur) { throw "ERREUR: Le propriétaire du salon n'a pas ROLE_COIFFEUR !" }
if ($HasClient) { throw "ERREUR: Le coiffeur a le rôle ROLE_CLIENT, ce qui est interdit !" }
Write-Host "✅ Utilisateur Coiffeur vérifié : Login=$($OwnerUser.login), Authorities=$($OwnerUser.authorities -join ', ')" -ForegroundColor Green

# 3. Connexion du Coiffeur via OTP
Write-Host "`n[3/9] Test Connexion OTP Coiffeur..." -ForegroundColor Yellow
Invoke-RestMethod -Uri "$BaseUrl/auth/otp/send" -Method Post -Body (@{ phone = $CoiffeurPhone; role = "COIFFEUR" } | ConvertTo-Json) -ContentType "application/json"
$CoiffeurAuth = Invoke-RestMethod -Uri "$BaseUrl/auth/otp/verify" -Method Post -Body (@{ phone = $CoiffeurPhone; code = "123456" } | ConvertTo-Json) -ContentType "application/json"

$CoiffeurToken = $CoiffeurAuth.token
if ($CoiffeurAuth.user.role -ne "coiffeur") { throw "ERREUR: Le rôle dans le profil n'est pas 'coiffeur' !" }
if ($CoiffeurAuth.user.homeRoute -ne "/coiffeur/home") { throw "ERREUR: homeRoute n'est pas '/coiffeur/home' !" }
Write-Host "✅ Coiffeur connecté avec succès : Role=$($CoiffeurAuth.user.role), HomeRoute=$($CoiffeurAuth.user.homeRoute), SalonId=$($CoiffeurAuth.user.salonId)" -ForegroundColor Green

$CoiffeurHeaders = @{
    Authorization = "Bearer $CoiffeurToken"
}

# 4. Bascule Ouvrir / Fermer la file du Salon en temps réel
Write-Host "`n[4/9] Test Bascule Statut File Salon (Toggle Status)..." -ForegroundColor Yellow
$ToggleRes1 = Invoke-RestMethod -Uri "$BaseUrl/salons/$SalonId/toggle-status" -Method Put -Headers $CoiffeurHeaders
if ($ToggleRes1.status -ne "CLOSED") { throw "ERREUR: Le statut attendu après toggle 1 était CLOSED !" }
Write-Host "✅ Toggle 1 : Salon passé à CLOSED" -ForegroundColor Green

$ToggleRes2 = Invoke-RestMethod -Uri "$BaseUrl/salons/$SalonId/toggle-status" -Method Put -Headers $CoiffeurHeaders
if ($ToggleRes2.status -ne "OPEN") { throw "ERREUR: Le statut attendu après toggle 2 était OPEN !" }
Write-Host "✅ Toggle 2 : Salon repassé à OPEN" -ForegroundColor Green

# 5. Connexion Client & Réservation de Tickets
Write-Host "`n[5/9] Test Prise de Tickets par un Client..." -ForegroundColor Yellow
$ClientPhone = "+22177$($UniqueSuffix)99"
Invoke-RestMethod -Uri "$BaseUrl/auth/otp/send" -Method Post -Body (@{ phone = $ClientPhone; role = "CLIENT" } | ConvertTo-Json) -ContentType "application/json"
$ClientAuth = Invoke-RestMethod -Uri "$BaseUrl/auth/otp/verify" -Method Post -Body (@{ phone = $ClientPhone; code = "123456" } | ConvertTo-Json) -ContentType "application/json"
$ClientToken = $ClientAuth.token
$ClientHeaders = @{ Authorization = "Bearer $ClientToken" }

# Réservation Ticket 1 (Moi) + Ticket 2 (Proche)
$BookingPayload = @{
    salonId = $SalonId
    beneficiaries = @(
        @{ ownerType = "SELF"; ownerName = "Amadou Diallo" },
        @{ ownerType = "RELATIVE"; ownerName = "Ibrahima Diallo (Fils)" }
    )
} | ConvertTo-Json

$BookedTickets = Invoke-RestMethod -Uri "$BaseUrl/tickets/book-multiple" -Method Post -Body $BookingPayload -ContentType "application/json" -Headers $ClientHeaders
if ($BookedTickets.Count -ne 2) { throw "ERREUR: 2 tickets devaient être créés !" }
$Ticket1 = $BookedTickets[0]
$Ticket2 = $BookedTickets[1]
Write-Host "✅ Tickets réservés : #$($Ticket1.ticketNumber) ($($Ticket1.ownerName)) & #$($Ticket2.ticketNumber) ($($Ticket2.ownerName))" -ForegroundColor Green

# 6. Vérification File d'attente du Salon & Affluence
Write-Host "`n[6/9] Test File d'attente du Salon & Temps d'attente..." -ForegroundColor Yellow
$Queue = Invoke-RestMethod -Uri "$BaseUrl/salons/$SalonId/queue" -Method Get
Write-Host "✅ File active du salon : $($Queue.Count) personnes en attente" -ForegroundColor Green

# 7. Appel du Prochain Ticket (Coiffeur) & Alerte proactive
Write-Host "`n[7/9] Test Progression File & Appel Prochain Ticket..." -ForegroundColor Yellow
$CalledTicket = Invoke-RestMethod -Uri "$BaseUrl/tickets/$($Ticket1.id)/call-next" -Method Post -Headers $CoiffeurHeaders
Write-Host "✅ Ticket #$($CalledTicket.ticketNumber) appelé (Statut: $($CalledTicket.status))" -ForegroundColor Green

# 8. Annulation de Ticket & Notification In-App
Write-Host "`n[8/9] Test Annulation de Ticket par le Client..." -ForegroundColor Yellow
$CancelledTicket = Invoke-RestMethod -Uri "$BaseUrl/tickets/$($Ticket2.id)/cancel" -Method Post -Headers $ClientHeaders
Write-Host "✅ Ticket #$($CancelledTicket.ticketNumber) annulé (Statut: $($CancelledTicket.status))" -ForegroundColor Green

# Test des notifications in-app pour le client et le coiffeur
$ClientNotifs = Invoke-RestMethod -Uri "$BaseUrl/notifications/my-notifications" -Method Get -Headers $ClientHeaders
Write-Host "✅ Notifications Client reçues : $($ClientNotifs.Count) notification(s)" -ForegroundColor Green
foreach ($n in $ClientNotifs) {
    Write-Host "   - [$($n.type)] $($n.title) : $($n.message)" -ForegroundColor Gray
}

# Marquer une notification comme lue
if ($ClientNotifs.Count -gt 0) {
    $FirstNotifId = $ClientNotifs[0].id
    $ReadNotif = Invoke-RestMethod -Uri "$BaseUrl/notifications/$FirstNotifId/read" -Method Patch -Headers $ClientHeaders
    Write-Host "✅ Notification #$FirstNotifId marquée comme lue (isRead=$($ReadNotif.isRead))" -ForegroundColor Green
}

# 9. Test Complet Boutique Fotolou Shop & Commande
Write-Host "`n[9/9] Test Complet Fotolou Shop (Catalogue & Commande)..." -ForegroundColor Yellow
$Categories = Invoke-RestMethod -Uri "$BaseUrl/categories" -Method Get
Write-Host "✅ Catégories boutique disponibles : $($Categories.Count)" -ForegroundColor Green

$Products = Invoke-RestMethod -Uri "$BaseUrl/products" -Method Get
if ($Products.Count -eq 0) { throw "ERREUR: Aucun produit dans la boutique !" }
$Prod1 = $Products[0]
Write-Host "✅ Produits boutique disponibles : $($Products.Count) (Exemple: $($Prod1.title) - $($Prod1.price) FCFA)" -ForegroundColor Green

# Passer une commande
$OrderPayload = @{
    items = @(
        @{ productId = $Prod1.id; quantity = 2 }
    )
    deliveryAddress = "Almadies Zone 4, Villa 12"
    deliveryDistrict = "Dakar Ouest"
    orderType = "WHATSAPP"
    customerName = "Amadou Diallo"
    customerPhone = $ClientPhone
    notes = "Appeler à l'arrivée"
} | ConvertTo-Json

$OrderResult = Invoke-RestMethod -Uri "$BaseUrl/orders/checkout" -Method Post -Body $OrderPayload -ContentType "application/json" -Headers $ClientHeaders
Write-Host "✅ Commande créée : N°$($OrderResult.orderNumber), Total=$($OrderResult.totalPrice) FCFA, Statut=$($OrderResult.status)" -ForegroundColor Green
Write-Host "   Lien WhatsApp généré : $($OrderResult.whatsAppUrl.Substring(0, [Math]::Min(60, $OrderResult.whatsAppUrl.Length)))..." -ForegroundColor Gray

# Mise à jour du statut de commande par Admin
$UpdatedOrder = Invoke-RestMethod -Uri "$BaseUrl/orders/$($OrderResult.id)/status" -Method Patch -Body (@{ status = "LIVRE" } | ConvertTo-Json) -ContentType "application/json" -Headers $AdminHeaders
Write-Host "✅ Commande passée à : $($UpdatedOrder.status)" -ForegroundColor Green

# Vérification finale des notifications du client après commande
$FinalClientNotifs = Invoke-RestMethod -Uri "$BaseUrl/notifications/my-notifications" -Method Get -Headers $ClientHeaders
Write-Host "✅ Total notifications client après commande et livraison : $($FinalClientNotifs.Count)" -ForegroundColor Green

Write-Host "`n==========================================================" -ForegroundColor Cyan
Write-Host "🎉 TOUS LES TESTS D'INTEGRATION SONT VALIDES AVEC SUCCES !" -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan
