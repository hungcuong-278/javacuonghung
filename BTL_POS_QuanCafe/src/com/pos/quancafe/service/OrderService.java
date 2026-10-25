package com.pos.quancafe.service;

import com.pos.quancafe.data.BanRepo;
import com.pos.quancafe.data.HoaDonRepo;
import com.pos.quancafe.data.MonRepo;
import com.pos.quancafe.model.Ban;
import com.pos.quancafe.model.ChiTietHoaDon;
import com.pos.quancafe.model.HoaDon;
import com.pos.quancafe.model.Mon;
import com.pos.quancafe.model.PhuongThucTT;
import com.pos.quancafe.model.TrangThaiBan;
import com.pos.quancafe.model.TrangThaiHD;
import com.pos.quancafe.util.HoaDonPrinter;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Nghiệp vụ đặt món và thanh toán của quán.
 * <p>
 * Lớp này là "trung tâm" của phần POS: nối bàn với hóa đơn, thêm món vào giỏ,
 * chuyển trạng thái và chốt thanh toán. Mọi thay đổi đều được ghi xuống file
 * CSV ngay trong lúc xử lý, nên đóng ứng dụng bất cứ lúc nào cũng không mất dữ liệu.
 */
public class OrderService {

    private final BanRepo banRepo;
    private final HoaDonRepo hoaDonRepo;
    private final MonRepo monRepo;

    public OrderService(BanRepo banRepo, HoaDonRepo hoaDonRepo, MonRepo monRepo) {
        this.banRepo = banRepo;
        this.hoaDonRepo = hoaDonRepo;
        this.monRepo = monRepo;
    }

    /**
     * Sinh mã hóa đơn mới theo ngày, ví dụ: HD20261025001.
     * <p>
     * Mã gồm 3 phần: "HD" + ngày yyyyMMdd + số thứ tự 3 chữ số.
     * Số thứ tự lấy bằng cách tìm mã lớn nhất đã có trong ngày đó.
     *
     * @return mã hóa đơn mới, chưa tồn tại trong hệ thống
     */
    public String taoMaHoaDon() {
        return hoaDonRepo.sinhMaHoaDonMoi();
    }

    /**
     * Mở bàn: tạo hóa đơn mới cho một bàn đang trống.
     * <p>
     * Trình tự 6 bước:
     * <ol>
     *     <li>Kiểm tra bàn có tồn tại không</li>
     *     <li>Chỉ mở được khi bàn đang trống, tránh đè lên hóa đơn đang phục vụ</li>
     *     <li>Sinh mã hóa đơn mới</li>
     *     <li>Khởi tạo hóa đơn ở trạng thái đang phục vụ</li>
     *     <li>Chuyển bàn sang có khách và gắn mã hóa đơn</li>
     *     <li>Lưu đồng bộ cả hóa đơn lẫn bàn xuống file</li>
     * </ol>
     *
     * @param maBan mã bàn cần mở
     * @return bàn sau khi mở, hoặc null nếu bàn không tồn tại
     */
    public Ban moBan(String maBan) {
        // 1. Kiểm tra bàn có tồn tại trong hệ thống hay không
        Ban ban = banRepo.findByMa(maBan);
        if (ban == null) {
            return null;
        }

        // 2. Chỉ cho phép mở nếu bàn đang ở trạng thái TRONG
        if (ban.getTrangThai() != TrangThaiBan.TRONG) {
            return ban; // Tránh tạo đè hóa đơn mới nếu bàn đang có khách
        }

        // 3. Sinh mã hóa đơn mới theo định dạng quy ước (ví dụ: HD20261025001)
        String maHD = taoMaHoaDon();

        // 4. Khởi tạo đối tượng hóa đơn mới
        HoaDon hoaDon = new HoaDon();
        hoaDon.setMaHD(maHD);
        hoaDon.setMaBan(maBan);
        hoaDon.setNgayMo(LocalDateTime.now());
        hoaDon.setTrangThai(TrangThaiHD.DANG_PHUC_VU);
        hoaDon.setDsChiTiet(new ArrayList<>());
        hoaDon.setTongTien(0);
        hoaDon.setTienKhachDua(0);
        hoaDon.setTienThua(0);
        hoaDon.setPhuongThucTT(null);
        hoaDon.setNgayDong(null);

        // 5. Đổi trạng thái bàn sang có khách và gán mã hóa đơn vừa tạo
        ban.moBan(maHD);

        // 6. Lưu đồng bộ cả hai đối tượng xuống file CSV
        hoaDonRepo.save(hoaDon);
        banRepo.save(ban);

        return ban;
    }

