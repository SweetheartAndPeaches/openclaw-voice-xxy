# 前端开发文档

## 目录
- [开发环境搭建](#开发环境搭建)
- [依赖安装](#依赖安装)
- [运行说明](#运行说明)
- [代码规范](#代码规范)
- [测试说明](#测试说明)
- [部署流程](#部署流程)
- [常见问题](#常见问题)

## 开发环境搭建

### 系统要求
- Node.js 18.x 或更高版本
- npm 8.x 或更高版本
- Git
- Docker (可选，用于容器化开发)

### 安装步骤

#### 1. 克隆项目
```bash
git clone https://github.com/SweetheartAndPeaches/openclaw-voice-xxy.git
cd openclaw-voice-xxy/frontend
```

#### 2. 安装 Node.js
推荐使用 [nvm](https://github.com/nvm-sh/nvm) 管理 Node.js 版本：

```bash
# 安装 nvm
curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.39.0/install.sh | bash

# 安装 Node.js 18
nvm install 18
nvm use 18
```

#### 3. 验证环境
```bash
node --version  # 应显示 v18.x.x
npm --version   # 应显示 8.x.x
```

## 依赖安装

### 安装项目依赖
```bash
# 安装所有依赖
npm install

# 或者使用 yarn (如果已安装)
yarn install
```

### 依赖说明
| 依赖类型 | 包名 | 用途 |
|---------|------|------|
| **核心依赖** | react, react-dom | React 核心库 |
| | react-router-dom | 路由管理 |
| | howler | 音频播放 |
| | zustand | 状态管理 |
| **开发依赖** | typescript | TypeScript 支持 |
| | vite | 构建工具和开发服务器 |
| | @types/* | TypeScript 类型定义 |

## 运行说明

### 开发模式
```bash
# 启动开发服务器
npm run dev

# 默认地址: http://localhost:3000
```

开发服务器特性：
- 热重载 (HMR)
- 自动打开浏览器
- API 代理到后端 (`/api` → `http://localhost:8080`)

### 生产构建
```bash
# 构建生产版本
npm run build

# 输出目录: dist/
```

### 预览生产版本
```bash
# 在本地预览生产构建
npm run preview

# 默认地址: http://localhost:4173
```

## 代码规范

### 项目结构
```
frontend/
├── src/
│   ├── components/     # React 组件
│   ├── store/          # Zustand 状态管理
│   ├── utils/          # 工具函数
│   ├── assets/         # 静态资源
│   ├── App.tsx         # 主应用组件
│   └── main.tsx        # 应用入口
├── public/             # 公共静态文件
├── dist/               # 构建输出目录
└── ...
```

### TypeScript 规范
- 所有组件必须使用 TypeScript
- 接口命名使用 PascalCase
- Props 必须定义接口
- 避免使用 `any` 类型

```typescript
// 示例：组件 Props 接口
interface AudioPlayerProps {
  src: string;
  title?: string;
  onPlay?: () => void;
  onPause?: () => void;
}
```

### CSS 规范
- 使用 CSS Modules 避免样式冲突
- 类名使用 kebab-case
- 组件样式文件与组件同名

```css
/* AudioPlayer.module.css */
.audio-player {
  /* 样式定义 */
}
```

### Git 提交规范
使用 [Conventional Commits](https://www.conventionalcommits.org/) 格式：

```
feat: 添加新功能
fix: 修复 bug
docs: 文档更新
style: 代码格式调整
refactor: 代码重构
test: 测试相关
chore: 构建/工具变更
```

## 测试说明

### 单元测试
当前项目使用 Jest 进行单元测试（待实现）：

```bash
# 运行所有测试
npm test

# 运行测试并监听文件变化
npm test --watch
```

### 端到端测试
使用 Cypress 进行 E2E 测试（待实现）：

```bash
# 启动 E2E 测试
npm run e2e
```

### 手动测试清单
在提交代码前，请手动验证以下功能：

- [ ] VoiceGenerator 组件正常工作
- [ ] AudioPlayer 播放/暂停功能正常
- [ ] TaskManager 任务状态更新正常
- [ ] 响应式布局在移动设备上正常显示
- [ ] 错误处理和加载状态正常

## 部署流程

### 本地 Docker 部署
```bash
# 构建前端镜像
docker build -t podcast-frontend:latest ./frontend

# 运行容器
docker run -d -p 80:80 --name podcast-frontend podcast-frontend:latest
```

### 与后端一起部署
使用 docker-compose 一键部署：

```bash
# 在项目根目录执行
docker-compose up --build
```

### 生产环境部署
1. 构建生产版本：`npm run build`
2. 将 `dist/` 目录部署到 CDN 或 Web 服务器
3. 配置 Nginx 反向代理到后端 API

### 环境变量
前端支持以下环境变量：

| 变量名 | 默认值 | 说明 |
|-------|--------|------|
| VITE_API_BASE_URL | http://localhost:8080/api | 后端 API 基础 URL |
| VITE_STRIPE_PUBLIC_KEY | - | Stripe 公钥 |

在 `.env` 文件中配置：

```env
VITE_API_BASE_URL=https://your-domain.com/api
VITE_STRIPE_PUBLIC_KEY=pk_test_xxx
```

## 常见问题

### 1. 依赖安装失败
**问题**: `npm install` 报错或卡住

**解决方案**:
```bash
# 清除 npm 缓存
npm cache clean --force

# 删除 node_modules 和 package-lock.json
rm -rf node_modules package-lock.json

# 重新安装
npm install
```

### 2. 开发服务器启动失败
**问题**: `npm run dev` 报错端口被占用

**解决方案**:
```bash
# 修改端口
VITE_PORT=3001 npm run dev

# 或者在 vite.config.ts 中修改
server: {
  port: 3001
}
```

### 3. API 代理不工作
**问题**: 前端无法连接到后端 API

**解决方案**:
检查 `vite.config.ts` 中的代理配置：

```typescript
server: {
  proxy: {
    '/api': {
      target: 'http://localhost:8080', // 确保后端端口正确
      changeOrigin: true
    }
  }
}
```

### 4. 构建失败
**问题**: `npm run build` 报 TypeScript 错误

**解决方案**:
```bash
# 检查 TypeScript 错误
npm run type-check

# 修复类型错误后重新构建
npm run build
```

### 5. Docker 构建缓慢
**问题**: Docker 构建过程很慢

**解决方案**:
- 确保 `.dockerignore` 文件存在，排除不必要的文件
- 使用 Docker BuildKit 加速构建：
  ```bash
  DOCKER_BUILDKIT=1 docker build -t podcast-frontend .
  ```

### 6. 音频播放问题
**问题**: AudioPlayer 组件无法播放音频

**解决方案**:
- 检查音频文件 URL 是否可访问
- 确保音频格式为 MP3 或其他 Howler.js 支持的格式
- 检查浏览器控制台是否有 CORS 错误

### 7. 状态管理问题
**问题**: TaskManager 组件状态不更新

**解决方案**:
- 检查 Zustand store 的 action 是否正确调用
- 确保组件正确订阅了状态变化
- 查看浏览器 DevTools 中的 Zustand 状态

---

## 贡献指南

欢迎贡献代码！请遵循以下步骤：

1. Fork 项目
2. 创建特性分支 (`git checkout -b feat/your-feature`)
3. 提交更改 (`git commit -am 'feat: add some feature'`)
4. 推送到分支 (`git push origin feat/your-feature`)
5. 创建 Pull Request

## 技术支持

如有问题，请联系：
- **徐旭尧** (CEO/后端开发)
- **虾天尊** (技术合伙人/前端开发)

或者在 GitHub Issues 中提交问题。