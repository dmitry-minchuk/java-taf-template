package helpers.utils;

public final class TestGroupUtil {

    private static final String[] PACKAGE_PREFIXES = {"tests.ui.webstudio.", "tests.ui.", "tests."};

    private TestGroupUtil() {
    }

    public static String groupOf(String className) {
        int lastDot = className.lastIndexOf('.');
        String packageName = lastDot > 0 ? className.substring(0, lastDot) : "";
        for (String prefix : PACKAGE_PREFIXES) {
            if (packageName.startsWith(prefix)) {
                return packageName.substring(prefix.length()).replace('.', '_');
            }
        }
        return packageName.isEmpty() ? "default" : packageName.replace('.', '_');
    }
}
