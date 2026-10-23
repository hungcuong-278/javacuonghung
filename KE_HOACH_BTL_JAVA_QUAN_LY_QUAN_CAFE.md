# KẾ HOẠCH BÀI TÁP LỚN – MÁY POS QUÁN CÀ PHÊ (2 NGÀY)

**Ngôn ngữ:** Java (duy nhất) – JDK 17
**Giao diện:** Java Swing (chạy thẳng trong IntelliJ IDEA, không cài thêm gì)
**Lưu trữ:** File CSV trong thư mục `data/` (không cài CSDL)
**Thời gian code:** 2 ngày — **23/10 & 24/10/2026**
**Hoàn thiện & nộp:** 25/10 – 28/10/2026 (báo cáo ngày **28/10/2026**)
**Nhóm:** 2 người — Người A làm **2/3**, Người B làm **1/3** (⭐ = bạn)

---

## 1. Ý tưởng bài toán

Mô phỏng **máy POS (Point of Sale) của một quán cà phê** như thật:

- Nhìn **sơ đồ 9 bàn** (3×3) với 3 màu trạng thái
- Bấm vào bàn trống để **mở bàn** → chọn món gọi vào
- **Xem bàn đó ăn hết bao nhiêu tiền** ngay trên sơ đồ
- **Yêu cầu tính tiền** → **thanh toán** (tiền mặt có trả thừa / chuyển khoản)
- Sau khi thanh toán: **lưu hóa đơn ra file**, xem **lịch sử hóa đơn & doanh thu**

### 1.1. Ba yêu cầu bạn đặt ra → cách đáp ứng

| Yêu cầu | Cách đáp ứng trong thiết kế |
|----------|------------------------------|
| **Dễ hiểu** | 4 màn hình, mỗi màn hình 1 việc; mô hình chỉ có **1 bảng `HoaDon`** với 3 trạng thái rõ ràng, giảng thích trong 1 phút |
| **Dễ tương tác** | Bấm bàn → bấm món là cộng số lượng; mọi thao tác sai có thông báo tiếng Việt; màu bàn thay đổi theo trạng thái (nhìn là biết quán đang bận hay rảnh) |
| **Tính thực tế cao** | Có tồn kho món, có tổng tiền ngay trên bàn, có trả tiền thừa, có lịch sử & doanh thu — đúng những gì quán thật dùng |

### 1.2. Cố ý KHÔNG làm (để kịp 2 ngày)

- ❌ Quản lý khách hàng / thành viên / tích điểm
- ❌ Chia tiền (tách hóa đơn cho 2 người ngồi chung bàn)
- ❌ Kết nối máy in thật, in QR, thanh toán online
- ❌ Đăng nhập / phân quyền
- ❌ Thêm – bàn bằng cách kéo thả vị trí
- ❌ Thư viện ngoài (đúng yêu cầu "chỉ dùng Java"), không dùng Maven

> Nguyên tắc: **thiếu 1 tính năng nhỏ còn hơn trễ 1 ngày.** Thứ tự cắt nếu kịp: trả tiền thừa → tìm kiếm trong lịch sử → tồn kho cảnh báo.

---

## 2. Chia việc 2/3 – 1/3

### 2.1. Người A (2/3) — phần lõi, làm trước để mở đường

| STT | Việc | Cụ thể | Giờ |
|-----|------|--------|-----|
| A1 | Khởi tạo project | Project `BTL_POS_QuanCafe` (Java 17, Application), cấu trúc thư mục mục 4.2, `.gitignore`, `README.md` | 0,5h |
| A2 | Model lõi | `Mon`, `NhomMon`, `Ban`, `TrangThaiBan`, `HoaDon`, `ChiTietHoaDon`, `TrangThaiHD`, `PhuongThucTT` | 2h |
| A3 | Tầng lưu trữ | `FileStore` (đọc/ghi CSV UTF-8) + `BanRepo` + `HoaDonRepo` + `MonRepo` | 3h |
| A4 | Khung giao diện | `MainFrame` (menu trái 4 mục + `CardLayout`) | 1h |
| A5 | `BanPanel` | Sơ đồ 9 bàn 3×3, tô màu theo trạng thái, hiện tổng tiền trên bàn, đồng bộ lại khi quay lại màn hình | 3h |
| A6 | `GoiMonPanel` | 4 tab *Cà phê / Nước uống / Trà / Bánh*, giỏ `JTable`, `+SL / −SL / Xóa dòng`, nút *Yêu cầu tính tiền* và *Thanh toán* (có trả thừa) | 4,5h |
| A7 | Tích hợp | Nối 3 màn hình vào `MainFrame`, chạy trọn luồng, ghép phần của B | 2h |
| | **Tổng A** | | **≈ 16h** |

