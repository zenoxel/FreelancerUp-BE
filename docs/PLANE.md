# Backend Implementation Plan - FreelancerUp MVP

## 📋 Overview

**Project**: FreelancerUp - Freelancer Marketplace Platform
**Architecture**: Polyglot Persistence (PostgreSQL + MongoDB + Redis)
**Timeline**: 8 weeks (2 months)
**Status**: 🚧 In Development
**Last Updated**: 2026-01-20

---

## 🎯 Implementation Phases

### Phase 1: Core Entities (PostgreSQL) ⭐ Priority 1

**Duration**: 3-4 days | **Week**: 1

#### Objective

Create all PostgreSQL entity classes for transactional data.

#### Tasks

##### 1.1 Client Entity

**File**: `src/main/java/com/dev/model/entity/Client.java`

```java
@Entity
@Table(name = "clients")
@Data @Builder @AllArgsConstructor @NoArgsConstructor
public class Client {
    @Id
    private UUID id;

    @Column(nullable = false)
    private String companyName;

    private String industry;

    @Enumerated(EnumType.STRING)
    private CompanySize companySize;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private List<PaymentMethod> paymentMethods;

    private BigDecimal totalSpent;
    private Integer postedProjects;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
```

**Fields**:

- `id` (UUID) - References users.id
- `companyName` (VARCHAR 255)
- `industry` (VARCHAR 100)
- `companySize` (ENUM: 1-10, 11-50, 51-200, 201-500, 500+)
- `paymentMethods` (JSONB)
- `totalSpent` (DECIMAL 15,2)
- `postedProjects` (INTEGER)

---

##### 1.2 Wallet Entity

**File**: `src/main/java/com/dev/model/entity/Wallet.java`

```java
@Entity
@Table(name = "wallets")
@Data @Builder @AllArgsConstructor @NoArgsConstructor
public class Wallet {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal escrowBalance = BigDecimal.ZERO;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal totalEarned = BigDecimal.ZERO;

    private String currency = "USD";

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
```

**Fields**:

- `id` (UUID)
- `userId` (UUID FK → users.id)
- `balance` (DECIMAL 15,2) - Available balance
- `escrowBalance` (DECIMAL 15,2) - Held in escrow
- `totalEarned` (DECIMAL 15,2) - Lifetime earnings
- `currency` (VARCHAR 3) - Default: USD

---

##### 1.3 Transaction Entity

**File**: `src/main/java/com/dev/model/entity/Transaction.java`

```java
@Entity
@Table(name = "transactions")
@Data @Builder @AllArgsConstructor @NoArgsConstructor
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "wallet_id", nullable = false)
    private Wallet wallet;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status = TransactionStatus.PENDING;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    private String description;

    private String referenceId; // project_id

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal balanceBefore;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal balanceAfter;

    @CreationTimestamp
    private LocalDateTime createdAt;

    private LocalDateTime completedAt;
}
```

**Enums**:

- `TransactionType`: CREDIT, DEBIT, ESCROW_HOLD, ESCROW_RELEASE
- `TransactionStatus`: PENDING, COMPLETED, FAILED

---

##### 1.4 Payment Entity

**File**: `src/main/java/com/dev/model/entity/Payment.java`

```java
@Entity
@Table(name = "payments")
@Data @Builder @AllArgsConstructor @NoArgsConstructor
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String projectId; // MongoDB Project ID

    @ManyToOne
    @JoinColumn(name = "from_user_id", nullable = false)
    private User fromUser; // Client

    @ManyToOne
    @JoinColumn(name = "to_user_id", nullable = false)
    private User toUser; // Freelancer

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status = PaymentStatus.PENDING;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(precision = 15, scale = 2)
    private BigDecimal fee = BigDecimal.ZERO;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal netAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod method;

    @Column(nullable = false)
    private Boolean isEscrow = true;

    private LocalDateTime escrowFundedAt;
    private LocalDateTime escrowReleasedAt;

    @CreationTimestamp
    private LocalDateTime createdAt;

    private LocalDateTime completedAt;
}
```

**Enums**:

- `PaymentType`: FINAL, REFUND
- `PaymentStatus`: PENDING, ESCROW_HOLD, RELEASED, COMPLETED, REFUNDED, FAILED
- `PaymentMethod`: CREDIT_CARD, PAYPAL, BANK_TRANSFER, WALLET

---

##### 1.5 Contract Entity

**File**: `src/main/java/com/dev/model/entity/Contract.java`

```java
@Entity
@Table(name = "contracts")
@Data @Builder @AllArgsConstructor @NoArgsConstructor
public class Contract {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String projectId; // MongoDB Project ID

    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false)
    private User client;

    @ManyToOne
    @JoinColumn(name = "freelancer_id", nullable = false)
    private User freelancer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ContractStatus status = ContractStatus.ACTIVE;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
```

**Enums**:

- `ContractStatus`: ACTIVE, COMPLETED

---

##### 1.6 Review Entity

**File**: `src/main/java/com/dev/model/entity/Review.java`

```java
@Entity
@Table(name = "reviews",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"from_user_id", "to_user_id", "project_id"})
    })
@Data @Builder @AllArgsConstructor @NoArgsConstructor
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String projectId; // MongoDB Project ID

    @ManyToOne
    @JoinColumn(name = "from_user_id", nullable = false)
    private User fromUser;

    @ManyToOne
    @JoinColumn(name = "to_user_id", nullable = false)
    private User toUser;

    @Column(nullable = false)
    private Integer rating; // 1-5

    @Column(columnDefinition = "TEXT")
    private String comment;

    // Review Categories (optional for MVP, but good to have)
    private Integer communicationRating;
    private Integer qualityRating;
    private Integer timelineRating;
    private Integer professionalismRating;
    private Integer responsivenessRating;

    @Column(nullable = false)
    private Boolean isVisible = true;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
```

**Validation**:

- `rating`: 1-5 (CHECK constraint)
- Unique combination: (from_user_id, to_user_id, project_id)

---

##### 1.7 Conversation Entity

**File**: `src/main/java/com/dev/model/entity/Conversation.java`

```java
@Entity
@Table(name = "conversations")
@Data @Builder @AllArgsConstructor @NoArgsConstructor
public class Conversation {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String projectId; // MongoDB Project ID

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb", nullable = false)
    private List<UUID> participantIds;

    private LocalDateTime lastMessageAt;

    @Column(columnDefinition = "TEXT")
    private String lastMessagePreview;

    @Column(nullable = false)
    private Boolean isActive = true;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
```

---

#### Deliverables

- ✅ All 7 entity classes created
- ✅ Enums defined (Role, CompanySize, TransactionType, PaymentStatus, etc.)
- ✅ JPA annotations properly configured
- ✅ Lombok annotations for boilerplate reduction

#### Testing

```bash
./mvnw test -Dtest=EntityTest
```

---

### Phase 2: MongoDB Documents ⭐ Priority 1

**Duration**: 2-3 days | **Week**: 1

#### Objective

Create all MongoDB document classes for flexible, document-based data.

#### Tasks

##### 2.1 Project Document (Update)

**File**: `src/main/java/com/dev/model/document/Project.java`

