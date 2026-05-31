# 高校校历管理系统

一个基于 Spring Boot 3.2.5 开发的高校校历管理系统，提供学年学期管理、活动安排、节假日设置、校历导出等功能。

## 功能特性

- **校历视图**：以日历形式展示学年活动、节假日等信息
- **自然年校历**：支持从春节后开始的自然年校历视图
- **学年学期管理**：支持学年、学期的创建和管理
- **活动管理**：支持校运会、考试、典礼等各类活动的添加和管理
- **节假日设置**：支持法定节假日和学校假期的设置，可自动生成中国法定节假日
- **通知公告栏**：滚动展示当前学期的各类活动事件，支持查看更多和分页
- **模板管理**：支持学年模板的导入导出，方便快速创建新学年
- **iCal导出**：支持将校历导出为 iCal 格式，可导入到各类日历应用
- **多视图支持**：支持月视图、周视图、日程列表视图
- **学年学期跳转**：支持从学期卡片快速跳转到对应的日历视图
- **自动加载当前学年**：系统自动识别并加载当前学年

## 技术栈

### 后端
- **Java 21** - 开发语言
- **Spring Boot 3.2.5** - 后端框架
- **Spring Data JPA** - 数据持久化
- **H2 Database** - 嵌入式数据库（开发环境）
- **Lombok** - 简化代码

### 前端
- **Thymeleaf** - 模板引擎
- **Bootstrap 5.1.3** - UI框架
- **FullCalendar 5.11.5** - 日历组件

### 构建工具
- **Maven 3.9.11** - 项目构建和依赖管理

## 项目结构

```
school_calander/
├── src/
│   ├── main/
│   │   ├── java/com/calendarsystem/
│   │   │   ├── controller/          # 控制器层
│   │   │   │   ├── CalendarController.java    # 校历控制器
│   │   │   │   ├── AdminController.java       # 管理后台控制器
│   │   │   │   ├── HolidayController.java     # 节假日控制器
│   │   │   │   └── TemplateController.java    # 模板控制器
│   │   │   ├── model/               # 数据模型
│   │   │   │   ├── AcademicYear.java # 学年
│   │   │   │   ├── Semester.java     # 学期
│   │   │   │   ├── Event.java       # 活动事件
│   │   │   │   └── Holiday.java     # 节假日
│   │   │   ├── repository/          # 数据访问层
│   │   │   ├── service/             # 业务逻辑层
│   │   │   │   ├── CalendarService.java        # 校历服务
│   │   │   │   ├── NaturalYearService.java    # 自然年服务
│   │   │   │   └── ICalService.java           # iCal导出服务
│   │   │   └── CalendarSystemApplication.java
│   │   └── resources/
│   │       ├── static/              # 静态资源（本地化CDN资源）
│   │       │   ├── css/             # 样式文件
│   │       │   └── js/              # JavaScript文件
│   │       ├── templates/           # Thymeleaf模板
│   │       │   ├── index.html      # 校历首页
│   │       │   ├── admin.html       # 管理后台
│   │       │   └── dashboard.html   # 学年视图
│   │       ├── application.properties
│   │       └── data.sql            # 初始化数据
│   └── test/                        # 测试代码
├── docs/                            # 文档目录
│   ├── API.md                       # API规范文档
│   └── USER_MANUAL.md               # 用户手册
├── pom.xml                          # Maven配置
└── README.md                        # 项目说明
```

## 快速开始

### 环境要求

- JDK 21+
- Maven 3.9+

### 运行步骤

1. **克隆项目**
   ```bash
   git clone https://github.com/your-username/school_calander.git
   cd school_calander
   ```

2. **编译项目**
   ```bash
   mvn clean compile
   ```

3. **运行项目**
   ```bash
   mvn spring-boot:run
   ```

4. **访问应用**
   - 校历首页：http://localhost:8080/
   - 学年视图：http://localhost:8080/dashboard
   - 管理后台：http://localhost:8080/admin
   - H2控制台：http://localhost:8080/h2-console

### 默认数据

系统启动时会自动初始化以下测试数据：
- 2025-2026学年（秋季学期、春季学期）
- 2026-2027学年（秋季学期、春季学期）
- 各类活动事件（开学典礼、校运会、期末考试等）
- 法定节假日（元旦、春节、清明、劳动节、端午、中秋、国庆）

## 主要功能说明

### 1. 校历视图（首页）

- **视图切换**：支持学年校历和自然年校历两种视图
- **自然年校历**：从春节后开始，显示跨学年的完整校历
- **日历展示**：月视图、周视图、日程列表视图
- **通知公告栏**：滚动展示活动事件，支持分页查看
- **自动加载**：自动识别并加载当前学年
- **iCal导出**：导出校历到日历应用

### 2. 自然年校历

自然年校历从第一年春节后开始，到第二年春节后结束，包含：

- **春季学期**（2-7月）
- **秋季学期**（9月-次年1月）
- **寒假**（约1-2月）
- **暑假**（约7-8月）

支持的春节日期范围：2024-2030年

### 3. 学年学期管理

- 学年学期总览，显示统计数据
- 学期卡片展示详细信息
- 点击学期卡片可跳转到对应的日历视图
- 学年统计对比功能

### 4. 管理后台

- 学年管理：创建、发布学年
- 活动管理：添加各类活动事件
- 节假日管理：添加节假日、生成中国法定节假日
- 模板管理：导入导出学年模板
- 日历预览：实时查看校历效果

## API接口

详见 [API规范文档](docs/API.md)

## 用户手册

详见 [用户手册](docs/USER_MANUAL.md)

## 配置说明

### application.properties 主要配置

```properties
spring.application.name=school-calendar
server.port=8080

# H2 Database
spring.datasource.url=jdbc:h2:mem:calendardb
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

# JPA/Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false

# H2 Console
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console
```

## 开发说明

### 数据模型

- **AcademicYear**：学年信息（名称、日期范围、状态）
- **Semester**：学期信息（学期类型、日期、授课周数）
- **Event**：活动事件（类型、日期、优先级）
- **Holiday**：节假日信息（类型、日期、是否年度重复）

### 扩展开发

1. 添加新的活动类型：修改 `Event.EventType` 枚举
2. 自定义节假日生成逻辑：修改 `HolidayController` 中的生成方法
3. 扩展导出格式：修改 `ICalService` 或添加新的导出服务
4. 自然年春节日期：修改 `NaturalYearService` 中的 SPRING_FESTIVAL_DATES 映射

## 许可证

MIT License

## 贡献指南

欢迎提交 Issue 和 Pull Request！

## 更新日志

### v1.1.0 (2026-05-31)
- ✨ **新增自然年校历功能**：支持从春节后开始的自然年视图
- ✨ **新增视图切换**：学年校历和自然年校历自由切换
- ✨ **新增查看更多功能**：通知公告栏支持分页和筛选
- ✨ **实现自动加载当前学年**：系统自动识别当前学年
- 📝 完善项目文档（README、API文档、用户手册）
- 🐛 修复CDN资源加载问题（本地化静态资源）
- 🐛 修复JavaScript语法错误

### v1.0.0 (2026-05-30)
- 完成基础校历管理功能
- 实现学年学期管理
- 支持活动事件和节假日管理
- 添加模板导入导出功能
- 实现iCal导出