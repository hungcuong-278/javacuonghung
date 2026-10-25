package com.pos.quancafe.ui.panel;

import com.pos.quancafe.data.MonRepo;
import com.pos.quancafe.model.Ban;
import com.pos.quancafe.model.ChiTietHoaDon;
import com.pos.quancafe.model.HoaDon;
import com.pos.quancafe.model.Mon;
import com.pos.quancafe.model.NhomMon;
import com.pos.quancafe.model.PhuongThucTT;
import com.pos.quancafe.model.TrangThaiBan;
import com.pos.quancafe.model.TrangThaiHD;
import com.pos.quancafe.service.OrderService;
import com.pos.quancafe.ui.UiUtil;
import com.pos.quancafe.util.FormatUtil;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * Màn hình gọi món cho một bàn: chọn món theo nhóm, giỏ hàng, thanh toán.
 * <p>
 * Bên trái là 4 tab món theo nhóm (Cà phê, Nước uống, Trà, Bánh), bên phải
 * là giỏ hàng của bàn. Mỗi món là một nút bấm được, bấm vào để thêm vào
 * giỏ. Bấm đúp vào một dòng trong giỏ để tăng số lượng, chuột phải để giảm
 * hoặc xóa.
 */
public class GoiMonPanel extends JPanel {

    private final MonRepo monRepo;
    private final OrderService orderService;

    /** Mã bàn đang gọi món */
    private String maBanHienTai = null;

