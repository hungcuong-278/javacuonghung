package com.pos.quancafe;

import com.pos.quancafe.data.BanRepo;
import com.pos.quancafe.data.HoaDonRepo;
import com.pos.quancafe.data.MonRepo;
import com.pos.quancafe.model.Ban;
import com.pos.quancafe.model.TrangThaiBan;
import com.pos.quancafe.service.OrderService;
import com.pos.quancafe.service.ThongKeService;
import com.pos.quancafe.ui.UiUtil;
import com.pos.quancafe.ui.panel.BanPanel;
import com.pos.quancafe.ui.panel.GoiMonPanel;
import com.pos.quancafe.ui.panel.LichSuHoaDonPanel;
import com.pos.quancafe.ui.panel.QuanLyMonPanel;
import com.pos.quancafe.util.FormatUtil;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.util.List;

/**
 * Khung chính của ứng dụng POS quán cà phê.
 * <p>
 * Bố cục: thanh menu bên trái liệt kê các màn hình, vùng nội dung bên phải
 * dùng {@link CardLayout} để chuyển qua lại mà không mở thêm cửa sổ.
 * <p>
 * Màn hình Sơ đồ bàn và Gọi món liên kết với nhau: bấm vào bàn ở sơ đồ sẽ
 * chuyển sang màn hình gọi món của đúng bàn đó.
 */
public class MainFrame extends JFrame {

    // Các khoá dùng cho CardLayout
    private static final String MAN_HINH_BAN = "BAN";
    private static final String MAN_HINH_GOI_MON = "GOI_MON";
    private static final String MAN_HINH_MON = "MON";
    private static final String MAN_HINH_LICH_SU = "LICH_SU";

    // Tầng dữ liệu
    private final BanRepo banRepo;
    private final HoaDonRepo hoaDonRepo;
    private final MonRepo monRepo;
    private final OrderService orderService;
    private final ThongKeService thongKeService;

    // Các màn hình
    private final BanPanel banPanel;
    private final GoiMonPanel goiMonPanel;
    private final QuanLyMonPanel quanLyMonPanel;
    private final LichSuHoaDonPanel lichSuHoaDonPanel;

    // Vùng nội dung và bộ chuyển màn hình
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel pnlNoiDung = new JPanel(cardLayout);

    // Nút menu bên trái, dùng để tô màu nút đang chọn
    private final java.util.Map<String, javax.swing.JButton> dsNutMenu = new java.util.LinkedHashMap<>();

    // Bàn đang được gọi món
    private String banDangGoiMon = null;

    public MainFrame() {
        super("Quản lý quán cà phê - Phần Người 2");

        banRepo = new BanRepo();
        hoaDonRepo = new HoaDonRepo();
        monRepo = new MonRepo();
        orderService = new OrderService(banRepo, hoaDonRepo, monRepo);
        thongKeService = new ThongKeService(hoaDonRepo);

        banPanel = new BanPanel(banRepo, hoaDonRepo, orderService);
        goiMonPanel = new GoiMonPanel(monRepo, orderService);
        quanLyMonPanel = new QuanLyMonPanel(monRepo);
        lichSuHoaDonPanel = new LichSuHoaDonPanel(hoaDonRepo, thongKeService);

        // Bấm bàn ở sơ đồ thì chuyển sang màn hình gọi món của bàn đó
        banPanel.setMoManHinhGoiMon(this::moGoiMon);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280, 780);
        setMinimumSize(new Dimension(1100, 700));
        setLocationRelativeTo(null);
        getContentPane().setBackground(Color.WHITE);

        setLayout(new BorderLayout());
        add(taoThanhMenu(), BorderLayout.WEST);
        add(pnlNoiDung, BorderLayout.CENTER);
        add(taoChanTrang(), BorderLayout.SOUTH);

        // Đưa các màn hình vào CardLayout
        pnlNoiDung.add(banPanel, MAN_HINH_BAN);
        pnlNoiDung.add(goiMonPanel, MAN_HINH_GOI_MON);
        pnlNoiDung.add(quanLyMonPanel, MAN_HINH_MON);
        pnlNoiDung.add(lichSuHoaDonPanel, MAN_HINH_LICH_SU);

