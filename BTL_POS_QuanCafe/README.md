# Phần công việc của Người 2

Thư mục này chứa **riêng phần code của Người 2** trong bài BTL máy POS quán cà phê.
Chạy **độc lập**, không phụ thuộc phần code của Người 1, để có thể làm và test song song.

## Cài đặt và chạy

Yêu cầu: JDK 17 trở lên.

### Cách 1: Chạy trong IntelliJ IDEA

1. Mở thư mục `BTL_POS_QuanCafe` bằng IntelliJ IDEA
2. Đặt Project SDK là 17
3. Mở file `src/com/pos/quancafe/MainNguoi2.java`
4. Bấm nút **Run**

### Cách 2: Chạy bằng dòng lệnh

```bash
javac -encoding UTF-8 -d out src/com/pos/quancafe/**/*.java
java -Dfile.encoding=UTF-8 -cp out com.pos.quancafe.MainNguoi2
```

Chạy từ thư mục gốc `BTL_POS_QuanCafe` vì dữ liệu nằm ở đường dẫn tương đối `data/`.

## Danh sách file

### Model (định nghĩa chung)

| File | Nội dung |
|------|----------|
| `model/Mon.java` | Một món trong thực đơn ⭐ |
| `model/NhomMon.java` | 4 nhóm: Cà phê, Nước uống, Trà, Bánh |
| `model/HoaDon.java` | Hóa đơn của một bàn |
| `model/ChiTietHoaDon.java` | Một dòng hóa đơn: món, số lượng, đơn giá |
| `model/TrangThaiBan.java` | Trống / Có khách / Chờ thanh toán |
| `model/TrangThaiHD.java` | Đang phục vụ / Chờ thanh toán / Đã thanh toán |
| `model/PhuongThucTT.java` | Tiền mặt / Chuyển khoản |

### Tầng dữ liệu

| File | Nội dung |
|------|----------|
| `data/FileStore.java` | Đọc ghi file CSV, xử lý dấu phẩy và dấu nháy kép ⭐ |
| `data/MonRepo.java` | Thêm, sửa, xóa, tìm kiếm món ⭐ |
| `data/HoaDonRepo.java` | Đọc ghi hóa đơn và chi tiết hóa đơn ⭐ |

### Tầng nghiệp vụ và tiện ích

| File | Nội dung |
|------|----------|
| `service/ThongKeService.java` | Tính doanh thu, số hóa đơn theo ngày ⭐ |
| `util/FormatUtil.java` | Định dạng tiền, ngày giờ, bỏ dấu tiếng Việt |
| `util/HoaDonPrinter.java` | Sinh hóa đơn chữ và xuất ra file txt ⭐ |

### Giao diện

| File | Nội dung |
|------|----------|
| `ui/panel/QuanLyMonPanel.java` | Màn hình thêm, sửa, xóa, tìm món ⭐ |
| `ui/panel/LichSuHoaDonPanel.java` | Màn hình lịch sử và doanh thu ⭐ |
| `MainNguoi2.java` | Khung chương trình riêng cho phần Người 2 |

⭐ = đã viết xong

## Chức năng đã có

**Màn hình Quản lý món**

- Xem danh sách 24 món đủ 4 nhóm, tồn kho bằng 0 hiện chữ đỏ
- Thêm món, mã tự sinh dạng MON001, MON002
- Sửa món bằng cách bấm vào dòng rồi bấm Sửa
- Xóa món có hỏi xác nhận trước
- Tìm món không phân biệt hoa thường và dấu, gõ `phin den` ra được `Phin đen đá`
- Chặn nhập sai: tên trống, giá bằng 0, tồn kho âm
- Chặn trùng tên món
- Tải lại dữ liệu từ file

**Màn hình Lịch sử hóa đơn**

- Ba thẻ số liệu: doanh thu hôm nay, số hóa đơn hôm nay, tiền trung bình mỗi hóa đơn
- Bảng lịch sử mới nhất lên đầu
- Tìm theo mã hóa đơn hoặc số bàn
- Xem hôm nay, xem tất cả
- Bấm đúp vào dòng để xem chi tiết hóa đơn, giống hệt bản in
- Lưu hóa đơn ra file txt

**Hàm dùng chung cho Người 1**

- `HoaDonPrinter.taoNoiDung(...)` sinh nội dung hóa đơn
- `HoaDonPrinter.xuatFileTxt(...)` ghi ra file
- `HoaDonPrinter.taoFileHoaDon(maHD)` tạo đường dẫn `hoa_don/maHD.txt`

## Dữ liệu

| File | Nội dung |
|------|----------|
| `data/mon.csv` | 24 món, đủ 4 nhóm, tự tạo khi chạy lần đầu |
| `data/hoadon.csv` | 8 hóa đơn mẫu đã thanh toán |
| `data/hoadon_chitiet.csv` | Chi tiết từng món của các hóa đơn |

Xóa thư mục `data/` rồi chạy lại để tạo dữ liệu mới từ đầu.

## Kiểm thử

| File | Cách chạy | Kiểm tra gì |
|------|-----------|-------------|
| `KiemTra.java` | Chạy cấu hình **Kiem Tra Logic** | 11 mục logic: đọc ghi CSV, thêm sửa xóa, tìm kiếm, thống kê, xuất hóa đơn |
| `KiemTraBoDau.java` | Chạy trực tiếp | Hàm bỏ dấu tiếng Việt, tìm kiếm không phân biệt dấu |
| `KiemTraGiaoDien.java` | Chạy trực tiếp | Vẽ bảng thật để bắt lỗi renderer, không cần mở cửa sổ |
| `KiemTraHoaDon.java` | Chạy trực tiếp | Kiểm tra hóa đơn: mọi dòng trong khung phải dài đều nhau, tiền quá 1 tỷ phải xuống dòng |

