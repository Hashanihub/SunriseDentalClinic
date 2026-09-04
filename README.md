# Sunrise Dental Clinic Management System

## Project Overview

Sunrise Dental Clinic Management System is a comprehensive web-based application designed to modernize the operations of Sunrise Dental Clinic in Colombo. The system replaces manual paper-based processes with a digital solution that manages patient appointments, treatment records, billing, and reporting.

## Features

- **User Authentication**: Secure login with role-based access control
- **Patient Management**: Register, search, and manage patient records
- **Appointment Management**: Schedule, reschedule, cancel, and track appointments
- **Dentist Management**: Manage dentist profiles and availability
- **Treatment Management**: Define treatments with costs and consultation fees
- **Billing System**: Generate bills with automatic calculation of treatment costs
- **Reports**: Comprehensive reports for decision making
- **Responsive UI**: Professional interface optimized for all devices

## Technology Stack

### Backend
- Java 17
- Jakarta Servlets 6.0
- JDBC
- Maven 3.9+
- MySQL 8.0

### Frontend
- HTML5
- CSS3
- Vanilla JavaScript

### Server
- Apache Tomcat 10.1+

### Testing
- JUnit 5
- Mockito

### DevOps
- Git
- GitHub Actions CI/CD

## Architecture

The application follows a 3-Tier Architecture:

1. **Presentation Layer**: HTML, CSS, JavaScript (Frontend)
2. **Business Logic Layer**: Java Service classes
3. **Data Access Layer**: DAO classes with JDBC

### Design Patterns
- MVC Pattern
- DAO Pattern
- Service Layer Pattern
- Singleton Pattern
- Factory Pattern

## Database Design

The MySQL database `sunrise_dental_db` contains the following tables:
- users
- patients
- dentists
- treatments
- appointments
- bills
- bill_items
- payments

## Installation Guide

### Prerequisites

- Java 17 or 21
- Apache Maven 3.9+
- MySQL 8.0+
- Apache Tomcat 10.1+
- Git (optional)

### Database Setup

1. Start MySQL server
2. Run SQL scripts in order:
   ```bash
   mysql -u root -p < database/01_create_database.sql
   mysql -u root -p sunrise_dental_db < database/02_create_tables.sql
   mysql -u root -p sunrise_dental_db < database/03_insert_sample_data.sql
   mysql -u root -p sunrise_dental_db < database/04_constraints_and_indexes.sql
   mysql -u root -p sunrise_dental_db < database/05_views.sql
   mysql -u root -p sunrise_dental_db < database/06_stored_procedures.sql
   mysql -u root -p sunrise_dental_db < database/07_triggers.sql# SunriseDentalClinic