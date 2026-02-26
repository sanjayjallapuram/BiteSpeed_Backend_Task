Live Deployment

The application is deployed on Render.

URL: https://bitespeed-backend-task-lrcc.onrender.com/

### Endpoint

POST https://bitespeed-backend-task-lrcc.onrender.com/identify


# To Run Locally

## Clone Repository

git clone https://github.com/your-username/identity-reconciliation.git


## Set Environment Variables


$env:DB_URL="jdbc:postgresql://your-host:5432/your-db?sslmode=require"

$env:DB_USER="your-username"

$env:DB_PASS="your-password"

## Start Application

./mvnw spring-boot:run

Windows:

mvnw.cmd spring-boot:run


POST http://localhost:8080/identify
