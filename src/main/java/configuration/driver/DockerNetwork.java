package configuration.driver;

import org.testcontainers.containers.Network;

public class DockerNetwork {
    private static Network network = null;

    private DockerNetwork() {}

    private static void initNetwork() {
        network = Network.newNetwork();
    }

    public static Network getNetwork() {
        if(network == null)
            initNetwork();
        return network;
    }
}
