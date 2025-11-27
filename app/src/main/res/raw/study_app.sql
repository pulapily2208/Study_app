PRAGMA foreign_keys = ON;

CREATE TABLE IF NOT EXISTS hoc_ky (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    ten_hoc_ky TEXT,
    nam_hoc TEXT
);

CREATE TABLE IF NOT EXISTS khoa (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    ten_khoa TEXT NOT NULL,
    dia_chi TEXT
);

CREATE TABLE IF NOT EXISTS hoc_phan_tu_chon (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    ten_nhom TEXT,
    tong_tin_chi INTEGER,
    ghi_chu TEXT
);

CREATE TABLE IF NOT EXISTS mon_hoc (
    ma_hp TEXT PRIMARY KEY,
    ten_hp TEXT NOT NULL,
    so_tin_chi INTEGER,
    so_tiet_ly_thuyet INTEGER,
    so_tiet_thuc_hanh INTEGER,
    nhom_tu_chon TEXT,
    hoc_ky INTEGER,
    loai_hp TEXT,
    khoa_id INTEGER,
    giang_vien TEXT,
    phong_hoc TEXT,
    ngay_bat_dau TEXT,
    ngay_ket_thuc TEXT,
    gio_bat_dau TEXT,
    gio_ket_thuc TEXT,
    ghi_chu TEXT,
    so_tuan INTEGER,
    color_tag TEXT DEFAULT '#448184',
    FOREIGN KEY (khoa_id) REFERENCES khoa(id)
);

CREATE TABLE IF NOT EXISTS hoc_phan_tien_quyet (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    ma_hp TEXT,
    ma_hp_tien_quyet TEXT,
    UNIQUE(ma_hp, ma_hp_tien_quyet),
    FOREIGN KEY (ma_hp) REFERENCES mon_hoc(ma_hp),
    FOREIGN KEY (ma_hp_tien_quyet) REFERENCES mon_hoc(ma_hp)
);

-- Người dùng
CREATE TABLE IF NOT EXISTS users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    email TEXT UNIQUE,
    username TEXT UNIQUE,
    password_hash TEXT,
    display_name TEXT,
    role TEXT DEFAULT 'student',
    timezone TEXT,
    created_at INTEGER,
    updated_at INTEGER,
    is_active INTEGER DEFAULT 1
);

-- Deadline / Task
CREATE TABLE IF NOT EXISTS deadline (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    tieu_de TEXT,
    noi_dung TEXT,
    ngay_bat_dau TEXT,
    ngay_ket_thuc TEXT,
    completed INTEGER DEFAULT 0,
    ma_hp TEXT,
    FOREIGN KEY (ma_hp) REFERENCES mon_hoc(ma_hp) ON DELETE SET NULL
);

