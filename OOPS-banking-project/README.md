# Apex Banking System

This project includes a Java banking CLI and a local web dashboard backed by Java and SQLite.

## 📁 Full Folder Structure Included in Zip
```text
OOPS-banking-project/
├── pom.xml                                 # Maven Build File
├── bank_database_schema.sql                # Complete SQL Script (Basics -> Views -> Triggers)
├── erdplus_bank_schema.sql                 # ERDPlus DDL Import File
├── README.md                               # Project Guide
├── bin/
│   └── db.properties                       # JDBC Configuration Properties
├── public/
│   └── index.html                          # Executive Banking Web Dashboard (INR)
└── src/
    └── main/
        ├── java/
        │   └── com/
        │       └── bank/
        │           ├── Main.java           # CLI Entry Point
        │           ├── dao/
        │           │   ├── AccountDAO.java
        │           │   ├── CustomerDAO.java
        │           │   └── TransactionDAO.java
        │           ├── model/
        │           │   ├── Account.java
        │           │   ├── Customer.java
        │           │   └── Transaction.java
        │           ├── service/
        │           │   └── BankService.java
        │           ├── WebServer.java               # Local dashboard and JSON API
        │           └── util/
        │               ├── DatabaseInitializer.java
        │               └── DatabaseUtil.java
        └── resources/
            └── db.properties               # Resource Config File
```

---

## Run the Java CLI

Java 25 LTS or newer is required.

From PowerShell in the project folder:

```bash
.\run.ps1 -Cli
```

Alternatively, open `src/main/java/com/bank/Main.java` in VS Code and use the Java extension's **Run** action.

## Run the Web Dashboard with SQLite

From PowerShell in the project folder, run the Java backend and dashboard together:

```powershell
.\run.ps1
```

Open [http://127.0.0.1:8000](http://127.0.0.1:8000) in a browser. Stop the server with **Ctrl+C**. The web server binds to localhost and serves the dashboard and its API from the same process. From Command Prompt, `run.bat web` starts the same web mode.
Keep the PowerShell window running while using the dashboard; closing it stops the server. The default `run.ps1` mode is the web dashboard. Use `.\run.ps1 -Cli` when you want the console interface.

Dashboard balances and recent activity are read from `bank_system.db`. Deposits and transfers are written transactionally to SQLite; the database trigger updates account balances, and the dashboard reloads the values from the database after each successful operation. Restarting the app or refreshing the page does not discard those account changes.

The balance trend, spending plan, savings goal, bills, card controls, and loan widgets remain preview-only. They do not change the account balances or transaction records. The app uses a local SQLite database only; it does not connect to a real bank.

The health endpoint is available at [http://127.0.0.1:8000/api/health](http://127.0.0.1:8000/api/health).
