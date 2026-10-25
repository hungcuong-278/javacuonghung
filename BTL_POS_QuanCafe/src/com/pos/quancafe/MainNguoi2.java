package com.pos.quancafe;

import com.pos.quancafe.data.HoaDonRepo;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Điểm khởi chạy riêng cho phần công việc của Người 2.
 * <p>
 * Mở khung chính {@link MainFrame} gồm 4 màn hình: Sơ đồ bàn, Gọi món,
 * Quản lý món và Lịch sử hóa đơn. Chạy độc lập, không phụ thuộc phần code
 * của Người 1.
 */
public class MainNguoi2 {

    public static void main(String[] args) {
        // Tạo dữ liệu hóa đơn mẫu nếu chưa có, để màn hình thống kê có số liệu ngay
        HoaDonRepo hoaDonRepo = new HoaDonRepo();
        hoaDonRepo.taoDuLieuMauNeuRong();

        SwingUtilities.invokeLater(() -> {
            try {
                // Dùng giao diện mặc định của hệ điều hành
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                // Không đổi được giao diện thì dùng giao diện mặc định
            }

            new MainFrame().setVisible(true);
        });
    }
}