    // Bảng giỏ hàng
    private final DefaultTableModel modelGio = new DefaultTableModel(
            new Object[]{"Mã món", "Tên món", "SL", "Đơn giá", "Thành tiền"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable bangGio = new JTable(modelGio);

    // Thẻ thông tin bàn và tổng tiền
    private final JLabel lblBan = new JLabel("Chưa chọn bàn");
    private final JLabel lblHoaDon = new JLabel(" ");
    private final JLabel lblTongTien = new JLabel("0 đ");

    // Thanh tìm kiếm món
    private final JTextField txtTimKiem = new JTextField(18);

    // Lựa chọn thanh toán
    private final JComboBox<PhuongThucTT> cboPhuongThuc = new JComboBox<>(PhuongThucTT.values());
    private final JTextField txtTienKhachDua = new JTextField(12);
    private final JLabel lblTienThua = new JLabel("0 đ");

    /** Bảng các nút món, dùng để vẽ lại khi tìm kiếm */
    private JTabbedPane tabNhomMon;

    /** Các khung chứa nút món, theo đúng thứ tự các tab */
    private final java.util.Map<NhomMon, JPanel> dsKhungMon = new java.util.LinkedHashMap<>();

    public GoiMonPanel(MonRepo monRepo, OrderService orderService) {
        this.monRepo = monRepo;
        this.orderService = orderService;

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        add(taoTieuDe(), BorderLayout.NORTH);
        add(taoNoiDung(), BorderLayout.CENTER);
        add(taoHangCongCu(), BorderLayout.SOUTH);

        capNhatGio();
    }

    // ==================== Bố cục giao diện ====================

    /** Tiêu đề kèm thông tin bàn và hóa đơn */
    private JPanel taoTieuDe() {
        JPanel panel = new JPanel(new BorderLayout(10, 5));
        panel.setOpaque(false);

        JLabel lblTieuDe = new JLabel("GỌI MÓN", SwingConstants.CENTER);
        lblTieuDe.setFont(new Font("Arial", Font.BOLD, 20));
        lblTieuDe.setForeground(Color.BLACK);
        panel.add(lblTieuDe, BorderLayout.CENTER);

        JPanel panelThongTin = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        panelThongTin.setOpaque(false);

        lblBan.setFont(new Font("Arial", Font.BOLD, 14));
        panelThongTin.add(lblBan);

        lblHoaDon.setFont(new Font("Arial", Font.PLAIN, 12));
        lblHoaDon.setForeground(Color.DARK_GRAY);
        panelThongTin.add(lblHoaDon);

        panel.add(panelThongTin, BorderLayout.SOUTH);

        return panel;
    }

    /** Khu vực chính: bên trái danh sách món, bên phải giỏ hàng */
    private JPanel taoNoiDung() {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setOpaque(false);

        // Bên trái: tab theo nhóm món
        JTabbedPane tab = new JTabbedPane();
        this.tabNhomMon = tab;
        this.dsKhungMon.clear();

        for (NhomMon nhom : NhomMon.values()) {
            JPanel pnl = taoDanhSachMonTheoNhom(nhom);
            dsKhungMon.put(nhom, pnl);
            tab.addTab(nhom.getTenHienThi(), pnl);
        }

        JPanel panelTrai = new JPanel(new BorderLayout(0, 6));
        panelTrai.setOpaque(false);

        // Ô tìm kiếm món
        JPanel panelTimKiem = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        panelTimKiem.setOpaque(false);
        panelTimKiem.add(new JLabel("Tìm món:"));
        panelTimKiem.add(txtTimKiem);
        panelTimKiem.add(UiUtil.taoNut("Tìm", new Color(0xEF, 0x6C, 0x00), () -> timKiemMon()));
        panelTimKiem.add(UiUtil.taoNut("Xem tất cả", new Color(0x75, 0x75, 0x75), () -> {
            txtTimKiem.setText("");
            veLaiTatCaTab();
        }));
        panelTrai.add(panelTimKiem, BorderLayout.NORTH);

        panelTrai.add(tab, BorderLayout.CENTER);
        panel.add(panelTrai, BorderLayout.CENTER);

        // Bên phải: giỏ hàng
        panel.add(taoPhanGio(), BorderLayout.EAST);

        return panel;
    }

    /** Bảng giỏ hàng và tổng tiền */
    private JPanel taoPhanGio() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(520, 100));

        bangGio.setRowHeight(26);
        bangGio.setFont(new Font("Arial", Font.PLAIN, 13));
        bangGio.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        bangGio.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        bangGio.getTableHeader().setBackground(new Color(0xF5, 0xDE, 0xB3));

        bangGio.getColumnModel().getColumn(0).setPreferredWidth(70);
        bangGio.getColumnModel().getColumn(1).setPreferredWidth(150);
        bangGio.getColumnModel().getColumn(2).setPreferredWidth(45);
        bangGio.getColumnModel().getColumn(3).setPreferredWidth(90);
        bangGio.getColumnModel().getColumn(4).setPreferredWidth(100);

        for (int cot = 0; cot < modelGio.getColumnCount(); cot++) {
            bangGio.getColumnModel().getColumn(cot).setCellRenderer(new GioRenderer(cot));
        }

        // Bấm đúp vào dòng để tăng số lượng
        bangGio.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && bangGio.getSelectedRow() >= 0) {
                    tangSoLuong();
                }
            }
        });

        panel.add(new JScrollPane(bangGio), BorderLayout.CENTER);

        // Thẻ tổng tiền
        JPanel panelTong = new JPanel(new BorderLayout(5, 5));
        panelTong.setBackground(UiUtil.lamNhat(new Color(0x2E, 0x7D, 0x32), 0.88));
        panelTong.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0x2E, 0x7D, 0x32), 2),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));

        JLabel lblTieuDeTong = new JLabel("TỔNG TIỀN", SwingConstants.CENTER);
        lblTieuDeTong.setFont(new Font("Arial", Font.BOLD, 13));
        lblTieuDeTong.setForeground(Color.BLACK);

        lblTongTien.setHorizontalAlignment(SwingConstants.CENTER);
        lblTongTien.setFont(new Font("Arial", Font.BOLD, 26));
        lblTongTien.setForeground(Color.BLACK);

        panelTong.add(lblTieuDeTong, BorderLayout.NORTH);
        panelTong.add(lblTongTien, BorderLayout.CENTER);
        panel.add(panelTong, BorderLayout.SOUTH);

        return panel;
    }

    /** Hàng nút thao tác phía dưới */
    private JPanel taoHangCongCu() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setOpaque(false);

        // Bên trái: các nút thao tác trên giỏ
        JPanel panelNut = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        panelNut.setOpaque(false);

        panelNut.add(UiUtil.taoNut("Tăng SL", new Color(0x2E, 0x7D, 0x32), () -> tangSoLuong()));
        panelNut.add(UiUtil.taoNut("Giảm SL", new Color(0xEF, 0x6C, 0x00), () -> giamSoLuong()));
        panelNut.add(UiUtil.taoNut("Xóa dòng", new Color(0xC6, 0x28, 0x28), () -> xoaDongGio()));
        panelNut.add(UiUtil.taoNut("Xóa hết giỏ", new Color(0x75, 0x75, 0x75), () -> xoaHetGio()));
        panelNut.add(Box.createHorizontalStrut(16));
        panelNut.add(UiUtil.taoNut("Yêu cầu tính tiền", new Color(0x6A, 0x1B, 0x9A), () -> yeuCauTinhTien()));
        panelNut.add(UiUtil.taoNut("Huỷ bàn", new Color(0x54, 0x54, 0x54), () -> huyBan()));
        panel.add(panelNut, BorderLayout.NORTH);

        // Bên phải: phần thanh toán
        JPanel panelThanhToan = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        panelThanhToan.setOpaque(false);

        panelThanhToan.add(new JLabel("Hình thức TT:"));
        panelThanhToan.add(cboPhuongThuc);
        panelThanhToan.add(new JLabel("Tiền khách đưa:"));
        panelThanhToan.add(txtTienKhachDua);
        panelThanhToan.add(new JLabel("Tiền thừa:"));
        lblTienThua.setFont(new Font("Arial", Font.BOLD, 13));
        panelThanhToan.add(lblTienThua);
        panelThanhToan.add(UiUtil.taoNut("THANH TOÁN", new Color(0xC6, 0x28, 0x28), () -> thanhToan()));

        panel.add(panelThanhToan, BorderLayout.SOUTH);

        return panel;
    }

    // ==================== Danh sách món ====================

    /** Tạo khung chứa danh sách món của một nhóm */
    private JPanel taoDanhSachMonTheoNhom(NhomMon nhom) {
        JPanel pnl = new JPanel(new BorderLayout());
        pnl.setOpaque(false);
        pnl.setName(nhom.name());

        veDanhSachMon(pnl, nhom, "");

        return pnl;
    }

    /**
     * Vẽ danh sách nút món.
     *
     * @param pnl      khung chứa nút
     * @param nhom     nhóm món, null nghĩa là tất cả các nhóm
     * @param tuKhoa   từ khóa lọc, rỗng nghĩa là không lọc
     */
    private void veDanhSachMon(JPanel pnl, NhomMon nhom, String tuKhoa) {
        pnl.removeAll();

        List<Mon> dsMon = monRepo.timTheoTen(tuKhoa);
        if (nhom != null) {
            dsMon.removeIf(m -> m.getNhomMon() != nhom);
        }

        if (dsMon.isEmpty()) {
            JLabel lbl = new JLabel("Không có món nào khớp", SwingConstants.CENTER);
            lbl.setForeground(Color.GRAY);
            lbl.setFont(new Font("Arial", Font.ITALIC, 14));
            pnl.add(lbl, BorderLayout.CENTER);
            pnl.revalidate();
            pnl.repaint();
            return;
        }

        // Bố trí nút món theo lưới 3 cột
        JPanel luoi = new JPanel(new GridLayout(0, 3, 8, 8));
        luoi.setOpaque(false);

        for (Mon mon : dsMon) {
            luoi.add(taoNutMon(mon));
        }

        JScrollPane scroll = new JScrollPane(luoi);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        pnl.add(scroll, BorderLayout.CENTER);

        pnl.revalidate();
        pnl.repaint();
    }

    /**
     * Tạo nút cho một món.
     * <p>
     * Món hết hàng thì nút bị làm mờ và không bấm được.
     * <p>
     * Nền được pha nhạt và chữ đen để luôn đọc được. Lưu ý phải đặt
     * {@code setContentAreaFilled(false)}, nếu không hệ giao diện Windows sẽ
     * vẽ đè nền trắng của hệ thống và màu nền của nút sẽ không hiện ra.
     */
    private JButton taoNutMon(Mon mon) {
        String hienThi = "<html><center><b>" + mon.getTenMon() + "</b><br/>"
                + FormatUtil.formatTien(mon.getDonGia()) + "<br/>"
                + "<small>Còn: " + mon.getTonKho() + "</small></center></html>";

        JButton nut = new JButton(hienThi);
        nut.setFont(new Font("Arial", Font.PLAIN, 12));
        nut.setFocusPainted(false);
        // Lưu ý: phải gọi setOpaque(true) SAU setContentAreaFilled(false),
        // vì setContentAreaFilled(false) tự đặt lại opaque thành false.
        nut.setContentAreaFilled(false);
        nut.setOpaque(true);
        nut.setPreferredSize(new Dimension(150, 90));
        nut.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        boolean hetHang = mon.getTonKho() <= 0;
        if (hetHang) {
            nut.setBackground(UiUtil.lamNhat(new Color(0xC6, 0x28, 0x28), 0.85));
            nut.setForeground(Color.DARK_GRAY);
            nut.setEnabled(false);
            nut.setToolTipText("Món đã hết hàng");
            nut.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(0xC6, 0x28, 0x28), 2),
                    BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        } else {
            nut.setBackground(UiUtil.lamNhat(new Color(0x8D, 0x6E, 0x63), 0.80));
            nut.setForeground(Color.BLACK);
            nut.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(0x8D, 0x6E, 0x63), 2),
                    BorderFactory.createEmptyBorder(5, 10, 5, 10)));
            nut.addActionListener(e -> themMonVaoGio(mon));
        }

        return nut;
    }

    /** Tìm món theo từ khóa, vẽ lại toàn bộ các tab */
    private void timKiemMon() {
        veLaiTatCaTab(txtTimKiem.getText().trim());
    }

    /** Vẽ lại toàn bộ các tab theo từ khóa, rỗng nghĩa là vẽ tất cả */
    private void veLaiTatCaTab() {
        veLaiTatCaTab("");
    }

    /** Vẽ lại toàn bộ các tab theo từ khóa */
    private void veLaiTatCaTab(String tuKhoa) {
        for (java.util.Map.Entry<NhomMon, JPanel> entry : dsKhungMon.entrySet()) {
            veDanhSachMon(entry.getValue(), entry.getKey(), tuKhoa);
        }
    }

    // ==================== Nghiệp vụ giỏ hàng ====================

    /** Thêm món vào giỏ của bàn hiện tại */
    private void themMonVaoGio(Mon mon) {
        if (!coBanHienTai()) {
            return;
        }

        HoaDon hd = orderService.layHoaDonCuaBan(maBanHienTai);
        if (hd == null) {
            JOptionPane.showMessageDialog(this,
                    "Không tìm thấy hóa đơn của bàn " + maBanHienTai + ".",
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Kiểm tra tồn kho
        int soLuongTrongGio = 0;
        if (hd.getDsChiTiet() != null) {
            for (ChiTietHoaDon ct : hd.getDsChiTiet()) {
                if (ct.getMaMon().equalsIgnoreCase(mon.getMaMon())) {
                    soLuongTrongGio = ct.getSoLuong();
                    break;
                }
            }
        }
        if (soLuongTrongGio + 1 > mon.getTonKho()) {
            JOptionPane.showMessageDialog(this,
                    "Không đủ hàng để thêm món \"" + mon.getTenMon() + "\".\n"
                            + "Tồn kho hiện có: " + mon.getTonKho(),
                    "Hết hàng", JOptionPane.WARNING_MESSAGE);
            return;
        }

        orderService.themMon(hd.getMaHD(), mon, 1);
        capNhatGio();
    }

    /** Tăng số lượng của dòng đang chọn trong giỏ */
    private void tangSoLuong() {
        ChiTietHoaDon ct = layChiTietDangChon();
        if (ct == null) {
            return;
        }

        Mon mon = monRepo.findByMa(ct.getMaMon());
        if (mon == null) {
            JOptionPane.showMessageDialog(this,
                    "Không tìm thấy món \"" + ct.getTenMon() + "\" trong thực đơn.",
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (mon.getTonKho() <= ct.getSoLuong()) {
            JOptionPane.showMessageDialog(this,
                    "Món \"" + ct.getTenMon() + "\" đã đạt tối đa tồn kho (" + mon.getTonKho() + ").",
                    "Hết hàng", JOptionPane.WARNING_MESSAGE);
            return;
        }

        orderService.themMon(ct.getMaHD(), mon, 1);
        capNhatGio();
    }

    /** Giảm số lượng của dòng đang chọn trong giỏ */
    private void giamSoLuong() {
        ChiTietHoaDon ct = layChiTietDangChon();
        if (ct == null) {
            return;
        }

        Mon mon = monRepo.findByMa(ct.getMaMon());
        if (mon == null) {
            return;
        }
        orderService.themMon(ct.getMaHD(), mon, -1);
        capNhatGio();
    }

    /** Xóa một dòng khỏi giỏ */
    private void xoaDongGio() {
        ChiTietHoaDon ct = layChiTietDangChon();
        if (ct == null) {
            return;
        }

        int luaChon = JOptionPane.showConfirmDialog(this,
                "Xóa \"" + ct.getTenMon() + "\" khỏi giỏ?",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (luaChon != JOptionPane.YES_OPTION) {
            return;
        }

        // Giảm hết số lượng để dòng bị loại khỏi hóa đơn
        Mon mon = monRepo.findByMa(ct.getMaMon());
        if (mon != null) {
            orderService.themMon(ct.getMaHD(), mon, -ct.getSoLuong());
        }
        capNhatGio();
    }

    /** Xóa toàn bộ món trong giỏ */
    private void xoaHetGio() {
        HoaDon hd = layHoaDonHienTai();
        if (hd == null || hd.getDsChiTiet() == null || hd.getDsChiTiet().isEmpty()) {
            return;
        }

        int luaChon = JOptionPane.showConfirmDialog(this,
                "Xóa toàn bộ món trong giỏ của bàn " + maBanHienTai + "?",
                "Xác nhận xóa hết", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (luaChon != JOptionPane.YES_OPTION) {
            return;
        }

        for (ChiTietHoaDon ct : new java.util.ArrayList<>(hd.getDsChiTiet())) {
            Mon mon = monRepo.findByMa(ct.getMaMon());
            if (mon != null) {
                orderService.themMon(ct.getMaHD(), mon, -ct.getSoLuong());
            }
        }
        capNhatGio();
    }

    /**
     * Lấy chi tiết hóa đơn của dòng đang chọn trong bảng giỏ.
     *
     * @return chi tiết hóa đơn, null nếu chưa chọn dòng nào
     */
    private ChiTietHoaDon layChiTietDangChon() {
        int dong = bangGio.getSelectedRow();
        if (dong < 0) {
            JOptionPane.showMessageDialog(this,
                    "Hãy chọn một dòng trong giỏ hàng.",
                    "Chưa chọn món", JOptionPane.INFORMATION_MESSAGE);
            return null;
        }

        int dongModel = bangGio.convertRowIndexToModel(dong);
        if (dongModel < 0 || dongModel >= modelGio.getRowCount()) {
            return null;
        }

        HoaDon hd = layHoaDonHienTai();
        if (hd == null || hd.getDsChiTiet() == null || dongModel >= hd.getDsChiTiet().size()) {
            return null;
        }

        return hd.getDsChiTiet().get(dongModel);
    }

    /** Hóa đơn của bàn hiện tại, null nếu chưa chọn bàn */
    private HoaDon layHoaDonHienTai() {
        if (maBanHienTai == null) {
            return null;
        }
        return orderService.layHoaDonCuaBan(maBanHienTai);
    }

    /** Kiểm tra đã chọn bàn hay chưa */
    private boolean coBanHienTai() {
        if (maBanHienTai == null) {
            JOptionPane.showMessageDialog(this,
                    "Hãy chọn một bàn ở màn hình Sơ đồ bàn trước khi gọi món.",
                    "Chưa chọn bàn", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }

    // ==================== Thanh toán ====================

    /** Chuyển hóa đơn sang trạng thái chờ thanh toán */
    private void yeuCauTinhTien() {
        if (!coBanHienTai()) {
            return;
        }

        HoaDon hd = layHoaDonHienTai();
        if (hd == null) {
            return;
        }
        if (hd.getDsChiTiet() == null || hd.getDsChiTiet().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Giỏ hàng đang trống, không có gì để tính tiền.",
                    "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        orderService.yeuCauTinhTien(hd.getMaHD());
        capNhatGio();

        JOptionPane.showMessageDialog(this,
                "Bàn " + maBanHienTai + " đã chuyển sang chờ thanh toán.\n"
                        + "Tổng tiền: " + FormatUtil.formatTien(hd.getTongTien()),
                "Yêu cầu tính tiền", JOptionPane.INFORMATION_MESSAGE);
    }

    /** Thanh toán hóa đơn của bàn hiện tại */
    private void thanhToan() {
        if (!coBanHienTai()) {
            return;
        }

        HoaDon hd = layHoaDonHienTai();
        if (hd == null) {
            return;
        }
        if (hd.getTrangThai() == TrangThaiHD.DA_THANH_TOAN) {
            JOptionPane.showMessageDialog(this,
                    "Hóa đơn này đã thanh toán rồi.",
                    "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        if (hd.getDsChiTiet() == null || hd.getDsChiTiet().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Giỏ hàng đang trống, không có gì để thanh toán.",
                    "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        PhuongThucTT pt = (PhuongThucTT) cboPhuongThuc.getSelectedItem();
        long tongTien = hd.getTongTien();

        // Hỏi khách đưa bao nhiêu nếu thanh toán tiền mặt
        long tienKhachDua = 0;
        if (pt == PhuongThucTT.TIEN_MAT) {
            String nhap = txtTienKhachDua.getText().trim();
            if (nhap.isEmpty()) {
                // Không nhập thì mặc định khách đưa đúng số tiền cần trả
                tienKhachDua = tongTien;
            } else {
                try {
                    tienKhachDua = Long.parseLong(nhap);
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(this,
                            "Số tiền khách đưa phải là số.",
                            "Sai định dạng", JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }

            if (tienKhachDua < tongTien) {
                JOptionPane.showMessageDialog(this,
                        "Tiền khách đưa (" + FormatUtil.formatTien(tienKhachDua)
                                + ") không đủ tổng tiền (" + FormatUtil.formatTien(tongTien) + ").",
                        "Không đủ tiền", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        try {
            orderService.thanhToan(hd.getMaHD(), pt, tienKhachDua);
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(),
                    "Lỗi thanh toán", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Lưu hóa đơn ra file để khách có bản in
        orderService.xuatHoaDon(hd.getMaHD());

        long tienThua = pt == PhuongThucTT.TIEN_MAT ? tienKhachDua - tongTien : 0;

        capNhatGio();
        txtTienKhachDua.setText("");
        lblTienThua.setText("0 đ");

        StringBuilder sb = new StringBuilder();
        sb.append("Thanh toán thành công!\n\n");
        sb.append("Mã hóa đơn : ").append(hd.getMaHD()).append("\n");
        sb.append("Bàn         : ").append(hd.getMaBan()).append("\n");
        sb.append("Tổng tiền  : ").append(FormatUtil.formatTien(tongTien)).append("\n");
        sb.append("Hình thức TT: ").append(pt.getTenHienThi()).append("\n");
        if (pt == PhuongThucTT.TIEN_MAT) {
            sb.append("Tiền khách đưa: ").append(FormatUtil.formatTien(tienKhachDua)).append("\n");
            sb.append("Tiền thừa      : ").append(FormatUtil.formatTien(tienThua)).append("\n");
        }
        sb.append("\nHóa đơn đã được lưu vào thư mục hoa_don/");

        JOptionPane.showMessageDialog(this, sb.toString(),
                "Thanh toán thành công", JOptionPane.INFORMATION_MESSAGE);

        // Bàn đã trống, quay về màn hình sơ đồ bàn
        maBanHienTai = null;
        capNhatGio();
    }

    /** Huỷ bàn và quay lại màn hình sơ đồ */
    private void huyBan() {
        if (!coBanHienTai()) {
            return;
        }

        int luaChon = JOptionPane.showConfirmDialog(this,
                "Huỷ bàn " + maBanHienTai + "?\nHóa đơn đang mở sẽ bị xoá.",
                "Xác nhận huỷ bàn", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (luaChon != JOptionPane.YES_OPTION) {
            return;
        }

        if (!orderService.huyBan(maBanHienTai)) {
            JOptionPane.showMessageDialog(this,
                    "Không huỷ được bàn này.\n"
                            + "Bàn đã yêu cầu tính tiền thì hãy thanh toán trước.",
                    "Không huỷ được", JOptionPane.WARNING_MESSAGE);
            return;
        }

        maBanHienTai = null;
        capNhatGio();
    }

    // ==================== Cập nhật hiển thị ====================

    /**
     * Nạp bàn cần gọi món.
     * <p>
     * Được màn hình sơ đồ bàn gọi khi người dùng bấm vào một bàn.
     *
     * @param maBan mã bàn
     */
    public void chonBan(String maBan) {
        this.maBanHienTai = maBan;
        capNhatGio();
    }

    /** Vẽ lại giỏ hàng và các nhãn liên quan */
    public void capNhatGio() {
        modelGio.setRowCount(0);

        if (maBanHienTai == null) {
            lblBan.setText("Chưa chọn bàn");
            lblHoaDon.setText(" ");
            lblTongTien.setText("0 đ");
            lblTienThua.setText("0 đ");
            return;
        }

        HoaDon hd = layHoaDonHienTai();
        if (hd == null) {
            lblBan.setText("Bàn " + maBanHienTai);
            lblHoaDon.setText("Hóa đơn không tồn tại");
            lblTongTien.setText("0 đ");
            return;
        }

        lblBan.setText("Bàn " + maBanHienTai);
        lblHoaDon.setText("Hóa đơn " + hd.getMaHD() + "  |  " + hd.getTrangThai().getTenHienThi());

        // Chỉ cho sửa giỏ khi hóa đơn còn đang phục vụ
        boolean choPhepSua = hd.getTrangThai() == TrangThaiHD.DANG_PHUC_VU;

        if (hd.getDsChiTiet() != null) {
            for (ChiTietHoaDon ct : hd.getDsChiTiet()) {
                modelGio.addRow(new Object[]{
                        ct.getMaMon(),
                        ct.getTenMon(),
                        String.valueOf(ct.getSoLuong()),
                        FormatUtil.formatTien(ct.getDonGia()),
                        FormatUtil.formatTien(ct.thanhTien())
                });
            }
        }

        lblTongTien.setText(FormatUtil.formatTien(hd.getTongTien()));

        if (!choPhepSua) {
            lblHoaDon.setText("Hóa đơn " + hd.getMaHD() + "  |  "
                    + hd.getTrangThai().getTenHienThi() + " (không thể sửa giỏ)");
        }
    }

    // ==================== Renderer bảng giỏ ====================

    /**
     * Renderer cho bảng giỏ hàng.
     * <p>
     * Cột tên món canh trái vì nội dung dài, các cột còn lại canh giữa.
     */
    private class GioRenderer extends DefaultTableCellRenderer {

        /** Cột tên món */
        private static final int COT_TEN_MON = 1;

        GioRenderer(int cot) {
            if (cot == COT_TEN_MON) {
                setHorizontalAlignment(SwingConstants.LEFT);
                setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 0));
            } else {
                setHorizontalAlignment(SwingConstants.CENTER);
            }
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            Component thanh = super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column);

            // Dòng đang vẽ, luôn hợp lệ
            int dongModel = table.convertRowIndexToModel(row);
            if (!isSelected && dongModel >= 0 && dongModel < modelGio.getRowCount()) {
                Object sl = modelGio.getValueAt(dongModel, 2);
                // Dòng số lượng bằng 1 thì tô nền nhạt cho dễ nhận biết
                if (sl != null && "1".equals(sl.toString())) {
                    setBackground(UiUtil.lamNhat(new Color(0x2E, 0x7D, 0x32), 0.90));
                } else {
                    setBackground(table.getBackground());
                }
            }

            return thanh;
        }
    }
}
