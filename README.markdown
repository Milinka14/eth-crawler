EthCrawler

A Spring Boot application for retrieving Ethereum blockchain data, including historical balances and transactions, using Etherscan and OpenSea APIs. Data is stored in a MySQL database, and a lightweight React frontend is provided for testing. The application runs in a Dockerized environment.

---

## Features

- Fetch transaction history and balances for Ethereum addresses
- Integrate with Etherscan and OpenSea APIs
- Store data in a MySQL database with persistent storage
- Test via a simple React-based UI
- Deploy easily with Docker and Docker Compose

---

## Prerequisites

Ensure you have the following installed:

- **Docker** and **Docker Compose** (e.g., Docker Desktop for Windows/Mac or Docker Engine for Linux)
- **Java 21** (handled within Docker)
- **Git** for cloning the repository
- **Internet access** for downloading Docker images and accessing APIs

---

## Setup

### 1. Clone the Repository

```bash
git clone <repository-url>
cd ethcrawler
```

*Replace* `<repository-url>` *with the GitHub repository URL.*

### 2. Verify Files

Confirm the following files exist in the project root:

- `docker-compose.yml` – Defines `app` and `db` services
- `Dockerfile` – Builds the Spring Boot application
- `pom.xml` – Maven dependencies
- `src/main/resources/application.properties` – Backend configuration
- `src/main/resources/static/index.html` – React frontend

### 3. Configure Environment Variables

The `docker-compose.yml` includes default environment variables:

```yaml
ETHERSCAN_API_KEY: IY6AIIRJPF7XX1B8NK9ZNS48JND3UUQF51
OPENSEA_API_KEY: 279cf63c382a4c589a5b2e7101362f41
MYSQL_ROOT_PASSWORD: 123
```

> **Note**: API keys are hardcoded for simplicity. To use custom keys, update `docker-compose.yml`.

---

## Running the Application

### 1. Start Docker Services

Build and run the containers:

```bash
docker-compose up --build
```

This starts:

- `app`: Spring Boot backend on **port 8001**
- `db`: MySQL database with persistent storage

### 2. Check Logs

Monitor service status:

```bash
docker-compose logs -f
```

Look for:

- `db-1`: `"ready for connections"`
- `app-1`: `"Started EthCrawlerApplication in X.XXX seconds"`

### 3. Access the Frontend

Open your browser and navigate to:

```
http://localhost:8001/
```

Use the React UI to:

- Fetch Ethereum address transaction history
- View current and historical balances

---

## Stopping the Application

Stop the containers:

```bash
docker-compose down
```

This preserves the MySQL database volume for data persistence.

---

## Troubleshooting

- **API Errors**: Verify Etherscan/OpenSea API keys and rate limits.
- **Docker Issues**: Ensure Docker is running and check logs for errors.
- **Port Conflicts**: Confirm port `8001` is available.

---

## Notes

- A stable internet connection is required for API access.
- For production, secure API keys using environment files or a secrets manager.
- The MySQL database retains data between container restarts via a persistent volume.

---

## Contributing

Contributions are welcome! Submit pull requests or open issues on the GitHub repository for bugs, features, or improvements.

---

## License

This project is licensed under the MIT License. See the `LICENSE` file for details.