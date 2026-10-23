package com.pos.quancafe.service;

import com.pos.quancafe.data.HoaDonRepo;
import com.pos.quancafe.model.HoaDon;

import java.time.LocalDate;
import java.util.List;

/**
 * Tính toán số liệu kinh doanh: doanh thu và số hóa đơn theo ngày.
 * Phần này thuộc công việc của Người 2.
 */
public class ThongKeService {

    private final HoaDonRepo hoaDonRepo;

    public ThongKeService(HoaDonRepo hoaDonRepo) {
        this.hoaDonRepo = hoaDonRepo;
    }

    /**
     * Tính tổng doanh thu trong một ngày.
     * Chỉ cộng những hóa đơn đã thanh toán và có thời điểm đóng trong ngày đó.
     *
     * @param ngay ngày cần tính
     * @return tổng tiền, trả về 0 nếu không có hóa đơn nào
     */
    public long doanhThuTrongNgay(LocalDate ngay) {
        if (ngay == null) {
            return 0;
        }

        long tong = 0;
        for (HoaDon hd : hoaDonRepo.findDaThanhToan()) {
            if (hd.getNgayDong() != null && hd.getNgayDong().toLocalDate().equals(ngay)) {
                tong += hd.getTongTien();
            }
        }
        return tong;
    }

    /**
     * Đếm số hóa đơn đã thanh toán trong một ngày.
     *
     * @param ngay ngày cần đếm
     * @return số hóa đơn
     */
    public int soHoaDonTrongNgay(LocalDate ngay) {
        if (ngay == null) {
            return 0;
        }

        int dem = 0;
        for (HoaDon hd : hoaDonRepo.findDaThanhToan()) {
            if (hd.getNgayDong() != null && hd.getNgayDong().toLocalDate().equals(ngay)) {
                dem++;
            }
        }
        return dem;
    }

    /**
     * Doanh thu của hôm nay, dùng cho thẻ thống kê trên màn hình lịch sử.
     */
    public long doanhThuHomNay() {
        return doanhThuTrongNgay(LocalDate.now());
    }

    /** Số hóa đơn của hôm nay */
    public int soHoaDonHomNay() {
        return soHoaDonTrongNgay(LocalDate.now());
    }

    /**
     * Doanh thu của một khoảng thời gian, dùng cho báo cáo theo tuần hoặc theo tháng.
     *
     * @param tuNgay ngày bắt đầu, có thể null
     * @param denNgay ngày kết thúc, có thể null
     * @return tổng tiền trong khoảng
     */
    public long doanhThuTrongKhoang(LocalDate tuNgay, LocalDate denNgay) {
        long tong = 0;
        for (HoaDon hd : hoaDonRepo.findDaThanhToan()) {
            if (hd.getNgayDong() == null) {
                continue;
            }
            LocalDate ngayHD = hd.getNgayDong().toLocalDate();
            boolean sauTuNgay = tuNgay == null || !ngayHD.isBefore(tuNgay);
            boolean truocDenNgay = denNgay == null || !ngayHD.isAfter(denNgay);
            if (sauTuNgay && truocDenNgay) {
                tong += hd.getTongTien();
            }
        }
        return tong;
    }

    /**
     * Bình quân mỗi hóa đơn trong ngày, dùng để biết khách hay chi bao nhiêu.
     *
     * @param ngay ngày cần tính
     * @return số tiền trung bình, trả về 0 nếu không có hóa đơn
     */
    public long tienTrungBinhMoiHoaDon(LocalDate ngay) {
        int soHD = soHoaDonTrongNgay(ngay);
        if (soHD == 0) {
            return 0;
        }
        return doanhThuTrongNgay(ngay) / soHD;
    }

    /**
     * Tìm bàn nào bán chạy nhất trong một ngày, dựa trên số hóa đơn.
     *
     * @param ngay ngày cần thống kê
     * @return mã bàn bán chạy nhất, hoặc chuỗi rỗng nếu không có dữ liệu
     */
    public String banBanChayNhat(LocalDate ngay) {
        String banTotNhat = "";
        int nhieuNhat = 0;

        for (HoaDon hd : hoaDonRepo.findDaThanhToan()) {
            if (hd.getNgayDong() == null || !hd.getNgayDong().toLocalDate().equals(ngay)) {
                continue;
            }
            int dem = soHoaDonCuaBan(hd.getMaBan(), ngay);
            if (dem > nhieuNhat) {
                nhieuNhat = dem;
                banTotNhat = hd.getMaBan();
            }
        }

        return banTotNhat;
    }

    /** Đếm số hóa đơn của một bàn trong ngày */
    private int soHoaDonCuaBan(String maBan, LocalDate ngay) {
        int dem = 0;
        for (HoaDon hd : hoaDonRepo.findDaThanhToan()) {
            if (hd.getNgayDong() != null
                    && hd.getNgayDong().toLocalDate().equals(ngay)
                    && hd.getMaBan().equalsIgnoreCase(maBan)) {
                dem++;
            }
        }
        return dem;
    }

    /**
     * Lấy danh sách hóa đơn đã thanh toán trong ngày, sắp xếp mới nhất lên đầu.
     *
     * @param ngay ngày cần lấy
     * @return danh sách hóa đơn
     */
    public List<HoaDon> hoaDonTrongNgay(LocalDate ngay) {
        List<HoaDon> ketQua = new java.util.ArrayList<>();

        for (HoaDon hd : hoaDonRepo.findDaThanhToan()) {
            if (hd.getNgayDong() != null && hd.getNgayDong().toLocalDate().equals(ngay)) {
                ketQua.add(hd);
            }
        }

        // Mới nhất lên đầu
        ketQua.sort((a, b) -> {
            if (a.getNgayDong() == null || b.getNgayDong() == null) {
                return 0;
            }
            return b.getNgayDong().compareTo(a.getNgayDong());
        });

        return ketQua;
    }
}