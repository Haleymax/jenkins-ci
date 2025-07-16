// Groovy Pipeline 示例脚本
// 这个文件展示了更多的Groovy语法和Jenkins Pipeline功能

@Library('jenkins-shared-library') _  // 引用共享库

// 定义全局变量
def projectConfig = [
    name: 'advanced-jenkins-pipeline',
    version: '2.0.0',
    dockerRegistry: 'registry.example.com',
    notifications: [
        slack: '#ci-cd',
        email: ['team@example.com']
    ]
]

// 定义自定义函数
def deployToEnvironment(environment, version) {
    echo "部署版本 ${version} 到 ${environment} 环境"
    
    // 模拟部署步骤
    def deploySteps = [
        'dev': { deployToDev(version) },
        'test': { deployToTest(version) },
        'staging': { deployToStaging(version) },
        'prod': { deployToProduction(version) }
    ]
    
    deploySteps[environment]()
}

def deployToDev(version) {
    echo "开发环境部署: ${version}"
    sh "kubectl apply -f k8s/dev/ --record"
}

def deployToTest(version) {
    echo "测试环境部署: ${version}"
    sh "kubectl apply -f k8s/test/ --record"
}

def deployToStaging(version) {
    echo "预发布环境部署: ${version}"
    sh "kubectl apply -f k8s/staging/ --record"
}

def deployToProduction(version) {
    echo "生产环境部署: ${version}"
    sh "kubectl apply -f k8s/prod/ --record"
}

// 自定义通知函数
def sendNotification(status, environment) {
    def color = status == 'SUCCESS' ? 'good' : 'danger'
    def message = """
    Pipeline 执行${status == 'SUCCESS' ? '成功' : '失败'}
    项目: ${projectConfig.name}
    环境: ${environment}
    构建号: ${env.BUILD_NUMBER}
    分支: ${env.GIT_BRANCH}
    """
    
    // 发送Slack通知
    slackSend(
        channel: projectConfig.notifications.slack,
        color: color,
        message: message
    )
}

// 检查代码质量的函数
def checkCodeQuality() {
    script {
        def sonarQubeResults = [:]
        
        try {
            // 运行SonarQube分析
            withSonarQubeEnv('SonarQube') {
                sh "mvn sonar:sonar -Dsonar.projectKey=${projectConfig.name}"
            }
            
            // 等待质量门检查
            timeout(time: 10, unit: 'MINUTES') {
                def qg = waitForQualityGate()
                sonarQubeResults.status = qg.status
                
                if (qg.status != 'OK') {
                    error "代码质量检查失败: ${qg.status}"
                }
            }
        } catch (Exception e) {
            echo "代码质量检查出错: ${e.getMessage()}"
            currentBuild.result = 'UNSTABLE'
        }
        
        return sonarQubeResults
    }
}

