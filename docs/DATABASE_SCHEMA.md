# Database Schema - FreelancerUp MVP

## Polyglot Persistence Architecture

Simplified polyglot persistence architecture for MVP with PostgreSQL + MongoDB + Redis.

---

## 📊 Database Overview

```
┌─────────────────────────────────────────────────────────────┐
│              POLYGLOT PERSISTENCE ARCHITECTURE (MVP)         │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│  PostgreSQL          ←     MongoDB          ←    Redis      │
│  (Transactional)     ←     (Documents)        ←   (Cache)    │
│                                                              │
│  💰 Payments         👤 Freelancer Data    📦 Sessions       │
│  💳 Wallets          💼 Projects          🔐 Auth tokens    │
│  📊 Transactions     📝 Bids              ⚡ Real-time      │
│  🔑 Contracts        💬 Messages                            │
│  ⭐ Reviews          ❌ No Invites                          │
│  💬 Conversations    ❌ No Portfolios                      │
│                                                              │
└─────────────────────────────────────────────────────────────┘
```

---

## 🗄️ PostgreSQL Schema (Transactional Data)

### Why PostgreSQL for these entities?
- ✅ ACID transactions for financial operations
- ✅ Referential integrity (foreign keys)
- ✅ Strong data consistency for payments

---

### 1. Users Table

```sql
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    avatar_url VARCHAR(500),
    role VARCHAR(20) NOT NULL CHECK (role IN ('USER', 'CLIENT', 'FREELANCER')),
    is_active BOOLEAN DEFAULT true,
    reputation_score DECIMAL(3,2) DEFAULT 0.00,
    total_projects INTEGER DEFAULT 0,
    total_earnings DECIMAL(15,2) DEFAULT 0.00,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_users_reputation ON users(reputation_score DESC);

CREATE TRIGGER update_users_updated_at BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
```

---

### 2. Clients Table

```sql
CREATE TABLE clients (
    id UUID PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    company_name VARCHAR(255),
    industry VARCHAR(100),
    company_size VARCHAR(50) CHECK (company_size IN ('1-10', '11-50', '51-200', '201-500', '500+')),
    payment_methods JSONB, -- Stored as JSON array: [{"type": "visa", "last4": "1234"}]
    total_spent DECIMAL(15,2) DEFAULT 0.00,
    posted_projects INTEGER DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TRIGGER update_clients_updated_at BEFORE UPDATE ON clients
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
```

---

### 3. Wallets Table

```sql
CREATE TABLE wallets (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    balance DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    escrow_balance DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    total_earned DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    currency VARCHAR(3) DEFAULT 'USD',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id)
);

CREATE INDEX idx_wallets_user_id ON wallets(user_id);

CREATE TRIGGER update_wallets_updated_at BEFORE UPDATE ON wallets
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
```

---

### 4. Transactions Table

```sql
CREATE TYPE transaction_type AS ENUM (
    'CREDIT', 'DEBIT', 'ESCROW_HOLD', 'ESCROW_RELEASE'
);

CREATE TYPE transaction_status AS ENUM (
    'PENDING', 'COMPLETED', 'FAILED'
);

CREATE TABLE transactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    wallet_id UUID NOT NULL REFERENCES wallets(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    type transaction_type NOT NULL,
    status transaction_status NOT NULL DEFAULT 'PENDING',
    amount DECIMAL(15,2) NOT NULL,
    description TEXT,
    reference_id VARCHAR(255), -- project_id
    balance_before DECIMAL(15,2) NOT NULL,
    balance_after DECIMAL(15,2) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_transactions_wallet_id ON transactions(wallet_id);
CREATE INDEX idx_transactions_user_id ON transactions(user_id);
CREATE INDEX idx_transactions_type ON transactions(type);
CREATE INDEX idx_transactions_created_at ON transactions(created_at DESC);
```

---

### 5. Payments Table

