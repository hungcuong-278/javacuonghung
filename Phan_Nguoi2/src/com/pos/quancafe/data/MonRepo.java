package com.pos.quancafe.data;

import com.pos.quancafe.model.Mon;
import com.pos.quancafe.model.NhomMon;
import com.pos.quancafe.util.FormatUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Quản lý dữ liệu món: đọc, tìm kiếm, thêm, sửa, xóa trong file mon.csv.
 * Phần này thuộc công việc của Người 2.
 */
public class MonRepo {

    private final File file = new File("data/mon.csv");

    private static final String[] TIEU_DE = {
            "maMon", "tenMon", "nhomMon", "donGia", "tonKho", "moTa"
    };

    private List<Mon> dsMon = new ArrayList<>();

    /**
     * Nạp toàn bộ dữ liệu món từ file.
     * Nếu file chưa tồn tại thì tự tạo file kèm dữ liệu mẫu.
     */
    public MonRepo() {
        napDuLieu();
    }

    /**
     * Đọc danh sách món từ file vào bộ nhớ.
     */
    public void napDuLieu() {
        dsMon = new ArrayList<>();
        List<String[]> dsDong = FileStore.docTatCaDong(file);

        for (String[] dong : dsDong) {
            Mon mon = new Mon(
                    FileStore.layPhanTu(dong, 0),
                    FileStore.layPhanTu(dong, 1),
                    NhomMon.fromChuoi(FileStore.layPhanTu(dong, 2)),
                    FormatUtil.docTien(FileStore.layPhanTu(dong, 3)),
                    FormatUtil.docSoNguyen(FileStore.layPhanTu(dong, 4)),
                    FileStore.layPhanTu(dong, 5)
            );

            // Bỏ qua dòng hỏng không có mã món
            if (mon.getMaMon() != null && !mon.getMaMon().trim().isEmpty()) {
                dsMon.add(mon);
            }
        }

        // Chưa có file hoặc file rỗng thì tạo dữ liệu mẫu
        if (dsMon.isEmpty()) {
            taoDuLieuMau();
        }
    }

    /** Lấy toàn bộ danh sách món */
    public List<Mon> findAll() {
        return new ArrayList<>(dsMon);
    }

    /** Tìm món theo mã */
    public Mon findByMa(String maMon) {
        for (Mon mon : dsMon) {
            if (mon.getMaMon().equalsIgnoreCase(maMon)) {
                return mon;
            }
        }
        return null;
    }

    /** Tìm món theo nhóm, dùng cho các tab trong màn hình gọi món */
    public List<Mon> findByNhom(NhomMon nhom) {
        List<Mon> ketQua = new ArrayList<>();
        for (Mon mon : dsMon) {
            if (mon.getNhomMon() == nhom) {
                ketQua.add(mon);
            }
        }
        return ketQua;
    }

    /**
     * Tìm món theo tên, không phân biệt hoa thường và dấu tiếng Việt.
     * Chuỗi rỗng nghĩa là lấy tất cả.
     */
    public List<Mon> timTheoTen(String tuKhoa) {
        List<Mon> ketQua = new ArrayList<>();
        String tuKhoaSach = FormatUtil.boDau(tuKhoa);

        for (Mon mon : dsMon) {
            if (tuKhoaSach.isEmpty()
                    || FormatUtil.boDau(mon.getTenMon()).contains(tuKhoaSach)
                    || FormatUtil.boDau(mon.getMaMon()).contains(tuKhoaSach)) {
                ketQua.add(mon);
            }
        }
        return ketQua;
    }

