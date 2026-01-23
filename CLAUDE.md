# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**FreelancerUp** is a freelancer marketplace platform built with Spring Boot 4.0.1 and Java 17. The platform connects clients with freelancers, featuring project posting, bidding, secure escrow payments, real-time messaging, and reputation systems.

### Technology Stack

- **Framework**: Spring Boot 4.0.1
- **Java**: 17
- **Build Tool**: Maven (with wrapper)
- **Databases**: Polyglot persistence architecture
  - PostgreSQL (transactional data: users, payments, wallets, reviews)
  - MongoDB (document data: projects, bids, messages, freelancer profiles)
  - Redis (caching: sessions, profiles, listings)

## Common Development Commands

### Environment Configuration

**CRITICAL**: Always follow `/docs/.clauderc` protocol for ALL code changes:

**Risk Classification:**

- **Type A** (Safe): New files, comments, tests, isolated config → Apply immediately
- **Type B** (Risky): Logic changes, refactoring → Show diff, await approval
- **Type C** (Dangerous): Breaking changes, schema mods → Full impact analysis

**Environment Variables (.env):**
All database credentials use single URI format:

```bash
# PostgreSQL (Supabase - JDBC uses query parameters)
SPRING_DATASOURCE_URL=jdbc:postgresql://host:5432/dbname?user=user&password=pass

# MongoDB (MongoDB Atlas - uses user:password@host format)
SPRING_DATA_MONGODB_URI=mongodb+srv://user:password@host/dbname

# Redis (Upstash - uses user:password@host format)
SPRING_DATA_REDIS_URI=rediss://default:password@host:6379
```

**Security Rules (MANDATORY):**

- ✓ NEVER commit actual credentials to git
- ✓ `.env` file must be in `.gitignore`
- ✓ Use placeholder `${VARIABLE_NAME}` in `application.yaml`
- ✓ `DotEnvConfig.java` loads `.env` before Spring context
- ✗ NEVER hardcode passwords, tokens, or API keys

**Configuration Loading Order:**

1. `DotEnvConfig.java` static block loads `.env`
2. Sets `System.setProperty()` for each variable
3. Spring Boot reads from system properties
4. `application.yaml` references via `${VARIABLE_NAME}`

### Building and Running

```bash
# Clean build
./mvnw clean install

# Run application (development)
./mvnw spring-boot:run

# Skip tests during build
./mvnw clean install -DskipTests

# Package for deployment
./mvnw clean package
```

### Testing

```bash
# Run all tests
./mvnw test

# Run specific test class
./mvnw test -Dtest=ClientServiceTest

# Run specific test method
./mvnw test -Dtest=ProjectServiceTest#testCreateProject

# Run tests by pattern
./mvnw test -Dtest=*ServiceTest    # All service tests
./mvnw test -Dtest=*ControllerTest # All controller tests
./mvnw test -Dtest=*IntegrationTest # All integration tests
```

### Docker

```bash
# Note: No docker-compose.yml exists in current setup
# Use external database services (Supabase, MongoDB Atlas, Upstash)
# or add docker-compose.yml manually if needed

# Initialize PostgreSQL database (if using local Docker)
docker exec -it <container_name> psql -U postgres -d freelancerup \
    -f /docker-entrypoint-initdb.d/init.sql
```

## Architecture

### Polyglot Persistence

The application uses three databases for different data types:

**PostgreSQL** - Transactional/Relational Data (ACID compliance required)

- `users`, `clients`, `wallets`, `transactions`
- `payments`, `contracts`, `reviews`, `conversations`
- All financial operations must use `@Transactional` annotation

**MongoDB** - Flexible/Document Data

- `projects`, `bids`, `messages`
- `freelancers`, `experiences`, `education`
- Fast document reads, nested data structures

**Redis** - Caching Layer

- User sessions (TTL: 7 days)
- User profiles (TTL: 1 hour)
- Project listings (TTL: 15 minutes)
- Online users tracking

### Cross-Database Operations

When data spans multiple databases:

1. Query MongoDB first (faster for documents)
2. Query PostgreSQL for related entities
3. Combine into DTO for response
4. Use application-level joins (no cross-database foreign keys)

Example from docs/PLANE.md:

