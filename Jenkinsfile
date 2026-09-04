package main.jenkins

def openlTabletsGitIrl = "https://github.com/openl-tablets/openl-tablets.git"
def openlTestsGitIrl = "https://github.com/dmitry-minchuk/java-taf-template.git"

def protocol_prefix = "https://"
def image_hub_registry = "ghcr.io/"
// ":x" used to be the floating "newest build" tag; the registry publishes "latest" instead.
def studio = "openl-tablets/webstudio:latest"
def ws = "openl-tablets/ws:latest-all"
// def demo = "openl-tablets/demo:latest"

class JenkinsNode {
    String nodeLabel
    String ip

    public JenkinsNode(String nodeLabel, String ip) {
        this.nodeLabel = nodeLabel
        this.ip = ip
    }
}

class JenkinsLabel {
    JenkinsNode master = new JenkinsNode("Jenkins_Master", "10.23.173.186")
    JenkinsNode slave1 = new JenkinsNode("Jenkins_Node_1", "10.23.173.187")
    JenkinsNode slave2SAML = new JenkinsNode("Jenkins_Node_2", "10.23.173.188")
}

class Job {
    String suiteName
    String studioImageName
    String wsImageName
    String containerAppPath
    String nodeToRunOn

    public Job(String suiteName, String studioImageName, String wsImageName, String containerAppPath, String nodeToRunOn) {
        this.suiteName = suiteName
        this.studioImageName = studioImageName
        this.wsImageName = wsImageName
        this.containerAppPath = containerAppPath
        this.nodeToRunOn = nodeToRunOn
    }
}

def jenkinsLabel = new JenkinsLabel()

// Dynamic node selection - Jenkins will pick any available node from the list
def anyAvailableNode = "${jenkinsLabel.master.nodeLabel} || ${jenkinsLabel.slave1.nodeLabel} || ${jenkinsLabel.slave2SAML.nodeLabel}"

def functionalJobList = [
                         // Regular test suites - run on any available node for optimal load balancing
                         new Job("studio_issues", image_hub_registry + studio, image_hub_registry + studio, "", anyAvailableNode),
                         new Job("studio_smoke", image_hub_registry + studio, image_hub_registry + ws, "", anyAvailableNode),
                         new Job("studio_acl", image_hub_registry + studio, image_hub_registry + ws, "", anyAvailableNode),
                         new Job("studio_rules_editor", image_hub_registry + studio, image_hub_registry + studio, "", anyAvailableNode),
                         new Job("studio_git", image_hub_registry + studio, image_hub_registry + studio, "", anyAvailableNode),
                         new Job("studio_sso", image_hub_registry + studio, image_hub_registry + studio, "", anyAvailableNode),
                         new Job("service_smoke", image_hub_registry + ws, image_hub_registry + ws, "", anyAvailableNode),
                         new Job("studio_open_api", image_hub_registry + studio, image_hub_registry + studio, "", anyAvailableNode)
                         ]
def jenkinsLabelList = [jenkinsLabel.master.nodeLabel, jenkinsLabel.slave1.nodeLabel, jenkinsLabel.slave2SAML.nodeLabel]

