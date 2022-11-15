package configuration.domain;

public enum PropertyNameSpace {
    BASE_URL("base_url"),
    BASE_API_URL("base_api_url"),
    LOAD_URL("load_url"),
    ENV("env"),
    BROWSER("browser"),
    BROWSER_VERSION("browser_version"),
    PLATFORM("platform"),
    SELENIUM_HOST("selenium_host"), // The host where the selenium server is
    SELENIDE_REMOTE("selenide.remote"), // Name of the selenide property where the SELENIUM_HOST should be set
    IMPLICIT_TIMEOUT("implicit_timeout"),
    RETRY_COUNT("retry_count");

    private String value;

    PropertyNameSpace(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}