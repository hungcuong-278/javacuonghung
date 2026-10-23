package com.pos.quancafe.data;

import com.pos.quancafe.model.ChiTietHoaDon;
import com.pos.quancafe.model.HoaDon;
import com.pos.quancafe.model.PhuongThucTT;
import com.pos.quancafe.model.TrangThaiHD;
import com.pos.quancafe.util.FormatUtil;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Quản lý dữ liệu hóa đơn: đọc, tìm kiếm, lưu hóa đơn và chi tiết hóa đơn.
 * Phần này thuộc công việc của Người 2.
 */
public class HoaDonRepo {

    private final File fileHD = new File("data/hoadon.csv");
    private final File fileCT = new File("data/hoadon_chitiet.csv");

    private static final String[] TIEU_DE_HD = {
            "maHD", "maBan", "ngayMo", "ngayDong", "trangThai",
            "tongTien", "tienKhachDua", "tienThua", "phuongThucTT"
    };

    private static final String[] TIEU_DE_CT = {
            "maHD", "maMon", "tenMon", "soLuong", "donGia"
    };

    private List<HoaDon> dsHoaDon = new ArrayList<>();

    public HoaDonRepo() {
        napDuLieu();
    }

    /** Đọc toàn bộ hóa đơn và chi tiết từ file */
    public void napDuLieu() {
        dsHoaDon = new ArrayList<>();

        // Đọc trước chi tiết, gom theo mã hóa đơn
        Map<String, List<ChiTietHoaDon>> chiTietTheoHD = new LinkedHashMap<>();
        for (String[] dong : FileStore.docTatCaDong(fileCT)) {
            String maHD = FileStore.layPhanTu(dong, 0);
            if (maHD.trim().isEmpty()) {
                continue;
            }
            ChiTietHoaDon ct = new ChiTietHoaDon(
                    maHD,
                    FileStore.layPhanTu(dong, 1),
                    FileStore.layPhanTu(dong, 2),
                    FormatUtil.docSoNguyen(FileStore.layPhanTu(dong, 3)),
                    FormatUtil.docTien(FileStore.layPhanTu(dong, 4))
            );
            chiTietTheoHD.computeIfAbsent(maHD, k -> new ArrayList<>()).add(ct);
        }

        // Đọc hóa đơn, gắn chi tiết tương ứng
        for (String[] dong : FileStore.docTatCaDong(fileHD)) {
            String maHD = FileStore.layPhanTu(dong, 0);
            if (maHD.trim().isEmpty()) {
                continue;
            }

            HoaDon hd = new HoaDon();
            hd.setMaHD(maHD);
            hd.setMaBan(FileStore.layPhanTu(dong, 1));
            hd.setNgayMo(FormatUtil.docThoiGian(FileStore.layPhanTu(dong, 2)));
            hd.setNgayDong(FormatUtil.docThoiGian(FileStore.layPhanTu(dong, 3)));
            hd.setTrangThai(TrangThaiHD.fromChuoi(FileStore.layPhanTu(dong, 4)));
            hd.setTongTien(FormatUtil.docTien(FileStore.layPhanTu(dong, 5)));
            hd.setTienKhachDua(FormatUtil.docTien(FileStore.layPhanTu(dong, 6)));
            hd.setTienThua(FormatUtil.docTien(FileStore.layPhanTu(dong, 7)));

            String phuongThuc = FileStore.layPhanTu(dong, 8);
            hd.setPhuongThucTT(phuongThuc.trim().isEmpty() ? null : PhuongThucTT.fromChuoi(phuongThuc));

            hd.setDsChiTiet(chiTietTheoHD.getOrDefault(maHD, new ArrayList<>()));

            dsHoaDon.add(hd);
        }
    }

    /** Lấy toàn bộ hóa đơn */
    public List<HoaDon> findAll() {
        return new ArrayList<>(dsHoaDon);
    }

    /** Tìm hóa đơn theo mã */
    public HoaDon findByMa(String maHD) {
        for (HoaDon hd : dsHoaDon) {
            if (hd.getMaHD().equalsIgnoreCase(maHD)) {
                return hd;
            }
        }
        return null;
    }