### 2.2. Người B (1/3) ⭐ — bạn

| STT | Việc | Cụ thể | Giờ |
|-----|------|--------|-----|
| B1 | Model phụ | `Mon` (dùng chung enum `NhomMon` của A) + dữ liệu mẫu `data/mon.csv` ≥ 24 món, đủ 4 nhóm | 0,5h |
| B2 | `QuanLyMonPanel` | CRUD **đầy đủ**: Thêm / Sửa / Xóa (hỏi xác nhận) / Tìm theo tên; form Mã (tự sinh `MONxxx`) · Tên · Nhóm · Đơn giá (>0) · Mô tả | 3h |
| B3 | Tồn kho | Khi thanh toán trừ tồn kho món; món hết hàng thì không cho thêm vào giỏ (hiện màu xám trong menu) | 1h |
| B4 | `HoaDonPrinter` | Hàm sinh nội dung hóa đơn dạng text (quán/cafe, bàn, món, tổng, tiền khách đưa, tiền thừa) + hàm xuất ra file `.txt` — **A gọi hàm này** | 1,5h |
| B5 | `ThongKeService` + `LichSuHoaDonPanel` | Bảng hóa đơn đã thanh toán (mã, bàn, giờ, tổng, hình thức TT) + 2 thẻ: **Doanh thu hôm nay** và **Số hóa đơn hôm nay** | 2,5h |
| B6 | Kiểm thử | Chạy trọn luồng thật, soát lỗi tiếng Việt/dấu phẩy, dựng dữ liệu mẫu đẹp | 1,5h |
| B7 | Tài liệu | `README.md`, `BAO_CAO.md`, cấu trúc báo cáo Word mục 9, slide thuyết trình | 2h |
| | **Tổng B** | | **≈ 12h** |

**Tỉ lệ: A 16h / B 12h ≈ 57% – 43% theo giờ, nhưng theo độ khó: A phụ trách toàn bộ hạ tầng + 2 màn hình khó nhất (sơ đồ bàn, gọi món + thanh toán); B phụ trách 1 màn hình CRUD dễ + 1 màn hình báo cáo + in hóa đơn + toàn bộ tài liệu → đúng tỉ lệ 2/3 – 1/3.**

### 2.3. Vì sao phần của bạn không bị "chờ" người A

Bạn làm được việc ngay từ sáng ngày 1 vì:

- `QuanLyMonPanel` chỉ cần `MonRepo` + file `mon.csv` → **tự tạo, không cần A**
- `LichSuHoaDonPanel` chỉ cần `HoaDonRepo` → **dùng bản CSV tự tạo để test trước**, sau đó đổi sang của A (cùng định dạng file)
- `HoaDonPrinter` là hàm độc lập, nhận `HoaDon` làm tham số → viết trước, A gọi sau

> **Chỉ có 1 điểm phải chờ A:** `MainFrame` và việc nối `GoiMonPanel` với `HoaDonPrinter`. Vì vậy **luyện `HoaDonPrinter` vào 11/10 sáng** và bàn cách gọi trước khi A cần.

### 2.4. Quy tắc phối hợp (bắt buộc tuân thủ)

| Quy tắc | Chi tiết |
|----------|----------|
| Nhánh Git | `main` (chạy được) → `feature/mon` (B), `feature/ban` + `feature/banhang` (A) |
| Commit | ≥ 1 commit/ngày, message: `feat(thongke): loc doanh thu theo ngay` |
| Merge lần 1 | **23/10 tối** – B merge `QuanLyMonPanel` vào nhánh của A |
| Merge lần 2 | **24/10 tối** – ghép tất cả vào `main`, đóng băng tính năng |
| Họp | 20:00 mỗi tối, 30 phút, báo cáo tiến độ + chụp màn hình |
| Backup | Copy thư mục `data/` vào USB mỗi tối |
| Không chặn nhau | Ai đang bị vướng thì làm việc B khác, đừng ngồi chờ |

