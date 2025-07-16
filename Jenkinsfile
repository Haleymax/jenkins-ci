pipeline {
    agent any  // 在任何可用代理上运行
    
    stages {
        stage('Checkout') {
            steps {
                // 自动检出当前触发构建的代码
                checkout scm
            }
        }
        
        stage('Greet') {
            steps {
                echo 'hello jenkins from SCM!'
            }
        }
    }
    
    post {
        always {
            echo 'Pipeline completed'
        }
    }
}