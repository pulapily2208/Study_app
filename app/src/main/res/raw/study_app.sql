PRAGMA foreign_keys = ON;
-- =====================
-- SCHEMA: BẢN GỐC (giữ nguyên/cải tiến)
-- =====================

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

-- =====================
-- SCHEMA: BẢNG MỞ RỘNG CHO ỨNG DỤNG
-- =====================

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

-- Bảng Weeks (Tuần học)
CREATE TABLE IF NOT EXISTS Weeks (
    week_id INTEGER PRIMARY KEY AUTOINCREMENT,
    ma_hp TEXT, -- Thay subject_id bằng ma_hp để liên kết với mon_hoc
    start_date TEXT NOT NULL,
    num_of_weeks INTEGER NOT NULL,
    end_date TEXT,
    FOREIGN KEY (ma_hp) REFERENCES mon_hoc(ma_hp) ON DELETE CASCADE
);

-- Bảng Icons
CREATE TABLE IF NOT EXISTS Icons (
    icon_id INTEGER PRIMARY KEY AUTOINCREMENT,
    icon_name TEXT,
    icon_path TEXT
);

-- Bảng Colors
CREATE TABLE IF NOT EXISTS Colors (
    color_id INTEGER PRIMARY KEY AUTOINCREMENT,
    color_name TEXT,
    color_code TEXT
);

-- Deadline / Task (CẬP NHẬT THEO YÊU CẦU)
CREATE TABLE IF NOT EXISTS Deadlines (
    deadline_id INTEGER PRIMARY KEY AUTOINCREMENT,
    week_id INTEGER NOT NULL,
    title TEXT NOT NULL,
    note TEXT,
    start_datetime TEXT NOT NULL, -- 'YYYY-MM-DD HH:MM:SS'
    end_datetime TEXT NOT NULL,
    
    repeat_type TEXT DEFAULT 'once', -- 'once', 'weekly', 'custom'
    repeat_days TEXT DEFAULT NULL,   -- Ví dụ: 'Mon,Tue,Wed'
    
    completed INTEGER DEFAULT 0,     -- 0 = False, 1 = True
    
    icon_id INTEGER,
    color_id INTEGER,
    
    FOREIGN KEY (week_id) REFERENCES Weeks(week_id) ON DELETE CASCADE,
    FOREIGN KEY (icon_id) REFERENCES Icons(icon_id) ON DELETE SET NULL,
    FOREIGN KEY (color_id) REFERENCES Colors(color_id) ON DELETE SET NULL
);

-- Bảng Reminders
CREATE TABLE IF NOT EXISTS Reminders (
    reminder_id INTEGER PRIMARY KEY AUTOINCREMENT,
    deadline_id INTEGER NOT NULL,
    
    before_start_minutes INTEGER DEFAULT 0,  -- Nhắc trước khi bắt đầu
    before_end_minutes INTEGER DEFAULT 0,    -- Nhắc trước khi đến hạn
    
    description TEXT,
    
    FOREIGN KEY (deadline_id) REFERENCES Deadlines(deadline_id) ON DELETE CASCADE
);

