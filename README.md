# Contact-Service

## Description
Communication management service for SUPMAP that handles user support requests, newsletter subscriptions,
and email notifications. Enables administrators to send bulk newsletters, processes user feedback
through support tickets, and manages user communication preferences with comprehensive metrics tracking
for engagement analysis.

## Features
- Friend request management (send, accept, reject)
- User contact list management
- Social sharing of routes and incident reports
- Community contribution management
- User relationship status tracking
- Real-time notifications for social interactions

## Tech Stack
- Java 21
- Spring Boot 3.4.4
- MongoDB for relationship data storage
- Kafka for event-driven social notifications
- Prometheus for monitoring
- Spring Mail for notification emails

## Dependencies
- shared-models: Common data models across SUPMAP services
- database-utils: Database utility functions
- Spring Boot Web for RESTful API endpoints
- Spring Boot Data MongoDB for data persistence
- Kafka for inter-service communication
- Spring Mail for email notifications
- Micrometer/Prometheus for metrics collection

## Configuration
The service can be configured through environment variables:

```yaml
supmap:
 properties:
   app-email: your-app-email@example.com
   database-name: contact_service_db
   elasticsearch-password: your-es-password
   elasticsearch-url: http://elasticsearch:9200
   elasticsearch-username: elastic
   kafka-bootstrap-servers: kafka:9092
   mail-modified-username: true
   mail-password: your-mail-password
   mongo-uri: mongodb://user:password@mongodb:27017/contact_service_db