# 1. User Service — Skillora E-Learning Platform

> **Projet :** Skillora — Plateforme E-Learning  
> **Module :** Architecture Web Distribuée — 4DAS13  
> **École :** ESPRIT

Le **user-service** est le microservice central de gestion des utilisateurs et de l'authentification JWT pour la plateforme **Skillora**. Il est responsable de :

- L'inscription et la connexion des utilisateurs (étudiants, enseignants).
- L'émission des **tokens JWT** utilisés par tous les autres microservices pour vérifier l'identité et le rôle de l'appelant.
- La gestion des profils et de l'administration des comptes.

---

## 2. Technologies

| Technologie | Version |
|---|---|
| Java | 17 |
| Spring Boot | 3.5.6 |
| Spring Cloud (BOM) | 2025.0.0 |
| Spring Cloud Netflix Eureka Client | géré par BOM |
| Spring Data MongoDB | géré par Spring Boot |
| Spring Security | géré par Spring Boot |
| Spring Validation | géré par Spring Boot |
| Spring Actuator | géré par Spring Boot |
| JJWT (jjwt-api / jjwt-impl / jjwt-jackson) | 0.12.6 |
| Springdoc OpenAPI (Swagger UI) | 2.8.9 |
| Lombok | géré par Spring Boot |
| MongoDB | 7.x (serveur local) |
| Maven Wrapper | 3.x |

---

## 3. Architecture

### Schéma ASCII

```
 ┌─────────────────────┐       HTTP/REST       ┌──────────────────────────┐
 │   Client            │ ─────────────────────▶│   user-service           │
 │ (Postman / Browser) │                        │   :8086                  │
 └─────────────────────┘                        │                          │
                                                │  Spring Boot + Security  │
                                                │  JWT (JJWT 0.12.6)       │
                                                └───────┬──────────┬───────┘
                                                        │          │
                                              MongoDB   │          │  Eureka
                                              :27017    ▼          ▼  :8761
                                           ┌──────────────┐  ┌──────────────┐
                                           │  elearning_  │  │   Eureka     │
                                           │  users (DB)  │  │   Server     │
                                           └──────────────┘  └──────────────┘
```

### Flux JWT

1. Le client envoie ses identifiants à `POST /api/v1/auth/login`.
2. **user-service** vérifie les identifiants en base MongoDB, puis génère un **JWT signé avec HS256** (secret partagé).
3. Le token contient : `sub` = userId, `role`, `email`, `iat`, `exp`.
4. Les autres microservices (**complaint-service**, **forum-service**, etc.) vérifient ce token **avec le même secret** sans appeler user-service — architecture **stateless**.

> [!IMPORTANT]
> Le secret JWT (`app.jwt.secret`) doit être **identique** dans tous les microservices qui valident les tokens.

---

## 4. Prérequis

| Prérequis | Version minimale | Vérification |
|---|---|---|
| JDK | 17 | `java -version` |
| Maven | 3.8+ (ou utiliser `mvnw`) | `mvn -version` |
| MongoDB | 6+ | `mongosh --eval "db.version()"` |
| Serveur Eureka | — | Doit tourner sur `http://localhost:8761` |

> [!NOTE]
> MongoDB doit être démarré et accessible sur `localhost:27017` avant de lancer le service.

---

## 5. Configuration

Fichier : `src/main/resources/application.properties`

| Propriété | Valeur par défaut | Description |
|---|---|---|
| `spring.application.name` | `USER-SERVICE` | Nom d'enregistrement dans Eureka |
| `server.port` | `8086` | Port d'écoute HTTP |
| `spring.data.mongodb.uri` | `mongodb://localhost:27017/elearning_users` | URI de connexion MongoDB |
| `spring.data.mongodb.auto-index-creation` | `true` | Crée automatiquement les index (`@Indexed`) |
| `eureka.client.service-url.defaultZone` | `http://localhost:8761/eureka` | URL du serveur Eureka |
| `eureka.instance.prefer-ip-address` | `true` | Enregistre l'IP plutôt que le hostname |
| `app.jwt.secret` | `ChangeMeWithAVeryLongSecretKey_AtLeast32Chars!` | **Clé secrète HMAC-SHA256 — à changer en production** |
| `app.jwt.expiration-ms` | `86400000` | Durée de validité du token (24 h) |
| `management.endpoints.web.exposure.include` | `health,info` | Endpoints Actuator exposés |

