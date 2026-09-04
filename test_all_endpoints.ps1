# ==============================================================================
# Script de Test End-to-End pour tous les Endpoints Fotolou Backend
# ==============================================================================
$ErrorActionPreference = "Continue"
$baseUrl = "http://localhost:8080"
$global:results = @()

function Test-Endpoint {
    param(
        [string]$Role,
        [string]$Method,
        [string]$Endpoint,
        [hashtable]$Headers = @{},
        [string]$Body = $null
    )

    $url = "$baseUrl$Endpoint"
    Write-Host "------------------------------------------------------------" -ForegroundColor Cyan
    Write-Host "[$Role] $Method $Endpoint" -ForegroundColor Yellow

    try {
        $params = @{
            Uri = $url
            Method = $Method
            ContentType = "application/json"
        }
        if ($Headers.Count -gt 0) {
            $params["Headers"] = $Headers
        }
        if ($Body) {
            $params["Body"] = $Body
        }

        $res = Invoke-RestMethod @params
        $json = $res | ConvertTo-Json -Depth 3 -Compress
        Write-Host "SUCCESS: Status 200/201" -ForegroundColor Green
        
        $global:results += [PSCustomObject]@{
            Role = $Role
            Method = $Method
            Endpoint = $Endpoint
            Status = "OK (200/201)"
            Result = "Passed"
        }
        return $res
    } catch {
        Write-Host "FAILED: $($_.Exception.Message)" -ForegroundColor Red
        $global:results += [PSCustomObject]@{
            Role = $Role
            Method = $Method
            Endpoint = $Endpoint
            Status = "ERROR"
            Result = $_.Exception.Message
        }
        return $null
    }
}

Write-Host "=== 1. TESTS CLIENT & PUBLIC (+221701234567) ===" -ForegroundColor Magenta

# 1.1 OTP Send Client
Test-Endpoint -Role "PUBLIC" -Method "POST" -Endpoint "/api/auth/otp/send" -Body '{"phone":"+221701234567","role":"CLIENT"}'

# 1.2 OTP Verify Client
$clientAuth = Test-Endpoint -Role "PUBLIC" -Method "POST" -Endpoint "/api/auth/otp/verify" -Body '{"phone":"+221701234567","code":"123456"}'
$clientToken = $clientAuth.id_token
$clientHeaders = @{ "Authorization" = "Bearer $clientToken" }

# 1.3 Liste Salons
$salons = Test-Endpoint -Role "CLIENT" -Method "GET" -Endpoint "/api/salons" -Headers $clientHeaders

# 1.4 Detail Salon
Test-Endpoint -Role "CLIENT" -Method "GET" -Endpoint "/api/salons/1" -Headers $clientHeaders

# 1.5 Categories Boutique
$categories = Test-Endpoint -Role "CLIENT" -Method "GET" -Endpoint "/api/product-categories" -Headers $clientHeaders

# 1.6 Produits Boutique
$products = Test-Endpoint -Role "CLIENT" -Method "GET" -Endpoint "/api/products" -Headers $clientHeaders

# 1.7 Proches du Client (GET)
Test-Endpoint -Role "CLIENT" -Method "GET" -Endpoint "/api/relatives" -Headers $clientHeaders

# 1.8 Ajouter un Proche (POST)
Test-Endpoint -Role "CLIENT" -Method "POST" -Endpoint "/api/relatives" -Headers $clientHeaders -Body '{"name":"Fatou Diop","relation":"SOEUR","phone":"+221771112233"}'

# 1.9 Favoris du Client (GET & TOGGLE)
Test-Endpoint -Role "CLIENT" -Method "GET" -Endpoint "/api/favorites/my-favorites" -Headers $clientHeaders
Test-Endpoint -Role "CLIENT" -Method "POST" -Endpoint "/api/favorites/toggle" -Headers $clientHeaders -Body '{"salonId":1}'

