package com.pos.quancafe.ui.panel;

import com.pos.quancafe.data.MonRepo;
import com.pos.quancafe.model.Mon;
import com.pos.quancafe.model.NhomMon;
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
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;

/**
 * Màn hình quản lý món: thêm, sửa, xóa, tìm kiếm trong thực đơn.
 * Phần này thuộc công việc của Người 2.
 */
public class QuanLyMonPanel extends JPanel {

    private final MonRepo monRepo = new MonRepo();

    // Bảng danh sách món
    private final DefaultTableModel modelBang = new DefaultTableModel(
            new Object[]{"Mã", "Tên món", "Nhóm", "Đơn giá", "Tồn kho", "Mô tả"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable bang = new JTable(modelBang);

    // Ô nhập liệu
    private final JTextField txtMaMon = new JTextField(8);
    private final JTextField txtTenMon = new JTextField(22);
    private final JComboBox<NhomMon> cboNhom = new JComboBox<>(NhomMon.values());
    private final JTextField txtDonGia = new JTextField(10);
    private final JTextField txtTonKho = new JTextField(8);
    private final JTextField txtMoTa = new JTextField(22);

    // Ô tìm kiếm
    private final JTextField txtTimKiem = new JTextField(20);

    // Thông báo
    private final JLabel lblThongBao = new JLabel(" ");

    /** Cờ đánh dấu đang sửa món cũ hay đang thêm món mới */
    private boolean dangSua = false;

    public QuanLyMonPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        add(taoTieuDe(), BorderLayout.NORTH);
        add(taoBang(), BorderLayout.CENTER);
        add(taoDuoi(), BorderLayout.SOUTH);

        capNhatBang();
        lamMoiFormThem();
    }

    /** Phần tiêu đề phía trên */
    private JPanel taoTieuDe() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        JLabel lblTieuDe = new JLabel("QUẢN LÝ THỰC ĐƠN", SwingConstants.CENTER);
        lblTieuDe.setFont(new Font("Arial", Font.BOLD, 20));
        lblTieuDe.setForeground(Color.BLACK);

        panel.add(lblTieuDe, BorderLayout.CENTER);
        return panel;
    }

    /** Bảng danh sách món ở giữa */
    private JScrollPane taoBang() {
        bang.setRowHeight(26);
        bang.setFont(new Font("Arial", Font.PLAIN, 13));
        bang.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        bang.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        bang.getTableHeader().setBackground(new Color(0xF5, 0xDE, 0xB3));

        // Bề rộng cột: mô tả chừa nhiều chỗ vì nội dung dài
        bang.getColumnModel().getColumn(0).setPreferredWidth(80);
        bang.getColumnModel().getColumn(1).setPreferredWidth(190);
        bang.getColumnModel().getColumn(2).setPreferredWidth(100);
        bang.getColumnModel().getColumn(3).setPreferredWidth(100);
        bang.getColumnModel().getColumn(4).setPreferredWidth(90);
        bang.getColumnModel().getColumn(5).setPreferredWidth(320);

        // Gắn renderer cho từng cột: vừa canh chữ đúng chỗ, vừa tô đỏ dòng món hết hàng
        for (int cot = 0; cot < modelBang.getColumnCount(); cot++) {
            bang.getColumnModel().getColumn(cot).setCellRenderer(new MonRenderer(cot));
        }

        // Bấm vào dòng thì đổ dữ liệu lên form
        bang.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) {
                return;
            }

