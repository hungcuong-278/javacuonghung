package com.pos.quancafe.ui;

import com.pos.quancafe.util.HoaDonPrinter;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Window;
import java.io.File;

/**
 * Hộp thoại hiển thị nội dung hóa đơn dạng chữ.
 * <p>
 * Dùng chung cho màn hình lịch sử hóa đơn và màn hình sơ đồ bàn, nội dung
 * hiển thị giống hệt bản in ra giấy.
 */
public class XemHoaDonDialog extends JDialog {

    /**
     * Mở hộp thoại xem hóa đơn.
     *
     * @param cha     thành phần cha, dùng để xác định cửa sổ chứa hộp thoại
     * @param maHD    mã hóa đơn
     * @param noiDung nội dung hóa đơn đã dựng sẵn
     */
    public XemHoaDonDialog(Component cha, String maHD, String noiDung) {
        super(timCuaSo(cha), "Hóa đơn " + maHD, ModalityType.APPLICATION_MODAL);
        setLayout(new BorderLayout(10, 10));

        // Vùng hiển thị nội dung hóa đơn
        JTextArea vungHienThi = new JTextArea(noiDung, 30, 58);
        vungHienThi.setEditable(false);
        // Font đơn cách: mỗi ký tự chiếm đúng 1 cột nên khung hóa đơn thẳng nhau
        vungHienThi.setFont(new Font("Monospaced", Font.PLAIN, 13));
        vungHienThi.setCaretPosition(0);
        vungHienThi.setBackground(Color.WHITE);
        vungHienThi.setForeground(Color.BLACK);
        vungHienThi.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        add(new JScrollPane(vungHienThi), BorderLayout.CENTER);

        // Hàng nút thao tác
        JPanel panelNut = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));

        JPanel panelTrai = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        panelTrai.add(UiUtil.taoNut("Lưu ra file", new Color(0x2E, 0x7D, 0x32), () -> luuRaFile(maHD, noiDung)));
        panelNut.add(panelTrai, BorderLayout.WEST);

        panelNut.add(UiUtil.taoNut("Đóng", new Color(0x54, 0x54, 0x54), () -> dispose()));
        add(panelNut, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(null);
    }

    /**
     * Tìm cửa sổ chứa một thành phần.
     * <p>
     * Hộp thoại cần một cửa sổ cha, nhưng thành phần gọi tới có thể là
     * JPanel nằm sâu trong cây component, nên phải đi ngược lên từ gốc.
     *
     * @param component thành phần cha
     * @return cửa sổ chứa nó, null nếu không tìm thấy
     */
    private static Window timCuaSo(Component component) {
        if (component == null) {
            return null;
        }
        if (component instanceof Window) {
            return (Window) component;
        }
        return timCuaSo(component.getParent());
    }

    /**
     * Lưu hóa đơn ra file txt.
     * <p>
     * Mặc định lưu vào thư mục hoa_don/, người dùng có thể chọn chỗ khác.
     */
    private void luuRaFile(String maHD, String noiDung) {
        JFileChooser chooser = new JFileChooser(HoaDonPrinter.taoFileHoaDon(maHD));
        chooser.setDialogTitle("Lưu hóa đơn " + maHD);
        chooser.setSelectedFile(new File("hoa_don", maHD + ".txt"));

        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File file = chooser.getSelectedFile();
        if (file == null) {
            return;
        }

        if (HoaDonPrinter.xuatFileTxt(noiDung, file)) {
            JOptionPane.showMessageDialog(this,
                    "Đã lưu hóa đơn ra file:\n" + file.getPath(),
                    "Lưu thành công", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this,
                    "Không lưu được file hóa đơn.",
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
}
