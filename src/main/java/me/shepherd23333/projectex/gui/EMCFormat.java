package me.shepherd23333.projectex.gui;

import me.shepherd23333.projectex.ProjectEXConfig;
import net.minecraft.client.gui.GuiScreen;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Locale;

/**
 * @author LatvianModder
 */
public class EMCFormat extends DecimalFormat {
    public static final EMCFormat INSTANCE = new EMCFormat(false);
    public static final EMCFormat INSTANCE_IGNORE_SHIFT = new EMCFormat(true);

    private final boolean ignoreShift;

    private EMCFormat(boolean is) {
        super("#,###");
        setRoundingMode(RoundingMode.DOWN);
        ignoreShift = is;
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

    public String format(BigInteger number) {
        return format(new BigDecimal(number));
    }

    public String format(BigDecimal number) {
        if (ProjectEXConfig.general.override_emc_formatter && number.compareTo(BigDecimal.valueOf(1e6)) >= 0 && (ignoreShift || !GuiScreen.isShiftKeyDown())) {
            int suf = 0;
            while (number.compareTo(BigDecimal.valueOf(1e63)) >= 0) {
                number = number.divide(BigDecimal.valueOf(1e63), 2, RoundingMode.HALF_UP);
                suf++;
            }
            String s = suf > 0 ? "De" + (suf > 1 ? suf : "") : "";
            NumberName n = NumberName.findName(number);
            if (n != null)
                return number.divide(n.getValue(), 2, RoundingMode.HALF_UP).toPlainString() + n.getName() + s;
            else
                return NumberFormat.getNumberInstance(Locale.US).format(number) + s;
        }
        return NumberFormat.getNumberInstance(Locale.US).format(number);
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
            return Arrays.stream(VALUES).filter(v -> val.compareTo(v.getValue()) > -1).reduce((first, second) -> second).orElse(null);
        }
    }
}