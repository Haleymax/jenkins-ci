# Jenkins Pipeline 配置说明

## 主要特性

### 1. Jenkinsfile 功能
- **参数化构建**: 支持环境选择、版本号、跳过测试等参数
- **触发器**: 定时触发和SCM轮询触发
- **并行执行**: 测试阶段并行运行单元测试和集成测试
- **条件执行**: 根据参数和环境条件执行不同阶段
- **审批机制**: 生产环境部署需要人工确认
- **Groovy脚本**: 使用Groovy语法处理复杂逻辑

### 2. 高级Groovy Pipeline功能 (groovy-pipeline-example.groovy)
- **共享库**: 使用Jenkins共享库
- **Kubernetes Agent**: 在Kubernetes Pod中运行
- **Docker集成**: 构建和推送Docker镜像
- **安全扫描**: 依赖漏洞和镜像安全扫描
- **性能测试**: JMeter性能测试集成
- **通知系统**: Slack和邮件通知
- **质量门**: SonarQube代码质量检查

## 使用方法

### 快速开始
1. 将 `Jenkinsfile` 放在你的Git仓库根目录
2. 在Jenkins中创建Pipeline任务
3. 配置SCM指向你的Git仓库
4. 保存并运行

### 参数说明
- **ENVIRONMENT**: 选择部署环境 (dev/test/staging/prod)
- **VERSION**: 指定版本号
- **SKIP_TESTS**: 是否跳过测试阶段
- **RELEASE_NOTES**: 发布说明文本

### 触发方式
1. **手动触发**: 在Jenkins界面手动构建
2. **定时触发**: 每天凌晨2点自动触发
3. **代码推送**: 检测到代码变化时触发
4. **Webhook**: 配置Git仓库webhook实时触发

## 环境要求

### 基础要求
- Jenkins 2.x+
- Git插件
- Pipeline插件

### 高级功能要求
- Kubernetes插件 (用于Kubernetes Agent)
- Docker插件
- SonarQube插件
- Slack插件
- Performance插件

## 自定义配置

### 修改触发时间
```groovy
triggers {
    cron('0 2 * * *')  // 每天凌晨2点
    // cron('H/15 * * * *')  // 每15分钟
    // cron('0 9-17 * * 1-5')  // 工作日9-17点每小时
}
```

### 添加新的部署环境
在parameters部分添加新的选择项：
```groovy
choice(
    name: 'ENVIRONMENT',
    choices: ['dev', 'test', 'staging', 'prod', 'demo'],  // 添加demo环境
    description: '选择部署环境'
)
```

### 自定义通知
修改post部分的通知逻辑：
```groovy
success {
    script {
        // 添加企业微信通知
        // 添加钉钉通知
        // 添加其他通知方式
    }
}
```

## 最佳实践

1. **安全**: 使用Jenkins Credentials存储敏感信息
2. **版本控制**: 将Jenkinsfile纳入版本控制
3. **测试**: 在非生产环境充分测试Pipeline
4. **监控**: 设置适当的通知和监控
5. **文档**: 保持Pipeline文档更新

## 故障排除

### 常见问题
1. **权限问题**: 确保Jenkins有足够权限访问Git仓库
2. **插件缺失**: 安装必要的Jenkins插件
3. **环境变量**: 检查环境变量配置
4. **超时问题**: 适当设置timeout时间

### 调试技巧
- 使用 `echo` 输出调试信息
- 检查Jenkins系统日志
- 使用Pipeline语法生成器验证语法
- 分阶段测试Pipeline功能