```java
@Document(collection = "projects")
@Data @Builder @AllArgsConstructor @NoArgsConstructor
public class Project {
    @Id
    private String id;

    private String clientId; // UUID from PostgreSQL users.id
    private String freelancerId; // UUID from PostgreSQL users.id (nullable)

    private String title;
    private String description;
    private String requirements;

    private List<String> skills;

    private ProjectBudget budget;

    private Integer duration; // in days

    @Enumerated(EnumType.STRING)
    private ProjectStatus status = ProjectStatus.OPEN;

    @Enumerated(EnumType.STRING)
    private ProjectType type;

    private LocalDateTime deadline;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;

    private String contractId; // UUID from PostgreSQL contracts.id

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}

// Embedded classes
@Data @AllArgsConstructor @NoArgsConstructor
class ProjectBudget {
    private BigDecimal minAmount;
    private BigDecimal maxAmount;
    private String currency = "USD";
    private Boolean isNegotiable = false;
}
```

**Enums**:

- `ProjectStatus`: OPEN, IN_PROGRESS, COMPLETED
- `ProjectType`: FIXED_PRICE, HOURLY

---

##### 2.2 Bid Document

**File**: `src/main/java/com/dev/model/document/Bid.java`

```java
@Document(collection = "bids")
@Data @Builder @AllArgsConstructor @NoArgsConstructor
public class Bid {
    @Id
    private String id;

    private String projectId; // MongoDB ObjectId
    private String freelancerId; // UUID from PostgreSQL users.id

    private String proposal;
    private BigDecimal price;
    private Integer estimatedDuration; // in days

    @Enumerated(EnumType.STRING)
    private BidStatus status = BidStatus.PENDING;

    private LocalDateTime submittedAt;
    private LocalDateTime respondedAt;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
```

**Enums**:

- `BidStatus`: PENDING, ACCEPTED, REJECTED

---

##### 2.3 Message Document

**File**: `src/main/java/com/dev/model/document/Message.java`

```java
@Document(collection = "messages")
@Data @Builder @AllArgsConstructor @NoArgsConstructor
public class Message {
    @Id
    private String id;

    private String conversationId; // UUID from PostgreSQL conversations.id
    private String projectId; // MongoDB ObjectId

    private String fromUserId; // UUID from PostgreSQL
    private String toUserId; // UUID from PostgreSQL

    private String content;

    private Boolean isRead = false;
    private LocalDateTime readAt;

    @CreationTimestamp
    private LocalDateTime createdAt;

    // Pre-insert method to handle readAt
    @PrePersist
    public void prePersist() {
        if (readAt == null && isRead) {
            readAt = LocalDateTime.now();
        }
    }
}
```

---

##### 2.4 Experience Document

**File**: `src/main/java/com/dev/model/document/Experience.java`

```java
@Document(collection = "experiences")
@Data @Builder @AllArgsConstructor @NoArgsConstructor
public class Experience {
    @Id
    private String id;

    private String freelancerId; // UUID from PostgreSQL users.id

    private String title;
    private String company;
    private String location;

    private LocalDateTime startDate;
    private LocalDateTime endDate;

    private Boolean isCurrentJob = false;

    private String description;
    private List<String> skills;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
```

---

##### 2.5 Education Document

**File**: `src/main/java/com/dev/model/document/Education.java`

```java
@Document(collection = "education")
@Data @Builder @AllArgsConstructor @NoArgsConstructor
public class Education {
    @Id
    private String id;

    private String freelancerId; // UUID from PostgreSQL users.id

    private String school;
    private String degree;
    private String fieldOfStudy;

    private LocalDateTime startDate;
    private LocalDateTime endDate;

    private String description;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
```

---

##### 2.6 Freelancer Document (Update)

**File**: `src/main/java/com/dev/model/document/Freelancer.java`

```java
@Document(collection = "freelancers")
@Data @Builder @AllArgsConstructor @NoArgsConstructor
public class Freelancer {
    @Id
    private String id;

    private String userId; // UUID from PostgreSQL users.id

    private String bio;
    private BigDecimal hourlyRate;

    @Enumerated(EnumType.STRING)
    private Availability availability = Availability.AVAILABLE;

    private BigDecimal totalEarned = BigDecimal.ZERO;
    private Integer completedProjects = 0;
    private Double successRate = 0.0;

    private List<FreelancerSkill> skills;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}

// Embedded class
@Data @AllArgsConstructor @NoArgsConstructor
class FreelancerSkill {
    private String skillId;
    private String name;
    private ProficiencyLevel proficiencyLevel;
    private Integer yearsOfExperience;
}

// Enums
enum Availability { AVAILABLE, BUSY, OFFLINE }
enum ProficiencyLevel { BEGINNER, INTERMEDIATE, ADVANCED, EXPERT }
```

---

#### Deliverables

- ✅ All 6 document classes created
- ✅ MongoDB annotations properly configured
- ✅ Embedded classes for complex data structures
- ✅ Index definitions (via @Indexed annotation or repository methods)

#### Testing

```bash
./mvnw test -Dtest=DocumentTest
```

---

### Phase 3: Repository Layer ⭐ Priority 1

**Duration**: 2-3 days | **Week**: 2

#### Objective

Create repository interfaces for both PostgreSQL and MongoDB.

#### Tasks

##### 3.1 PostgreSQL Repositories

###### ClientRepository

**File**: `src/main/java/com/dev/feature/client/repository/ClientRepository.java`

```java
@Repository
public interface ClientRepository extends JpaRepository<Client, UUID> {

    Optional<Client> findByUserId(UUID userId);

    @Query("SELECT c FROM Client c WHERE c.companyName LIKE %:keyword%")
    List<Client> searchByCompanyName(@Param("keyword") String keyword);

    @Query("SELECT c FROM Client c ORDER BY c.totalSpent DESC")
    List<Client> findTopSpenders(Pageable pageable);
}
```

---

###### WalletRepository

**File**: `src/main/java/com/dev/feature/payment/repository/WalletRepository.java`

```java
@Repository
public interface WalletRepository extends JpaRepository<Wallet, UUID> {

    Optional<Wallet> findByUserId(UUID userId);

    @Query("SELECT w FROM Wallet w WHERE w.balance > :minAmount")
    List<Wallet> findWalletsWithMinBalance(@Param("minAmount") BigDecimal minAmount);

    @Modifying
    @Query("UPDATE Wallet w SET w.balance = w.balance + :amount WHERE w.id = :id")
    void incrementBalance(@Param("id") UUID id, @Param("amount") BigDecimal amount);

    @Modifying
    @Query("UPDATE Wallet w SET w.escrowBalance = w.escrowBalance + :amount WHERE w.id = :id")
    void incrementEscrowBalance(@Param("id") UUID id, @Param("amount") BigDecimal amount);
}
```

---

###### TransactionRepository

**File**: `src/main/java/com/dev/feature/payment/repository/TransactionRepository.java`

```java
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    List<Transaction> findByWalletIdOrderByCreatedAtDesc(UUID walletId, Pageable pageable);

    List<Transaction> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    List<Transaction> findByUserIdAndTypeAndCreatedAtBetween(
        UUID userId,
        TransactionType type,
        LocalDateTime startDate,
        LocalDateTime endDate
    );

    @Query("SELECT t FROM Transaction t WHERE t.user.id = :userId " +
           "AND t.type IN :types AND t.createdAt BETWEEN :start AND :end")
    List<Transaction> findByUserAndMultipleTypes(
        @Param("userId") UUID userId,
        @Param("types") List<TransactionType> types,
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end
    );
}
```

