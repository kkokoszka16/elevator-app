# Elevator System Simulation

A real-time elevator management system implementing the LOOK scheduling algorithm. Built with hexagonal architecture principles.

## Technology Stack

| Component | Technology |
|-----------|------------|
| Backend | Java 21, Spring Boot 3.2, Maven |
| Frontend | React 18, TypeScript, Vite |
| Real-time | WebSocket (STOMP over SockJS) |
| Architecture | Hexagonal (Ports & Adapters) |
| Containerization | Docker, docker-compose |

## Quick Start

```bash
docker compose up --build
```

Access points:
- Frontend: http://localhost:3000
- Backend API: http://localhost:8080/api/v1
- Swagger UI: http://localhost:8080/swagger-ui.html
- Health Check: http://localhost:8080/actuator/health
- Prometheus Metrics: http://localhost:8080/actuator/prometheus

## Architecture

```
elevator-system/
├── elevator-domain/          # Pure domain logic (framework-free)
│   ├── model/               # Elevator, Floor, Direction, DoorState
│   ├── service/             # LookSchedulingStrategy
│   └── exception/           # Domain exceptions
├── elevator-application/     # Use cases and ports
│   ├── port/in/             # Input ports (use case interfaces)
│   ├── port/out/            # Output ports (repository interfaces)
│   └── usecase/             # Use case implementations
├── elevator-infrastructure/  # Framework adapters
│   ├── adapter/web/         # REST controllers, WebSocket handlers
│   ├── adapter/persistence/ # In-memory repositories
│   ├── adapter/event/       # WebSocket event publisher
│   └── config/              # Spring configuration
└── elevator-bootstrap/       # Application entry point
```

### Dependency Direction

```
bootstrap -> infrastructure -> application -> domain
```

Domain and application layers have zero framework dependencies.

## API Reference

### REST Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | /api/v1/elevators | Get all elevators status |
| GET | /api/v1/elevators/{id} | Get single elevator status |
| POST | /api/v1/elevators/call | Call elevator to floor |
| POST | /api/v1/elevators/{id}/select | Select floor from inside |
| GET | /api/v1/system/config | Get building configuration |
| PUT | /api/v1/system/config | Update configuration |
| POST | /api/v1/system/reset | Reset system |
| POST | /api/v1/system/simulation/start | Start simulation |
| POST | /api/v1/system/simulation/stop | Stop simulation |
| GET | /api/v1/system/simulation/status | Get simulation status |

### WebSocket Topics

| Destination | Direction | Description |
|-------------|-----------|-------------|
| /topic/elevators | Subscribe | Real-time elevator state updates |
| /topic/events | Subscribe | Domain events stream |
| /app/call | Send | Call elevator request |
| /app/select/{id} | Send | Floor selection request |

## API Examples

### Health Check

```bash
curl -s http://localhost:8080/actuator/health | jq
```

Response:
```json
{
  "status": "UP",
  "components": {
    "diskSpace": {"status": "UP"},
    "elevatorSystem": {
      "status": "UP",
      "details": {
        "numberOfFloors": 10,
        "numberOfElevators": 3,
        "activeElevators": 3,
        "simulationRunning": true
      }
    },
    "livenessState": {"status": "UP"},
    "ping": {"status": "UP"},
    "readinessState": {"status": "UP"}
  }
}
```

### Get All Elevators Status

```bash
curl -s http://localhost:8080/api/v1/elevators | jq
```

Response:
```json
{
  "numberOfFloors": 10,
  "numberOfElevators": 3,
  "elevators": [
    {
      "id": 0,
      "currentFloor": 0,
      "direction": "IDLE",
      "doorState": "CLOSED",
      "state": "IDLE",
      "destinations": []
    },
    {
      "id": 1,
      "currentFloor": 0,
      "direction": "IDLE",
      "doorState": "CLOSED",
      "state": "IDLE",
      "destinations": []
    },
    {
      "id": 2,
      "currentFloor": 0,
      "direction": "IDLE",
      "doorState": "CLOSED",
      "state": "IDLE",
      "destinations": []
    }
  ]
}
```

