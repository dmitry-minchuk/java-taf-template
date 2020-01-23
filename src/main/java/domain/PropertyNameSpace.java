package domain;

public enum PropertyNameSpace {
    BASE_URL("base_url"),
    SELENIUM_HOST("selenium_host");

    private String value;

    PropertyNameSpace(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