---

###### PaymentRepository

**File**: `src/main/java/com/dev/feature/payment/repository/PaymentRepository.java`

```java
@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    List<Payment> findByProjectId(String projectId);

    List<Payment> findByFromUserId(UUID fromUserId);

    List<Payment> findByToUserId(UUID toUserId);

    List<Payment> findByStatus(PaymentStatus status);

    @Query("SELECT p FROM Payment p WHERE p.projectId = :projectId " +
           "AND p.status IN :statuses")
    List<Payment> findByProjectIdAndStatuses(
        @Param("projectId") String projectId,
        @Param("statuses") List<PaymentStatus> statuses
    );

    List<Payment> findByFromUserIdOrToUserIdAndStatus(
        UUID userId, PaymentStatus status
    );
}
```

---

###### ContractRepository

**File**: `src/main/java/com/dev/feature/contract/repository/ContractRepository.java`

```java
@Repository
public interface ContractRepository extends JpaRepository<Contract, UUID> {

    Optional<Contract> findByProjectId(String projectId);

    List<Contract> findByClientId(UUID clientId);

    List<Contract> findByFreelancerId(UUID freelancerId);

    List<Contract> findByStatus(ContractStatus status);

    @Query("SELECT c FROM Contract c WHERE c.client.id = :userId " +
           "OR c.freelancer.id = :userId")
    List<Contract> findAllByUser(@Param("userId") UUID userId);
}
```

---

###### ReviewRepository

**File**: `src/main/java/com/dev/feature/review/repository/ReviewRepository.java`

```java
@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID> {

    List<Review> findByProjectId(String projectId);

    List<Review> findByFromUserId(UUID fromUserId);

    List<Review> findByToUserId(UUID toUserId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.toUser.id = :userId")
    Double getAverageRatingForUser(@Param("userId") UUID userId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.toUser.id = :userId")
    Long getTotalReviewsForUser(@Param("userId") UUID userId);

    List<Review> findByToUserIdAndIsVisibleTrue(UUID toUserId, Pageable pageable);
}
```

---

###### ConversationRepository

**File**: `src/main/java/com/dev/feature/chat/repository/ConversationRepository.java`

```java
@Repository
public interface ConversationRepository extends JpaRepository<Conversation, UUID> {

    Optional<Conversation> findByProjectId(String projectId);

    @Query("SELECT c FROM Conversation c WHERE :userId = ANY(c.participantIds) " +
           "AND c.isActive = true ORDER BY c.lastMessageAt DESC")
    List<Conversation> findActiveConversationsForUser(@Param("userId") UUID userId);

    @Query("SELECT c FROM Conversation c WHERE :userId = ANY(c.participantIds) " +
           "AND c.isActive = true AND c.lastMessageAt < :since")
    List<Conversation> findUnreadConversations(
        @Param("userId") UUID userId,
        @Param("since") LocalDateTime since
    );
}
```

---

##### 3.2 MongoDB Repositories

###### ProjectRepository

**File**: `src/main/java/com/dev/feature/project/repository/ProjectRepository.java`

```java
@Repository
public interface ProjectRepository extends MongoRepository<Project, String> {

    List<Project> findByClientId(String clientId);

    List<Project> findByFreelancerId(String freelancerId);

    List<Project> findByStatus(ProjectStatus status);

    List<Project> findByType(ProjectType type);

    // Search by skills (at least one match)
    List<Project> findBySkillsIn(List<String> skills);

    // Search by budget range
    @Query("{'budget.minAmount': {$gte: ?0}, 'budget.maxAmount': {$lte: ?1}}")
    List<Project> findByBudgetRange(BigDecimal minAmount, BigDecimal maxAmount);

    // Complex search with filters
    @Query("{$and: [" +
           "{$or: [{'status': ?0}, {'status': ?1}]}, " +
           "{'budget.maxAmount': {$lte: ?2}}, " +
           "{'skills': {$in: ?3}}" +
           "]}")
    List<Project> searchProjects(
        ProjectStatus status1,
        ProjectStatus status2,
        BigDecimal maxBudget,
        List<String> skills
    );

    // Full-text search on title/description
    @Query("{$or: [" +
           "{'title': {$regex: ?0, $options: 'i'}}, " +
           "{'description': {$regex: ?0, $options: 'i'}}" +
           "]}")
    List<Project> searchByKeyword(String keyword);

    // Find expiring projects
    List<Project> findByDeadlineBeforeAndStatus(
        LocalDateTime deadline,
        ProjectStatus status
    );
}
```

---

###### BidRepository

**File**: `src/main/java/com/dev/feature/bid/repository/BidRepository.java`

```java
@Repository
public interface BidRepository extends MongoRepository<Bid, String> {

    List<Bid> findByProjectId(String projectId);

    List<Bid> findByFreelancerId(String freelancerId);

    Optional<Bid> findByProjectIdAndFreelancerId(String projectId, String freelancerId);

    List<Bid> findByStatus(BidStatus status);

    @Query("{'projectId': ?0, $or: [{'status': 'PENDING'}, {'status': 'ACCEPTED'}]}")
    List<Bid> findActiveBidsForProject(String projectId);

    @Query("{'freelancerId': ?0, $or: [{'status': 'PENDING'}, {'status': 'ACCEPTED'}]}")
    List<Bid> findActiveBidsForFreelancer(String freelancerId);

    Long countByProjectIdAndStatus(String projectId, BidStatus status);
}
```

---

###### MessageRepository

**File**: `src/main/java/com/dev/feature/chat/repository/MessageRepository.java`

```java
@Repository
public interface MessageRepository extends MongoRepository<Message, String> {

    List<Message> findByConversationIdOrderByCreatedAtDesc(
        String conversationId,
        Pageable pageable
    );

    List<Message> findByFromUserIdOrToUserId(String fromUserId, String toUserId);

    @Query("{'conversationId': ?0, 'isRead': false, 'toUserId': ?1}")
    List<Message> findUnreadMessages(String conversationId, String toUserId);

    @Query("{$and: [" +
           "{'toUserId': ?0}, " +
           "{'isRead': false}" +
           "]}")
    Long countUnreadMessagesForUser(String toUserId);

    @Query("{'conversationId': ?0, 'createdAt': {$gte: ?1}}")
    List<Message> findMessagesSince(String conversationId, LocalDateTime since);
}
```

---

###### FreelancerRepository

**File**: `src/main/java/com/dev/feature/freelancer/repository/FreelancerRepository.java`

```java
@Repository
public interface FreelancerRepository extends MongoRepository<Freelancer, String> {

    Optional<Freelancer> findByUserId(String userId);

    @Query("{'skills.name': {$in: ?0}}")
    List<Freelancer> findBySkills(List<String> skillNames);

    @Query("{$and: [" +
           "{'availability': ?0}, " +
           "{'hourlyRate': {$lte: ?1}}" +
           "]}")
    List<Freelancer> findAvailableFreelancersWithinBudget(
        Availability availability,
        BigDecimal maxHourlyRate
    );

    @Query("{$or: [" +
           "{'bio': {$regex: ?0, $options: 'i'}}, " +
           "{'skills.name': {$regex: ?0, $options: 'i'}}" +
           "]}")
    List<Freelancer> searchByKeyword(String keyword);

    @Query("{'successRate': {$gte: ?0}}")
    List<Freelancer> findByMinSuccessRate(Double minSuccessRate);

    @Query("{'completedProjects': {$gte: ?0}}")
    List<Freelancer> findByMinCompletedProjects(Integer minProjects);
}
```

