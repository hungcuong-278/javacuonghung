package com.pos.quancafe.ui.panel;

import com.pos.quancafe.data.BanRepo;
import com.pos.quancafe.data.HoaDonRepo;
import com.pos.quancafe.model.Ban;
import com.pos.quancafe.model.HoaDon;
import com.pos.quancafe.model.TrangThaiBan;
import com.pos.quancafe.service.OrderService;
import com.pos.quancafe.ui.UiUtil;
import com.pos.quancafe.util.FormatUtil;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Màn hình sơ đồ bàn: hiển thị 9 bàn dạng lưới 3x3, tô màu theo trạng thái.
 * <p>
 * Mỗi bàn là một nút lớn, bên trong hiện mã bàn, trạng thái và tổng tiền
 * đang giao dịch. Bấm vào bàn trống để mở bàn, bấm vào bàn đang phục vụ
 * để vào màn hình gọi món. Chuột phải lên bàn để xem hóa đơn hoặc huỷ bàn.
 */
public class BanPanel extends JPanel {

    // Màu theo trạng thái bàn
    private static final Color MAU_TRONG = new Color(0x2E, 0xCC, 0x71);
    private static final Color MAU_CO_KHACH = new Color(0x34, 0x98, 0xDB);
    private static final Color MAU_CHO_THANH_TOAN = new Color(0xF1, 0xC4, 0x0F);

    private final BanRepo banRepo;
    private final HoaDonRepo hoaDonRepo;
    private final OrderService orderService;

    /** Ánh xạ mã bàn sang nút để cập nhật nhanh khi vẽ lại */
    private final Map<String, JButton> dsNutBan = new HashMap<>();

    /** Lời gọi lại khi cần mở màn hình gọi món cho một bàn */
    private java.util.function.Consumer<String> moManHinhGoiMon;

    public BanPanel(BanRepo banRepo, HoaDonRepo hoaDonRepo, OrderService orderService) {
        this.banRepo = banRepo;
        this.hoaDonRepo = hoaDonRepo;
        this.orderService = orderService;

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        add(taoTieuDe(), BorderLayout.NORTH);
        add(taoLuoiBan(), BorderLayout.CENTER);
        add(taoBangThongTin(), BorderLayout.SOUTH);

        capNhat();
    }

    /**
     * Đặt hành động được gọi khi người dùng bấm vào một bàn.
     *
     * @param hanhDong nhận mã bàn cần mở màn hình gọi món
     */
    public void setMoManHinhGoiMon(java.util.function.Consumer<String> hanhDong) {
        this.moManHinhGoiMon = hanhDong;
    }

    /** Tiêu đề phía trên kèm chú thích màu */
    private JPanel taoTieuDe() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        JLabel lblTieuDe = new JLabel("SƠ ĐỒ BÀN", SwingConstants.CENTER);
        lblTieuDe.setFont(new Font("Arial", Font.BOLD, 20));
        lblTieuDe.setForeground(Color.BLACK);
        panel.add(lblTieuDe, BorderLayout.CENTER);

