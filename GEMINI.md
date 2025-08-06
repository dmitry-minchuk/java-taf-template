# GEMINI.md
This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.
Always read README.md on start up.

- Turn on Plan Mode on start up.
  use-mcp ollama-rag
- Use ollama-rag to understand codebase in depth using Vector and Graph embeddings
  use-mcp playwright
- Use it for more UI understanding - open the application on localhost:8090 (credentials admin/admin)
  use-mcp context7
- Use context7 for searching documentation

Rules of Engagement:
1. Wait for my commands - do not proceed with migration bny yourself
2. One Step at a Time: We will proceed strictly according to the plan. Do not move to the next step until we have completed and confirmed the current one.
3. Ask Questions: If you lack information, ask clarifying questions.
4. Explain Your Code: For every code snippet, provide a brief explanation of what it does and why you chose that specific solution.
5. Maintain a Log: After each successful step, we will update CLAUDE.md, adding the decisions made and the final code. Start every response with an update to this file.
6. Do not add Java-doc.
7. Do not use Selenium style for new logic. You must copy Page -> Component -> Element hierarchy and inner methods logic, but use Playwright specific functionality in its native way (check with Context7) - no selenium-like waiters, no timeouts

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
- **SELENIUM**: Legacy Selenium mode (for compatibility during migration)

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