---

###### ExperienceRepository

**File**: `src/main/java/com/dev/feature/freelancer/repository/ExperienceRepository.java`

```java
@Repository
public interface ExperienceRepository extends MongoRepository<Experience, String> {

    List<Experience> findByFreelancerIdOrderByStartDateDesc(String freelancerId);

    @Query("{'freelancerId': ?0, 'isCurrentJob': true}")
    List<Experience> findCurrentJobs(String freelancerId);

    Long countByFreelancerId(String freelancerId);
}
```

---

###### EducationRepository

**File**: `src/main/java/com/dev/feature/freelancer/repository/EducationRepository.java`

```java
@Repository
public interface EducationRepository extends MongoRepository<Education, String> {

    List<Education> findByFreelancerIdOrderByEndDateDesc(String freelancerId);

    @Query("{'freelancerId': ?0, 'fieldOfStudy': {$regex: ?0, $options: 'i'}}")
    List<Education> findByFieldOfStudy(String fieldOfStudy);
}
```

---

#### Deliverables

- ✅ 7 PostgreSQL repository interfaces
- ✅ 6 MongoDB repository interfaces
- ✅ Custom queries for complex operations
- ✅ Proper indexing strategies

#### Testing

```bash
./mvnw test -Dtest=RepositoryTest
```

---

### Phase 4: Client Module ⭐⭐ Priority 2

**Duration**: 2-3 days | **Week**: 2-3

#### Objective

Build complete Client feature module (profile management).

#### Package Structure

```
com/dev/feature/client/
├── controller/
│   └── ClientController.java
├── dto/
│   ├── request/
│   │   ├── RegisterClientRequest.java
│   │   └── UpdateClientProfileRequest.java
│   └── response/
│       ├── ClientProfileResponse.java
│       └── ClientStatsResponse.java
├── repository/
│   └── ClientRepository.java
└── service/
    ├── ClientService.java
    └── impl/
        └── ClientServiceImpl.java
```

---

#### Tasks

##### 4.1 Create DTOs

###### RegisterClientRequest

**File**: `src/main/java/com/dev/feature/client/dto/request/RegisterClientRequest.java`

```java
@Data @Schema(description = "Register client request")
public class RegisterClientRequest {

    @NotBlank(message = "Company name is required")
    @Size(min = 2, max = 255)
    private String companyName;

    @Size(max = 100)
    private String industry;

    private CompanySize companySize;

    @Valid
    private List<PaymentMethodDTO> paymentMethods;
}
```

---

###### UpdateClientProfileRequest

**File**: `src/main/java/com/dev/feature/client/dto/request/UpdateClientProfileRequest.java`

```java
@Data @Schema(description = "Update client profile request")
public class UpdateClientProfileRequest {

    @Size(min = 2, max = 255)
    private String companyName;

    @Size(max = 100)
    private String industry;

    private CompanySize companySize;

    @Valid
    private List<PaymentMethodDTO> paymentMethods;
}
```

---

###### ClientProfileResponse

**File**: `src/main/java/com/dev/feature/client/dto/response/ClientProfileResponse.java`

```java
@Data @Builder @Schema(description = "Client profile response")
public class ClientProfileResponse {

    private UUID id;
    private String email;
    private String fullName;
    private String avatar;

    // Client-specific fields
    private String companyName;
    private String industry;
    private CompanySize companySize;
    private List<PaymentMethodDTO> paymentMethods;

    // Statistics
    private BigDecimal totalSpent;
    private Integer postedProjects;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

---

###### ClientStatsResponse

**File**: `src/main/java/com/dev/feature/client/dto/response/ClientStatsResponse.java`

```java
@Data @Builder @Schema(description = "Client statistics response")
public class ClientStatsResponse {

    private UUID clientId;
    private String companyName;

    // Project statistics
    private Integer totalProjects;
    private Integer activeProjects;
    private Integer completedProjects;

    // Financial statistics
    private BigDecimal totalSpent;
    private BigDecimal pendingEscrow;

    // Rating statistics
    private Double averageRating;
    private Integer totalReviews;
}
```

---

##### 4.2 Create Service

###### ClientService Interface

**File**: `src/main/java/com/dev/feature/client/service/ClientService.java`

```java
public interface ClientService {

    ClientProfileResponse registerClient(UUID userId, RegisterClientRequest request);

    ClientProfileResponse getClientProfile(UUID userId);

    ClientProfileResponse updateClientProfile(UUID userId, UpdateClientProfileRequest request);

    ClientStatsResponse getClientStats(UUID userId);

    void deleteClient(UUID userId);
}
```

---

###### ClientServiceImpl

**File**: `src/main/java/com/dev/feature/client/service/impl/ClientServiceImpl.java`

```java
@Service
@RequiredArgsConstructor
@Transactional
public class ClientServiceImpl implements ClientService {

    private final ClientRepository clientRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    @Override
    public ClientProfileResponse registerClient(UUID userId, RegisterClientRequest request) {
        // Validate user exists
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Check if client profile already exists
        if (clientRepository.existsById(userId)) {
            throw new ConflictException("Client profile already exists");
        }

        // Create client
        Client client = Client.builder()
            .id(userId)
            .companyName(request.getCompanyName())
            .industry(request.getIndustry())
            .companySize(request.getCompanySize())
            .paymentMethods(convertPaymentMethods(request.getPaymentMethods()))
            .totalSpent(BigDecimal.ZERO)
            .postedProjects(0)
            .build();

        client = clientRepository.save(client);

        // Update user role
        user.setRole(Role.CLIENT);
        userRepository.save(user);

        return convertToResponse(client, user);
    }

    @Override
    @Transactional(readOnly = true)
    public ClientProfileResponse getClientProfile(UUID userId) {
        Client client = clientRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Client profile not found"));

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return convertToResponse(client, user);
    }

    @Override
    public ClientProfileResponse updateClientProfile(UUID userId, UpdateClientProfileRequest request) {
        Client client = clientRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Client profile not found"));

        // Update fields if provided
        if (request.getCompanyName() != null) {
            client.setCompanyName(request.getCompanyName());
        }
        if (request.getIndustry() != null) {
            client.setIndustry(request.getIndustry());
        }
        if (request.getCompanySize() != null) {
            client.setCompanySize(request.getCompanySize());
        }
        if (request.getPaymentMethods() != null) {
            client.setPaymentMethods(convertPaymentMethods(request.getPaymentMethods()));
        }

        client = clientRepository.save(client);

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return convertToResponse(client, user);
    }

    @Override
    @Transactional(readOnly = true)
    public ClientStatsResponse getClientStats(UUID userId) {
        Client client = clientRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Client profile not found"));

        // Get project statistics
        // TODO: Implement after Project module is ready

        return ClientStatsResponse.builder()
            .clientId(client.getId())
            .companyName(client.getCompanyName())
            .totalProjects(client.getPostedProjects())
            .totalSpent(client.getTotalSpent())
            .build();
    }

