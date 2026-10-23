package com.pos.quancafe.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Lớp model chứa thông tin một hóa đơn của quán.
 * Phần định nghĩa chung, cả hai người đều dùng.
 */
public class HoaDon {
    private String maHD;             // ví dụ: "HD20261023001"
    private String maBan;            // ví dụ: "B01"
    private LocalDateTime ngayMo;
    private LocalDateTime ngayDong;  // null khi chưa thanh toán
    private TrangThaiHD trangThai;
    private long tongTien;
    private long tienKhachDua;       // 0 khi chuyển khoản
    private long tienThua;           // 0 khi chuyển khoản
    private PhuongThucTT phuongThucTT;

    private List<ChiTietHoaDon> dsChiTiet = new ArrayList<>();

    public HoaDon() {
        this.trangThai = TrangThaiHD.DANG_PHUC_VU;
    }

    /** Hàm dựng đầy đủ */
    public HoaDon(String maHD, String maBan, LocalDateTime ngayMo, LocalDateTime ngayDong,
                  TrangThaiHD trangThai, long tongTien, long tienKhachDua, long tienThua,
                  PhuongThucTT phuongThucTT, List<ChiTietHoaDon> dsChiTiet) {
        this.maHD = maHD;
        this.maBan = maBan;
        this.ngayMo = ngayMo;
        this.ngayDong = ngayDong;
        this.trangThai = trangThai;
        this.tongTien = tongTien;
        this.tienKhachDua = tienKhachDua;
        this.tienThua = tienThua;
        this.phuongThucTT = phuongThucTT;
        this.dsChiTiet = dsChiTiet == null ? new ArrayList<>() : dsChiTiet;
    }

    /**
     * Tính tổng tiền từ danh sách chi tiết.
     * Tự cộng để không phải cập nhật thủ công mỗi khi thêm món.
     */
    public long tinhTongTien() {
        long tong = 0;
        for (ChiTietHoaDon ct : dsChiTiet) {
            tong += ct.thanhTien();
        }
        return tong;
    }

    /** Hóa đơn đã hoàn tất thanh toán hay chưa */
    public boolean daThanhToan() {
        return trangThai == TrangThaiHD.DA_THANH_TOAN;
    }

    /** Hóa đơn đang mở, chưa thanh toán */
    public boolean dangMo() {
        return trangThai != TrangThaiHD.DA_THANH_TOAN;
    }

    /** Số lượng món đã gọi */
    public int tongSoMon() {
        return dsChiTiet.size();
    }

    public String getMaHD() {
        return maHD;
    }

    public void setMaHD(String maHD) {
        this.maHD = maHD;
    }

    public String getMaBan() {
        return maBan;
    }

    public void setMaBan(String maBan) {
        this.maBan = maBan;
    }

    public LocalDateTime getNgayMo() {
        return ngayMo;
    }

    public void setNgayMo(LocalDateTime ngayMo) {
        this.ngayMo = ngayMo;
    }

    public LocalDateTime getNgayDong() {
        return ngayDong;
    }

    public void setNgayDong(LocalDateTime ngayDong) {
        this.ngayDong = ngayDong;
    }

    public TrangThaiHD getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(TrangThaiHD trangThai) {
        this.trangThai = trangThai;
    }

    public long getTongTien() {
        return tongTien;
    }

    public void setTongTien(long tongTien) {
        this.tongTien = tongTien;
    }

    public long getTienKhachDua() {
        return tienKhachDua;
    }

    public void setTienKhachDua(long tienKhachDua) {
        this.tienKhachDua = tienKhachDua;
    }

    public long getTienThua() {
        return tienThua;
    }

    public void setTienThua(long tienThua) {
        this.tienThua = tienThua;
    }

    public PhuongThucTT getPhuongThucTT() {
        return phuongThucTT;
    }

    public void setPhuongThucTT(PhuongThucTT phuongThucTT) {
        this.phuongThucTT = phuongThucTT;
    }

    public List<ChiTietHoaDon> getDsChiTiet() {
        return dsChiTiet;
    }

    public void setDsChiTiet(List<ChiTietHoaDon> dsChiTiet) {
        this.dsChiTiet = dsChiTiet == null ? new ArrayList<>() : dsChiTiet;
    }
}