---

## 3. Mô hình dữ liệu (1 bảng `HoaDon` duy nhất)

### 3.1. Vì sao chỉ 1 bảng?

Không tách "phiếu bàn" và "hóa đơn" thành 2 khái niệm. Một bàn đang phục vụ **chính là một hóa đơng đang mở**, chỉ khác ở trạng thái:

```
DANG_PHUC_VU ──(Yêu cầu tính tiền)──> CHO_THANH_TOAN ──(Thanh toán)──> DA_THANH_TOAN
     (bàn xanh dương)                    (bàn vàng/cam)                (lưu vào lịch sử)
```

**Lợi ích:** không có dữ liệu "treo" lúc đóng phần mềm; mở app là đọc `ban.csv` thấy ngay bàn nào đang phục vụ hóa đơn nào; màn hình lịch sử chỉ cần lọc `DA_THANH_TOAN` — ít code, dễ giải thích khi thầy hỏi.

### 3.2. Sơ đồ quan hệ

```
     Mon 1───────n ChiTietHoaDon n───────1 HoaDon n───────1 Ban
  (mã, tên,        (mã HD, mã món,      (mã HD, mã bàn,   (mã bàn, vị trí,
   nhóm, đơn giá,   số lượng, đơn giá)   ngày, trạng thái,  trạng thái,
   tồn kho)                             tổng, khách đưa)  mã hóa đơn hiện tại)
```

### 3.3. Enum

```java
public enum NhomMon    { CA_PHE("Cà phê"), NUOC_UONG("Nước uống"), TRA("Trà"), BANH("Bánh") }
public enum TrangThaiBan { TRONG("Trống"), CO_KHACH("Có khách"), CHO_THANH_TOAN("Chờ thanh toán") }
public enum TrangThaiHD  { DANG_PHUC_VU, CHO_THANH_TOAN, DA_THANH_TOAN }
public enum PhuongThucTT { TIEN_MAT("Tiền mặt"), CHUYEN_KHOAN("Chuyển khoản") }
```

### 3.4. Lớp model

```java
public class Mon {
    private String maMon;        // "MON001"
    private String tenMon;
    private NhomMon nhomMon;
    private long donGia;
    private int tonKho;
    private String moTa;
    // + getter/setter, constructor rỗng, constructor đầy đủ, toString
}

public class Ban {
    private String maBan;             // "B01"
    private String viTri;             // "2-1"
    private TrangThaiBan trangThai;
    private String maHoaDon;          // null khi bàn trống
}

public class ChiTietHoaDon {
    private String maHD, maMon, tenMon;
    private int soLuong;
    private long donGia;              // chốt giá lúc gọi món
    public long thanhTien() { return soLuong * donGia; }
}

public class HoaDon {
    private String maHD;              // "HD20261023001"
    private String maBan;
    private LocalDateTime ngayMo;
    private LocalDateTime ngayDong;   // null khi chưa thanh toán
    private TrangThaiHD trangThai;
    private long tongTien;
    private long tienKhachDua;        // 0 khi chuyển khoản
    private long tienThua;            // 0 khi chuyển khoản
    private PhuongThucTT phuongThucTT;
    private List<ChiTietHoaDon> dsChiTiet;
    public long tongTien() { /* cộng dsChiTiet */ }
}
```

### 3.5. File CSV

**`data/mon.csv`**
```
maMon,tenMon,nhomMon,donGia,tonKho,moTa
MON001,Phin den da,CA_PHE,25000,100,Pha ca phe phin nuoc da
```

**`data/ban.csv`**
```
maBan,viTri,trangThai,maHoaDon
B01,2-1,TRONG,
B02,2-2,CO_KHACH,HD20261023001
```

**`data/hoadon.csv`**
```
maHD,maBan,ngayMo,ngayDong,trangThai,tongTien,tienKhachDua,tienThua,phuongThucTT
HD20261023001,B02,2026-10-23T14:05:30,,DANG_PHUC_VU,0,0,0,
```

**`data/hoadon_chitiet.csv`**
```
maHD,maMon,tenMon,soLuong,donGia
HD20261023001,MON001,Phin den da,1,25000
```

