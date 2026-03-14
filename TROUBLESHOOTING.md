# Docker 网络问题排查指南

## 问题描述
Docker Compose 无法拉取镜像，出现网络超时错误：
```
Error response from daemon: Get "https://registry-1.docker.io/v2/": net/http: request canceled while waiting for connection (Client.Timeout exceeded while awaiting headers)
```

## 已配置的镜像仓库
系统已经配置了以下 Docker 镜像仓库：
- https://docker.mirrors.ustc.edu.cn/
- https://hub-mirror.c.163.com/

## 排查步骤

### 1. 检查网络连通性
```bash
# 测试基本网络连接
ping google.com

# 测试 Docker registry 连接
ping registry-1.docker.io
```

### 2. 验证 Docker 配置
检查 `/etc/docker/daemon.json` 文件是否包含正确的镜像仓库配置：
```json
{
  "registry-mirrors": [
    "https://docker.mirrors.ustc.edu.cn/",
    "https://hub-mirror.c.163.com/"
  ]
}
```

### 3. 重启 Docker 服务
```bash
sudo systemctl restart docker
```

### 4. 手动测试镜像拉取
```bash
# 尝试拉取基础镜像
docker pull alpine:latest
```

### 5. 使用 BuildKit 替代传统构建器
如果传统构建器有问题，可以尝试使用 BuildKit：
```bash
# 启用 BuildKit
export DOCKER_BUILDKIT=1
docker build --no-cache -t your-image .
```

### 6. 离线构建方案
如果网络问题无法解决，可以考虑以下离线方案：

#### 方案 A: 使用本地构建的镜像
1. 在有网络的环境中构建并保存镜像
2. 将镜像导出为 tar 文件
3. 在目标环境中导入镜像

```bash
# 导出镜像
docker save -o myimage.tar myimage:tag

# 导入镜像
docker load -i myimage.tar
```

#### 方案 B: 修改 Dockerfile 使用本地基础镜像
如果可能，修改 Dockerfile 使用已经存在的本地镜像作为基础。

## 当前状态
- 所有应用代码已开发完成
- Docker Compose 配置文件已准备就绪
- 等待网络问题解决后进行集成测试

## 联系支持
如果以上步骤无法解决问题，请联系系统管理员检查：
- 防火墙规则
- 代理设置
- DNS 配置
- 网络路由