        JPanel panelChuThich = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 14, 0));
        panelChuThich.setOpaque(false);
        panelChuThich.add(taoChuThich(MAU_TRONG, "Trống"));
        panelChuThich.add(taoChuThich(MAU_CO_KHACH, "Có khách"));
        panelChuThich.add(taoChuThich(MAU_CHO_THANH_TOAN, "Chờ thanh toán"));
        panel.add(panelChuThich, BorderLayout.SOUTH);

        return panel;
    }

    /** Ô màu kèm chú thích trạng thái */
    private JPanel taoChuThich(Color mau, String chu) {
        JPanel p = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 4, 0));
        p.setOpaque(false);

        JLabel oMau = new JLabel("     ");
        oMau.setOpaque(true);
        oMau.setBackground(mau);
        oMau.setBorder(BorderFactory.createLineBorder(mau.darker(), 1));
        p.add(oMau);

        JLabel lblChu = new JLabel(chu);
        lblChu.setFont(new Font("Arial", Font.PLAIN, 12));
        p.add(lblChu);

        return p;
    }

    /** Lưới 3x3 chứa 9 nút bàn */
    private JPanel taoLuoiBan() {
        JPanel luoi = new JPanel(new GridLayout(3, 3, 10, 10));
        luoi.setOpaque(false);

        dsNutBan.clear();
        List<Ban> dsBan = banRepo.findAll();

        for (Ban ban : dsBan) {
            JButton nut = taoNutBan(ban);
            dsNutBan.put(ban.getMaBan(), nut);
            luoi.add(nut);
        }

        return luoi;
    }

    /**
     * Tạo nút cho một bàn.
     * <p>
     * Dùng HTML bên trong {@code setText} để nội dung xuống nhiều dòng.
     */
    private JButton taoNutBan(Ban ban) {
        JButton nut = new JButton();
        nut.setFont(new Font("Arial", Font.BOLD, 13));
        nut.setFocusPainted(false);
        // Không để hệ giao diện Windows tự vẽ đè nền nút,
        // nếu không nền sẽ bị thay bằng màu trắng của hệ thống.
        // Lưu ý: phải gọi setOpaque(true) SAU setContentAreaFilled(false),
        // vì setContentAreaFilled(false) tự đặt lại opaque thành false.
        nut.setContentAreaFilled(false);
        nut.setOpaque(true);
        nut.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(mauTheoTrangThai(ban.getTrangThai()), 2),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        nut.setPreferredSize(new Dimension(140, 110));
        nut.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        // Bấm vào bàn
        nut.addActionListener(e -> xuLyBamBan(ban.getMaBan()));

        // Chuột phải lên bàn
        nut.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                hienMenuBan(e, ban.getMaBan());
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                hienMenuBan(e, ban.getMaBan());
            }
        });

        veNutBan(ban);
        return nut;
    }

    /** Menu chuột phải: xem hóa đơn, huỷ bàn */
    private void hienMenuBan(MouseEvent e, String maBan) {
        if (!e.isPopupTrigger()) {
            return;
        }

        Ban ban = banRepo.findByMa(maBan);
        if (ban == null) {
            return;
        }

        JPopupMenu menu = new JPopupMenu();

        JMenuItem mnuXemHoaDon = new JMenuItem("Xem hóa đơn");
        mnuXemHoaDon.addActionListener(ev -> xemHoaDon(maBan));
        mnuXemHoaDon.setEnabled(ban.getMaHoaDon() != null);
        menu.add(mnuXemHoaDon);

        JMenuItem mnuHuyBan = new JMenuItem("Huỷ bàn");
        mnuHuyBan.addActionListener(ev -> huyBan(maBan));
        // Chỉ huỷ được khi bàn còn đang phục vụ, chưa tính tiền
        mnuHuyBan.setEnabled(ban.getTrangThai() == TrangThaiBan.CO_KHACH
                || ban.getTrangThai() == TrangThaiBan.CHO_THANH_TOAN);
        menu.add(mnuHuyBan);

        menu.show(e.getComponent(), e.getX(), e.getY());
    }

    /**
     * Vẽ lại nội dung và màu của một nút bàn theo trạng thái hiện tại.
     * <p>
     * Màu nền được pha nhạt từ màu trạng thái, chữ đen, để luôn đọc được.
     * Nếu dùng nền đậm với chữ trắng thì trên Windows, hệ giao diện sẽ vẽ đè
     * nền trắng của hệ thống và chữ trắng sẽ biến mất.
     */
    private void veNutBan(Ban ban) {
        JButton nut = dsNutBan.get(ban.getMaBan());
        if (nut == null) {
            return;
        }

        Color nen = mauTheoTrangThai(ban.getTrangThai());
        nut.setBackground(UiUtil.lamNhat(nen, 0.80));
        nut.setForeground(Color.BLACK);

        // Viền theo màu trạng thái, để vẫn nhận ra màu trạng thái của bàn
        nut.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(nen, 2),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)));

        // Lấy tổng tiền đang giao dịch trên bàn
        String dongTien = "";
        HoaDon hd = layHoaDonBan(ban);
        if (hd != null && hd.getTongTien() > 0) {
            dongTien = "<br/>" + FormatUtil.formatTien(hd.getTongTien());
        }

        nut.setText("<html><center><b>" + ban.getMaBan() + "</b><br/>"
                + ban.getTrangThai().getTenHienThi() + dongTien + "</center></html>");
    }

    /** Màu nền theo trạng thái bàn */
    private Color mauTheoTrangThai(TrangThaiBan trangThai) {
        if (trangThai == TrangThaiBan.CO_KHACH) {
            return MAU_CO_KHACH;
        }
        if (trangThai == TrangThaiBan.CHO_THANH_TOAN) {
            return MAU_CHO_THANH_TOAN;
        }
        return MAU_TRONG;
    }

    /** Lấy hóa đơn đang gắn với bàn, null nếu bàn không có hóa đơn */
    private HoaDon layHoaDonBan(Ban ban) {
        if (ban.getMaHoaDon() == null) {
            return null;
        }
        return hoaDonRepo.findByMa(ban.getMaHoaDon());
    }

    /**
     * Xử lý khi bấm vào một bàn.
     * <p>
     * Bàn trống thì hỏi xác nhận rồi mở bàn. Bàn đang phục vụ thì mở
     * thẳng màn hình gọi món để xem, thêm món hoặc thanh toán.
     */
    private void xuLyBamBan(String maBan) {
        Ban ban = banRepo.findByMa(maBan);
        if (ban == null) {
            return;
        }

        if (ban.getTrangThai() == TrangThaiBan.TRONG) {
            int luaChon = JOptionPane.showConfirmDialog(this,
                    "Mở bàn " + ban.getMaBan() + " cho khách mới?",
                    "Mở bàn", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (luaChon != JOptionPane.YES_OPTION) {
                return;
            }

            Ban banMoi = orderService.moBan(maBan);
            if (banMoi == null) {
                JOptionPane.showMessageDialog(this,
                        "Không mở được bàn " + maBan + ".",
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            capNhat();
            moGoiMon(maBan);
        } else {
            // Bàn đang có khách: vào màn hình gọi món
            moGoiMon(maBan);
        }
    }

    /** Gọi màn hình gọi món nếu lớp ngoài đã đăng ký */
    private void moGoiMon(String maBan) {
        if (moManHinhGoiMon != null) {
            moManHinhGoiMon.accept(maBan);
        }
    }

    /** Xem nội dung hóa đơn của bàn trong hộp thoại */
    private void xemHoaDon(String maBan) {
        Ban ban = banRepo.findByMa(maBan);
        if (ban == null || ban.getMaHoaDon() == null) {
            JOptionPane.showMessageDialog(this,
                    "Bàn này chưa có hóa đơn.",
                    "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        HoaDon hd = hoaDonRepo.findByMa(ban.getMaHoaDon());
        if (hd == null) {
            JOptionPane.showMessageDialog(this,
                    "Không tìm thấy hóa đơn của bàn này.",
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String noiDung = com.pos.quancafe.util.HoaDonPrinter.taoNoiDungTuChiTiet(
                hd.getMaHD(),
                hd.getMaBan(),
                hd.getNgayMo(),
                hd.getNgayDong(),
                hd.getDsChiTiet(),
                hd.getTongTien(),
                hd.getPhuongThucTT() == null ? "" : hd.getPhuongThucTT().getTenHienThi(),
                hd.getTienKhachDua(),
                hd.getTienThua());

        new com.pos.quancafe.ui.XemHoaDonDialog(this, hd.getMaHD(), noiDung).setVisible(true);
    }

    /** Huỷ bàn: xoá hóa đơn đang mở và đưa bàn về trạng thái trống */
    private void huyBan(String maBan) {
        Ban ban = banRepo.findByMa(maBan);
        if (ban == null || ban.getTrangThai() == TrangThaiBan.TRONG) {
            return;
        }

        int luaChon = JOptionPane.showConfirmDialog(this,
                "Huỷ bàn " + ban.getMaBan() + "?\n"
                        + "Hóa đơn đang mở của bàn này sẽ bị xoá.",
                "Xác nhận huỷ bàn", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (luaChon != JOptionPane.YES_OPTION) {
            return;
        }

        if (!orderService.huyBan(maBan)) {
            JOptionPane.showMessageDialog(this,
                    "Không huỷ được bàn này.\n"
                            + "Bàn đã yêu cầu tính tiền thì hãy thanh toán hoặc huỷ ở màn hình gọi món.",
                    "Không huỷ được", JOptionPane.WARNING_MESSAGE);
            return;
        }

        capNhat();
    }

    /** Bảng thống kê nhỏ ở dưới: số bàn đang phục vụ */
    private JPanel taoBangThongTin() {
        JPanel panel = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 20, 6));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createLineBorder(new Color(0xCC, 0xCC, 0xCC)));

        lblThongKeBan = new JLabel(" ", SwingConstants.CENTER);
        lblThongKeBan.setFont(new Font("Arial", Font.BOLD, 13));
        panel.add(lblThongKeBan);

        return panel;
    }

    /** Nhãn thống kê số bàn đang phục vụ */
    private JLabel lblThongKeBan;

    /**
     * Vẽ lại toàn bộ sơ đồ bàn.
     * <p>
     * Hàm này được gọi mỗi lần quay lại màn hình này, vì trạng thái bàn
     * có thể đã thay đổi ở màn hình gọi món.
     */
    public void capNhat() {
        List<Ban> dsBan = banRepo.findAll();

        for (Ban ban : dsBan) {
            JButton nut = dsNutBan.get(ban.getMaBan());
            if (nut == null) {
                continue;
            }
            veNutBan(ban);
        }

        // Đếm số bàn đang phục vụ
        int dangPhucVu = 0;
        int choThanhToan = 0;
        for (Ban ban : dsBan) {
            if (ban.getTrangThai() == TrangThaiBan.CO_KHACH) {
                dangPhucVu++;
            } else if (ban.getTrangThai() == TrangThaiBan.CHO_THANH_TOAN) {
                choThanhToan++;
            }
        }
        lblThongKeBan.setText("Có khách: " + dangPhucVu
                + " bàn   |   Chờ thanh toán: " + choThanhToan
                + " bàn   |   Tổng số bàn: " + dsBan.size());
    }
}
