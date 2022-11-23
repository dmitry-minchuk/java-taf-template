package helpers.service;

import configuration.driver.DockerNetwork;
import domain.serviceclasses.constants.ContainerType;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.ArrayList;
import java.util.List;

public class ContainerService {

    public static GenericContainer<?> createContainer(ContainerType containerType) {
        GenericContainer<?> container = new GenericContainer<>(DockerImageName.parse(containerType.getImageName()));
        List<String> portBindings = new ArrayList<>();
        portBindings.add(containerType.getPortMapping());
        container.setPortBindings(portBindings);
        container.withNetwork(DockerNetwork.getNetwork());
        container.withNetworkAliases(containerType.getContainerName());
        container.start();
        container.waitingFor(containerType.getWaitStrategy());
        return container;
    }
}