-- Bảng Notifications
CREATE TABLE IF NOT EXISTS Notifications (
    notification_id INTEGER PRIMARY KEY AUTOINCREMENT,
    deadline_id INTEGER NOT NULL,
    time_remaining INTEGER,       -- thời gian còn lại tính theo phút
    is_sent INTEGER DEFAULT 0,    -- 0 = False, 1 = True
    
    FOREIGN KEY (deadline_id) REFERENCES Deadlines(deadline_id) ON DELETE CASCADE
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

-- Attachments (generic link)
CREATE TABLE IF NOT EXISTS attachments (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    owner_type TEXT NOT NULL, -- 'note' | 'deadline' | 'timetable'
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
-- SEED DATA: DỮ LIỆU GỐC (ĐÃ ĐƠN GIẢN HÓA)
-- =====================

-- Icons
INSERT INTO Icons (icon_name, icon_path) VALUES
('Important', 'icons/important.png'),
('Study', 'icons/study.png'),
('Work', 'icons/work.png');

-- Colors
INSERT INTO Colors (color_name, color_code) VALUES
('Red', '#FF0000'),
('Blue', '#0000FF'),
('Green', '#00FF00');

-- Học kỳ
INSERT OR IGNORE INTO hoc_ky (id, ten_hoc_ky, nam_hoc) VALUES
 (1,'Học kỳ 1','2024-2025'),
 (2,'Học kỳ 2','2024-2025'),
 (3,'Học kỳ 3','2025-2026'),
 (4,'Học kỳ 4','2025-2026'),
 (5,'Học kỳ 5','2026-2027'),
 (6,'Học kỳ 6','2026-2027'),
 (7,'Học kỳ 7','2027-2028'),
 (8,'Học kỳ 8','2027-2028'),
 (9,'Học kỳ hè','2025');

-- Khoa
INSERT OR IGNORE INTO khoa (id, ten_khoa, dia_chi) VALUES
 (1,'Công nghệ thông tin','Nhà A'),
 (2,'Toán - Tin','Nhà B'),
 (3,'Triết học','Nhà C'),
 (4,'GD Quốc phòng','Nhà D'),
 (5,'GD Thể chất','Nhà E'),
 (6,'Tiếng Anh','Nhà F'),
 (7,'LLCT & GDCD','Nhà G'),
 (8,'Môn chung toàn trường','Nhà H');

-- Nhóm tuỳ chọn
INSERT OR IGNORE INTO hoc_phan_tu_chon (id, ten_nhom, tong_tin_chi, ghi_chu) VALUES
 (1,'Nhóm thể chất',2,'Chọn 1 trong nhiều nhóm thể chất'),
 (2,'Nhóm tiếng Anh',6,'Tiếng Anh A1/A2'),
 (3,'Nhóm đồ án tốt nghiệp',10,'Khóa luận / chuyên đề TN'),
 (4,'Nhóm chuyên ngành CNTT',13,'Các môn chuyên ngành (CNTT)');

-- Mon_hoc: giữ dữ liệu gốc
INSERT OR IGNORE INTO mon_hoc (ma_hp, ten_hp, so_tin_chi, so_tiet_ly_thuyet, so_tiet_thuc_hanh, nhom_tu_chon, hoc_ky, loai_hp, khoa_id) VALUES
('COMP301','Phân tích thiết kế hệ thống',3,45,0,NULL,NULL,'Bắt buộc',1),
('COMP304','Nhập môn An toàn thông tin',2,30,0,NULL,NULL,'Bắt buộc',1),
('COMP385','Khóa luận tốt nghiệp',10,150,0,NULL,8,'Tự chọn',1),
('DEFE2051','HP3 - Quân sự chung',1,30,0,NULL,1,'Bắt buộc',4),
('DEFE2062','HP4 - Kỹ thuật chiến đấu bộ binh và chiến thuật',2,60,0,NULL,1,'Bắt buộc',4),
('MATH137','Thống kê xã hội học',2,30,0,NULL,1,'Bắt buộc',2),
('PHIS105','Triết học Mác-Lênin',3,36,0,NULL,1,'Bắt buộc',3),
('PHYE151','Giáo dục thể chất 2',1,28,0,NULL,1,'Bắt buộc',5),
('PSYC101','Tâm lí học giáo dục',4,45,0,NULL,1,'Bắt buộc',8),
('COMM106','Tiếng Việt thực hành',2,30,0,'Nhóm chung toàn trường',1,'Tự chọn',8),
('COMM107','Nghệ thuật đại cương',2,30,0,'Nhóm chung toàn trường',1,'Tự chọn',8),
('COMP103','Tin học đại cương',2,30,0,'Nhóm CNTT',1,'Tự chọn',1),
('ENGL103','Tiếng Anh 1 - A1',3,45,0,NULL,1,'Tự chọn',6),
('ENGL104','Tiếng Anh 1 - A2',3,45,0,NULL,1,'Tự chọn',6),
('COMM104','Nhập môn KHTN và Công nghệ',3,36,0,NULL,2,'Bắt buộc',8),
('DEFE105','HP1 - Đường lối QP và AN của ĐCSVN',3,45,0,NULL,2,'Bắt buộc',4),
('DEFE106','HP2 - Công tác QP và AN',2,30,0,NULL,2,'Bắt buộc',4),
('MATH159','Phép tính vi tích phân hàm một biến',3,45,0,NULL,2,'Bắt buộc',2),
('MATH160','Nhập môn lý thuyết ma trận',2,30,0,NULL,2,'Bắt buộc',2),
('POLI106','Chủ nghĩa xã hội khoa học',2,20,0,NULL,2,'Bắt buộc',7),
('ENGL105','Tiếng Anh 2 - A1',3,45,0,NULL,2,'Tự chọn',6),
('ENGL106','Tiếng Anh 2 - A2',3,45,0,NULL,2,'Tự chọn',6),
('PHYE250BB','Giáo dục thể chất 3 (Bóng bàn)',1,28,0,'Thể chất',3,'Tự chọn',5),
('PHYE250BC','Giáo dục thể chất 3 (Bóng chuyền)',1,28,0,'Thể chất',3,'Tự chọn',5),
('PHYE250BD','Giáo dục thể chất 3 (Bóng đá)',1,28,0,'Thể chất',3,'Tự chọn',5),
('COMP122','Toán rời rạc',3,45,0,NULL,3,'Bắt buộc',2),
('COMP211','Cơ sở dữ liệu',3,45,0,NULL,3,'Bắt buộc',1),
('COMP262','Kiến trúc máy tính',3,45,0,NULL,3,'Bắt buộc',1),
('COMP267','Lập trình hướng đối tượng',4,60,0,NULL,3,'Bắt buộc',1),
('COMP275','Nền tảng phát triển web',3,45,0,NULL,3,'Bắt buộc',1),
('PHYE150','Giáo dục thể chất 1',1,28,0,NULL,3,'Bắt buộc',5),
('POLI104','Kinh tế chính trị Mác-Lênin',2,20,0,NULL,3,'Bắt buộc',7),
('POLI202','Tư tưởng Hồ Chí Minh',2,20,0,NULL,3,'Bắt buộc',7),
('COMP270','Hệ quản trị CSDL',3,45,0,NULL,4,'Bắt buộc',1),
('COMP271','Cấu trúc dữ liệu và giải thuật',4,60,0,NULL,4,'Bắt buộc',1),
('COMP272','Lập trình ứng dụng với Java',3,45,0,NULL,4,'Bắt buộc',1),
('COMP273','Mạng máy tính',3,45,0,NULL,4,'Bắt buộc',1),
('POLI204','Lịch sử Đảng Cộng sản Việt Nam',2,20,0,NULL,4,'Bắt buộc',7),
('COMP274','Đồ họa máy tính',3,45,0,NULL,4,'Tự chọn',1),
('COMP276','Nhập môn xử lý ảnh',3,45,0,NULL,4,'Tự chọn',1),
('COMP261','Trí tuệ nhân tạo',3,45,0,NULL,5,'Bắt buộc',1),
('COMP300','Nhập môn Công nghệ phần mềm',3,45,0,NULL,5,'Bắt buộc',1),
('COMP302','Phân tích và thiết kế toán',3,45,0,NULL,5,'Bắt buộc',1),
('COMP303','Quản trị mạng',3,45,0,NULL,5,'Bắt buộc',1),
('COMP306','Phát triển phần mềm cho thiết bị di động',3,45,0,'Nhóm chuyên ngành',5,'Tự chọn',1),
('COMP307','Công nghệ web',3,45,0,'Nhóm chuyên ngành',5,'Tự chọn',1),
('COMP356','Mạng máy tính nâng cao',2,30,0,'Nhóm chuyên ngành',5,'Tự chọn',1),
('COMP360','Thực hành dự án',3,45,0,NULL,6,'Bắt buộc',1),
('COMP309','Phần mềm mã nguồn mở',3,45,0,'Nhóm chuyên ngành',6,'Tự chọn',1),
('COMP355','Các vấn đề hiện đại Công nghệ thông tin',2,30,0,'Nhóm chuyên ngành',6,'Tự chọn',1),
('COMP357','Cơ sở dữ liệu tiên tiến',3,45,0,'Nhóm chuyên ngành',6,'Tự chọn',1),
('COMP358','Dữ liệu lớn (BigData)',3,45,0,'Nhóm chuyên ngành',7,'Tự chọn',1),
('COMP361','Lập trình trực quan C#',3,45,0,'Nhóm chuyên ngành',7,'Tự chọn',1),
('COMP362','Kiểm thử và đảm bảo CL phần mềm',3,45,0,'Nhóm chuyên ngành',7,'Tự chọn',1),
('COMP2463','Khai phá dữ liệu',3,45,0,'Nhóm chuyên ngành',7,'Tự chọn',1),
('COMP363','Thu thập và phân tích yêu cầu',3,45,0,'Nhóm chuyên ngành',7,'Tự chọn',1),
('COMP364','Thiết kế giao diện người dùng',3,45,0,'Nhóm chuyên ngành',7,'Tự chọn',1),
('COMP365','Quản lí dự án công nghệ thông tin',3,45,0,'Nhóm chuyên ngành',7,'Tự chọn',1),
('COMP366','Truyền thông đa phương tiện',3,45,0,'Nhóm chuyên ngành',7,'Tự chọn',1),
('COMP367','Phát triển phần mềm linh hoạt',3,45,0,'Nhóm chuyên ngành',7,'Tự chọn',1),
('COMP368','Các hệ thống thương mại điện tử',3,45,0,'Nhóm chuyên ngành',7,'Tự chọn',1),
('COMP369','Hệ thống thông tin doanh nghiệp',3,45,0,'Nhóm chuyên ngành',7,'Tự chọn',1),
('COMP370','Lập trình nâng cao (Python)',3,45,0,'Nhóm chuyên ngành',7,'Tự chọn',1),
('COMP371','Xác suất thống kê ứng dụng',3,45,0,'Nhóm chuyên ngành',7,'Tự chọn',2),
('COMP373','Học máy',3,45,0,'Nhóm chuyên ngành',7,'Tự chọn',1),
('COMP380','Thực tập công nghệ 1',2,30,0,NULL,8,'Bắt buộc',1),
('COMP381','Thực tập công nghệ 2',4,60,0,NULL,8,'Bắt buộc',1),
('COMP374','Lí thuyết độ phức tạp',3,45,0,'Nhóm chuyên ngành',8,'Tự chọn',1),
('COMP375','Lập trình song song và phân tán',3,45,0,'Nhóm chuyên ngành',8,'Tự chọn',1),
('COMP376','Tối ưu hoá',3,45,0,'Nhóm chuyên ngành',8,'Tự chọn',1),
('COMP377','Tin sinh học',3,45,0,'Nhóm chuyên ngành',8,'Tự chọn',1),
('COMP378','Xử lí ngôn ngữ tự nhiên',3,45,0,'Nhóm chuyên ngành',8,'Tự chọn',1),
('COMP382','Chuyên đề TN Công nghệ phần mềm',5,75,0,'Nhóm đồ án/khóa luận',8,'Tự chọn',1),
('COMP383','Ch.đề TN Dự án công nghệ/Khoa học',5,75,0,'Nhóm đồ án/khóa luận',8,'Tự chọn',1),
('COMP384','Chuyên đề TN Khoa học dữ liệu',5,75,0,'Nhóm đồ án/khóa luận',8,'Tự chọn',1),
('COMP106','Nhập môn Khoa học máy tính',2,30,0,NULL,9,'Bắt buộc',1);

-- Học phần tiên quyết (loại bỏ duplicate)
INSERT OR IGNORE INTO hoc_phan_tien_quyet (ma_hp, ma_hp_tien_quyet) VALUES
 ('COMP272','COMP267'),
 ('COMP271','COMP267'),
 ('COMP302','COMP271');
