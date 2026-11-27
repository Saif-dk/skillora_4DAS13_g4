# Microservice Gestion d'examen : tests de l'API

Microservice Spring Boot + MongoDB (collections `examens` et `questions`).

- **URL de base** : `http://localhost:8082/api`
- **Swagger UI** : http://localhost:8082/swagger-ui.html
- **Eureka** : http://localhost:8761 (service `EXAMEN-SERVICE`)

## Prérequis

1. MongoDB démarré (`mongodb://localhost:27017`)
2. Eureka_Server démarré (port 8761)
3. Microservice Examen démarré (port 8082)

## Remarques

- Les `id` sont des ObjectId MongoDB (chaînes de 24 caractères). Remplace `{examenId}` et `{questionId}` par les valeurs reçues dans les réponses.
- `dateExamen` doit être dans le futur.
- `bonneReponse` doit valoir `A`, `B`, `C` ou `D`.
- Les commandes `curl` fonctionnent dans Git Bash, WSL, Linux et macOS. Sous Windows, utilise `curl.exe` dans PowerShell, ou Swagger / Postman.

---

## 1. Examens

### Créer un examen (POST) : 201
```bash
curl -X POST http://localhost:8082/api/examens \
  -H "Content-Type: application/json" \
  -d '{
    "titre": "Examen Java",
    "description": "Chapitres 1 à 3",
    "dateExamen": "2026-12-20T09:00:00",
    "duree": 60,
    "courseId": "c1",
    "teacherId": "t1"
  }'
```
Réponse : l'examen avec son `id`, `statut: "BROUILLON"` et `noteTotale: 0`.

### Lister tous les examens (GET) : 200
```bash
curl http://localhost:8082/api/examens
```

### Lister les examens d'un cours (GET) : 200
```bash
curl "http://localhost:8082/api/examens?courseId=c1"
```

### Consulter un examen (GET) : 200
```bash
curl http://localhost:8082/api/examens/{examenId}
```

### Modifier un examen (PUT) : 200
```bash
curl -X PUT http://localhost:8082/api/examens/{examenId} \
  -H "Content-Type: application/json" \
  -d '{
    "titre": "Examen Java (modifié)",
    "description": "Chapitres 1 à 4",
    "dateExamen": "2026-12-22T10:00:00",
    "duree": 90,
    "courseId": "c1",
    "teacherId": "t1"
  }'
```

### Publier un examen (PATCH) : 200
```bash
curl -X PATCH http://localhost:8082/api/examens/{examenId}/publier
```
Réponse : `statut: "PUBLIE"`. L'examen doit contenir au moins une question.

### Supprimer un examen (DELETE) : 204
```bash
curl -X DELETE http://localhost:8082/api/examens/{examenId}
```
Supprime aussi toutes les questions de l'examen (composition).

---

## 2. Questions

### Ajouter une question à un examen (POST) : 201
```bash
curl -X POST http://localhost:8082/api/examens/{examenId}/questions \
  -H "Content-Type: application/json" \
  -d '{
    "enonce": "Quel mot-clé permet l héritage en Java ?",
    "typeQuestion": "QCM",
    "optionA": "implements",
    "optionB": "extends",
    "optionC": "inherits",
    "optionD": "super",
    "bonneReponse": "B",
    "points": 2
  }'
```
La `noteTotale` de l'examen est recalculée automatiquement.

### Lister les questions d'un examen (GET) : 200
```bash
curl http://localhost:8082/api/examens/{examenId}/questions
```

### Consulter une question (GET) : 200
```bash
curl http://localhost:8082/api/questions/{questionId}
```

### Modifier une question (PUT) : 200
```bash
curl -X PUT http://localhost:8082/api/questions/{questionId} \
  -H "Content-Type: application/json" \
  -d '{
    "enonce": "Quel mot-clé permet l héritage de classe en Java ?",
    "typeQuestion": "QCM",
    "optionA": "implements",
    "optionB": "extends",
    "optionC": "inherits",
    "optionD": "super",
    "bonneReponse": "B",
    "points": 3
  }'
```

### Supprimer une question (DELETE) : 204
```bash
curl -X DELETE http://localhost:8082/api/questions/{questionId}
```

---

## 3. Scénario de test complet

1. `POST /api/examens` puis noter l'`id`.
2. `POST /api/examens/{id}/questions` (deux fois) puis vérifier que `noteTotale` = somme des points via `GET /api/examens/{id}`.
3. `PUT /api/questions/{questionId}` puis vérifier que `noteTotale` est mise à jour.
4. `PATCH /api/examens/{id}/publier` puis vérifier que `statut` = `PUBLIE`.
5. `DELETE /api/examens/{id}` puis vérifier que `GET /api/examens/{id}/questions` renvoie 404.

## 4. Cas d'erreur à tester

| Test | Résultat attendu |
|------|------------------|
| `POST /api/examens` avec `titre` vide | 400 (validation) |
| `POST /api/examens` avec une date passée | 400 (`@Future`) |
| `POST .../questions` avec `bonneReponse` = `Z` | 400 |
| `PATCH .../publier` sur un examen sans question | 400 « Impossible de publier un examen sans question » |
| `PUT /api/examens/{id}` sur un examen publié | 400 « Un examen publié ne peut plus être modifié » |
| `POST .../questions` sur un examen publié | 400 « Examen publié : questions non modifiables » |
| `GET /api/examens/inconnu` | 404 « Examen introuvable » |

Format des erreurs :
```json
{ "timestamp": "2026-11-25T19:40:00", "status": 400, "message": "..." }
```

## 5. Vérifier la base MongoDB

Dans MongoDB Compass (`mongodb://localhost:27017`), ouvrir la base `examen_db`, collections `examens` et `questions`. Ou en ligne de commande :
```bash
mongosh
use examen_db
db.examens.find().pretty()
db.questions.find().pretty()
```

## 6. Vérifier Eureka

- Dashboard : http://localhost:8761, `EXAMEN-SERVICE` doit être en `UP`.
- API : http://localhost:8761/eureka/apps
