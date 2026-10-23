import com.pos.quancafe.ui.panel.LichSuHoaDonPanel;
import com.pos.quancafe.ui.panel.QuanLyMonPanel;

import javax.swing.JFrame;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 * Test GUI: tao panel that roi ve bang de kiem tra loi ArrayIndexOutOfBounds.
 * Khong can mo cua so that su, chi can ve vao anh trong bo dem.
 */
public class KiemTraGiaoDien {

    public static void main(String[] args) throws Exception {
        System.out.println("=== KIEM TRA GIAO DIEN (ve bang) ===\n");

        final boolean[] loi = {false};
        final String[] chiTietLoi = {""};

        SwingUtilities.invokeAndWait(() -> {
            try {
                // --- Panel quan ly mon ---
                QuanLyMonPanel pMon = new QuanLyMonPanel();
                pMon.setSize(1000, 650);
                pMon.doLayout();

                // Lay bang trong panel bang vi tri trong cay component
                JTable bangMon = timBang(pMon);
                System.out.println("[1] Tim thay bang trong QuanLyMonPanel: " + (bangMon != null));

                if (bangMon != null) {
                    System.out.println("    So dong: " + bangMon.getRowCount()
                            + " | So cot: " + bangMon.getColumnCount());

                    // Ve bang - day la cho bi loi xay ra
                    veBang(bangMon);
                    System.out.println("    [OK] Ve bang khong loi (chua chon dong nao)");

                    // Chon dong dau tien roi ve lai
                    if (bangMon.getRowCount() > 0) {
                        bangMon.setRowSelectionInterval(0, 0);
                        veBang(bangMon);
                        System.out.println("    [OK] Ve bang sau khi chon dong 0");

                        // Chon dong cuoi
                        bangMon.setRowSelectionInterval(bangMon.getRowCount() - 1, bangMon.getRowCount() - 1);
                        veBang(bangMon);
                        System.out.println("    [OK] Ve bang sau khi chon dong cuoi");

                        // Bo chon lai
                        bangMon.clearSelection();
                        veBang(bangMon);
                        System.out.println("    [OK] Ve bang sau khi bo chon");
                    }
                }

                // --- Panel lich su hoa don ---
                LichSuHoaDonPanel pHD = new LichSuHoaDonPanel();
                pHD.setSize(1000, 650);
                pHD.doLayout();

                JTable bangHD = timBang(pHD);
                System.out.println("\n[2] Tim thay bang trong LichSuHoaDonPanel: " + (bangHD != null));

                if (bangHD != null) {
                    System.out.println("    So dong: " + bangHD.getRowCount()
                            + " | So cot: " + bangHD.getColumnCount());
                    veBang(bangHD);
                    System.out.println("    [OK] Ve bang khong loi");

                    if (bangHD.getRowCount() > 0) {
                        bangHD.setRowSelectionInterval(0, 0);
                        veBang(bangHD);
                        System.out.println("    [OK] Ve bang sau khi chon dong 0");
                    }
                }

            } catch (Throwable t) {
                loi[0] = true;
                chiTietLoi[0] = t.toString();
                System.out.println("\n[LOI] " + t);
                t.printStackTrace();
            }
        });

        System.out.println("\n=== KET QUA ===");
        if (loi[0]) {
            System.out.println("CO LOI: " + chiTietLoi[0]);
            System.exit(1);
        } else {
            System.out.println("KHONG CO LOI. Bang ve binh thuong.");
        }
    }

    /** Tim JTable dau tien trong cay component */
    private static JTable timBang(java.awt.Container container) {
        for (java.awt.Component c : container.getComponents()) {
            if (c instanceof JTable) {
                return (JTable) c;
            }
            if (c instanceof java.awt.Container) {
                JTable t = timBang((java.awt.Container) c);
                if (t != null) {
                    return t;
                }
            }
        }
        return null;
    }

    /** Ve bang vao anh trong bo dem de kiem tra loi renderer */
    private static void veBang(JTable bang) {
        bang.setSize(new Dimension(900, 400));
        bang.doLayout();

        BufferedImage anh = new BufferedImage(900, 400, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = anh.createGraphics();
        try {
            bang.paint(g);
        } finally {
            g.dispose();
        }
    }
}