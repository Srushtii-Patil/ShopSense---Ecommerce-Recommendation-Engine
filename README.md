# 🛍️ ShopSense — E-Commerce Recommendation Engine

> **Personalized Product Recommendations & Dynamic Cart Suggestions**  
> Built with **Java 17**, **JDBC**, and **MySQL / MySQL-Compatible Hybrid Database**.

---

## 🌟 Key Highlights & Features

### 1. 🎯 Multi-Factor Personalized Recommendation Engine
ShopSense implements a hybrid scoring algorithm evaluated across 5 key dimensions:
* **User Preference Alignment**: Matches customer preferred categories (+25 pts) and preferred brands (+20 pts).
* **Collaborative Filtering & Co-occurrence**: Analyzes historical multi-item orders (`order_items`) across all users to compute real-time product affinity: *"Customers who bought item X also frequently bought Y"* (+25 pts).
* **Budget Headroom & Price Sensitivity**:
  * Evaluates remaining budget headroom: $\text{Headroom} = \text{Budget} - \text{Cart Total}$.
  * Adapts to customer sensitivity tiers (**BUDGET**, **BALANCED**, **PREMIUM**):
    * **BUDGET**: Rewards highest rating-per-dollar and items under 50% of budget.
    * **BALANCED**: Optimizes mid-tier sweet spots.
    * **PREMIUM**: Surfaces flagship items with top tier build quality.
* **Bayesian Quality & Popularity**: Combines product rating (4.0–5.0★) with verified review volume.
* **Smart Repurchase Suppression**: Suppresses durable goods already owned by the user (e.g. laptops, phones) while promoting accessories and complementary items.
* **Explainable AI (XAI)**: Generates human-readable reasons for every recommendation:
  * *"Matches your preferred category: Audio"*
  * *"Matches your preferred brand: Apple"*
  * *"Frequently bought together with items in your purchase history"*
  * *"Fits comfortably within your remaining $1,100.01 budget"*

---

### 2. 💡 Dynamic Cart Suggestions & Real-Time Basket Advisor
When items are added to the active shopping cart, ShopSense calculates dynamic real-time suggestions:
* **Complementary Cross-Sell**: Suggests accessories specifically engineered for items currently in cart (e.g. Laptop $\rightarrow$ USB-C 8-in-1 Hub, 360° Protective Sleeve, Ergonomic Stand).
* **Budget Gap Maximizer**: Identifies the highest-rated add-ons that fit *strictly within* the user's remaining budget headroom without exceeding it.
* **Free Shipping Threshold Booster**: Monitors cart total against the delivery threshold ($100.00). Highlights high-value add-ons that close the gap to unlock free delivery and save shipping fees.

---

### 3. 🗄️ Database Architecture (MySQL & JDBC)
* **Schema**:
  * `users` — Customer identity, email, budget limit, preferred categories, preferred brands, price sensitivity tier.
  * `products` — Product catalog with category, brand, price, rating, review counts, feature tags, stock, and descriptions.
  * `orders` & `order_items` — Transaction history with relational links used for collaborative filtering.
  * `cart_items` — Dynamic active shopping cart state per customer.
  * `product_complements` — Domain affinity relationships.
* **Hybrid Database Engine**:
  * Configured via `db.properties`.
  * Connects directly to **MySQL 8.x / 9.x** via official `mysql-connector-j-8.4.0.jar`.
  * Features an automatic embedded fallback mode (H2 with `MODE=MySQL`) for zero-friction setup even if MySQL credentials have not been configured yet.

---

## 🚀 Quick Start & How to Run

### Option 1: One-Click Windows Launcher
Double-click `run.bat` or run in PowerShell:
```powershell
.\run.bat
```
* Automatically compiles sources into `bin/`.
* Starts the ShopSense Web Server at **http://localhost:8080**.
* Launches the interactive console CLI.

---

### Option 2: Running via Java Command Line
To compile and launch manually:
```powershell
# 1. Compile Java sources
& "C:\Program Files\Eclipse Adoptium\jdk-17.0.12.7-hotspot\bin\javac.exe" -d bin -cp "lib/*" (Get-ChildItem -Path "src" -Recurse -Filter "*.java" | ForEach-Object { $_.FullName })

# 2. Launch Web Server & Interactive CLI
& "C:\Program Files\Eclipse Adoptium\jdk-17.0.12.7-hotspot\bin\java.exe" -cp "bin;lib/*" com.shopsense.Main --web
```

---

## ⚙️ MySQL Database Configuration (`db.properties`)

To connect ShopSense to your local MySQL Server:
1. Open `db.properties` in the project root:
   ```properties
   # Mode: 'auto', 'mysql', or 'embedded'
   db.mode=auto

   # MySQL 8.x / 9.x Server Configuration
   mysql.driver=com.mysql.cj.jdbc.Driver
   mysql.url=jdbc:mysql://localhost:3306/shopsense?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
   mysql.user=root
   mysql.password=your_mysql_password_here

   # Web Server Port
   server.port=8080
   ```
