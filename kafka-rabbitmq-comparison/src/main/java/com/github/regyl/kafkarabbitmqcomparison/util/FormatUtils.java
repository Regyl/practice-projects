package com.github.regyl.kafkarabbitmqcomparison.util;

import lombok.experimental.UtilityClass;

import java.util.Locale;

@UtilityClass
public class FormatUtils {

    public static String format(double value) {
        return String.format(Locale.ROOT, "%.1f", value);
    }
}
