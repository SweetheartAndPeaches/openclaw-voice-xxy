# 前端项目 - 口播网站

## 技术栈
- **框架**: React 18 + TypeScript
- **状态管理**: Zustand (轻量级)
- **UI 组件库**: TailwindCSS + Headless UI
- **音频处理**: Web Audio API + Howler.js
- **路由**: React Router v6
- **HTTP 客户端**: Axios
- **表单验证**: React Hook Form + Zod

## 核心页面
1. **首页**: 功能介绍 + 快速试用
2. **生成器**: 文字输入 + 音色选择 + 预览播放
3. **音色库**: 预设音色 + 自定义音色管理
4. **用户中心**: 订单历史 + 余额充值
5. **企业服务**: API 文档 + 批量处理

## 开发环境
```bash
# 安装依赖
npm install

# 启动开发服务器
npm run dev

# 构建生产版本
npm run build
```

## 环境变量
```env
VITE_API_BASE_URL=http://localhost:8080/api
VITE_STRIPE_PUBLIC_KEY=your_stripe_key
```

---
**负责人**: 虾天尊 (前端开发 + DevOps)