```java
public ProjectDetailDTO getProjectDetail(String projectId) {
    // 1. Get project from MongoDB
    Project project = mongoProjectRepository.findById(projectId)
        .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

    // 2. Get client from PostgreSQL
    User client = postgresUserRepository.findById(UUID.fromString(project.getClientId()))
        .orElseThrow(() -> new ResourceNotFoundException("Client not found"));

    // 3. Combine into DTO
    return ProjectDetailDTO.builder()
        .project(project)
        .client(client)
        .build();
}
```

## Package Structure

The codebase follows a feature-based package structure under `com.FreelancerUp`:

```
com.FreelancerUp/
├── model/
│   ├── entity/          # PostgreSQL entities (@Entity)
│   ├── document/        # MongoDB documents (@Document)
│   └── enums/           # Enumerations (CompanySize, ProjectStatus, etc.)
├── feature/
│   ├── auth/            # Authentication & JWT
│   ├── bid/             # Bidding system
│   ├── chat/            # Messaging & conversations
│   ├── client/          # Client management
│   ├── common/          # Shared utilities
│   ├── contract/        # Contract management
│   ├── freelancer/      # Freelancer profiles & skills
│   ├── payment/         # Wallets, payments, escrow
│   ├── project/         # Project CRUD & search
│   ├── review/          # Reviews & reputation
│   └── user/            # User management
├── config/              # Configuration classes (DotEnvConfig, Security, etc.)
├── cache/               # Redis caching service
├── validation/          # Custom validators
├── exception/           # Global exception handlers
└── FreelancerUpApplication.java  # Main Spring Boot application
```

Each feature module contains:

- `controller/` - REST endpoints
- `service/` - Business logic interfaces
- `service/impl/` - Service implementations
- `repository/` - Data access (Spring Data JPA/MongoDB)
- `dto/request/` - Request DTOs
- `dto/response/` - Response DTOs

## Critical Implementation Notes

### Payment System (Phase 7)

The payment/escrow system is the most critical component:

**All payment operations MUST:**

1. Use `@Transactional` annotation
2. Perform atomic wallet updates
3. Follow double-entry accounting (credit = debit)
4. Validate escrow state transitions
5. Have thorough test coverage

**Escrow Flow:**

```
Fund Escrow: Client Wallet → Escrow → ESCROW_HOLD transaction
Release Payment: Escrow → Freelancer Wallet → ESCROW_RELEASE transaction
Refund: Escrow → Client Wallet → REFUNDED status
```

See docs/PLANE.md Phase 7 for detailed implementation.

### Cache Strategy

Use Cache-Aside pattern:

1. Try Redis cache first
2. Query database on cache miss
3. Populate cache with result
4. Invalidate cache on updates

Cache keys:

- `session:{refreshToken}` - User sessions
- `profile:{userId}` - User profiles
- `projects:list:{filters_hash}` - Project search results
- `online:users` - Set of online user IDs

### Transaction Management

```java
// CRITICAL for all PostgreSQL operations
@Service
public class PaymentService {
    @Transactional
    public void fundEscrow(PaymentDTO dto) {
        // All operations in single transaction
        // If any fails, entire transaction rolls back
    }
}
```

## Implementation Status

**Current Phase**: Backend Module Implementation (Entities, Repositories, Services, Controllers)

### Completed Modules

- ✅ **Entities & Documents**: PostgreSQL entities and MongoDB documents defined
- ✅ **Repositories**: PostgreSQL and MongoDB repository interfaces
- ✅ **Client Module**: Registration, profile management, statistics
- ✅ **Project Module**: CRUD operations, search, status management
- ✅ **Bid Module**: Bid submission, acceptance, rejection, withdrawal

### Partially Implemented

- 🚧 **Cache Service**: [RedisCacheService.java](src/main/java/com/FreelancerUp/cache/RedisCacheService.java) stub exists (Phase 11)
- 🚧 **Security**: JWT configuration ready, auth filters pending

### Pending Modules

- ❌ **Payment/Escrow**: Critical system - requires `@Transactional` operations
- ❌ **Contract**: Auto-contract creation on bid acceptance
- ❌ **Chat**: Real-time messaging
- ❌ **Review**: Reputation system

See [docs/IMPLEMENTATION_SUMMARY.md](docs/IMPLEMENTATION_SUMMARY.md) for detailed progress.

## Code Change Protocol

Per [docs/.clauderc](docs/.clauderc), follow risk classification for ALL changes:

**Type A (Safe)**: New files, comments, tests, isolated config → Apply immediately
**Type B (Risky)**: Logic changes, refactoring → Show diff, await approval
**Type C (Dangerous)**: Breaking changes, schema mods → Full impact analysis

