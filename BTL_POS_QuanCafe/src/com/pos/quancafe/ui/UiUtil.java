package com.pos.quancafe.ui;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;

/**
 * Hàm dùng chung cho giao diện: tạo nút, pha màu.
 * Phần này thuộc công việc của Người 2.
 */
public class UiUtil {

    private UiUtil() {
        // Lớp tiện ích, không khởi tạo
    }

    /**
     * Tạo nút có màu nền nhạt và chữ đen.
     * <p>
     * Vì sao không dùng nền đậm với chữ trắng: trên Windows, hệ giao diện mặc định
     * (WindowsLookAndFeel) tự vẽ nền nút theo giao diện XP, bỏ qua lệnh
     * {@code setBackground} của JButton. Kết quả là chữ trắng nằm trên nền
     * trắng của hệ thống, không đọc được.
     * <p>
     * Cách chắc chắn hơn là nền nhạt kèm chữ đen, đọc được trên mọi hệ giao diện.
     *
     * @param chu      chữ trên nút
     * @param mauNen   màu chủ đạo, dùng để pha ra nền nhạt và đường viền
     * @param hanhDong hành động khi bấm nút
     * @return nút đã sẵn sàng hiển thị
     */
    public static JButton taoNut(String chu, Color mauNen, Runnable hanhDong) {
        JButton btn = new JButton(chu);

        // Nền nhạt lấy từ màu chủ đạo, chữ luôn là đen cho dễ đọc
        btn.setBackground(lamNhat(mauNen, 0.80));
        btn.setForeground(Color.BLACK);
        btn.setFont(new Font("Arial", Font.BOLD, 12));

        // Bỏ viền mặc định của hệ giao diện, thay bằng viền màu mình kiểm soát.
        // Lưu ý: phải gọi setOpaque(true) SAU setContentAreaFilled(false),
        // vì setContentAreaFilled(false) tự đặt lại opaque thành false.
        btn.setContentAreaFilled(false);
        btn.setOpaque(true);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(mauNen, 2),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)));

        btn.setPreferredSize(new Dimension(120, 34));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        if (hanhDong != null) {
            btn.addActionListener(e -> hanhDong.run());
        }

        return btn;
    }

    /**
     * Pha một màu với màu trắng.
     *
     * @param mau       màu gốc
     * @param tyLeTrang 0 = giữ nguyên, 1 = thành trắng hoàn toàn
     * @return màu sau khi pha
     */
    public static Color lamNhat(Color mau, double tyLeTrang) {
        if (mau == null) {
            return Color.WHITE;
        }
        if (tyLeTrang < 0) {
            tyLeTrang = 0;
        }
        if (tyLeTrang > 1) {
            tyLeTrang = 1;
        }

        int r = (int) Math.round(mau.getRed() + (255 - mau.getRed()) * tyLeTrang);
        int g = (int) Math.round(mau.getGreen() + (255 - mau.getGreen()) * tyLeTrang);
        int b = (int) Math.round(mau.getBlue() + (255 - mau.getBlue()) * tyLeTrang);

        return new Color(r, g, b);
    }

    /**
     * Chọn màu chữ tương phản với nền: nền sáng thì chữ tối, nền tối thì chữ sáng.
     * Dùng cho các thẻ số liệu có nền tùy ý.
     */
    public static Color chuTuongPhan(Color nen) {
        if (nen == null) {
            return Color.BLACK;
        }

        // Độ sáng trung bình theo công thức của W3C
        double doSang = (0.299 * nen.getRed()
                + 0.587 * nen.getGreen()
                + 0.114 * nen.getBlue()) / 255.0;

        return doSang > 0.6 ? Color.BLACK : Color.WHITE;
    }
}