**Quy tắc bắt buộc:**
1. UTF-8, có dòng tiêu đề, ngày giờ ISO `yyyy-MM-ddTHH:mm:ss` (dễ so sánh chuỗi).
2. Số dùng `Locale.US` để luôn có dấu `.`.
3. Trường chứa dấu phẩy → bọc `"` và nhân đôi `"` bên trong.
4. Không tồn tại → trả về danh sách rỗng (không ném exception ra giao diện).
5. Dòng hỏng → in log console và bỏ qua, không làm hỏng chương trình.

---

## 4. Cấu trúc dự án & công nghệ

### 4.1. Môi trường

- JDK 17, Project SDK = 17 (đã có trong IntelliJ)
- Tạo project dạng **Java Application** — không dùng Maven/Gradle để tránh rắc rối lúc trình bày
- Không thêm thư viện nào

### 4.2. Cây thư mục

```
BTL_POS_QuanCafe/
├── src/com/pos/quancafe/
│   ├── Main.java
│   ├── model/
│   │   ├── Mon.java              NhomMon.java
│   │   ├── Ban.java              TrangThaiBan.java
│   │   └── HoaDon.java           ChiTietHoaDon.java
│   │                             TrangThaiHD.java  PhuongThucTT.java
│   ├── data/
│   │   ├── FileStore.java        ← đọc/ghi CSV (xương sống, A viết)
│   │   ├── MonRepo.java          ⭐ B
│   │   ├── BanRepo.java
│   │   └── HoaDonRepo.java
│   ├── service/
│   │   ├── OrderService.java     ← mở bàn, thêm món, thanh toán, trừ kho
│   │   └── ThongKeService.java   ⭐ B
│   ├── ui/
│   │   ├── MainFrame.java
│   │   └── panel/
│   │       ├── BanPanel.java
│   │       ├── GoiMonPanel.java
│   │       ├── QuanLyMonPanel.java    ⭐ B
│   │       └── LichSuHoaDonPanel.java ⭐ B
│   └── util/
│       ├── FormatUtil.java           ← định dạng tiền "25.000 đ", ngày giờ
│       └── HoaDonPrinter.java        ⭐ B – sinh + xuất hóa đơn .txt
├── data/                       ← dữ liệu sinh ra lúc chạy (commit bản mẫu)
│   ├── mon.csv   ban.csv   hoadon.csv   hoadon_chitiet.csv
├── docs/BAO_CAO.md   docs/hoi-dong-giao-dien.png
├── README.md
└── .gitignore
```

Quy ước đặt tên: Class `PascalCase` · hàm/biến `camelCase` · hằng số `UPPER_SNAKE` · file khớp tên class.

---

## 5. Đặc tả từng màn hình

### 5.1. `MainFrame`

```
┌──────────────────────────────────────────────┐
│  QUÁN CÀ PHÊ – MÁY POS       23/10/2026 14:05│
├────────────┬─────────────────────────────────┤
│ ▸ Sơ đồ bàn │                                 │
│   Quản lý món│        CardLayout              │
│   Lịch sử   │                                 │
│   Thoát     │                                 │
└────────────┴─────────────────────────────────┘
```

- Menu trái 4 nút xếp dọc; đổi màn hình bằng `CardLayout`.
- Quay lại "Sơ đồ bàn" phải **vẽ lại 9 bàn** (vì trạng thái bàn vừa thay đổi) → gọi `capNhat()` mỗi lần chuyển sang.
- Nút *Thoát* hỏi xác nhận trước khi đóng.

### 5.2. `BanPanel` — sơ đồ bàn (A) ⭐ phần hay nhất để demo

```
┌────────┬────────┬────────┐
│  B01   │  B02   │  B03   │
│ Trống  │ 92.000 │ Chờ TT│
├────────┼────────┼────────┤
│  B04   │  B05   │  B06   │
│ Trống  │ 45.500 │ Trống │
├────────┼────────┼────────┤
│  B07   │  B08   │  B09   │
│ Trống  │ Trống  │ 28.000 │
└────────┴────────┴────────┘
```

- 9 `JButton` trong `JGridLayout(3, 3, 10, 10)`, kích thước ưu tiên `140×110`.
- Tô màu nền theo trạng thái + `setOpaque(true)`:

