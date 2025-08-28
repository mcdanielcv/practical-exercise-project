
# Practices

Project with microservices

## Environment Configuration
### Prerequisites
- Java 17 o superior
- Maven 3.8+
- Docker y Docker Compose
- Mysql

## Installation and Configuration
    1. Clone the Repository https://github.com/mcdanielcv/practical-exercise-project.git
    2. Configure Database archivo application.properties
    3. Execute Database Script (archivo BaseDatos) 
    4. Docker deployment


## Docker deployment

- Change the value of the spring.datasource.url parameter in the application.properties file to jdbc:mysql://mysqldb:3306/microservicios_db , where mysqldb is the database container in both microservices.
- To enter the client-person microservice, you must execute the following commands:
    - mvn clean install 
    - docker build -t client-person-microservice .
- To enter the account-movement microservice, you must execute the commands:
    - mvn clean install 
    - docker build -t account-movement-microservice .
- Open Docker Desktop 
- Go to the root directory of the project in the console and run the command: docker-compose up --build -d

## Execution of unit tests

- Run all tests

    mvn test
- Unit tests only
    
    mvn test -Dtest=*Test

- Specific test
    
    mvn test -Dtest=ClientTest
    
## Execution of Integration Tests
    mvn test -Dtest=*IntegrationTest    
    
## Testing with Postman
Postman Collection included: 
    ejercicio_practico.postman_collection

### Client

| Method | Endpoint | Description |
| --- | --- | --- |
| `GET` | /api/clients |   Get all clients |
| `GET` | /api/clients/{id} | Get client by ID  |
| `POST` | /api/clients/register | Create new client  |
| `POST` | /api/clients/register-with-account |Create new customer and account asynchronously  |
| `PATH` | /api/clients/{id} | Update client |
| `DELETE` | /api/clients/{id} | Delete client  |


### Account

| Method | Endpoint | Description |
| --- | --- | --- |
| `GET` | /api/accounts |   Get all account |
| `GET` | /api/accounts/{id} | Get account by ID  |
| `POST` | /api/accounts | Create new account  |
| `PUT` | /api/accounts/{id} | Update account |
| `DELETE` | /api/accounts/{id} | Delete account  |

### Transactions

| Method | Endpoint | Description |
| --- | --- | --- |
| `GET` | /api/transactions |   Get all transactions |
| `POST` | /api/transactions | Create new transactions  |
| `DELETE` | /api/transactions/{id} | Delete transactions  |


## 🔗 Links
[![portfolio](https://img.shields.io/badge/my_portfolio-000?style=for-the-badge&logo=ko-fi&logoColor=white)](https://github.com/mcdanielcv/practical-exercise-project)

[![linkedin](https://img.shields.io/badge/linkedin-0A66C2?style=for-the-badge&logo=linkedin&logoColor=white)](https://www.linkedin.com/in/mario-castellanosv)




