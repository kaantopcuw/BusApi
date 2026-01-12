# 🚌 BusApi - Bus Ticket Reservation System

![Java](https://img.shields.io/badge/Java-25-orange?style=for-the-badge&logo=java)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.0.1-green?style=for-the-badge&logo=spring)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue?style=for-the-badge&logo=postgresql)
![Docker](https://img.shields.io/badge/Docker-Available-blue?style=for-the-badge&logo=docker)

## 📖 Overview
BusApi is a robust backend solution designed for intercity bus ticket reservations. Leveraging my extensive experience in the travel domain (Airline/GDS), this project adapts complex booking workflows to the bus industry.

It provides a comprehensive set of RESTful endpoints for searching expeditions, selecting seats, managing passenger information, and processing ticket sales. The system is designed with **scalability** and **clean code principles** in mind.

## 🚀 Key Features
* **Expedition Search:** Optimized query performance for listing available bus trips based on origin, destination, and date.
* **Seat Management:** Real-time seat availability checks and concurrency handling (preventing double-booking).
* **Booking Lifecycle:** PNR generation, reservation hold, and ticket confirmation flows.
* **Admin Panel API:** Endpoints for managing bus fleets, routes, and schedules.
* **Security:** JWT based authentication and role-based access control (Admin/User).

## 🛠️ Tech Stack & Architecture
This project showcases modern backend engineering practices:

* **Language:** Java 21 (Utilizing latest features like Records, Pattern Matching)
* **Framework:** Spring Boot 3.x (Spring Web, Spring Data JPA, Spring Security)
* **Database:** PostgreSQL (with complex relational mapping)
* **Caching:** Redis (For caching frequent expedition searches) *[Eğer eklediyseniz]*
* **Containerization:** Docker & Docker Compose
* **Testing:** JUnit 5 & Mockito
* **Documentation:** Swagger / OpenAPI UI

## 🏗️ Architecture Design
The project follows **Layered Architecture** (Controller -> Service -> Repository) enriched with **SOLID** principles to ensure maintainability.
* **DTO Pattern:** Used to decouple the internal domain model from the API presentation layer.
* **Global Exception Handling:** Centralized error management for consistent API responses.

## ⚡ Getting Started

### Prerequisites
* Java 21 or higher
* Docker & Docker Compose

### Run with Docker
The easiest way to stand up the application and the database:
```bash
docker-compose up -d