# 🚀 FOTOLOU BACKEND — GUIDE DE DÉMARRAGE & FONCTIONNEMENT

> Backend officiel pour la plateforme **Fotolou** (Gestion de files d'attente virtuelles en direct & boutique e-commerce pour salons de coiffure à Dakar).  
> Stack : **Java 21, Spring Boot 3.x, JHipster 9, PostgreSQL 16, Spring Security JWT, Liquibase, Redis**.

---

## 📋 TABLE DES MATIÈRES

1. [Architecture & Fonctionnalités Implémentées](#1-architecture--fonctionnalit%C3%A9s-impl%C3%A9ment%C3%A9es)
2. [Prérequis & Installation](#2-pr%C3%A9requis--installation)
3. [Démarrage en 3 Étapes Rapides](#3-d%C3%A9marrage-en-3-%C3%A9tapes-rapides)
4. [Identifiants de Test & Codes par Défaut](#4-identifiants-de-test--codes-par-d%C3%A9faut)
5. [Endpoints REST Clés & Utilisation](#5-endpoints-rest-cl%C3%A9s--utilisation)
6. [Connexion avec le Frontend Angular PWA](#6-connexion-avec-le-frontend-angular-pwa)
7. [Structure du Code Source](#7-structure-du-code-source)

---

## 1. ARCHITECTURE & FONCTIONNALITÉS IMPLÉMENTÉES

Le backend est 100% opérationnel et fournit l'ensemble des fonctionnalités requises par le frontend :

- 🔐 **Authentification Téléphone + OTP SMS** (`/api/auth/otp/send`, `/api/auth/otp/verify`) :
  - Génération de code à 6 chiffres aléatoire.
  - Hachage sécurisé BCrypt en base de données.
  - Durée de validité 5 minutes, maximum 3 tentatives, rate limiting horaire.
  - Code universel de test en développement : **`123456`**.
  - Émission de token JWT et profil utilisateur (`CLIENT` / `COIFFEUR`).
- 🎟️ **Moteur de File d'Attente Temps Réel (`QueueEngineService`)** :
  - Numérotation séquentielle journalière par salon (`#1`, `#2`, `#3`...).
  - Prise de tickets multi-bénéficiaires (`/api/tickets/book-multiple`) pour soi, ses proches ou un tiers.
  - Ajout de client direct en présentiel par le coiffeur (`/api/tickets/walk-in`).
  - Recalcul automatique et atomique des positions (`peopleAhead`) et temps d'attente estimés lors des passages (`served`) ou annulations (`cancel`).
  - Envoi d'alertes SMS automatiques dès que le tour du client approche.
- 💈 **Gestion des Salons & Favoris** (`/api/salons`, `/api/favorites/toggle`, `/api/favorites/my-favorites`) :
  - Ouverture / fermeture de la file en 1 clic (`PATCH /api/salons/{id}/toggle-status`).
  - Enregistrement des salons favoris par client.
- 🛍️ **Boutique E-Commerce & Commandes WhatsApp** (`/api/orders/checkout`, `/api/products`, `/api/categories`) :
  - Validation du panier et des stocks en base.
  - Frais de livraison forfaitaires configurables (**2 000 FCFA**).
  - Génération automatique du message et lien **WhatsApp Business** pré-formaté (`https://wa.me/...`).
- 📁 **Stockage & Téléversement de Médias** (`/api/storage/upload`, `/api/files/**`) :
  - Upload d'images (avatars, bannières salons, photos produits) avec validation MIME (JPEG, PNG, WebP) et distribution HTTP directe.
- 📊 **Tableau de Bord Administrateur** (`/api/admin/dashboard-stats`) :
  - Métriques consolidées en direct pour le portail d'administration `/admin`.
- 🌱 **Initialisation Automatique (`DatabaseDataInitializer`)** :
  - Au premier démarrage, la base est automatiquement peuplée avec les salons de Dakar (King Barber Mermoz, Almadies, Plateau, Point E), le catalogue de produits, les catégories et le compte admin.

---

## 2. PRÉREQUIS & INSTALLATION

- **Java** : JDK 21 ou supérieur (`java -version`)
- **Node.js** : v20 ou supérieur (`node -v`)
- **Docker & Docker Compose** : (`docker compose version`)

---

## 3. DÉMARRAGE EN 3 ÉTAPES RAPIDES

### Étape 1 : Démarrer PostgreSQL & Redis avec Docker

À la racine du projet (`Perso/TechDegg`) :

```bash
docker compose up -d fotolou-postgresql fotolou-redis fotolou-pgadmin
```

### Étape 2 : Lancer le Backend Spring Boot

Dans le dossier `fotolou-backend` :

```bash
# Sur Windows PowerShell
.\mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# Ou sur Linux / macOS
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

_Le backend démarre sur **`http://localhost:8080`**._

### Étape 3 : Lancer le Frontend Angular PWA

Dans un autre terminal, dans le dossier `Fotolou PWA` :

```bash
npm install
npm start
```

_L'application s'ouvre sur **`http://localhost:4200`**._

---

## 4. IDENTIFIANTS DE TEST & CODES PAR DÉFAUT

### Pour les Clients Mobiles (PWA) :

- **Numéro de téléphone** : N'importe quel numéro sénégalais (ex : `77 862 70 52` ou `77 123 45 67`).
- **Code OTP SMS** : En mode développement, le code envoyé s'affiche dans la console du backend. Vous pouvez également utiliser le code passe-partout : **`123456`**.

### Pour les Coiffeurs / Barbiers :

- **Numéro de téléphone** : `+221770000001` (ou votre propre numéro en cochant l'onglet "Je suis Coiffeur").
- **Code OTP** : **`123456`**.

### Pour le Back-Office Administrateur (`http://localhost:4200/admin`) :

- **Email** : `admin@fotolou.sn`
- **Mot de passe** : `admin_fotolou_2026`

---

## 5. ENDPOINTS REST CLÉS & UTILISATION

| Méthode | URL                              | Rôle              | Description                                          |
| :------ | :------------------------------- | :---------------- | :--------------------------------------------------- |
| `POST`  | `/api/auth/otp/send`             | Public            | Envoie un code OTP à 6 chiffres par SMS.             |
| `POST`  | `/api/auth/otp/verify`           | Public            | Valide le code OTP et retourne le JWT token.         |
| `GET`   | `/api/salons`                    | Public            | Liste des salons avec affluence en direct.           |
| `GET`   | `/api/salons/{id}`               | Public            | Fiche détaillée d'un salon avec galerie photos.      |
| `PATCH` | `/api/salons/{id}/toggle-status` | Coiffeur / Admin  | Ouvre ou ferme la file du salon.                     |
| `POST`  | `/api/tickets/book-multiple`     | Client            | Prise groupée de tickets pour soi et/ou ses proches. |
| `POST`  | `/api/tickets/walk-in`           | Coiffeur / Admin  | Ajout d'un client direct sur place.                  |
| `POST`  | `/api/tickets/{id}/call-next`    | Coiffeur / Admin  | Appelle le client (Statut `YOUR_TURN` + Alerte SMS). |
| `POST`  | `/api/tickets/{id}/serve`        | Coiffeur / Admin  | Marque le ticket comme servi avec succès (`SERVED`). |
| `POST`  | `/api/tickets/{id}/cancel`       | Client / Coiffeur | Annule le ticket et met à jour la file.              |
| `GET`   | `/api/tickets/my-tickets`        | Client            | Liste de mes tickets actifs et passés.               |
| `GET`   | `/api/salons/{id}/queue`         | Public / Coiffeur | File d'attente active en temps réel du salon.        |
| `POST`  | `/api/favorites/toggle`          | Client            | Ajoute/retire un salon des favoris.                  |
| `GET`   | `/api/favorites/my-favorites`    | Client            | Liste des salons favoris du client.                  |
| `GET`   | `/api/products`                  | Public            | Catalogue des produits boutique en FCFA.             |
| `GET`   | `/api/categories`                | Public            | Rayons de la boutique e-commerce.                    |
| `POST`  | `/api/orders/checkout`           | Client            | Crée une commande et génère le lien WhatsApp.        |
| `GET`   | `/api/orders/my-orders`          | Client            | Historique des commandes du client.                  |
| `POST`  | `/api/storage/upload`            | Coiffeur / Admin  | Téléversement d'image (avatar, salon, produit).      |
| `GET`   | `/api/files/{filename}`          | Public            | Distribution et affichage de l'image.                |
| `GET`   | `/api/admin/dashboard-stats`     | Admin             | Statistiques consolidées en direct.                  |

---

## 6. CONNEXION AVEC LE FRONTEND ANGULAR PWA

Le frontend `Fotolou PWA` est déjà configuré dans `src/environments/environment.ts` pour pointer sur `http://localhost:8080/api`.

Le backend gère automatiquement :

1. Les en-têtes **CORS** pour autoriser `http://localhost:4200`.
2. L'interception des requêtes authentifiées avec le header `Authorization: Bearer <token>`.
3. Le format exact des réponses attendues par les services Angular (`SalonService`, `TicketService`, `ProductService`, `OrderService`, `RelativeService`, `AdminDataService`).

---

## 7. STRUCTURE DU CODE SOURCE

```text
src/main/java/com/fotolou/app/
├── config/
│   ├── ApplicationProperties.java   # Propriétés SMS, OTP, Storage, WhatsApp
│   ├── DatabaseDataInitializer.java # Seeder de données de démarrage
│   ├── SecurityConfiguration.java   # Sécurité Spring Security JWT & CORS
│   └── WebConfigurer.java           # Filtre CORS et servlets
├── domain/                          # Entités JPA (Salon, Ticket, Product, Order...)
├── repository/                      # Repositories Spring Data JPA
├── security/                        # Constantes de rôles, JWT TokenProvider
├── service/                         # Services générés (DTOs, Mappers MapStruct)
│   └── custom/
│       ├── otp/OtpService.java      # Service de gestion et vérification OTP
│       ├── sms/                     # Fournisseurs SMS (Mock, Orange, Twilio)
│       ├── queue/QueueEngineService.java # Moteur de file d'attente temps réel
│       └── storage/StorageService.java   # Gestion des uploads d'images
└── web/rest/                        # Contrôleurs REST
    ├── custom/                      # Contrôleurs métier Fotolou
    │   ├── AuthOtpResource.java     # Endpoint login & validation OTP
    │   ├── TicketCustomResource.java# Prise de tickets, file d'attente
    │   ├── SalonCustomResource.java # Statut file & favoris
    │   ├── OrderCustomResource.java # Commandes boutique & WhatsApp
    │   ├── StorageResource.java     # Upload & streaming de fichiers
    │   └── AdminDashboardResource.java # Métriques consolidées admin
    └── (CRUD standard JHipster)
```

---

_Backend Fotolou — Moins d'attente, plus de temps._
