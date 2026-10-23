package com.pos.quancafe.data;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Hàm đọc và ghi dữ liệu kiểu CSV.
 * Xương sống của toàn bộ chương trình: mọi thông tin đều lưu ở file CSV.
 * Phần này thuộc công việc của Người 2.
 */
public class FileStore {

    private FileStore() {
        // Lớp tiện ích, không khởi tạo
    }

    /**
     * Đọc toàn bộ nội dung file CSV thành danh sách dòng (không kể dòng tiêu đề).
     *
     * @param file đường dẫn tới file CSV
     * @return danh sách dòng, mỗi dòng là một mảng chuỗi đã tách theo dấu phẩy
     */
    public static List<String[]> docTatCaDong(File file) {
        List<String[]> dsDong = new ArrayList<>();
        if (file == null || !file.exists()) {
            return dsDong;
        }

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(Files.newInputStream(file.toPath()), StandardCharsets.UTF_8))) {

            String dong;
            boolean dongTieuDe = true;

            while ((dong = br.readLine()) != null) {
                // Bỏ qua dòng tiêu đề đầu tiên
                if (dongTieuDe) {
                    dongTieuDe = false;
                    continue;
                }
                // Bỏ dòng trống
                if (dong.trim().isEmpty()) {
                    continue;
                }
                dsDong.add(tachDong(dong));
            }

        } catch (IOException e) {
            System.out.println("[Loi] Khong doc duoc file: " + file.getName() + " - " + e.getMessage());
        }

        return dsDong;
    }

    /**
     * Ghi danh sách dòng ra file CSV, có ghi dòng tiêu đề.
     *
     * @param file       đường dẫn tới file CSV
     * @param tieuDe     danh sách tên các cột
     * @param dsDong     danh sách dữ liệu, mỗi dòng là một mảng chuỗi
     */
    public static void ghiTatCaDong(File file, String[] tieuDe, List<String[]> dsDong) {
        // Tạo thư mục cha nếu chưa có
        File thuMuc = file.getParentFile();
        if (thuMuc != null && !thuMuc.exists()) {
            thuMuc.mkdirs();
        }

        try (BufferedWriter bw = new BufferedWriter(
                new OutputStreamWriter(Files.newOutputStream(file.toPath()), StandardCharsets.UTF_8))) {

            // Ghi dòng tiêu đề
            if (tieuDe != null && tieuDe.length > 0) {
                bw.write(String.join(",", tieuDe));
                bw.newLine();
            }

            // Ghi từng dòng dữ liệu
            if (dsDong != null) {
                for (String[] dong : dsDong) {
                    bw.write(noiDongChuoi(dong));
                    bw.newLine();
                }
            }

        } catch (IOException e) {
            System.out.println("[Loi] Khong ghi duoc file: " + file.getName() + " - " + e.getMessage());
        }
    }

    /**
     * Nối các phần tử thành một dòng CSV.
     * Phần tử nào chứa dấu phẩy hoặc dấu nháy thì được bọc trong dấu nháy kép.
     */
    private static String noiDongChuoi(String[] dong) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < dong.length; i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(bocDauyPhay(dong[i]));
        }
        return sb.toString();
    }

    /**
     * Bọc dấu nháy kép quanh giá trị nếu cần.
     * Ví dụ: Phin đen, đá -> "Phin đen, đá"
     */
    private static String bocDauyPhay(String giaTri) {
        if (giaTri == null) {
            return "";
        }
        boolean canBoc = giaTri.contains(",") || giaTri.contains("\"")
                || giaTri.contains("\n") || giaTri.contains("\r");

        if (!canBoc) {
            return giaTri;
        }

        // Nhân đôi dấu nháy kép bên trong theo đúng chuẩn CSV
        return "\"" + giaTri.replace("\"", "\"\"") + "\"";
    }

    /**
     * Tách một dòng CSV thành mảng chuỗi, có xử lý dấu nháy kép.
     */
    private static String[] tachDong(String dong) {
        List<String> ketQua = new ArrayList<>();
        StringBuilder hienTai = new StringBuilder();
        boolean trongDauyPhay = false;

        for (int i = 0; i < dong.length(); i++) {
            char c = dong.charAt(i);

            if (c == '"') {
                // Hai dấu nháy liên tiếp bên trong nghĩa là một dấu nháy kép
                if (trongDauyPhay && i + 1 < dong.length() && dong.charAt(i + 1) == '"') {
                    hienTai.append('"');
                    i++;
                } else {
                    trongDauyPhay = !trongDauyPhay;
                }
            } else if (c == ',' && !trongDauyPhay) {
                ketQua.add(hienTai.toString());
                hienTai.setLength(0);
            } else {
                hienTai.append(c);
            }
        }

        ketQua.add(hienTai.toString());
        return ketQua.toArray(new String[0]);
    }

    /**
     * Lấy phần tử tại vị trí đã cho trong mảng.
     * Nếu vị trí vượt quá độ dài mảng thì trả về chuỗi rỗng, tránh lỗi.
     */
    public static String layPhanTu(String[] dong, int viTri) {
        if (dong != null && viTri >= 0 && viTri < dong.length) {
            return dong[viTri];
        }
        return "";
    }

    /**
     * Tạo mảng chuỗi từ danh sách, dùng khi ghi file.
     */
    public static String[] taoMang(String... cacPhanTu) {
        return Arrays.copyOf(cacPhanTu, cacPhanTu.length);
    }
}