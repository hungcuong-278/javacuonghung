package com.pos.quancafe.util;

import java.text.Normalizer;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Hàm tiện ích định dạng tiền và ngày giờ.
 * Phần này thuộc công việc của Người 2.
 */
public class FormatUtil {

    /** Định dạng ngày giờ dùng trong file CSV: 2026-10-23T14:05:30 */
    public static final DateTimeFormatter FORMAT_CSV =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    /** Định dạng ngày giờ hiển thị cho người dùng: 23/10/2026 14:05 */
    public static final DateTimeFormatter FORMAT_HIEN_THI =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /** Định dạng ngày hiển thị: 23/10/2026 */
    public static final DateTimeFormatter FORMAT_NGAY =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private FormatUtil() {
        // Lớp tiện ích, không khởi tạo
    }

    /**
     * Định dạng số tiền kiểu Việt Nam, ví dụ: 25000 -> "25.000 đ"
     */
    public static String formatTien(long soTien) {
        NumberFormat nf = NumberFormat.getInstance(Locale.US);
        nf.setGroupingUsed(true);
        nf.setMaximumFractionDigits(0);
        return nf.format(soTien) + " đ";
    }

    /**
     * Định dạng tiền không có ký hiệu "đ", dùng khi in ra hóa đơn
     */
    public static String formatTienKhongDonVi(long soTien) {
        NumberFormat nf = NumberFormat.getInstance(Locale.US);
        nf.setGroupingUsed(true);
        nf.setMaximumFractionDigits(0);
        return nf.format(soTien);
    }

    /**
     * Đọc số tiền từ chuỗi CSV, ví dụ: "25000" -> 25000
     * Nếu chuỗi sai định dạng thì trả về 0.
     */
    public static long docTien(String chuoi) {
        if (chuoi == null || chuoi.trim().isEmpty()) {
            return 0;
        }
        try {
            return Long.parseLong(chuoi.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Đọc số nguyên từ chuỗi CSV, ví dụ: "12" -> 12
     * Nếu chuỗi sai định dạng thì trả về 0.
     */
    public static int docSoNguyen(String chuoi) {
        if (chuoi == null || chuoi.trim().isEmpty()) {
            return 0;
        }
        try {
            return Integer.parseInt(chuoi.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /** Chuyển thời gian thành chuỗi để ghi ra file CSV */
    public static String thanhChuoiCSV(LocalDateTime thoiGian) {
        return thoiGian == null ? "" : thoiGian.format(FORMAT_CSV);
    }

    /** Đọc thời gian từ chuỗi CSV, nếu sai định dạng thì trả về null */
    public static LocalDateTime docThoiGian(String chuoi) {
        if (chuoi == null || chuoi.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDateTime.parse(chuoi.trim(), FORMAT_CSV);
        } catch (Exception e) {
            return null;
        }
    }

    /** Định dạng thời gian hiển thị cho người dùng */
    public static String hienThiThoiGian(LocalDateTime thoiGian) {
        return thoiGian == null ? "" : thoiGian.format(FORMAT_HIEN_THI);
    }

    /** Định dạng ngày hiển thị cho người dùng */
    public static String hienThiNgay(LocalDateTime thoiGian) {
        return thoiGian == null ? "" : thoiGian.format(FORMAT_NGAY);
    }

    /**
     * Đưa chuỗi về dạng viết thường không dấu, dùng để tìm kiếm không phân biệt hoa thường.
     * Ví dụ: "Phin Đen Đá" -> "phin den da"
     * <p>
     * Cách làm: dùng Normalizer của Java để tách ký tự có dấu thành ký tự gốc
     * rồi loại bỏ phần dấu. Cách này luôn đúng, không phải tự đối chiếu danh sách ký tự.
     */
    public static String boDau(String chuoi) {
        if (chuoi == null || chuoi.isEmpty()) {
            return "";
        }

        // Chuẩn hóa về dạng NFD: mỗi ký tự có dấu tách thành ký tự gốc + dấu
        String chuoiPhanTach = Normalizer.normalize(chuoi, Normalizer.Form.NFD);

        // Lấy nét dưới dùng để ghép các chữ Đ đ thành dạng d
        StringBuilder ketQua = new StringBuilder();
        for (int i = 0; i < chuoiPhanTach.length(); i++) {
            char c = chuoiPhanTach.charAt(i);

            // Bỏ qua các ký tự là dấu (thuộc nhóm Mn không dấu)
            if (Character.getType(c) == Character.NON_SPACING_MARK) {
                continue;
            }

            // Riêng chữ Đ và đ thì dùng nét dưới để thay thế
            if (c == '\u0110') {
                ketQua.append('D');
                continue;
            }
            if (c == '\u0111') {
                ketQua.append('d');
                continue;
            }

            ketQua.append(c);
        }

        return ketQua.toString().toLowerCase().trim();
    }
}