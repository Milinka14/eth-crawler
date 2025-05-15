# EthCrawler

EthCrawler is a Spring Boot application that fetches Ethereum blockchain data, including transaction history and balances, using Etherscan and OpenSea APIs. It stores data in a MySQL database for optimization and includes a React frontend for testing. The app runs in a Dockerized environment for easy deployment.

---

## Features

- Fetch Ethereum address transactions and balances
- Integrate with Etherscan and OpenSea APIs
- Store data in a MySQL database with persistence
- Test via a React-based UI
- Run with Docker and Docker Compose

---

## Prerequisites

- **Docker** and **Docker Compose** (Docker Desktop for Windows/Mac or Docker Engine for Linux)
- **Git** for cloning the repository
- **Internet access** for APIs and Docker images

---

## Setup

1. **Clone the Repository**

   ```bash
   git clone https://github.com/Milinka14/eth-crawler.git
   cd ethcrawler
   ```
   
2. **Verify Files**

   Ensure these files are in the project root:
   - `docker-compose.yml` (defines services)
   - `Dockerfile` (builds Spring Boot app)
   - `pom.xml` (Maven dependencies)
   - `src/main/resources/application.properties` (app config)
   - `src/main/resources/static/index.html` (React frontend)

3. **Configure Environment**

   The `docker-compose.yml` includes:
   ```yaml
   ETHERSCAN_API_KEY: IY6AIIRJPF7XX1B8NK9ZNS48JND3UUQF51
   OPENSEA_API_KEY: 279cf63c382a4c589a5b2e7101362f41
   MYSQL_ROOT_PASSWORD: 123
   ```

   > **Note**: API keys are hardcoded for simplicity. Update `docker-compose.yml` with your own keys if needed.

---

## Running the App

1. **Start Services**

   ```bash
   docker-compose up --build
   ```

   This runs:
   - `app`: Spring Boot backend on port **8001**
   - `db`: MySQL database with persistent storage

   > **Note**: If the app doesn’t start automatically after `docker-compose up --build`, manually start the app service in Docker Desktop.

2. **Check Logs** (Optional)

   ```bash
   docker-compose logs -f
   ```

   Confirm:
   - `db-1`: Shows `"ready for connections"`
   - `app-1`: Shows `"Started EthCrawlerApplication in X.XXX seconds"`

3. **Access the Frontend**

   Open: [http://localhost:8001/](http://localhost:8001/)

   Use the React UI to:
   - Fetch transaction history
   - View address balances

---

## Troubleshooting

- **API Issues**: Check Etherscan/OpenSea API keys and rate limits.
- **Docker Errors**: Ensure Docker is running; review logs.
- **Port Conflicts**: Verify port `8001` is free.

---

## Notes

- Requires internet for API access and Docker image downloads.
- For production, secure API keys with environment files or a secrets manager.
- MySQL data persists between container restarts.

---
