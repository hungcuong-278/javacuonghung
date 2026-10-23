package com.pos.quancafe.model;

/**
 * Lớp model chứa thông tin một món trong thực đơn.
 * Phần này thuộc công việc của Người 2.
 */
public class Mon {
    private String maMon;          // ví dụ: "MON001"
    private String tenMon;         // ví dụ: "Phin đen đá"
    private NhomMon nhomMon;       // thuộc nhóm nào
    private long donGia;           // đơn giá (VNĐ)
    private int tonKho;            // số lượng còn trong quán
    private String moTa;           // mô tả thêm, có thể rỗng

    /** Hàm dựng rỗng, cần cho Swing khi hiển thị danh sách */
    public Mon() {
    }

    /** Hàm dựng đầy đủ thông tin */
    public Mon(String maMon, String tenMon, NhomMon nhomMon, long donGia, int tonKho, String moTa) {
        this.maMon = maMon;
        this.tenMon = tenMon;
        this.nhomMon = nhomMon;
        this.donGia = donGia;
        this.tonKho = tonKho;
        this.moTa = moTa;
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

    public NhomMon getNhomMon() {
        return nhomMon;
    }

    public void setNhomMon(NhomMon nhomMon) {
        this.nhomMon = nhomMon;
    }

    public long getDonGia() {
        return donGia;
    }

    public void setDonGia(long donGia) {
        this.donGia = donGia;
    }

    public int getTonKho() {
        return tonKho;
    }

    public void setTonKho(int tonKho) {
        this.tonKho = tonKho;
    }

    public String getMoTa() {
        return moTa;
    }

    public void setMoTa(String moTa) {
        this.moTa = moTa;
    }

    /** Hết hàng khi tồn kho bằng 0 hoặc âm */
    public boolean hetHang() {
        return tonKho <= 0;
    }

    /** Còn món để bán */
    public boolean conMon() {
        return tonKho > 0;
    }

    /** Hiển thị trên danh sách, ví dụ: "Phin đen đá - 25.000 đ" */
    @Override
    public String toString() {
        return tenMon + " - " + com.pos.quancafe.util.FormatUtil.formatTien(donGia);
    }
}