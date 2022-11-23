package configuration.driver;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.openqa.selenium.WebDriver;
import org.testcontainers.containers.GenericContainer;

@Data
@AllArgsConstructor
public class ContainerizedDriver {
    private WebDriver driver;
    private GenericContainer driverContainer;
}
