package tests.testng;

import configuration.ProjectConfiguration;
import configuration.domain.PropertyNameSpace;
import jmeter.JmeterService;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerProxy;
import org.testng.annotations.Test;

public class SampleLoadTest extends BaseTest {
    String domain = ProjectConfiguration.getEnvUrl(PropertyNameSpace.BASE_API_URL);

    @Test
    public void requestToCompany() {
        JmeterService jmeterService = new JmeterService("HttpBin Get Request");
        jmeterService
                .setHeaderManager()
                .setHttpSampler(HTTPSamplerProxy.PROTOCOL_HTTPS, domain, "", HTTPSamplerProxy.GET, 443, "")
                .setLoopController(1)
                .getSetupThreadGroup("Test_1", 10, 15)
                .build()
                .run();
    }
}
