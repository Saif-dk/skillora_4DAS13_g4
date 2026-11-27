# Complaint Service — Gestion des Réclamations (Skillora)

## 1. Description

`complaint-service` est le microservice de **gestion des réclamations** de la plateforme e-learning **Skillora**, développé dans le cadre du projet **ESPRIT — Architecture Web Distribuée (4DAS13)**.

Il permet aux étudiants (`STUDENT`) et enseignants (`TEACHER`) de soumettre des réclamations, d'y répondre et d'en suivre l'avancement, et aux administrateurs (`ADMIN`) de consulter toutes les réclamations, d'y répondre et de mettre à jour leur statut.

---

## 2. Technologies

Versions issues de `pom.xml` :

| Technologie | Version / Artefact | Rôle |
|---|---|---|
| Java | 17 | Langage |
| Spring Boot | `3.5.6` (`spring-boot-starter-parent`) | Framework principal |
| Spring Cloud | `2025.0.0` (`spring-cloud-dependencies`) | Écosystème microservices |
| Spring Web | `spring-boot-starter-web` | API REST |
| Spring Data JPA | `spring-boot-starter-data-jpa` | Persistance ORM / Hibernate |
| Spring Security | `spring-boot-starter-security` | Sécurité stateless par JWT |
| Spring Validation | `spring-boot-starter-validation` | Validation des DTO (`@Valid`) |
| Spring Actuator | `spring-boot-starter-actuator` | Monitoring (`/actuator/health`, `/actuator/info`) |
| MySQL Connector/J | `mysql-connector-j` (runtime) | Pilote JDBC MySQL |
| Netflix Eureka Client | `spring-cloud-starter-netflix-eureka-client` | Enregistrement et découverte de services |
| JJWT | `0.12.6` (`jjwt-api`, `jjwt-impl`, `jjwt-jackson`) | Vérification et parsing des tokens JWT (HS256) |
| Springdoc OpenAPI | `2.8.9` (`springdoc-openapi-starter-webmvc-ui`) | Documentation interactive Swagger UI |
| Lombok | optionnel (exclu du jar final) | Réduction du code boilerplate |

---

## 3. Architecture

`complaint-service` **ne génère pas de tokens JWT**. Il vérifie uniquement les JWT émis par `user-service` à l'aide du même secret partagé (`app.jwt.secret`).

```text
+-------------+       1. POST /api/v1/auth/login       +------------------+
|             | -------------------------------------> |   user-service   |
|   Client    | <------------------------------------- |   (port 8086)    |
| (Swagger /  |          JWT (HS256, userId,           +------------------+
|  Postman /  |               email, role)
|  Frontend)  |
|             |       2. Requête + Bearer <JWT>        +-------------------+       +-------------------+
|             | -------------------------------------> | complaint-service | <---> |   MySQL (3306)    |
+-------------+                                        |   (port 8087)     |       | elearning_        |
                                                       +-------------------+       | complaints        |
                                                                 |                 +-------------------+
                                                                 | Enregistrement
                                                                 v
                                                       +-------------------+
                                                       |  Eureka Server    |
                                                       |   (port 8761)     |
                                                       +-------------------+
```

---

## 4. Prérequis

- **JDK 17** installé et configuré (`JAVA_HOME`)
- **Maven** (ou le wrapper `./mvnw` / `mvnw.cmd` inclus dans le dossier)
- **MySQL** en cours d'exécution sur le port `3306` (XAMPP, WAMP ou Docker) :
  ```bash
  docker run -d -p 3306:3306 -e MYSQL_ALLOW_EMPTY_PASSWORD=yes --name mysql mysql:8
  ```
- **Eureka Server** en cours d'exécution sur le port `8761`
- **`user-service`** en cours d'exécution sur le port `8086` pour générer les tokens JWT

---

## 5. Configuration

Fichier : `src/main/resources/application.properties`

