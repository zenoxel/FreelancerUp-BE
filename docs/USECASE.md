graph TD
subgraph "FreelancerUp MVP - Use Cases (2 Months Scope)"
%% ===== CLIENT ACTOR =====
C[Client] --> DKC([Đăng ký/Đăng nhập])
C --> QHS([Quản lý Hồ sơ])
C --> DDA([Đăng Dự án])
C --> TFL([Tìm Freelancer])
C --> QLD([Quản lý Bidders])
C --> QHD([Quản lý Hợp đồng])
C --> QDA([Quản lý Dự án])
C --> QMS([Quản lý Milestone])
C --> NTET([Nạp tiền Escrow])
C --> TTMS([Thanh toán Milestone])
C --> DGF([Đánh giá Freelancer])
C --> HTN([Hệ thống Tin nhắn])
C -.-> XLSG([Xem Lịch sử Giao dịch])

        %% ===== FREELANCER ACTOR =====
        F[Freelancer] --> DKF([Đăng ký/Đăng nhập])
        F --> QHSP([Quản lý Hồ sơ cá nhân])
        F --> TDD([Tìm Dự án])
        C --> LTKFL([Lọc/Tìm kiếm Freelancers])
        F --> DDAU([Đấu thầu Dự án])
        F --> CLO([Chấp nhận Lời mời])
        F --> NBGBM([Nộp Bàn giao Milestone])
        F --> DGC([Đánh giá Client])
        F --> RTT([Rút Tiền])
        F --> XLSG([Xem Lịch sử Giao dịch])
        F --> HTNF([Hệ thống Tin nhắn])
        F --> TBF([Thông báo])
        F --> MTDT([Mở Tranh chấp])
        F --> XCTU([Xây dựng Uy tín])

        %% ===== ADMIN ACTOR =====
        A[Admin] --> QLM([Quản lý Nền tảng])
        A --> QND([Quản lý Người dùng])
        A --> KDND([Kiểm duyệt Nội dung])
        A --> XLTC([Xử lý Tranh chấp])
        A --> PDXM([Phê duyệt Xác minh])
        A --> PTPT([Phân tích Nền tảng])

        %% ===== RELATIONSHIPS - CLIENT =====
        DDA -->|<<include>>| QDA
        QLD -->|<<include>>| QHD
        QHD -->|<<include>>| QDA
        QDA -->|<<include>>| NTET
        NTET -->|<<include>>| TTD
        TTD -->|<<extend>>| DGF
        TFL -->|<<include>>| LTKFL

        %% ===== RELATIONSHIPS - FREELANCER =====
        TDD -->|<<include>>| DDAU
        DDAU -->|<<extend>>| QHD
        QDA -->|<<include>>| NBBD
        NBBD -->|<<extend>>| TTD
        DGF -->|<<include>>| XCTU

        %% ===== COMMUNICATION =====
        HTN -->|<<include>>| HTNF
    end

    %% ===== STYLING =====
    style C fill:#d1e7dd,stroke:#0f5132,stroke-width:3px
    style F fill:#cfe2ff,stroke:#084298,stroke-width:3px

    %% ===== MVP SCOPE NOTES =====
    %% Removed for MVP:
    %% - Milestone management (chỉ final payment)
    %% - Portfolio upload
    %% - Skill verification
    %% - Invite system
    %% - Dispute resolution
    %% - Withdrawal system
    %% - Notification system (chỉ có real-time chat)
    %% - Review categories (chỉ rating 1-5 + comment)
    %% - Electronic signature
