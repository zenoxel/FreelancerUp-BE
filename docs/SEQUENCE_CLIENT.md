sequenceDiagram
    participant C as Client
    participant S as System
    participant F as Freelancer

    %% ===== PHASE 1: PRE-PROJECT (Tìm kiếm & Khám phá) =====
    rect rgb(240, 248, 255)
        Note over C,F: PHASE 1: TÌM KIẾM & KHÁM PHÁ
        C->>S: Tìm kiếm Freelancer (theo skill, rating, budget)
        S->>C: Hiển thị danh sách Freelancers
        C->>S: Xem Profile/Portfolio/Rating
        S->>C: Hiển thị chi tiết Freelancer
    end

    %% ===== PHASE 2: POST PROJECT & BIDDING =====
    rect rgb(250, 255, 240)
        Note over C,F: PHASE 2: ĐĂNG DỰ ÁN & ĐẤU THẦU
        C->>S: Đăng Project (brief, budget, deadline)
        S->>S: Đăng Public

        %% Freelancers bid on project
        loop Multiple Freelancers
            F->>S: Bid Project (proposal, giá, timeline)
            S->>C: Thông báo có Bid mới
        end

        %% Client reviews bids
        C->>S: Xem danh sách Bids
        loop Xem Profile từng Freelancer
            C->>S: Xem Profile Freelancer
            S->>C: Hiển thị chi tiết
        end

        %% Chat trao đổi
        loop Chat với Freelancers tiềm năng
            C->>S: Gửi tin nhắn
            S->>F: Thông báo tin nhắn
            F->>S: Trả lời tin nhắn
            S->>C: Hiển thị tin nhắn
        end

        C->>S: Chọn Freelancer
    end

    %% ===== PHASE 3: CONTRACT & MILESTONE SETUP =====
    rect rgb(255, 248, 220)
        Note over C,F: PHASE 3: HỢP ĐỒNG & ESCROW
        C->>S: Gửi Offer & Nạp tiền vào Escrow
        S->>S: Xác nhận Escrow funded
        S->>F: Thông báo Offer

        F->>S: Chấp nhận Offer
        S->>S: Tự động tạo Contract
        S->>C: Thông báo Bắt đầu Dự án
        S->>F: Thông báo Bắt đầu Dự án
    end

    %% ===== PHASE 4: PROJECT EXECUTION =====
    rect rgb(255, 240, 245)
        Note over C,F: PHASE 4: THỰC HIỆN DỰ ÁN

        %% Communication throughout project
        par Communication liên tục
            loop Chat/Trao đổi
                C->>S: Gửi tin nhắn/câu hỏi
                S->>F: Thông báo
                F->>S: Trả lời
                S->>C: Hiển thị
            end
        and Progress Updates
            loop Status updates
                F->>S: Update tiến độ
                S->>C: Thông báo progress
            end
        end
    end

    %% ===== PHASE 5: COMPLETION & PAYMENT =====
    rect rgb(240, 255, 240)
        Note over C,F: PHASE 6: HOÀN TẤT & THANH TOÁN
        C->>S: Đánh dấu Project Complete
        S->>S: Release Final Payment từ Escrow
        S->>F: Thông báo Final Payment

        F->>S: Withdraw Funds (rút về tài khoản)
        S->>S: Xử lý Withdraw
        S->>F: Xác nhận Withdraw thành công
    end

    %% ===== PHASE 7: REVIEW & REPUTATION =====
    rect rgb(230, 240, 255)
        Note over C,F: PHASE 7: ĐÁNH GIÁ & UY TÍN
        C->>S: Đánh giá Freelancer (rating, review)
        S->>F: Thông báo có review mới
        S->>S: Cập nhật Reputation Score của F
        S->>C: Hiển thị final stats
        S->>F: Hiển thị final stats
    end

    %% ===== PHASE 7: POST-PROJECT =====
    rect rgb(245, 245, 245)
        Note over C,F: PHASE 8: SAU DỰ ÁN
        S->>C: Request Feedback (về Platform)
        opt Client phản hồi
            C->>S: Submit Platform Feedback
        end

        S->>F: Request Feedback (về Platform)
        opt Freelancer phản hồi
            F->>S: Submit Platform Feedback
        end

        S->>S: Archive Project
        S->>C: Project archived notification
        S->>F: Project archived notification
    end

    %% ===== MVP SCOPE NOTES =====
    %% Removed from MVP:
    %% - Invite system
    %% - Milestone payments (chỉ final payment)
    %% - Dispute resolution
    %% - Revision workflow (chỉ approve/reject đơn giản)
    %% - Platform feedback
