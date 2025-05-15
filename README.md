EthCrawler: Ethereum Blockchain Crawler
Overview
EthCrawler is a Spring Boot application that interacts with the Ethereum blockchain to retrieve historical balances and transactions for a specified Ethereum address. It leverages the Etherscan and OpenSea APIs to fetch data and stores the results in a MySQL database. The application includes a lightweight React-based frontend for testing and is deployed in a Dockerized environment with two services:

app: Spring Boot backend
db: MySQL database


Features

Fetches transaction history and balances for Ethereum addresses
Integrates with Etherscan and OpenSea APIs
Stores data in a MySQL database with persistent storage
Provides a simple React-based UI for testing
Runs in a fully Dockerized environment for easy setup and deployment


Prerequisites
To run EthCrawler, ensure you have the following installed:

Docker: Docker and Docker Compose (e.g., Docker Desktop for Windows/Mac or Docker Engine for Linux)
Java 21: Required to build the Spring Boot backend (handled within Docker)
Git: To clone the repository
Internet Access: Needed to download Docker images and access external APIs


Setup Instructions
1. Clone the Repository
Clone the EthCrawler repository to your local machine and navigate to the project directory:
git clone <repository-url>
cd ethcrawler

Replace <repository-url> with the GitHub repository URL.
2. Verify Key Files
Ensure the following files are present in the project root:

docker-compose.yml: Defines the app and db services
Dockerfile: Builds the Spring Boot application
pom.xml: Contains Maven dependencies for the backend
src/main/resources/application.properties: Configuration for the Spring Boot app
src/main/resources/static/index.html: React-based frontend for testing

3. Environment Variables
The docker-compose.yml file includes the following default environment variables:

ETHERSCAN_API_KEY: IY6AIIRJPF7XX1B8NK9ZNS48JND3UUQF51
OPENSEA_API_KEY: 279cf63c382a4c589a5b2e7101362f41
MYSQL_ROOT_PASSWORD: 123

Note: For simplicity, API keys are hardcoded in docker-compose.yml. To use your own API keys, update the docker-compose.yml file with your credentials.

Running the Application
1. Start the Containers
From the project root, run the following command to build and start the services:
docker-compose up --build

This will start:

app: Spring Boot backend running on port 8001
db: MySQL database with a persistent volume

2. Monitor Logs
To verify that the services are running correctly, check the logs:
docker-compose logs -f

Look for the following indicators:

db-1: Logs containing "ready for connections"
app-1: Logs containing "Started EthCrawlerApplication in X.XXX seconds"

3. Access the Frontend
Once the services are running, open a web browser and navigate to:
http://localhost:8001/

Use the React-based UI to:

Fetch transaction history for an Ethereum address
Retrieve current and historical balances


Stopping the Application
To stop the running containers, press Ctrl+C in the terminal where docker-compose up is running, or execute:
docker-compose down

This will stop and remove the containers while preserving the MySQL database volume for persistence.

Notes

Ensure you have a stable internet connection to access the Etherscan and OpenSea APIs.
If you encounter issues, check the Docker logs for error messages or consult the documentation for Etherscan/OpenSea API rate limits.
For production use, consider securing API keys by using environment variable files or a secrets manager instead of hardcoding them.


Contributing
Contributions are welcome! Please submit a pull request or open an issue on the GitHub repository for bugs, feature requests, or improvements.

License
This project is licensed under the MIT License. See the LICENSE file for details.
