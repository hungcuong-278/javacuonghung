package com.pos.quancafe.util;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Sinh nội dung hóa đơn dạng chữ và xuất ra file txt.
 * Phần này thuộc công việc của Người 2, được Người 1 gọi sau khi thanh toán.
 * <p>
 * Bố cục hóa đơn: mọi nội dung nằm gọn trong một khung có viền,
 * các cột cách nhau đều nhau. Nếu một ô nội dung quá dài thì được xuống dòng
 * thay vì làm lệch cột, ví dụ số tiền vượt 1 tỷ sẽ tách thành "1.234.567.890"
 * rồi xuống dòng ghi tiếp phần "đồng".
 */
public class HoaDonPrinter {

    /** Tên quán hiển thị trên hóa đơn */
    private static final String TEN_QUAN = "QUÁN CÀ PHÊ";

    /** Bề rộng khung hóa đơn, tính cả 2 ký tự viền */
    private static final int BE_RONG_KHUNG = 56;

    /** Bề rộng trong khung, không tính 2 ký tự viền */
    private static final int BE_RONG_NOI_DUNG = BE_RONG_KHUNG - 4;

    /** Số tiền lớn nhất hiển thị trên 1 dòng, quá ngưỡng này sẽ xuống dòng */
    private static final long NGUONG_TIEN_MOT_DONG = 1_000_000_000L;

    private HoaDonPrinter() {
        // Lớp tiện ích, không khởi tạo
    }

    /**
     * Tạo một dòng hóa đơn bằng dữ liệu dạng chữ.
     * Hàm này không phụ thuộc lớp hóa đơn cụ thể nên nhận thông tin qua tham số,
     * nhờ vậy dùng được cả khi đang xem lại hóa đơn cũ.
     *
     * @param maHD          mã hóa đơn
     * @param maBan         mã bàn
     * @param ngayMo        thời điểm mở bàn
     * @param ngayDong      thời điểm thanh toán
     * @param dsMon         danh sách dòng hóa đơn, mỗi phần tử gồm: tên món, số lượng, đơn giá, thành tiền
     * @param tongTien      tổng tiền
     * @param phuongThucTT  hình thức thanh toán
     * @param tienKhachDua  tiền khách đưa, bằng 0 nếu chuyển khoản
     * @param tienThua      tiền thừa trả lại, bằng 0 nếu chuyển khoản
     * @return nội dung hóa đơn hoàn chỉnh
     */
    public static String taoNoiDung(String maHD, String maBan, LocalDateTime ngayMo,
                                    LocalDateTime ngayDong, List<String[]> dsMon,
                                    long tongTien, String phuongThucTT,
                                    long tienKhachDua, long tienThua) {

        StringBuilder sb = new StringBuilder();

        // ---------- Phần đầu ----------
        sb.append(khungTren());
        sb.append(dongGiua(TEN_QUAN));
        sb.append(dongGiua("HÓA ĐƠN THANH TOÁN"));
        sb.append(khungGiua());

        // ---------- Thông tin hóa đơn ----------
        sb.append(dongNhan("Mã hóa đơn", chuanHoa(maHD)));
        sb.append(dongNhan("Bàn", chuanHoa(maBan)));
        sb.append(dongNhan("Mở bàn", chuanHoa(FormatUtil.hienThiThoiGian(ngayMo))));
        sb.append(dongNhan("Thanh toán", chuanHoa(FormatUtil.hienThiThoiGian(ngayDong))));
        sb.append(khungGiua());

        // ---------- Bảng các món ----------
        // Bề rộng 4 cột, tổng đúng bằng BE_RONG_NOI_DUNG để khung thẳng
        int beTen = 22;
        int beSoLuong = 4;
        int beDonGia = 13;
        int beThanhTien = 13;

        sb.append("| ");
        sb.append(canChu("MÓN", beTen, false));
        sb.append(canChu("SL", beSoLuong, true));
        sb.append(canChu("ĐƠN GIÁ", beDonGia, true));
        sb.append(canChu("THÀNH TIỀN", beThanhTien, true));
        sb.append(" |\n");
        sb.append(khungGiua());

        if (dsMon != null) {
            for (String[] dong : dsMon) {
                String tenMon = dong.length > 0 ? dong[0] : "";
                int soLuong = dong.length > 1 ? FormatUtil.docSoNguyen(dong[1]) : 0;
                long donGia = dong.length > 2 ? FormatUtil.docTien(dong[2]) : 0;
                long thanhTien = dong.length > 3 ? FormatUtil.docTien(dong[3]) : soLuong * donGia;

                sb.append(dongMon(tenMon, soLuong, donGia, thanhTien,
                        beTen, beSoLuong, beDonGia, beThanhTien));
            }
        }
        sb.append(khungGiua());

        // ---------- Phần thanh toán ----------
        // Nhãn chiếm phần trái, giá trị chiếm phần phải
        int beNhan = 30;

        sb.append(dongTien("TỔNG TIỀN", tongTien, beNhan));
        sb.append(dongChu("HÌNH THỨC THANH TOÁN", chuanHoa(phuongThucTT), beNhan));

        // Tiền khách đưa và tiền thừa chỉ hiện khi thanh toán tiền mặt
        if ("Tiền mặt".equalsIgnoreCase(phuongThucTT)) {
            sb.append(dongTien("TIỀN KHÁCH ĐƯA", tienKhachDua, beNhan));
            sb.append(dongTien("TIỀN THỪA TRẢ LẠI", tienThua, beNhan));
        }

        // ---------- Phần cuối ----------
        sb.append(khungGiua());
        sb.append(dongGiua("Cảm ơn quý khách, hẹn gặp lại!"));
        sb.append(khungDuoi());

        return sb.toString();
    }

