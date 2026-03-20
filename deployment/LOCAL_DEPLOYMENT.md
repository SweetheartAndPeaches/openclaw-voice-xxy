# 本地部署解决方案

由于Docker网络连接问题，提供以下替代部署方案：

## 方案1：本地开发环境部署

### 后端部署
```bash
# 进入后端目录
cd /workspace/projects/podcast-website/backend

# 安装依赖
mvn clean install

# 运行应用（确保PostgreSQL和Redis已安装并运行）
mvn spring-boot:run
```

### 前端部署
```bash
# 进入前端目录
cd /workspace/projects/podcast-website/frontend

# 安装依赖
npm install

# 开发环境运行
npm run dev

# 生产环境构建
npm run build
```

### 数据库设置
```bash
# 安装PostgreSQL
sudo apt update
sudo apt install postgresql postgresql-contrib

# 启动PostgreSQL
sudo systemctl start postgresql
sudo systemctl enable postgresql

# 创建数据库和用户
sudo -u postgres psql
CREATE DATABASE podcast_db;
CREATE USER podcast_user WITH PASSWORD 'podcast_pass';
GRANT ALL PRIVILEGES ON DATABASE podcast_db TO podcast_user;
\q
```

### Redis设置
```bash
# 安装Redis
sudo apt install redis-server

# 启动Redis
sudo systemctl start redis-server
sudo systemctl enable redis-server
```

## 方案2：使用国内镜像源

### 修改Docker配置
```bash
# 编辑Docker配置文件
sudo nano /etc/docker/daemon.json

# 添加以下内容：
{
  "registry-mirrors": [
    "https://docker.m.daocloud.io",
    "https://docker.nju.edu.cn",
    "https://docker.mirrors.ustc.edu.cn"
  ]
}

# 重启Docker
sudo systemctl daemon-reload
sudo systemctl restart docker
```

### 使用国内Maven镜像
在后端的pom.xml中添加：
```xml
<repositories>
    <repository>
        <id>aliyunmaven</id>
        <name>阿里云公共仓库</name>
        <url>https://maven.aliyun.com/repository/public</url>
    </repository>
</repositories>
```

## 方案3：手动构建镜像

### 构建后端镜像
```bash
# 进入后端目录
cd /workspace/projects/podcast-website/backend

# 先本地构建JAR包
mvn clean package -DskipTests

# 创建简化版Dockerfile（不依赖多阶段构建）
cat > Dockerfile.simple << 'EOF'
FROM openjdk:17-jre-slim
WORKDIR /app
COPY target/podcast-website-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
EOF

# 构建镜像
docker build -f Dockerfile.simple -t podcast-backend .
```

### 构建前端镜像
```bash
# 进入前端目录
cd /workspace/projects/podcast-website/frontend

# 构建生产版本
npm install
npm run build

# 创建Dockerfile
cat > Dockerfile << 'EOF'
FROM nginx:alpine
COPY dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/nginx.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
EOF

# 创建nginx配置
cat > nginx.conf << 'EOF'
events {
    worker_connections 1024;
}
http {
    include /etc/nginx/mime.types;
    default_type application/octet-stream;
    
    server {
        listen 80;
        server_name localhost;
        
        location / {
            root /usr/share/nginx/html;
            index index.html index.htm;
            try_files $uri $uri/ /index.html;
        }
        
        location /api {
            proxy_pass http://backend:8080;
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
        }
    }
}
EOF

# 构建镜像
docker build -t podcast-frontend .
```

## 推荐方案

当前网络环境下，建议采用**方案1**进行本地开发环境部署，待网络问题解决后再使用Docker容器化部署。

## 环境变量配置

确保在运行前设置以下环境变量：
```bash
export DB_USERNAME=postgres
export DB_PASSWORD=postgres
export DB_HOST=localhost
export DB_PORT=5432
export DB_NAME=podcast_db
export REDIS_HOST=localhost
export REDIS_PORT=6379
export COZE_API_KEY=your_coze_api_key
export STRIPE_SECRET_KEY=your_stripe_secret_key
export STRIPE_PUBLISHABLE_KEY=your_stripe_publishable_key
export JWT_SECRET=your_jwt_secret
```

## 验证部署

后端健康检查：
```bash
curl http://localhost:8080/api/actuator/health
```

前端访问：
```bash
# 开发环境
http://localhost:5173

# 生产环境
http://localhost:3000
```