| Trạng thái | Màu | Nghĩa |
|-----------|-----|-------|
| `TRONG` | xanh lá `#2ECC71` | Khách đã đi, bàn sẵn sàng |
| `CO_KHACH` | xanh dương `#3498DB` | Đang phục vụ, **hiện tổng tiền tạm tính** |
| `CHO_THANH_TOAN` | vàng/cam `#F1C40F` | Đã gọi tính tiền, chờ thu tiền |

- Nội dung nút nhiều dòng: mã bàn + trạng thái (+ tổng tiền). Dùng HTML trong `setText()` cho xuống dòng: `setText("<html><b>B02</b><br/>92.000 đ</html>")`.
- Bấm vào bàn:
  - `TRONG` → mở màn hình gọi món (hỏi trước: *"Mở bàn B01 cho khách mới?"*)
  - `CO_KHACH` hoặc `CHO_THANH_TOAN` → vào gọi món để xem/thêm/thanh toán
- Chuột phải lên bàn → menu nhỏ: *Xem hóa đơn*, *Hủy bàn* (chỉ khi `DANG_PHUC_VU`, hỏi xác nhận).

### 5.3. `GoiMonPanel` (A)

- **Trái:** `JTabbedPane` 4 tab *Cà phê / Nước uống / Trà / Bánh*, mỗi tab là `JList<Mon>` hiện `tên – giá`. Bấm 1 lần = thêm vào giỏ; bấm 2 lần = thêm 1 phần nữa. Món hết tồn kho → `setEnabled(false)` và làm mờ.
- **Phải:** tiêu đề bàn + giờ; `JTable` giỏ (Món · SL · Đơn giá · Thành tiền); nút `+SL`, `−SL`, `Xóa dòng`.
- **Dưới:** khung tổng `Tổng: 92.000 đ` (đậm, cỡ lớn) + 2 nút:
  - **Yêu cầu tính tiền** → đổi trạng thái sang `CHO_THANH_TOAN`, khóa sửa giỏ (đúng như quán thật: đã gọi tính tiền thì không thêm nữa)
  - **Thanh toán** → mở hộp thoại:
    - Chọn *Tiền mặt* → nhập tiền khách đưa → hiện *Tiền thừa* → OK = thanh toán
    - Chọn *Chuyển khoản* → không cần nhập tiền đưa → OK = thanh toán
    - Số tiền đưa < tổng → báo lỗi tiếng Việt, không cho OK
  - Sau khi thanh toán → hỏi *"In/Lưu hóa đơn?"* → gọi `HoaDonPrinter.xuatFileTxt(...)` → bàn về `TRONG`, quay lại `BanPanel`
- **Trước khi đóng màn hình**: nếu bàn đang `DANG_PHUC_VU` mà giỏ rỗng → tự hủy bàn cho về `TRONG` (tránh bàn "kẹt").

### 5.4. `QuanLyMonPanel` (B) ⭐

- `JTable`: Mã · Tên · Nhóm · Đơn giá · Tồn kho · Mô tả. Tồn kho = 0 → tô đỏ.
- Form nhập: Mã (tự sinh `MON001`), Tên (bắt buộc), Nhóm (`JComboBox`), Đơn giá (> 0), Tồn kho (≥ 0), Mô tả.
- Nút: **Thêm**, **Sửa** (bấm 1 dòng để đổ dữ liệu lên form), **Xóa** (hỏi xác nhận), **Tìm theo tên** (ô `JTextField` + nút), **Làm mới**.
- Bắt lỗi: tên trống / giá ≤ 0 / tồn kho < 0 → `JOptionPane` tiếng Việt.

### 5.5. `LichSuHoaDonPanel` (B) ⭐

- 2 thẻ màu ở trên: **DOANH THU HÔM NAY: 1.245.000 đ** · **SỐ HÓA ĐƠN HÔM NAY: 12**
- `JTable` lịch sử: Mã HD · Bàn · Giờ · Tổng tiền · Hình thức TT · Tiền thừa
- Ô tìm theo mã hóa đơn hoặc số bàn (rẻ, hiệu quả).
- Bấm 1 dòng → xem nội dung hóa đơn đầy đủ trong hộp thoại (dùng lại `HoaDonPrinter`).
- Trên cùng có nút **Tải lại** để cập nhật sau khi bán hàng.

