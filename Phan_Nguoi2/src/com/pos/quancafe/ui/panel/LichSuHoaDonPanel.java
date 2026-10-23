package com.pos.quancafe.ui.panel;

import com.pos.quancafe.data.HoaDonRepo;
import com.pos.quancafe.model.HoaDon;
import com.pos.quancafe.service.ThongKeService;
import com.pos.quancafe.ui.UiUtil;
import com.pos.quancafe.util.FormatUtil;
import com.pos.quancafe.util.HoaDonPrinter;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.File;
import java.time.LocalDate;
import java.util.List;

/**
 * Màn hình lịch sử hóa đơn và thống kê doanh thu.
 * Phần này thuộc công việc của Người 2.
 */
public class LichSuHoaDonPanel extends JPanel {

    private final HoaDonRepo hoaDonRepo = new HoaDonRepo();
    private final ThongKeService thongKeService = new ThongKeService(hoaDonRepo);

    // Bảng lịch sử
    private final DefaultTableModel modelBang = new DefaultTableModel(
            new Object[]{"Mã hóa đơn", "Bàn", "Giờ thanh toán", "Tổng tiền", "Hình thức TT", "Tiền thừa"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable bang = new JTable(modelBang);

    private final JTextField txtTimKiem = new JTextField(18);

    // Hai thẻ thống kê
    private final JLabel lblDoanhThu = new JLabel("0 đ");
    private final JLabel lblSoHoaDon = new JLabel("0");
    private final JLabel lblTienTrungBinh = new JLabel("0 đ");

    public LichSuHoaDonPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        add(taoTieuDe(), BorderLayout.NORTH);
        add(taoBang(), BorderLayout.CENTER);
        add(taoDuoi(), BorderLayout.SOUTH);

        capNhatThongKe();
        capNhatBang();
    }

    /** Tiêu đề phía trên */
    private JPanel taoTieuDe() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        JLabel lblTieuDe = new JLabel("LỊCH SỬ HÓA ĐƠN & DOANH THU", SwingConstants.CENTER);
        lblTieuDe.setFont(new Font("Arial", Font.BOLD, 20));
        lblTieuDe.setForeground(Color.BLACK);

        panel.add(lblTieuDe, BorderLayout.CENTER);
        return panel;
    }