    /** Lấy hóa đơn đã thanh toán, dùng cho màn hình lịch sử */
    public List<HoaDon> findDaThanhToan() {
        List<HoaDon> ketQua = new ArrayList<>();
        for (HoaDon hd : dsHoaDon) {
            if (hd.daThanhToan()) {
                ketQua.add(hd);
            }
        }
        return ketQua;
    }

    /** Tìm hóa đơn theo mã hóa đơn hoặc số bàn, dùng cho ô tìm kiếm */
    public List<HoaDon> timTheoMaHoacBan(String tuKhoa) {
        List<HoaDon> ketQua = new ArrayList<>();
        String tuKhoaSach = FormatUtil.boDau(tuKhoa);

        for (HoaDon hd : findDaThanhToan()) {
            if (tuKhoaSach.isEmpty()
                    || FormatUtil.boDau(hd.getMaHD()).contains(tuKhoaSach)
                    || FormatUtil.boDau(hd.getMaBan()).contains(tuKhoaSach)) {
                ketQua.add(hd);
            }
        }
        return ketQua;
    }

    /**
     * Thêm mới hoặc cập nhật hóa đơn.
     * Ghi kèm toàn bộ chi tiết hóa đơn.
     */
    public void save(HoaDon hd) {
        for (int i = 0; i < dsHoaDon.size(); i++) {
            if (dsHoaDon.get(i).getMaHD().equalsIgnoreCase(hd.getMaHD())) {
                dsHoaDon.set(i, hd);
                ghiFile();
                return;
            }
        }
        dsHoaDon.add(hd);
        ghiFile();
    }

    /** Sinh mã hóa đơn mới theo ngày, ví dụ: HD20261023001 */
    public String sinhMaHoaDonMoi() {
        String ngay = java.time.LocalDate.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
        String tienTo = "HD" + ngay;

        int lonNhat = 0;
        for (HoaDon hd : dsHoaDon) {
            if (hd.getMaHD().startsWith(tienTo)) {
                try {
                    int so = Integer.parseInt(hd.getMaHD().substring(tienTo.length()));
                    if (so > lonNhat) {
                        lonNhat = so;
                    }
                } catch (NumberFormatException e) {
                    // Bỏ qua mã không đúng định dạng
                }
            }
        }
        return String.format("%s%03d", tienTo, lonNhat + 1);
    }

    /** Ghi toàn bộ hóa đơn và chi tiết ra file */
    private void ghiFile() {
        List<String[]> dsDongHD = new ArrayList<>();
        List<String[]> dsDongCT = new ArrayList<>();

        for (HoaDon hd : dsHoaDon) {
            dsDongHD.add(new String[]{
                    hd.getMaHD(),
                    hd.getMaBan(),
                    FormatUtil.thanhChuoiCSV(hd.getNgayMo()),
                    FormatUtil.thanhChuoiCSV(hd.getNgayDong()),
                    hd.getTrangThai().name(),
                    String.valueOf(hd.getTongTien()),
                    String.valueOf(hd.getTienKhachDua()),
                    String.valueOf(hd.getTienThua()),
                    hd.getPhuongThucTT() == null ? "" : hd.getPhuongThucTT().name()
            });

            for (ChiTietHoaDon ct : hd.getDsChiTiet()) {
                dsDongCT.add(new String[]{
                        ct.getMaHD(),
                        ct.getMaMon(),
                        ct.getTenMon() == null ? "" : ct.getTenMon(),
                        String.valueOf(ct.getSoLuong()),
                        String.valueOf(ct.getDonGia())
                });
            }
        }

        FileStore.ghiTatCaDong(fileHD, TIEU_DE_HD, dsDongHD);
        FileStore.ghiTatCaDong(fileCT, TIEU_DE_CT, dsDongCT);
    }