### Get Single Elevator Status

```bash
curl -s http://localhost:8080/api/v1/elevators/0 | jq
```

Response:
```json
{
  "id": 0,
  "currentFloor": 1,
  "direction": "UP",
  "doorState": "CLOSED",
  "state": "MOVING",
  "destinations": [5]
}
```

### Call Elevator to Floor

```bash
curl -X POST http://localhost:8080/api/v1/elevators/call \
  -H "Content-Type: application/json" \
  -d '{"floor": 5, "direction": "UP"}' \
  -w "\nHTTP Code: %{http_code}\n"
```

Response: HTTP 202 Accepted (no body - asynchronous operation)

```bash
curl -X POST http://localhost:8080/api/v1/elevators/call \
  -H "Content-Type: application/json" \
  -d '{"floor": 3, "direction": "DOWN"}' \
  -w "\nHTTP Code: %{http_code}\n"
```

Response: HTTP 202 Accepted

### Select Floor from Inside Elevator

```bash
curl -X POST http://localhost:8080/api/v1/elevators/0/select \
  -H "Content-Type: application/json" \
  -d '{"floor": 8}' \
  -w "\nHTTP Code: %{http_code}\n"
```

Response: HTTP 202 Accepted (no body - asynchronous operation)

```bash
curl -X POST http://localhost:8080/api/v1/elevators/1/select \
  -H "Content-Type: application/json" \
  -d '{"floor": 7}' \
  -w "\nHTTP Code: %{http_code}\n"
```

Response: HTTP 202 Accepted

### Get Building Configuration

```bash
curl -s http://localhost:8080/api/v1/system/config | jq
```

Response:
```json
{
  "numberOfFloors": 10,
  "numberOfElevators": 3,
  "doorOpenDurationSeconds": 3,
  "floorTravelDurationSeconds": 2
}
```

### Update Building Configuration

```bash
curl -X PUT http://localhost:8080/api/v1/system/config \
  -H "Content-Type: application/json" \
  -d '{
    "numberOfFloors": 15,
    "numberOfElevators": 4,
    "doorOpenDurationSeconds": 3,
    "floorTravelDurationSeconds": 2
  }' | jq
```

Response:
```json
{
  "numberOfFloors": 15,
  "numberOfElevators": 4,
  "doorOpenDurationSeconds": 3,
  "floorTravelDurationSeconds": 2
}
```

### Reset System

```bash
curl -X POST http://localhost:8080/api/v1/system/reset \
  -w "\nHTTP Code: %{http_code}\n"
```

Response: HTTP 204 No Content (no body - operation successful)

### Get Simulation Status

```bash
curl -s http://localhost:8080/api/v1/system/simulation/status | jq
```

Response:
```json
{
  "running": true
}
```

### Start Simulation

```bash
curl -X POST http://localhost:8080/api/v1/system/simulation/start | jq
```

Response:
```json
{
  "running": true
}
```

### Stop Simulation

```bash
curl -X POST http://localhost:8080/api/v1/system/simulation/stop | jq
```

Response:
```json
{
  "running": false
}
```

### Complete Test Scenario

```bash
# 1. Check system is healthy
curl -s http://localhost:8080/actuator/health | jq '.status'

# 2. Get initial elevator state
curl -s http://localhost:8080/api/v1/elevators | jq

# 3. Call elevator to floor 5 going UP
curl -X POST http://localhost:8080/api/v1/elevators/call \
  -H "Content-Type: application/json" \
  -d '{"floor": 5, "direction": "UP"}'

# 4. Wait a moment, then check elevator status
sleep 2
curl -s http://localhost:8080/api/v1/elevators | jq

# 5. Select floor 8 from elevator 0
curl -X POST http://localhost:8080/api/v1/elevators/0/select \
  -H "Content-Type: application/json" \
  -d '{"floor": 8}'

# 6. Check specific elevator
curl -s http://localhost:8080/api/v1/elevators/0 | jq

# 7. Stop simulation
curl -X POST http://localhost:8080/api/v1/system/simulation/stop | jq

# 8. Reset system
curl -X POST http://localhost:8080/api/v1/system/reset

# 9. Start simulation again
curl -X POST http://localhost:8080/api/v1/system/simulation/start | jq
```

