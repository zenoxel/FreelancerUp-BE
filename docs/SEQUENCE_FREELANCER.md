sequenceDiagram
    participant F as Freelancer
    participant S as System
    participant C as Client

    %% ===== PHASE 0: PROFILE SETUP (Before Bidding) =====
    rect rgb(245, 245, 250)
        Note over F,S: PHASE 0: THIẾT LẬP PROFILE
        F->>S: Hoàn thiện Profile (avatar, bio, hourly rate)
        F->>S: Upload Portfolio (projects, case studies)
        F->>S: Thêm Skills (technical, soft skills)
        F->>S: Verify Profile (identity, skills, certificates)
        S->>S: Gửi yêu cầu verification
        S->>A: Thông báo cần phê duyệt
        A->>S: Approve/Reject verification
        S->>F: Thông báo kết quả verification
        S->>S: Cập nhật Profile status (Verified)
    end

    %% ===== PHASE 1: SEARCH & DISCOVERY =====
    rect rgb(240, 248, 255)
        Note over F,C: PHASE 2: TÌM KIẾM DỰ ÁN
        F->>S: Tìm kiếm Projects (filter by skill, budget, type, deadline)
        S->>F: Hiển thị danh sách Projects

        loop Xem chi tiết Projects
            F->>S: Xem chi tiết Project (description, requirements, budget)
            S->>F: Hiển thị full details
            F->>S: Xem Client Profile & Rating
            S->>F: Hiển thị Client reputation
        end

        opt Lưu Projects tiềm năng
            F->>S: Bookmark/Save Project
            S->>F: Xác nhận đã lưu
        end
    end

    %% ===== PHASE 2: BIDDING =====
    rect rgb(250, 255, 240)
        Note over F,C: PHASE 3: ĐẤU THẦU DỰ ÁN
        F->>S: Bid Project (proposal, price, timeline)
        S->>C: Thông báo có Bid mới

        opt Client muốn trao đổi
            C->>S: Gửi tin nhắn cho Freelancer
            S->>F: Thông báo tin nhắn
            F->>S: Trả lời tin nhắn
            S->>C: Hiển thị tin nhắn
        end

        C->>S: Chọn Freelancer
    end

    %% ===== PHASE 3: CONTRACT ACCEPTANCE =====
    rect rgb(255, 248, 220)
        Note over F,S: PHASE 3: CHẤP NHẬN HỢP ĐỒNG
        S->>F: Thông báo có Offer mới
        F->>S: Xem chi tiết Offer (budget, scope, deadline)
        F->>S: Xem Milestones (số lượng, amount, deadlines)

        opt Freelancer cần điều chỉnh
            F->>S: Request adjustment (milestone/timeline)
            S->>C: Thông báo request
            C->>S: Accept/Reject adjustment
            S->>F: Thông báo kết quả
        end

        F->>S: Chấp nhận Offer
        S->>S: Tạo Contract (electronic)
        S->>F: Contract ready for signature
        F->>S: Ký Contract (electronic signature)
        S->>S: Contract activated
        S->>C,F: Thông báo Bắt đầu Dự án
    end

    %% ===== PHASE 4: PROJECT EXECUTION =====
    rect rgb(255, 240, 245)
        Note over F,C: PHASE 5: THỰC HIỆN DỰ ÁN

        %% Ongoing communication
        par Communication liên tục
            loop Chat/Trao đổi
                C->>S: Gửi câu hỏi/clarification
                S->>F: Thông báo
                F->>S: Trả lời
                S->>C: Hiển thị
            end
        and Progress Updates
            loop Update tiến độ
                F->>S: Update status/progress (% completed)
                S->>C: Thông báo progress
            end
        end
    end

    %% ===== PHASE 6: COMPLETION & PAYMENT =====
    rect rgb(240, 255, 240)
        Note over F,S: PHASE 6: HOÀN TẤT & THANH TOÁN
        F->>S: Submit Final Deliverable
        S->>C: Request final approval
        C->>S: Approve Final
        S->>S: Release Final Payment từ Escrow
        S->>F: Thông báo Final Payment received
        S->>F: Cập nhật Balance
    end

    %% ===== PHASE 7: REVIEW & REPUTATION =====
    rect rgb(230, 240, 255)
        Note over F,C: PHASE 7: ĐÁNH GIÁ & UY TÍN
        C->>S: Đánh giá Freelancer (rating, review)
        S->>F: Thông báo có review mới
        S->>S: Cập nhật Reputation Score của Freelancer
        S->>F: Hiển thị final stats (completed projects, total earned, rating)
    end

    %% ===== PHASE 8: POST-PROJECT =====
    rect rgb(245, 245, 245)
        Note over F,S: PHASE 8: SAU DỰ ÁN
        F->>S: Update Skills (nếu học được skill mới)
        S->>S: Cập nhật Skill list
        S->>S: Archive Project
        S->>F: Project archived notification
        S->>F: Update statistics (total projects, earnings)
    end

    %% ===== MVP SCOPE NOTES =====
    %% Removed from MVP:
    %% - Profile verification
    %% - Portfolio upload
    %% - Invite system
    %% - Milestone submissions (chỉ final deliverable)
    %% - Dispute resolution
    %% - Withdrawal system
    %% - Two-way reviews (chỉ Client review Freelancer)