| Propriété | Valeur par défaut | Description |
|---|---|---|
| `spring.application.name` | `COMPLAINT-SERVICE` | Nom du service dans Eureka |
| `server.port` | `8087` | Port HTTP du microservice |
| `spring.datasource.url` | `jdbc:mysql://localhost:3306/elearning_complaints?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC` | URL JDBC MySQL (la base `elearning_complaints` est créée automatiquement) |
| `spring.datasource.username` | `root` | Utilisateur MySQL |
| `spring.datasource.password` | *(vide)* | Mot de passe MySQL |
| `spring.jpa.hibernate.ddl-auto` | `update` | Création / mise à jour automatique des tables |
| `spring.jpa.show-sql` | `true` | Affichage des requêtes SQL dans les logs |
| `spring.jpa.open-in-view` | `false` | Désactive Open Session in View |
| `eureka.client.service-url.defaultZone` | `http://localhost:8761/eureka` | URL du serveur Eureka |
| `eureka.instance.prefer-ip-address` | `true` | Enregistrement avec l'adresse IP |
| `app.jwt.secret` | `ChangeMeWithAVeryLongSecretKey_AtLeast32Chars!` | **Doit être IDENTIQUE à celui de `user-service`** |
| `management.endpoints.web.exposure.include` | `health,info` | Endpoints Actuator exposés |

---

## 6. Modèle de données

Toutes les clés primaires utilisent `UUID` (`@GeneratedValue(strategy = GenerationType.UUID)`).

| Table MySQL | Entité JPA | Colonnes principales |
|---|---|---|
| `complaint_categories` | `ComplaintCategory` | `id` (PK), `libelle` (unique, not null) |
| `complaint_statuses` | `ComplaintStatus` | `id` (PK), `libelle` (unique, not null) |
| `complaints` | `Complaint` | `id` (PK), `reference` (unique, `REC-XXXXXXXX`), `requester_id` (not null), `subject` (max 150, not null), `description` (`TEXT`, not null), `category_id` (FK), `status_id` (FK), `assigned_admin_id` (nullable), `created_at`, `updated_at` |
| `complaint_replies` | `ComplaintReply` | `id` (PK), `complaint_id` (FK, not null), `sender_id` (not null), `sender_role` (not null), `content` (`TEXT`, not null), `created_at` |

### Schéma relationnel

```text
+-----------------------+          +-----------------------+
| complaint_categories  |          |  complaint_statuses   |
+-----------------------+          +-----------------------+
| PK id (UUID/String)   |          | PK id (UUID/String)   |
|    libelle (UNIQUE)   |          |    libelle (UNIQUE)   |
+-----------------------+          +-----------------------+
            |                                  |
            | 1..*                             | 1..*
            v                                  v
+----------------------------------------------------------+
|                       complaints                         |
+----------------------------------------------------------+
| PK id (UUID/String)                                      |
|    reference (UNIQUE, "REC-" + 8 chars)                  |
|    requester_id                                          |
|    subject (VARCHAR 150)                                 |
|    description (TEXT)                                    |
| FK category_id -> complaint_categories(id)               |
| FK status_id   -> complaint_statuses(id)                 |
|    assigned_admin_id (nullable)                          |
|    created_at, updated_at                                |
+----------------------------------------------------------+
            |
            | 1..* (cascade ALL, orderBy createdAt ASC)
            v
+----------------------------------------------------------+
|                    complaint_replies                     |
+----------------------------------------------------------+
| PK id (UUID/String)                                      |
| FK complaint_id -> complaints(id)                        |
|    sender_id                                             |
|    sender_role                                           |
|    content (TEXT)                                        |
|    created_at                                            |
+----------------------------------------------------------+
```

---

## 7. Intégration Eureka

- La classe principale `ComplaintServiceApplication` porte `@SpringBootApplication` et `@EnableDiscoveryClient`.
- Le service s'enregistre auprès d'Eureka sous le nom **`COMPLAINT-SERVICE`**.
- Vérification dans l'interface Eureka : [http://localhost:8761](http://localhost:8761)
- Pour lancer une **2e instance** sur le port `8097` :
  ```bash
  mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=8097
  ```

