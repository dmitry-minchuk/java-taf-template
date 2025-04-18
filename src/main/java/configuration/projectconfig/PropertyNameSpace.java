package configuration.projectconfig;

public enum PropertyNameSpace {
    BROWSER("browser"),
    BROWSER_VERSION("browser_version"),
    RETRY_COUNT("test_retry_count"),
    WEB_ELEMENT_EXPLICIT_WAIT("web_element_explicit_wait"),
    DEFAULT_APP_PORT("default_app_port"),
    DOCKER_IMAGE_NAME("docker_image_name"),
    DEPLOYED_APP_PATH("deployed_app_path"),
    HOST_RESOURCE_PATH("host_resource_path"),
    CONTAINER_RESOURCE_PATH("container_resource_path"),
    HOST_SCREENSHOTS_PATH("host_screenshot_path"),
    CONTAINER_SCREENSHOTS_PATH("container_screenshot_path"),
    GIT_URL_RULESERVICE("git.url.ruleservice"),
    GIT_LOGIN_RULESERVICE("git.login.ruleservice"),
    GIT_TOKEN_RULESERVICE("git.token.ruleservice");

    private String value;

    PropertyNameSpace(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}