    /**
     * Thêm món vào giỏ của hóa đơn.
     * <p>
     * Nếu món đã có trong giỏ thì cộng thêm số lượng. Nếu số lượng mới
     * bằng 0 hoặc âm thì dòng đó bị xóa khỏi hóa đơn. Đơn giá được chốt
     * ngay tại lúc gọi món nên sửa giá món sau này không làm đổi hóa đơn cũ.
     *
     * @param maHD    mã hóa đơn
     * @param mon     món cần thêm
     * @param soLuong số lượng, âm nghĩa là giảm
     * @return hóa đơn sau khi cập nhật, null nếu tham số không hợp lệ
     */
    public HoaDon themMon(String maHD, Mon mon, int soLuong) {
        // 1. Kiểm tra tham số đầu vào
        if (maHD == null || mon == null) {
            return null;
        }

        HoaDon hd = hoaDonRepo.findByMa(maHD);
        if (hd == null) {
            return null;
        }

        // 2. Chỉ cho phép thêm/sửa món khi hóa đơn đang ở trạng thái DANG_PHUC_VU
        if (hd.getTrangThai() != TrangThaiHD.DANG_PHUC_VU) {
            return hd;
        }

        List<ChiTietHoaDon> ds = hd.getDsChiTiet();
        if (ds == null) {
            ds = new ArrayList<>();
            hd.setDsChiTiet(ds);
        }

        // 3. Tìm xem món đã tồn tại trong danh sách chi tiết hay chưa
        ChiTietHoaDon chiTietTonTai = null;
        for (ChiTietHoaDon ct : ds) {
            if (ct.getMaMon() != null && ct.getMaMon().equalsIgnoreCase(mon.getMaMon())) {
                chiTietTonTai = ct;
                break;
            }
        }

        // 4. Xử lý tăng/giảm hoặc thêm mới món
        if (chiTietTonTai != null) {
            int slMoi = chiTietTonTai.getSoLuong() + soLuong;
            if (slMoi <= 0) {
                // Nếu giảm số lượng về 0 hoặc âm thì xóa dòng khỏi hóa đơn
                ds.remove(chiTietTonTai);
            } else {
                chiTietTonTai.setSoLuong(slMoi);
            }
        } else if (soLuong > 0) {
            // Thêm mới dòng chi tiết, chốt đơn giá tại thời điểm gọi món
            ChiTietHoaDon ctMoi = new ChiTietHoaDon(
                    maHD,
                    mon.getMaMon(),
                    mon.getTenMon(),
                    soLuong,
                    mon.getDonGia()
            );
            ds.add(ctMoi);
        }

        // 5. Tính toán lại tổng tiền của hóa đơn
        long tongTien = 0;
        for (ChiTietHoaDon ct : ds) {
            tongTien += ct.thanhTien();
        }
        hd.setTongTien(tongTien);

        // 6. Lưu cập nhật hóa đơn và danh sách chi tiết xuống file CSV
        hoaDonRepo.save(hd);

        return hd;
    }