// 主Pipeline
pipeline {
    agent {
        kubernetes {
            yaml """
apiVersion: v1
kind: Pod
spec:
  containers:
  - name: maven
    image: maven:3.8.1-openjdk-11
    command:
    - cat
    tty: true
  - name: docker
    image: docker:latest
    command:
    - cat
    tty: true
    volumeMounts:
    - mountPath: /var/run/docker.sock
      name: docker-sock
  volumes:
  - name: docker-sock
    hostPath:
      path: /var/run/docker.sock
"""
        }
    }
    
    // 选项配置
    options {
        buildDiscarder(logRotator(numToKeepStr: '10'))  // 保留最近10次构建
        timeout(time: 30, unit: 'MINUTES')              // 30分钟超时
        retry(3)                                         // 失败时重试3次
        timestamps()                                     // 显示时间戳
    }
    
    // 触发器
    triggers {
        cron('H 2 * * 1-5')  // 工作日凌晨2点触发
        githubPush()         // GitHub push触发
    }
    
    // 参数
    parameters {
        choice(
            name: 'DEPLOY_ENV',
            choices: ['none', 'dev', 'test', 'staging', 'prod'],
            description: '选择部署环境'
        )
        string(
            name: 'TAG_VERSION',
            defaultValue: "${projectConfig.version}",
            description: '标签版本'
        )
        booleanParam(
            name: 'RUN_SECURITY_SCAN',
            defaultValue: true,
            description: '运行安全扫描'
        )
        booleanParam(
            name: 'PUSH_TO_REGISTRY',
            defaultValue: false,
            description: '推送到Docker仓库'
        )
    }
    
    // 环境变量
    environment {
        DOCKER_IMAGE = "${projectConfig.dockerRegistry}/${projectConfig.name}"
        KUBECONFIG = credentials('kubeconfig')
        DOCKER_REGISTRY_CREDS = credentials('docker-registry')
    }
    
    stages {
        stage('初始化') {
            steps {
                script {
                    // 打印构建信息
                    echo """
                    🚀 开始构建 ${projectConfig.name}
                    📦 版本: ${params.TAG_VERSION}
                    🌍 目标环境: ${params.DEPLOY_ENV}
                    🔧 构建号: ${env.BUILD_NUMBER}
                    📅 构建时间: ${new Date()}
                    """
                    
                    // 设置构建描述
                    currentBuild.description = "Version: ${params.TAG_VERSION}, Env: ${params.DEPLOY_ENV}"
                    
                    // 检查分支保护
                    if (env.GIT_BRANCH == 'main' && params.DEPLOY_ENV == 'prod') {
                        echo "⚠️ 主分支生产环境部署需要额外验证"
                    }
                }
            }
        }
        
        stage('代码检出') {
            steps {
                checkout scm
                script {
                    // 获取详细的Git信息
                    env.GIT_COMMIT_FULL = sh(returnStdout: true, script: 'git rev-parse HEAD').trim()
                    env.GIT_AUTHOR = sh(returnStdout: true, script: 'git log -1 --pretty=format:"%an"').trim()
                    env.GIT_MESSAGE = sh(returnStdout: true, script: 'git log -1 --pretty=format:"%s"').trim()
                    
                    echo "📝 最新提交: ${env.GIT_MESSAGE}"
                    echo "👤 提交者: ${env.GIT_AUTHOR}"
                }
            }
        }
        
        stage('依赖检查') {
            parallel {
                stage('Maven依赖') {
                    when { fileExists 'pom.xml' }
                    steps {
                        container('maven') {
                            sh 'mvn dependency:resolve'
                            sh 'mvn dependency:analyze'
                        }
                    }
                }
                stage('NPM依赖') {
                    when { fileExists 'package.json' }
                    steps {
                        sh 'npm ci'
                        sh 'npm audit --audit-level=high'
                    }
                }
                stage('安全漏洞扫描') {
                    when { params.RUN_SECURITY_SCAN }
                    steps {
                        script {
                            try {
                                sh 'safety check --json > security-report.json'
                            } catch (Exception e) {
                                echo "安全扫描发现问题: ${e.getMessage()}"
                                currentBuild.result = 'UNSTABLE'
                            }
                        }
                    }
                }
            }
        }
        
        stage('构建和测试') {
            parallel {
                stage('编译') {
                    steps {
                        container('maven') {
                            sh 'mvn clean compile'
                        }
                    }
                }
                stage('单元测试') {
                    steps {
                        container('maven') {
                            sh 'mvn test'
                        }
                        publishTestResults testResultsPattern: 'target/surefire-reports/*.xml'
                    }
                }
                stage('集成测试') {
                    steps {
                        container('maven') {
                            sh 'mvn integration-test'
                        }
                    }
                }
            }
        }
        
        stage('代码质量') {
            steps {
                script {
                    def qualityResults = checkCodeQuality()
                    echo "代码质量检查完成: ${qualityResults.status}"
                }
            }
        }
        
        stage('构建镜像') {
            when {
                anyOf {
                    environment name: 'DEPLOY_ENV', value: 'test'
                    environment name: 'DEPLOY_ENV', value: 'staging'
                    environment name: 'DEPLOY_ENV', value: 'prod'
                }
            }
            steps {
                container('docker') {
                    script {
                        def imageTag = "${env.DOCKER_IMAGE}:${params.TAG_VERSION}"
                        
                        // 构建Docker镜像
                        sh "docker build -t ${imageTag} ."
                        
                        // 镜像安全扫描
                        if (params.RUN_SECURITY_SCAN) {
                            sh "docker run --rm -v /var/run/docker.sock:/var/run/docker.sock aquasec/trivy:latest image ${imageTag}"
                        }
                        
                        // 推送到仓库
                        if (params.PUSH_TO_REGISTRY) {
                            withCredentials([usernamePassword(credentialsId: 'docker-registry', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')]) {
                                sh "docker login ${projectConfig.dockerRegistry} -u $USERNAME -p $PASSWORD"
                                sh "docker push ${imageTag}"
                            }
                        }
                    }
                }
            }
        }
        
        stage('部署') {
            when {
                not { environment name: 'DEPLOY_ENV', value: 'none' }
            }
            steps {
                script {
                    def approvalRequired = params.DEPLOY_ENV in ['staging', 'prod']
                    
                    if (approvalRequired) {
                        def approvers = params.DEPLOY_ENV == 'prod' ? 'admin,lead' : 'dev-team'
                        
                        input(
                            message: "确认部署到 ${params.DEPLOY_ENV} 环境?",
                            ok: '部署',
                            submitterParameter: 'APPROVER',
                            parameters: [
                                choice(name: 'DEPLOY_STRATEGY', choices: ['rolling', 'blue-green', 'canary'], description: '部署策略')
                            ]
                        )
                        
                        echo "部署已被 ${env.APPROVER} 批准，使用 ${env.DEPLOY_STRATEGY} 策略"
                    }
                    
                    // 执行部署
                    deployToEnvironment(params.DEPLOY_ENV, params.TAG_VERSION)
                    
                    // 部署后验证
                    sleep(10)  // 等待服务启动
                    sh "curl -f http://${params.DEPLOY_ENV}-api.example.com/health || exit 1"
                }
            }
        }
        
        stage('性能测试') {
            when {
                anyOf {
                    environment name: 'DEPLOY_ENV', value: 'test'
                    environment name: 'DEPLOY_ENV', value: 'staging'
                }
            }
            steps {
                script {
                    echo "执行性能测试..."
                    
                    // 使用JMeter进行性能测试
                    sh """
                    jmeter -n -t performance-test.jmx \
                           -l performance-results.jtl \
                           -e -o performance-report/
                    """
                    
                    // 分析性能结果
                    def performanceThreshold = [
                        'averageResponseTime': 500,  // 毫秒
                        'errorRate': 1               // 百分比
                    ]
                    
                    // 这里可以添加性能结果分析逻辑
                    publishHTML([
                        allowMissing: false,
                        alwaysLinkToLastBuild: true,
                        keepAll: true,
                        reportDir: 'performance-report',
                        reportFiles: 'index.html',
                        reportName: 'Performance Report'
                    ])
                }
            }
        }
    }
    
    post {
        always {
            script {
                // 收集构建制品
                archiveArtifacts artifacts: 'target/*.jar', allowEmptyArchive: true
                
                // 发布测试结果
                publishTestResults testResultsPattern: 'target/surefire-reports/*.xml'
                
                // 清理工作空间
                cleanWs()
                
                echo "🏁 Pipeline 执行完成"
            }
        }
        
        success {
            script {
                sendNotification('SUCCESS', params.DEPLOY_ENV)
                
                if (params.DEPLOY_ENV == 'prod') {
                    echo "🎉 生产环境部署成功!"
                    
                    // 创建Git标签
                    sh "git tag -a v${params.TAG_VERSION} -m 'Release version ${params.TAG_VERSION}'"
                    sh "git push origin v${params.TAG_VERSION}"
                }
            }
        }
        
        failure {
            script {
                sendNotification('FAILURE', params.DEPLOY_ENV)
                
                echo "❌ Pipeline 执行失败"
                echo "请检查日志并修复问题"
                
                // 如果是生产环境失败，发送紧急通知
                if (params.DEPLOY_ENV == 'prod') {
                    emailext(
                        subject: "🚨 生产环境部署失败 - ${projectConfig.name}",
                        body: "生产环境部署失败，请立即检查！构建号: ${env.BUILD_NUMBER}",
                        to: "${projectConfig.notifications.email.join(',')}"
                    )
                }
            }
        }
        
        unstable {
            script {
                echo "⚠️ Pipeline 执行不稳定"
                echo "某些测试失败或存在质量问题"
            }
        }
        
        aborted {
            script {
                echo "🛑 Pipeline 被中止"
                sendNotification('ABORTED', params.DEPLOY_ENV)
            }
        }
    }
}