    /**
     * Tạo dữ liệu hóa đơn mẫu đã thanh toán, dùng khi chưa có dữ liệu.
     * Giúp màn hình lịch sử và thống kê có số liệu ngay khi chạy.
     */
    public void taoDuLieuMau() {
        dsHoaDon = new ArrayList<>();

        String[][] duLieu = {
                // mã hóa đơn, bàn, giờ mở, số món, hình thức
                {"HD20261022001", "B01", "2026-10-22T08:15:00", "TIEN_MAT"},
                {"HD20261022002", "B03", "2026-10-22T09:20:00", "CHUYEN_KHOAN"},
                {"HD20261022003", "B05", "2026-10-22T10:05:00", "TIEN_MAT"},
                {"HD20261023001", "B02", "2026-10-23T08:30:00", "TIEN_MAT"},
                {"HD20261023002", "B07", "2026-10-23T09:45:00", "CHUYEN_KHOAN"},
                {"HD20261023003", "B04", "2026-10-23T11:10:00", "TIEN_MAT"},
                {"HD20261023004", "B09", "2026-10-23T13:25:00", "TIEN_MAT"},
                {"HD20261023005", "B06", "2026-10-23T14:40:00", "CHUYEN_KHOAN"}
        };

        // Danh sách món dạng phẳng, mỗi hóa đơn dùng 2 dòng liên tiếp.
        // Định dạng: mã món, tên món, số lượng, đơn giá
        String[][] dsMon = {
                {"MON001", "Phin đen đá", "2", "25000"},
                {"MON019", "Bánh mì chà bông", "2", "15000"},

                {"MON013", "Trà đá", "1", "25000"},
                {"MON007", "Nước suối", "2", "10000"},

                {"MON003", "Cappuccino", "2", "45000"},
                {"MON022", "Tiramisu", "1", "55000"},

                {"MON001", "Phin đen đá", "1", "25000"},
                {"MON020", "Croissant bơ", "1", "30000"},

                {"MON016", "Trà sữa Trân châu", "2", "45000"},
                {"MON010", "Nước cam tươi", "1", "30000"},

                {"MON005", "Americano", "1", "38000"},
                {"MON019", "Bánh mì chà bông", "1", "15000"},

                {"MON006", "Mocha", "2", "48000"},
                {"MON021", "Bánh mì sữa", "1", "25000"},

                {"MON018", "Trà bạc xỉu", "1", "45000"},
                {"MON023", "Cheese cake", "1", "45000"}
        };

        for (int i = 0; i < duLieu.length; i++) {
            HoaDon hd = new HoaDon();
            hd.setMaHD(duLieu[i][0]);
            hd.setMaBan(duLieu[i][1]);
            hd.setNgayMo(LocalDateTime.parse(duLieu[i][2],
                    java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")));
            hd.setNgayDong(hd.getNgayMo().plusMinutes(35));
            hd.setTrangThai(TrangThaiHD.DA_THANH_TOAN);
            hd.setPhuongThucTT(PhuongThucTT.fromChuoi(duLieu[i][3]));

            List<ChiTietHoaDon> dsCT = new ArrayList<>();
            long tong = 0;

            // Mỗi hóa đơn lấy 2 dòng món tương ứng trong dsMon
            for (int j = 0; j < 2; j++) {
                String[] mon = dsMon[i * 2 + j];
                int sl = Integer.parseInt(mon[2]);
                long gia = Long.parseLong(mon[3]);
                dsCT.add(new ChiTietHoaDon(hd.getMaHD(), mon[0], mon[1], sl, gia));
                tong += sl * gia;
            }

            hd.setDsChiTiet(dsCT);
            hd.setTongTien(tong);

            if (hd.getPhuongThucTT() == PhuongThucTT.TIEN_MAT) {
                // Làm tròn lên hàng nghìn cho giống thật
                long tienDua = ((tong + 999) / 1000) * 1000;
                hd.setTienKhachDua(tienDua);
                hd.setTienThua(tienDua - tong);
            }

            dsHoaDon.add(hd);
        }

        ghiFile();
    }

    /** Nếu chưa có dữ liệu hóa đơn thì tạo dữ liệu mẫu */
    public void taoDuLieuMauNeuRong() {
        if (dsHoaDon.isEmpty()) {
            taoDuLieuMau();
        }
    }
}