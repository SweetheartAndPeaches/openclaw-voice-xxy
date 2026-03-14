# 部署指南 - 口播网站

## 项目状态
**当前状态**: 所有核心功能已开发完成，等待网络基础设施问题解决后进行集成测试。

**阻塞问题**: 无法访问 Docker registry（网络连接超时），即使配置了镜像仓库。

## 技术架构
- **后端**: Spring Boot + PostgreSQL + Redis + JWT + Stripe
- **前端**: React + TypeScript + Vite + PWA + Responsive Design
- **AI 服务**: Coze Voice Gen API + 音色克隆
- **部署**: Docker Compose

## 目录结构
```
podcast-website/
├── backend/           # 后端服务
│   ├── src/main/java/com/podcast/voice/
│   │   ├── controller/    # API 控制器
│   │   ├── entity/        # 数据实体
│   │   ├── repository/    # 数据访问层
│   │   ├── service/       # 业务逻辑层
│   │   ├── security/      # 安全配置
│   │   └── config/        # 配置类
│   └── pom.xml           # Maven 依赖
├── frontend/          # 前端应用
│   ├── src/
│   │   ├── components/    # React 组件
│   │   ├── hooks/         # 自定义 Hook
│   │   ├── store/         # 状态管理
│   │   ├── App.tsx        # 主应用组件
│   │   └── main.tsx       # 入口文件
│   └── package.json      # NPM 依赖
└── deployment/        # 部署配置
    ├── docker-compose.yml
    ├── Dockerfile.backend
    └── Dockerfile.frontend
```

## 已实现功能
### ✅ 用户认证系统
- JWT Token 认证
- 用户注册/登录
- 密码加密存储
- 角色权限管理

### ✅ 计费系统集成
- Stripe 支付集成
- 订阅管理
- 用量跟踪和配额控制
- Webhook 事件处理

### ✅ 音色克隆功能
- 音频上传和处理
- 自定义音色生成
- 语音任务管理
- Coze Voice Gen API 集成

### ✅ 移动端适配
- PWA 支持
- 响应式设计
- 移动端优化
- Touch 手势支持

## 部署步骤

### 1. 环境准备
```bash
# 安装 Docker 和 Docker Compose
sudo apt update
sudo apt install docker.io docker-compose

# 验证安装
docker --version
docker-compose --version
```

### 2. 配置环境变量
创建 `.env` 文件：
```env
# 数据库配置
DB_USERNAME=postgres
DB_PASSWORD=postgres

# Stripe 配置
STRIPE_SECRET_KEY=your_stripe_secret_key
STRIPE_PUBLISHABLE_KEY=your_stripe_publishable_key

# JWT 配置
JWT_SECRET=your_jwt_secret_key
JWT_EXPIRATION=86400

# Coze API 配置
COZE_API_KEY=your_coze_api_key
```

### 3. 构建和启动服务
```bash
cd deployment
docker-compose up --build
```

## 网络问题排查

### 当前问题
- 无法访问 `registry-1.docker.io`
- 即使配置了镜像仓库 (`https://docker.mirrors.ustc.edu.cn/`, `https://hub-mirror.c.163.com/`)
- 网络连接完全被阻断

### 解决方案
1. **检查网络防火墙设置**
   ```bash
   # 检查 iptables 规则
   sudo iptables -L
   
   # 检查 ufw 状态
   sudo ufw status
   ```

2. **验证 DNS 解析**
   ```bash
   nslookup registry-1.docker.io
   dig registry-1.docker.io
   ```

3. **测试代理设置**
   ```bash
   # 检查是否需要代理
   echo $HTTP_PROXY
   echo $HTTPS_PROXY
   
   # 如果需要，配置 Docker 代理
   mkdir -p /etc/systemd/system/docker.service.d
   cat > /etc/systemd/system/docker.service.d/http-proxy.conf << EOF
   [Service]
   Environment="HTTP_PROXY=http://proxy.example.com:8080"
   Environment="HTTPS_PROXY=http://proxy.example.com:8080"
   EOF
   
   systemctl daemon-reload
   systemctl restart docker
   ```

4. **使用离线镜像**
   - 在有网络的机器上拉取所需镜像
   - 使用 `docker save` 导出镜像
   - 在目标机器上使用 `docker load` 导入镜像

5. **手动构建基础镜像**
   ```bash
   # 创建本地 alpine 基础镜像
   docker build -t local/alpine:latest - << EOF
   FROM scratch
   ADD https://dl-cdn.alpinelinux.org/alpine/v3.18/releases/x86_64/alpine-minirootfs-3.18.4-x86_64.tar.gz /
   EOF
   ```

## 测试脚本
一旦网络问题解决，运行以下测试脚本验证完整功能：

```bash
# 部署测试脚本
cd deployment
python3 test_docker_compose.py
```

## 故障排除
- **Docker Compose 启动失败**: 检查端口冲突 (8080, 3000, 5432, 6379)
- **数据库连接失败**: 验证 PostgreSQL 配置和网络
- **API 调用失败**: 检查 Coze API 密钥和网络连接
- **前端无法加载**: 验证 Nginx 配置和静态资源路径

## 联系支持
如果遇到无法解决的问题，请联系：
- **徐旭尧** (CEO/后端): 负责后端服务和计费系统
- **虾天尊** (合伙人/前端+DevOps): 负责前端、部署和AI集成

---
**最后更新**: 2026-03-14  
**项目状态**: 功能完成，等待网络修复 🦐