    /**
     * Chuyển hóa đơn sang trạng thái chờ thanh toán.
     * <p>
     * Hóa đơn rỗng thì không chuyển trạng thái, vì bàn không có món thì
     * chưa có gì để tính tiền. Trạng thái của bàn cũng được đổi theo cho khớp.
     *
     * @param maHD mã hóa đơn
     */
    public void yeuCauTinhTien(String maHD) {
        if (maHD == null) {
            return;
        }

        HoaDon hd = hoaDonRepo.findByMa(maHD);
        if (hd == null) {
            return;
        }
        if (hd.getTrangThai() != TrangThaiHD.DANG_PHUC_VU) {
            return;
        }
        if (hd.getDsChiTiet() == null || hd.getDsChiTiet().isEmpty()) {
            return;
        }

        hd.setTrangThai(TrangThaiHD.CHO_THANH_TOAN);
        hoaDonRepo.save(hd);

        Ban ban = banRepo.findByMa(hd.getMaBan());
        if (ban != null) {
            ban.setTrangThai(TrangThaiBan.CHO_THANH_TOAN);
            banRepo.save(ban);
        }
    }

    /**
     * Thanh toán hóa đơn.
     * <p>
     * Trình tự xử lý:
     * <ol>
     *     <li>Kiểm tra hóa đơn hợp lệ và chưa thanh toán trước đó</li>
     *     <li>Tính tiền khách đưa và tiền thừa theo hình thức thanh toán</li>
     *     <li>Chốt thời gian đóng và đổi trạng thái hóa đơn</li>
     *     <li>Trừ tồn kho cho từng món đã gọi</li>
     *     <li>Giải phóng bàn về trạng thái trống</li>
     *     <li>Lưu hóa đơn hoàn tất xuống file CSV</li>
     * </ol>
     * <p>
     * Việc xuất file hóa đơn ra thư mục riêng do lớp giao diện quyết định
     * (người dùng có lưu hay không), nên gọi thêm {@link #xuatHoaDon(String)}.
     *
     * @param maHD         mã hóa đơn
     * @param pt           hình thức thanh toán
     * @param tienKhachDua tiền khách đưa, bỏ qua khi chuyển khoản
     * @throws IllegalArgumentException khi tiền khách đưa không đủ tổng tiền
     */
    public void thanhToan(String maHD, PhuongThucTT pt, long tienKhachDua) {
        if (maHD == null || maHD.trim().isEmpty() || pt == null) {
            return;
        }

        HoaDon hd = hoaDonRepo.findByMa(maHD);
        if (hd == null) {
            return;
        }

        // Không xử lý nếu hóa đơn này đã được thanh toán trước đó
        if (hd.getTrangThai() == TrangThaiHD.DA_THANH_TOAN) {
            return;
        }

        long tongTien = hd.getTongTien();

        // 1. Kiểm tra và tính toán tiền đưa / tiền thừa
        if (pt == PhuongThucTT.TIEN_MAT) {
            if (tienKhachDua < tongTien) {
                throw new IllegalArgumentException("Số tiền khách đưa không đủ để thanh toán!");
            }
            hd.setTienKhachDua(tienKhachDua);
            hd.setTienThua(tienKhachDua - tongTien);
        } else {
            // Chuyển khoản: tiền khách đưa và tiền thừa mặc định là 0
            hd.setTienKhachDua(0);
            hd.setTienThua(0);
        }

        // 2. Chốt thời gian đóng và đổi trạng thái hóa đơn
        hd.setPhuongThucTT(pt);
        hd.setNgayDong(LocalDateTime.now());
        hd.setTrangThai(TrangThaiHD.DA_THANH_TOAN);

        // 3. Trừ số lượng tồn kho từng món
        if (hd.getDsChiTiet() != null) {
            for (ChiTietHoaDon ct : hd.getDsChiTiet()) {
                monRepo.truTonKho(ct.getMaMon(), ct.getSoLuong());
            }
        }

        // 4. Giải phóng bàn: đưa về trạng thái TRONG và xóa mã hóa đơn
        Ban ban = banRepo.findByMa(hd.getMaBan());
        if (ban != null) {
            ban.traBan();
            banRepo.save(ban);
        }

        // 5. Lưu hóa đơn hoàn tất xuống file CSV
        hoaDonRepo.save(hd);
    }

