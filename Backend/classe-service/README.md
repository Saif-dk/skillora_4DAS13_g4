# classe-service — microservice Gestion des classes

Microservice Spring Boot 4.2.0 / Spring Cloud 2026.0.0 (Java 17) de la plateforme Skillora.
Il gère les classes, leurs séances et les inscriptions, et consomme les événements `PaymentSucceeded`.

## Démarrage

```bash
docker run -d --name mysql-classe -e MYSQL_ROOT_PASSWORD=root -p 3306:3306 mysql:8
docker run -d --name rabbit -p 5672:5672 -p 15672:15672 rabbitmq:3-management
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

- API : `http://localhost:8083/api/v1`
- Swagger UI : `http://localhost:8083/swagger-ui.html`
- OpenAPI JSON : `http://localhost:8083/v3/api-docs`
- RabbitMQ Management : `http://localhost:15672` (`guest` / `guest`)

La base `classe_db` est créée automatiquement. Le profil `dev` insère 2 classes, 2 séances et 2 inscriptions si la table `classrooms` est vide.

Sans RabbitMQ, démarrer avec :
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev -Dspring-boot.run.arguments=--spring.rabbitmq.listener.simple.auto-startup=false
```

Si le service Cours n'existe pas encore :
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev -Dspring-boot.run.arguments=--services.cours.check-enabled=false
```

## Configuration principale

| Propriété | Valeur |
|---|---|
| `server.port` | `8083` |
| `spring.datasource.url` | `jdbc:mysql://localhost:3306/classe_db?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true` |
| `spring.jpa.hibernate.ddl-auto` | `update` |
| `eureka.client.service-url.defaultZone` | `http://localhost:8761/eureka/` |
| `services.cours.url` | `http://localhost:8081` |
| `services.cours.check-enabled` | `true` |
| CORS | `http://localhost:4200` sur `/api/**` |

## Format d'erreur

```json
{
  "timestamp": "2026-04-12T10:30:00.123456",
  "status": 404,
  "message": "Classe introuvable"
}
```

Codes : `400` (BusinessException / validation), `404` (ResourceNotFoundException), `409` (ConflictException), `503` (ServiceUnavailableException).
Les erreurs de validation listent les champs dans `message`, par exemple `Validation échouée : name: Le nom est obligatoire`.

## API REST

Toutes les routes sont préfixées par `/api/v1`.

### POST /classes

Crée une classe après vérification du cours distant. Statut initial : `ACTIVE`.

```json
{
  "name": "Java Spring Boot",
  "description": "Initiation aux microservices",
  "courseId": 1,
  "instructorId": 10,
  "capacity": 30
}
```

Réponse `201` :
```json
{
  "id": 1,
  "name": "Java Spring Boot",
  "description": "Initiation aux microservices",
  "courseId": 1,
  "instructorId": 10,
  "capacity": 30,
  "status": "ACTIVE",
  "sessions": []
}
```

### GET /classes?courseId=1&instructorId=10

Les deux filtres sont optionnels. Réponse `200` :
```json
[
  {
    "id": 1,
    "name": "Java Spring Boot",
    "description": "Initiation aux microservices",
    "courseId": 1,
    "instructorId": 10,
    "capacity": 30,
    "status": "ACTIVE",
    "sessions": []
  }
]
```

### GET /classes/1

Réponse `200` (détail avec ses séances), `404` si absente :
```json
{
  "id": 1,
  "name": "Java Spring Boot",
  "description": "Initiation aux microservices",
  "courseId": 1,
  "instructorId": 10,
  "capacity": 30,
  "status": "ACTIVE",
  "sessions": [
    {
      "id": 1,
      "classroomId": 1,
      "title": "Introduction à Spring Boot",
      "startDate": "2026-04-14T09:00:00",
      "endDate": "2026-04-14T11:00:00",
      "meetingLink": "https://meet.example.com/java-1",
      "status": "PLANNED"
    }
  ]
}
```

### PUT /classes/1

```json
{
  "name": "Java Spring Boot avancé",
  "description": "Microservices et persistance",
  "courseId": 1,
  "instructorId": 10,
  "capacity": 40
}
```

Réponse `200` : même forme que `GET /classes/1`. `409` si la nouvelle capacité est inférieure au nombre d'inscriptions non annulées.

### DELETE /classes/1