    /**
     * Tạo nội dung hóa đơn từ danh sách chi tiết hóa đơn có sẵn.
     * Phiên bản tiện lợi dùng cho màn hình lịch sử xem lại hóa đơn cũ.
     * Không phụ thuộc lớp chi tiết hóa đơn cụ thể nên nhận kiểu List.
     *
     * @param maHD         mã hóa đơn
     * @param maBan        mã bàn
     * @param ngayMo       thời điểm mở bàn
     * @param ngayDong     thời điểm thanh toán
     * @param dsChiTiet    danh sách chi tiết, mỗi phần tử có 4 trường: tên món, số lượng, đơn giá, thành tiền
     * @param tongTien     tổng tiền
     * @param phuongThucTT hình thức thanh toán
     * @param tienKhachDua tiền khách đưa
     * @param tienThua     tiền thừa
     * @return nội dung hóa đơn hoàn chỉnh
     */
    public static String taoNoiDungTuChiTiet(String maHD, String maBan, LocalDateTime ngayMo,
                                             LocalDateTime ngayDong, List<?> dsChiTiet,
                                             long tongTien, String phuongThucTT,
                                             long tienKhachDua, long tienThua) {

        List<String[]> dsDon = new ArrayList<>();

        if (dsChiTiet != null) {
            for (Object obj : dsChiTiet) {
                dsDon.add(layThongTinMon(obj));
            }
        }

        return taoNoiDung(maHD, maBan, ngayMo, ngayDong, dsDon,
                tongTien, phuongThucTT, tienKhachDua, tienThua);
    }

    /**
     * Lấy 4 thông tin tên món, số lượng, đơn giá, thành tiền từ một đối tượng.
     * Dùng để không phụ thuộc vào lớp chi tiết hóa đơn cụ thể.
     */
    private static String[] layThongTinMon(Object obj) {
        try {
            java.lang.reflect.Method layTen = obj.getClass().getMethod("getTenMon");
            java.lang.reflect.Method laySoLuong = obj.getClass().getMethod("getSoLuong");
            java.lang.reflect.Method layDonGia = obj.getClass().getMethod("getDonGia");
            java.lang.reflect.Method layThanhTien = obj.getClass().getMethod("thanhTien");

            Object ten = layTen.invoke(obj);
            Object soLuong = laySoLuong.invoke(obj);
            Object donGia = layDonGia.invoke(obj);
            Object thanhTien = layThanhTien.invoke(obj);

            return new String[]{
                    ten == null ? "" : ten.toString(),
                    soLuong == null ? "0" : soLuong.toString(),
                    donGia == null ? "0" : donGia.toString(),
                    thanhTien == null ? "0" : thanhTien.toString()
            };
        } catch (Exception e) {
            return new String[]{"Không đọc được", "0", "0", "0"};
        }
    }

