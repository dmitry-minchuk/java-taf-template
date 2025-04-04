package configuration;

public enum PropertyNameSpace {
    BROWSER("browser"),
    BROWSER_VERSION("browser_version"),
    RETRY_COUNT("retry_count"),
    DEFAULT_APP_PORT("default_app_port"),
    DOCKER_IMAGE_NAME("docker_image_name"),
    DEPLOYED_APP_PATH("deployed_app_path");

    private String value;

    PropertyNameSpace(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}