    /**
     * Sinh nội dung hóa đơn và ghi ra file txt trong thư mục hoa_don/.
     * <p>
     * Dùng {@link HoaDonPrinter#taoNoiDungTuChiTiet} để nội dung hiển thị
     * giống hệt bản in, kể cả khi xem lại hóa đơn cũ.
     *
     * @param maHD mã hóa đơn
     * @return file đã ghi thành công, null nếu không tìm thấy hóa đơn hoặc ghi lỗi
     */
    public File xuatHoaDon(String maHD) {
        return xuatHoaDon(maHD, null);
    }

    /**
     * Sinh nội dung hóa đơn và ghi ra file txt.
     *
     * @param maHD      mã hóa đơn
     * @param thuMucCha thư mục cha để lưu file, null nghĩa là dùng mặc định hoa_don/
     * @return file đã ghi thành công, null nếu không tìm thấy hóa đơn hoặc ghi lỗi
     */
    public File xuatHoaDon(String maHD, File thuMucCha) {
        if (maHD == null) {
            return null;
        }

        HoaDon hd = hoaDonRepo.findByMa(maHD);
        if (hd == null) {
            return null;
        }

        try {
            String noiDung = HoaDonPrinter.taoNoiDungTuChiTiet(
                    hd.getMaHD(),
                    hd.getMaBan(),
                    hd.getNgayMo(),
                    hd.getNgayDong(),
                    hd.getDsChiTiet(),
                    hd.getTongTien(),
                    hd.getPhuongThucTT() == null ? "" : hd.getPhuongThucTT().getTenHienThi(),
                    hd.getTienKhachDua(),
                    hd.getTienThua());

            File file;
            if (thuMucCha == null) {
                file = HoaDonPrinter.taoFileHoaDon(maHD);
            } else {
                file = new File(thuMucCha, maHD + ".txt");
            }

            if (HoaDonPrinter.xuatFileTxt(noiDung, file)) {
                return file;
            }
            return null;
        } catch (Exception e) {
            System.err.println("Loi khi ghi file hoa don: " + e.getMessage());
            return null;
        }
    }

    /**
     * Hủy bàn: huỷ hóa đơn đang mở và đưa bàn về trạng thái trống.
     * <p>
     * Chỉ hủy được khi hóa đơn còn ở trạng thái đang phục vụ. Hóa đơn đã
     * yêu cầu tính tiền thì phải thanh toán hoặc dùng chức năng hủy ở màn
     * hình gọi món, tránh mất tiền của khách.
     *
     * @param maBan mã bàn cần hủy
     * @return true nếu hủy thành công, false nếu không hủy được
     */
    public boolean huyBan(String maBan) {
        if (maBan == null || maBan.trim().isEmpty()) {
            return false;
        }

        Ban ban = banRepo.findByMa(maBan);
        if (ban == null) {
            return false;
        }
        if (ban.getTrangThai() == TrangThaiBan.TRONG) {
            return false;
        }

        // Lấy hóa đơn đang gắn với bàn
        HoaDon hd = ban.getMaHoaDon() == null
                ? null
                : hoaDonRepo.findByMa(ban.getMaHoaDon());

        if (hd != null) {
            // Hóa đơn đã yêu cầu tính tiền hoặc đã thanh toán thì không hủy ở đây
            if (hd.getTrangThai() != TrangThaiHD.DANG_PHUC_VU) {
                return false;
            }
            // Xóa hóa đơn đang mở cùng toàn bộ chi tiết của nó
            hoaDonRepo.delete(hd.getMaHD());
        }

        // Giải phóng bàn về trạng thái trống
        ban.traBan();
        banRepo.save(ban);

        return true;
    }

    /**
     * Lấy hóa đơn đang gắn với một bàn.
     *
     * @param maBan mã bàn
     * @return hóa đơn đang phục vụ, null nếu bàn trống
     */
    public HoaDon layHoaDonCuaBan(String maBan) {
        Ban ban = banRepo.findByMa(maBan);
        if (ban == null || ban.getMaHoaDon() == null) {
            return null;
        }
        return hoaDonRepo.findByMa(ban.getMaHoaDon());
    }
}
