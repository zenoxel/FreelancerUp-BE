classDiagram
    %% ========================================================================
    %% CORE ENTITIES
    %% ========================================================================
    class User {
        +id: String
        +email: String
        +password: Hash
        +fullName: String
        +phone: String
        +avatar: String
        +role: Enum (USER, CLIENT, FREELANCER)
        +isActive: Boolean
        +isVerified: Boolean
        +reputationScore: Float
        +totalProjects: Integer
        +totalEarnings: BigDecimal
        +createdAt: DateTime
        +updatedAt: DateTime
        +register()
        +login()
        +updateProfile()
        +getStats()
    }

    class Client {
        +companyName: String
        +industry: String
        +companySize: Enum
        +paymentMethods: Array
        +totalSpent: BigDecimal
        +postedProjects: Integer
        +postProject()
        +manageBids()
        +hireFreelancer()
        +manageProject()
        +releasePayment()
        +rateFreelancer()
        +getStats()
    }

    class Freelancer {
        +bio: String
        +hourlyRate: BigDecimal
        +skills: Array
        +experience: Array
        +education: Array
        +availability: Enum (AVAILABLE, BUSY, OFFLINE)
        +totalEarned: BigDecimal
        +completedProjects: Integer
        +successRate: Float
        +searchProjects()
        +bidProject()
        +acceptOffer()
        +submitDeliverable()
        +rateClient()
        +buildReputation()
        +updateAvailability()
        +updateSkills()
    }

    %% ========================================================================
    %% PROJECT MANAGEMENT (SIMPLIFIED)
    %% ========================================================================
    class Project {
        +id: String
        +clientId: String
        +freelancerId: String
        +title: String
        +description: String
        +requirements: String
        +skills: Array
        +budget: ProjectBudget
        +duration: Integer (days)
        +status: Enum (OPEN, IN_PROGRESS, COMPLETED)
        +type: Enum (FIXED_PRICE, HOURLY)
        +deadline: DateTime
        +startedAt: DateTime
        +completedAt: DateTime
        +createdAt: DateTime
        +updatedAt: DateTime
        +create()
        +updateStatus()
        +assignFreelancer()
        +start()
        +complete()
        +getProgress()
    }

    class ProjectBudget {
        +minAmount: BigDecimal
        +maxAmount: BigDecimal
        +currency: String
        +isNegotiable: Boolean
    }

    %% ========================================================================
    %% BIDDING & CONTRACT (SIMPLIFIED)
    %% ========================================================================
    class Bid {
        +id: String
        +projectId: String
        +freelancerId: String
        +proposal: String
        +price: BigDecimal
        +estimatedDuration: Integer (days)
        +status: Enum (PENDING, ACCEPTED, REJECTED)
        +submittedAt: DateTime
        +respondedAt: DateTime
        +createdAt: DateTime
        +updatedAt: DateTime
        +submit()
        +accept()
        +reject()
        +update()
    }

    class Contract {
        +id: String
        +projectId: String
        +clientId: String
        +freelancerId: String
        +status: Enum (ACTIVE, COMPLETED)
        +createdAt: DateTime
        +updatedAt: DateTime
        +sign()
        +activate()
        +terminate()
        +getExtension()
    }

    class Invite {
        +id: String
        +projectId: String
        +fromUserId: String (Client)
        +toUserId: String (Freelancer)
        +message: String
        +status: Enum (PENDING, ACCEPTED, REJECTED, EXPIRED)
        +expiresAt: DateTime
        +respondedAt: DateTime
        +createdAt: DateTime
        +send()
        +accept()
        +reject()
        +expire()
        +withdraw()
    }

    %% ========================================================================
    %% FINANCIAL
    %% ========================================================================
    class Payment {
        +id: String
        +projectId: String
        +milestoneId: String
        +fromUserId: String (Client)
        +toUserId: String (Freelancer)
        +type: Enum (FINAL, REFUND)
        +amount: BigDecimal
        +fee: BigDecimal
        +netAmount: BigDecimal
        +status: Enum (PENDING, ESCROW_HOLD, RELEASED, COMPLETED, REFUNDED, FAILED)
        +method: Enum (CREDIT_CARD, PAYPAL, BANK_TRANSFER, WALLET)
        +isEscrow: Boolean
        +escrowFundedAt: DateTime
        +escrowReleasedAt: DateTime
        +createdAt: DateTime
        +completedAt: DateTime
        +fundEscrow()
        +release()
        +refund()
        +calculateFee()
        +process()
    }

    class Wallet {
        +id: String
        +userId: String
        +balance: BigDecimal
        +escrowBalance: BigDecimal
        +totalEarned: BigDecimal
        +currency: String
        +createdAt: DateTime
        +updatedAt: DateTime
        +getBalance()
        +credit()
        +debit()
        +holdInEscrow()
        +releaseFromEscrow()
        +getTransactionHistory()
    }

    class Transaction {
        +id: String
        +walletId: String
        +userId: String
        +type: Enum (CREDIT, DEBIT, ESCROW_HOLD, ESCROW_RELEASE)
        +amount: BigDecimal
        +status: Enum (PENDING, COMPLETED, FAILED, CANCELLED)
        +description: String
        +referenceId: String (projectId)
        +balanceBefore: BigDecimal
        +balanceAfter: BigDecimal
        +createdAt: DateTime
        +completedAt: DateTime
        +process()
        +complete()
        +fail()
        +getDetails()
    }

    class Withdrawal {
        +id: String
        +userId: String
        +walletId: String
        +amount: BigDecimal
        +method: Enum (BANK_TRANSFER, PAYPAL, STRIPE)
        +accountDetails: Object
        +status: Enum (PENDING, PROCESSING, COMPLETED, REJECTED)
        +fee: BigDecimal
        +netAmount: BigDecimal
        +requestedAt: DateTime
        +processedAt: DateTime
        +transactionId: String
        +request()
        +approve()
        +reject()
        +process()
        +cancel()
        +getReceipt()
    }

    %% ========================================================================
    %% COMMUNICATION (SIMPLIFIED)
    %% ========================================================================
    class Message {
        +id: String
        +conversationId: String
        +projectId: String
        +fromUserId: String
        +toUserId: String
        +content: String
        +isRead: Boolean
        +readAt: DateTime
        +createdAt: DateTime
        +send()
        +markAsRead()
        +delete()
    }

    class Conversation {
        +id: String
        +projectId: String
        +participants: Array
        +lastMessageAt: DateTime
        +lastMessagePreview: String
        +isActive: Boolean
        +createdAt: DateTime
        +updatedAt: DateTime
        +getMessages()
        +markAsRead()
        +close()
        +archive()
    }

    %% ========================================================================
    %% REPUTATION & REVIEWS (SIMPLIFIED)
    %% ========================================================================
    class Review {
        +id: String
        +projectId: String
        +fromUserId: String
        +toUserId: String
        +rating: Integer (1-5)
        +comment: String
        +isVisible: Boolean
        +createdAt: DateTime
        +updatedAt: DateTime
        +submit()
        +update()
        +delete()
        +getAverageRating()
    }

    class ReviewCategories {
        +communication: Integer (1-5)
        +quality: Integer (1-5)
        +timeline: Integer (1-5)
        +professionalism: Integer (1-5)
        +responsiveness: Integer (1-5)
        +getAverage()
    }

    class PlatformFeedback {
        +id: String
        +userId: String
        +projectId: String
        +rating: Integer (1-5)
        +category: Enum (BUG, FEATURE, GENERAL, COMPLAINT)
        +comment: String
        +attachments: Array
        +status: Enum (PENDING, REVIEWED, RESOLVED)
        +createdAt: DateTime
        +submit()
        +update()
        +resolve()
    }

    %% ========================================================================
    %% PORTFOLIO & SKILLS
    %% ========================================================================
    class Skill {
        +id: String
        +name: String
        +category: String
        +createdAt: DateTime
        +getPopular()
        +search()
    }

    class FreelancerSkill {
        +id: String
        +freelancerId: String
        +skillId: String
        +proficiencyLevel: Enum (BEGINNER, INTERMEDIATE, ADVANCED, EXPERT)
        +yearsOfExperience: Integer
        +createdAt: DateTime
        +update()
        +remove()
    }

    class Experience {
        +id: String
        +freelancerId: String
        +title: String
        +company: String
        +location: String
        +startDate: DateTime
        +endDate: DateTime
        +isCurrentJob: Boolean
        +description: String
        +skills: Array
        +createdAt: DateTime
        +updatedAt: DateTime
    }

    class Education {
        +id: String
        +freelancerId: String
        +school: String
        +degree: String
        +fieldOfStudy: String
        +startDate: DateTime
        +endDate: DateTime
        +description: String
        +createdAt: DateTime
        +updatedAt: DateTime
    }

    %% ========================================================================
    %% INHERITANCE RELATIONSHIPS
    %% ========================================================================
    User <|-- Client
    User <|-- Freelancer

    %% ========================================================================
    %% CORE RELATIONSHIPS
    %% ========================================================================
    Client "1" -- "many" Project : creates > posts
    Freelancer "many" -- "1" Project : works on > is hired for
    Project "1" -- "0..1" Freelancer : is assigned to

    %% ========================================================================
    %% PROJECT RELATIONSHIPS
    %% ========================================================================
    Project "1" -- "0..1" Contract : requires > has
    Project "1" -- "many" Bid : receives > has
    Project "1" -- "many" Payment : has
    Project "1" -- "1" Conversation : has
    Project "1" -- "many" Review : can have

    %% ========================================================================
    %% BID & CONTRACT RELATIONSHIPS
    %% ========================================================================
    Freelancer "1" -- "many" Bid : submits > has
    Bid "many" -- "1" Project : is for > belongs to

    Client "1" -- "many" Contract : signs
    Freelancer "1" -- "many" Contract : signs

    %% ========================================================================
    %% WALLET & FINANCIAL RELATIONSHIPS
    %% ========================================================================
    User "1" -- "1" Wallet : owns > has
    User "1" -- "many" Transaction : has

    Wallet "1" -- "many" Transaction : contains

    %% ========================================================================
    %% COMMUNICATION RELATIONSHIPS
    %% ========================================================================
    User "1" -- "many" Message : sends
    User "1" -- "many" Message : receives
    User "1" -- "many" Conversation : participates in

    Conversation "1" -- "many" Message : contains > has
    Project "1" -- "1" Conversation : associated with

    %% ========================================================================
    %% REVIEW & REPUTATION RELATIONSHIPS
    %% ========================================================================
    User "1" -- "many" Review : can give > writes
    User "1" -- "many" Review : can receive > gets

    Project "1" -- "many" Review : generates > has

    %% ========================================================================
    %% PORTFOLIO & SKILLS RELATIONSHIPS
    %% ========================================================================
    Freelancer "1" -- "many" FreelancerSkill : has > possesses
    Freelancer "1" -- "many" Experience : has
    Freelancer "1" -- "many" Education : has

    Skill "1" -- "many" FreelancerSkill : is used in

    %% ========================================================================
    %% MVP SCOPE NOTES
    %% ========================================================================
    %% Removed for MVP:
    %% - Admin entity
    %% - Milestone & Deliverable classes
    %% - Invite class
    %% - Withdrawal class
    %% - Dispute class
    %% - PlatformFeedback class
    %% - PortfolioItem class
    %% - Verification class
    %% - Notification class
    %% - ReviewCategories class (chỉ giữ rating đơn giản)
    %% - Contract signature fields
    %% - Complex project statuses (chỉ OPEN, IN_PROGRESS, COMPLETED)