    @Override
    public void deleteClient(UUID userId) {
        // Soft delete - mark user as inactive
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setIsActive(false);
        userRepository.save(user);

        // Delete client profile
        clientRepository.deleteById(userId);
    }

    // Helper methods
    private ClientProfileResponse convertToResponse(Client client, User user) {
        return ClientProfileResponse.builder()
            .id(client.getId())
            .email(user.getEmail())
            .fullName(user.getFullName())
            .avatar(user.getAvatarUrl())
            .companyName(client.getCompanyName())
            .industry(client.getIndustry())
            .companySize(client.getCompanySize())
            .paymentMethods(convertToDTO(client.getPaymentMethods()))
            .totalSpent(client.getTotalSpent())
            .postedProjects(client.getPostedProjects())
            .createdAt(client.getCreatedAt())
            .updatedAt(client.getUpdatedAt())
            .build();
    }

    private List<PaymentMethod> convertPaymentMethods(List<PaymentMethodDTO> dtos) {
        // Implementation
        return new ArrayList<>();
    }

    private List<PaymentMethodDTO> convertToDTO(List<PaymentMethod> entities) {
        // Implementation
        return new ArrayList<>();
    }
}
```

---

##### 4.3 Create Controller

###### ClientController

**File**: `src/main/java/com/dev/feature/client/controller/ClientController.java`

```java
@RestController
@RequestMapping("/api/v1/clients")
@RequiredArgsConstructor
@Tag(name = "Client", description = "Client Management APIs")
public class ClientController {

    private final ClientService clientService;

