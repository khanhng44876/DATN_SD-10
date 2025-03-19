create database DATN_SD10;
go
use DATN_SD10;
go
CREATE TABLE san_pham (
                          id INT IDENTITY(1,1) PRIMARY KEY,
                          ma_san_pham NVARCHAR(100) not null,
                          ten_san_pham NVARCHAR(100) not null,
                          ngay_nhap DATETIME DEFAULT GETDATE(),
                          ngay_sua DATETIME DEFAULT GETDATE(),
                          trang_thai NVARCHAR(100) not null,
                          id_danh_muc INT,
                          id_hang INT
);
CREATE TABLE san_pham_chi_tiet (
                                   id INT IDENTITY(1,1) PRIMARY KEY,
                                   don_gia DECIMAL(10, 2),
                                   so_luong INT,
                                   id_san_pham INT,
                                   id_kich_thuoc INT,
                                   id_mau_sac INT,
                                   id_chat_lieu INT,
                                   mo_ta NVARCHAR(255),
                                   hinh_anh NVARCHAR(255),
                                   trang_thai NVARCHAR(20)

);
CREATE TABLE khuyen_mai (
                            id INT IDENTITY(1,1) PRIMARY KEY,
                            so_luong int not null,
                            ten_khuyen_mai NVARCHAR(100) NOT NULL,
                            mo_ta NVARCHAR(255),
                            ma_khuyen_mai NVARCHAR(255) NOT NULL,
                            muc_giam int not null,
                            ngay_bat_dau DATE NOT NULL,
                            ngay_ket_thuc DATE NOT NULL,
                            dieu_kien float NOT NULL,
                            giam_toi_da float NOT NULL,
                            id_hoa_don int,
                            trang_thai NVARCHAR(50) not null,
                            so_luong_sd int
);

CREATE TABLE danh_muc (
                          id INT IDENTITY(1,1) PRIMARY KEY,
                          ten_danh_muc NVARCHAR(255)NOT NULL,
                          mo_ta NVARCHAR (200),
                          trang_thai NVARCHAR(50)
);
CREATE TABLE hoa_don_chi_tiet (
                                  id INT IDENTITY(1,1) PRIMARY KEY,
                                  id_hoa_don INT NOT NULL,
                                  id_san_pham_chi_tiet INT NOT NULL,
                                  so_luong INT NOT NULL,
                                  don_gia DECIMAL,
                                  tong_tien DECIMAL(10, 2) NOT NULL,
                                  thanh_tien DECIMAL(10, 2) NOT NULL,
                                  ngay_tao DATE,
                                  ngay_sua DATE,
                                  trang_thai NVARCHAR(50)


);

CREATE TABLE hoa_don (
                         id INT IDENTITY(1,1) PRIMARY KEY,
                         id_nhan_vien int not null,
                         id_khach_hang INT NOT NULL,
                         id_khuyen_mai INT,
                         ngay_tao DATE NOT NULL,
                         ngay_sua DATE,
                         don_gia DECIMAL,
                         tong_tien DECIMAL(10, 2) NOT NULL,
                         trang_thai_thanh_toan NVARCHAR(50),
                         hinh_thuc_thanh_toan NVARCHAR(50),
                         dia_chi_giao_hang NVARCHAR(200),
                         ghi_chu NVARCHAR (100)

);

CREATE TABLE khach_hang (
                            id INT IDENTITY(1,1) PRIMARY KEY,
                            ten_khach_hang NVARCHAR(100) NOT NULL,
                            tai_khoan NVARCHAR(100),
                            email NVARCHAR(100),
                            mat_khau NVARCHAR(255),
                            so_dien_thoai NVARCHAR(15),
                            dia_chi NVARCHAR(255),
                            ngay_sinh DATE,
                            gioi_tinh NVARCHAR(10)
);