Réponse `204` (sans corps). `409` s'il existe une inscription `ACTIVE`.

### POST /classes/1/enrollments

```json
{
  "studentId": 27
}
```

Réponse `201` (statut initial `PENDING_PAYMENT`) :
```json
{
  "id": 15,
  "classroomId": 1,
  "studentId": 27,
  "enrolledAt": "2026-04-12T10:30:00",
  "status": "PENDING_PAYMENT"
}
```

Une inscription `CANCELLED` existante est réactivée en `PENDING_PAYMENT`. `409` en cas de doublon actif ou de capacité atteinte, `400` si la classe est `INACTIVE`.

### GET /classes/1/enrollments

Réponse `200` :
```json
[
  {
    "id": 15,
    "classroomId": 1,
    "studentId": 27,
    "enrolledAt": "2026-04-12T10:30:00",
    "status": "PENDING_PAYMENT"
  }
]
```

### GET /enrollments?studentId=27

Réponse `200` :
```json
[
  {
    "id": 15,
    "classroomId": 1,
    "studentId": 27,
    "enrolledAt": "2026-04-12T10:30:00",
    "status": "ACTIVE"
  }
]
```

### DELETE /enrollments/15

Annule l'inscription (statut `CANCELLED`). Réponse `204` (sans corps), `404` si absente.

### POST /classes/1/sessions

`endDate` doit être postérieure à `startDate`.

```json
{
  "title": "Introduction à Spring Boot",
  "startDate": "2026-04-14T09:00:00",
  "endDate": "2026-04-14T11:00:00",
  "meetingLink": "https://meet.example.com/java-1"
}
```

Réponse `201` :
```json
{
  "id": 1,
  "classroomId": 1,
  "title": "Introduction à Spring Boot",
  "startDate": "2026-04-14T09:00:00",
  "endDate": "2026-04-14T11:00:00",
  "meetingLink": "https://meet.example.com/java-1",
  "status": "PLANNED"
}
```

### GET /classes/1/sessions

Réponse `200` : tableau de séances triées par date de début, au format ci-dessus.

### PUT /sessions/1

Même corps que `POST /classes/1/sessions`. Réponse `200` au format d'une séance, `400` si les dates sont invalides.

### PATCH /sessions/1/status

```json
{
  "status": "COMPLETED"
}
```

Réponse `200` :
```json
{
  "id": 1,
  "classroomId": 1,
  "title": "Introduction à Spring Boot",
  "startDate": "2026-04-14T09:00:00",
  "endDate": "2026-04-14T11:00:00",
  "meetingLink": "https://meet.example.com/java-1",
  "status": "COMPLETED"
}
```

### DELETE /sessions/1

Réponse `204` (sans corps), `404` si absente.

### GET /api/v1/internal/enrollments/eligibility?classId=1&studentId=27

Endpoint interne pour les services Quiz/Examens. Réponse `200` :
```json
{
  "eligible": true,
  "enrollmentStatus": "ACTIVE"
}
```

`eligible` vaut `true` uniquement si l'inscription existe avec le statut `ACTIVE`. `enrollmentStatus` vaut `null` si aucune inscription. `404` si la classe n'existe pas.

### POST /api/v1/internal/dev/simulate-payment/15

Disponible uniquement avec le profil `dev`. Réponse `200` :
```json
{
  "processed": true,
  "eventId": "event-7f2c1d3e",
  "enrollmentId": 15
}
```

## Contrat de l'événement PaymentSucceeded

- Exchange topic : `payment.exchange`
- File : `classe.payment.succeeded.queue`
- Routing key : `payment.succeeded`
- Conversion : `JacksonJsonMessageConverter` (Jackson 3, Spring AMQP 4), champs inconnus ignorés

```json
{
  "eventId": "event-123",
  "eventType": "PaymentSucceeded",
  "occurredAt": "2026-04-12T10:30:00Z",
  "data": {
    "paymentId": "payment-001",
    "enrollmentId": 15,
    "studentId": 27,
    "amount": 150.00,
    "currency": "EUR"
  }
}
```

Traitement : l'inscription `15` passe de `PENDING_PAYMENT` à `ACTIVE`. L'idempotence est assurée par la table `processed_events` (clé unique sur `eventId`). Si l'inscription n'existe pas, un warning est journalisé sans faire échouer le message.

## Build

```bash
mvn clean package -DskipTests
```