```bash
javac -encoding UTF-8 -cp out -d out KiemTra.java KiemTraBoDau.java KiemTraGiaoDien.java KiemTraHoaDon.java
java -Dfile.encoding=UTF-8 -cp out KiemTra
java -Dfile.encoding=UTF-8 -cp out KiemTraHoaDon
```

## Cấu hình IntelliJ IDEA

File `.idea/runConfigurations.xml` đã có sẵn 2 cấu hình:

| Cấu hình | Main class | Chạy cái gì |
|-----------|-----------|------------|
| **Chay Phan Nguoi 2** | `com.pos.quancafe.MainNguoi2` | Mở cửa sổ 2 tab |
| **Kiem Tra Logic** | `KiemTra` | Chạy test logic ra console |

Cả hai đều đặt sẵn:

- **Working directory** = `$PROJECT_DIR$` — bắt buộc, code đọc `data/mon.csv` theo đường dẫn tương đối
- **VM options** = `-Dfile.encoding=UTF-8` — giữ tiếng Việt có dấu khi in ra console

Chạy nhanh: mở `MainNguoi2.java` rồi bấm **Shift+F10**.

## Bố cục hóa đơn

Hóa đơn in ra dạng chữ, nằm gọn trong **một khung có viền** rộng 56 ký tự:

```
+------------------------------------------------------+
| QUÁN CÀ PHÊ                                          |
| HÓA ĐƠN THANH TOÁN                                   |
|------------------------------------------------------|
| Mã hóa đơn    : HD20261023001                        |
| Bàn           : B01                                  |
| Mở bàn        : 23/10/2026 08:30                     |
| Thanh toán    : 23/10/2026 09:15                     |
|------------------------------------------------------|
| MÓN                     SL      ĐƠN GIÁ   THÀNH TIỀN |
|------------------------------------------------------|
| Phin đen đá              2       25,000       50,000 |
| Bánh mì chà bông         2       15,000       30,000 |
|------------------------------------------------------|
| TỔNG TIỀN                                  153,000 đ |
| HÌNH THỨC THANH TOÁN                        Tiền mặt |
| TIỀN KHÁCH ĐƯA                             200,000 đ |
| TIỀN THỪA TRẢ LẠI                           47,000 đ |
|------------------------------------------------------|
| Cảm ơn quý khách, hẹn gặp lại!                       |
+------------------------------------------------------+
```

| Quy tắc | Chi tiết |
|---------|----------|
| Mọi dòng trong khung dài đúng 56 ký tự | Kiểm tra tự động bằng `KiemTraHoaDon` |
| Số tiền **trên 1 tỷ sẽ xuống dòng** | Nhãn ở dòng trên, số tiền ở dòng dưới, vẫn nằm trong khung |
| Tên món quá dài bị cắt gọn | Cắt bớt và thêm dấu `...` |
| Font hiển thị là `Monospaced` | Mỗi ký tự chiếm đúng 1 cột nên khung thẳng nhau |

### Lỗi đã sửa: hóa đơn "nhảy lung tung"

Hàm cũ đếm ký tự có dấu là **2 cột**:

```java
if (c >= 0x1100) {
    dem += 2;   // sai
}
```

Nhưng hóa đơn hiển thị bằng font đơn cách, ở đó **mọi ký tự đều chiếm đúng 1 cột**,
kể cả ký tự có dấu. Nên `Soda chanh muối` bị tính là 16 cột trong khi thực tế
chiếm 15 cột → các cột lệch nhau.

Cách sửa: bỏ hàm `doRongHienThi`, dùng thẳng `String.length()`.

## Quy tắc căn lề trong bảng

| Quy tắc | Áp dụng |
|---------|---------|
| Mặc định **canh giữa** cho mọi cột | Dễ nhìn, dòng nào cũng thẳng hàng |
| **Mô tả canh trái** | Cột mô tả ở bảng món, nội dung dài và cần đọc như đoạn văn |
| Tiêu đề cột canh giữa theo mặc định của JTable | Không cần canh thủ công |

Không còn dùng cột canh phải cho các cột tiền — tiền cũng canh giữa cho đồng bộ.

Cột mô tả được chừa rộng 320px và có khoảng cách 8px ở mép trái cho dễ đọc.

## Vài lỗi đã gặp và cách xử lý

**`ArrayIndexOutOfBoundsException: Index -1` khi mở bảng.**

Lỗi này do renderer của bảng dùng `bang.getSelectedRow()` để tra dữ liệu. Khi chưa
chọn dòng nào hàm này trả về `-1`, rồi `getValueAt(-1, ...)` ném exception ngay
trong lúc vẽ bảng.

Cách sửa đúng là override `getTableCellRendererComponent` — hàm này có sẵn tham số
`row` là dòng đang vẽ, luôn hợp lệ. Không dùng `setValue` vì hàm đó không cho biết
đang vẽ dòng nào. Xem lớp `MonRenderer` trong `QuanLyMonPanel`.

**Tiếng Việt hiện `?` trong console.**

Thêm `-Dfile.encoding=UTF-8` vào VM options của cấu hình chạy.

**Bảng trống, không có dữ liệu.**

Sai Working directory. Phải trỏ về thư mục gốc `BTL_POS_QuanCafe` (nơi có thư mục `data`).

**Chạy lại từ đầu.**

Xóa thư mục `data/` và `hoa_don/`, chạy lại là chương trình tự tạo dữ liệu mới.

## Khi ghép với phần Người 1

Copy nguyên các file trong `src/com/pos/quancafe/` sang project chính, trừ `MainNguoi2.java`.
Sau đó kiểm tra lại tên các lớp và phương thức trong mục "Hàm dùng chung" phía trên.