    @PostMapping("/register")
    @Operation(summary = "Register as client")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ClientProfileResponse> registerClient(
        @AuthenticationPrincipal UserDetails userDetails,
        @Valid @RequestBody RegisterClientRequest request
    ) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        ClientProfileResponse response = clientService.registerClient(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/profile")
    @Operation(summary = "Get client profile")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<ClientProfileResponse> getClientProfile(
        @AuthenticationPrincipal UserDetails userDetails
    ) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        ClientProfileResponse response = clientService.getClientProfile(userId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/profile")
    @Operation(summary = "Update client profile")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<ClientProfileResponse> updateClientProfile(
        @AuthenticationPrincipal UserDetails userDetails,
        @Valid @RequestBody UpdateClientProfileRequest request
    ) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        ClientProfileResponse response = clientService.updateClientProfile(userId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/stats")
    @Operation(summary = "Get client statistics")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<ClientStatsResponse> getClientStats(
        @AuthenticationPrincipal UserDetails userDetails
    ) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        ClientStatsResponse response = clientService.getClientStats(userId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/profile")
    @Operation(summary = "Delete client profile")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<Void> deleteClient(
        @AuthenticationPrincipal UserDetails userDetails
    ) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        clientService.deleteClient(userId);
        return ResponseEntity.noContent().build();
    }
}
```

---

#### Deliverables

- ✅ Client DTOs (request/response)
- ✅ ClientService interface + implementation
- ✅ ClientController with all endpoints
- ✅ Validation & error handling
- ✅ Integration tests

#### API Endpoints

| Method | Endpoint                   | Description        | Auth   |
| ------ | -------------------------- | ------------------ | ------ |
| POST   | `/api/v1/clients/register` | Register as client | USER   |
| GET    | `/api/v1/clients/profile`  | Get profile        | CLIENT |
| PUT    | `/api/v1/clients/profile`  | Update profile     | CLIENT |
| GET    | `/api/v1/clients/stats`    | Get statistics     | CLIENT |
| DELETE | `/api/v1/clients/profile`  | Delete profile     | CLIENT |

#### Testing

```bash
# Run client module tests
./mvnw test -Dtest=ClientControllerTest
./mvnw test -Dtest=ClientServiceTest
```

---

### Phase 5: Project Module ⭐⭐ Priority 2

**Duration**: 3-4 days | **Week**: 3-4

#### Objective

Build complete Project feature module with search/filter capabilities.

#### Package Structure

```
com/dev/feature/project/
├── controller/
│   └── ProjectController.java
├── dto/
│   ├── request/
│   │   ├── CreateProjectRequest.java
│   │   ├── UpdateProjectRequest.java
│   │   └── ProjectSearchRequest.java
│   └── response/
│       ├── ProjectResponse.java
│       └── ProjectDetailResponse.java
├── repository/
│   └── ProjectRepository.java (MongoDB)
└── service/
    ├── ProjectService.java
    └── impl/
        └── ProjectServiceImpl.java
```

---

#### Tasks

##### 5.1 Create DTOs

###### CreateProjectRequest

```java
@Data @Schema(description = "Create project request")
public class CreateProjectRequest {

    @NotBlank(message = "Title is required")
    @Size(min = 10, max = 200)
    private String title;

    @NotBlank(message = "Description is required")
    @Size(min = 50, max = 5000)
    private String description;

    @Size(max = 2000)
    private String requirements;

    @NotEmpty(message = "At least one skill is required")
    private List<String> skills;

    @Valid
    @NotNull(message = "Budget information is required")
    private ProjectBudgetDTO budget;

    @Min(value = 1, message = "Duration must be at least 1 day")
    @Max(value = 365, message = "Duration cannot exceed 365 days")
    private Integer duration;

    @NotNull(message = "Project type is required")
    private ProjectType type;

    @Future(message = "Deadline must be in the future")
    private LocalDateTime deadline;
}
```

---

###### ProjectSearchRequest

```java
@Data @Schema(description = "Project search request")
public class ProjectSearchRequest {

    private String keyword;

    private List<String> skills;

    private BigDecimal minBudget;
    private BigDecimal maxBudget;

    private ProjectType type;

    private List<ProjectStatus> statuses;

    private String sortBy = "createdAt"; // createdAt, deadline, budget
    private String sortDirection = "DESC"; // ASC, DESC

    private Integer page = 0;
    private Integer size = 20;
}
```

---

##### 5.2 Create Service

###### ProjectServiceImpl (Key Methods)

```java
@Service
@RequiredArgsConstructor
@Transactional
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ClientRepository clientRepository;
    private final ModelMapper modelMapper;
    private final RedisCacheService redisCacheService;

    @Override
    public ProjectResponse createProject(UUID clientId, CreateProjectRequest request) {
        // Validate client exists
        Client client = clientRepository.findById(clientId)
            .orElseThrow(() -> new ResourceNotFoundException("Client not found"));

        // Create project
        Project project = Project.builder()
            .clientId(clientId.toString())
            .title(request.getTitle())
            .description(request.getDescription())
            .requirements(request.getRequirements())
            .skills(request.getSkills())
            .budget(convertToBudget(request.getBudget()))
            .duration(request.getDuration())
            .status(ProjectStatus.OPEN)
            .type(request.getType())
            .deadline(request.getDeadline())
            .build();

        project = projectRepository.save(project);

        // Update client stats
        client.setPostedProjects(client.getPostedProjects() + 1);
        clientRepository.save(client);

        // Clear cache
        invalidateProjectListCache();

        return convertToResponse(project, client);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProjectResponse> searchProjects(ProjectSearchRequest request) {
        // Check cache first
        String cacheKey = generateCacheKey(request);
        String cached = redisCacheService.get(cacheKey);
        if (cached != null) {
            // Return from cache
            return parseFromCache(cached);
        }

        // Build dynamic query
        Pageable pageable = PageRequest.of(
            request.getPage(),
            request.getSize(),
            Sort.by(Sort.Direction.fromString(request.getSortDirection()), request.getSortBy())
        );

        Page<Project> projects = projectRepository.findAll(
            buildSpecification(request),
            pageable
        );

        // Cache result
        redisCacheService.set(cacheKey, serializeToCache(projects), 15, TimeUnit.MINUTES);

        return projects.map(p -> {
            User client = userRepository.findById(UUID.fromString(p.getClientId()))
                .orElse(null);
            return convertToResponse(p, client);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectDetailResponse getProjectDetail(String projectId) {
        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        UUID clientId = UUID.fromString(project.getClientId());
        User client = userRepository.findById(clientId)
            .orElseThrow(() -> new ResourceNotFoundException("Client not found"));

        User freelancer = null;
        if (project.getFreelancerId() != null) {
            freelancer = userRepository.findById(UUID.fromString(project.getFreelancerId()))
                .orElse(null);
        }

        return convertToDetailResponse(project, client, freelancer);
    }

    @Override
    public ProjectResponse updateProjectStatus(String projectId, ProjectStatus status) {
        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        // Validate status transitions
        validateStatusTransition(project.getStatus(), status);

        project.setStatus(status);

        if (status == ProjectStatus.IN_PROGRESS) {
            project.setStartedAt(LocalDateTime.now());
        } else if (status == ProjectStatus.COMPLETED) {
            project.setCompletedAt(LocalDateTime.now());
        }

        project = projectRepository.save(project);

        // Clear cache
        invalidateProjectListCache();

        User client = userRepository.findById(UUID.fromString(project.getClientId()))
            .orElse(null);

        return convertToResponse(project, client);
    }

    // Helper methods
    private Specification<Project> buildSpecification(ProjectSearchRequest request) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Status filter
            if (request.getStatuses() != null && !request.getStatuses().isEmpty()) {
                predicates.add(root.get("status").in(request.getStatuses()));
            }

            // Type filter
            if (request.getType() != null) {
                predicates.add(cb.equal(root.get("type"), request.getType()));
            }

            // Skills filter (at least one match)
            if (request.getSkills() != null && !request.getSkills().isEmpty()) {
                predicates.add(root.get("skills").in(request.getSkills()));
            }

            // Budget range filter
            if (request.getMinBudget() != null || request.getMaxBudget() != null) {
                if (request.getMinBudget() != null) {
                    predicates.add(cb.greaterThanOrEqualTo(
                        root.get("budget").get("maxAmount"),
                        request.getMinBudget()
                    ));
                }
                if (request.getMaxBudget() != null) {
                    predicates.add(cb.lessThanOrEqualTo(
                        root.get("budget").get("minAmount"),
                        request.getMaxBudget()
                    ));
                }
            }

            // Keyword search (title or description)
            if (request.getKeyword() != null && !request.getKeyword().isBlank()) {
                String pattern = "%" + request.getKeyword().toLowerCase() + "%";
                Predicate titleMatch = cb.like(cb.lower(root.get("title")), pattern);
                Predicate descMatch = cb.like(cb.lower(root.get("description")), pattern);
                predicates.add(cb.or(titleMatch, descMatch));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private void validateStatusTransition(ProjectStatus from, ProjectStatus to) {
        // Define allowed transitions
        Map<ProjectStatus, List<ProjectStatus>> allowedTransitions = Map.of(
            ProjectStatus.OPEN, List.of(ProjectStatus.IN_PROGRESS, ProjectStatus.COMPLETED),
            ProjectStatus.IN_PROGRESS, List.of(ProjectStatus.COMPLETED),
            ProjectStatus.COMPLETED, List.of()
        );

        List<ProjectStatus> allowed = allowedTransitions.get(from);
        if (allowed == null || !allowed.contains(to)) {
            throw new BadRequestException(
                String.format("Cannot transition from %s to %s", from, to)
            );
        }
    }

    private void invalidateProjectListCache() {
        // Invalidate all project list caches
        Set<String> keys = redisCacheService.keys("projects:list:*");
        keys.forEach(key -> redisCacheService.delete(key));
    }
}
```

---

##### 5.3 Create Controller

###### ProjectController (Key Endpoints)

```java
@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
@Tag(name = "Project", description = "Project Management APIs")
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping
    @Operation(summary = "Create a new project")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<ProjectResponse> createProject(
        @AuthenticationPrincipal UserDetails userDetails,
        @Valid @RequestBody CreateProjectRequest request
    ) {
        UUID clientId = UUID.fromString(userDetails.getUsername());
        ProjectResponse response = projectService.createProject(clientId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/search")
    @Operation(summary = "Search projects with filters")
    public ResponseEntity<Page<ProjectResponse>> searchProjects(
        @Valid ProjectSearchRequest request
    ) {
        Page<ProjectResponse> response = projectService.searchProjects(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get project details")
    public ResponseEntity<ProjectDetailResponse> getProjectDetail(
        @PathVariable String id
    ) {
        ProjectDetailResponse response = projectService.getProjectDetail(id);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update project status")
    @PreAuthorize("hasAnyRole('CLIENT', 'FREELANCER')")
    public ResponseEntity<ProjectResponse> updateProjectStatus(
        @PathVariable String id,
        @RequestParam ProjectStatus status
    ) {
        ProjectResponse response = projectService.updateProjectStatus(id, status);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete project")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<Void> deleteProject(
        @PathVariable String id,
        @AuthenticationPrincipal UserDetails userDetails
    ) {
        UUID clientId = UUID.fromString(userDetails.getUsername());
        projectService.deleteProject(id, clientId);
        return ResponseEntity.noContent().build();
    }
}
```

---

#### Deliverables

- ✅ Project DTOs (request/response)
- ✅ ProjectService with cross-database operations
- ✅ ProjectController with all endpoints
- ✅ Search/filter functionality
- ✅ Redis caching for project listings
- ✅ Integration tests

#### API Endpoints

| Method | Endpoint                       | Description            | Auth              |
| ------ | ------------------------------ | ---------------------- | ----------------- |
| POST   | `/api/v1/projects`             | Create project         | CLIENT            |
| GET    | `/api/v1/projects/search`      | Search/filter projects | Public            |
| GET    | `/api/v1/projects/{id}`        | Get project details    | Public            |
| PATCH  | `/api/v1/projects/{id}/status` | Update status          | CLIENT/FREELANCER |
| DELETE | `/api/v1/projects/{id}`        | Delete project         | CLIENT            |

---

### Phase 6: Bidding System ⭐⭐⭐ Priority 3

**Duration**: 3-4 days | **Week**: 4-5

#### Objective

Build complete Bidding system with notifications.

#### Package Structure

```
com/dev/feature/bid/
├── controller/
│   └── BidController.java
├── dto/
│   ├── request/
│   │   └── SubmitBidRequest.java
│   └── response/
│       └── BidResponse.java
├── repository/
│   └── BidRepository.java (MongoDB)
└── service/
    ├── BidService.java
    └── impl/
        └── BidServiceImpl.java
```

---

#### Key Features

1. Submit bid on project
2. List bids for project owner
3. Accept/reject bid
4. Validate bid eligibility
5. Auto-create contract when bid accepted
6. Notifications for new bids

---

#### API Endpoints

| Method | Endpoint                            | Description  | Auth       |
| ------ | ----------------------------------- | ------------ | ---------- |
| POST   | `/api/v1/projects/{projectId}/bids` | Submit bid   | FREELANCER |
| GET    | `/api/v1/projects/{projectId}/bids` | List bids    | CLIENT     |
| PATCH  | `/api/v1/bids/{id}/accept`          | Accept bid   | CLIENT     |
| PATCH  | `/api/v1/bids/{id}/reject`          | Reject bid   | CLIENT     |
| DELETE | `/api/v1/bids/{id}`                 | Withdraw bid | FREELANCER |

---

### Phase 7: Payment & Escrow System ⭐⭐⭐⭐⭐ CRITICAL!

**Duration**: 4-5 days | **Week**: 5-6

#### Objective

Build secure payment system with escrow functionality.

#### Package Structure

```
com/dev/feature/payment/
├── controller/
│   ├── PaymentController.java
│   └── WalletController.java
├── dto/
│   ├── request/
│   │   ├── FundEscrowRequest.java
│   │   ├── ReleasePaymentRequest.java
│   │   └── RefundPaymentRequest.java
│   └── response/
│       ├── PaymentResponse.java
│       ├── WalletResponse.java
│       └── TransactionResponse.java
├── repository/ (PostgreSQL)
│   ├── WalletRepository.java
│   ├── PaymentRepository.java
│   └── TransactionRepository.java
└── service/
    ├── WalletService.java
    ├── PaymentService.java
    ├── TransactionService.java
    └── impl/
        └── ...
```

---

#### CRITICAL REQUIREMENTS

- ⚠️ **All payment operations MUST use `@Transactional`**
- ⚠️ **Atomic operations for wallet updates**
- ⚠️ **Double-entry accounting (credit = debit)**
- ⚠️ **Escrow state machine validation**
- ⚠️ **Thorough testing required**

---

#### Key Flows

##### Fund Escrow Flow

```
1. Validate wallet balance
2. DEBIT from client wallet
3. HOLD in escrow
4. Create ESCROW_HOLD transaction
5. Update payment status to ESCROW_HOLD
6. Notify freelancer
```

##### Release Payment Flow

```
1. Validate payment in ESCROW_HOLD
2. RELEASE from escrow
3. CREDIT to freelancer wallet
4. Create ESCROW_RELEASE transaction
5. Update payment status to RELEASED/COMPLETED
6. Update freelancer stats
7. Notify both parties
```

##### Refund Flow

```
1. Validate payment in ESCROW_HOLD
2. RELEASE from escrow
3. CREDIT back to client wallet
4. Update payment status to REFUNDED
5. Notify both parties
```

---

#### API Endpoints

| Method | Endpoint                          | Description         | Auth   |
| ------ | --------------------------------- | ------------------- | ------ |
| GET    | `/api/v1/wallets`                 | Get wallet balance  | USER   |
| GET    | `/api/v1/wallets/transactions`    | Transaction history | USER   |
| POST   | `/api/v1/payments/escrow/fund`    | Fund escrow         | CLIENT |
| POST   | `/api/v1/payments/escrow/release` | Release payment     | CLIENT |
| POST   | `/api/v1/payments/escrow/refund`  | Refund payment      | SYSTEM |

---

#### Testing Requirements

```bash
# CRITICAL: Run these tests thoroughly
./mvnw test -Dtest=PaymentServiceTest
./mvnw test -Dtest=WalletServiceTest
./mvnw test -Dtest=EscrowFlowTest

# Test edge cases
- Insufficient funds
- Double payment attempts
- Concurrent payment operations
- Escrow state transitions
- Refund scenarios
```

---

### Phase 8: Contract System ⭐⭐ Priority 3

**Duration**: 2-3 days | **Week**: 6

#### Objective

Auto-contract creation and management.

#### Package Structure

```
com/dev/feature/contract/
├── controller/
│   └── ContractController.java
├── dto/
│   └── response/
│       └── ContractResponse.java
├── repository/
│   └── ContractRepository.java (PostgreSQL)
└── service/
    ├── ContractService.java
    └── impl/
        └── ContractServiceImpl.java
```

---

#### Key Features

1. Auto-create contract when bid is accepted
2. Track contract status (ACTIVE, COMPLETED)
3. Link contract to project
4. Update freelancer assignment

---

### Phase 9: Chat System ⭐⭐ Priority 2

**Duration**: 3-4 days | **Week**: 6-7

#### Objective

Real-time messaging system with MongoDB + PostgreSQL.

#### Package Structure

```
com/dev/feature/chat/
├── controller/
│   ├── MessageController.java
│   └── ConversationController.java
├── dto/
│   ├── request/
│   │   └── SendMessageRequest.java
│   └── response/
│       ├── MessageResponse.java
│       └── ConversationResponse.java
├── repository/
│   └── MessageRepository.java (MongoDB)
└── service/
    ├── MessageService.java
    ├── ConversationService.java
    └── impl/
        └── ...
```

---

#### Key Features

1. Send/retrieve messages with pagination
2. Conversation management
3. Read/unread status tracking
4. Real-time updates (WebSocket - optional for MVP)

---

#### API Endpoints

| Method | Endpoint                              | Description        | Auth |
| ------ | ------------------------------------- | ------------------ | ---- |
| POST   | `/api/v1/messages`                    | Send message       | USER |
| GET    | `/api/v1/conversations/{id}/messages` | Get messages       | USER |
| GET    | `/api/v1/conversations`               | List conversations | USER |
| PATCH  | `/api/v1/messages/{id}/read`          | Mark as read       | USER |

---

### Phase 10: Review System ⭐⭐ Priority 2

**Duration**: 2-3 days | **Week**: 7

#### Objective

Build review system with reputation calculation.

#### Package Structure

```
com/dev/feature/review/
├── controller/
│   └── ReviewController.java
├── dto/
│   ├── request/
│   │   └── CreateReviewRequest.java
│   └── response/
│       └── ReviewResponse.java
├── repository/
│   └── ReviewRepository.java (PostgreSQL)
└── service/
    ├── ReviewService.java
    └── impl/
        └── ReviewServiceImpl.java
```

---

#### Key Features

1. Submit review (validate: project must be completed)
2. Retrieve reviews by user/project
3. Calculate reputation score
4. Update user statistics

---

#### API Endpoints

| Method | Endpoint                              | Description         | Auth   |
| ------ | ------------------------------------- | ------------------- | ------ |
| POST   | `/api/v1/reviews`                     | Create review       | USER   |
| GET    | `/api/v1/reviews/project/{projectId}` | Get project reviews | Public |
| GET    | `/api/v1/reviews/user/{userId}`       | Get user reviews    | Public |

---

### Phase 11: Redis Cache Layer ⭐ Priority 1

**Duration**: 2-3 days | **Week**: 7

#### Objective

Implement caching layer for performance optimization.

#### Package Structure

```
com/dev/cache/service/
└── RedisCacheService.java
```

---

#### Cache Patterns

##### 1. Session Caching

```java
// Key: session:{refreshToken}
// Value: {userId, email, role, createdAt}
// TTL: 7 days
```

##### 2. Profile Caching

```java
// Key: profile:{userId}
// Value: JSON serialized user profile
// TTL: 1 hour
```

##### 3. Project Listing Cache

```java
// Key: projects:list:{filters_hash}
// Value: Array of project IDs
// TTL: 15 minutes
```

##### 4. Online Users

```java
// Key: online:users
// Type: Set
// Members: [userId1, userId2, ...]
```

---

#### Cache Invalidation Strategy

```
1. Time-based expiration (TTL)
2. Event-based invalidation (on update)
3. Tag-based invalidation (related keys)
4. Selective invalidation (by pattern)
```

---

### Phase 12: Security & Validation ⭐⭐⭐ Priority 3

**Duration**: 2-3 days | **Week**: 7

#### Tasks

##### 12.1 Role-Based Access Control

```java
// Add to all controllers
@PreAuthorize("hasRole('CLIENT')")
@PreAuthorize("hasRole('FREELANCER')")
@PreAuthorize("hasAnyRole('CLIENT', 'FREELANCER')")
```

##### 12.2 Input Validation

```java
// Add to all DTOs
@NotBlank, @NotNull, @Size, @Min, @Max, @Pattern, @Email
@Valid for nested objects
Custom validators for complex rules
```

##### 12.3 Rate Limiting

```java
// Sensitive endpoints
- Login: 5 attempts per 15 min
- Payment: 10 requests per minute
- Bid submission: 20 per hour
```

##### 12.4 CORS Configuration

```yaml
# application.yaml
cors:
  allowed-origins: http://localhost:3000,http://localhost:3001
  allowed-methods: GET,POST,PUT,DELETE,PATCH
  allowed-headers: '*'
  allow-credentials: true
  max-age: 3600
```

---

## 🎯 Success Criteria

### Functional Requirements

- ✅ User can register as client/freelancer
- ✅ Client can post projects
- ✅ Freelancer can search and bid on projects
- ✅ Client can accept/reject bids
- ✅ Payment flows through escrow securely
- ✅ Parties can communicate via chat
- ✅ Users can leave reviews
- ✅ Reputation scores are calculated correctly

### Non-Functional Requirements

- ✅ API response time < 200ms (p95)
- ✅ 99.9% uptime for payment operations
- ✅ All critical operations are transactional
- ✅ Cache hit rate > 80% for listings
- ✅ Zero data loss in payment operations
- ✅ Comprehensive test coverage (> 80%)

### Security Requirements

- ✅ JWT-based authentication
- ✅ Role-based access control
- ✅ Input validation on all endpoints
- ✅ Rate limiting on sensitive operations
- ✅ CORS properly configured
- ✅ SQL injection prevention (parameterized queries)
- ✅ XSS prevention (input sanitization)

---

## 📝 Notes & Best Practices

### Cross-Database Operations

```java
// Always query MongoDB first (faster for documents)
// Then query PostgreSQL for related entities
// Combine in DTO for response

@Service
public class ProjectService {
    public ProjectDetailDTO getProjectDetail(String projectId) {
        // 1. Get project from MongoDB
        Project project = mongoProjectRepository.findById(projectId)
            .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        // 2. Get client from PostgreSQL
        User client = postgresUserRepository.findById(
            UUID.fromString(project.getClientId())
        ).orElseThrow(() -> new ResourceNotFoundException("Client not found"));

        // 3. Get freelancer (if assigned)
        User freelancer = null;
        if (project.getFreelancerId() != null) {
            freelancer = postgresUserRepository.findById(
                UUID.fromString(project.getFreelancerId())
            ).orElse(null);
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

### Transaction Management

```java
// CRITICAL: Always use @Transactional for PostgreSQL operations
@Service
public class PaymentService {
    @Transactional
    public void fundEscrow(PaymentDTO dto) {
        // All operations in single transaction
        // If any fails, entire transaction rolls back

        // 1. Debit from wallet
        wallet.setBalance(wallet.getBalance().subtract(dto.getAmount()));

        // 2. Hold in escrow
        wallet.setEscrowBalance(wallet.getEscrowBalance().add(dto.getAmount()));

        // 3. Create transaction
        Transaction transaction = Transaction.builder()
            .wallet(wallet)
            .type(TransactionType.ESCROW_HOLD)
            .amount(dto.getAmount())
            .build();

        // If any operation fails, all roll back
    }
}
```

### Cache Strategy

```java
// Cache-Aside pattern
@Service
public class ProjectService {
    public List<Project> searchProjects(ProjectSearchRequest request) {
        String cacheKey = generateCacheKey(request);

        // 1. Try cache first
        List<Project> cached = redisCacheService.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        // 2. Query database if cache miss
        List<Project> projects = projectRepository.findAll(
            buildSpecification(request)
        );

        // 3. Populate cache
        redisCacheService.set(cacheKey, projects, 15, TimeUnit.MINUTES);

        return projects;
    }

    public Project updateProject(String id, UpdateProjectRequest request) {
        Project project = projectRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        // Update project
        // ...

        // Invalidate related caches
        redisCacheService.delete("project:" + id);
        redisCacheService.deletePattern("projects:list:*");

        return project;
    }
}
```

---

## 🚀 Getting Started

### Quick Start Commands

```bash
# 1. Start all services
cd FreelancerUp-BE
docker compose up -d --build

# 2. Verify services
docker compose ps
docker compose logs -f

# 3. Initialize database
docker exec -it freelancerup_postgres psql -U postgres -d freelancerup \
    -f /docker-entrypoint-initdb.d/init.sql

# 4. Run tests
./mvnw test

# 5. Access application
# API: http://localhost:8080
# Swagger: http://localhost:8080/swagger-ui/index.html
```

### Development Workflow

```bash
# 1. Create feature branch
git checkout -b feature/client-module

# 2. Implement feature
# - Create entities/documents
# - Create repositories
# - Create DTOs
# - Create services
# - Create controllers
# - Write tests

# 3. Build and test
./mvnw clean install
./mvnw test

# 4. Commit changes
git add .
git commit -m "feat: implement client module"

# 5. Push and create PR
git push origin feature/client-module
```

---

## 📚 Resources

### Documentation

- [CLAUDE.md](../CLAUDE.md) - Backend-specific guidance
- [DATABASE_SCHEMA.md](DATABASE_SCHEMA.md) - Complete database schema
- [MVP_SCOPE.md](MVP_SCOPE.md) - MVP features and timeline
- [USECASE.md](USECASE.md) - Use case diagrams
- [CLASS.md](CLASS.md) - Class diagrams
- [SEQUENCE_FREELANCER.md](SEQUENCE_FREELANCER.md) - Freelancer workflows
- [SEQUENCE_CLIENT.md](SEQUENCE_CLIENT.md) - Client workflows

### External Resources

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Spring Data JPA](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/)
- [Spring Data MongoDB](https://docs.spring.io/spring-data/mongodb/docs/current/reference/html/)
- [Spring Data Redis](https://docs.spring.io/spring-data/redis/docs/current/reference/html/)

---

**Last Updated**: 2026-01-20
**Version**: MVP v1.0
**Status**: 🚧 In Development (Week 1-2)