CREATE TABLE nhan_vien (
                           id INT IDENTITY(1,1) PRIMARY KEY,
                           tai_khoan NVARCHAR(100) NOT NULL UNIQUE,
                           mat_khau NVARCHAR(255) NOT NULL,
                           ten_nhan_vien NVARCHAR(100),
                           email NVARCHAR(100) UNIQUE,
                           chuc_vu NVARCHAR(100),
                           sdt NVARCHAR(15),
                           dia_chi NVARCHAR(200),
                           gioi_tinh NVARCHAR(10),
                           ngay_tao DATETIME DEFAULT GETDATE(),
                           ngay_sua DATETIME DEFAULT GETDATE(),
                           trang_thai NVARCHAR(50) DEFAULT 'Đang hoạt động'
);
CREATE TABLE kich_thuoc (
                            id INT IDENTITY(1,1) PRIMARY KEY,
                            ten_kich_thuoc NVARCHAR(10) NOT NULL UNIQUE,
                            mo_ta NVARCHAR(255)
);
CREATE TABLE mau_sac (
                         id INT IDENTITY(1,1) PRIMARY KEY,
                         ten_mau_sac NVARCHAR(50) NOT NULL UNIQUE,
                         mo_ta NVARCHAR(255)
);
CREATE TABLE hang (
                      id INT IDENTITY(1,1) PRIMARY KEY,
                      ten_hang NVARCHAR(50) NOT NULL UNIQUE,
                      trang_thai NVARCHAR(255)
);
CREATE TABLE chat_lieu (
                           id INT IDENTITY(1,1) PRIMARY KEY,
                           ten_chat_lieu NVARCHAR(50) NOT NULL UNIQUE,
                           mo_ta NVARCHAR(255)
);

--------San pham chi tiet---------------
alter table san_pham_chi_tiet
    add constraint fk_spct_mauSac foreign key (id_mau_sac) references mau_sac (id)
    go

alter table san_pham_chi_tiet
    add constraint fk_spct_chatLieu foreign key (id_chat_lieu) references chat_lieu (id)
    go

alter table san_pham_chi_tiet
    add constraint fk_spct_size foreign key (id_kich_thuoc) references kich_thuoc (id)
    go

alter table san_pham_chi_tiet
    add constraint fk_spct_sanPham foreign key (id_san_pham) references san_pham (id)
    go
-----------Hoa don----------
alter table hoa_don
    add constraint fk_hoaDon_nhan_vien foreign key (id_nhan_vien) references nhan_vien (id)
    go

alter table hoa_don
    add constraint fk_hoaDon_khachHang foreign key (id_khach_hang) references khach_hang (id)
    go

alter table hoa_don
    add constraint fk_hoaDon_khuyen_mai foreign key (id_khuyen_mai) references khuyen_mai (id)
    go

-------------Hoa don chi tiet---------------

alter table hoa_don_chi_tiet
    add constraint fk_hoa_don_hoa_don_chi_tiet foreign key (id_hoa_don) references hoa_don (id)
    go

alter table hoa_don_chi_tiet
    add constraint fk_hoa_don_chi_tiet_san_pham_chi_tiet foreign key (id_san_pham_chi_tiet) references san_pham_chi_tiet (id)
    go


-----------San Pham-----------
alter table san_pham
    add constraint fk_san_pham_danh_muc foreign key (id_danh_muc) references danh_muc (id) ON DELETE CASCADE;
go

alter table san_pham
    add constraint fk_san_pham_hang foreign key (id_hang) references hang (id) ON DELETE CASCADE;
go

ALTER TABLE hoa_don ALTER COLUMN id_khuyen_mai INT NULL;