---

## 8. Lancement

### Ordre de démarrage recommandé

1. **Eureka Server** : lancer la classe `EurekaApplication` (et **non** la classe de test).
2. **MySQL** : démarrer le serveur MySQL sur le port `3306`.
3. **`user-service`** : démarrer sur le port `8086`.
4. **`complaint-service`** : démarrer sur le port `8087`.

### Via IntelliJ IDEA

Ouvrir `Backend/complaint-service`, attendre l'import Maven et exécuter `ComplaintServiceApplication.main()`.

### Via Maven (ligne de commande)

```bash
cd Backend/complaint-service
mvn spring-boot:run
```
*(ou `.\mvnw.cmd spring-boot:run` sous Windows).*

---

## 9. Données initiales

Au démarrage, `DataInitializer` (`CommandLineRunner`) insère les référentiels suivants uniquement s'ils n'existent pas déjà :

| Type | Valeurs initiales |
|---|---|
| Catégories (`complaint_categories`) | `PAIEMENT`, `COURS`, `EXAMEN`, `TECHNIQUE`, `AUTRE` |
| Statuts (`complaint_statuses`) | `OPEN`, `IN_PROGRESS`, `ACCEPTED`, `REJECTED` |

---

## 10. Règles métier

- **Création de réclamation (`POST /api/v1/complaints`)** :
  - Réservée aux rôles `STUDENT` et `TEACHER` (`403` si `ADMIN`).
  - `requesterId` est extrait du `subject` du JWT (jamais pris depuis le body).
  - La référence est générée automatiquement au format `REC-` + 8 caractères alphanumériques majuscules aléatoires.
  - Le statut initial est toujours `OPEN`.
- **Consultation de la liste (`GET /api/v1/complaints`)** :
  - `STUDENT` / `TEACHER` : retourne uniquement ses propres réclamations.
  - `ADMIN` : retourne toutes les réclamations, avec filtre optionnel `?status=OPEN|IN_PROGRESS|ACCEPTED|REJECTED` (`400` si statut inconnu).
- **Consultation détaillée (`GET /api/v1/complaints/{id}`)** :
  - Accessible au propriétaire (`requesterId == userId`) ou à `ADMIN` (`403` sinon).
  - Un `id` qui n'est pas un UUID valide renvoie `400`.
- **Ajout d'une réponse (`POST /api/v1/complaints/{id}/replies`)** :
  - Autorisé uniquement pour le propriétaire ou `ADMIN` (`403` sinon).
  - Impossible si la réclamation a le statut `ACCEPTED` ou `REJECTED` (`409 Conflict`).
  - Si un `ADMIN` répond alors que le statut est `OPEN`, la réclamation passe automatiquement à `IN_PROGRESS` et `assignedAdminId` est renseigné avec l'`userId` de l'admin.
- **Mise à jour du statut (`PATCH /api/v1/admin/complaints/{id}/status`)** :
  - Réservée aux `ADMIN` (`403` sinon).
  - Seuls `IN_PROGRESS`, `ACCEPTED` et `REJECTED` sont acceptés (`400` pour `OPEN` ou toute autre valeur).
  - Impossible si la réclamation est déjà `ACCEPTED` ou `REJECTED` (`409 Conflict`).
  - Met à jour `assignedAdminId` avec l'`userId` de l'admin connecté.

---

## 11. API

Préfixe commun : `/api/v1`. **Tous les endpoints ci-dessous nécessitent un JWT valide.**