    /**
     * Ghi nội dung hóa đơn ra file txt với mã hóa UTF-8.
     *
     * @param noiDungHoaDon nội dung hóa đơn đã tạo
     * @param file           file đích, ví dụ thư mục hoa_don/HD20261023001.txt
     * @return true nếu ghi thành công
     */
    public static boolean xuatFileTxt(String noiDungHoaDon, File file) {
        if (file == null) {
            return false;
        }

        try {
            // Tạo thư mục cha nếu chưa có
            File thuMuc = file.getParentFile();
            if (thuMuc != null && !thuMuc.exists()) {
                thuMuc.mkdirs();
            }

            try (PrintWriter pw = new PrintWriter(
                    Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8))) {
                pw.print(noiDungHoaDon);
            }

            return true;
        } catch (IOException e) {
            System.out.println("[Loi] Khong ghi duoc hoa don: " + e.getMessage());
            return false;
        }
    }

    /**
     * Tạo đường dẫn file hóa đơn theo mã hóa đơn.
     * Ví dụ: hoa_don/HD20261023001.txt
     */
    public static File taoFileHoaDon(String maHD) {
        return new File("hoa_don/" + maHD + ".txt");
    }

    // ==================== Hàm dựng từng dòng ====================

    /** Đường viền trên cùng của khung */
    private static String khungTren() {
        return "+" + "-".repeat(BE_RONG_KHUNG - 2) + "+\n";
    }

    /** Đường viền giữa, tách các mục */
    private static String khungGiua() {
        return "|" + "-".repeat(BE_RONG_KHUNG - 2) + "|\n";
    }

    /** Đường viền dưới cùng của khung */
    private static String khungDuoi() {
        return "+" + "-".repeat(BE_RONG_KHUNG - 2) + "+\n";
    }

    /**
     * Dòng chữ canh giữa trong khung, dùng cho tiêu đề.
     */
    private static String dongGiua(String chuoi) {
        return "| " + canChu(chuoi, BE_RONG_NOI_DUNG, false) + " |\n";
    }

    /**
     * Dòng thông tin dạng "Nhãn    : Giá trị".
     * Nhãn được canh thẳng cột bằng cách đệm trước khi thêm dấu hai chấm.
     */
    private static String dongNhan(String nhan, String giaTri) {
        String nhanCanh = canChu(nhan, 14, false);
        return "| " + nhanCanh + ": " + canChu(giaTri, BE_RONG_NOI_DUNG - 16, false) + " |\n";
    }

    /**
     * Dòng kết quả thanh toán: nhãn bên trái, số tiền bên phải.
     * Nếu số tiền quá 1 tỷ thì tách phần số ra dòng riêng cho vừa khung.
     */
    private static String dongTien(String nhan, long soTien, int beNhan) {
        String soTienChu = FormatUtil.formatTienKhongDonVi(soTien);

        // Quá 1 tỷ thì xuống dòng: nhãn ở dòng trên, số tiền ở dòng dưới
        if (soTien > NGUONG_TIEN_MOT_DONG) {
            return "| " + canChu(nhan, beNhan, false)
                    + canChu("", BE_RONG_NOI_DUNG - beNhan, false) + " |\n"
                    + "| " + canChu("", beNhan, false)
                    + canChu(soTienChu + " đ", BE_RONG_NOI_DUNG - beNhan, true) + " |\n";
        }

        return "| " + canChu(nhan, beNhan, false)
                + canChu(soTienChu + " đ", BE_RONG_NOI_DUNG - beNhan, true) + " |\n";
    }

    /**
     * Dòng nhãn bên trái, chuỗi bên phải, dùng cho hình thức thanh toán.
     */
    private static String dongChu(String nhan, String giaTri, int beNhan) {
        return "| " + canChu(nhan, beNhan, false)
                + canChu(giaTri, BE_RONG_NOI_DUNG - beNhan, true) + " |\n";
    }

    /**
     * Dòng của một món trong bảng.
     * <p>
     * Nếu tên món quá dài thì cắt bớt, nếu số tiền quá 1 tỷ thì xuống dòng.
     * Mọi trường đều được canh vào đúng độ rộng cột nên các dòng thẳng hàng với nhau.
     */
    private static String dongMon(String tenMon, int soLuong, long donGia, long thanhTien,
                                  int beTen, int beSoLuong, int beDonGia, int beThanhTien) {

        String ten = catNgan(tenMon, beTen);
        String sl = String.valueOf(soLuong);
        String dg = FormatUtil.formatTienKhongDonVi(donGia);
        String tt = FormatUtil.formatTienKhongDonVi(thanhTien);

        // Đơn giá và thành tiền vượt 1 tỷ thì tách xuống dòng riêng
        boolean dgXep = donGia > NGUONG_TIEN_MOT_DONG;
        boolean ttXep = thanhTien > NGUONG_TIEN_MOT_DONG;

        StringBuilder sb = new StringBuilder();

        // Dòng chính
        sb.append("| ");
        sb.append(canChu(ten, beTen, false));
        sb.append(canChu(sl, beSoLuong, true));
        sb.append(canChu(dgXep ? "" : dg, beDonGia, true));
        sb.append(canChu(ttXep ? "" : tt, beThanhTien, true));
        sb.append(" |\n");

        // Dòng phụ khi số tiền quá dài, số nằm ở cột của nó
        if (dgXep || ttXep) {
            sb.append("| ");
            sb.append(canChu("", beTen, false));
            sb.append(canChu("", beSoLuong, true));
            sb.append(canChu(dgXep ? dg : "", beDonGia, true));
            sb.append(canChu(ttXep ? tt : "", beThanhTien, true));
            sb.append(" |\n");
        }

        return sb.toString();
    }

    /**
     * Chuẩn hóa chuỗi để hiển thị: thay ký tự lạ và loại bỏ khoảng trắng thừa.
     */
    private static String chuanHoa(String chuoi) {
        if (chuoi == null) {
            return "";
        }
        return chuoi.replaceAll("\\s+", " ").trim();
    }

    /**
     * Cắt ngắn chuỗi nếu dài quá số ký tự cho phép, phần bị cắt thay bằng dấu chấm.
     */
    private static String catNgan(String chuoi, int doDaiToiDa) {
        if (chuoi == null) {
            return "";
        }
        if (chuoi.length() <= doDaiToiDa) {
            return chuoi;
        }
        return chuoi.substring(0, doDaiToiDa - 3) + "...";
    }

    /**
     * Canh chữ vào đúng một số cột.
     * <p>
     * Lý do phải tự làm: chuỗi "Phin đen đá" có 11 ký tự nhưng hiển thị rộng hơn
     * chuỗi "ABCDEFGHIJK" 11 ký tự, nên dùng %-24s sẽ lệch cột khi có tiếng Việt.
     * <p>
     * Lưu ý quan trọng: hóa đơn hiển thị bằng font đơn cách (Monospaced),
     * ở đó mỗi ký tự đều chiếm đúng 1 cột, kể cả ký tự có dấu.
     * Vì vậy số cột cần đệm chính là số ký tự thật, không nhân đôi.
     *
     * @param chuoi     nội dung cần canh
     * @param doRong    số cột muốn chiếm
     * @param canPhai   true để canh phải, false để canh trái
     * @return chuỗi đã thêm khoảng trắng bù
     */
    private static String canChu(String chuoi, int doRong, boolean canPhai) {
        String noiDung = chuoi == null ? "" : chuoi;

        // Số khoảng trắng cần thêm vào
        int thieu = doRong - noiDung.length();
        if (thieu <= 0) {
            return noiDung;
        }

        StringBuilder khoangTrang = new StringBuilder();
        for (int i = 0; i < thieu; i++) {
            khoangTrang.append(' ');
        }

        if (canPhai) {
            return khoangTrang + noiDung;
        }
        return noiDung + khoangTrang;
    }
}