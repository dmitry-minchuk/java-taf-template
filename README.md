# Selenide Based Project Template
This project consists of 2 main branches: _allure_integration_ and _report_portal_integration_. The branch _report_portal_integration_ is also a _master_.

## Core libraries:
* REST Assured
* Selenide
* TestNG
* Cucumber 4
* Log4j2

## Basic Configuration
Basic parameters are in _src/main/resources/config.properties_

## Reporting
As you may noticed you have 2 options here: **Allure** and **ReportPortal**

### Allure Reporting
Allure reporting implemented through native Allure listener that allows to add all the needed steps and screenshots in following way:
```
SelenideLogger.addListener("AllureSelenide", new AllureSelenide().enableLogs(LogType.DRIVER, Level.ALL));
SelenideLogger.addListener("AllureSelenide", new AllureSelenide().screenshots(true).savePageSource(false));
```

That means we can see the steps of Selenide wrapped into Cucumber steps with attached screenshots and stacktrace in case of test failure.

Used dependencies:
```
        <dependency>
            <groupId>io.qameta.allure</groupId>
            <artifactId>allure-java-commons</artifactId>
            <version>${allure.basic.version}</version>
        </dependency>

        <dependency>
            <groupId>io.qameta.allure</groupId>
            <artifactId>allure-cucumber4-jvm</artifactId>
            <version>${allure.basic.version}</version>
        </dependency>

        <dependency>
            <groupId>io.qameta.allure</groupId>
            <artifactId>allure-selenide</artifactId>
            <version>${allure.basic.version}</version>
        </dependency>

        <dependency>
            <groupId>org.aspectj</groupId>
            <artifactId>aspectjweaver</artifactId>
            <version>${aspectj.version}</version>
        </dependency>
```

```
        <allure.basic.version>2.13.1</allure.basic.version>
        <aspectj.version>1.9.5</aspectj.version>
```

The way you can get pretty Allure Report may vary depending on your needs: 
* Jenkins allure plugin. This plugin as being added as the post build step in your job and attaches the report to the every job run results.
* Allure command line tool installed on your server. This tool can generate report from your _allure-results_ folder in your jenkins job space to any specified _allure-report_ folder on your hard drive. But this way you will get results only for your last test run and will be able to see the .html file through any webserver like nginx.  
* Building full Allure Report with test run history. This is a little bit tricky way also with limited history size. It includes the previous step, but instead of building _allure-report_ from job run _allure-results_ you will have to copy the contents of this folder to any common _allure-results_ via executing bash script as jenkins job bost build step and only then generate _allure-report_.
* Due to possible performance issues in future I consider an option to update _allure-java-commons_ and _allure-generator_ libraries to support mongoDB. Any help is appreciated.

### Report Portal Reporting
Unlike Allure, RP is a stand alone tool raised on the separate server and collecting data through the sockets into its inbuilt Postgress DB.
 
 The only dependency used for this integration to log cucumber steps:
 
 ```
        <dependency>
            <groupId>com.epam.reportportal</groupId>
            <artifactId>agent-java-cucumber4</artifactId>
            <version>${rp.cucumber.version}</version>
        </dependency>
```
All other data is being attached into cucumber steps via listener:
```
public class SelenideListener implements LogEventListener {
    protected static final Logger LOGGER = LogManager.getLogger(SelenideListener.class);

    @Override
    public void afterEvent(LogEvent logEvent) {
        String logStr = logEvent.getElement() + " : " + logEvent.getSubject() + " : " + logEvent.getStatus();
        // Logging into console
        LOGGER.info(logStr);
        // Logging into Report Portal
        if(logEvent.getStatus().equals(LogEvent.EventStatus.FAIL)) {
            ReportPortal.emitLog(logStr, "INFO", new Date(), Screenshots.takeScreenShotAsFile());
        } else {
            ReportPortal.emitLog(logStr, "INFO", new Date());
        }
    }

    @Override
    public void beforeEvent(LogEvent logEvent) {

    }
}
```

Of course you have to include this listener to your TestNG suite:

```
    <listeners>
        <listener class-name="configuration.listeners.SelenideListener"/>
    </listeners>
```

To send any data to the RP server there is a configuration file (src/test/resources/reportportal.properties) containing server IP, access token and some other required fileds.
