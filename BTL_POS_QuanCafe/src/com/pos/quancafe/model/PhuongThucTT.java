package com.pos.quancafe.model;

/**
 * Hình thức thanh toán hóa đơn.
 */
public enum PhuongThucTT {
    TIEN_MAT("Tiền mặt"),
    CHUYEN_KHOAN("Chuyển khoản");

    private final String tenHienThi;

    PhuongThucTT(String tenHienThi) {
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
    public static PhuongThucTT fromChuoi(String chuoi) {
        if (chuoi != null) {
            for (PhuongThucTT p : values()) {
                if (p.name().equalsIgnoreCase(chuoi.trim())) {
                    return p;
                }
            }
        }
        return TIEN_MAT;
    }
}