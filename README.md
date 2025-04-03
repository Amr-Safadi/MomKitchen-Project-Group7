# Mom's Kitchen 🍽️

A complete restaurant management system built using **JavaFX**, **OCSF**, **Hibernate**, and **MySQL**.  
This is an enhanced version of the original OCSF Mediator Example, now supporting real-time operations for managing meals, reservations, branches, complaints, and users.

---

## 🧩 Project Structure

This project is split into three Maven modules:

### 1. `client` – JavaFX GUI  
- Built using JavaFX.
- Uses **EventBus** to implement the Mediator pattern for decoupling controllers and the networking layer.
- Allows users to:
  - **Browse meals** (per branch, by category)
  - **Place/cancel orders**
  - **Make table reservations**
  - **Submit and resolve complaints**
  - **Login based on role** (e.g., branch manager, general manager, dietitian, regular employee)
  - **Upload/view meal images**

### 2. `server` – OCSF Server  
- Built using the OCSF (Object Client-Server Framework).
- Handles:
  - Reservation allocation
  - Dynamic meal updates
  - Complaint tracking and resolution
  - Real-time sync of tables/meals/reservations across clients
  - Secure user authentication with password encryption
  - Role-based access and permission control

### 3. `entities` – Shared Module  
- Contains all data models (Entities) used by both client and server.
- Integrated with Hibernate for seamless DB persistence:
  - `Meals`, `Orders`, `Reservation`, `Branch`, `User`, `RestaurantTable`, `PriceChangeRequest`, `ContactRequest`, and more.

---

## 🚀 Getting Started

### 📦 Prerequisites
- Java 21
- Maven 3.6+
- MySQL 
- IntelliJ IDEA 

### 🔧 Running the Project

1. **Build the parent project** (right-click the root `pom.xml` > `Maven` > `Reimport` or run `mvn clean install`)
2. **Start the server**  
   - Go to `server/` module  
   - Run: `mvn exec:java`
3. **Start the client**  
   - Go to `client/` module  
   - Run: `mvn javafx:run`
4. **Login or explore as a guest**, depending on your role.

---

## 🛠️ Features Implemented

- ✅ **User Roles & Login System**
  - General Manager, Branch Manager, Dietitian, Service Employee, Regular Employee
  - Session tracking & login prevention for already-logged-in users

- ✅ **Meal Management**
  - Add/update/delete meals per branch
  - Upload meal images (stored locally by name)
  - Price change approval flow via notifications

- ✅ **Reservation System**
  - Choose time/date, number of guests, and seating preference (indoor/outdoor)
  - Branch-based table allocation
  - Live table sync between all clients

- ✅ **Complaint System**
  - Users can submit complaints
  - Service employees and managers can resolve them with optional refunds
  - Email notifications on resolution

- ✅ **Real-Time Updates**
  - Table status, reservations, meal updates, and complaints propagate to all clients live

- ✅ **Branch Management**
  - Multi-branch support (Haifa, Acre, Tel-Aviv, Netanya)
  - Branch managers only see/manage their own data
  - General manager has global access

- ✅ **Reports**
  - View orders, reservations, and complaint statistics
  - Role-based access to reports

---

## 🧠 Technologies Used

- JavaFX (UI)
- Hibernate (ORM)
- OCSF (networking)
- EventBus (Mediator pattern)
- MySQL (DB)
- Maven (build tool)