# 1.10 Prise de Ticket (POST)
Test-Endpoint -Role "CLIENT" -Method "POST" -Endpoint "/api/tickets" -Headers $clientHeaders -Body '{"ownerName":"Awa Diop","salon":{"id":1}}'

# 1.11 Mes Tickets (GET)
Test-Endpoint -Role "CLIENT" -Method "GET" -Endpoint "/api/tickets" -Headers $clientHeaders

# 1.12 Passer une Commande Boutique (POST Checkout)
Test-Endpoint -Role "CLIENT" -Method "POST" -Endpoint "/api/orders/checkout" -Headers $clientHeaders -Body '{"items":[{"productId":1,"quantity":2}],"deliveryAddress":"Mermoz Pyrotechnie, Dakar","deliveryDistrict":"Mermoz","orderType":"WHATSAPP","customerName":"Awa Diop"}'

# 1.13 Mes Commandes (GET)
Test-Endpoint -Role "CLIENT" -Method "GET" -Endpoint "/api/orders" -Headers $clientHeaders

# 1.14 Mes Notifications (GET)
Test-Endpoint -Role "CLIENT" -Method "GET" -Endpoint "/api/notifications" -Headers $clientHeaders


Write-Host "=== 2. TESTS COIFFEUR / BARBIER (+221774567890) ===" -ForegroundColor Magenta

# 2.1 OTP Send Coiffeur
Test-Endpoint -Role "COIFFEUR" -Method "POST" -Endpoint "/api/auth/otp/send" -Body '{"phone":"+221774567890","role":"COIFFEUR"}'

# 2.2 OTP Verify Coiffeur
$coiffeurAuth = Test-Endpoint -Role "COIFFEUR" -Method "POST" -Endpoint "/api/auth/otp/verify" -Body '{"phone":"+221774567890","code":"123456"}'
$coiffeurToken = $coiffeurAuth.id_token
$coiffeurHeaders = @{ "Authorization" = "Bearer $coiffeurToken" }

# 2.3 Profils Coiffeurs (GET)
Test-Endpoint -Role "COIFFEUR" -Method "GET" -Endpoint "/api/coiffeurs" -Headers $coiffeurHeaders
Test-Endpoint -Role "COIFFEUR" -Method "GET" -Endpoint "/api/coiffeur-profiles" -Headers $coiffeurHeaders

# 2.4 File d'attente Tickets (GET)
Test-Endpoint -Role "COIFFEUR" -Method "GET" -Endpoint "/api/tickets" -Headers $coiffeurHeaders


Write-Host "=== 3. TESTS ADMINISTRATEUR (admin@fotolou.sn) ===" -ForegroundColor Magenta

# 3.1 Login Admin (JWT Standard)
$adminAuth = Test-Endpoint -Role "ADMIN" -Method "POST" -Endpoint "/api/authenticate" -Body '{"username":"admin@fotolou.sn","password":"admin_fotolou_2026","rememberMe":true}'
$adminToken = $adminAuth.id_token
$adminHeaders = @{ "Authorization" = "Bearer $adminToken" }

# 3.2 Tableau de Bord Statistiques
Test-Endpoint -Role "ADMIN" -Method "GET" -Endpoint "/api/admin/dashboard-stats" -Headers $adminHeaders

# 3.3 Parametres Plateforme (GET)
Test-Endpoint -Role "ADMIN" -Method "GET" -Endpoint "/api/platform-settings" -Headers $adminHeaders

# 3.4 Sante du Systeme (Actuator Health)
Test-Endpoint -Role "ADMIN" -Method "GET" -Endpoint "/management/health" -Headers $adminHeaders

# 3.5 Documentation OpenAPI (JSON)
Test-Endpoint -Role "ADMIN" -Method "GET" -Endpoint "/v3/api-docs" -Headers $adminHeaders

Write-Host "=== RESULTATS DE TOUS LES TESTS END-TO-END ===" -ForegroundColor Magenta
$global:results | Format-Table -AutoSize