| Méthode | URL | Accès | Codes HTTP |
|---|---|---|---|
| `GET` | `/api/v1/complaint-categories` | Authentifié | `200`, `401` |
| `GET` | `/api/v1/complaint-statuses` | Authentifié | `200`, `401` |
| `POST` | `/api/v1/complaints` | `STUDENT`, `TEACHER` | `201`, `400`, `401`, `403`, `404` |
| `GET` | `/api/v1/complaints` | Authentifié (`?status=` optionnel) | `200`, `400`, `401` |
| `GET` | `/api/v1/complaints/{id}` | Propriétaire ou `ADMIN` | `200`, `400`, `401`, `403`, `404` |
| `POST` | `/api/v1/complaints/{id}/replies` | Propriétaire ou `ADMIN` | `201`, `400`, `401`, `403`, `404`, `409` |
| `PATCH` | `/api/v1/admin/complaints/{id}/status` | `ADMIN` | `200`, `400`, `401`, `403`, `404`, `409` |

### Exemples de payloads

**`POST /api/v1/complaints` — Request :**
```json
{
  "subject": "Problème de paiement",
  "description": "J'ai payé mais je n'ai pas accès au cours.",
  "categoryId": "11111111-2222-3333-4444-555555555555"
}
```

**`POST /api/v1/complaints` / `GET /api/v1/complaints/{id}` — Response :**
```json
{
  "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "reference": "REC-8F3K2P9Q",
  "requesterId": "42",
  "subject": "Problème de paiement",
  "description": "J'ai payé mais je n'ai pas accès au cours.",
  "category": "PAIEMENT",
  "status": "OPEN",
  "assignedAdminId": null,
  "createdAt": "2026-11-27T13:10:00Z",
  "updatedAt": "2026-11-27T13:10:00Z",
  "replies": []
}
```

**`POST /api/v1/complaints/{id}/replies` — Request :**
```json
{
  "content": "Merci de me répondre rapidement."
}
```

**`POST /api/v1/complaints/{id}/replies` — Response (`201`) :**
```json
{
  "id": "f9e8d7c6-b5a4-3210-9876-543210fedcba",
  "senderId": "42",
  "senderRole": "STUDENT",
  "content": "Merci de me répondre rapidement.",
  "createdAt": "2026-11-27T13:12:00Z"
}
```

**`PATCH /api/v1/admin/complaints/{id}/status` — Request :**
```json
{
  "status": "ACCEPTED"
}
```

---

## 12. Comment obtenir un token JWT

Les tokens sont délivrés par `user-service` (`http://localhost:8086`) :

- **Compte Administrateur** :
  - `POST http://localhost:8086/api/v1/auth/login`
  - Body : `{ "email": "admin@elearning.com", "password": "Admin123!" }`
- **Compte Étudiant** :
  - Créer un compte via `POST http://localhost:8086/api/v1/auth/register` (rôle `STUDENT`), puis se connecter via `POST http://localhost:8086/api/v1/auth/login`.

Le JWT doit contenir `sub` (`userId`), `email` et `role` (`STUDENT`, `TEACHER` ou `ADMIN`), signé en `HS256` avec le même `app.jwt.secret`.

---

## 13. Swagger UI

