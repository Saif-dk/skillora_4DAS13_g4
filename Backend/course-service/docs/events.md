# Événements RabbitMQ - course-service

## Périmètre

Cette intégration concerne uniquement le microservice `course-service`. Aucun fichier d’un autre microservice n’a été modifié.

## Exchange

- Nom : `skillora.events`
- Type : `TopicExchange`
- Constante : `RabbitMQConfig.SKILLORA_EXCHANGE`

## Publication

### `CourseCreated`

- Routing key : `course.created`
- Producteur : `CourseEventPublisher`
- Déclenchement : après création réussie d’un cours dans `CourseServiceImpl#createCourse`
- Payload (`CourseCreatedEvent`) :
  - `eventId` : identifiant unique d’événement
  - `courseId` : identifiant du cours créé
  - `title` : titre du cours
  - `teacherId` : identifiant de l’enseignant
  - `occurredAt` : horodatage de l’événement

## Consommation

### `PaymentSucceeded`

- Queue : `course.payment.succeeded.queue`
- Routing key : `payment.succeeded`
- Binding : `course.payment.succeeded.queue` <- `skillora.events` (`payment.succeeded`)
- Listener : `PaymentSucceededListener`
- Payload (`PaymentSucceededEvent`) :
  - `eventId`
  - `paymentId`
  - `enrollmentId`
  - `studentId`
  - `courseId`
  - `amount`
  - `currency`
  - `occurredAt`

## Idempotence

La table `processed_events` (Flyway `V2__create_processed_events.sql`) stocke les événements traités :

- `id` BIGINT PK auto-increment
- `event_id` VARCHAR(100) NOT NULL UNIQUE
- `event_type` VARCHAR(100) NOT NULL
- `processed_at` TIMESTAMP NOT NULL

`PaymentSucceededListener` vérifie `ProcessedEventRepository.existsByEventId(eventId)` avant traitement. Si l’événement est déjà connu, il est ignoré et aucun traitement métier n’est rejoué. Le listener enregistre un `ProcessedEvent` puis journalise l’événement ; il n’invente aucune modification de domaine enrollment/paiement.

## Tests

- `CourseEventPublisherTest` : vérifie l’appel `RabbitTemplate.convertAndSend` avec l’exchange et la routing key.
- `PaymentSucceededListenerTest` : vérifie l’enregistrement d’un événement et l’idempotence sur doublon.
- `application-test.yml` désactive l’auto-startup des listeners Rabbit (`spring.rabbitmq.listener.simple.auto-startup=false`) pour éviter toute dépendance à un broker pendant les tests.