--------------------------------------------------------------------
insert into danh_muc(ten_danh_muc, mo_ta, trang_thai)
values (N'Mùa Hè',N'Mùa Hè', N'Hoạt Động'),
       (N'Mùa Đông',N'Mùa Đông',N'Hoạt Động'),
       (N'Nam',N'Nam', N'Hoạt Động'),
       (N'Nữ',N'Nữ', N'Hoạt Động'),
       (N'Mùa Thu',N'Mùa Thu',N'Hoạt Động'),
       (N'Mùa Xuân',N'Mùa Xuân',N'Hoạt Động')

    insert into hang(ten_hang,trang_thai)
values('ADADIS','Con'),
    ('Nike','Con'),
    ('Pama','Con')

insert into san_pham(ma_san_pham, ten_san_pham, ngay_nhap, trang_thai, id_danh_muc, id_hang)
values (N'SP001',N'Bộ áo đấu câu lạc bộ Manchester United','2025-02-12',N'Còn hàng',1,2),
    (N'SP002',N'Bộ áo đấu câu lạc bộ Hà Nội','2025-02-12',N'Còn hàng',2,3),
    (N'SP003',N'Bộ áo đấu câu lạc bộ Việt Nam','2025-02-12',N'Còn hàng',3,1),
    (N'SP004', N'Bộ áo đấu câu lạc bộ Manchester City', '2025-02-12', N'Còn hàng', 1, 3),
    (N'SP005', N'Bộ áo đấu câu lạc bộ Real Madrid', '2025-02-12', N'Còn hàng', 1, 1),
    (N'SP006', N'Bộ áo đấu câu lạc bộ Barcelona', '2025-02-12', N'Còn hàng', 2, 1),
    (N'SP007', N'Bộ áo đấu câu lạc bộ Bayern Munich', '2025-02-12', N'Còn hàng', 3, 2),
    (N'SP008', N'Bộ áo đấu câu lạc bộ PSG', '2025-02-12', N'Còn hàng', 1, 2),
    (N'SP009', N'Bộ áo đấu câu lạc bộ Chelsea', '2025-02-12', N'Còn hàng', 2, 3),
    (N'SP010', N'Bộ áo đấu câu lạc bộ Juventus', '2025-02-12', N'Còn hàng', 3, 1),
    (N'SP011', N'Bộ áo đấu câu lạc bộ AC Milan', '2025-02-12', N'Còn hàng', 1, 1),
    (N'SP012', N'Bộ áo đấu câu lạc bộ Inter Milan', '2025-02-12', N'Còn hàng', 2, 2),
    (N'SP013', N'Bộ áo đấu câu lạc bộ Arsenal', '2025-02-12', N'Còn hàng', 3, 3),
    (N'SP014', N'Bộ áo đấu câu lạc bộ Tottenham Hotspur', '2025-02-12', N'Còn hàng', 1, 2),
    (N'SP015', N'Bộ áo đấu câu lạc bộ Borussia Dortmund', '2025-02-12', N'Còn hàng', 2, 3),
    (N'SP016', N'Bộ áo đấu câu lạc bộ Atletico Madrid', '2025-02-12', N'Còn hàng', 3, 1),
    (N'SP017', N'Bộ áo đấu câu lạc bộ Napoli', '2025-02-12', N'Còn hàng', 1, 2),
    (N'SP018', N'Bộ áo đấu câu lạc bộ Sevilla', '2025-02-12', N'Còn hàng', 2, 3),
    (N'SP019', N'Bộ áo đấu câu lạc bộ AS Roma', '2025-02-12', N'Còn hàng', 3, 1),
    (N'SP020', N'Bộ áo đấu câu lạc bộ Benfica', '2025-02-12', N'Còn hàng', 1, 2),
    (N'SP021', N'Bộ áo đấu câu lạc bộ Porto', '2025-02-12', N'Còn hàng', 2, 3),
    (N'SP022', N'Bộ áo đấu câu lạc bộ Sporting Lisbon', '2025-02-12', N'Còn hàng', 3, 1),
    (N'SP023', N'Bộ áo đấu câu lạc bộ Lyon', '2025-02-12', N'Còn hàng', 1, 2),
    (N'SP024', N'Bộ áo đấu câu lạc bộ Marseille', '2025-02-12', N'Còn hàng', 2, 3),
    (N'SP025', N'Bộ áo đấu câu lạc bộ Ajax', '2025-02-12', N'Còn hàng', 3, 1);


