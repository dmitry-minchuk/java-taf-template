package helpers.utils;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public final class EntityIdUtil {

    public static final String URL_SAFE_BASE64_PATTERN = "[A-Za-z0-9_=-]+";

    private EntityIdUtil() {
    }

    public static String lastUrlSegment(String url) {
        String withoutQuery = url.split("\\?")[0];
        return withoutQuery.substring(withoutQuery.lastIndexOf('/') + 1);
    }

    public static String decodeUrlSafeId(String idSegment) {
        String padded = idSegment + "=".repeat((4 - idSegment.length() % 4) % 4);
        return new String(Base64.getUrlDecoder().decode(padded), StandardCharsets.UTF_8);
    }
}