> [!CAUTION]
> En production, remplacez `app.jwt.secret` par une clé d'au moins 32 caractères aléatoires et stockez-la dans une variable d'environnement ou un coffre-fort (Vault, etc.).

---

## 6. Modèle de données

### Collection `roles`

| Champ | Type | Contrainte | Description |
|---|---|---|---|
| `_id` | String | PK | Identifiant MongoDB |
| `name` | String | Unique, indexé | Nom du rôle : `STUDENT`, `TEACHER`, `ADMIN` |
| `description` | String | — | Description lisible |

### Collection `users`

| Champ | Type | Contrainte | Description |
|---|---|---|---|
| `_id` | String | PK | Identifiant MongoDB |
| `firstName` | String | — | Prénom |
| `lastName` | String | — | Nom de famille |
| `email` | String | Unique, indexé | Email (normalisé en minuscules) |
| `passwordHash` | String | — | Mot de passe haché en BCrypt (`$2a$...`) |
| `roleId` | String | FK → roles._id | Référence au rôle |
| `status` | String | — | Statut : `ACTIVE` |
| `createdAt` | Instant | — | Date de création (UTC) |

---

## 7. Intégration Eureka

| Paramètre | Valeur |
|---|---|
| Nom du service | `USER-SERVICE` |
| URL du serveur Eureka | `http://localhost:8761/eureka` |
| Dashboard Eureka | `http://localhost:8761` |

### Vérifier l'enregistrement

Ouvrez `http://localhost:8761` dans votre navigateur. Vous devriez voir **USER-SERVICE** dans la liste des instances enregistrées.

### Lancer une 2e instance (port 8096)

```bash
mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=8096
```

Les deux instances (**8086** et **8096**) apparaîtront alors dans le dashboard Eureka sous `USER-SERVICE`.

---

## 8. Lancement

### Ordre de démarrage recommandé

1. **MongoDB** — doit être démarré en premier.
2. **Eureka Server** — lancez la classe `EurekaApplication` (pas la classe de test).
3. **user-service** — seulement après MongoDB et Eureka.

> [!WARNING]
> Ne lancez **pas** la classe de test Eureka (ex. `EurekaServerApplicationTests`). Elle démarre un contexte Spring qui se coupe aussitôt et arrête le serveur.

### Via IntelliJ IDEA

Ouvrez et exécutez la classe **`UserServiceApplication`** (`src/main/java/com/elearning/user/UserServiceApplication.java`).

### Via Maven (terminal)

```bash
# Depuis le dossier Backend/user-service
mvn spring-boot:run

# Ou avec le Maven Wrapper
./mvnw spring-boot:run          # Linux / macOS
mvnw.cmd spring-boot:run        # Windows CMD
```

---

## 9. Compte admin par défaut et rôles initiaux

Au démarrage, le `DataInitializer` crée automatiquement :

### Rôles créés

| Rôle | Description |
|---|---|
| `STUDENT` | Étudiant de la plateforme |
| `TEACHER` | Enseignant de la plateforme |
| `ADMIN` | Administrateur de la plateforme |

### Compte administrateur par défaut

| Champ | Valeur |
|---|---|
| Email | `admin@elearning.com` |
| Mot de passe | `Admin123!` |
| Rôle | `ADMIN` |
| Statut | `ACTIVE` |

> [!NOTE]
> Si le compte admin ou les rôles existent déjà en base, ils ne sont pas recréés (idempotent).

---

## 10. API — Endpoints

Préfixe commun : `/api/v1`

### POST /api/v1/auth/register — Inscription

| Champ | Valeur |
|---|---|
| **Méthode** | `POST` |
| **URL** | `http://localhost:8086/api/v1/auth/register` |
| **Accès** | Public |

**Corps JSON :**
```json
{
  "firstName": "Ali",
  "lastName": "Ben Salah",
  "email": "ali@esprit.tn",
  "password": "Password123",
  "role": "STUDENT"
}
```

**Réponse 201 :**
```json
{
  "id": "64f1a2b3c4d5e6f7a8b9c0d1",
  "firstName": "Ali",
  "lastName": "Ben Salah",
  "email": "ali@esprit.tn",
  "role": "STUDENT",
  "status": "ACTIVE",
  "createdAt": "2024-11-27T11:30:00Z"
}
```

