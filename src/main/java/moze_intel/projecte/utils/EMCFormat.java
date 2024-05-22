package moze_intel.projecte.utils;

import moze_intel.projecte.config.ProjectEConfig;
import net.minecraft.client.gui.GuiScreen;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * @author LatvianModder
 */
public class EMCFormat extends DecimalFormat {
    public static final EMCFormat INSTANCE = new EMCFormat();
    private static final DecimalFormat decimalFormat = new DecimalFormat();

    private EMCFormat() {
        super("#,###");
        setRoundingMode(RoundingMode.DOWN);
    }

    @Override
    public StringBuffer format(double number, StringBuffer result, FieldPosition fieldPosition) {
        String formatted = format(BigDecimal.valueOf(number));
        return new StringBuffer(formatted);
    }

    @Override
    public StringBuffer format(long number, StringBuffer result, FieldPosition fieldPosition) {
        String formatted = format(BigDecimal.valueOf(number));
        return new StringBuffer(formatted);
    }

    public static String format(BigInteger number) {
        return format(new BigDecimal(number), IgnoreShiftType.NONE);
    }

    public static String format(BigInteger value, IgnoreShiftType ignoreShiftType) {
        return format(new BigDecimal(value), ignoreShiftType);
    }

    public static String format(BigDecimal value) {
        return format(value, IgnoreShiftType.NONE);
    }

    public static boolean shouldFormat(BigDecimal value, IgnoreShiftType ignoreShiftType) {
        return ProjectEConfig.misc.useEMCFormatter && ignoreShiftType != IgnoreShiftType.NO_FORMAT
                && value.compareTo(new BigDecimal("1e6")) >= 0
                && (ignoreShiftType == IgnoreShiftType.FORMAT || !GuiScreen.isShiftKeyDown());
    }

    public static String format(BigDecimal number, IgnoreShiftType ignoreShiftType) {
        if (shouldFormat(number, ignoreShiftType)) {
            int suf = 0;
            BigDecimal de = new BigDecimal("1e63");
            while (number.compareTo(de) >= 0) {
                number = number.divide(de, 2, RoundingMode.DOWN);
                suf++;
            }
            String s = suf > 0 ? "De" + (suf > 1 ? suf : "") : "";
            NumberName n = NumberName.findName(number);
            if (n != null)
                return number.divide(n.getValue(), 2, RoundingMode.DOWN).toPlainString() + n.getName() + s;
            else
                return NumberFormat.getNumberInstance(Locale.US).format(number) + s;
        }
        return NumberFormat.getNumberInstance(Locale.US).format(number);
    }

    public enum IgnoreShiftType {
        NONE,
        FORMAT,
        NO_FORMAT
    }
    public enum NumberName {
        MILLION(1e6, "M"),
        BILLION(1e9, "G"),
        TRILLION(1e12, "T"),
        QUADRILLION(1e15, "P"),
        QUINTILLION(1e18, "E"),
        SEXTILLION(1e21, "Z"),
        SEPTILLION(1e24, "Y"),
        OCTILLION(1e27, "R"),
        NONILLION(1e30, "Q"),
        DECILLION(1e33, "D"),
        UNDECILLION(1e36, "U"),
        DUODECILLION(1e39, "Du"),
        TREDECILLION(1e42, "Tr"),
        QUATTUORDECILLION(1e45, "Qt"),
        QUINDECILLION(1e48, "Qd"),
        SEXDECILLION(1e51, "Sd"),
        SEPTENDECILLION(1e54, "St"),
        OCTODECILLION(1e57, "Oc"),
        NOVEMDECILLION(1e60, "No");

        public static final NumberName[] VALUES = values();
        private final BigDecimal value;
        private final String shortName;

        NumberName(double val, String shortName) {
            this.value = new BigDecimal(val);
            this.shortName = shortName;
        }

        public BigDecimal getValue() {
            return value;
        }

        public String getName() {
            return shortName;
        }

        static @Nullable NumberName findName(BigDecimal val) {
            int exp = val.precision() - val.scale() - 1;
            return exp > 5 ? VALUES[exp / 3 - 2] : null;
            //return Arrays.stream(VALUES).filter(v -> val.compareTo(v.getValue()) > -1).reduce((first, second) -> second).orElse(null);
        }
    }
}