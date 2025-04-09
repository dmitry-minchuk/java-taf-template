package configuration.appcontainer;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;

@Data
@AllArgsConstructor
public class AppContainerData {
    private GenericContainer<?> appContainer;
    private String appHostUrl;
}
