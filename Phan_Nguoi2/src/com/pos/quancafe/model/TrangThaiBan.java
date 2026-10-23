package com.pos.quancafe.model;

/**
 * Trạng thái của một bàn trong quán.
 * Thuộc phần định nghĩa chung của chương trình.
 */
public enum TrangThaiBan {
    TRONG("Trống"),
    CO_KHACH("Có khách"),
    CHO_THANH_TOAN("Chờ thanh toán");

    private final String tenHienThi;

    TrangThaiBan(String tenHienThi) {
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
    public static TrangThaiBan fromChuoi(String chuoi) {
        if (chuoi != null) {
            for (TrangThaiBan t : values()) {
                if (t.name().equalsIgnoreCase(chuoi.trim())) {
                    return t;
                }
            }
        }
        return TRONG;
    }
}