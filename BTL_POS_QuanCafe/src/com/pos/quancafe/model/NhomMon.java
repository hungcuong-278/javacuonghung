package com.pos.quancafe.model;

/**
 * Nhóm món trong thực đơn quán cà phê.
 * Dùng để chia thực đơn thành các tab khi gọi món.
 */
public enum NhomMon {
    CA_PHE("Cà phê"),
    NUOC_UONG("Nước uống"),
    TRA("Trà"),
    BANH("Bánh");

    /** Tên tiếng Việt có dấu, hiển thị trên giao diện */
    private final String tenHienThi;

    NhomMon(String tenHienThi) {
        this.tenHienThi = tenHienThi;
    }

    public String getTenHienThi() {
        return tenHienThi;
    }

    @Override
    public String toString() {
        return tenHienThi;
    }

    /**
     * Đổi chuỗi trong file CSV thành enum.
     *
     * @param chuoi tên nhóm đọc từ file (CA_PHE, NUOC_UONG, ...)
     * @return nhóm tương ứng, hoặc CA_PHE nếu chuỗi không hợp lệ
     */
    public static NhomMon fromChuoi(String chuoi) {
        if (chuoi == null) {
            return CA_PHE;
        }
        for (NhomMon n : values()) {
            if (n.name().equalsIgnoreCase(chuoi.trim())) {
                return n;
            }
        }
        return CA_PHE;
    }
}