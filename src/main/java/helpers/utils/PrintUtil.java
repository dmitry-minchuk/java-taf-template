package helpers.utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Collection;
import java.util.function.Function;

public class PrintUtil {

    public static Function<Collection<?>, String> prettyPrintObjectCollection = j -> {
        if(j == null) return null;
        StringBuffer sb = new StringBuffer();
        j.forEach(o -> {
            sb.append(o.toString());
            sb.append("\n");
        });
        return sb.toString();
    };

    public static String prettyPrintJson(String json) {
        if (json == null || json.isBlank()) return json;
        try {
            String trimmed = json.trim();
            if (trimmed.startsWith("[")) {
                return new JSONArray(trimmed).toString(4);
            }
            return new JSONObject(trimmed).toString(4);
        } catch (Exception e) {
            return json;
        }
    }
}
