package domain.api;

import io.restassured.http.Method;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

import java.util.LinkedHashMap;
import java.util.Map;

public class GetApplicationInfoMethod extends ApiBaseMethod {

    public GetApplicationInfoMethod() {
        super("/web/public/info/openl.json");
    }

    public Response getApplicationInfo() {
        LOGGER.debug("Retrieving application info from: {}", fullApiUrl);
        return callApi(Method.GET, null, true);
    }

    public Map<String, String> getApplicationInfoFields() {
        Map<String, String> fields = new LinkedHashMap<>();
        try {
            Response response = getApplicationInfo();
            if (response.getStatusCode() != 200) {
                throw new RuntimeException("Failed to retrieve application info. Status: " + response.getStatusCode());
            }
            JsonPath jsonPath = JsonPath.from(response.asString());
            putIfPresent(fields, "version", jsonPath.getString("'openl.version'"));
            putIfPresent(fields, "buildDate", jsonPath.getString("'openl.build.date'"));
            putIfPresent(fields, "buildNumber", jsonPath.getString("'openl.build.number'"));
        } catch (Exception e) {
            fields.put("error", String.valueOf(e.getMessage()));
        }
        return fields;
    }

    public String getApplicationInfoOneLiner() {
        return describe(getApplicationInfoFields());
    }

    public static String describe(Map<String, String> fields) {
        if (fields.containsKey("error")) {
            return "Application info unavailable: " + fields.get("error");
        }
        return String.format("Application started: version=%s, build=%s, commit=%s",
                fields.getOrDefault("version", "unknown"),
                fields.getOrDefault("buildDate", "unknown"),
                fields.getOrDefault("buildNumber", "unknown"));
    }

    private static void putIfPresent(Map<String, String> fields, String key, String value) {
        if (value != null) {
            fields.put(key, value);
        }
    }
}
