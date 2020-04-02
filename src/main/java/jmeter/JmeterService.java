package jmeter;

import org.apache.commons.io.FileUtils;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.config.gui.ArgumentsPanel;
import org.apache.jmeter.control.LoopController;
import org.apache.jmeter.control.gui.LoopControlPanel;
import org.apache.jmeter.control.gui.TestPlanGui;
import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jmeter.protocol.http.control.Header;
import org.apache.jmeter.protocol.http.control.HeaderManager;
import org.apache.jmeter.protocol.http.control.gui.HttpTestSampleGui;
import org.apache.jmeter.protocol.http.gui.HeaderPanel;
import org.apache.jmeter.protocol.http.sampler.HTTPSampler;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerProxy;
import org.apache.jmeter.report.config.ConfigurationException;
import org.apache.jmeter.report.dashboard.GenerationException;
import org.apache.jmeter.report.dashboard.ReportGenerator;
import org.apache.jmeter.reporters.ResultCollector;
import org.apache.jmeter.reporters.Summariser;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jmeter.threads.SetupThreadGroup;
import org.apache.jmeter.threads.gui.ThreadGroupGui;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.collections.HashTree;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import utils.JsUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class JmeterService {
    protected static final Logger LOGGER = LogManager.getLogger(JmeterService.class);

    private final String RESOURCES_PATH = "src/main/resources";
    private final String JMETER_SOURCE_PATH = RESOURCES_PATH + "/jmeter521";

    private HashTree hashTree = new HashTree();
    private HeaderManager manager = new HeaderManager();
    private HTTPSamplerProxy httpSampler = new HTTPSamplerProxy();
    private LoopController loopController = new LoopController();
    private SetupThreadGroup setupThreadGroup = new SetupThreadGroup();
    private StandardJMeterEngine jmeterEngine = new StandardJMeterEngine();
    private ReportGenerator reportGenerator;
    private SampleResult sampleResult;
    private String requestName;

    //#1
    public JmeterService(String requestName) {
        this.requestName = requestName;
    }

    //#2
    public JmeterService setHeaderManager(String accessToken) {
        setHeaderManager();
        manager.add(new Header("Authorization", "Bearer" + accessToken));
        return this;
    }

    public JmeterService setHeaderManager() {
        manager.add(new Header("Content-type", "application/json"));
        manager.setName(JMeterUtils.getResString("header_manager_title")); // $NON-NLS-1$
        manager.setProperty(TestElement.TEST_CLASS, HeaderManager.class.getName());
        manager.setProperty(TestElement.GUI_CLASS, HeaderPanel.class.getName());
        return this;
    }

    //#3
    public JmeterService setHttpSampler(String protocol, String domainName, String path, String httpMethod, int port, String rqBody) {
        //Note that HTTPS connection is available on 443 port only in common case!
        httpSampler.setDomain(domainName);
        httpSampler.setPort(port);
        httpSampler.setProtocol(protocol);
        httpSampler.setPath(path);
        httpSampler.setMethod(httpMethod);
        httpSampler.setHeaderManager(manager);
        httpSampler.setName(requestName);

        if (!httpMethod.equals("GET")) {
            httpSampler.addNonEncodedArgument("", rqBody, "");
            httpSampler.setPostBodyRaw(true);
        }

        httpSampler.setProperty(TestElement.TEST_CLASS, HTTPSamplerProxy.class.getName());
        httpSampler.setProperty(TestElement.GUI_CLASS, HttpTestSampleGui.class.getName());

        return this;
    }

    //#4
    public JmeterService setLoopController(int loopsCount) {
        loopController.setLoops(loopsCount);
        loopController.setFirst(true);
        loopController.setProperty(TestElement.TEST_CLASS, LoopController.class.getName());
        loopController.setProperty(TestElement.GUI_CLASS, LoopControlPanel.class.getName());
        loopController.initialize();
        return this;
    }

    //#5
    public JmeterService getSetupThreadGroup(String threadGroupName, int threadCount, int rampUp) {
        setupThreadGroup.setName(threadGroupName);
        setupThreadGroup.setNumThreads(threadCount);
        setupThreadGroup.setRampUp(rampUp);
        setupThreadGroup.setSamplerController(loopController);
        setupThreadGroup.setProperty(TestElement.TEST_CLASS, ThreadGroup.class.getName());
        setupThreadGroup.setProperty(TestElement.GUI_CLASS, ThreadGroupGui.class.getName());
        return this;
    }

    //#6
    public JmeterService build() {
        JMeterUtils.setJMeterHome(JMETER_SOURCE_PATH);
        JMeterUtils.loadJMeterProperties(JMETER_SOURCE_PATH + "/bin/jmeter.properties");
        JMeterUtils.initLocale();
        JMeterUtils.setProperty("jmeter.reportgenerator.exporter.html.property.output_dir", "src/main/resources/HTMLReports");

        //Create the tesPlan item
        TestPlan testPlan = new TestPlan();
        testPlan.setProperty(TestElement.TEST_CLASS, TestPlan.class.getName());
        testPlan.setProperty(TestElement.GUI_CLASS, TestPlanGui.class.getName());
        testPlan.setUserDefinedVariables((Arguments) new ArgumentsPanel().createTestElement());
        hashTree.add(testPlan);

        HashTree groupTree = hashTree.add(testPlan, setupThreadGroup);
        groupTree.add(httpSampler);
        groupTree.add(manager);
        sampleResult = httpSampler.sample();

        try {
            SaveService.saveTree(hashTree, new FileOutputStream(RESOURCES_PATH + "/projectFile.jmx"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        setReportEnvironment(hashTree, new Summariser("summaryOfResults"));
        return this;
    }

    //#7
    public SampleResult run() {
        jmeterEngine.configure(hashTree);
        jmeterEngine.run();

        try {
            reportGenerator.generate();
        } catch (GenerationException e) {
            e.printStackTrace();
        }
        return sampleResult;
    }

    private synchronized void setReportEnvironment(HashTree hashTree, Summariser summariser) {
        String logFile = RESOURCES_PATH + "/report.jtl";
        File file = new File(logFile);

        if (file.exists()) {
            try {
                FileUtils.forceDelete(file);
                FileUtils.forceDelete(new File("report-output"));
                FileUtils.forceDelete(new File("src/main/resources/HTMLReports"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        ResultCollector logger = new ResultCollector(summariser);
        logger.setFilename(logFile);
        hashTree.add(hashTree.getArray()[0], logger);

        try {
            reportGenerator = new ReportGenerator(logFile, logger);
        } catch (ConfigurationException e) {
            e.printStackTrace();
        }
    }
}
