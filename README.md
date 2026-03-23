# 电视招聘展示系统 - 后台管理系统

## 项目概述

本系统服务于公司50台电视的招聘展示场景，实现职位信息管理、招聘海报自动生成、内容精准推送、电视终端统一管控等功能。

## 技术栈

- **Java 17**
- **Spring Boot 3.2.3**
- **MySQL 8.0+**
- **MyBatis-Plus 3.5.5**
- **Spring Security 6.x + JWT**
- **WebSocket**
- **Knife4j (Swagger)**

## 项目结构

```
tv_recru/
├── src/main/java/com/tv/recruitment/
│   ├── TvRecruitmentApplication.java  # 启动类
│   ├── common/                        # 公共模块
│   │   ├── config/                    # 配置类
│   │   ├── exception/                 # 异常处理
│   │   ├── result/                    # 统一响应
│   │   ├── enums/                     # 枚举类
│   │   └── utils/                     # 工具类
│   ├── security/                      # 安全模块
│   ├── entity/                        # 实体类
│   ├── mapper/                        # Mapper接口
│   ├── service/                       # 服务层
│   ├── controller/                    # 控制器
│   ├── dto/                           # 数据传输对象
│   └── websocket/                     # WebSocket模块
├── src/main/resources/
│   ├── application.yml                # 主配置
│   ├── application-dev.yml            # 开发环境
│   ├── application-prod.yml           # 生产环境
│   └── db/init.sql                    # 数据库初始化脚本
└── pom.xml
```

## 快速开始

### 1. 环境要求

- JDK 17+
- Maven 3.6+
- MySQL 8.0+

### 2. 数据库配置

```sql
-- 创建数据库
CREATE DATABASE tv_recruitment DEFAULT CHARACTER SET utf8mb4;

-- 执行初始化脚本
source src/main/resources/db/init.sql
```

### 3. 修改配置

编辑 `src/main/resources/application-dev.yml`，修改数据库连接信息：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/tv_recruitment?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: your_password
```

### 4. 启动项目

```bash
# 编译
mvn clean compile

# 运行
mvn spring-boot:run

# 或打包后运行
mvn clean package -DskipTests
java -jar target/tv-recruitment-1.0.0.jar
```

### 5. 访问API文档

启动成功后访问：http://localhost:8080/doc.html

## 默认账号

- 用户名：`admin`
- 密码：`admin123`

## API接口

| 模块 | 路径 | 说明 |
|------|------|------|
| 认证 | /api/auth | 登录、登出、修改密码 |
| 用户 | /api/users | 用户管理（管理员） |
| 职位 | /api/jobs | 职位管理 |
| 设备 | /api/devices | 设备管理 |
| 海报 | /api/posters | 海报管理 |
| 视频 | /api/videos | 视频管理 |
| 推送 | /api/push | 内容推送 |
| 日志 | /api/logs | 操作日志 |
| 配置 | /api/config | 系统配置 |

## WebSocket

连接地址：`ws://localhost:8080/ws?token={jwt}&deviceCode={code}`

消息类型：
- `HEARTBEAT` - 心跳
- `PUSH_CONTENT` - 内容推送
- `CONTROL` - 控制指令
- `STATUS_REPORT` - 状态上报

## 文件存储

本地文件存储，默认路径：`D:/tv-files/`

目录结构：
```
D:/tv-files/
├── images/     # 图片
├── videos/     # 视频
├── posters/    # 海报
└── templates/  # 模板
```

访问方式：`http://localhost:8080/files/{type}/{path}`

## 打包部署

```bash
# 打包
mvn clean package -DskipTests

# 运行（生产环境）
java -jar tv-recruitment-1.0.0.jar --spring.profiles.active=prod
```