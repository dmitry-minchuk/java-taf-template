package domain;

public enum PropertyName {
    BASE_URL("base_url"),
    SELENIUM_HOST("selenium_host");

    private String value;

    PropertyName(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
