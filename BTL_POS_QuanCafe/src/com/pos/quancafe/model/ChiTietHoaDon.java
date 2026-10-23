package com.pos.quancafe.model;

/**
 * Chi tiết một dòng trong hóa đơn: món nào, gọi bao nhiêu, giá bao nhiêu.
 * Phần định nghĩa chung, cả hai người đều dùng.
 */
public class ChiTietHoaDon {
    private String maHD;
    private String maMon;
    private String tenMon;      // lưu cả tên để hóa đơn cũ không bị ảnh hưởng khi đổi tên món
    private int soLuong;
    private long donGia;        // chốt giá lúc gọi món, không lấy lại từ thực đơn

    public ChiTietHoaDon() {
    }

    public ChiTietHoaDon(String maHD, String maMon, String tenMon, int soLuong, long donGia) {
        this.maHD = maHD;
        this.maMon = maMon;
        this.tenMon = tenMon;
        this.soLuong = soLuong;
        this.donGia = donGia;
    }

    /** Thành tiền của dòng này */
    public long thanhTien() {
        return soLuong * donGia;
    }

    /** Tạo chi tiết từ một món trong thực đỡn */
    public static ChiTietHoaDon tuMon(String maHD, Mon mon, int soLuong) {
        return new ChiTietHoaDon(maHD, mon.getMaMon(), mon.getTenMon(), soLuong, mon.getDonGia());
    }

    public String getMaHD() {
        return maHD;
    }

    public void setMaHD(String maHD) {
        this.maHD = maHD;
    }

    public String getMaMon() {
        return maMon;
    }

    public void setMaMon(String maMon) {
        this.maMon = maMon;
    }

    public String getTenMon() {
        return tenMon;
    }

    public void setTenMon(String tenMon) {
        this.tenMon = tenMon;
    }

    public int getSoLuong() {
        return soLuong;
    }

    public void setSoLuong(int soLuong) {
        this.soLuong = soLuong;
    }

    public long getDonGia() {
        return donGia;
    }

    public void setDonGia(long donGia) {
        this.donGia = donGia;
    }

    @Override
    public String toString() {
        return tenMon + " x" + soLuong;
    }
}