-- Notes
CREATE TABLE IF NOT EXISTS notes (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    ma_hp TEXT,
    title TEXT,
    body TEXT,
    pinned INTEGER DEFAULT 0,
    color_tag TEXT,
    created_at INTEGER,
    updated_at INTEGER,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (ma_hp) REFERENCES mon_hoc(ma_hp) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS note_images (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    note_id INTEGER NOT NULL,
    image_path TEXT NOT NULL,
    created_at INTEGER,
    FOREIGN KEY (note_id) REFERENCES notes(id) ON DELETE CASCADE
);

-- Attachments (generic link)
CREATE TABLE IF NOT EXISTS attachments (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    owner_type TEXT NOT NULL,
    owner_id INTEGER NOT NULL,
    file_path TEXT,
    file_name TEXT,
    mime_type TEXT,
    size INTEGER,
    uploaded_at INTEGER
);

-- Timetable / Sessions
CREATE TABLE IF NOT EXISTS timetable_sessions (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    ma_hp TEXT,
    weekday INTEGER,
    start_time TEXT,
    end_time TEXT,
    room TEXT,
    teacher TEXT,
    start_date INTEGER,
    end_date INTEGER,
    repeat_week_pattern TEXT,
    color_tag TEXT,
    note TEXT,
    created_at INTEGER,
    updated_at INTEGER,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (ma_hp) REFERENCES mon_hoc(ma_hp) ON DELETE SET NULL
);

-- Enrollments / Grades
CREATE TABLE IF NOT EXISTS enrollments (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    ma_hp TEXT NOT NULL,
    hoc_ky INTEGER,
    grade REAL,
    credit INTEGER,
    grade_type TEXT,
    created_at INTEGER,
    updated_at INTEGER,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (ma_hp) REFERENCES mon_hoc(ma_hp) ON DELETE CASCADE,
    FOREIGN KEY (hoc_ky) REFERENCES hoc_ky(id) ON DELETE SET NULL
);

-- Notification schedules
CREATE TABLE IF NOT EXISTS notification_schedules (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    related_table TEXT,
    related_id INTEGER,
    scheduled_time INTEGER,
    channel TEXT,
    repeat_rule TEXT,
    sent INTEGER DEFAULT 0,
    last_sent_at INTEGER,
    created_at INTEGER,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Mapping môn học -> nhóm tuỳ chọn (nếu 1 môn thuộc nhiều nhóm)
CREATE TABLE IF NOT EXISTS mon_hoc_tu_chon_map (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    ma_hp TEXT NOT NULL,
    hoc_phan_tu_chon_id INTEGER NOT NULL,
    UNIQUE(ma_hp, hoc_phan_tu_chon_id),
    FOREIGN KEY (ma_hp) REFERENCES mon_hoc(ma_hp) ON DELETE CASCADE,
    FOREIGN KEY (hoc_phan_tu_chon_id) REFERENCES hoc_phan_tu_chon(id) ON DELETE CASCADE
);

-- =====================
-- SEED DATA: DỮ LIỆU GỐC
-- =====================
-- User
INSERT OR IGNORE INTO users (id, email, username, password_hash, display_name, role, timezone, created_at, updated_at, is_active)
VALUES (1, 'local@local', 'local', '', 'Local User', 'student', 'UTC', strftime('%s','now'), strftime('%s','now'), 1);

-- 1. BẢNG HỌC KỲ
INSERT OR IGNORE INTO hoc_ky (id, ten_hoc_ky, nam_hoc) VALUES
 (1, 'Học kỳ 1', '2023-2024'),
 (2, 'Học kỳ 2', '2023-2024'),
 (3, 'Học kỳ 3', '2024-2025'),
 (4, 'Học kỳ 4', '2024-2025'),
 (5, 'Học kỳ 5', '2025-2026'),
 (6, 'Học kỳ 6', '2025-2026'),
 (7, 'Học kỳ 7', '2026-2027'),
 (8, 'Học kỳ 8', '2026-2027 '),
 (9, 'Học kỳ hè năm 1', '2023-2024'),
 (10, 'Học kỳ hè năm 2', '2024-2025'),
 (11, 'Học kỳ hè năm 3', '2025-2026'),
 (12, 'Học kỳ hè năm 4', '2026-2027'),

-- 2. BẢNG KHOA / BỘ MÔN
INSERT OR IGNORE INTO khoa (id, ten_khoa, dia_chi) VALUES
 (1, 'Công nghệ thông tin', 'Nhà A'),
 (2, 'Toán - Tin', 'Nhà B'),
 (3, 'Triết học', 'Nhà C'),
 (4, 'GD Quốc phòng', 'Nhà D'),
 (5, 'GD Thể chất', 'Nhà E'),
 (6, 'Tiếng Anh', 'Nhà F'),
 (7, 'LLCT & GDCD', 'Nhà G'),
 (8, 'Môn chung toàn trường', 'Nhà H'),
 (9, 'Tâm lí Giáo dục', 'Nhà I');

-- 3. BẢNG NHÓM TỰ CHỌN
INSERT OR IGNORE INTO hoc_phan_tu_chon (id, ten_nhom, tong_tin_chi, ghi_chu) VALUES
 (1, 'Kiến thức đại cương tự chọn', 2, 'Chọn 1 môn (2 tín chỉ)'),
 (2, 'Ngoại ngữ', 6, 'Chọn để tích lũy 6 tín chỉ'),
 (3, 'Giáo dục thể chất 3', 1, 'Chọn 1 môn thể chất'),
 (4, 'Giáo dục thể chất 4', 1, 'Chọn 1 môn thể chất'),
 (5, 'Kiến thức bổ trợ/Chuyên ngành 1', 13, 'Tích lũy 13 tín chỉ (HK3-6)'),
 (6, 'Kiến thức chuyên ngành 2', 12, 'Tích lũy 12 tín chỉ (HK7-8)'),
 (7, 'Tốt nghiệp', 10, 'Chọn Khóa luận hoặc các chuyên đề');

-- 4. BẢNG MÔN HỌC (Bulk Insert) -- ĐÃ LOẠI BỎ [cite_start] và chú thích lạ khỏi INSERT
-- HỌC KỲ 1
INSERT OR IGNORE INTO mon_hoc (ma_hp, ten_hp, so_tin_chi, so_tiet_ly_thuyet, so_tiet_thuc_hanh, nhom_tu_chon, hoc_ky, loai_hp, khoa_id) VALUES
 ('DEFE2051', 'HP3: Quân sự chung', 1, 30, 0, NULL, 1, 'Bắt buộc', 4),
 ('DEFE2062', 'HP4: Kỹ thuật chiến đấu bộ binh và chiến thuật', 2, 60, 0, NULL, 1, 'Bắt buộc', 4),
 ('MATH137', 'Thống kê xã hội học', 2, 30, 0, NULL, 1, 'Bắt buộc', 2),
 ('PHIS105', 'Triết học Mác-Lênin', 3, 36, 0, NULL, 1, 'Bắt buộc', 3),
 ('PHYE151', 'Giáo dục thể chất 2', 1, 28, 0, NULL, 1, 'Bắt buộc', 5),
 ('PSYC101', 'Tâm lí học giáo dục', 4, 45, 0, NULL, 1, 'Bắt buộc', 9),
 ('COMM106', 'Tiếng Việt thực hành', 2, 30, 0, 1, 1, 'Tự chọn', 8),
 ('COMM107', 'Nghệ thuật đại cương', 2, 30, 0, 1, 1, 'Tự chọn', 8),
 ('COMP103', 'Tin học đại cương', 2, 30, 0, 1, 1, 'Tự chọn', 1),
 ('ENGL103', 'Tiếng Anh 1-A1', 3, 45, 0, 2, 1, 'Tự chọn', 6),

-- HỌC KỲ 2
 ('ENGL104', 'Tiếng Anh 1-A2', 3, 45, 0, 2, 2, 'Tự chọn', 6),
 ('COMM104', 'Nhập môn KHTN và Công nghệ', 3, 36, 0, NULL, 2, 'Bắt buộc', 8),
 ('DEFE105', 'HP1: Đường lối và AN của ĐCSVN', 3, 45, 0, NULL, 2, 'Bắt buộc', 4),
 ('DEFE106', 'HP2: Công tác QP và AN', 2, 30, 0, NULL, 2, 'Bắt buộc', 4),
 ('MATH159', 'Phép tính vi tích phân hàm một biến', 3, 45, 0, NULL, 2, 'Bắt buộc', 2),
 ('MATH160', 'Nhập môn lý thuyết ma trận', 2, 30, 0, NULL, 2, 'Bắt buộc', 2),
 ('POLI106', 'Chủ nghĩa xã hội khoa học', 2, 20, 0, NULL, 2, 'Bắt buộc', 7),
 ('ENGL105', 'Tiếng Anh 2-A1', 3, 45, 0, 2, 2, 'Tự chọn', 6),
 ('ENGL106', 'Tiếng Anh 2-A2', 3, 45, 0, 2, 2, 'Tự chọn', 6),

-- HỌC KỲ 3
 ('COMP122', 'Toán rời rạc', 3, 45, 0, NULL, 3, 'Bắt buộc', 1),
 ('COMP211', 'Cơ sở dữ liệu', 3, 45, 0, NULL, 3, 'Bắt buộc', 1),
 ('COMP262', 'Kiến trúc máy tính', 3, 45, 0, NULL, 3, 'Bắt buộc', 1),
 ('COMP267', 'Lập trình hướng đối tượng', 4, 60, 0, NULL, 3, 'Bắt buộc', 1),
 ('COMP275', 'Nền tảng phát triển web', 3, 45, 0, NULL, 3, 'Bắt buộc', 1),
 ('PHYE150', 'Giáo dục thể chất 1', 1, 28, 0, NULL, 3, 'Bắt buộc', 5),
 ('POLI104', 'Kinh tế chính trị Mác - Lênin', 2, 20, 0, NULL, 3, 'Bắt buộc', 7),
 ('POLI202', 'Tư tưởng Hồ Chí Minh', 2, 20, 0, NULL, 3, 'Bắt buộc', 7),
 ('COMP231', 'PLuật về Đạo đức nghề nghiệp trong CNTT', 2, 30, 0, 5, 3, 'Tự chọn', 1),
 ('PHYE250BB', 'Giáo dục thể chất 3 (Bóng bàn)', 1, 28, 0, 3, 3, 'Tự chọn', 5),
 ('PHYE250BC', 'Giáo dục thể chất 3 (Bóng chuyền)', 1, 28, 0, 3, 3, 'Tự chọn', 5),
 ('PHYE250BD', 'Giáo dục thể chất 3 (Bóng đá)', 1, 28, 0, 3, 3, 'Tự chọn', 5),
 ('PHYE250BN', 'Giáo dục thể chất 3 (Bóng ném)', 1, 28, 0, 3, 3, 'Tự chọn', 5),
 ('PHYE250BR', 'Giáo dục thể chất 3 (Bóng rổ)', 1, 28, 0, 3, 3, 'Tự chọn', 5),
 ('PHYE250CL', 'Giáo dục thể chất 3 (Cầu lông)', 1, 28, 0, 3, 3, 'Tự chọn', 5),
 ('PHYE250DC', 'Giáo dục thể chất 3 (Đá cầu)', 1, 28, 0, 3, 3, 'Tự chọn', 5),
 ('PHYE250DK', 'Giáo dục thể chất 3 (Điền kinh)', 1, 28, 0, 3, 3, 'Tự chọn', 5),
 ('PHYE250KV', 'Giáo dục thể chất 3 (Khiêu vũ)', 1, 28, 0, 3, 3, 'Tự chọn', 5),
 ('PHYE250TD', 'Giáo dục thể chất 3 (Thể dục nhịp điệu)', 1, 28, 0, 3, 3, 'Tự chọn', 5),
 ('PHYE250V', 'Giáo dục thể chất 3 (Võ thuật)', 1, 28, 0, 3, 3, 'Tự chọn', 5),

-- HỌC KỲ 4
 ('COMP270', 'Hệ quản trị CSDL', 3, 45, 0, NULL, 4, 'Bắt buộc', 1),
 ('COMP271', 'Cấu trúc dữ liệu và giải thuật', 4, 60, 0, NULL, 4, 'Bắt buộc', 1),
 ('COMP272', 'Lập trình ứng dụng với Java', 3, 45, 0, NULL, 4, 'Bắt buộc', 1),
 ('COMP273', 'Mạng máy tính', 3, 45, 0, NULL, 4, 'Bắt buộc', 1),
 ('POLI204', 'Lịch sử Đảng Cộng sản Việt Nam', 2, 20, 0, NULL, 4, 'Bắt buộc', 7),
 ('COMP274', 'Đồ họa máy tính', 3, 45, 0, 5, 4, 'Tự chọn', 1),
 ('COMP276', 'Nhập môn xử lý ảnh', 3, 45, 0, 5, 4, 'Tự chọn', 1),
 ('PHYE251BB', 'Giáo dục thể chất 4 (Bóng bàn)', 1, 28, 0, 4, 4, 'Tự chọn', 5),
 ('PHYE251BC', 'Giáo dục thể chất 4 (Bóng chuyền)', 1, 28, 0, 4, 4, 'Tự chọn', 5),
 ('PHYE251BD', 'Giáo dục thể chất 4 (Bóng đá)', 1, 28, 0, 4, 4, 'Tự chọn', 5),
 ('PHYE251BN', 'Giáo dục thể chất 4 (Bóng ném)', 1, 28, 0, 4, 4, 'Tự chọn', 5),
 ('PHYE251BR', 'Giáo dục thể chất 4 (Bóng rổ)', 1, 28, 0, 4, 4, 'Tự chọn', 5),
 ('PHYE251CL', 'Giáo dục thể chất 4 (Cầu lông)', 1, 28, 0, 4, 4, 'Tự chọn', 5),
 ('PHYE251DC', 'Giáo dục thể chất 4 (Đá cầu)', 1, 28, 0, 4, 4, 'Tự chọn', 5),
 ('PHYE251DK', 'Giáo dục thể chất 4 (Điền kinh)', 1, 28, 0, 4, 4, 'Tự chọn', 5),
 ('PHYE251KV', 'Giáo dục thể chất 4 (Khiêu vũ)', 1, 28, 0, 4, 4, 'Tự chọn', 5),
 ('PHYE251TD1', 'Giáo dục thể chất 4 (Thể dục nhịp điệu)', 1, 28, 0, 4, 4, 'Tự chọn', 5),
 ('PHYE251V1', 'Giáo dục thể chất 4 (Võ thuật)', 1, 28, 0, 4, 4, 'Tự chọn', 5),

-- HỌC KỲ 5
 ('COMP301', 'Phân tích thiết kế hệ thống', 3, 45, 0, NULL, 5, 'Bắt buộc', 1),
 ('COMP304', 'Nhập môn An toàn thông tin', 2, 30, 0, NULL, 5, 'Bắt buộc', 1),
 ('COMP261', 'Trí tuệ nhân tạo', 3, 45, 0, NULL, 5, 'Bắt buộc', 1),
 ('COMP300', 'Nhập môn Công nghệ phần mềm', 3, 45, 0, NULL, 5, 'Bắt buộc', 1),
 ('COMP302', 'Phân tích và thiết kế thuật toán', 3, 45, 0, NULL, 5, 'Bắt buộc', 1),
 ('COMP303', 'Quản trị mạng', 3, 45, 0, NULL, 5, 'Bắt buộc', 1),
 ('COMP306', 'Phát triển phần mềm cho thiết bị di động', 3, 45, 0, 5, 5, 'Tự chọn', 1),
 ('COMP307', 'Công nghệ web', 3, 45, 0, 5, 5, 'Tự chọn', 1),
 ('COMP356', 'Mạng máy tính nâng cao', 2, 30, 0, 5, 5, 'Tự chọn', 1),

-- HỌC KỲ 6
 ('COMP360', 'Thực hành dự án', 3, 45, 0, NULL, 6, 'Bắt buộc', 1),
 ('COMP309', 'Phần mềm mã nguồn mở', 3, 45, 0, 5, 6, 'Tự chọn', 1),
 ('COMP355', 'Các vấn đề hiện đại CNTT', 2, 30, 0, 5, 6, 'Tự chọn', 1),
 ('COMP357', 'Cơ sở dữ liệu tiên tiến', 3, 45, 0, 5, 6, 'Tự chọn', 1),
 ('COMP358', 'Dữ liệu lớn (BigData)', 3, 45, 0, 5, 6, 'Tự chọn', 1),
 ('COMP361', 'Lập trình trực quan C#', 3, 45, 0, 6, 6, 'Tự chọn', 1),
 ('COMP362', 'Kiểm thử và đảm bảo CL phần mềm', 3, 45, 0, 6, 6, 'Tự chọn', 1),

-- HỌC KỲ 7
 ('COMP2463', 'Khai phá dữ liệu', 3, 45, 0, 6, 7, 'Tự chọn', 1),
 ('COMP363', 'Thu thập và phân tích yêu cầu', 3, 45, 0, 6, 7, 'Tự chọn', 1),
 ('COMP364', 'Thiết kế giao diện người dùng', 3, 45, 0, 6, 7, 'Tự chọn', 1),
 ('COMP365', 'Quản lí dự án công nghệ thông tin', 3, 45, 0, 6, 7, 'Tự chọn', 1),
 ('COMP366', 'Truyền thông đa phương tiện', 3, 45, 0, 6, 7, 'Tự chọn', 1),
 ('COMP367', 'Phát triển phần mềm linh hoạt', 3, 45, 0, 6, 7, 'Tự chọn', 1),
 ('COMP368', 'Các hệ thống thương mại điện tử', 3, 45, 0, 6, 7, 'Tự chọn', 1),
 ('COMP369', 'Hệ thống thông tin doanh nghiệp', 3, 45, 0, 6, 7, 'Tự chọn', 1),
 ('COMP370', 'Lập trình nâng cao (Python)', 3, 45, 0, 6, 7, 'Tự chọn', 1),
 ('COMP371', 'Xác suất thống kê ứng dụng', 3, 45, 0, 6, 7, 'Tự chọn', 1),
 ('COMP373', 'Học máy', 3, 45, 0, 6, 7, 'Tự chọn', 1),

-- HỌC KỲ 8
 ('COMP380', 'Thực tập công nghệ 1', 2, 30, 0, NULL, 8, 'Bắt buộc', 1),
 ('COMP381', 'Thực tập công nghệ 2', 4, 60, 0, NULL, 8, 'Bắt buộc', 1),
 ('COMP374', 'Lí thuyết độ phức tạp', 3, 45, 0, 6, 8, 'Tự chọn', 1),
 ('COMP375', 'Lập trình song song và phân tán', 3, 45, 0, 6, 8, 'Tự chọn', 1),
 ('COMP376', 'Tối ưu hoá', 3, 45, 0, 6, 8, 'Tự chọn', 1),
 ('COMP377', 'Tin sinh học', 3, 45, 0, 6, 8, 'Tự chọn', 1),
 ('COMP378', 'Xử lí ngôn ngữ tự nhiên', 3, 45, 0, 6, 8, 'Tự chọn', 1),
 ('COMP385', 'Khóa luận tốt nghiệp', 10, 150, 0, 7, 8, 'Tự chọn', 1),
 ('COMP382', 'Chuyên đề TN Công nghệ phần mềm', 5, 75, 0, 7, 8, 'Tự chọn', 1),
 ('COMP383', 'Chuyên đề TN Dự án công nghệ Khoa học', 5, 75, 0, 7, 8, 'Tự chọn', 1),
 ('COMP384', 'Chuyên đề TN Khoa học dữ liệu', 5, 75, 0, 7, 8, 'Tự chọn', 1),

-- HỌC KỲ HÈ (Năm 2)
 ('COMP106', 'Nhập môn Khoa học máy tính', 2, 30, 0, NULL, 9, 'Bắt buộc', 1);

-- 5. BẢNG HỌC PHẦN TIÊN QUYẾT (Bulk Insert)
INSERT OR IGNORE INTO hoc_phan_tien_quyet (ma_hp, ma_hp_tien_quyet) VALUES
 ('COMP301','COMP211'),
 ('COMP304','COMP103'), ('COMP304','COMP273'),
 ('COMP267','COMP106'),
 ('POLI104','PHIS105'),
 ('COMP271','COMP267'),
 ('COMP272','COMP267'),
 ('COMP274','COMP267'),
 ('COMP276','COMP267'),
 ('COMP300','COMP267'), ('COMP300','COMP271'), ('COMP300','COMP273'),
 ('COMP306','COMP267'),
 ('COMP307','COMP275'),
 ('COMP356','COMP273'),
 ('COMP360','COMP272'), ('COMP360','COMP300'),
 ('COMP309','COMP267'),
 ('COMP357','COMP211'),
 ('COMP361','COMP267'),
 ('COMP362','COMP301'),
 ('COMP363','COMP267'),
 ('COMP365','COMP267'), ('COMP365','COMP300'), ('COMP365','COMP301'),
 ('COMP367','COMP300'),
 ('COMP369','COMP211'),
 ('COMP370','COMP106'),
 ('COMP371','MATH137'),
 ('COMP375','COMP122'), ('COMP375','COMP271'), ('COMP375','COMP302');