package services.util;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;

public class ThousandsSeparator {
    /**
     * int 숫자를 받아서 1000 단위로 콤마 "," 찍은 문자열을 반환
     * @param number
     * @return
     */
    public static String parse(int number) {
        DecimalFormat formatter = (DecimalFormat) NumberFormat.getInstance(Locale.US);
        DecimalFormatSymbols symbols = formatter.getDecimalFormatSymbols();

        symbols.setGroupingSeparator(' ');

        return formatter.format(number);
    }
}
