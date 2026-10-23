package com.pos.quancafe.model;

/**
 * Trạng thái của một hóa đơn.
 * Một bàn đang phục vụ chính là một hóa đơn đang mở, chỉ khác ở trạng thái.
 */
public enum TrangThaiHD {
    DANG_PHUC_VU("Đang phục vụ"),
    CHO_THANH_TOAN("Chờ thanh toán"),
    DA_THANH_TOAN("Đã thanh toán");

    private final String tenHienThi;

    TrangThaiHD(String tenHienThi) {
        this.tenHienThi = tenHienThi;
    }

    public String getTenHienThi() {
        return tenHienThi;
    }

    @Override
    public String toString() {
        return tenHienThi;
    }

    /** Đổi chuỗi trong file CSV thành enum */
    public static TrangThaiHD fromChuoi(String chuoi) {
        if (chuoi != null) {
            for (TrangThaiHD t : values()) {
                if (t.name().equalsIgnoreCase(chuoi.trim())) {
                    return t;
                }
            }
        }
        return DANG_PHUC_VU;
    }
}