package main.jenkins

def openlTabletsGitIrl = "https://github.com/openl-tablets/openl-tablets.git"
def openlTestsGitIrl = "https://github.com/dmitry-minchuk/java-taf-template.git"

def protocol_prefix = "https://"
def image_hub_registry = "ghcr.io/"
def studio = "openl-tablets/webstudio:x"
def ws = "openl-tablets/ws:x-all"
// def demo = "openl-tablets/demo:x"

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
    String imageName
    String containerAppPath
    String nodeToRunOn

    public Job(String suiteName, String imageName, String containerAppPath, String nodeToRunOn) {
        this.suiteName = suiteName
        this.imageName = imageName
        this.containerAppPath = containerAppPath
        this.nodeToRunOn = nodeToRunOn
    }
}

def jenkinsLabel = new JenkinsLabel()
def functionalJobList = [new Job("studio_issues", image_hub_registry + studio, "", jenkinsLabel.master.nodeLabel),
                         new Job("studio_smoke", image_hub_registry + studio, "", jenkinsLabel.slave1.nodeLabel),
                         new Job("studio_rules_editor", image_hub_registry + studio, "", jenkinsLabel.slave2SAML.nodeLabel)]
def jenkinsLabelList = [jenkinsLabel.master.nodeLabel, jenkinsLabel.slave1.nodeLabel, jenkinsLabel.slave2SAML.nodeLabel]

pipeline {
    agent {
        label jenkinsLabel.master.nodeLabel
    }
    triggers {
        parameterizedCron('H 3 * * * %APPLICATION_GIT_COMMIT_HASH_VERSION=latest_nightly_run;TESTS_BRANCH=selenium_testcontainers')
    }
    options {
        throttleJobProperty categories: [], limitOneJobWithMatchingParams: false, maxConcurrentPerNode: 3, maxConcurrentTotal: 9, paramsToUseForLimit: '', throttleEnabled: true, throttleOption: 'project'
    }
    environment {
        // All env vars should be declared here and Jenkins System Env Vars should be disabled
        JAVA_HOME = "${env.JAVA_HOME}"
        M2_HOME = "${env.M2_HOME}"
        PATH = "${env.M2_HOME}/bin:${env.PATH}"
    }
    parameters {
        string(name: 'APPLICATION_GIT_COMMIT_HASH_VERSION', defaultValue: 'latest_nightly_run', description: 'Source application branch (openl-tablets)')
        string(name: 'TESTS_BRANCH', defaultValue: 'selenium_testcontainers', description: 'Autotests branch (openl-tests)')
    }
    stages {
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
//                                   def demo_image = docker.image(demo)
                                  sh "docker system prune -f"
                                  sh "docker image rm -f ghcr.io/${studio_image.imageName()}"
                                  sh "docker image rm -f ghcr.io/${ws_image.imageName()}"
//                                   sh "docker image rm -f ghcr.io/${demo_image.imageName()}"
                                  sh "docker pull ${studio_image.imageName()}"
                                  sh "docker pull ${ws_image.imageName()}"
//                                   sh "docker pull ${demo_image.imageName()}"
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
                                env.BUILD_NUMBER = params.APPLICATION_GIT_COMMIT_HASH_VERSION
                                withMaven() {
                                    sh("bash -lc 'git branch'")
                                    sh("""bash -lc '
                                        mvn clean test \\
                                            -Drp.endpoint=http://10.23.172.185:8080 \\
                                            -Drp.project=OpenL_Tests \\
                                            -Drp.launch=${suite.suiteName} \\
                                            -Drp.uuid=${RP_UUID} \\
                                            -Drp.attributes="build:${APPLICATION_GIT_COMMIT_HASH_VERSION};tests_branch:${TESTS_BRANCH}" \\
                                            -Dsuite=${suite.suiteName} \\
                                            -Ddeployed_app_path=${suite.containerAppPath} \\
                                            -Ddocker_image_name=${suite.imageName} \\
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
                }
            }
        }
    }
}