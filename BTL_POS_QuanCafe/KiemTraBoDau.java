import com.pos.quancafe.util.FormatUtil;

public class KiemTraBoDau {
    public static void main(String[] args) {
        String[] thu = {
            "Phin \u0111en \u0111\u00e1", "B\u00e1nh m\u00ec ch\u00e0 b\u00f4ng", "Tr\u00e0 s\u1eefa Tr\u00e2n ch\u00e2u",
            "N\u01b0\u1edbc cam t\u01b0\u01a1i", "\u0110\u01b0\u1eddng", "B\u00e1nh m\u00ec s\u1eefa"
        };
        for (String s : thu) {
            System.out.println("  \"" + s + "\"  ->  \"" + FormatUtil.boDau(s) + "\"");
        }
        System.out.println();
        System.out.println("Kiem tra khop:");
        System.out.println("  phin den == phin den da      : "
            + FormatUtil.boDau("Phin \u0111en \u0111\u00e1").equals(FormatUtil.boDau("phin den")));
        System.out.println("  banh mi == BANH MI CHA BONG  : "
            + FormatUtil.boDau("B\u00e1nh m\u00ec ch\u00e0 b\u00f4ng").equals(FormatUtil.boDau("B\u00c1NH M\u00cc CH\u00c0 B\u00d4NG")));
        System.out.println("  Duong -> duong              : "
            + FormatUtil.boDau("\u0110\u01b0\u1eddng").equals("duong"));
    }
}