---

## 6. "Hợp đồng" giữa 2 người (chốt trước 23/10 09:00)

Tên class và tên hàm phải **giống hệt**, nếu không ghép không nổi:

```java
// ===== Người A cung cấp =====
public class MonRepo {
    public List<Mon> findAll();
    public Mon findByMa(String maMon);
    public List<Mon> findByNhom(NhomMon nhom);
    public void save(Mon mon);          // thêm mới hoặc cập nhật
    public void delete(String maMon);
}

public class BanRepo {
    public List<Ban> findAll();
    public Ban findByMa(String maBan);
    public void save(Ban ban);
}

public class HoaDonRepo {
    public List<HoaDon> findAll();
    public HoaDon findByMa(String maHD);
    public List<HoaDon> findDaThanhToan();          // lọc DA_THANH_TOAN
    public void save(HoaDon hd);                     // kèm ghi dsChiTiet
}

public class OrderService {
    public String taoMaHoaDon();                     // "HD20261023001"
    public Ban moBan(String maBan);                  // TRONG -> CO_KHACH
    public HoaDon themMon(String maHD, Mon mon, int soLuong);
    public void yeuCauTinhTien(String maHD);
    public void thanhToan(String maHD, PhuongThucTT pt, long tienKhachDua);
    public void huyBan(String maBan);
}

// ===== Người B cung cấp (A GỌI HÀM NÀY) =====
public class HoaDonPrinter {
    public static String taoNoiDung(HoaDon hd, List<ChiTietHoaDon> ds);
    public static void xuatFileTxt(HoaDon hd, List<ChiTietHoaDon> ds, File file);
}

public class ThongKeService {
    public long doanhThuTrongNgay(LocalDate ngay);
    public long soHoaDonTrongNgay(LocalDate ngay);
}
```

**Luồng liên kết 2 bên:** `GoiMonPanel` (A) → `OrderService` (A) → trừ tồn kho qua `MonRepo` (B) → khi thanh toán gọi `HoaDonPrinter` (B) → trả về `BanPanel` (A). ⭐ Vì vậy `HoaDonPrinter` phải xong **trước 24/10 12:00**.

---

## 7. Kế hoạch theo ngày

### 7.1. 2 ngày code (23/10 & 24/10)

| Giờ | Người A (2/3) | Người B (1/3) ⭐ |
|-----|---------------|------------------|
| **23/10 – Buổi sáng** | Tạo project, model `Mon`/`NhomMon`/`Ban`/`TrangThaiBan` (0,5h) · `HoaDon`/`ChiTietHoaDon`/`TrangThaiHD`/`PhuongThucTT` (1,5h) | `Mon` dùng chung + tạo `data/mon.csv` 24 món đủ 4 nhóm (0,5h) · `MonRepo` (1h) |
| **23/10 – Buổi chiều** | `FileStore` (1,5h) · `BanRepo` + `HoaDonRepo` (1,5h) | `QuanLyMonPanel`: bảng + form + Thêm/Sửa/Xóa/Tìm (3h) |
| **23/10 – Tối** | `MainFrame` + khung `CardLayout` (1h) | Hoàn thiện `QuanLyMonPanel`, tự test 4 nhóm món (0,5h) |
| **Mốc 23/10 tối** | ✔ Chạy được: đọc file, hiện menu món | ✔ Màn hình quản lý món chạy độc lập → **merge vào nhánh A** |
| **24/10 – Buổi sáng** | `BanPanel`: sơ đồ 3×3 + tô màu + hiện tổng tiền (2h) | `ThongKeService` + `LichSuHoaDonPanel`: 2 thẻ + bảng (2h) |
| **24/10 – Buổi chiều** | `GoiMonPanel`: 4 tab + giỏ + tăng/giảm SL (2,5h) | `HoaDonPrinter`: sinh nội dung + xuất `.txt` (1,5h) → bàn giao cho A trước 12:00 |
| **24/10 – Buổi tối** | `OrderService` + thanh toán + trả thừa + gọi `HoaDonPrinter` (1,5h) | Ghép tất cả vào `main`, chạy trọn luồng, sửa lỗi (1h) |
| **Mốc 24/10 tối** | ✔ Trọn luồng: mở bàn → gọi món → tính tiền → thanh toán → in hóa đơn | ✔ Lịch sử & doanh thu chạy được |