**Codes HTTP :** `201` succès · `400` validation échouée ou rôle invalide · `409` email déjà utilisé.

---

### POST /api/v1/auth/login — Connexion

| Champ | Valeur |
|---|---|
| **Méthode** | `POST` |
| **URL** | `http://localhost:8086/api/v1/auth/login` |
| **Accès** | Public |

**Corps JSON :**
```json
{
  "email": "admin@elearning.com",
  "password": "Admin123!"
}
```

**Réponse 200 :**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer",
  "user": {
    "id": "64f1a2b3c4d5e6f7a8b9c0d1",
    "firstName": "Admin",
    "lastName": "Skillora",
    "email": "admin@elearning.com",
    "role": "ADMIN",
    "status": "ACTIVE",
    "createdAt": "2024-11-27T11:00:00Z"
  }
}
```

**Codes HTTP :** `200` succès · `400` corps invalide · `401` identifiants incorrects.

---

### GET /api/v1/users/me — Profil personnel

| Champ | Valeur |
|---|---|
| **Méthode** | `GET` |
| **URL** | `http://localhost:8086/api/v1/users/me` |
| **Accès** | JWT requis (tout rôle) |
| **Header** | `Authorization: Bearer <token>` |

**Réponse 200 :** Objet `UserResponse` (voir ci-dessus, sans `passwordHash`).

**Codes HTTP :** `200` succès · `401` token absent ou invalide.

---

### GET /api/v1/admin/users — Liste de tous les utilisateurs

| Champ | Valeur |
|---|---|
| **Méthode** | `GET` |
| **URL** | `http://localhost:8086/api/v1/admin/users` |
| **Accès** | JWT requis + rôle `ADMIN` |
| **Header** | `Authorization: Bearer <token>` |

**Réponse 200 :** Tableau JSON de `UserResponse`.

**Codes HTTP :** `200` succès · `401` non authentifié · `403` rôle insuffisant.

---

## 11. Swagger UI