            int dong = bang.getSelectedRow();
            if (dong < 0) {
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

            Mon mon = monRepo.findByMa(giaTri.toString());
            if (mon != null) {
                dienFormTuMon(mon);
                dangSua = true;
                lblThongBao.setText("Đang sửa món: " + mon.getTenMon());
                lblThongBao.setForeground(new Color(0xB8, 0x86, 0x0B));
            }
        });

        return new JScrollPane(bang);
    }

    /** Phần dưới gồm form nhập liệu và các nút thao tác */
    private JPanel taoDuoi() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setOpaque(false);

        panel.add(taoFormNhap(), BorderLayout.CENTER);
        panel.add(taoHangNut(), BorderLayout.SOUTH);

        return panel;
    }

    /** Form nhập thông tin món */
    private JPanel taoFormNhap() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(0xA0, 0xA0, 0xA0)),
                "Thông tin món", TitledBorder.LEFT, TitledBorder.TOP));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 6, 4, 6);
        gbc.anchor = GridBagConstraints.WEST;

        // Hàng 1: Mã - Tên - Nhóm
        gbc.gridx = 0;
        gbc.gridy = 0;
        form.add(new JLabel("Mã món:"), gbc);
        gbc.gridx = 1;
        form.add(txtMaMon, gbc);

        gbc.gridx = 2;
        form.add(new JLabel("Tên món:"), gbc);
        gbc.gridx = 3;
        form.add(txtTenMon, gbc);

        gbc.gridx = 4;
        form.add(new JLabel("Nhóm:"), gbc);
        gbc.gridx = 5;
        form.add(cboNhom, gbc);

        // Hàng 2: Đơn giá - Tồn kho - Mô tả
        gbc.gridx = 0;
        gbc.gridy = 1;
        form.add(new JLabel("Đơn giá:"), gbc);
        gbc.gridx = 1;
        form.add(txtDonGia, gbc);

        gbc.gridx = 2;
        form.add(new JLabel("Tồn kho:"), gbc);
        gbc.gridx = 3;
        form.add(txtTonKho, gbc);

        gbc.gridx = 4;
        form.add(new JLabel("Mô tả:"), gbc);
        gbc.gridx = 5;
        gbc.gridwidth = 3;
        form.add(txtMoTa, gbc);

        return form;
    }

    /** Hàng nút thao tác và ô tìm kiếm */
    private JPanel taoHangNut() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        // Nút thao tác
        JPanel panelNut = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        panelNut.setOpaque(false);

        JButton btnThem = taoNut("Thêm món", new Color(0x2E, 0x7D, 0x32), () -> themMon());
        JButton btnSua = taoNut("Sửa món", new Color(0x15, 0x65, 0xC0), () -> suaMon());
        JButton btnXoa = taoNut("Xóa món", new Color(0xC6, 0x28, 0x28), () -> xoaMon());
        JButton btnLamMoi = taoNut("Làm mới form", new Color(0x54, 0x54, 0x54), () -> lamMoiFormThem());
        JButton btnTaiLai = taoNut("Tải lại từ file", new Color(0x6A, 0x1B, 0x9A), () -> {
            monRepo.napDuLieu();
            capNhatBang();
            hienThongBao("Đã tải lại dữ liệu từ file.", new Color(0x2E, 0x7D, 0x32));
        });

        panelNut.add(btnThem);
        panelNut.add(btnSua);
        panelNut.add(btnXoa);
        panelNut.add(btnLamMoi);
        panelNut.add(btnTaiLai);
        panelNut.add(Box.createHorizontalStrut(20));

        // Ô tìm kiếm
        panelNut.add(new JLabel("Tìm món:"));
        panelNut.add(txtTimKiem);
        panelNut.add(taoNut("Tìm", new Color(0xEF, 0x6C, 0x00), () -> timKiem()));
        panelNut.add(taoNut("Xem tất cả", new Color(0x75, 0x75, 0x75), () -> {
            txtTimKiem.setText("");
            capNhatBang();
        }));

        panel.add(panelNut, BorderLayout.CENTER);
        panel.add(lblThongBao, BorderLayout.EAST);

        return panel;
    }

    // ==================== Xử lý nghiệp vụ ====================

    /** Thêm mới một món vào thực đỡn */
    private void themMon() {
        // Kiểm tra dữ liệu nhập
        if (!kiemTraDuLieuNhap()) {
            return;
        }

        // Kiểm tra trùng tên
        if (monRepo.tonTaiTen(txtTenMon.getText().trim())) {
            hienThongBao("Tên món đã tồn tại trong thực đơn!", Color.RED);
            JOptionPane.showMessageDialog(this,
                    "Món \"" + txtTenMon.getText().trim() + "\" đã có trong thực đơn.\n" +
                            "Hãy nhập tên khác hoặc bấm Sửa để cập nhật món cũ.",
                    "Trùng tên món", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Mon mon = docMonTuForm();
        mon.setMaMon(monRepo.sinhMaMonMoi());

        monRepo.save(mon);
        capNhatBang();
        lamMoiFormThem();

        hienThongBao("Đã thêm món: " + mon.getTenMon(), new Color(0x2E, 0x7D, 0x32));
        JOptionPane.showMessageDialog(this,
                "Đã thêm món \"" + mon.getTenMon() + "\" với mã " + mon.getMaMon() + ".",
                "Thêm thành công", JOptionPane.INFORMATION_MESSAGE);
    }

    /** Cập nhật thông tin món đang chọn */
    private void suaMon() {
        if (!dangSua || txtMaMon.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Hãy bấm vào một dòng trong bảng để chọn món cần sửa.",
                    "Chưa chọn món", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!kiemTraDuLieuNhap()) {
            return;
        }

        String maCu = txtMaMon.getText().trim();
        Mon mon = docMonTuForm();
        mon.setMaMon(maCu);

        monRepo.save(mon);
        capNhatBang();
        lamMoiFormThem();

        hienThongBao("Đã cập nhật món: " + mon.getTenMon(), new Color(0x15, 0x65, 0xC0));
        JOptionPane.showMessageDialog(this,
                "Đã cập nhật món \"" + mon.getTenMon() + "\".",
                "Sửa thành công", JOptionPane.INFORMATION_MESSAGE);
    }

    /** Xóa món đang chọn, có hỏi xác nhận trước */
    private void xoaMon() {
        if (!dangSua || txtMaMon.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Hãy bấm vào một dòng trong bảng để chọn món cần xóa.",
                    "Chưa chọn món", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String maMon = txtMaMon.getText().trim();
        String tenMon = txtTenMon.getText().trim();

        int luaChon = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc muốn xóa món \"" + tenMon + "\" khỏi thực đỡn?\n" +
                        "Thao tác này không thể hoàn tác.",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (luaChon != JOptionPane.YES_OPTION) {
            return;
        }

        if (monRepo.delete(maMon)) {
            capNhatBang();
            lamMoiFormThem();
            hienThongBao("Đã xóa món: " + tenMon, new Color(0xC6, 0x28, 0x28));
            JOptionPane.showMessageDialog(this,
                    "Đã xóa món \"" + tenMon + "\" khỏi thực đơn.",
                    "Xóa thành công", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /** Lọc danh sách món theo từ khóa */
    private void timKiem() {
        String tuKhoa = txtTimKiem.getText().trim();
        List<Mon> dsKetQua = monRepo.timTheoTen(tuKhoa);

        if (dsKetQua.isEmpty()) {
            hienThongBao("Không tìm thấy món nào phù hợp với \"" + tuKhoa + "\"", Color.RED);
        } else {
            hienThongBao("Tìm thấy " + dsKetQua.size() + " món phù hợp.", new Color(0x2E, 0x7D, 0x32));
        }

        hienBangTheoDanhSach(dsKetQua);
    }

    /**
     * Kiểm tra dữ liệu nhập có hợp lệ không.
     * Trả về true nếu hợp lệ, false nếu có lỗi (đã hiện thông báo).
     */
    private boolean kiemTraDuLieuNhap() {
        String tenMon = txtTenMon.getText().trim();

        if (tenMon.isEmpty()) {
            hienThongBao("Tên món không được để trống!", Color.RED);
            JOptionPane.showMessageDialog(this, "Vui lòng nhập tên món.",
                    "Thiếu tên món", JOptionPane.WARNING_MESSAGE);
            txtTenMon.requestFocus();
            return false;
        }

        long donGia;
        try {
            donGia = Long.parseLong(txtDonGia.getText().trim());
        } catch (NumberFormatException e) {
            hienThongBao("Đơn giá phải là số!", Color.RED);
            JOptionPane.showMessageDialog(this, "Đơn giá phải là số, ví dụ: 25000",
                    "Sai định dạng đơn giá", JOptionPane.WARNING_MESSAGE);
            txtDonGia.requestFocus();
            return false;
        }

        if (donGia <= 0) {
            hienThongBao("Đơn giá phải lớn hơn 0!", Color.RED);
            JOptionPane.showMessageDialog(this, "Đơn giá phải lớn hơn 0.",
                    "Sai định dạng đơn giá", JOptionPane.WARNING_MESSAGE);
            txtDonGia.requestFocus();
            return false;
        }

        int tonKho;
        try {
            tonKho = Integer.parseInt(txtTonKho.getText().trim());
        } catch (NumberFormatException e) {
            hienThongBao("Tồn kho phải là số!", Color.RED);
            JOptionPane.showMessageDialog(this, "Tồn kho phải là số, ví dụ: 100",
                    "Sai định dạng tồn kho", JOptionPane.WARNING_MESSAGE);
            txtTonKho.requestFocus();
            return false;
        }

        if (tonKho < 0) {
            hienThongBao("Tồn kho không được âm!", Color.RED);
            JOptionPane.showMessageDialog(this, "Tồn kho không được nhỏ hơn 0.",
                    "Sai định dạng tồn kho", JOptionPane.WARNING_MESSAGE);
            txtTonKho.requestFocus();
            return false;
        }

        return true;
    }

    /** Đọc dữ liệu từ các ô nhập thành đối tượng Mon */
    private Mon docMonTuForm() {
        return new Mon(
                txtMaMon.getText().trim(),
                txtTenMon.getText().trim(),
                (NhomMon) cboNhom.getSelectedItem(),
                Long.parseLong(txtDonGia.getText().trim()),
                Integer.parseInt(txtTonKho.getText().trim()),
                txtMoTa.getText().trim()
        );
    }

    /** Đổ dữ liệu của một món lên form */
    private void dienFormTuMon(Mon mon) {
        txtMaMon.setText(mon.getMaMon());
        txtTenMon.setText(mon.getTenMon());
        cboNhom.setSelectedItem(mon.getNhomMon());
        txtDonGia.setText(String.valueOf(mon.getDonGia()));
        txtTonKho.setText(String.valueOf(mon.getTonKho()));
        txtMoTa.setText(mon.getMoTa() == null ? "" : mon.getMoTa());
    }

    /** Xóa sạch form và chuẩn bị thêm món mới */
    private void lamMoiFormThem() {
        dangSua = false;
        txtMaMon.setText(monRepo.sinhMaMonMoi());
        txtMaMon.setEditable(false);
        txtMaMon.setForeground(Color.GRAY);
        txtTenMon.setText("");
        cboNhom.setSelectedIndex(0);
        txtDonGia.setText("");
        txtTonKho.setText("0");
        txtMoTa.setText("");
        txtTenMon.requestFocus();
        lblThongBao.setText("Nhập thông tin món mới rồi bấm Thêm.");
        lblThongBao.setForeground(Color.DARK_GRAY);
    }

    // ==================== Cập nhật bảng ====================

    /** Nạp lại toàn bộ danh sách món vào bảng */
    private void capNhatBang() {
        hienBangTheoDanhSach(monRepo.findAll());
    }

    /** Hiển thị một danh sách món bất kỳ lên bảng */
    private void hienBangTheoDanhSach(List<Mon> dsMon) {
        modelBang.setRowCount(0);
        for (Mon mon : dsMon) {
            modelBang.addRow(new Object[]{
                    mon.getMaMon(),
                    mon.getTenMon(),
                    mon.getNhomMon().getTenHienThi(),
                    FormatUtil.formatTien(mon.getDonGia()),
                    String.valueOf(mon.getTonKho()),
                    mon.getMoTa() == null ? "" : mon.getMoTa()
            });
        }
    }

    // ==================== Hàm phụ ====================

    /**
     * Tạo nút có màu nền nhạt và chữ đen.
     * Dùng chung với LichSuHoaDonPanel qua lớp UiUtil.
     */
    private JButton taoNut(String chu, Color mauNen, XuLyNut xuLy) {
        return UiUtil.taoNut(chu, mauNen, xuLy::xuLy);
    }

    /**
     * Renderer cho bảng món.
     * <p>
     * Ghi chú quan trọng: hàm này cố tình override
     * {@code getTableCellRendererComponent} chứ không override {@code setValue},
     * vì {@code setValue} không cho biết đang vẽ dòng nào. Nếu dùng
     * {@code bang.getSelectedRow()} để tra dữ liệu thì khi chưa chọn dòng nào
     * hàm này trả về -1 và JTable sẽ ném ArrayIndexOutOfBoundsException
     * ngay trong lúc vẽ bảng.
     * <p>
     * Quy tắc căn lề: mặc định canh giữa cho dễ nhìn, riêng cột mô tả
     * (cột cuối) canh trái vì nội dung dài và cần đọc như đoạn văn.
     */
    private class MonRenderer extends DefaultTableCellRenderer {

        /** Cột mô tả, nội dung dài nên canh trái */
        private static final int COT_MO_TA = 5;

        MonRenderer(int cot) {
            if (cot == COT_MO_TA) {
                // Mô tả: canh trái, cách mép một chút cho dễ đọc
                setHorizontalAlignment(SwingConstants.LEFT);
                setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 0));
            } else {
                // Tất cả cột còn lại canh giữa
                setHorizontalAlignment(SwingConstants.CENTER);
            }
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            Component thanh = super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column);

            // Đây là dòng đang được vẽ, luôn hợp lệ
            int dongModel = table.convertRowIndexToModel(row);

            boolean hetHang = false;
            if (dongModel >= 0 && dongModel < modelBang.getRowCount()) {
                Object tonKho = modelBang.getValueAt(dongModel, 4);
                hetHang = tonKho != null && "0".equals(tonKho.toString());
            }

            if (!isSelected) {
                // Món hết hàng thì chữ đỏ, còn lại chữ đen
                setForeground(hetHang ? Color.RED : Color.BLACK);
            }

            return thanh;
        }
    }

    /** Hiện thông báo nhỏ ở góc phải */
    private void hienThongBao(String noiDung, Color mau) {
        lblThongBao.setText(noiDung);
        lblThongBao.setForeground(mau);
    }

    /** Giao diện dùng để gán hành động cho nút, tránh dùng nhiều lớp vô danh */
    @FunctionalInterface
    private interface XuLyNut {
        void xuLy();
    }
}