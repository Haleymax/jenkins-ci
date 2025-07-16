pipeline {
    agent any  // 在任何可用代理上运行
    
    // 配置触发器
    triggers {
        // 每天凌晨2点触发构建
        cron('0 2 * * *')
        // 当有新的提交推送到主分支时触发
        pollSCM('H/5 * * * *')  // 每5分钟检查一次SCM变化
    }
    
    // 参数化构建
    parameters {
        choice(
            name: 'ENVIRONMENT',
            choices: ['dev', 'test', 'staging', 'prod'],
            description: '选择部署环境'
        )
        string(
            name: 'VERSION',
            defaultValue: '1.0.0',
            description: '版本号'
        )
        booleanParam(
            name: 'SKIP_TESTS',
            defaultValue: false,
            description: '是否跳过测试阶段'
        )
        text(
            name: 'RELEASE_NOTES',
            defaultValue: '',
            description: '发布说明'
        )
    }
    
    // 环境变量
    environment {
        APP_NAME = 'jenkins-test-app'
        BUILD_TIME = "${new Date().format('yyyy-MM-dd HH:mm:ss')}"
    }
    
    stages {
        stage('准备阶段') {
            steps {
                script {
                    // Groovy脚本示例
                    echo "开始构建 ${env.APP_NAME}"
                    echo "构建时间: ${env.BUILD_TIME}"
                    echo "目标环境: ${params.ENVIRONMENT}"
                    echo "版本号: ${params.VERSION}"
                    
                    // 检查参数
                    if (params.ENVIRONMENT == 'prod' && params.VERSION.contains('SNAPSHOT')) {
                        error('生产环境不能使用SNAPSHOT版本!')
                    }
                }
            }
        }
        
        stage('Checkout') {
            steps {
                // 自动检出当前触发构建的代码
                checkout scm
                script {
                    // 获取Git信息 (跨平台兼容)
                    try {
                        echo "正在获取Git信息..."
                        // 使用Jenkins内置的Git信息
                        env.GIT_COMMIT_SHORT = env.GIT_COMMIT?.take(8) ?: 'unknown'
                        env.GIT_BRANCH_NAME = env.GIT_BRANCH ?: 'unknown'
                        echo "Git分支: ${env.GIT_BRANCH_NAME}"
                        echo "Git提交: ${env.GIT_COMMIT_SHORT}"
                    } catch (Exception e) {
                        echo "获取Git信息时出现问题: ${e.getMessage()}"
                        env.GIT_COMMIT_SHORT = 'unknown'
                        env.GIT_BRANCH_NAME = 'unknown'
                    }
                }
            }
        }
        
        stage('构建') {
            steps {
                script {
                    echo "正在构建项目..."
                    // 模拟构建过程
                    sleep(2)
                    echo "构建完成!"
                }
            }
        }
        
        stage('测试') {
            when {
                expression { !params.SKIP_TESTS }
            }
            parallel {
                stage('单元测试') {
                    steps {
                        script {
                            echo "运行单元测试..."
                            // 模拟测试
                            sleep(3)
                            echo "单元测试通过!"
                        }
                    }
                }
                stage('集成测试') {
                    steps {
                        script {
                            echo "运行集成测试..."
                            // 模拟测试
                            sleep(2)
                            echo "集成测试通过!"
                        }
                    }
                }
            }
        }
        
        stage('代码质量检查') {
            when {
                expression { 
                    params.ENVIRONMENT == 'test' || 
                    params.ENVIRONMENT == 'staging' || 
                    params.ENVIRONMENT == 'prod' 
                }
            }
            steps {
                script {
                    echo "执行代码质量检查..."
                    // 模拟代码质量检查
                    def qualityGate = [
                        'coverage': 85,
                        'duplicated_lines': 3,
                        'security_rating': 'A'
                    ]
                    echo "代码覆盖率: ${qualityGate.coverage}%"
                    echo "重复代码行: ${qualityGate.duplicated_lines}%"
                    echo "安全评级: ${qualityGate.security_rating}"
                }
            }
        }
        
        stage('部署') {
            when {
                expression { 
                    params.ENVIRONMENT == 'test' || 
                    params.ENVIRONMENT == 'staging' || 
                    params.ENVIRONMENT == 'prod' 
                }
            }
            steps {
                script {
                    echo "部署到 ${params.ENVIRONMENT} 环境..."
                    
                    // 根据环境执行不同的部署逻辑
                    switch(params.ENVIRONMENT) {
                        case 'test':
                            echo "部署到测试环境"
                            break
                        case 'staging':
                            echo "部署到预发布环境"
                            // 可能需要审批
                            try {
                                timeout(time: 5, unit: 'MINUTES') {
                                    input message: '确认部署到预发布环境?', ok: '确认'
                                }
                            } catch (Exception e) {
                                echo "部署审批超时或被取消"
                                error("部署被取消")
                            }
                            break
                        case 'prod':
                            echo "部署到生产环境"
                            // 生产环境需要审批
                            try {
                                timeout(time: 10, unit: 'MINUTES') {
                                    input message: '确认部署到生产环境?', ok: '确认', 
                                          submitterParameter: 'APPROVER'
                                }
                                echo "部署已被 ${env.APPROVER ?: '未知用户'} 批准"
                            } catch (Exception e) {
                                echo "生产环境部署审批超时或被取消"
                                error("生产环境部署被取消")
                            }
                            break
                        default:
                            echo "跳过部署阶段"
                    }
                    
                    echo "部署完成!"
                }
            }
        }
        
        stage('通知') {
            steps {
                script {
                    def message = """
                    🎉 构建完成!
                    项目: ${env.APP_NAME}
                    版本: ${params.VERSION}
                    环境: ${params.ENVIRONMENT}
                    分支: ${env.GIT_BRANCH_NAME}
                    提交: ${env.GIT_COMMIT_SHORT}
                    构建时间: ${env.BUILD_TIME}
                    """
                    
                    if (params.RELEASE_NOTES && params.RELEASE_NOTES.trim()) {
                        message += "\n发布说明:\n${params.RELEASE_NOTES}"
                    }
                    
                    echo message
                }
            }
        }
    }
    
    post {
        always {
            script {
                echo "Pipeline 执行完成"
                // 清理工作空间
                try {
                    cleanWs()
                } catch (Exception e) {
                    echo "清理工作空间时出现问题: ${e.getMessage()}"
                }
            }
        }
        success {
            script {
                echo "✅ 构建成功!"
                // 这里可以添加成功通知逻辑
                if (params.ENVIRONMENT == 'prod') {
                    echo "🚀 生产环境部署成功!"
                }
            }
        }
        failure {
            script {
                echo "❌ 构建失败!"
                // 这里可以添加失败通知逻辑
                echo "请检查构建日志并修复问题"
            }
        }
        unstable {
            script {
                echo "⚠️ 构建不稳定"
                echo "某些测试可能失败或有警告"
            }
        }
        aborted {
            script {
                echo "🛑 构建被中止"
                echo "Pipeline执行被用户或系统中止"
            }
        }
    }
}