insert into kich_thuoc(ten_kich_thuoc, mo_ta)
values ('S',N'Size nhỏ'),
       ('M',N'Size trung bình'),
       ('L',N'Size lớn'),
       ('XL',N'Size rất lớn')

    insert into mau_sac(ten_mau_sac, mo_ta)
values(N'Đen', N'Màu đen sang trọng'),
    (N'Trắng', N'Màu trắng tinh khiết'),
    (N'Xanh', N'Màu xanh dương'),
    (N'Vàng', N'Màu vàng bóng bẩy');

insert into chat_lieu(ten_chat_lieu, mo_ta)
values(N'Polyester', N'Vải Polyester thoáng khí'),
      (N'Cotton', N'Vải Cotton mềm mại'),
      (N'Spandex', N'Vải Spandex co giãn tốt');





insert into san_pham_chi_tiet(don_gia, so_luong, id_san_pham, id_kich_thuoc, id_mau_sac, id_chat_lieu, mo_ta, hinh_anh,trang_thai)
values (1200, 150, 1, 1, 1, 1, N'Chất liệu thấm hút tốt', NULL, N'Còn hàng'),
       (1250, 130, 2, 2, 2, 1, N'Chất liệu cao cấp', NULL, N'Còn hàng'),
       (1100, 200, 3, 3, 3, 1, N'Áo đấu chính hãng', NULL, N'Còn hàng'),
       (1400, 100, 4, 1, 2, 2, N'Co giãn tốt', NULL, N'Còn hàng'),
       (1350, 170, 5, 2, 3, 2, N'Thiết kế thoáng mát', NULL, N'Còn hàng'),
       (1500, 190, 6, 3, 1, 1, N'Phiên bản giới hạn', NULL, N'Còn hàng'),
       (1450, 160, 7, 1, 3, 2, N'Mềm mại, thoải mái', NULL, N'Còn hàng'),
       (1550, 120, 8, 2, 2, 3, N'Dành cho fan hâm mộ', NULL, N'Còn hàng'),
       (1600, 180, 9, 3, 3, 1, N'Form áo ôm vừa vặn', NULL, N'Còn hàng'),
       (1650, 140, 10, 1, 1, 2, N'Thiết kế tinh tế', NULL, N'Còn hàng'),
       (1700, 130, 11, 2, 2, 3, N'Thích hợp vận động', NULL, N'Còn hàng'),
       (1750, 150, 12, 3, 3, 1, N'Phong cách thời thượng', NULL, N'Còn hàng'),
       (1800, 120, 13, 1, 1, 2, N'Mang phong cách cổ điển', NULL, N'Còn hàng'),
       (1850, 110, 14, 2, 2, 3, N'Độ bền cao', NULL, N'Còn hàng'),
       (1900, 100, 15, 3, 3, 1, N'Chất liệu siêu nhẹ', NULL, N'Còn hàng'),
       (1950, 90, 16, 1, 1, 2, N'Thiết kế trẻ trung', NULL, N'Còn hàng'),
       (2000, 80, 17, 2, 2, 3, N'Phiên bản đặc biệt', NULL, N'Còn hàng'),
       (2050, 70, 18, 3, 3, 1, N'Áo đấu biểu tượng', NULL, N'Còn hàng'),
       (2100, 60, 19, 1, 1, 2, N'Tinh tế và đẳng cấp', NULL, N'Còn hàng'),
       (2150, 50, 20, 2, 2, 3, N'Phiên bản limited', NULL, N'Còn hàng'),
       (2200, 40, 21, 3, 3, 1, N'Chất liệu cực thoáng', NULL, N'Còn hàng'),
       (2250, 30, 22, 1, 1, 2, N'Thiết kế sang trọng', NULL, N'Còn hàng'),
       (2300, 20, 23, 2, 2, 3, N'Áo đấu huyền thoại', NULL, N'Còn hàng'),
       (2350, 10, 24, 3, 3, 1, N'Màu sắc nổi bật', NULL, N'Còn hàng'),
       (2400, 5, 25, 1, 1, 2, N'Áo đấu siêu bền', NULL, N'Còn hàng');