## API Documentation

Access Swagger UI at: `http://localhost:8080/swagger-ui/index.html`

See [docs/API_DOCUMENTATION.md](docs/API_DOCUMENTATION.md) for complete API reference including:

- Authentication (JWT Bearer tokens)
- Request/response formats
- Pagination parameters
- Rate limiting
- Example requests

**Endpoint conventions:**

- `/api/v1/{resource}` - Standard CRUD endpoints
- `/api/v1/{resource}/search` - Search/filter endpoints
- Use `@PreAuthorize` for role-based access control

**Common roles:**

- `USER` - Basic registered user
- `CLIENT` - Client role (can post projects)
- `FREELANCER` - Freelancer role (can bid on projects)

## Entity Relationships

**User** (base entity in PostgreSQL)

- `Client` extends User (company info, payment methods)
- `Freelancer` document in MongoDB (skills, experience, rates)

**Project** (MongoDB)

- `clientId` → PostgreSQL users.id
- `freelancerId` → PostgreSQL users.id (nullable)
- `contractId` → PostgreSQL contracts.id

**Bid** (MongoDB)

- `projectId` → MongoDB projects.\_id
- `freelancerId` → PostgreSQL users.id

**Payment** (PostgreSQL)

- `projectId` → MongoDB projects.\_id
- `from_user_id` → PostgreSQL users.id (client)
- `to_user_id` → PostgreSQL users.id (freelancer)

## Important Files

**Implementation Planning:**

- `docs/PLANE.md` - Detailed 8-week implementation plan with code examples
- `docs/IMPLEMENTATION_SUMMARY.md` - Current implementation status

**Design Documentation:**

- `docs/DATABASE_SCHEMA.md` - Complete database schema and relationships
- `docs/CLASS.md` - UML class diagrams for all entities
- `docs/USECASE.md` - Use case diagrams
- `docs/SEQUENCE_CLIENT.md` - Client workflow sequences
- `docs/SEQUENCE_FREELANCER.md` - Freelancer workflow sequences
- `docs/API_DOCUMENTATION.md` - Complete API reference

**Configuration:**

- `src/main/resources/application.yaml` - Spring Boot configuration (uses `${VARIABLE_NAME}` placeholders)
- `src/main/java/com/FreelancerUp/config/DotEnvConfig.java` - Loads `.env` before Spring context
- `.env` - Environment variables (NOT in git - see `.env.example`)

**Code Change Protocol:**

- `docs/.clauderc` - Risk classification system for code changes (Type A/B/C)

## Development Workflow

### Full Stack Development

**Starting both backend and frontend:**

```bash
# Terminal 1 - Backend
cd FreelancerUp-BE
./mvnw spring-boot:run

# Terminal 2 - Frontend
cd FreelancerUp-FE
pnpm dev
```

**Backend**: http://localhost:8080
**Frontend**: http://localhost:3000
**Swagger UI**: http://localhost:8080/swagger-ui/index.html

### Common Development Tasks

**Adding a new feature module:**

1. Create package structure under `com.FreelancerUp.feature.{featureName}/`
2. Add entities/documents to `model/`
3. Create repository interface extending `JpaRepository` or `MongoRepository`
4. Create service interface and implementation
5. Create controller with `@RestController` and `@RequestMapping("/api/v1/{featureName}")`
6. Create request/response DTOs
7. Add validation annotations
8. Write unit tests

**Implementing cross-database operations:**

1. Query MongoDB first (for document data)
2. Query PostgreSQL for relational entities
3. Combine results into DTO
4. No cross-database foreign keys - use application-level joins

**Adding caching to a service:**

1. Inject `RedisCacheService`
2. Use cache-aside pattern (try cache → query DB → populate cache)
3. Invalidate cache on updates/deletes
4. Use appropriate TTL (sessions: 7d, profiles: 1h, listings: 15min)

## Common Gotchas

1. **Package name**: Use `com.FreelancerUp` (not `com.dev`)
2. **Environment variables**: Must use `.env` file, not system environment
3. **Cross-database queries**: No foreign keys between PostgreSQL and MongoDB
4. **Payment operations**: Always use `@Transactional` for wallet updates
5. **Maven wrapper**: Use `./mvnw` (not `mvn`) for consistency
6. **Service layer**: Always use interfaces + implementations (service/ + service/impl/)
