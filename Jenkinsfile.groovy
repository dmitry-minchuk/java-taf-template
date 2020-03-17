pipeline {
  agent any
  stages {
    stage('test') {
      steps {
        sh '${MVN_COMMAND}'
      }
    }
  }
}