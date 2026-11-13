# Henri Trip (back-end)

**Henri Trip** est une application de gestion de guides touristiques permettant aux administrateurs de créer et gérer des guides, d'organiser des activités au sein d'itinéraires journaliers et d'inviter des utilisateurs à consulter leurs guides. Les utilisateurs peuvent parcourir les guides auxquels ils ont accès et consulter leurs itinéraires ainsi que les détails des activités.

Ce dépôt contient la partie **back-end** de l'application, développée avec **Java** et **Spring Boot**. Elle repose sur **Spring Data JPA** pour la persistance des données, **PostgreSQL** comme système de gestion de base de données et **Flyway** pour la gestion et le versionnement du schéma de base de données.

L'application expose une **API REST** sécurisée par **authentification JWT** et s'appuie sur **Spring Security** pour la gestion des autorisations et des rôles. Elle prend notamment en charge la gestion des utilisateurs, des guides, des activités et des invitations, ainsi que le contrôle des accès aux guides selon les droits de chaque utilisateur.

## Sommaire

1. [Prérequis](#1-prérequis)
2. [Cloner le dépôt](#2-cloner-le-dépôt)
3. [Base de données](#3-base-de-données)
   - [Configuration de PostgreSQL](#configuration-de-postgresql)
   - [Initialisation du schéma](#initialisation-du-schéma)
4. [Variables d'environnement](#4-variables-denvironnement)
5. [Démarrage du back-end](#5-démarrage-du-back-end)
6. [Création du compte administrateur](#6-création-du-compte-administrateur)
7. [Front-end](#7-front-end)
8. [Fonctionnalités principales](#8-fonctionnalités-principales)
9. [Tests](#9-tests)
   - [Exécuter les tests](#exécuter-les-tests)
10. [Documentation](#10-documentation)
    - [API](#api)
    - [Javadoc](#javadoc)
11. [Technologies et bibliothèques utilisées](#11-technologies-et-bibliothèques-utilisées)
    
___
## 1. Prérequis

Les outils suivants sont nécessaires :

-   **Java 25** (JDK)
-   **Maven**
-   **Git**
-   **Docker Desktop** (pour exécuter PostgreSQL et les tests d'intégration avec Testcontainers)

## 2. Cloner le dépôt

Ouvrez un terminal et exécutez les commandes suivantes :
```
git clone https://github.com/Erika-Belicova/henri-trip-backend.git

cd henri-trip-backend
```

## 3. Base de données

### Configuration de PostgreSQL

La base de données utilise **PostgreSQL 18.6**, exécuté dans un conteneur Docker via **Docker Compose**.

Le fichier `docker-compose.yml`, situé à la racine du projet, contient la configuration du service PostgreSQL.

Démarrez le conteneur avec la commande suivante :
```
docker compose up -d
```
PostgreSQL sera accessible sur le port `5434` de votre machine. La base de données `henri_trip` est automatiquement créée lors de l'initialisation du conteneur.

### Initialisation du schéma

Les tables, les types énumérés, les contraintes et les index sont automatiquement créés et gérés par **Flyway** lors du premier démarrage de l'application Spring Boot.

Aucune importation manuelle du script SQL n'est nécessaire.

## 4. Variables d'environnement

L'application nécessite les variables d'environnement suivantes :

-   `DB_USERNAME` : nom d'utilisateur PostgreSQL.
-   `DB_PASSWORD` : mot de passe PostgreSQL.
-   `JWT_SECRET` : clé secrète utilisée pour signer et vérifier les jetons JWT.

La variable `JWT_SECRET` doit contenir une clé suffisamment longue et sécurisée (au moins 256 bits).

Vous pouvez générer une clé aléatoire avec OpenSSL :
```
openssl rand -base64 32
```

Ces variables doivent être configurées avant de démarrer l'application.

## 5. Démarrage du back-end

Depuis la racine du projet, compilez l'application et installez ses dépendances avec Maven :
```
mvn clean install
```
Cette commande exécute également les tests du projet.

Démarrez ensuite l'application Spring Boot :
```
mvn spring-boot:run
```
L'API REST sera accessible à l'adresse suivante : 
```
http://localhost:8080/
```
## 6. Création du compte administrateur

L'application ne dispose pas de point d'accès public permettant la création de comptes. Un compte administrateur doit donc être créé manuellement dans la base de données avant la première connexion.

Après le premier démarrage de l'application, connectez-vous à la base de données :
```
psql -h localhost -p 5434 -U <votre_utilisateur> -d henri_trip
```
Insérez ensuite un compte administrateur :
```sql
INSERT INTO users (email, password_hash, role, created_at, updated_at)
VALUES ('admin@henritrip.com', '<hash_bcrypt>', 'ADMIN', NOW(), NOW());
```
Remplacez `<hash_bcrypt>` par un mot de passe préalablement encodé avec BCrypt, compatible avec le `PasswordEncoder` utilisé par l'application.

Vous pourrez ensuite vous connecter avec l'adresse e-mail et le mot de passe correspondant au hash généré.

## 7. Front-end

Le back-end Henri Trip fonctionne conjointement avec une application **front-end** développée avec **Angular 21**.

Le dépôt front-end est disponible séparément : `Henri Trip (front-end)`

Pour démarrer l'interface utilisateur, consultez le README du dépôt front-end, qui décrit les prérequis, l'installation des dépendances et la configuration nécessaire.

Une fois démarrée, l'application est accessible à l'adresse suivante :
```
http://localhost:4200/
```
**Important :** le back-end Spring Boot doit être démarré et accessible sur le `port 8080` pour que le front-end puisse communiquer avec l'API.


## 8. Fonctionnalités principales

-   **Gestion des guides :** création, consultation, modification et suppression de guides touristiques.
-   **Organisation des itinéraires :** structuration des guides en plusieurs journées et organisation des activités associées.
-   **Gestion des activités :** ajout d'activités avec leurs descriptions, catégories, adresses, coordonnées de contact et informations pratiques.
-   **Gestion des invitations :** invitation d'utilisateurs à consulter des guides spécifiques.
-   **Gestion des accès :** distinction entre administrateurs et utilisateurs avec des permissions adaptées à chaque rôle.
-   **Authentification sécurisée :** authentification par JWT et protection des endpoints selon les autorisations de l'utilisateur.
-   **Consultation des guides :** accès aux guides autorisés, avec pagination et filtrage selon les droits d'accès.

## 9. Tests

Le projet dispose d'une suite de tests automatisés couvrant les différentes couches de l'application, notamment la persistance des données, la logique métier, les endpoints REST et les mécanismes de sécurité.

Les tests utilisent les outils suivants :

-   **JUnit 5 :** exécution et organisation des tests.
-   **AssertJ :** vérification des résultats et des assertions.
-   **Spring Boot Test :** tests d'intégration du contexte Spring.
-   **MockMvc :** tests des contrôleurs REST sans démarrer de serveur HTTP.
-   **Spring Security Test :** simulation de l'authentification et vérification des autorisations.
-   **Testcontainers :** utilisation d'une véritable base de données PostgreSQL dans des conteneurs Docker pendant les tests.
    
### Exécuter les tests

Depuis le répertoire du back-end, exécutez :
```
mvn clean test
```
Les résultats des tests sont affichés dans la console et des rapports détaillés sont générés dans :
```
target/surefire-reports/
```

## 10. Documentation

### API

La documentation interactive de l'API REST est générée automatiquement à partir des contrôleurs, des annotations et des informations de l'application grâce à **Springdoc OpenAPI**.

Une fois l'application démarrée, elle est accessible aux adresses suivantes :

-   **Swagger UI :** `http://localhost:8080/swagger-ui/index.html`
-   **Spécification OpenAPI (JSON) :** `http://localhost:8080/v3/api-docs`

### Javadoc

La documentation technique repose sur des commentaires Javadoc au niveau des classes et peut être générée avec Maven à l'aide du plugin Javadoc.

Depuis la racine du projet, exécutez :
```
mvn javadoc:javadoc
```

Les fichiers générés sont disponibles dans :
```
target/reports/apidocs/index.html
```
Ouvrez le fichier `index.html` dans un navigateur pour consulter la documentation.

##  11. Technologies et bibliothèques utilisées

-  **Langage et framework :** Java 25, Spring Boot 4.1.2, Spring Web MVC
-  **Base de données et persistance :** PostgreSQL 18.6, Flyway, Spring Data JPA, Hibernate
-  **Sécurité :** Spring Security, OAuth2 Resource Server, JWT
-  **Validation et mapping :** Spring Validation, ModelMapper
- **Outils de développement :** Lombok, Maven, Git, Docker, Docker Compose
- **Documentation :** Springdoc OpenAPI, Swagger UI, Javadoc
___
