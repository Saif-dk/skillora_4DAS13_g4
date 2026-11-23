# Course Service — API REST

Préfixe : `/api/v1` (choisi d'après le PDF technique). Le service écoute sur le port `8082`.

## Authentification

Les jetons JWT sont émis par le **User service** (`jwt.issuer=skillora-user-service`, signature HS256 avec `jwt.secret`). Le Course service ne fait que valider le jeton : il lit les claims `userId`, `role` et le `subject` (email), puis expose un principal `AuthenticatedUser` dans le `SecurityContext`. Un jeton absent, invalide ou expiré laisse la requête non authentifiée : les routes protégées répondent alors `401`.

Le `teacherId` d'un cours est **toujours** pris dans le claim `userId` du JWT (lors de `POST /api/v1/courses`) et jamais dans le corps de la requête. De même, les identifiants de cours/leçon des ressources sont imposés par le chemin de l'URL et écrasent toute valeur fournie dans le corps.

Rôles attendus dans le claim `role` : `ADMIN`, `PROFESSOR`, `STUDENT`. Les routes GET sont ouvertes à tout utilisateur authentifié ; les routes qui modifient l'état (`POST`, `PUT`, `PATCH`, `DELETE`) sont réservées à `ADMIN` et `PROFESSOR`.

Origines CORS autorisées : `http://localhost:4200` et `http://127.0.0.1:4200`.

## Endpoints

| Méthode | Chemin | Rôle | Codes de statut |
|---|---|---|---|
| GET | `/actuator/health` | Public | `200` |
| POST | `/api/v1/courses` | ADMIN, PROFESSOR | `201`, `400`, `401`, `403` |
| GET | `/api/v1/courses` | tout authentifié | `200`, `401` |
| GET | `/api/v1/courses/{id}` | tout authentifié | `200`, `401`, `403`, `404` |
| PUT | `/api/v1/courses/{id}` | ADMIN, PROFESSOR | `200`, `400`, `401`, `403`, `404` |
| PATCH | `/api/v1/courses/{id}/publish` | ADMIN, PROFESSOR | `200`, `401`, `403`, `404`, `409` |
| PATCH | `/api/v1/courses/{id}/unpublish` | ADMIN, PROFESSOR | `200`, `401`, `403`, `404`, `409` |
| DELETE | `/api/v1/courses/{id}` | ADMIN, PROFESSOR | `204`, `401`, `403`, `404` |
| GET | `/api/v1/courses/{courseId}/lessons` | tout authentifié | `200`, `401`, `403`, `404` |
| POST | `/api/v1/courses/{courseId}/lessons` | ADMIN, PROFESSOR | `201`, `400`, `401`, `403`, `404` |
| PUT | `/api/v1/lessons/{lessonId}` | ADMIN, PROFESSOR | `200`, `400`, `401`, `403`, `404` |
| PATCH | `/api/v1/lessons/{lessonId}/reorder?position=...` | ADMIN, PROFESSOR | `200`, `400`, `401`, `403`, `404` |
| DELETE | `/api/v1/lessons/{lessonId}` | ADMIN, PROFESSOR | `204`, `401`, `403`, `404` |
| POST | `/api/v1/courses/{courseId}/resources` | ADMIN, PROFESSOR | `201`, `400`, `401`, `403`, `404`, `409` |
| POST | `/api/v1/lessons/{lessonId}/resources` | ADMIN, PROFESSOR | `201`, `400`, `401`, `403`, `404`, `409` |
| GET | `/api/v1/resources/{id}` | tout authentifié | `200`, `401`, `403`, `404` |
| DELETE | `/api/v1/resources/{id}` | ADMIN, PROFESSOR | `204`, `401`, `403`, `404` |
| GET | `/api/v1/internal/courses/{id}` | tout authentifié | `200`, `401`, `403`, `404` |

`GET /api/v1/courses` accepte les paramètres optionnels `search`, `status` (`DRAFT`, `PUBLISHED`, `ARCHIVED`), `page` et `size`.

Les erreurs métier et de validation sont renvoyées au format `ApiError` (`instant`, `status`, `error`, `message`, `path`) ; les refus de sécurité renvoient `{"status":401|403,"message":"..."}`.
