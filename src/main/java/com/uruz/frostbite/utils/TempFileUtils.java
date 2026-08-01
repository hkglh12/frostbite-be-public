package com.uruz.frostbite.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class TempFileUtils {

    @Value("${frostbite.multipart.temp.ttl}")
    private String ttl;

    private static final Pattern TTL_PATTERN = Pattern.compile("^(\\d+)([smhd])$");

    public long getTempFileTTLToSeconds () {
        if (ttl == null || ttl.isBlank()) {
            throw new IllegalArgumentException("TTL must not be blank");
        }

        String normalizedTtl = ttl.trim().toLowerCase(Locale.ROOT);

        Matcher matcher = TTL_PATTERN.matcher(normalizedTtl);
        if (!matcher.matches()) {
            throw new IllegalArgumentException(
                    "Invalid TTL format. Expected format: {number}{unit}, e.g. 30s, 10m, 12h, 2d"
            );
        }

        long amount = Long.parseLong(matcher.group(1));
        String unit = matcher.group(2);

        return switch (unit) {
            case "s" -> amount;
            case "m" -> amount * 60;
            case "h" -> amount * 60 * 60;
            case "d" -> amount * 60 * 60 * 24;
            default -> throw new IllegalArgumentException("Unsupported TTL unit: " + unit);
        };
    }
}