2. When launched, ShopSense automatically verifies the MySQL connection, creates tables via `sql/schema.sql`, and seeds initial catalog data via `sql/sample_data.sql`.

---

## 👥 Preloaded Customer Personas for Testing

| Customer Profile | Budget | Preference Categories | Preferred Brands | Price Tier | Starting Cart |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Alex Chen** | $1,200.00 | Laptops, Accessories, Audio | Apple, Sony, Logitech | PREMIUM | Logitech MX Master 3S Mouse |
| **Sarah Miller** | $450.00 | Audio, Accessories, Wearables | Sony, Bose, Apple | BALANCED | Anker 120W GaN Charger |
| **David Kim** | $120.00 | Accessories, Smart Home | Anker, Logitech, TP-Link | BUDGET | Empty |
| **Emma Watson** | $850.00 | Laptops, Smart Home, Audio | Apple, Philips, Sony | BALANCED | Empty |
| **Marcus Vance** | $650.00 | Gaming, Audio, Accessories | Razer, Sony, Logitech | BALANCED | Empty |

---

## 🌐 REST API Endpoints

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/api/users` | List all customer profiles |
| `GET` | `/api/user?id={id}` | Retrieve profile for customer `{id}` |
| `POST` | `/api/user/budget` | Update customer budget dynamically |
| `POST` | `/api/user/preferences` | Update preferred categories, brands, or price tier |
| `GET` | `/api/recommendations?userId={id}` | Retrieve personalized product recommendations with score breakdown & reasons |
| `GET` | `/api/cart?userId={id}` | Retrieve active cart items, subtotal, and remaining budget |
| `POST` | `/api/cart/add` | Add product to active cart (`userId`, `productId`, `quantity`) |
| `POST` | `/api/cart/update` | Update quantity for a cart item |
| `POST` | `/api/cart/remove` | Remove item from cart |
| `POST` | `/api/cart/clear` | Clear all items from active cart |
| `GET` | `/api/cart/suggestions?userId={id}` | Retrieve real-time dynamic cart suggestions (cross-sells & budget add-ons) |
| `POST` | `/api/checkout` | Process order transaction and update collaborative purchase history |
| `GET` | `/api/orders?userId={id}` | Retrieve customer order and purchase history |
| `GET` | `/api/db-status` | Check active database engine and JDBC connection parameters |

---

## 📂 Project Directory Structure

```
ShopSense/
├── lib/
│   ├── mysql-connector-j-8.4.0.jar       # MySQL JDBC Connector/J driver
│   └── h2-2.2.224.jar                    # Embedded MySQL-mode fallback engine
├── src/
│   └── com/shopsense/
│       ├── Main.java                     # Application entry point
│       ├── config/
│       │   ├── DatabaseConfig.java       # Connection manager (MySQL & embedded fallback)
│       │   └── DatabaseInitializer.java  # DDL execution & seed script runner
│       ├── model/
│       │   ├── User.java                 # Customer profile & budget entity
│       │   ├── Product.java              # Product catalog entity
│       │   ├── Order.java & OrderItem.java # Transaction & purchase history
│       │   ├── CartItem.java             # Shopping cart item entity
│       │   ├── Recommendation.java       # Recommendation scoring & reason breakdown
│       │   └── CartSuggestion.java       # Cross-sell & budget suggestion model
│       ├── dao/
│       │   ├── UserDAO.java              # User JDBC operations
│       │   ├── ProductDAO.java           # Product catalog queries & complement lookups
│       │   ├── OrderDAO.java             # Order history & collaborative filtering SQL
│       │   └── CartDAO.java              # Dynamic cart JDBC operations
│       ├── engine/
│       │   ├── RecommendationEngine.java # Multi-factor personalized scorer
│       │   └── CartSuggestionEngine.java # Real-time cross-sell & budget gap advisor
│       ├── service/
│       │   └── ShopSenseService.java     # Business logic orchestrator
│       ├── server/
│       │   ├── ShopSenseWebServer.java   # Built-in lightweight HTTP REST & Web server
│       │   └── JsonHelper.java           # Zero-dependency JSON serializer
│       └── cli/
│           └── ShopSenseCLI.java         # Interactive terminal CLI
├── sql/
│   ├── schema.sql                        # MySQL DDL table schema
│   └── sample_data.sql                   # Realistic products, users & multi-item orders
├── web/
│   ├── index.html                        # Modern glassmorphic web dashboard
│   ├── style.css                         # CSS design system & micro-animations
│   └── app.js                            # Interactive client-side controller
├── db.properties                         # Database configuration
├── run.bat                               # Windows 1-click launcher
├── compile.bat                           # Compilation batch script
└── README.md                             # Comprehensive documentation
```