pipeline {
    agent {
        label jenkinsLabel.master.nodeLabel
    }
//     triggers {
//         parameterizedCron('H 3 * * * %APPLICATION_GIT_COMMIT_HASH_VERSION=;TESTS_BRANCH=playwright_testcontainers')
//     }
    options {
        throttleJobProperty categories: [], limitOneJobWithMatchingParams: false, maxConcurrentPerNode: 3, maxConcurrentTotal: 9, paramsToUseForLimit: '', throttleEnabled: true, throttleOption: 'project'
    }
    environment {
        // All env vars should be declared here and Jenkins System Env Vars should be disabled
        JAVA_HOME = "${env.JAVA_HOME}"
        M2_HOME = "${env.M2_HOME}"
        PATH = "${env.M2_HOME}/bin:${env.PATH}"
        PLAYWRIGHT_JAVA_SRC = "src/test/java:src/main/java"
    }
    parameters {
        string(name: 'APPLICATION_GIT_COMMIT_HASH_VERSION', defaultValue: '', description: 'Tested application version (openl-tablets). Special chars like : or | or [] not allowed here!')
        string(name: 'TESTS_BRANCH', defaultValue: 'main', description: 'Autotests repository branch')
        string(name: 'TESTS', defaultValue: '', description: 'Selective run. Leave EMPTY to run the full regression (all 8 suites of this job). To run only some tests, list their test CLASS names separated by commas or spaces: simple names (TestMethodTable) or fully qualified ones (tests.ui.webstudio.git.TestGitBranchSwitching). Individual @Test methods cannot be selected, the whole class runs. Every listed class is looked up in the 8 suite XML files this job runs (src/test/resources/testng_suites, the *_regression suites are not part of this job): it runs inside its own suite with that suite\'s Studio and Rule Services images, TestNG listeners, retry analyzer and ReportPortal launch name, so the results stay in the same RP history as the full regression; the RP launch gets the extra attribute run:selective. Suites that contain none of the listed classes only check out the repository and are skipped without starting Maven or containers. The build fails before pulling images if a name contains characters other than letters, digits, dots and underscores, and fails after the run if any listed class was not found in a suite (check the spelling). The Docker image pull stage still runs, so a selective run takes the pull time plus the selected tests instead of the whole regression.')
    }
    stages {
        stage('Validate Parameters') {
            steps {
                script {
                    selectedTests = (params.TESTS ?: '').split(/[\s,;]+/).findAll { it }
                    def invalidNames = selectedTests.findAll { !(it ==~ /[A-Za-z0-9_.]+/) }
                    if (!invalidNames.isEmpty()) {
                        error("TESTS accepts only class names made of letters, digits, dots and underscores, got: ${invalidNames}")
                    }
                    selectiveRun = !selectedTests.isEmpty()
                    selectedClassPattern = selectedTests.collect { it.contains('.') ? it.replace('.', '\\.') : '([^"]*\\.)?' + it }.join('|')
                    matchedClasses = []
                    rpRunAttribute = selectiveRun ? ';run:selective' : ''
                    if (selectiveRun) {
                        echo "Selective run requested for: ${selectedTests}"
                    }
                }
            }
        }
        stage('Pull Docker Images') {
            steps {
                script {
                    parallel jenkinsLabelList.collectEntries() { nodeLabel ->
                        [(nodeLabel): {
                            node(nodeLabel) {
                                deleteDir()
                                docker.withRegistry(protocol_prefix + image_hub_registry) {
                                  def studio_image = docker.image(studio)
                                  def ws_image = docker.image(ws)
                                  sh "docker system prune -f"
                                  // imageName() already carries the registry prefix inside withRegistry.
                                  sh "docker image rm -f ${studio_image.imageName()}"
                                  sh "docker image rm -f ${ws_image.imageName()}"
                                  sh "docker pull ${studio_image.imageName()}"
                                  sh "docker pull ${ws_image.imageName()}"
                                }
                            }
                        }]
                    }
                }
            }
        }
        stage('Clear settings.xml in .m2 if exists') {
            steps {
                script {
                    parallel jenkinsLabelList.collectEntries() { nodeLabel ->
                        [(nodeLabel): {
                            node(nodeLabel) {
                                def settingsFilePath = "${env.HOME}/.m2/settings.xml"
                                def fileExists = sh(script: "[ -f '${settingsFilePath}' ] && echo 'true' || echo 'false'", returnStdout: true).trim()
                                if (fileExists == 'true') {
                                    echo "settings.xml exists in .m2, deleting..."
                                    sh "rm -f '${settingsFilePath}'"
                                    echo "settings.xml deleted from .m2"
                                } else {
                                    echo "settings.xml does not exist in .m2, skipping deletion."
                                }
                            }
                        }]
                    }
                }
            }
        }
        stage('Run Test Suites') {
            steps {
                script {
                    // No `def`: binding-level so it survives CPS in parallel closures (else RP "build" attr is empty).
                    buildNumber = params.APPLICATION_GIT_COMMIT_HASH_VERSION
                    if (!buildNumber || buildNumber.trim().isEmpty()) {
                        def currentDate = new Date()
                        buildNumber = currentDate.format("MMM_dd_yyyy_HH_mm", TimeZone.getTimeZone('UTC'))
                        echo "APPLICATION_GIT_COMMIT_HASH_VERSION is empty, generated timestamp: ${buildNumber}"
                    }

                    parallel functionalJobList.collectEntries() { suite ->
                        [(suite): {
                            node(suite.nodeToRunOn) {
                                deleteDir()
                                checkout([
                                        $class: 'GitSCM',
                                        branches: [[name: params.TESTS_BRANCH]],
                                        extensions: [[$class: 'CloneOption', noTags: true, shallow: true, depth: 20, timeout: 30]],
                                        userRemoteConfigs: [[url: openlTestsGitIrl]]
                                ])
                                env.BUILD_NUMBER = buildNumber
                                env.TESTS_BRANCH = params.TESTS_BRANCH
                                echo "RP attributes will be: build:${env.BUILD_NUMBER};tests_branch:${env.TESTS_BRANCH}${rpRunAttribute}"
                                def suiteXmlOption = ''
                                if (selectiveRun) {
                                    def selectiveXml = "${pwd()}/selective_${suite.suiteName}.xml"
                                    def matched = sh(returnStdout: true, script: """
                                        SRC='src/test/resources/testng_suites/${suite.suiteName}.xml'
                                        OUT='${selectiveXml}'
                                        test -f "\$SRC"
                                        TESTS_TOTAL=\$(grep -c '<test ' "\$SRC" || true)
                                        TESTS_ONE_LINE=\$(grep '<test ' "\$SRC" | grep -c 'class name=' || true)
                                        if [ "\$TESTS_TOTAL" -ne "\$TESTS_ONE_LINE" ]; then
                                            echo "Every <test> of \$SRC must declare its class on the same line for the selective filter" >&2
                                            exit 1
                                        fi
                                        {
                                            grep -v '<test ' "\$SRC" | grep -v '</suite>' || true
                                            grep '<test ' "\$SRC" | grep -E 'class name="(${selectedClassPattern})"' || true
                                            echo '</suite>'
                                        } > "\$OUT"
                                        grep '<test ' "\$OUT" | grep -oE 'class name="[^"]*"' | sed 's/class name="//; s/"//' || true
                                    """).trim()
                                    def classes = matched ? matched.split(/\s+/).toList() : []
                                    if (classes.isEmpty()) {
                                        echo "Suite ${suite.suiteName} holds none of the selected tests, skipping"
                                        return
                                    }
                                    echo "Suite ${suite.suiteName} will run ${classes.size()} selected class(es): ${classes}"
                                    matchedClasses.addAll(classes)
                                    suiteXmlOption = "-DsuiteXmlFile=\"${selectiveXml}\""
                                }
                                // Testcontainers hard-codes a 2-min pull timeout — too short for ~700MB Keycloak on a cold agent.
                                if (suite.suiteName == "studio_sso") {
                                    sh '''
                                        for i in 1 2 3; do
                                            if docker pull quay.io/keycloak/keycloak:26.0; then
                                                echo "Keycloak image cached after $i attempt(s)"
                                                exit 0
                                            fi
                                            echo "Pull attempt $i failed, retrying in 10s..." >&2
                                            sleep 10
                                        done
                                        echo "Failed to pull Keycloak image after 3 attempts" >&2
                                        exit 1
                                    '''
                                }
                                // We need this because variables defined in Jenkins Global Properties are not straightfully accessible from Java and have to put them into the Maven process
                                withMaven() {
                                    sh("bash -lc 'git branch'")
                                    sh("""bash -lc '
                                        mvn clean test \\
                                            -Drp.endpoint=http://10.23.172.185:8080 \\
                                            -Dexecution.mode=PLAYWRIGHT_DOCKER \\
                                            -Drp.project=OpenL_Tests \\
                                            -Drp.launch=${suite.suiteName} \\
                                            -Drp.uuid=${RP_UUID} \\
                                            -Drp.attributes="build:${env.BUILD_NUMBER};tests_branch:${env.TESTS_BRANCH}${rpRunAttribute}" \\
                                            -Dsuite=${suite.suiteName} \\
                                            ${suiteXmlOption} \\
                                            -Ddeployed_app_path=${suite.containerAppPath} \\
                                            -Ddocker_image_name=${suite.studioImageName} \\
                                            -Dws_docker_image_name=${suite.wsImageName} \\
                                            -Dtestng.dtd.http=true \\
                                    '""")
                                }
                                publishHTML([allowMissing          : false,
                                             alwaysLinkToLastBuild : true,
                                             keepAll               : true,
                                             reportDir             : './target/surefire-reports',
                                             reportFiles           : 'index.html',
                                             reportName            : "HTML Report ${suite.suiteName}",
                                             reportTitles          : '',
                                             useWrapperFileDirectly: true])
                            }
                        }]
                    }
                    if (selectiveRun) {
                        def notFound = selectedTests.findAll { name ->
                            !matchedClasses.any { it == name || it.endsWith('.' + name) }
                        }
                        if (!notFound.isEmpty()) {
                            error("Selected tests not found in any suite of this job (check the spelling): ${notFound}")
                        }
                    }
                }
            }
        }
    }
}
