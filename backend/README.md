# 后端项目 - 口播网站

## 技术栈
- **框架**: Spring Boot 2.7 + Java 8
- **数据库**: PostgreSQL + JPA/Hibernate
- **缓存**: Redis
- **安全**: Spring Security + JWT
- **支付**: Stripe API
- **AI 集成**: Coze Voice Gen API
- **监控**: Spring Boot Actuator

## 核心模块
1. **用户认证**: JWT 认证、用户注册/登录
2. **语音任务**: 文字转语音任务管理
3. **计费系统**: Stripe 支付集成、用量统计
4. **音色克隆**: 音频上传、自定义音色生成
5. **API 网关**: RESTful API 接口

## 开发环境
```bash
# 构建项目
mvn clean package

# 运行应用
java -jar target/podcast-website-1.0.0.jar

# 数据库迁移
# 使用 Flyway 或 Liquibase (待实现)
```

## 环境变量
```env
DB_USERNAME=postgres
DB_PASSWORD=postgres
COZE_API_KEY=your_coze_api_key
STRIPE_SECRET_KEY=your_stripe_secret_key
STRIPE_PUBLISHABLE_KEY=your_stripe_publishable_key
JWT_SECRET=your_jwt_secret_key
```

## API 文档
- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI Spec: http://localhost:8080/v3/api-docs

---
**负责人**: 徐旭尧 (后端开发)