    /** Bảng lịch sử ở giữa */
    private JScrollPane taoBang() {
        bang.setRowHeight(26);
        bang.setFont(new Font("Arial", Font.PLAIN, 13));
        bang.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        bang.getTableHeader().setBackground(new Color(0xF5, 0xDE, 0xB3));

        bang.getColumnModel().getColumn(0).setPreferredWidth(150);
        bang.getColumnModel().getColumn(1).setPreferredWidth(60);
        bang.getColumnModel().getColumn(2).setPreferredWidth(130);
        bang.getColumnModel().getColumn(3).setPreferredWidth(110);
        bang.getColumnModel().getColumn(4).setPreferredWidth(110);
        bang.getColumnModel().getColumn(5).setPreferredWidth(110);

        // Quy tắc căn lề: toàn bộ canh giữa cho dễ nhìn,
        // bảng này không có cột mô tả nên không cần canh trái
        for (int cot = 0; cot < modelBang.getColumnCount(); cot++) {
            bang.getColumnModel().getColumn(cot).setCellRenderer(canGiua());
        }

        // Bấm đúp vào dòng để xem chi tiết hóa đơn
        bang.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && bang.getSelectedRow() >= 0) {
                    xemChiTietHoaDon();
                }
            }
        });

        return new JScrollPane(bang);
    }

    /** Phần dưới gồm thẻ thống kê và nút thao tác */
    private JPanel taoDuoi() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setOpaque(false);

        panel.add(taoTheThongKe(), BorderLayout.NORTH);
        panel.add(taoHangCongCu(), BorderLayout.SOUTH);

        return panel;
    }

    /** Ba thẻ hiển thị số liệu kinh doanh */
    private JPanel taoTheThongKe() {
        JPanel panel = new JPanel(new GridLayout(1, 3, 10, 0));
        panel.setOpaque(false);

        panel.add(taoThe("DOANH THU HÔM NAY", lblDoanhThu, new Color(0x2E, 0x7D, 0x32)));
        panel.add(taoThe("SỐ HÓA ĐƠN HÔM NAY", lblSoHoaDon, new Color(0x15, 0x65, 0xC0)));
        panel.add(taoThe("TIỀN TRUNG BÌNH", lblTienTrungBinh, new Color(0xEF, 0x6C, 0x00)));

        return panel;
    }

    /**
     * Tạo một thẻ thống kê gồm tiêu đề và giá trị.
     * <p>
     * Nền thẻ được pha nhạt từ màu chủ đạo, chữ đen để dễ đọc trên mọi hệ giao diện.
     *
     * @param tieuDe  tiêu đề thẻ
     * @param lblGiaTri nhãn chứa giá trị số
     * @param mauChu  màu chủ đạo, dùng cho viền và tiêu đề
     */
    private JPanel taoThe(String tieuDe, JLabel lblGiaTri, Color mauChu) {
        JPanel the = new JPanel(new BorderLayout(5, 5));
        the.setBackground(UiUtil.lamNhat(mauChu, 0.88));
        the.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(mauChu, 2),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)));

        JLabel lblTieuDe = new JLabel(tieuDe, SwingConstants.CENTER);
        lblTieuDe.setFont(new Font("Arial", Font.BOLD, 13));
        lblTieuDe.setForeground(Color.BLACK);

        lblGiaTri.setHorizontalAlignment(SwingConstants.CENTER);
        lblGiaTri.setFont(new Font("Arial", Font.BOLD, 24));
        lblGiaTri.setForeground(Color.BLACK);

        the.add(lblTieuDe, BorderLayout.NORTH);
        the.add(lblGiaTri, BorderLayout.CENTER);

        return the;
    }

    /** Hàng nút thao tác và ô tìm kiếm */
    private JPanel taoHangCongCu() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        panel.setOpaque(false);

        panel.add(taoNut("Tải lại", new Color(0x54, 0x54, 0x54), () -> {
            hoaDonRepo.napDuLieu();
            capNhatThongKe();
            capNhatBang();
        }));

        panel.add(taoNut("Xem hôm nay", new Color(0x2E, 0x7D, 0x32), () -> {
            hienBangTheoDanhSach(thongKeService.hoaDonTrongNgay(LocalDate.now()));
        }));

        panel.add(new JLabel("Tìm hóa đơn hoặc bàn:"));
        panel.add(txtTimKiem);
        panel.add(taoNut("Tìm", new Color(0xEF, 0x6C, 0x00), () -> {
            List<HoaDon> ds = hoaDonRepo.timTheoMaHoacBan(txtTimKiem.getText().trim());
            hienBangTheoDanhSach(ds);
            if (ds.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Không tìm thấy hóa đơn nào phù hợp.",
                        "Không có kết quả", JOptionPane.INFORMATION_MESSAGE);
            }
        }));

        panel.add(taoNut("Xem tất cả", new Color(0x75, 0x75, 0x75), () -> {
            txtTimKiem.setText("");
            capNhatBang();
        }));

        panel.add(Box.createHorizontalStrut(20));
        panel.add(taoNut("Xem chi tiết", new Color(0x6A, 0x1B, 0x9A), () -> xemChiTietHoaDon()));

        return panel;
    }

    // ==================== Cập nhật hiển thị ====================

    /** Cập nhật ba thẻ số liệu của hôm nay */
    private void capNhatThongKe() {
        LocalDate homNay = LocalDate.now();

        lblDoanhThu.setText(FormatUtil.formatTien(thongKeService.doanhThuTrongNgay(homNay)));
        lblSoHoaDon.setText(String.valueOf(thongKeService.soHoaDonTrongNgay(homNay)));
        lblTienTrungBinh.setText(FormatUtil.formatTien(thongKeService.tienTrungBinhMoiHoaDon(homNay)));
    }

    /** Nạp toàn bộ lịch sử hóa đơn vào bảng */
    private void capNhatBang() {
        hienBangTheoDanhSach(hoaDonRepo.findDaThanhToan());
    }

    /** Hiển thị một danh sách hóa đơn lên bảng, mới nhất lên đầu */
    private void hienBangTheoDanhSach(List<HoaDon> dsHoaDon) {
        modelBang.setRowCount(0);

        if (dsHoaDon == null) {
            return;
        }

        // Sao chép rồi sắp xếp mới nhất lên đầu
        List<HoaDon> dsSapXep = new java.util.ArrayList<>(dsHoaDon);
        dsSapXep.sort((a, b) -> {
            if (a.getNgayDong() == null || b.getNgayDong() == null) {
                return 0;
            }
            return b.getNgayDong().compareTo(a.getNgayDong());
        });

        for (HoaDon hd : dsSapXep) {
            modelBang.addRow(new Object[]{
                    hd.getMaHD(),
                    hd.getMaBan(),
                    FormatUtil.hienThiThoiGian(hd.getNgayDong()),
                    FormatUtil.formatTien(hd.getTongTien()),
                    hd.getPhuongThucTT() == null ? "" : hd.getPhuongThucTT().getTenHienThi(),
                    hd.getPhuongThucTT() == com.pos.quancafe.model.PhuongThucTT.TIEN_MAT
                            ? FormatUtil.formatTien(hd.getTienThua()) : "-"
            });
        }
    }

    /**
     * Xem chi tiết hóa đơn đang chọn.
     * Dùng lại đúng hàm tạo hóa đơn của HoaDonPrinter để nội dung nhất quán với bản in.
     */
    private void xemChiTietHoaDon() {
        int dong = bang.getSelectedRow();
        if (dong < 0) {
            JOptionPane.showMessageDialog(this,
                    "Hãy chọn một hóa đơn trong bảng để xem chi tiết.",
                    "Chưa chọn hóa đơn", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int dongModel = bang.convertRowIndexToModel(dong);
        if (dongModel < 0 || dongModel >= modelBang.getRowCount()) {
            return;
        }

        Object giaTri = modelBang.getValueAt(dongModel, 0);
        if (giaTri == null) {
            return;
        }

        String maHD = giaTri.toString();
        HoaDon hd = hoaDonRepo.findByMa(maHD);

        if (hd == null) {
            JOptionPane.showMessageDialog(this,
                    "Không tìm thấy hóa đơn \"" + maHD + "\" trong dữ liệu.",
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String noiDung = HoaDonPrinter.taoNoiDungTuChiTiet(
                hd.getMaHD(),
                hd.getMaBan(),
                hd.getNgayMo(),
                hd.getNgayDong(),
                hd.getDsChiTiet(),
                hd.getTongTien(),
                hd.getPhuongThucTT() == null ? "" : hd.getPhuongThucTT().getTenHienThi(),
                hd.getTienKhachDua(),
                hd.getTienThua()
        );

        hienHopThoaiXemHoaDon(hd.getMaHD(), noiDung);
    }

    /** Hiện hộp thoại xem hóa đơn */
    private void hienHopThoaiXemHoaDon(String maHD, String noiDung) {
        JDialog hopThoai = new JDialog((javax.swing.JFrame) null, "Hóa đơn " + maHD, true);
        hopThoai.setLayout(new BorderLayout(10, 10));

        JTextArea vungHienThi = new JTextArea(noiDung, 30, 58);
        vungHienThi.setEditable(false);
        // Font đơn cách: mỗi ký tự chiếm đúng 1 cột nên khung hóa đơn thẳng nhau
        vungHienThi.setFont(new Font("Monospaced", Font.PLAIN, 13));
        vungHienThi.setCaretPosition(0);
        // Nền trắng, chữ đen cho dễ đọc và dễ sao chép
        vungHienThi.setBackground(Color.WHITE);
        vungHienThi.setForeground(Color.BLACK);
        vungHienThi.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        hopThoai.add(new JScrollPane(vungHienThi), BorderLayout.CENTER);

        JPanel panelNut = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelNut.add(taoNut("Lưu ra file", new Color(0x2E, 0x7D, 0x32), () -> {
            File file = HoaDonPrinter.taoFileHoaDon(maHD);
            if (HoaDonPrinter.xuatFileTxt(noiDung, file)) {
                JOptionPane.showMessageDialog(hopThoai,
                        "Đã lưu hóa đơn ra file:\n" + file.getPath(),
                        "Lưu thành công", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(hopThoai,
                        "Không lưu được file hóa đơn.",
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }));
        panelNut.add(taoNut("Đóng", new Color(0x54, 0x54, 0x54), () -> hopThoai.dispose()));

        hopThoai.add(panelNut, BorderLayout.SOUTH);
        hopThoai.pack();
        hopThoai.setLocationRelativeTo(null);
        hopThoai.setVisible(true);
    }

    // ==================== Hàm phụ ====================

    /**
     * Tạo nút có màu nền nhạt và chữ đen.
     * Dùng chung với QuanLyMonPanel qua lớp UiUtil.
     */
    private JButton taoNut(String chu, Color mauNen, XuLyNut xuLy) {
        return UiUtil.taoNut(chu, mauNen, xuLy::xuLy);
    }

    /** Canh giữa cho các cột của bảng */
    private DefaultTableCellRenderer canGiua() {
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
        renderer.setHorizontalAlignment(SwingConstants.CENTER);
        return renderer;
    }

    /** Giao diện dùng để gán hành động cho nút */
    @FunctionalInterface
    private interface XuLyNut {
        void xuLy();
    }
}