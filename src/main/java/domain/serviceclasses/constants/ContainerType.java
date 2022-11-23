package domain.serviceclasses.constants;

import org.testcontainers.containers.wait.strategy.HttpWaitStrategy;
import org.testcontainers.containers.wait.strategy.Wait;

public enum ContainerType {
    WEBSTUDIO("openlapp", "8080:8080", "webstudio:5.26.3", Wait.forHttp("/webstudio"));

    private String containerName;
    private String portMapping;
    private String imageName;
    private HttpWaitStrategy wait;

    ContainerType(String containerName, String portMapping, String imageName, HttpWaitStrategy wait) {
        this.containerName = containerName;
        this.portMapping = portMapping;
        this.imageName = imageName;
        this.wait = wait;
    }

    public String getContainerName() {
        return containerName;
    }

    public String getPortMapping() {
        return portMapping;
    }

    public String getImageName() {
        return imageName;
    }

    public HttpWaitStrategy getWaitStrategy() {
        return wait;
    }
}