### 7.2. Hoàn thiện & nộp (25/10 → 28/10)

| Ngày | Việc | Ai |
|------|------|-----|
| **T7 25/10** | Test toàn bộ theo checklist mục 9, sửa lỗi, chạy 2 vòng thử "mở app" (tiêu hóa 20 phút) | Cả 2 |
| **T8 26/10** | Dựng dữ liệu mẫu đẹp (24 món, 15–20 hóa đơn đã thanh toán), chụp ≥ 5 ảnh màn hình, mở file `.txt` hóa đơn để chụp | B ⭐ + A |
| **T9 27/10** | Viết báo cáo Word + MD theo mục 9, làm slide thuyết trình (8–10 slide), chuẩn bị kịch bản demo | B ⭐ (A phần thiết kế) |
| **T10 28/10** | **Nộp & trình bày**: báo cáo (Word + MD), source code, `README.md`, ảnh màn hình | Cả 2 |

> **Quy tắc "đóng băng tính năng": từ 24/10 tối chỉ sửa lỗi, không thêm tính năng mới.**

---

## 8. Cấu trúc báo cáo (Word + MD)

| Mục | Nội dung | Người viết | Dung lượng |
|-----|----------|-----------|-----------|
| 1 | **Trang bìa** – tên bài, thành viên, lớp, ngày nộp | Cả 2 | 1 trang |
| 2 | **Mục lục** | A | 0,5 trang |
| 3 | **Giới thiệu bài toán** – máy POS quán cà phê làm gì, vì sao cần | B ⭐ | 0,5 trang |
| 4 | **Phân tích yêu cầu** – chức năng chính/phụ, yêu cầu phi chức năng | B ⭐ | 1 trang |
| 5 | **Thiết kế hệ thống** – use-case, **sơ đồ lớp** (Mon–Ban–HoaDon–ChiTietHoaDon), kiến trúc 3 tầng, **lý do chỉ dùng 1 bảng HoaDon** | A | 1–2 trang |
| 6 | **Thiết kế lưu trữ** – cấu trúc 4 file CSV, lý do chọn file thay vì MySQL | A | 1 trang |
| 7 | **Mô tả giao diện** – từng màn hình + **≥ 5 ảnh chụp** (sơ đồ bàn, gọi món, giỏ hàng, hộp thoại thanh toán, quản lý món, lịch sử) | Cả 2 | 2 trang |
| 8 | **Phân công công việc** – bảng 2/3 – 1/3 | B ⭐ | 0,5 trang |
| 9 | **Kết quả & hướng phát triển** – đã làm được gì, muốn làm thêm gì | B ⭐ | 0,5 trang |
| 10 | **Kết luận** | Cả 2 | 0,5 trang |
| 11 | **Tài liệu tham khảo** | A | 0,5 trang |
| 12 | **Phụ lục** – mã nguồn, hướng dẫn cài & chạy | A | dài |

**Định dạng Word:** A4, Times New Roman 13–14, giãn dòng 1.5, đánh số mục tự động, ảnh có caption "Hình x: …".

---

## 9. Checklist trước khi nộp

**Luồng nghiệp vụ**
- [ ] Mở app → hiện đúng 9 bàn trống
- [ ] Bấm bàn trống → bàn đổi xanh dương, vào được màn hình gọi món
- [ ] Bấm món 3 lần → giỏ có 1 dòng SL = 3, tổng tiền đúng
- [ ] Tăng/giảm SL, xóa dòng → tổng cập nhật đúng
- [ ] Quay lại sơ đồ → bàn hiện đúng tổng tiền đang ăn
- [ ] *Yêu cầu tính tiền* → bàn đổi vàng, khóa sửa giỏ
- [ ] Thanh toán tiền mặt: nhập thiếu → báo lỗi; nhập đủ → hiện tiền thừa đúng
- [ ] Thanh toán chuyển khoản → không hỏi tiền đưa
- [ ] Thanh toán xong → bàn về xanh lá, hỏi in/lưu hóa đơn → file `.txt` mở ra đúng
- [ ] Tồn kho món giảm đúng sau khi thanh toán; món hết hàng không thêm vào giỏ được
- [ ] *Hủy bàn* → bàn về trống, hỏi xác nhận trước

