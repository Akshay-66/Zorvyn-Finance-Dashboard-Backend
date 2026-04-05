* Finance Dashboard Backend

This is a simple backend assignment project by Akshay for a finance dashboard system.

I used plain Java, PostgreSQL, JDBC, and Docker. I kept the code simple, structured, scalable and readable.

* Tech Stack

- Java 17
- PostgreSQL 16
- JDBC
- Built in Java `HttpServer`
- Maven
- Docker and Docker Compose

* Why This Approach

Ive previously worked in Zoho with the exact same backend stack core java + postgreSQL so it feels comfortable to me to build this project using these and also the system built is scalable and modular, thus anyone can understand the flow easily and changes without affecting other parts of the system.

I still tried to keep the code structured well:

- `handler` for HTTP layer
- `service` for business logic
- `repository` for database queries
- `security` for role-based access control
- `dto` for request and response objects

* Design Ideas Used

- `Strategy pattern` in the access control layer. Each role has its own permission strategy.
- `Facade pattern` through `FinanceFacade` so handlers can use one simple object instead of depending on many services.
- `SOLID and DRY` by separating request handling, business rules, and database code.

* Features Covered

** Mandatory

- User creation and management
- Role assignment
- Active and inactive user status
- Role based access control
- Financial record CRUD
- Filtering records by type, category, date range
- Dashboard summary APIs
- Validation and proper error responses
- PostgreSQL persistence

** Optional enhancements added

- Simple token-based authentication using `X-Auth-Token`
- Pagination in record listing
- Search in records using category or notes
- Soft delete for financial records
- Docker Compose one command startup

* Run the Project

Make sure Docker is installed, then run:


`docker compose up --build`


The API will start on:

http://localhost:8080


* Demo Users

These users are inserted automatically when PostgreSQL starts:

| Role | Email | Token |
|------|-------|-------|
| Admin - `admin@zorvyn.com` - `admin-token` |
| Analyst - `analyst@zorvyn.com` - `analyst-token` |
| Viewer - `viewer@zorvyn.com` - `viewer-token` |

Use the token in the request header:

`X-Auth-Token: admin-token`

* Role Access

| Role | Permissions |
|------|-------------|
| Viewer -> Can access dashboard summary only |
| Analyst -> Can view records and dashboard summary |
| Admin -> Full access to users, records and dashboard

* Main API Endpoints

** Health

- `GET /api/health`

** Current User

- `GET /api/me`

** Users

- `GET /api/users` admin only
- `GET /api/users/{id}` admin only
- `POST /api/users` admin only
- `PATCH /api/users/{id}` admin only

** Records

- `GET /api/records` analyst and admin
- `GET /api/records/{id}` analyst and admin
- `POST /api/records` admin only
- `PUT /api/records/{id}` admin only
- `PATCH /api/records/{id}` admin only
- `DELETE /api/records/{id}` admin only

** Dashboard

- `GET /api/dashboard/summary` viewer, analyst, admin

* Record Filters

`GET /api/records` supports:

- `type=INCOME` or `EXPENSE`
- `category=Rent`
- `search=salary`
- `fromDate=2026-03-01`
- `toDate=2026-03-31`
- `page=1`
- `size=10`

Example:

`
curl "http://localhost:8080/api/records?type=EXPENSE&page=1&size=5" \
  -H "X-Auth-Token: analyst-token"
`

for cmd
`curl "http://localhost:8080/api/records?type=EXPENSE&page=1&size=5" -H "X-Auth-Token: analyst-token"`

Sample output response

`{"items":[{"id":"10000000-0000-0000-0000-000000000005","amount":4200.00,"type":"EXPENSE","category":"Transport","date":"2026-03-18","notes":"Travel and fuel","createdBy":"00000000-0000-0000-0000-000000000001","createdAt":"2026-04-05T07:13:33.321161","updatedAt":"2026-04-05T07:13:33.321161"},{"id":"10000000-0000-0000-0000-000000000003","amount":3500.00,"type":"EXPENSE","category":"Groceries","date":"2026-03-06","notes":"Weekly groceries","createdBy":"00000000-0000-0000-0000-000000000002","createdAt":"2026-04-05T07:13:33.321161","updatedAt":"2026-04-05T07:13:33.321161"},{"id":"10000000-0000-0000-0000-000000000002","amount":15000.00,"type":"EXPENSE","category":"Rent","date":"2026-03-03","notes":"House rent","createdBy":"00000000-0000-0000-0000-000000000001","createdAt":"2026-04-05T07:13:33.321161","updatedAt":"2026-04-05T07:13:33.321161"}],"page":1,"size":5,"totalItems":3,"totalPages":1}`

* Sample Requests

** Get dashboard summary

`curl "http://localhost:8080/api/dashboard/summary" \
  -H "X-Auth-Token: viewer-token"
`

for cmd
`curl "http://localhost:8080/api/dashboard/summary" -H "X-Auth-Token: viewer-token"`

Sample output response