        chuyenManHinh(MAN_HINH_BAN);
    }

    /** Thanh menu bên trái liệt kê các màn hình của ứng dụng */
    private JPanel taoThanhMenu() {
        JPanel thanh = new JPanel();
        thanh.setLayout(new javax.swing.BoxLayout(thanh, javax.swing.BoxLayout.Y_AXIS));
        thanh.setBackground(new Color(0x3E, 0x2C, 0x23));
        thanh.setBorder(BorderFactory.createEmptyBorder(18, 12, 18, 12));
        thanh.setPreferredSize(new Dimension(210, 100));

        // Tiêu đề quán
        JLabel lblTenQuan = new JLabel("QUÁN CÀ PHÊ");
        lblTenQuan.setFont(new Font("Arial", Font.BOLD, 17));
        lblTenQuan.setForeground(Color.WHITE);
        lblTenQuan.setAlignmentX(CENTER_ALIGNMENT);
        thanh.add(lblTenQuan);

        JLabel lblPhuDe = new JLabel("Hệ thống quản lý bán hàng");
        lblPhuDe.setFont(new Font("Arial", Font.PLAIN, 11));
        lblPhuDe.setForeground(new Color(0xC9, 0xB8, 0xA8));
        lblPhuDe.setAlignmentX(CENTER_ALIGNMENT);
        thanh.add(lblPhuDe);

        thanh.add(javax.swing.Box.createVerticalStrut(22));

        // Các nút menu
        themNutMenu(thanh, MAN_HINH_BAN, "Sơ đồ bàn");
        themNutMenu(thanh, MAN_HINH_GOI_MON, "Gọi món");
        themNutMenu(thanh, MAN_HINH_MON, "Quản lý món");
        themNutMenu(thanh, MAN_HINH_LICH_SU, "Lịch sử hóa đơn");

        thanh.add(javax.swing.Box.createVerticalGlue());

        // Nút thoát
        javax.swing.JButton btnThoat = taoNutMenu("Thoát chương trình", new Color(0x8E, 0x24, 0x24));
        btnThoat.addActionListener(e -> {
            int luaChon = javax.swing.JOptionPane.showConfirmDialog(this,
                    "Thoát chương trình?",
                    "Xác nhận thoát", javax.swing.JOptionPane.YES_NO_OPTION);
            if (luaChon == javax.swing.JOptionPane.YES_OPTION) {
                dispose();
                System.exit(0);
            }
        });
        btnThoat.setAlignmentX(CENTER_ALIGNMENT);
        thanh.add(btnThoat);

        return thanh;
    }

    /**
     * Thêm một nút vào thanh menu.
     *
     * @param thanh  thanh menu
     * @param khoa   khoá màn hình
     * @param chu    chữ trên nút
     */
    private void themNutMenu(JPanel thanh, String khoa, String chu) {
        javax.swing.JButton nut = taoNutMenu(chu, new Color(0x6F, 0x4E, 0x37));
        nut.addActionListener(e -> chuyenManHinh(khoa));
        nut.setAlignmentX(CENTER_ALIGNMENT);
        thanh.add(nut);
        dsNutMenu.put(khoa, nut);
    }

    /**
     * Tạo một nút menu có màu nền kiểu phẳng, chữ trắng.
     * <p>
     * Lưu ý: phải đặt {@code setContentAreaFilled(false)} để hệ giao diện
     * Windows không tự vẽ đè nền trắng của hệ thống lên nền đậm của nút.
     * Nếu không, chữ trắng sẽ nằm trên nền trắng và không đọc được.
     */
    private javax.swing.JButton taoNutMenu(String chu, Color mauNen) {
        javax.swing.JButton nut = new javax.swing.JButton(chu);
        nut.setFont(new Font("Arial", Font.PLAIN, 14));
        nut.setBackground(mauNen);
        nut.setForeground(Color.WHITE);
        nut.setFocusPainted(false);
        nut.setContentAreaFilled(false);
        nut.setOpaque(true);
        nut.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(mauNen.darker(), 2),
                BorderFactory.createEmptyBorder(12, 10, 12, 10)));
        nut.setMaximumSize(new Dimension(190, 42));
        nut.setAlignmentX(CENTER_ALIGNMENT);
        nut.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return nut;
    }

    /** Chân trang hiển thị thông tin chung */
    private JPanel taoChanTrang() {
        JPanel chan = new JPanel(new BorderLayout());
        chan.setBorder(BorderFactory.createEmptyBorder(4, 10, 6, 10));
        chan.setBackground(Color.WHITE);

        lblTrangThai = new JLabel(" ");
        lblTrangThai.setFont(new Font("Arial", Font.PLAIN, 12));
        lblTrangThai.setForeground(Color.DARK_GRAY);

        chan.add(lblTrangThai, BorderLayout.WEST);

        lblDoanhThu = new JLabel(" ");
        lblDoanhThu.setFont(new Font("Arial", Font.BOLD, 13));
        lblDoanhThu.setForeground(new Color(0x2E, 0x7D, 0x32));
        chan.add(lblDoanhThu, BorderLayout.EAST);

        return chan;
    }

    /** Nhãn trạng thái ở chân trang */
    private JLabel lblTrangThai;

    /** Nhãn doanh thu hôm nay ở chân trang */
    private JLabel lblDoanhThu;

    /**
     * Chuyển sang một màn hình và tô màu nút menu tương ứng.
     *
     * @param khoa khoá màn hình
     */
    private void chuyenManHinh(String khoa) {
        // Cập nhật dữ liệu cho màn hình sắp hiển thị
        if (MAN_HINH_BAN.equals(khoa)) {
            banPanel.capNhat();
        } else if (MAN_HINH_GOI_MON.equals(khoa)) {
            goiMonPanel.capNhatGio();
        } else if (MAN_HINH_MON.equals(khoa)) {
            quanLyMonPanel.capNhatDuLieu();
        } else if (MAN_HINH_LICH_SU.equals(khoa)) {
            lichSuHoaDonPanel.capNhatDuLieu();
        }

        cardLayout.show(pnlNoiDung, khoa);

        // Tô màu nút đang chọn.
        // Nút đang chọn dùng nền sáng hơn nên chuyển sang chữ đen,
        // nếu giữ chữ trắng thì chữ sẽ không đọc được trên nền sáng.
        for (java.util.Map.Entry<String, javax.swing.JButton> entry : dsNutMenu.entrySet()) {
            boolean dangChon = entry.getKey().equals(khoa);
            entry.getValue().setBackground(dangChon ? new Color(0xA9, 0x7C, 0x5C) : new Color(0x6F, 0x4E, 0x37));
            entry.getValue().setForeground(dangChon ? Color.BLACK : Color.WHITE);
            entry.getValue().setFont(dangChon
                    ? new Font("Arial", Font.BOLD, 14)
                    : new Font("Arial", Font.PLAIN, 14));
        }

        capNhatChanTrang(khoa);
    }

    /** Mở màn hình gọi món cho một bàn */
    private void moGoiMon(String maBan) {
        banDangGoiMon = maBan;
        goiMonPanel.chonBan(maBan);
        chuyenManHinh(MAN_HINH_GOI_MON);
    }

    /**
     * Cập nhật thông tin ở chân trung: số bàn đang phục vụ và doanh thu hôm nay.
     */
    private void capNhatChanTrang(String khoa) {
        // Đếm bàn đang phục vụ
        int dangPhucVu = 0;
        int choThanhToan = 0;
        List<Ban> dsBan = banRepo.findAll();
        for (Ban ban : dsBan) {
            if (ban.getTrangThai() == TrangThaiBan.CO_KHACH) {
                dangPhucVu++;
            } else if (ban.getTrangThai() == TrangThaiBan.CHO_THANH_TOAN) {
                choThanhToan++;
            }
        }

        String tenManHinh;
        switch (khoa) {
            case MAN_HINH_BAN:
                tenManHinh = "Sơ đồ bàn";
                break;
            case MAN_HINH_GOI_MON:
                tenManHinh = banDangGoiMon == null ? "Gọi món" : "Gọi món - Bàn " + banDangGoiMon;
                break;
            case MAN_HINH_MON:
                tenManHinh = "Quản lý món";
                break;
            default:
                tenManHinh = "Lịch sử hóa đơn";
                break;
        }

        lblTrangThai.setText(tenManHinh
                + "   |   Bàn đang có khách: " + dangPhucVu
                + "   |   Chờ thanh toán: " + choThanhToan);

        long doanhThu = thongKeService.doanhThuTrongNgay(LocalDate.now());
        lblDoanhThu.setText("Doanh thu hôm nay: " + FormatUtil.formatTien(doanhThu));
    }
}