```sql
CREATE TYPE payment_status AS ENUM (
    'PENDING', 'ESCROW_HOLD', 'RELEASED', 'COMPLETED', 'REFUNDED', 'FAILED'
);

CREATE TABLE payments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id VARCHAR(255) NOT NULL, -- Reference to MongoDB Project
    from_user_id UUID NOT NULL REFERENCES users(id), -- Client
    to_user_id UUID NOT NULL REFERENCES users(id), -- Freelancer
    type payment_type NOT NULL,
    status payment_status NOT NULL DEFAULT 'PENDING',
    amount DECIMAL(15,2) NOT NULL,
    fee DECIMAL(15,2) DEFAULT 0.00, -- Platform fee
    net_amount DECIMAL(15,2) NOT NULL, -- amount - fee
    method payment_method NOT NULL,
    is_escrow BOOLEAN DEFAULT true,
    escrow_funded_at TIMESTAMP WITH TIME ZONE,
    escrow_released_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_payments_project_id ON payments(project_id);
CREATE INDEX idx_payments_from_user_id ON payments(from_user_id);
CREATE INDEX idx_payments_to_user_id ON payments(to_user_id);
CREATE INDEX idx_payments_status ON payments(status);
```

---

### 6. Contracts Table

```sql
CREATE TYPE contract_status AS ENUM (
    'ACTIVE', 'COMPLETED'
);

CREATE TABLE contracts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id VARCHAR(255) UNIQUE NOT NULL, -- Reference to MongoDB Project
    client_id UUID NOT NULL REFERENCES users(id),
    freelancer_id UUID NOT NULL REFERENCES users(id),
    status contract_status NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_contracts_project_id ON contracts(project_id);
CREATE INDEX idx_contracts_client_id ON contracts(client_id);
CREATE INDEX idx_contracts_freelancer_id ON contracts(freelancer_id);
CREATE INDEX idx_contracts_status ON contracts(status);

CREATE TRIGGER update_contracts_updated_at BEFORE UPDATE ON contracts
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
```

---

### 7. Reviews Table

```sql
CREATE TABLE reviews (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id VARCHAR(255) NOT NULL, -- Reference to MongoDB Project
    from_user_id UUID NOT NULL REFERENCES users(id),
    to_user_id UUID NOT NULL REFERENCES users(id),
    rating INTEGER NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comment TEXT,
    communication_rating review_category CHECK (communication_rating BETWEEN 1 AND 5),
    quality_rating review_category CHECK (quality_rating BETWEEN 1 AND 5),
    timeline_rating review_category CHECK (timeline_rating BETWEEN 1 AND 5),
    professionalism_rating review_category CHECK (professionalism_rating BETWEEN 1 AND 5),
    responsiveness_rating review_category CHECK (responsiveness_rating BETWEEN 1 AND 5),
    is_visible BOOLEAN DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(from_user_id, to_user_id, project_id)
);

CREATE INDEX idx_reviews_project_id ON reviews(project_id);
CREATE INDEX idx_reviews_from_user_id ON reviews(from_user_id);
CREATE INDEX idx_reviews_to_user_id ON reviews(to_user_id);
CREATE INDEX idx_reviews_rating ON reviews(rating);
CREATE INDEX idx_reviews_created_at ON reviews(created_at DESC);

CREATE TRIGGER update_reviews_updated_at BEFORE UPDATE ON reviews
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
```

---

### 8. Conversations Table

```sql
CREATE TABLE conversations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id VARCHAR(255) UNIQUE NOT NULL, -- Reference to MongoDB Project
    participant_ids JSONB NOT NULL, -- Array of user UUIDs
    last_message_at TIMESTAMP WITH TIME ZONE,
    last_message_preview TEXT,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_conversations_project_id ON conversations(project_id);
CREATE INDEX idx_conversations_is_active ON conversations(is_active);

CREATE TRIGGER update_conversations_updated_at BEFORE UPDATE ON conversations
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
```

---

## 📁 MongoDB Schema (Document Data)

### Why MongoDB for these entities?
- ✅ Flexible schema for varying project structures
- ✅ Fast document reads for profile data
- ✅ Natural fit for nested data (skills, experience)

---

### 1. Freelancers Collection

