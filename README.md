# Gestion Location Voitures

A complete Android car rental management application developed in Java for Android. It allows rental agencies to manage their vehicle fleet, clients, reservations, and payments.

## Features

### Admin Features
- **Dashboard** - Statistics overview (available/total cars, clients, active reservations, revenue)
- **Vehicle Management** - Add/Edit/Delete vehicles with details (make, model, year, price/day, mileage, color, photo, location)
- **Client Management** - View, add, edit, delete client records
- **Reservation Management** - View all reservations, create reservations, validate/refuse pending reservations
- **Payment Management** - View all payments, validate pending payments
- **Map View** - See all vehicles on map with location markers
- **Validation Panel** - Approve/reject reservations and payments
- **PDF Contract Generation** - Generate rental contracts

### Client Features
- **Dashboard** - View available cars count
- **Browse Vehicles** - View available cars with photos, location on map
- **Make Reservations** - Select dates, calculate total price
- **My Reservations** - View own reservations, make payments, download PDF contracts
- **My Payments** - View payment history
- **Map View** - See available vehicles on map

## User Types

| Role | Access |
|------|--------|
| Admin | Full system access - manages all features |
| Client | Limited access - browse cars, make reservations, view own data |

## Database Tables

| Table | Purpose |
|------|---------|
| utilisateurs | User accounts (admin/client) |
| voitures | Vehicle fleet |
| clients | Client records |
| reservations | Rental reservations |
| paiements | Payment records |

## Technology Stack

- **Language**: Java
- **Database**: SQLite
- **Map**: OSMDroid (OpenStreetMap)
- **PDF**: Android PdfDocument API
- **Min SDK**: 23 (Android 6.0)

## Default Credentials

- **Admin**: `admin` / `admin`

## Installation

1. Clone the repository
2. Open in Android Studio
3. Build and run on device/emulator

## Screenshots

The app includes:
- Login/Register screens
- Admin and Client dashboards
- Vehicle list with filters (All/Available/Rented)
- Reservation workflow with status flow: `en_attente` → `en_cours` → `payee` → `confirmee`
- Interactive map view with vehicle markers
- PDF contract generation and sharing