**Quản lý món & lịch sử**
- [ ] Thêm/Sửa/Xóa/Tìm món chạy đúng; dữ liệu ghi xuống file, đóng lại mở lại vẫn còn
- [ ] Thẻ doanh thu hôm nay khớp với tổng cột tiền trong bảng lịch sử

**Giao diện & kỹ thuật**
- [ ] Mọi lỗi nhập liệu có thông báo tiếng Việt, không crash
- [ ] Tiếng Việt có dấu hiển thị đúng, không lỗi font, không dính dấu phẩy khi lưu file
- [ ] Tiền có định dạng `#,##0 đ`
- [ ] Đóng app mở lại: bàn đang phục vụ vẫn còn nguyên trạng thái
- [ ] Chạy trên máy khác không lỗi đường dẫn file (dùng đường dẫn tương đối `data/`)
- [ ] Không dùng thư viện ngoài; code có chú thích tiếng Việt; không còn `TODO`
- [ ] Git log sạch, không có file `.class` trong commit

---

## 10. Rủi ro & cách phòng tránh

| Rủi ro | Xác suất | Cách phòng tránh |
|--------|----------|------------------|
| **Lỗi đọc/ghi CSV** (dấu phẩy trong tên món, tiếng Việt có dấu, số thập phân, dấu nháy) | Cao | Test `FileStore` **ngay đầu ngày 1**, trước khi viết giao diện; luôn UTF-8 + `Locale.US` |
| Không kịp `HoaDonPrinter` → A không in được hóa đơn | Trung bình | B làm xong **trước 24/10 12:00**, A test nối ngay buổi chiều |
| Merge xung đột làm mất code | Trung bình | Mỗi người 1 nhánh, merge 2 lần (23/10 tối và 24/10 tối), commit hằng ngày |
| Giao diện đẹp mà nghiệp vụ sai | Cao | Làm nghiệp vụ (tính tiền, trừ kho, trả thừa) trước, sắc màu sau |
| Demo bị treo vì dữ liệu bẩn (hóa đơn cũ chiếm bàn) | Trung bình | Nếu demo lỗi → xóa 4 file trong `data/` rồi chạy lại, app tự tạo dữ liệu mới |
| Trình bày hỏng do mở ở máy khác | Thấp | Đóng gói `.jar` + hướng dẫn chạy trong `README.md` |

---

## 11. Phần "code chay" – nên tự tay viết ⭐

| File | Nội dung tự viết | Gợi ý |
|------|-------------------|--------|
| `QuanLyMonPanel.java` | Toàn bộ | Dễ nhất, làm nắm Swing; dùng `BorderLayout` + `GridBagLayout` |
| `ThongKeService.java` | `doanhThuTrongNgay`, `soHoaDonTrongNgay` | Dùng `LocalDate`, `Stream`, `filter` + `mapToLong` |
| `HoaDonPrinter.java` | `taoNoiDung` (dùng `StringBuilder`) + `xuatFileTxt` (`PrintWriter`) | Hàm text hay nhất để "code chay", dễ demo |
| `FormatUtil.java` | `formatTien()`, `formatNgayGio()` | Dùng `NumberFormat` + `DateTimeFormatter` |
| `Mon.java` | Toàn bộ model | ÔOP cơ bản |
| `BanPanel.java` | Hàm `toMauBan()` | Tự tô màu 3 trạng thái – điểm "wow" khi demo |

**Để AI hỗ trợ phần lặp nhiều:** `FileStore` đọc/ghi CSV, getter/setter, boilerplate listener.

---

## 12. Việc cần làm ngay bây giờ (tối 22/10)

- [ ] **Gửi mục 6 cho người A** – chốt thống nhất tên class + tên hàm trước 09:00 sáng mai
- [ ] Người A tạo project `BTL_POS_QuanCafe` + 4 gói `model/data/service/ui` + git init + commit đầu
- [ ] Bạn soạn sẵn **24 món** đủ 4 nhóm (Cà phê / Nước uống / Trà / Bánh) — chỉ cần tên + nhóm + giá + tồn kho
- [ ] Hẹn 20:00 tối nay gọi 30 phút để chốt tiến độ & hẹn giờ merge 23/10 tối