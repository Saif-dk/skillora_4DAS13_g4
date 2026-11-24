# Skillora — frontend intégré

Application Angular unique regroupant :

- la gestion des utilisateurs (inscription, connexion, profil, administration) via le User Service `http://localhost:8081/api` ;
- la gestion des cours (catalogue, CRUD cours/leçons/ressources) via le Course Service `http://localhost:8082/api/v1`.

Le même JWT est utilisé pour les deux backends ; il est conservé dans `localStorage.skillora_token`.

## Démarrage

```bash
npm install
npm start
```

Ouvrir `http://localhost:4200/`.

## Backends requis

```bash
# User Service
cd backend/user-service
./mvnw.cmd spring-boot:run

# Course Service, profil local sans RabbitMQ
cd Backend/course-service
./mvnw.cmd spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=local"
```

## Comptes de démonstration

- `admin@skillora.fr` / `Admin123!`
- `professor@skillora.fr` / `Prof123!`
- `student@skillora.fr` / `Student123!`

## Vérifications

```bash
npm run build
npm test -- --watch=false
```
