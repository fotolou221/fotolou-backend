# 🚀 Guide de Déploiement Complet - Fotolou (Test Cloud & Futur VPS)

Ce guide détaille étape par étape la procédure pour déployer l'écosystème complet **Fotolou** :

- **Frontend** : Progressive Web App Angular 19 déployée sur **Vercel**
- **Backend** : API REST Spring Boot 4 / Java 21 déployée sur **Render**
- **Base de données** : PostgreSQL managé cloud (Render / Neon / Supabase)
- **Cache** : Redis managé (Upstash Redis)
- **Médias & Images** : Stockage CDN automatique sur **Cloudinary**
- **SMS & Alertes** : Expédition des SMS (OTP, rappels, file d'attente) via **Twilio**
- **Futur VPS de Production** : Architecture Docker Compose, Nginx Reverse Proxy et certificats SSL Let's Encrypt.

---

## 📑 Table des Matières

1. [Vue d'Ensemble & Architecture](#1-vue-densemble--architecture)
2. [Étape 1 : Configuration des Services Cloud Tiers](#2-étape-1--configuration-des-services-cloud-tiers)
   - [A. Cloudinary (Images & Photos)](#a-cloudinary-stockage-des-images)
   - [B. Twilio (Envoi de SMS)](#b-twilio-passerelle-sms)
   - [C. Base de données PostgreSQL Cloud](#c-base-de-données-postgresql-cloud)
   - [D. Cache Redis Cloud (Upstash)](#d-cache-redis-cloud-upstash)
3. [Étape 2 : Déploiement du Backend sur Render](#3-étape-2--déploiement-du-backend-sur-render)
4. [Étape 3 : Déploiement du Frontend sur Vercel](#4-étape-3--déploiement-du-frontend-sur-vercel)
5. [Étape 4 : Connexion & Configuration CORS](#5-étape-4--connexion--configuration-cors)
6. [Étape 5 : Checklist de Vérification & Recette](#6-étape-5--checklist-de-vérification--recette)
7. [Étape 6 : Guide de Migration vers un Serveur VPS (Production Dédiée)](#7-étape-6--guide-de-migration-vers-un-serveur-vps)

---

## 1. Vue d'Ensemble & Architecture

```mermaid
flowchart TD
    subgraph Clients["📱 Utilisateurs & Coiffeurs"]
        UserMobile["Client Mobile / Web Browser"]
        AdminWeb["Console Gérant / Admin"]
    end

    subgraph VercelHost["🌐 Frontend (Vercel)"]
        PWA["Fotolou PWA (Angular 19)
        https://fotolou.vercel.app"]
    end

    subgraph RenderHost["⚙️ Backend (Render)"]
        SpringAPI["Fotolou Backend API (Spring Boot 4 / Java 21)
        https://fotolou-backend.onrender.com"]
    end

    subgraph CloudServices["☁️ Services Cloud Tiers"]
        PG[(PostgreSQL Cloud)]
        Redis[(Upstash Redis Cache)]
        Cloudinary["Cloudinary CDN
        (Photos Salons & Produits)"]
        Twilio["Twilio SMS Gateway
        (OTP & Alertes File)"]
    end

    UserMobile -->|HTTPS / PWA| PWA
    AdminWeb -->|HTTPS / PWA| PWA
    PWA -->|REST API /api| SpringAPI
    SpringAPI -->|JDBC / SSL| PG
    SpringAPI -->|Redisson / TLS| Redis
    SpringAPI -->|Upload HTTPS| Cloudinary
    SpringAPI -->|REST API HTTP Basic| Twilio
    Twilio -->|SMS Réseau GSM| UserMobile
```

### Avantages de cette stack de test :

- **0 € de coût récurrent** pour valider toutes les fonctionnalités avec vos premiers utilisateurs.
- **SSL / HTTPS automatique** sur Vercel et Render.
- **Images hébergées sur CDN ultra-rapide** (Cloudinary compresse et délivre les images en WebP).
- **Code 100% prêt pour la production** : les mêmes variables et conteneurs Docker seront transférés sur votre VPS sans aucune réécriture.

---

## 2. Étape 1 : Configuration des Services Cloud Tiers

### A. Cloudinary (Stockage des images)

Cloudinary permet de stocker les photos des salons, coiffeurs, produits de la vitrine et avatars utilisateurs sans surcharger votre serveur.

1. Créez un compte gratuit sur [https://cloudinary.com](https://cloudinary.com).
2. Rendez-vous dans votre **Dashboard Cloudinary**.
3. Notez les 3 identifiants suivants (ou votre `CLOUDINARY_URL`) :
   - **Cloud Name** (ex: `dxyz12345`)
   - **API Key** (ex: `123456789012345`)
   - **API Secret** (ex: `AbCdEfGhIjKlMnOpQrStUvWxYz`)
4. _(Optionnel)_ Dans **Settings > Upload**, vous pouvez créer un dossier par défaut nommé `fotolou`.

---

### B. Twilio (Passerelle SMS) - Ou Alternative 100% Gratuite (Mock)

Pour la réception des SMS (codes OTP et alertes de file d'attente), vous avez deux choix :

#### 🌟 Option 1 (Recommandée pour tester sans rien payer) : Le Mode `mock` (Zéro frais, zéro compte)

Vous n'avez **absolument pas besoin de configurer Twilio** pour tester votre application sur Render et Vercel !

1. Dans vos variables Render, définissez simplement :
   ```dotenv
   SMS_PROVIDER=mock
   ```
2. **Comment vous connecter lors du test ?**
   - Vous entrez votre numéro de téléphone (ou n'importe quel numéro sénégalais, ex: `77 123 45 67`).
   - L'application accepte directement le code universel de test : **`123456`** !
   - Le vrai code à 6 chiffres généré s'affiche également dans les **Logs en direct de Render** (`[MOCK SMS ENVOYÉ] Code OTP: xxxxxx`).
   - ✅ **Résultat** : 0 € dépensé, aucune carte bancaire requise, aucun blocage.

---

#### 📱 Option 2 : Vrai envoi SMS via Twilio (Pourquoi avez-vous vu que c'était payant ?)

Si vous souhaitez tester l'envoi d'un vrai SMS sur votre smartphone :

> ⚠️ **ATTENTION - Erreur fréquente :**
>
> - Ne cliquez **JAMAIS** sur **"Buy a Phone Number"** (Acheter un numéro) en choisissant le Sénégal (+221) ! L'achat de numéros sénégalais sur Twilio est payant (environ 15$/mois) et réservé aux entreprises avec justificatifs légaux.
> - **Vous n'avez pas besoin d'acheter un numéro sénégalais pour envoyer des SMS au Sénégal.**

**Voici comment utiliser Twilio 100% Gratuitement avec les 15$ de crédits d'essai :**

1. Créez votre compte sur [https://www.twilio.com](https://www.twilio.com) (compte Trial).
2. Sur le tableau de bord, cliquez sur le bouton rouge **"Get a Twilio Phone Number"** : Twilio vous attribue automatiquement un **numéro américain gratuit** (ex: `+1205...`). Le coût est déduit de vos 15$ de crédit virtuel gratuit. C'est ce numéro qui sera votre `TWILIO_PHONE_NUMBER` (l'expéditeur).
3. **Pour ajouter votre propre numéro sénégalais (Destinataire)** :
   - Ne l'achetez pas !
   - Allez dans le menu latéral gauche : **Phone Numbers > Manage > Verified Caller IDs** (ou recherchez "Verified Caller IDs").
   - Cliquez sur **Add a new Caller ID** (ou le bouton `+`).
   - Entrez votre vrai numéro sénégalais (ex: `+22177XXXXXXX`).
   - Sélectionnez "SMS" pour recevoir le code de validation Twilio sur votre mobile.
   - Entrez le code à 6 chiffres : votre numéro est maintenant **vérifié gratuitement** !
4. Récupérez vos clés sur le Dashboard :
   - `TWILIO_ACCOUNT_SID` (ex: `AC...`)
   - `TWILIO_AUTH_TOKEN` (clé secrète)
   - `TWILIO_PHONE_NUMBER` (le numéro US gratuit fourni à l'étape 2)
5. Twilio enverra désormais des SMS réels depuis le numéro US vers votre numéro sénégalais vérifié sans que vous ne déboursiez un seul franc.

---

### C. Base de données PostgreSQL Cloud

Pour la version de test sur Render, deux options s'offrent à vous :

#### Option 1 (Recommandée & Gratuite à vie) : **Neon.tech** ou **Supabase**

- Créez un projet PostgreSQL gratuit sur [https://neon.tech](https://neon.tech) ou [https://supabase.com](https://supabase.com).
- Créez une base de données nommée `fotolou_db`.
- Copiez la chaîne de connexion JDBC :
  ```text
  jdbc:postgresql://ep-xyz.eu-central-1.aws.neon.tech/fotolou_db?sslmode=require
  ```
- Notez l'utilisateur (`fotolou_user`) et le mot de passe généré.

#### Option 2 : **Render PostgreSQL**

- Dans votre tableau de bord Render, cliquez sur **New + > PostgreSQL**.
- Nom : `fotolou-postgres`
- Database : `fotolou_db`
- User : `fotolou_user`
- Plan : **Free**
- Une fois créée, copiez l'**Internal Database URL** (ou l'External URL si requise).

---

### D. Cache Redis Cloud (Upstash)

Le backend Spring Boot Fotolou utilise Redis et Redisson pour la gestion du cache de session et des entités.

1. Créez un compte gratuit sur [https://upstash.com](https://upstash.com).
2. Cliquez sur **Create Database** :
   - Nom : `fotolou-redis`
   - Type : Serverless
   - Région : Choisissez la plus proche (ex: Francfort ou Paris)
   - Plan : Free (10 000 requêtes/jour gratuites)
3. Dans l'onglet **Details**, copiez la chaîne **Rediss URL** (avec double 's' pour TLS) :
   ```text
   rediss://default:votre_mot_de_passe_upstash@eu-central-1.upstash.io:6379
   ```

---

## 3. Étape 2 : Déploiement du Backend sur Render

Le projet inclut un fichier `fotolou-backend/Dockerfile` multi-stage optimisé pour Java 21 et le profil de production Spring Boot.

### 1. Structure du Répertoire Git

Assurez-vous que votre projet est poussé sur votre compte GitHub (ou GitLab) :

```bash
git add .
git commit -m "feat: configuration de déploiement Render et Vercel"
git push origin main
```

### 2. Création du Web Service sur Render

1. Rendez-vous sur votre tableau de bord [https://dashboard.render.com](https://dashboard.render.com).
2. Cliquez sur **New +** puis **Web Service**.
3. Sélectionnez **Build and deploy from a Git repository** et connectez votre dépôt GitHub.
4. Remplissez les champs de configuration :
   - **Name** : `fotolou-backend`
   - **Region** : Frankfurt (EU Central) _(ou la région la plus proche de votre base de données)_
   - **Branch** : `main`
   - **Root Directory** : `fotolou-backend` _(si monorepo avec frontend et backend ensemble)_
   - **Runtime / Environment** : `Docker`
   - **Dockerfile Path** : `./Dockerfile`
   - **Instance Type** : `Free` (0.1 CPU, 512 MB RAM)

### 3. Variables d'Environnement sur Render

Dans l'onglet **Environment Variables**, ajoutez les variables suivantes :

| Clé (Variable Name)                                  | Exemple de Valeur                                               | Description                                               |
| :--------------------------------------------------- | :-------------------------------------------------------------- | :-------------------------------------------------------- |
| `SPRING_PROFILES_ACTIVE`                             | `prod,api-docs`                                                 | Active le profil de production JHipster et la doc Swagger |
| `SPRING_DATASOURCE_URL`                              | `jdbc:postgresql://ep-xyz.neon.tech/fotolou_db?sslmode=require` | URL JDBC de votre PostgreSQL cloud                        |
| `SPRING_DATASOURCE_USERNAME`                         | `votre_user_postgres`                                           | Nom d'utilisateur de la base de données                   |
| `SPRING_DATASOURCE_PASSWORD`                         | `votre_password_postgres`                                       | Mot de passe de la base de données                        |
| `JHIPSTER_CACHE_REDIS_SERVER`                        | `rediss://default:token@xyz.upstash.io:6379`                    | URL Redis Upstash pour le cache                           |
| `JHIPSTER_SECURITY_AUTHENTICATION_JWT_BASE64_SECRET` | _(Générer avec la commande ci-dessous)_                         | Clé secrète de signature JWT                              |
| `JHIPSTER_CORS_ALLOWED_ORIGINS`                      | `https://fotolou.vercel.app,http://localhost:4200`              | Domaines autorisés à interroger l'API                     |
| `STORAGE_PROVIDER`                                   | `cloudinary`                                                    | Active le stockage cloud sur Cloudinary                   |
| `CLOUDINARY_CLOUD_NAME`                              | `votre_cloud_name`                                              | Cloud Name Cloudinary                                     |
| `CLOUDINARY_API_KEY`                                 | `votre_api_key`                                                 | Clé API Cloudinary                                        |
| `CLOUDINARY_API_SECRET`                              | `votre_api_secret`                                              | Clé secrète Cloudinary                                    |
| `SMS_PROVIDER`                                       | `twilio`                                                        | Active la passerelle Twilio (`mock` en secours)           |
| `SMS_SENDER_NAME`                                    | `Fotolou`                                                       | Nom de l'expéditeur                                       |
| `TWILIO_ACCOUNT_SID`                                 | `ACxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx`                            | Account SID de votre compte Twilio                        |
| `TWILIO_AUTH_TOKEN`                                  | `xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx`                              | Auth Token secret Twilio                                  |
| `TWILIO_PHONE_NUMBER`                                | `+12055550199`                                                  | Numéro de téléphone Twilio attribué                       |

> 💡 **Générer une clé secrète JWT Base64 valide (en PowerShell ou Bash) :**
>
> ```bash
> # En PowerShell :
> [Convert]::ToBase64String([System.Security.Cryptography.RandomNumberGenerator]::GetBytes(64))
> # En Bash / Linux :
> openssl rand -base64 64
> ```

5. Cliquez sur **Create Web Service**.
6. Render va télécharger le code, exécuter le Dockerfile multi-stage (compilation Maven puis empaquetage JRE 21), appliquer automatiquement les migrations Liquibase et démarrer l'application.
7. Une fois le déploiement terminé, notez l'URL publique de votre backend :
   ```text
   https://fotolou-backend.onrender.com
   ```
8. Vous pouvez tester que l'API répond en ouvrant :
   `https://fotolou-backend.onrender.com/api/uptime` (ou `/management/health` ou `/swagger-ui/index.html`).

---

### 4. Configuration Anti-Veille & Auto-Reboot sur Render (UptimeRobot)

> ⚠️ **Le Problème de l'offre gratuite Render (Free Tier) :**
> Render met automatiquement en veille (_spin down_) les instances Web gratuites après **15 minutes d'inactivité**.
> Au réveil, une application Spring Boot / Java 21 met entre **45 et 80 secondes à redémarrer** (cold start), donnant l'impression à vos utilisateurs que le site est en panne.
>
> 💡 **La Solution (100% Gratuite) : UptimeRobot**
> UptimeRobot envoie une requête HTTP `GET` toutes les 5 à 10 minutes vers votre backend. Render détecte du trafic continu et **ne met JAMAIS le conteneur en veille** !

#### A. Configurer le Health Check Path de Render (Auto-Reboot natif)

1. Sur le dashboard Render, cliquez sur votre Web Service `fotolou-backend`.
2. Allez dans l'onglet **Settings**.
3. Déroulez jusqu'à la section **Health Check Path**.
4. Saisissez :
   ```text
   /api/uptime
   ```
   _(ou `/api/uptime/ping` pour un ping encore plus léger)_.
5. Cliquez sur **Save Changes**.
6. **Bénéfice :** Si l'application rencontre un problème de mémoire ou se bloque, Render détecte l'absence de réponse 200 et **reboote automatiquement** le conteneur (_Self-Healing / Auto-Reboot_) !

#### B. Configurer UptimeRobot pour le Keep-Alive 24/7 (Anti-Sleep)

1. Créez un compte gratuit sur [https://uptimerobot.com](https://uptimerobot.com).
2. Cliquez sur le bouton vert **+ Add New Monitor**.
3. Remplissez les paramètres suivants :
   - **Monitor Type** : `HTTP(s)`
   - **Friendly Name** : `Fotolou Backend Render`
   - **URL (or IP)** : `https://fotolou-backend.onrender.com/api/uptime`
   - **Monitoring Interval** : `Every 5 minutes` _(ou 10 minutes)_
   - **Monitor Timeout** : `30 seconds`
4. Cochez votre adresse email dans **"Select "Alert Contacts To Notify"** pour être alerté si le serveur tombe en panne.
5. Cliquez sur **Create Monitor**.
6. ✅ **Résultat garanti :**
   - Votre backend Render reste **éveillé et chaud 24h/24**.
   - Temps de réponse instantané (< 1 seconde) pour tous vos utilisateurs PWA.
   - En cas d'anomalie, UptimeRobot vous envoie une notification immédiate par email.

## 4. Étape 3 : Déploiement du Frontend sur Vercel

Le frontend est une Progressive Web App Angular 19 complète située dans le dossier `Fotolou PWA`.

### 1. Configurer l'URL du Backend dans le code

Ouvrez le fichier `Fotolou PWA/src/environments/environment.prod.ts` et assurez-vous qu'il pointe vers votre URL Render suivie de `/api` :

```typescript
export const environment = {
  production: true,
  apiUrl: 'https://fotolou-backend.onrender.com/api', // Remplacez par le sous-domaine exact de votre Render
};
```

Poussez cette modification sur GitHub :

```bash
git add "Fotolou PWA/src/environments/environment.prod.ts"
git commit -m "fix: pointer environment.prod.ts vers l'API Render"
git push origin main
```

### 2. Importer le Projet sur Vercel

1. Rendez-vous sur [https://vercel.com](https://vercel.com) et connectez-vous avec GitHub.
2. Cliquez sur **Add New... > Project**.
3. Sélectionnez votre dépôt GitHub `TechDegg`.
4. Dans la fenêtre de configuration :
   - **Project Name** : `fotolou` (donnera `https://fotolou.vercel.app`)
   - **Framework Preset** : `Angular`
   - **Root Directory** : Cliquez sur **Edit** et sélectionnez le sous-dossier `Fotolou PWA`.
   - **Build and Output Settings** :
     - **Build Command** : `npm run build`
     - **Output Directory** : `dist/fotolou-pwa/browser`
     - **Install Command** : `npm install`
5. Cliquez sur **Deploy**.
6. Vercel compile l'application et la publie en quelques secondes sur son CDN mondial.

### 3. Fichier de Routage SPA & PWA (`vercel.json`)

Le fichier `Fotolou PWA/vercel.json` est déjà configuré à la racine du frontend pour gérer le routage Angular (évite les erreurs 404 lors d'un rechargement de page F5) et le cache du Service Worker :

```json
{
  "rewrites": [{ "source": "/(.*)", "destination": "/index.html" }],
  "headers": [
    {
      "source": "/manifest.webmanifest",
      "headers": [{ "key": "Content-Type", "value": "application/manifest+json" }]
    },
    {
      "source": "/ngsw-worker.js",
      "headers": [
        { "key": "Cache-Control", "value": "no-cache" },
        { "key": "Content-Type", "value": "application/javascript" }
      ]
    }
  ]
}
```

---

## 5. Étape 4 : Connexion & Configuration CORS

Pour des raisons de sécurité, les navigateurs bloquent les requêtes du Frontend (`fotolou.vercel.app`) vers le Backend (`fotolou-backend.onrender.com`) si le domaine Vercel n'est pas explicitement autorisé.

Dans `fotolou-backend/src/main/resources/config/application-prod.yml`, nous avons configuré :

- La prise en charge automatique de tous les domaines `https://*.vercel.app` via `allowed-origin-patterns`.
- La prise en charge de la variable d'environnement `JHIPSTER_CORS_ALLOWED_ORIGINS`.

**Action à effectuer :**
Sur le dashboard Render, dans les variables d'environnement de votre Web Service `fotolou-backend`, assurez-vous que la variable suivante contient l'URL exacte générée par Vercel :

```text
JHIPSTER_CORS_ALLOWED_ORIGINS = https://fotolou.vercel.app
```

---

## 6. Étape 5 : Checklist de Vérification & Recette

Effectuez les tests suivants pour valider que tous les composants fonctionnent en harmonie :

### 1. Connexion Administrateur

- Ouvrez votre frontend : `https://fotolou.vercel.app/login`
- Identifiants par défaut créés en base :
  - **Identifiant** : `admin@fotolou.sn`
  - **Mot de passe** : `admin` _(Pensez à le changer après la première connexion !)_
- Vérifiez l'accès à la console d'administration (`/admin`).
- Testez la déconnexion : le modale de confirmation personnalisé doit s'afficher.

### 2. Test du Stockage Cloudinary (Upload d'images)

- Depuis la console Admin, modifiez un salon ou ajoutez un produit dans la vitrine.
- Téléversez une photo depuis votre ordinateur ou smartphone.
- Vérifiez que l'image s'affiche instantanément.
- Vérifiez dans votre console Cloudinary : l'image doit être présente dans le dossier `fotolou/`.

### 3. Test des SMS Twilio

- Déconnectez-vous et connectez-vous en tant que client avec votre numéro vérifié sur Twilio (ex: `77XXXXXXX`).
- Vous devez recevoir le SMS OTP contenant le code à 6 chiffres sur votre mobile.
- Prenez un ticket dans un salon : la notification SMS de confirmation doit être envoyée.

### 4. Test du Cycle de la File d'Attente

- Ouvrez un salon dans l'espace client.
- Prenez un ticket : vérifiez que le modale de confirmation s'ouvre, puis affiche l'icône verte de validation pendant 2 secondes avant de fermer.
- Ouvrez l'écran du salon : vérifiez que la position s'actualise dynamiquement (ex: 1er en cours, barre de progression bleue).

---

## 7. Étape 6 : Guide de Migration vers un Serveur VPS (Production Dédiée)

Une fois vos tests validés, lorsque vous achèterez un serveur VPS (ex: **Hetzner CPX21** à ~7€/mois, **OVH VPS Starter**, ou **DigitalOcean** avec Ubuntu 24.04 LTS), voici la procédure pour déployer l'intégralité du système sur votre propre machine avec Docker et Nginx.

### 1. Spécifications recommandées du VPS

- **OS** : Ubuntu 24.04 LTS
- **CPU** : 2 vCPU
- **RAM** : 4 Go de RAM (recommandé pour Java 21 + PostgreSQL + Redis + Nginx)
- **Disque** : 40 Go SSD / NVMe

### 2. Installation des Prérequis sur le VPS

Connectez-vous en SSH à votre serveur :

```bash
ssh root@ip_de_votre_vps
```

Installez Docker et Docker Compose :

```bash
# Mise à jour des paquets
apt update && apt upgrade -y

# Installation de Docker
curl -fsSL https://get.docker.com -o get-docker.sh
sh get-docker.sh

# Vérification
docker --version
docker compose version
```

### 3. Cloner le Projet sur le VPS

```bash
cd /opt
git clone https://github.com/votre-compte/TechDegg.git fotolou
cd /opt/fotolou
```

### 4. Configurer les Variables d'Environnement de Production

Créez le fichier `.env` sur le serveur :

```bash
nano .env
```

Contenu du `.env` :

```dotenv
# Profil
SPRING_PROFILES_ACTIVE=prod

# Base de données PostgreSQL locale sur le VPS
POSTGRES_DB=fotolou_db
POSTGRES_USER=fotolou_user
POSTGRES_PASSWORD=un_mot_de_passe_tres_solide_ici

SPRING_DATASOURCE_URL=jdbc:postgresql://fotolou-postgresql:5432/fotolou_db
SPRING_DATASOURCE_USERNAME=fotolou_user
SPRING_DATASOURCE_PASSWORD=un_mot_de_passe_tres_solide_ici

# Cache Redis interne
JHIPSTER_CACHE_REDIS_SERVER=redis://fotolou-redis:6379

# Sécurité JWT
JHIPSTER_SECURITY_AUTHENTICATION_JWT_BASE64_SECRET=votre_cle_jwt_generee

# CORS pour votre domaine de production
JHIPSTER_CORS_ALLOWED_ORIGINS=https://app.fotolou.sn,https://admin.fotolou.sn

# Cloudinary
STORAGE_PROVIDER=cloudinary
CLOUDINARY_CLOUD_NAME=votre_cloud_name
CLOUDINARY_API_KEY=votre_api_key
CLOUDINARY_API_SECRET=votre_api_secret

# Twilio SMS
SMS_PROVIDER=twilio
SMS_SENDER_NAME=Fotolou
TWILIO_ACCOUNT_SID=votre_account_sid
TWILIO_AUTH_TOKEN=votre_auth_token
TWILIO_PHONE_NUMBER=votre_numero_twilio
```

### 5. Démarrage des Conteneurs avec Docker Compose

Le fichier `docker-compose.yml` déjà présent à la racine du projet gère automatiquement :

1. Le conteneur **PostgreSQL 16**
2. Le conteneur **Redis 7**
3. Le conteneur **pgAdmin 4** (pour administrer vos tables)
4. L'application **Fotolou Backend** (compilée via le Dockerfile)

Lancez tous les services :

```bash
docker compose up -d --build
```

Vérifiez l'état des conteneurs :

```bash
docker compose ps
docker compose logs -f fotolou-app
```

### 6. Configuration de Nginx & Certificats SSL Gratuits (Let's Encrypt)

Pour lier vos noms de domaine (ex: `api.fotolou.sn` et `app.fotolou.sn`) avec HTTPS automatique :

Installez Nginx et Certbot :

```bash
apt install nginx certbot python3-certbot-nginx -y
```

Configurez le bloc Nginx pour l'API Backend :

```nginx
# /etc/nginx/sites-available/fotolou-backend.conf
server {
    server_name api.fotolou.sn;

    client_max_body_size 25M;

    location / {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

Activez le site et générez le certificat SSL :

```bash
ln -s /etc/nginx/sites-available/fotolou-backend.conf /etc/nginx/sites-enabled/
nginx -t && systemctl reload nginx
certbot --nginx -d api.fotolou.sn
```

---

## 8. Résumé des URLs et Endpoints Utiles

| Service                       | Environnement de Test                                        | Environnement VPS Production                   |
| :---------------------------- | :----------------------------------------------------------- | :--------------------------------------------- |
| **Frontend Web / PWA**        | `https://fotolou.vercel.app`                                 | `https://app.fotolou.sn`                       |
| **Backend API REST**          | `https://fotolou-backend.onrender.com`                       | `https://api.fotolou.sn`                       |
| **Documentation Swagger**     | `https://fotolou-backend.onrender.com/swagger-ui/index.html` | `https://api.fotolou.sn/swagger-ui/index.html` |
| **Santé de l'API (Actuator)** | `https://fotolou-backend.onrender.com/management/health`     | `https://api.fotolou.sn/management/health`     |
| **Console Cloudinary**        | [cloudinary.com/console](https://cloudinary.com/console)     | Identique                                      |
| **Console Twilio SMS**        | [twilio.com/console](https://console.twilio.com)             | Identique                                      |

---

_Documentation générée pour Fotolou - Prêt pour tests en ligne et bascule VPS._
