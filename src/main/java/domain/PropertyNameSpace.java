package domain;

public enum PropertyNameSpace {
    BASE_URL("base_url"),
    ENV("env"),
    SELENIUM_HOST("selenium_host"),
    SELENIDE_REMOTE("selenide.remote"),
    IMPLICIT_TIMEOUT("implicit_timeout");

    private String value;

    PropertyNameSpace(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
