# CLAUDE.md
This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.
Always read README.md on start up.

You are experienced Test Automation Engineer with 10+ years of experience with Java, TestNG, log4j, Playwright, Jenkins, CICD, Docker, testcontainers.
When you analyzing the issue or going to implement any code you MUST take a look at ALL the related codebase (and documentation if needed) and check all the connections and imports.

 - Turn on Plan Mode on start up.
use-mcp ollama-rag
 - Use ollama-rag to understand codebase in depth using Vector and Graph embeddings
use-mcp playwright
 - Use it for more UI understanding - open the application on localhost:8090 (credentials admin/admin)
use-mcp context7
 - Use context7 for searching documentation

## Reasoning Protocol

Before responding, follow this internal reasoning protocol:

1. Perform a **chain-of-thought analysis**: break down the problem into logical steps, even if the user doesn’t explicitly ask for it.
2. Identify **hidden dependencies** and **non-obvious interactions** between components, modules, or concepts.
3. If the context is ambiguous or incomplete, **ask clarifying questions** before proceeding.
4. Always consider **alternative solutions**, even if one seems obvious.
5. Highlight **potential side effects, trade-offs, and edge cases**.
6. Use **structured output**: include analysis, dependencies, risks, recommendations, and a step-by-step plan.
7. If multiple interpretations are possible, **explicitly list them** and explain which one you chose and why.
8. Prioritize **depth and accuracy** over brevity or speed.
9. When analyzing code, **simulate execution mentally** to uncover runtime implications.
10. If relevant, **cite reasoning steps** or internal assumptions that led to your conclusion.

### **ARCHITECTURAL OVERVIEW** 🏗️
```
Components → PlaywrightDriverPool (Unified Interface)
                    ↓
            [Automatic Mode Detection]
                    ↓
    LOCAL Mode → Direct Playwright → Container App
    DOCKER Mode → Container Playwright → Container App
                    ↓
            [File Operations Support]
                    ↓
    Upload: Volume Mapping + TestDataUtil
    Download: PlaywrightDownloadUtil (mode-aware)
```

## **TEST EXECUTION GUIDE** 🚀

### **Execution Modes**
The framework supports multiple execution modes controlled by the `execution.mode` system property:

- **PLAYWRIGHT_LOCAL** (default): Playwright runs directly on the host machine
- **PLAYWRIGHT_DOCKER**: Playwright runs inside Docker containers for isolation

### **Running Test Suites**

#### **Available Test Suites**
Located in `src/test/resources/testng_suites/`:
- `playwright_parallel_suite.xml` - Playwright tests with parallel execution (2 threads)
- `studio_smoke.xml` - Smoke tests
- `studio_issues.xml` - Issue regression tests
- `studio_rules_editor.xml` - Rules editor tests

#### **Basic Suite Execution**
```bash
# Run default suite (LOCAL mode)
mvn clean test -Dsuite=playwright_parallel_suite

# Run specific suite with mode
mvn clean test -Dsuite=playwright_parallel_suite -Dexecution.mode=PLAYWRIGHT_LOCAL
mvn clean test -Dsuite=playwright_parallel_suite -Dexecution.mode=PLAYWRIGHT_DOCKER

# Run other suites
mvn clean test -Dsuite=studio_smoke -Dexecution.mode=PLAYWRIGHT_LOCAL
mvn clean test -Dsuite=studio_issues -Dexecution.mode=PLAYWRIGHT_DOCKER
```

#### **Parallel Execution Verification**
The `playwright_parallel_suite.xml` runs with `parallel="methods"` and `thread-count="2"`:
- **LOCAL Mode**: ~25.8s execution time with 2 parallel threads
- **DOCKER Mode**: ~23.9s execution time with 2 parallel Docker containers

Look for log entries like:
```
[TestNG-test-PlaywrightParallelTest-1] [INFO] Initializing test with Playwright: testPlaywrightAdminEmail
[TestNG-test-PlaywrightParallelTest-2] [INFO] Initializing test with Playwright: testPlaywrightAddProperty
```

### **Running Individual Tests**

#### **Single Test Class**
```bash
# Run single test class in LOCAL mode
mvn clean test -Dtest=TestPlaywrightAdminEmail -Dexecution.mode=PLAYWRIGHT_LOCAL

# Run single test class in DOCKER mode
mvn clean test -Dtest=TestPlaywrightAddProperty -Dexecution.mode=PLAYWRIGHT_DOCKER
```

#### **Single Test Method**
```bash
# Run specific test method
mvn clean test -Dtest=TestPlaywrightAdminEmail#testPlaywrightAdminEmail -Dexecution.mode=PLAYWRIGHT_LOCAL
mvn clean test -Dtest=TestPlaywrightAddProperty#testPlaywrightAddProperty -Dexecution.mode=PLAYWRIGHT_DOCKER
```

#### **Multiple Test Classes**
```bash
# Run multiple test classes
mvn clean test -Dtest=TestPlaywrightAdminEmail,TestPlaywrightAddProperty -Dexecution.mode=PLAYWRIGHT_LOCAL
```
