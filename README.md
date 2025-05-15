EthCrawler: Ethereum Blockchain Crawler
Overview
EthCrawler is a Spring Boot application that interacts with the Ethereum blockchain to retrieve historical balances and transaction data for a given Ethereum address. It integrates with Etherscan and OpenSea APIs and stores results in a MySQL database. The application includes a lightweight React-based frontend for testing and runs in a Dockerized environment with two services:

app: Spring Boot backend
db: MySQL database with persistent storage

Features

Retrieve transaction history and balances for Ethereum addresses
Integrate with Etherscan and OpenSea APIs
Store data in a MySQL database
Provide a simple React UI for testing
Deploy easily with Docker and Docker Compose

Prerequisites
Before setting up EthCrawler, ensure you have:

Docker: Docker and Docker Compose installed (e.g., Docker Desktop for Windows/Mac or Docker Engine for Linux)
Java 21: Required for the Spring Boot backend (handled within Docker)
Git: To clone the repository
Internet Access: For downloading Docker images and accessing APIs

Setup Instructions
1. Clone the Repository
Clone the EthCrawler repository and navigate to the project directory:
git clone <repository-url>cd ethcrawler
Replace <repository-url> with the GitHub repository URL.
2. Verify Key Files
Ensure the following files are present in the project root:

docker-compose.yml: Defines the app and db services
Dockerfile: Builds the Spring Boot application
pom.xml: Specifies Maven dependencies
src/main/resources/application.properties: Configures the Spring Boot app
src/main/resources/static/index.html: Provides the React frontend

3. Configure Environment Variables
The docker-compose.yml file includes default environment variables:

ETHERSCAN_API_KEY: IY6AIIRJPF7XX1B8NK9ZNS48JND3UUQF51
OPENSEA_API_KEY: 279cf63c382a4c589a5b2e7101362f41
MYSQL_ROOT_PASSWORD: 123

Note: API keys are hardcoded for simplicity. To use your own keys, update docker-compose.yml with your Etherscan and OpenSea API credentials.
Running the Application
1. Start the Services
From the project root, build and start the Docker containers:
docker-compose up --build
This launches:

app: Spring Boot backend on port 8001
db: MySQL database with persistent storage

2. Monitor Logs
Check the status of the services by viewing the logs:
docker-compose logs -f
Look for:

db-1: "ready for connections"
app-1: "Started EthCrawlerApplication in X.XXX seconds"

3. Access the Frontend
Open a browser and navigate to:
http://localhost:8001/
Use the React UI to:

Fetch Ethereum address transaction history
Retrieve current and historical balances

Stopping the Application
To stop the containers, press Ctrl+C in the terminal running docker-compose up, or run:
docker-compose down
This stops and removes the containers while preserving the MySQL database volume.
Troubleshooting

API Issues: Verify your Etherscan and OpenSea API keys and check rate limits.
Docker Issues: Ensure Docker is running and review logs for errors.
Port Conflicts: Confirm port 8001 is free.

Notes

A stable internet connection is required for API access.
For production, secure API keys using environment variable files or a secrets manager.
The MySQL database uses a persistent volume to retain data between container restarts.

Contributing
Contributions are welcome! Please submit pull requests or open issues on the GitHub repository for bugs, features, or improvements.
License
This project is licensed under the MIT License. See the LICENSE file for details.