insert into khach_hang(ten_khach_hang, email, so_dien_thoai, dia_chi,ngay_sinh, gioi_tinh,tai_khoan, mat_khau)
values (N'Khách lẻ',null,null,null,null,null,null,null),
        ('Nguyen Gia Khanh', 'khanhngph44876@fpt.edu.vn','0345760481','Dai Mo, HN','1998-02-12','Nam','khanh123', '123456'),
       ('Nguyen Quach Vu', 'vunqph45621@fpt.edu.vn','0396311217','Nhon, HN','1995-12-22','Nam','vu123', '123456'),
       ('Vi Cong Minh', 'minhvcph45103@fpt.edu.vn','0702202307','Huu Lung, LS','2004-05-09','Nam','minh123', '123456'),
       ('Nguyen Van Sao', 'saonvph45620@fpt.edu.vn','0879913025','n, BN','2003-05-09','Nam','sao123', '123456'),
       ('Tran Thi Thu phuong', 'phuongtttph45219@fpt.edu.vn','0947052726','n, LS','2003-05-09',N'Nữ','phuong123', '123456')



    insert into nhan_vien(tai_khoan,ten_nhan_vien, mat_khau,chuc_vu, email,sdt, ngay_tao, ngay_sua,trang_thai)
values ('khanhok123','khanh','SA12342',N'Nhân Viên','jdk1234@gmail.com','0987654321','2025-02-14','2025-02-14','Hoat dong'),
    ('vuok321','quachvu','as12312',N'Nhân Viên','zzzzz@gmail.com','0988769287','2025-02-14','2025-02-14','Hoat dong'),
    ('minhok123','minh','11111',N'Quản Lý','skbd9999@gmail.com','0999999999','2025-02-14','2025-02-14','Hoat dong')



insert into khuyen_mai(so_luong,ten_khuyen_mai, mo_ta, ma_khuyen_mai, muc_giam, ngay_bat_dau,ngay_ket_thuc,dieu_kien,giam_toi_da,id_hoa_don,trang_thai,so_luong_sd)
values(10,'Monday','abc','SP012345',10,'2025-02-14','2025-03-01',100,30,1,'Đang kích hoạt',0),
    (20,'friday','abc','SP015645',10,'2025-02-14','2025-03-01',200,50,1,'Đang kích hoạt',0),
    (30,'sunday','abc','SP23415',15,'2025-02-14','2025-03-01',150,40,1,'Đang kích hoạt',0)

insert into hoa_don(id_khuyen_mai,id_nhan_vien, id_khach_hang, ngay_tao,ngay_sua, tong_tien, trang_thai_thanh_toan, hinh_thuc_thanh_toan)
values (1,3,1,'2025-02-14','2025-02-14',22222222,'Da thanh toan','Chuyen Khoan'),
    (1,2,2,'2025-02-14','2025-02-14',33333333,'Da thanh toan','Tien Mat'),
    (3,3,3,'2025-02-14','2025-02-14',55555555,'Chua thanh toan','Chuyen Khoan')



insert into hoa_don_chi_tiet(id_hoa_don, id_san_pham_chi_tiet, so_luong,don_gia, tong_tien, thanh_tien)
values (1,3,100,1900,111111.0,22222.0),
    (2,2,120,1500,100000.0,1266628.0),
    (3,1,180,1000,1333333.0,1976128.0)