### Response Type Notes

- HTTP 202 (Accepted): Request accepted for asynchronous processing, no response body expected
- HTTP 204 (No Content): Operation successful, no response body by design
- All GET endpoints return JSON with data
- Simulation endpoints (start/stop/status) return JSON status

## Elevator Algorithm

The system implements the LOOK algorithm (optimized SCAN):

1. Elevator continues in current direction, servicing requests
2. Changes direction only when no more requests in current direction
3. Picks up passengers going in the same direction (en route)

Selection priority:
1. Closest idle elevator
2. Elevator moving towards request in same direction
3. Least busy elevator (fewest destinations)

## Configuration

Default building configuration:
- 10 floors
- 3 elevators
- 3 second door open duration
- 2 second floor travel duration

Configuration can be modified via the REST API or frontend panel.

## Development

### Prerequisites

- Java 21
- Maven 3.9+
- Node.js 18+ (for frontend development)
- Docker and docker-compose

### Build

Build entire project:
```bash
./mvnw clean package
```

Build without tests:
```bash
./mvnw clean package -DskipTests
```

Build specific module:
```bash
./mvnw clean package -pl elevator-domain
```

### Test

Run all tests:
```bash
./mvnw test
```

Run tests with coverage report:
```bash
./mvnw clean test jacoco:report
```

Coverage report location: `target/site/jacoco/index.html` in each module.

Run tests for specific module:
```bash
./mvnw test -pl elevator-application
```

Run specific test class:
```bash
./mvnw test -Dtest=ElevatorServiceTest
```

Check test coverage threshold (80%):
```bash
./mvnw verify
```

### Run

Production (Docker):
```bash
docker compose up --build
```

Local development (backend only):
```bash
./mvnw spring-boot:run -pl elevator-bootstrap
```

Local development (frontend only):
```bash
cd frontend
npm install
npm run dev
```

Backend runs on port 8080, frontend on port 3000 (dev) or 8080 (Docker).

### Verify Build

Check for security vulnerabilities:
```bash
./mvnw dependency-check:check
```

Verify hexagonal architecture rules:
```bash
./mvnw test -Dtest=HexagonalArchitectureTest
```

Generate dependency tree:
```bash
./mvnw dependency:tree
```

## Production Features

| Feature | Implementation |
|---------|----------------|
| Rate Limiting | Bucket4j, 100 requests/minute per IP |
| Health Checks | Spring Actuator with custom indicators |
| Metrics | Micrometer with Prometheus registry |
| Security | CORS, CSP, X-Frame-Options headers |
| Graceful Shutdown | 30 second timeout for in-flight requests |

## Project Structure

```
.
├── elevator-domain/           # Domain module
├── elevator-application/      # Application module
├── elevator-infrastructure/   # Infrastructure module
├── elevator-bootstrap/        # Bootstrap module
├── frontend/                  # React frontend
├── Dockerfile                 # Backend container
├── docker-compose.yml         # Container orchestration
└── pom.xml                    # Parent POM
```

## Troubleshooting

### Build Issues

Maven wrapper permission denied:
```bash
chmod +x mvnw
```

Out of memory during build:
```bash
export MAVEN_OPTS="-Xmx1024m"
./mvnw clean package
```

### Docker Issues

Containers not starting:
```bash
docker compose down -v
docker compose up --build
```

View container logs:
```bash
docker compose logs -f backend
docker compose logs -f frontend
```

Check container health:
```bash
docker compose ps
```

### Port Conflicts

Port 8080 already in use:
```bash
lsof -i :8080
kill -9 <PID>
```

Or modify ports in docker-compose.yml:
```yaml
ports:
  - "8081:8080"  # Change external port
```
