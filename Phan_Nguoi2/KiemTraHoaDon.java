import com.pos.quancafe.util.HoaDonPrinter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Kiem tra bo cuc hoa don: canh cot dung va xuong dong khi qua 1 ty. */
public class KiemTraHoaDon {

    public static void main(String[] args) {
        System.out.println("=== KIEM TRA BO CUC HOA DON ===\n");

        // ---------- 1. Hoa don binh thuong ----------
        List<String[]> dsMon = new ArrayList<>();
        dsMon.add(new String[]{"Phin đen đá", "2", "25000", "50000"});
        dsMon.add(new String[]{"Bánh mì chà bông", "2", "15000", "30000"});
        dsMon.add(new String[]{"Soda chanh muối", "1", "28000", "28000"});
        dsMon.add(new String[]{"Trà sữa Trân châu", "1", "45000", "45000"});

        String hd = HoaDonPrinter.taoNoiDung(
                "HD20261023001", "B01",
                LocalDateTime.of(2026, 10, 23, 8, 30),
                LocalDateTime.of(2026, 10, 23, 9, 15),
                dsMon, 153000, "Tiền mặt", 200000, 47000);

        System.out.println("--- 1. Hoa don binh thuong ---");
        System.out.println(hd);
        kiemTraCanh(hd);

        // ---------- 2. Hoa don qua 1 ty ----------
        System.out.println("--- 2. Hoa don tong tien qua 1 ty ---");
        List<String[]> dsMon2 = new ArrayList<>();
        dsMon2.add(new String[]{"Phin đen đá", "100", "5000000", "500000000"});
        dsMon2.add(new String[]{"Cà phê sữa đá", "50", "30000000", "1500000000"});

        String hd2 = HoaDonPrinter.taoNoiDung(
                "HD20261023002", "B05",
                LocalDateTime.of(2026, 10, 23, 10, 0),
                LocalDateTime.of(2026, 10, 23, 11, 0),
                dsMon2, 2000000000L, "Chuyển khoản", 0, 0);

        System.out.println(hd2);
        kiemTraCanh(hd2);

        // ---------- 3. Ten mon rat dai ----------
        System.out.println("--- 3. Ten mon dai ---");
        List<String[]> dsMon3 = new ArrayList<>();
        dsMon3.add(new String[]{"Cà phê sữa đá size lớn đặc biệt có kem và bột lạt", "1", "50000", "50000"});

        String hd3 = HoaDonPrinter.taoNoiDung(
                "HD20261023003", "B09",
                LocalDateTime.of(2026, 10, 23, 12, 0),
                LocalDateTime.of(2026, 10, 23, 12, 30),
                dsMon3, 50000, "Tiền mặt", 50000, 0);

        System.out.println(hd3);
        kiemTraCanh(hd3);
    }

    /** Kiem tra tat ca dong trong khung co cung do dai khong */
    private static void kiemTraCanh(String noiDung) {
        String[] dong = noiDung.split("\n");

        // Chi lay cac dong co viền
        java.util.List<String> trongKhung = new ArrayList<>();
        for (String d : dong) {
            if (d.startsWith("|")) {
                trongKhung.add(d);
            }
        }

        boolean sai = false;
        int doDaiChuan = -1;

        for (String d : trongKhung) {
            if (doDaiChuan == -1) {
                doDaiChuan = d.length();
            }
            if (d.length() != doDaiChuan) {
                System.out.println("  [LOI] Do dai dong khong deu: " + d.length()
                        + " (chuẩn " + doDaiChuan + ") -> '" + d + "'");
                sai = true;
            }
        }

        if (!sai) {
            System.out.println("  [OK] Tat ca " + trongKhung.size()
                    + " dong trong khung deu dai " + doDaiChuan + " ky tu");
        }
        System.out.println();
    }
}