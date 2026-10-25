package com.pos.quancafe;

import com.pos.quancafe.data.HoaDonRepo;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Điểm khởi chạy của ứng dụng POS quán cà phê.
 * <p>
 * Mở khung chính {@link MainFrame} gồm 4 màn hình: Sơ đồ bàn, Gọi món,
 * Quản lý món và Lịch sử hóa đơn.
 */
public class Main {

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
