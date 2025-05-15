          EthCrawler (Ethereum Crawler)
Overview:
---------
EthCrawler is a Spring Boot application designed to interact with the Ethereum blockchain.
It retrieves historical balances and transactions for a given Ethereum address using 
Etherscan and OpenSea APIs. Results are stored in a MySQL database.

A lightweight React-based frontend is included for testing, and the entire application 
runs in a Dockerized environment with two services:
  - app (Spring Boot backend)
  - db (MySQL database)

-------------------------------------
Prerequisites:
--------------
- Docker: Docker + Docker Compose installed
  (Docker Desktop for Windows/Mac or Docker Engine for Linux)
- Java 21: Required to build the backend (handled inside Docker)
- Git: To clone the repository
- Internet Access: Required to download Docker images and access APIs

-------------------------------------
Setup Instructions:
-------------------

1. Clone the Repository:
   ----------------------
   Run the following in your terminal:
     git clone <repository-url>
     cd ethcrawler

   Replace <repository-url> with the GitHub URL provided.

2. Verify Key Files:
   ------------------
   Ensure the following files exist:
     - docker-compose.yml            (Defines app and db services)
     - Dockerfile                    (Builds the Spring Boot app)
     - pom.xml                       (Maven dependencies)
     - src/main/resources/application.properties (App config)
     - src/main/resources/static/index.html     (React test frontend)

3. Environment Variables:
   -----------------------
   docker-compose.yml already includes:
     - ETHERSCAN_API_KEY: IY6AIIRJPF7XX1B8NK9ZNS48JND3UUQF51
     - OPENSEA_API_KEY : 279cf63c382a4c589a5b2e7101362f41
     - MYSQL_ROOT_PASSWORD: 123

   *Note: For simplicity, API keys are hardcoded. Consider using real env vars in production.*

-------------------------------------
Running the Application:
-------------------------

1. Start the Containers:
   ----------------------
   From the project root, run:
     docker-compose up --build

   This starts:
     - app: Spring Boot backend (on port 8001)
     - db : MySQL database with persistent volume

2. Monitor Logs:
   --------------
   To check the status of the services:
     docker-compose logs -f

   Look for:
     - db-1  : "ready for connections"
     - app-1 : "Started EthCrawlerApplication in X.XXX seconds"

3. Access the Frontend:
   ---------------------
   Open your browser and go to:
     http://localhost:8001/

   Use the React-based UI to:
     - Fetch transaction history
     - Get Ethereum address balances
-------------------------------------
