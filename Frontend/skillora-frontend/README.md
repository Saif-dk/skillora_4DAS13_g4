# Skillora — back-office des cours

Ce front Angular consomme l'API REST du microservice `course-service` (`http://localhost:8082/api/v1`)
via de vrais appels `HttpClient`. Aucune donnée n'est simulée.

## Démarrage

1. Démarrer le microservice cours (H2 local par défaut, ou profil `postgres`) :

   ```bash
   cd Backend/course-service
   mvn spring-boot:run
   ```

   avec PostgreSQL :

   ```bash
   mvn spring-boot:run -Dspring-boot.run.profiles=postgres
   ```

   Vérifier : `http://localhost:8082/actuator/health`.

2. Démarrer le front (depuis ce répertoire `Frontend/skillora-frontend`) :

   ```bash
   npm install
   npm start
   ```

   Application : `http://localhost:4200`.

## Authentification

Toutes les requêtes vers `/api/v1` nécessitent un JWT. Connectez-vous via le front du microservice
utilisateur : le token doit être stocké dans `localStorage` sous la clé **`skillora_token`**.
L'intercepteur ajoute automatiquement `Authorization: Bearer <token>`. Le token doit contenir un claim
`role` (`ADMIN`, `PROFESSOR` ou `STUDENT`) : seuls `ADMIN` et `PROFESSOR` peuvent modifier les cours.

Si besoin, en développement :

```js
localStorage.setItem('skillora_token', '<JWT émis par user-service>');
```

## Parcours de test

1. Se connecter avec le front utilisateur puis ouvrir `http://localhost:4200/courses` : le catalogue provient de `GET /api/v1/courses`.
2. Avec un compte `PROFESSOR`/`ADMIN`, cliquer sur **Nouveau cours** (`/courses/new`), remplir le titre,
   la description, la catégorie, le niveau, le statut et le prix, puis **Enregistrer** (`POST /courses`).
3. Sur la fiche du cours, cliquer sur **Publier** (`PATCH /courses/{id}/publish`) ; le statut passe à `PUBLISHED` et la page est rechargée.
4. Cliquer sur **Ajouter une leçon** (`/courses/{id}/lessons/new`), saisir titre, contenu et position
   (`POST /courses/{courseId}/lessons`). Modifier une leçon via **Modifier** (`/lessons/{id}/edit`, `PUT /lessons/{id}`).
5. Cliquer sur **Ajouter une ressource** (`/courses/{id}/resources/new` ou `/lessons/{id}/resources/new`),
   renseigner nom, type (`PDF`, `VIDEO`, `DOCUMENT`, `LINK`), URL et taille (`POST .../resources`).
6. Modifier le cours via **Modifier** (`/courses/{id}/edit`, `PUT /courses/{id}`), supprimer une ressource
   ou une leçon (`DELETE /resources/{id}`, `DELETE /lessons/{id}`), puis **Supprimer** le cours (`DELETE /courses/{id}`).
7. Sur un cours publié, cliquer sur **Dépublier** (`PATCH /courses/{id}/unpublish`) pour repasser en `DRAFT`.

Les comptes `STUDENT` voient le catalogue et les détails en lecture seule : tous les boutons de gestion sont masqués.

## Vérification

```bash
npm test -- --watch=false
npm run build
```
