# Project Summary

## Overview of Technologies Used
The project is primarily built using Java, leveraging various frameworks and libraries to facilitate testing and API interactions. The key technologies include:

- **Languages**: Java
- **Frameworks**: 
  - Selenide (for browser automation)
  - Cucumber (for behavior-driven development)
  - TestNG (for unit testing)
- **Main Libraries**: 
  - Log4j (for logging)
  - ReportPortal (for reporting)

## Purpose of the Project
The project aims to implement automated testing for web applications and APIs, using a combination of Selenide for UI testing and Cucumber/TestNG for structured testing methodologies. It includes various test cases for both API and UI interactions, ensuring that the application functions as expected across different scenarios.

## Build and Configuration Files
The following files are relevant for the configuration and building of the project:

1. `/pom.xml` - Maven project configuration file.
2. `/Jenkinsfile.groovy` - Jenkins pipeline configuration file for CI/CD integration.

## Source Files Directories
Source files can be found in the following directories:

- **Java Source Files**: 
  - `/src/main/java/api/methods`
  - `/src/main/java/configuration`
  - `/src/main/java/configuration/domain`
  - `/src/main/java/configuration/listeners`
  - `/src/main/java/utils`
  - `/src/main/java/web/components`
  - `/src/main/java/web/pages`
- **Test Source Files**:
  - `/src/test/java/stepdefinitions`
  - `/src/test/java/tests/cucumber`
  - `/src/test/java/tests/testng`

## Documentation Files Location
Documentation files are located in the following paths:

- `/README.md` - Contains project overview and instructions.
- `/src/main/resources/config.properties` - Configuration properties for the application.
- `/src/test/resources/log4j2-test.xml` - Logging configuration file.
- `/src/test/resources/reportportal.properties` - ReportPortal configuration file. 

This summary provides a comprehensive overview of the project's structure, technologies, and files relevant for building and configuring the application.