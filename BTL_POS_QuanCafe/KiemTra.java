import com.pos.quancafe.data.HoaDonRepo;
import com.pos.quancafe.data.MonRepo;
import com.pos.quancafe.model.HoaDon;
import com.pos.quancafe.model.Mon;
import com.pos.quancafe.model.NhomMon;
import com.pos.quancafe.service.ThongKeService;
import com.pos.quancafe.util.FormatUtil;
import com.pos.quancafe.util.HoaDonPrinter;

import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Kiem tra logic phan Nguoi 2 khong can mo cua so. */
public class KiemTra {

    public static void main(String[] args) {
        System.out.println("================ KIEM TRA PHAN NGUOI 2 ================\n");

        // --- 1. MonRepo: doc va tao du lieu mau ---
        MonRepo monRepo = new MonRepo();
        List<Mon> dsMon = monRepo.findAll();
        System.out.println("[1] Doc duoc " + dsMon.size() + " mon tu file data/mon.csv");

        int demCafe = monRepo.findByNhom(NhomMon.CA_PHE).size();
        int demNuoc = monRepo.findByNhom(NhomMon.NUOC_UONG).size();
        int demTra = monRepo.findByNhom(NhomMon.TRA).size();
        int demBanh = monRepo.findByNhom(NhomMon.BANH).size();
        System.out.println("    Phan bo 4 nhom: Ca phe=" + demCafe + ", Nuoc=" + demNuoc
                + ", Tra=" + demTra + ", Banh=" + demBanh);

        // --- 2. Them mon moi ---
        String maMoi = monRepo.sinhMaMonMoi();
        monRepo.save(new Mon(maMoi, "Kiem tra tu dong", NhomMon.TRA, 33000, 50, "Mon them kiem thu"));
        System.out.println("[2] Them mon moi -> " + monRepo.findAll().size() + " mon, ma moi = " + maMoi);

        // --- 3. Sua mon ---
        Mon monCanSua = monRepo.findByMa(maMoi);
        monCanSua.setDonGia(35000);
        monRepo.save(monCanSua);
        System.out.println("[3] Sua gia thanh cong -> " + FormatUtil.formatTien(monRepo.findByMa(maMoi).getDonGia()));

        // --- 4. Tim kiem bo dau ---
        List<Mon> ketQuaTim = monRepo.timTheoTen("phin den");
        System.out.println("[4] Tim kiem 'phin den' (khong dau, thuong) -> " + ketQuaTim.size() + " ket qua");

        // --- 5. Trung ten bi chan ---
        System.out.println("[5] Kiem tra trung ten 'Phin den da' -> "
                + (monRepo.tonTaiTen("Phin đen đá") ? "BChan dung" : "Thong bao loi"));

        // --- 6. Xoa mon ---
        monRepo.delete(maMoi);
        System.out.println("[6] Xoa mon -> con lai " + monRepo.findAll().size() + " mon");

        // --- 7. HoaDonRepo: tao va doc du lieu mau ---
        HoaDonRepo hoaDonRepo = new HoaDonRepo();
        hoaDonRepo.taoDuLieuMauNeuRong();
        List<HoaDon> dsHD = hoaDonRepo.findDaThanhToan();
        System.out.println("\n[7] Doc " + dsHD.size() + " hoa don da thanh toan");

        // Kiem tra ghi va doc lai chi tiet co dung khong
        HoaDon hd = dsHD.get(0);
        System.out.println("    Hoa don " + hd.getMaHD() + " tai ban " + hd.getMaBan()
                + " co " + hd.getDsChiTiet().size() + " dong chi tiet");
        for (var ct : hd.getDsChiTiet()) {
            System.out.println("      - " + ct.getTenMon() + " x" + ct.getSoLuong()
                    + " = " + FormatUtil.formatTien(ct.thanhTien()));
        }

        // Tong tien co khop khong
        long tongTinhLai = 0;
        for (var ct : hd.getDsChiTiet()) {
            tongTinhLai += ct.thanhTien();
        }
        System.out.println("    Tong luu file = " + FormatUtil.formatTien(hd.getTongTien())
                + " | Tong tinh lai = " + FormatUtil.formatTien(tongTinhLai)
                + " | " + (tongTinhLai == hd.getTongTien() ? "KHOP" : "SAI"));

        // --- 8. Thong ke doanh thu ---
        ThongKeService tk = new ThongKeService(hoaDonRepo);
        LocalDate homNay = LocalDate.now();
        System.out.println("\n[8] Doanh thu hom nay (" + homNay + "): "
                + FormatUtil.formatTien(tk.doanhThuHomNay()));
        System.out.println("    So hoa don hom nay: " + tk.soHoaDonHomNay());
        System.out.println("    Tien trung binh moi hoa don: " + FormatUtil.formatTien(tk.tienTrungBinhMoiHoaDon(homNay)));

        // --- 9. Tao noi dung hoa don ---
        List<Object> dsCT = new ArrayList<>(hd.getDsChiTiet());
        String noiDung = HoaDonPrinter.taoNoiDungTuChiTiet(
                hd.getMaHD(), hd.getMaBan(), hd.getNgayMo(), hd.getNgayDong(), dsCT,
                hd.getTongTien(),
                hd.getPhuongThucTT() == null ? "" : hd.getPhuongThucTT().getTenHienThi(),
                hd.getTienKhachDua(), hd.getTienThua());
        System.out.println("\n[9] Noi dung hoa don:\n");
        System.out.println(noiDung);

        // --- 10. Xuat file hoa don ---
        File file = HoaDonPrinter.taoFileHoaDon(hd.getMaHD());
        boolean thanhCong = HoaDonPrinter.xuatFileTxt(noiDung, file);
        System.out.println("[10] Xuat file hoa don: "
                + (thanhCong ? "THANH CONG -> " + file.getPath() : "THAT BAI")
                + " | File ton tai = " + file.exists());

        // --- 11. Tien thua khi thanh toan tien mat ---
        System.out.println("\n[11] Kiem tra tien thua: dua = "
                + FormatUtil.formatTien(hd.getTienKhachDua())
                + ", tong = " + FormatUtil.formatTien(hd.getTongTien())
                + ", thua = " + FormatUtil.formatTien(hd.getTienThua())
                + " | " + (hd.getTienKhachDua() - hd.getTongTien() == hd.getTienThua() ? "KHOP" : "SAI"));

        System.out.println("\n================ HET ================");
    }
}