    /** Tìm món theo tên chính xác, dùng khi kiểm tra trùng tên */
    public boolean tonTaiTen(String tenMon) {
        String tenSach = FormatUtil.boDau(tenMon);
        for (Mon mon : dsMon) {
            if (FormatUtil.boDau(mon.getTenMon()).equals(tenSach)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Thêm mới hoặc cập nhật một món.
     * Nếu mã đã tồn tại thì cập nhật, ngược lại thêm mới.
     */
    public void save(Mon mon) {
        for (int i = 0; i < dsMon.size(); i++) {
            if (dsMon.get(i).getMaMon().equalsIgnoreCase(mon.getMaMon())) {
                dsMon.set(i, mon);
                ghiFile();
                return;
            }
        }
        dsMon.add(mon);
        ghiFile();
    }

    /** Xóa món theo mã */
    public boolean delete(String maMon) {
        for (int i = 0; i < dsMon.size(); i++) {
            if (dsMon.get(i).getMaMon().equalsIgnoreCase(maMon)) {
                dsMon.remove(i);
                ghiFile();
                return true;
            }
        }
        return false;
    }

    /**
     * Trừ tồn kho sau khi bán món.
     * Không cho tồn kho âm.
     */
    public void truTonKho(String maMon, int soLuong) {
        Mon mon = findByMa(maMon);
        if (mon != null) {
            mon.setTonKho(Math.max(0, mon.getTonKho() - soLuong));
            ghiFile();
        }
    }

    /**
     * Sinh mã món mới, ví dụ: MON001, MON002, ...
     */
    public String sinhMaMonMoi() {
        int lonNhat = 0;
        for (Mon mon : dsMon) {
            String ma = mon.getMaMon();
            if (ma != null && ma.toUpperCase().startsWith("MON")) {
                try {
                    int so = Integer.parseInt(ma.substring(3).trim());
                    if (so > lonNhat) {
                        lonNhat = so;
                    }
                } catch (NumberFormatException e) {
                    // Bỏ qua mã không đúng định dạng
                }
            }
        }
        return String.format("MON%03d", lonNhat + 1);
    }

    /** Ghi toàn bộ danh sách món xuống file */
    private void ghiFile() {
        List<String[]> dsDong = new ArrayList<>();
        for (Mon mon : dsMon) {
            dsDong.add(new String[]{
                    mon.getMaMon(),
                    mon.getTenMon(),
                    mon.getNhomMon().name(),
                    String.valueOf(mon.getDonGia()),
                    String.valueOf(mon.getTonKho()),
                    mon.getMoTa() == null ? "" : mon.getMoTa()
            });
        }
        FileStore.ghiTatCaDong(file, TIEU_DE, dsDong);
    }

    /** Tạo sẵn 24 món mẫu đủ 4 nhóm khi chưa có dữ liệu */
    private void taoDuLieuMau() {
        dsMon = new ArrayList<>();

        // Nhóm Cà phê
        themMon("MON001", "Phin đen đá", NhomMon.CA_PHE, 25000, 100, "Pha phin truyền thống với đá");
        themMon("MON002", "Phin đen nóng", NhomMon.CA_PHE, 25000, 100, "Phin đen không đá");
        themMon("MON003", "Cappuccino", NhomMon.CA_PHE, 45000, 80, "Sữa tươi và bọt mềm");
        themMon("MON004", "Latte", NhomMon.CA_PHE, 42000, 80, "Sữa tươi mịn");
        themMon("MON005", "Americano", NhomMon.CA_PHE, 38000, 75, "Espresso pha thêm nước");
        themMon("MON006", "Mocha", NhomMon.CA_PHE, 48000, 60, "Kết hợp cà phê và sôcôla");

        // Nhóm Nước uống
        themMon("MON007", "Nước suối", NhomMon.NUOC_UONG, 10000, 200, "Chai nước suối đóng bình");
        themMon("MON008", "Coca Cola", NhomMon.NUOC_UONG, 15000, 150, "Lon 330ml");
        themMon("MON009", "Pepsi", NhomMon.NUOC_UONG, 15000, 150, "Lon 330ml");
        themMon("MON010", "Nước cam tươi", NhomMon.NUOC_UONG, 30000, 40, "Cam tươi ép tại quán");
        themMon("MON011", "Nước dừa tươi", NhomMon.NUOC_UONG, 35000, 40, "Nước dừa tươi có đá");
        themMon("MON012", "Soda chanh muối", NhomMon.NUOC_UONG, 28000, 50, "Soda và chanh muối");

        // Nhóm Trà
        themMon("MON013", "Trà đá", NhomMon.TRA, 25000, 90, "Trà bột pha sẵn với đá");
        themMon("MON014", "Trà nóng", NhomMon.TRA, 25000, 90, "Trà bột pha sẵn nóng");
        themMon("MON015", "Trà tắc", NhomMon.TRA, 32000, 70, "Trà kết hợp chanh tắc");
        themMon("MON016", "Trà sữa Trân châu", NhomMon.TRA, 45000, 60, "Trà sữa trân châu đen");
        themMon("MON017", "Trà sữa Thái", NhomMon.TRA, 40000, 60, "Trà sữa vị dừa");
        themMon("MON018", "Trà bạc xỉu", NhomMon.TRA, 45000, 50, "Trà đen và sữa tươi");

        // Nhóm Bánh
        themMon("MON019", "Bánh mì chà bông", NhomMon.BANH, 15000, 120, "Ổ bánh mì kẹp chà bông");
        themMon("MON020", "Croissant bơ", NhomMon.BANH, 30000, 40, "Bánh sừng bơ Pháp");
        themMon("MON021", "Bánh mì sữa", NhomMon.BANH, 25000, 45, "Ổ bánh mì sữa nướng");
        themMon("MON022", "Tiramisu", NhomMon.BANH, 55000, 25, "Bánh kem mặt bùa Pháp");
        themMon("MON023", "Cheese cake", NhomMon.BANH, 45000, 25, "Bánh phô mai chanh dây");
        themMon("MON024", "Mousse chocolate", NhomMon.BANH, 48000, 20, "Mousse sôcôla đen");

        ghiFile();
    }

    /** Thêm món vào danh sách khi tạo dữ liệu mẫu */
    private void themMon(String ma, String ten, NhomMon nhom, long gia, int tonKho, String moTa) {
        dsMon.add(new Mon(ma, ten, nhom, gia, tonKho, moTa));
    }
}