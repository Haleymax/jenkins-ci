# Jenkins Pipeline 测试项目

这是一个用于测试Jenkins Pipeline功能的示例项目，包含了丰富的CI/CD流程和Groovy脚本示例。

## 📁 项目结构

```
jenkins-ci/
├── Jenkinsfile                      # 基础Pipeline配置
├── Jenkinsfile.simple               # 简化版本Pipeline配置
├── groovy-pipeline-example.groovy   # 高级Groovy Pipeline示例
├── PIPELINE_GUIDE.md               # Pipeline使用指南
└── README.md                       # 项目说明
```

## 🚀 功能特性

### 基础Pipeline (Jenkinsfile)
- ✅ 参数化构建
- ✅ 定时和SCM触发
- ✅ 并行测试执行
- ✅ 条件部署
- ✅ 审批流程
- ✅ 环境变量管理
- ✅ 构建通知

### 高级Pipeline (groovy-pipeline-example.groovy)
- 🔧 Kubernetes Agent支持
- 🐳 Docker集成
- 🔒 安全扫描
- 📊 代码质量检查
- 🚀 多环境部署
- 📈 性能测试
- 💬 多渠道通知
- 🏷️ 自动标签管理

## 🛠️ 快速开始

1. **克隆项目**
   ```bash
   git clone <your-repo-url>
   cd jenkins-ci
   ```

2. **在Jenkins中创建Pipeline任务**
   - 新建Pipeline项目
   - 配置SCM指向你的Git仓库
   - 保存并运行

3. **参数配置**
   - ENVIRONMENT: 选择目标环境
   - VERSION: 设置版本号
   - SKIP_TESTS: 是否跳过测试
   - RELEASE_NOTES: 发布说明

## 📋 使用场景

### 开发环境
- 代码提交自动触发构建
- 快速反馈和调试

### 测试环境  
- 自动化测试执行
- 代码质量检查
- 性能测试

### 生产环境
- 审批流程控制
- 安全扫描验证
- 回滚机制

## 🔧 自定义配置

详细的配置说明请参考 [PIPELINE_GUIDE.md](./PIPELINE_GUIDE.md)

## 🔧 故障排除

### 常见语法错误修复

如果遇到Jenkins Pipeline语法错误，可以使用简化版本：

1. **使用简化版本**: `Jenkinsfile.simple` - 包含基本功能，语法简单
2. **检查Jenkins版本**: 确保Jenkins版本支持Pipeline语法
3. **验证插件**: 确保安装了必要的Pipeline插件

### 版本说明

- **Jenkinsfile**: 完整功能版本，包含所有高级特性
- **Jenkinsfile.simple**: 简化版本，适合基础Jenkins环境
- **groovy-pipeline-example.groovy**: 高级示例，需要额外插件支持

### 快速测试

如果主Jenkinsfile出现问题，可以：
1. 临时重命名 `Jenkinsfile` 为 `Jenkinsfile.backup`
2. 复制 `Jenkinsfile.simple` 为 `Jenkinsfile`
3. 提交代码测试基本功能

## 📞 支持

如有问题或建议，请创建Issue或联系维护团队。

---

💡 **提示**: 这个项目是学习Jenkins Pipeline的绝佳起点，包含了企业级CI/CD的最佳实践！