- **URL** : [http://localhost:8087/swagger-ui/index.html](http://localhost:8087/swagger-ui/index.html)
- **Mode d'emploi** :
  1. Récupérer un token JWT via `user-service` (`POST http://localhost:8086/api/v1/auth/login`).
  2. Cliquer sur **Authorize** en haut à droite dans Swagger UI et coller le token **sans le préfixe `Bearer `**.
  3. Appeler `GET /api/v1/complaint-categories` pour copier un `categoryId` (UUID).
  4. Avec un token `STUDENT`, créer une réclamation via `POST /api/v1/complaints`.
  5. Se reconnecter avec un token `ADMIN` (via **Authorize**), répondre via `POST /api/v1/complaints/{id}/replies` puis changer le statut via `PATCH /api/v1/admin/complaints/{id}/status`.

---

## 14. Tests Postman

| # | Requête | Token utilisé | Body | Code attendu |
|---|---|---|---|---|
| 1 | `GET /api/v1/complaint-categories` | Étudiant ou Admin | — | `200` |
| 2 | `GET /api/v1/complaint-statuses` | Étudiant ou Admin | — | `200` |
| 3 | `GET /api/v1/complaints` | *(aucun)* | — | `401` |
| 4 | `POST /api/v1/complaints` | Étudiant | `subject`, `description`, `categoryId` valide | `201` |
| 5 | `POST /api/v1/complaints` | Étudiant | `subject: ""` (champ vide) | `400` |
| 6 | `POST /api/v1/complaints` | Étudiant | `categoryId` UUID inexistant | `404` |
| 7 | `POST /api/v1/complaints` | Admin | `subject`, `description`, `categoryId` valide | `403` |
| 8 | `GET /api/v1/complaints` | Étudiant | — | `200` (uniquement les siennes) |
| 9 | `GET /api/v1/complaints` | Admin | — | `200` (toutes) |
| 10 | `GET /api/v1/complaints/{id}` | Autre étudiant | — | `403` |
| 11 | `POST /api/v1/complaints/{id}/replies` | Propriétaire | `{"content": "Merci..."}` | `201` |
| 12 | `PATCH /api/v1/admin/complaints/{id}/status` | Étudiant | `{"status": "ACCEPTED"}` | `403` |
| 13 | `PATCH /api/v1/admin/complaints/{id}/status` | Admin | `{"status": "ACCEPTED"}` | `200` |
| 14 | `POST /api/v1/complaints/{id}/replies` (statut `ACCEPTED`/`REJECTED`) | Propriétaire ou Admin | `{"content": "Autre message"}` | `409` |
| 15 | `PATCH /api/v1/admin/complaints/{id}/status` (statut `ACCEPTED`/`REJECTED`) | Admin | `{"status": "REJECTED"}` | `409` |

---

## 15. Scénario complet conseillé

1. Lister les catégories avec le token étudiant (`GET /api/v1/complaint-categories`) et copier l'`id` de `PAIEMENT`.
2. Créer une réclamation avec le token étudiant (`POST /api/v1/complaints`) → statut `OPEN`.
3. Vérifier la réclamation dans la liste de l'étudiant (`GET /api/v1/complaints`).
4. Avec le token admin, filtrer les réclamations ouvertes (`GET /api/v1/complaints?status=OPEN`).
5. Avec le token admin, ajouter une réponse (`POST /api/v1/complaints/{id}/replies`) → le statut passe automatiquement à `IN_PROGRESS`.
6. Avec le token étudiant, consulter la réclamation (`GET /api/v1/complaints/{id}`) et répondre à nouveau.
7. Avec le token admin, clôturer la réclamation (`PATCH /api/v1/admin/complaints/{id}/status` avec `ACCEPTED`).
8. Vérifier qu'une nouvelle réponse ou un nouveau changement de statut renvoie `409 Conflict`.

---

## 16. Tests curl (CMD Windows)

```cmd
set TOKEN_STUDENT=COLLEZ_VOTRE_TOKEN_ETUDIANT
set TOKEN_ADMIN=COLLEZ_VOTRE_TOKEN_ADMIN

:: 1. Lister les catégories
curl -H "Authorization: Bearer %TOKEN_STUDENT%" http://localhost:8087/api/v1/complaint-categories

:: 2. Créer une réclamation
curl -X POST http://localhost:8087/api/v1/complaints ^
  -H "Authorization: Bearer %TOKEN_STUDENT%" ^
  -H "Content-Type: application/json" ^
  -d "{\"subject\":\"Problème de paiement\",\"description\":\"J'ai payé mais je n'ai pas accès au cours.\",\"categoryId\":\"REMPLACER_PAR_UUID_CATEGORIE\"}"

:: 3. Lister ses réclamations (étudiant)
curl -H "Authorization: Bearer %TOKEN_STUDENT%" http://localhost:8087/api/v1/complaints

:: 4. Répondre à une réclamation (admin -> passe à IN_PROGRESS)
curl -X POST http://localhost:8087/api/v1/complaints/REMPLACER_PAR_ID/replies ^
  -H "Authorization: Bearer %TOKEN_ADMIN%" ^
  -H "Content-Type: application/json" ^
  -d "{\"content\":\"Nous vérifions votre paiement.\"}"

:: 5. Accepter la réclamation (admin)
curl -X PATCH http://localhost:8087/api/v1/admin/complaints/REMPLACER_PAR_ID/status ^
  -H "Authorization: Bearer %TOKEN_ADMIN%" ^
  -H "Content-Type: application/json" ^
  -d "{\"status\":\"ACCEPTED\"}"
```

---

## 17. Vérification

- **Eureka** : vérifier la présence de `COMPLAINT-SERVICE` sur [http://localhost:8761](http://localhost:8761).
- **Actuator Health** : [http://localhost:8087/actuator/health](http://localhost:8087/actuator/health) doit retourner `{"status":"UP"}`.
- **phpMyAdmin / MySQL** : vérifier la création automatique de la base `elearning_complaints` et des 4 tables (`complaint_categories`, `complaint_statuses`, `complaints`, `complaint_replies`).

---

## 18. Format JSON des erreurs

Toutes les erreurs (sécurité `401`/`403`, validation `400`, métier `400`/`403`/`404`/`409`) suivent le format unifié :

```json
{
  "timestamp": "2026-11-27T13:15:00.123456Z",
  "status": 400,
  "error": "Bad Request",
  "message": "subject: Le sujet ne peut pas être vide",
  "path": "/api/v1/complaints"
}
```

---

## 19. Sécurité

- Politique de session `STATELESS` avec désactivation de CSRF.
- Authentification par filtre `JwtAuthFilter` (`OncePerRequestFilter`) avant `UsernamePasswordAuthenticationFilter`.
- Endpoints publics : `/actuator/**`, `/error`, `/swagger-ui/**`, `/swagger-ui.html`, `/v3/api-docs/**`.
- Endpoints admin : `/api/v1/admin/**` protégés par `hasRole("ADMIN")`.
- Tout autre endpoint exige un JWT valide (`anyRequest().authenticated()`).
- Les réponses `401` et `403` de Spring Security sont formatées en JSON via `AuthenticationEntryPoint` et `AccessDeniedHandler`.

---

## 20. Dépannage

| Erreur / Symptôme | Cause probable | Solution |
|---|---|---|
| `Communications link failure` | MySQL non démarré ou mauvais port | Démarrer MySQL sur le port `3306` |
| `Access denied for user 'root'` | Mot de passe MySQL configuré localement | Renseigner `spring.datasource.password` dans `application.properties` |
| `401 Unauthorized` avec un token valide | `app.jwt.secret` différent de celui de `user-service` | Copier exactement la même clé secrète que `user-service` |
| `Port 8087 was already in use` | Une instance tourne déjà sur 8087 | Arrêter l'ancien processus ou lancer sur un autre port (`--server.port=8097`) |
| Service absent d'Eureka | Eureka Server non lancé ou mauvaise URL | Démarrer Eureka sur `8761` et vérifier `eureka.client.service-url.defaultZone` |
| Eureka s'arrête tout seul au lancement | Classe de test lancée à la place de la classe main | Exécuter `EurekaApplication` (dans `src/main`) et non `...Tests` |
| Erreur de compilation Lombok | Annotation processing désactivé dans IntelliJ | Activer *Enable annotation processing* dans Settings → Build → Compiler → Annotation Processors |

---

## 21. Équipe

| Membre | Microservice / Module |
|---|---|
| **Maram Dhambri** | Cours |
| **Badis Awedi** | Quiz / Examens |
| **Aymen Ben Salem** | Réclamations (`complaint-service`) |
| **Dheker Derouiche** | Classes |
| **Saif Dkhaili** | Paiements |
| **Souleimen Belhadj** | Forum |
| **Commun** | User / Auth (`user-service`) |

**Encadrant** : Oussema Sellami