```javascript
{
  _id: ObjectId,
  userId: "uuid-from-postgres", // Reference to PostgreSQL users.id
  bio: String,
  hourlyRate: Number,
  availability: "AVAILABLE" | "BUSY" | "OFFLINE",
  totalEarned: Number,
  completedProjects: Number,
  successRate: Number,
  skills: [{
    skillId: String,
    name: String,
    proficiencyLevel: "BEGINNER" | "INTERMEDIATE" | "ADVANCED" | "EXPERT",
    yearsOfExperience: Number
  }],
  createdAt: ISODate,
  updatedAt: ISODate
}

// Indexes
db.freelancers.createIndex({ userId: 1 }, { unique: true })
db.freelancers.createIndex({ "skills.name": 1 })
db.freelancers.createIndex({ availability: 1 })
db.freelancers.createIndex({ hourlyRate: 1 })
db.freelancers.createIndex({ successRate: -1 })
```

---

### 2. Experiences Collection

```javascript
{
  _id: ObjectId,
  freelancerId: "uuid-from-postgres",
  title: String,
  company: String,
  location: String,
  startDate: ISODate,
  endDate: ISODate,
  isCurrentJob: Boolean,
  description: String,
  skills: [String],
  createdAt: ISODate,
  updatedAt: ISODate
}

// Indexes
db.experiences.createIndex({ freelancerId: 1 })
db.experiences.createIndex({ startDate: -1 })
```

---

### 3. Education Collection

```javascript
{
  _id: ObjectId,
  freelancerId: "uuid-from-postgres",
  school: String,
  degree: String,
  fieldOfStudy: String,
  startDate: ISODate,
  endDate: ISODate,
  description: String,
  createdAt: ISODate,
  updatedAt: ISODate
}

// Indexes
db.education.createIndex({ freelancerId: 1 })
db.education.createIndex({ endDate: -1 })
```

---

### 4. Projects Collection

```javascript
{
  _id: ObjectId,
  clientId: "uuid-from-postgres", // Reference to PostgreSQL users.id
  freelancerId: "uuid-from-postgres", // Reference to PostgreSQL users.id (nullable when posting)
  title: String,
  description: String,
  requirements: String,
  skills: [String],
  budget: {
    minAmount: Number,
    maxAmount: Number,
    currency: "USD",
    isNegotiable: Boolean
  },
  duration: Number, // in days
  status: "OPEN" | "IN_PROGRESS" | "COMPLETED",
  type: "FIXED_PRICE" | "HOURLY",
  deadline: ISODate,
  startedAt: ISODate,
  completedAt: ISODate,
  contractId: "uuid-from-postgres", // Reference to PostgreSQL contracts.id
  createdAt: ISODate,
  updatedAt: ISODate
}

// Indexes
db.projects.createIndex({ clientId: 1 })
db.projects.createIndex({ freelancerId: 1 })
db.projects.createIndex({ status: 1 })
db.projects.createIndex({ type: 1 })
db.projects.createIndex({ skills: 1 })
db.projects.createIndex({ "budget.minAmount": 1, "budget.maxAmount": 1 })
db.projects.createIndex({ createdAt: -1 })
db.projects.createIndex({ deadline: 1 })
```

---

### 5. Bids Collection

```javascript
{
  _id: ObjectId,
  projectId: ObjectId, // Reference to projects._id
  freelancerId: "uuid-from-postgres", // Reference to PostgreSQL users.id
  proposal: String,
  price: Number,
  estimatedDuration: Number, // in days
  status: "PENDING" | "ACCEPTED" | "REJECTED",
  submittedAt: ISODate,
  respondedAt: ISODate,
  createdAt: ISODate,
  updatedAt: ISODate
}

// Indexes
db.bids.createIndex({ projectId: 1 })
db.bids.createIndex({ freelancerId: 1 })
db.bids.createIndex({ status: 1 })
db.bids.createIndex({ submittedAt: -1 })
db.bids.createIndex({ projectId: 1, freelancerId: 1 }, { unique: true })
```

---

### 6. Messages Collection

