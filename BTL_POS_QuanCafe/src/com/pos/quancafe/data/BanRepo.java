package com.pos.quancafe.data;

import com.pos.quancafe.model.Ban;
import com.pos.quancafe.model.TrangThaiBan;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Quản lý dữ liệu bàn: đọc, tìm kiếm và lưu trạng thái bàn trong file ban.csv.
 */
public class BanRepo {

    private final File fileBan;

    private static final String[] TIEU_DE = {"maBan", "viTri", "trangThai", "maHoaDon"};

    private List<Ban> dsBan = new ArrayList<>();

    public BanRepo() {
        this(new File("data/ban.csv"));
    }

    /**
     * Nạp dữ liệu bàn từ một file cụ thể.
     * <p>
     * Cách này cho phép chương trình kiểm thử dùng một file riêng,
     * không đụng tới dữ liệu thật của quán.
     *
     * @param fileBan file CSV chứa danh sách bàn
     */
    public BanRepo(File fileBan) {
        this.fileBan = fileBan;
        napDuLieu();
    }

    /**
     * Đọc toàn bộ bàn từ file CSV.
     * Nếu file chưa có hoặc rỗng thì tự tạo dữ liệu mẫu 9 bàn dạng lưới 3x3.
     */
    public void napDuLieu() {
        dsBan = new ArrayList<>();
        List<String[]> dsDong = FileStore.docTatCaDong(fileBan);

        for (String[] dong : dsDong) {
            String maHoaDon = FileStore.layPhanTu(dong, 3);
            Ban ban = new Ban(
                    FileStore.layPhanTu(dong, 0),
                    FileStore.layPhanTu(dong, 1),
                    TrangThaiBan.fromChuoi(FileStore.layPhanTu(dong, 2)),
                    (maHoaDon == null || maHoaDon.trim().isEmpty()) ? null : maHoaDon.trim()
            );

            // Bỏ qua dòng hỏng không có mã bàn
            if (ban.getMaBan() != null && !ban.getMaBan().trim().isEmpty()) {
                dsBan.add(ban);
            }
        }

        if (dsBan.isEmpty()) {
            taoDuLieuMau();
        }
    }

    /** Tạo dữ liệu mẫu: 9 bàn xếp lưới 3x3, tất cả đang trống */
    private void taoDuLieuMau() {
        dsBan = new ArrayList<>();
        int stt = 0;
        for (int i = 1; i <= 3; i++) {
            for (int j = 1; j <= 3; j++) {
                stt++;
                themBan("B0" + stt, i + "-" + j, TrangThaiBan.TRONG, null);
            }
        }
        ghiFile();
    }

    /** Thêm bàn vào danh sách khi tạo dữ liệu mẫu */
    private void themBan(String maBan, String viTri, TrangThaiBan trangThai, String maHoaDon) {
        dsBan.add(new Ban(maBan, viTri, trangThai, maHoaDon));
    }

    /** Ghi toàn bộ danh sách bàn xuống file CSV */
    private void ghiFile() {
        List<String[]> dsDong = new ArrayList<>();

        for (Ban ban : dsBan) {
            dsDong.add(new String[]{
                    ban.getMaBan(),
                    ban.getViTri(),
                    ban.getTrangThai().name(),
                    (ban.getMaHoaDon() == null) ? "" : ban.getMaHoaDon()
            });
        }

        FileStore.ghiTatCaDong(fileBan, TIEU_DE, dsDong);
    }

    /** Lấy toàn bộ danh sách bàn */
    public List<Ban> findAll() {
        return new ArrayList<>(dsBan);
    }

    /** Tìm bàn theo mã, không phân biệt hoa thường */
    public Ban findByMa(String maBan) {
        if (maBan == null) {
            return null;
        }
        for (Ban ban : dsBan) {
            if (ban.getMaBan().equalsIgnoreCase(maBan.trim())) {
                return ban;
            }
        }
        return null;
    }

    /**
     * Thêm mới hoặc cập nhật một bàn.
     * Nếu mã đã tồn tại thì cập nhật, ngược lại thêm mới.
     */
    public void save(Ban ban) {
        if (ban == null || ban.getMaBan() == null) {
            return;
        }
        for (int i = 0; i < dsBan.size(); i++) {
            if (dsBan.get(i).getMaBan().equalsIgnoreCase(ban.getMaBan())) {
                dsBan.set(i, ban);
                ghiFile();
                return;
            }
        }
        dsBan.add(ban);
        ghiFile();
    }
}