`{"totalIncome":128000.00,"totalExpense":25200.00,"netBalance":102800.00,"categoryTotals":[{"category":"Salary","type":"INCOME","total":120000.00},{"category":"Rent","type":"EXPENSE","total":15000.00},{"category":"Freelance","type":"INCOME","total":8000.00},{"category":"Transport","type":"EXPENSE","total":4200.00},{"category":"Groceries","type":"EXPENSE","total":3500.00},{"category":"Food","type":"EXPENSE","total":2500.00}],"recentActivity":[{"id":"32272827-47a0-4cef-9050-4471dda60ffb","amount":2500.00,"type":"EXPENSE","category":"Food","date":"2026-04-01","notes":"Team lunch","createdBy":"00000000-0000-0000-0000-000000000001","createdAt":"2026-04-05T07:24:35.353266","updatedAt":"2026-04-05T07:24:35.353266"},{"id":"10000000-0000-0000-0000-000000000005","amount":4200.00,"type":"EXPENSE","category":"Transport","date":"2026-03-18","notes":"Travel and fuel","createdBy":"00000000-0000-0000-0000-000000000001","createdAt":"2026-04-05T07:13:33.321161","updatedAt":"2026-04-05T07:13:33.321161"},{"id":"10000000-0000-0000-0000-000000000004","amount":8000.00,"type":"INCOME","category":"Freelance","date":"2026-03-15","notes":"Website project payment","createdBy":"00000000-0000-0000-0000-000000000002","createdAt":"2026-04-05T07:13:33.321161","updatedAt":"2026-04-05T07:13:33.321161"},{"id":"10000000-0000-0000-0000-000000000003","amount":3500.00,"type":"EXPENSE","category":"Groceries","date":"2026-03-06","notes":"Weekly groceries","createdBy":"00000000-0000-0000-0000-000000000002","createdAt":"2026-04-05T07:13:33.321161","updatedAt":"2026-04-05T07:13:33.321161"},{"id":"10000000-0000-0000-0000-000000000002","amount":15000.00,"type":"EXPENSE","category":"Rent","date":"2026-03-03","notes":"House rent","createdBy":"00000000-0000-0000-0000-000000000001","createdAt":"2026-04-05T07:13:33.321161","updatedAt":"2026-04-05T07:13:33.321161"}],"monthlyTrends":[{"month":"2026-03","income":128000.00,"expense":22700.00},{"month":"2026-04","income":0,"expense":2500.00}]}`

** Create a user

`curl -X POST "http://localhost:8080/api/users" \
  -H "Content-Type: application/json" \
  -H "X-Auth-Token: admin-token" \
  -d "{\"name\":\"Rahul\",\"email\":\"rahul@example.com\",\"role\":\"ANALYST\",\"status\":\"ACTIVE\"}"
`

** Create a record

`curl -X POST "http://localhost:8080/api/records" \
  -H "Content-Type: application/json" \
  -H "X-Auth-Token: admin-token" \
  -d "{\"amount\":2500,\"type\":\"EXPENSE\",\"category\":\"Food\",\"date\":\"2026-04-01\",\"notes\":\"Team lunch\"}"
`

for cmd
`curl -X POST "http://localhost:8080/api/records" -H "Content-Type: application/json" -H "X-Auth-Token: admin-token" -d "{\"amount\":2500,\"type\":\"EXPENSE\",\"category\":\"Food\",\"date\":\"2026-04-01\",\"notes\":\"Team lunch\"}"`

sample output response

`{"id":"32272827-47a0-4cef-9050-4471dda60ffb","amount":2500.00,"type":"EXPENSE","category":"Food","date":"2026-04-01","notes":"Team lunch","createdBy":"00000000-0000-0000-0000-000000000001","createdAt":"2026-04-05T07:24:35.353266","updatedAt":"2026-04-05T07:24:35.353266"}`

* Sample Response Shape

Dashboard summary response contains:

- `totalIncome`
- `totalExpense`
- `netBalance`
- `categoryTotals`
- `recentActivity`
- `monthlyTrends`

* Validation and Error Handling

The backend returns:

- `400` for invalid input
- `401` for missing or wrong token
- `403` for role or inactive user restriction
- `404` for missing route or missing resource
- `405` for wrong HTTP method
- `409` for duplicate email
- `500` for unexpected server error

Errors are returned in JSON format with status, message, and timestamp.

* Assumptions

- Authentication is simplified using pre-generated tokens instead of login and password.
- User management is only allowed for admin to keep access rules clear.
- Viewer is restricted to dashboard summary only.
- Deleting a record means soft delete, so old data is not permanently removed.
- This is not production-ready and does not include connection pooling or advanced security.

* Project Structure - tree view -t

`
src/main/java/com/zorvyn/finance
|-- config
|-- db
|-- dto
|-- exception
|-- handler
|-- model
|-- repository
|-- security
|-- service
|-- util
`

* Notes

- Before docker up first turn on docker desktop or similar wsl.
- PostgreSQL tables and demo data are created from `db/init.sql`
- The app waits for the database to become ready when started through Docker Compose
- Since this is a plain Java project, the code stays close to core backend concepts like routing, validation, SQL, and access control.

* Final words

- Thank you and please let me know for any other clarification about the project :).
- And the imports are automatically done by Intel Ij idea IDE.
- I've tried to give my best with my knowledge on system design and backend development.
- To get system and DB design clarity I've attached attached images below `README.md` file for your reference, Its in `zorvyn\docs\diagrams`.

"# Zorvyn-Finance-Dashboard-Backend" 
