package tests.testng;

import configuration.ProjectConfiguration;
import configuration.domain.PropertyNameSpace;
import jmeter.JmeterService;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerProxy;
import org.testng.annotations.Test;

public class SampleLoadTest extends BaseTest {
    String domain = ProjectConfiguration.getEnvUrl(PropertyNameSpace.LOAD_URL);

    @Test
    public void testGetRequest() {
        JmeterService jmeterService = new JmeterService();
        jmeterService
                .setHeaderManager()
                .setHttpSampler(HTTPSamplerProxy.PROTOCOL_HTTPS, domain, "", HTTPSamplerProxy.GET, 443,"", "1_user")
                .setHttpSampler(HTTPSamplerProxy.PROTOCOL_HTTPS, domain, "", HTTPSamplerProxy.GET, 443,"", "100_user")
                .setLoopController(1)
                .setThreadGroup("1_user", 1, 15)
                .setThreadGroup("100_user", 100, 15)
                .build()
                .run();
    }
}
