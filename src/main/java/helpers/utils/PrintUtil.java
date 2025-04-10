package helpers.utils;

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
}