**URL :** [`http://localhost:8086/swagger-ui/index.html`](http://localhost:8086/swagger-ui/index.html)

### Mode d'emploi

1. Ouvrez Swagger UI dans votre navigateur.
2. Appelez `POST /api/v1/auth/login` avec les identifiants admin.
3. Copiez la valeur du champ `token` dans la réponse.
4. Cliquez sur le bouton **Authorize** 🔓 (en haut à droite).
5. Dans le champ `Value`, collez **uniquement le token** (sans le préfixe `Bearer`).
6. Cliquez **Authorize** puis **Close**.
7. Testez maintenant `GET /api/v1/users/me` ou `GET /api/v1/admin/users` directement depuis Swagger.

> [!TIP]
> Les endpoints `/register` et `/login` sont marqués comme publics dans Swagger (pas de cadenas fermé) — pas besoin de token pour les appeler.

---

## 12. Tests Postman

### Configuration d'Authorization dans Postman

Pour les endpoints protégés, dans Postman :
- Onglet **Authorization** → Type : **Bearer Token** → coller le token JWT.

### Tableau des tests

| # | Requête | Corps JSON (résumé) | Code attendu |
|---|---|---|---|
| 1 | `POST /api/v1/auth/register` | `{firstName, lastName, email, password, role: "STUDENT"}` | `201` |
| 2 | `POST /api/v1/auth/register` | Même email qu'au test 1 | `409` |
| 3 | `POST /api/v1/auth/register` | `{password: "abc"}` (trop court) | `400` |
| 4 | `POST /api/v1/auth/register` | `{role: "ADMIN"}` | `400` |
| 5 | `POST /api/v1/auth/login` | `{email: "admin@elearning.com", password: "Admin123!"}` | `200` |
| 6 | `POST /api/v1/auth/login` | `{email: "admin@elearning.com", password: "mauvais"}` | `401` |
| 7 | `GET /api/v1/users/me` | — (sans header Authorization) | `401` |
| 8 | `GET /api/v1/users/me` | — (avec `Bearer <token admin>`) | `200` |
| 9 | `GET /api/v1/admin/users` | — (avec `Bearer <token étudiant>`) | `403` |
| 10 | `GET /api/v1/admin/users` | — (avec `Bearer <token admin>`) | `200` |

---

## 13. Tests curl (CMD Windows)

```cmd
:: 1. Inscription
curl -X POST http://localhost:8086/api/v1/auth/register ^
  -H "Content-Type: application/json" ^
  -d "{\"firstName\":\"Ali\",\"lastName\":\"Ben Salah\",\"email\":\"ali@esprit.tn\",\"password\":\"Password123\",\"role\":\"STUDENT\"}"

:: 2. Connexion admin
curl -X POST http://localhost:8086/api/v1/auth/login ^
  -H "Content-Type: application/json" ^
  -d "{\"email\":\"admin@elearning.com\",\"password\":\"Admin123!\"}"

:: 3. Profil (remplacer TOKEN par la valeur réelle)
curl -X GET http://localhost:8086/api/v1/users/me ^
  -H "Authorization: Bearer TOKEN"

:: 4. Liste admin
curl -X GET http://localhost:8086/api/v1/admin/users ^
  -H "Authorization: Bearer TOKEN"
```

---

## 14. Vérification

| Point de vérification | Comment vérifier |
|---|---|
| Eureka | Ouvrir `http://localhost:8761` → voir `USER-SERVICE` dans la liste |
| Actuator health | `GET http://localhost:8086/actuator/health` → `{"status":"UP"}` |
| MongoDB Compass | Se connecter à `mongodb://localhost:27017` → base `elearning_users` → collections `roles` (3 docs) et `users` (1 doc admin) |
| passwordHash | Dans MongoDB Compass, le champ `passwordHash` doit commencer par `$2a$` (BCrypt) |

---

## 15. Format JSON uniforme des erreurs

Toutes les erreurs retournent le même format JSON :

```json
{
  "timestamp": "2024-11-27T11:35:22.123456Z",
  "status": 409,
  "error": "Conflict",
  "message": "Email already in use: ali@esprit.tn",
  "path": "/api/v1/auth/register"
}
```

| Code | Situation |
|---|---|
| `400` | Validation échouée, rôle invalide, corps JSON malformé |
| `401` | Token absent, token invalide, identifiants incorrects |
| `403` | Token valide mais rôle insuffisant |
| `404` | Ressource introuvable |
| `409` | Email déjà utilisé |

---

## 16. Sécurité

| Mesure | Détail |
|---|---|
| **BCrypt** | Les mots de passe ne sont jamais stockés en clair |
| **JWT stateless** | Aucune session serveur — chaque requête porte son token |
| **CSRF désactivé** | API REST stateless — pas de CSRF |
| **Rôles** | `STUDENT`, `TEACHER`, `ADMIN` — contrôle d'accès par URL dans `SecurityConfig` |
| **Normalisation email** | Trim + minuscules à l'inscription et au login |
| **Pas de fuite d'info** | Login invalide : même message d'erreur que email introuvable |

---

## 17. Dépannage

| Erreur | Cause probable | Solution |
|---|---|---|
| `MongoTimeoutException` | MongoDB non démarré | Lancer MongoDB : `mongod` ou démarrer le service Windows |
| Port 8086 déjà utilisé | Un autre processus occupe le port | `netstat -ano \| findstr :8086` puis tuer le processus, ou changer `server.port` |
| `401` avec un token valide | Token expiré (> 24 h) | Se reconnecter pour obtenir un nouveau token |
| Service absent d'Eureka | Eureka non démarré avant user-service | Démarrer Eureka en premier, puis user-service |
| Eureka s'arrête brutalement | Classe de test Eureka lancée | Lancer uniquement `EurekaApplication`, pas `EurekaServerApplicationTests` |
| Erreur Lombok (`cannot find symbol`) | Lombok non activé dans l'IDE | IntelliJ : `File > Settings > Build > Compiler > Annotation Processors > Enable` |

---

## 18. Équipe

### Projet Skillora — 4DAS13 — ESPRIT

| Membre | Module |
|---|---|
| **Maram Dhambri** | Cours |
| **Badis Awedi** | Quiz / Examens |
| **Aymen Ben Salem** | Réclamations |
| **Dheker Derouiche** | Classes |
| **Saif Dkhaili** | Paiements |
| **Souleimen Belhadj** | Forum |
| *(commun)* | User / Authentification (ce service) |

**Encadrant :** Oussema Sellami