```javascript
{
  _id: ObjectId,
  conversationId: "uuid-from-postgres", // Reference to PostgreSQL conversations.id
  projectId: "objectid-from-mongodb", // Reference to projects._id
  fromUserId: "uuid-from-postgres",
  toUserId: "uuid-from-postgres",
  content: String,
  isRead: Boolean,
  readAt: ISODate,
  createdAt: ISODate
}

// Indexes
db.messages.createIndex({ conversationId: 1, createdAt: -1 })
db.messages.createIndex({ fromUserId: 1 })
db.messages.createIndex({ toUserId: 1 })
db.messages.createIndex({ isRead: 1 })
db.messages.createIndex({ createdAt: -1 })
```

---

## 🔴 Redis Data Structures (MVP)

### 1. User Sessions

```
Key: session:{refreshToken}
Value: {
  userId: "uuid",
  email: "user@example.com",
  role: "FREELANCER",
  createdAt: "timestamp"
}
TTL: 7 days (604800 seconds)
```

---

### 2. User Profile Cache

```
Key: profile:{userId}
Value: {JSON serialized user profile}
TTL: 1 hour (3600 seconds)
```

---

### 3. Project Listings Cache

```
Key: projects:list:{filters_hash}
Value: [Array of project IDs]
TTL: 15 minutes (900 seconds)
```

---

### 4. Online Users

```
Key: online:users
Type: Set
Members: [userId1, userId2, ...]
```

---

## 🔗 Cross-Database Relationships

### Application-Level Joins

```java
// Example: Get project with client and freelancer info
@Service
public class ProjectService {

    @Autowired
    private MongoProjectRepository mongoProjectRepository;

    @Autowired
    private PostgresUserRepository postgresUserRepository;

    public ProjectDetailDTO getProjectDetail(String projectId) {
        // 1. Get project from MongoDB
        Project project = mongoProjectRepository.findById(projectId)
            .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        // 2. Get client from PostgreSQL
        User client = postgresUserRepository.findById(project.getClientId())
            .orElseThrow(() -> new ResourceNotFoundException("Client not found"));

        // 3. Get freelancer (if assigned)
        User freelancer = null;
        if (project.getFreelancerId() != null) {
            freelancer = postgresUserRepository.findById(project.getFreelancerId())
                .orElse(null);
        }

        // 4. Combine into DTO
        return ProjectDetailDTO.builder()
            .project(project)
            .client(client)
            .freelancer(freelancer)
            .build();
    }
}
```

---

## 🚀 MVP Scope Summary

### PostgreSQL Tables (8 tables)
- ✅ `users` - Core user data
- ✅ `clients` - Client specific data
- ✅ `wallets` - User wallet balances
- ✅ `transactions` - Financial transactions
- ✅ `payments` - Project payments with escrow
- ✅ `contracts` - Project contracts (simplified)
- ✅ `reviews` - User reviews (1-5 rating + comment)
- ✅ `conversations` - Message conversation threads

**Removed from MVP:**
- ❌ `milestones` - Too complex for MVP
- ❌ `withdrawals` - No withdrawal system in MVP
- ❌ `disputes` - No dispute resolution in MVP

### MongoDB Collections (6 collections)
- ✅ `freelancers` - Extended freelancer profiles
- ✅ `experiences` - Work experience history
- ✅ `education` - Educational background
- ✅ `projects` - Project details
- ✅ `bids` - Project bids
- ✅ `messages` - Chat messages

**Removed from MVP:**
- ❌ `portfolio_items` - No portfolio upload
- ❌ `invites` - No invite system
- ❌ `notifications` - No notification system

### Redis Patterns (4 patterns)
- ✅ User sessions
- ✅ Profile cache
- ✅ Project listings cache
- ✅ Online users tracking

**Removed from MVP:**
- ❌ Rate limiting (can add later)
- ❌ Notification queues
- ❌ Counters

---

## 📚 Additional Resources

- [PostgreSQL Documentation](https://www.postgresql.org/docs/)
- [MongoDB Documentation](https://docs.mongodb.com/)
- [Redis Documentation](https://redis.io/documentation)
- [Spring Data JPA](https://spring.io/projects/spring-data-jpa)
- [Spring Data MongoDB](https://docs.spring.io/spring-data/mongodb/docs/current/reference/html/)
- [Spring Data Redis](https://docs.spring.io/spring-data/redis/docs/current/reference/html/)
