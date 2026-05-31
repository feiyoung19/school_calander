# 高校校历管理系统 - API规范文档

## 目录

1. [API概述](#api概述)
2. [通用规范](#通用规范)
3. [接口列表](#接口列表)
4. [数据模型](#数据模型)
5. [错误处理](#错误处理)

---

## API概述

### 基础URL

```
http://localhost:8080
```

### API版本

当前版本：v1.0.0

### 认证方式

当前版本无需认证（开发环境）。

---

## 通用规范

### 请求格式

- **Content-Type**: `application/json`
- **字符编码**: UTF-8

### 响应格式

所有API响应均为JSON格式：

```json
{
  "success": true,
  "data": {},
  "message": "操作成功"
}
```

错误响应：

```json
{
  "success": false,
  "message": "错误描述",
  "error": "ERROR_CODE"
}
```

### HTTP状态码

| 状态码 | 说明 |
|--------|------|
| 200 | 成功 |
| 201 | 创建成功 |
| 400 | 请求参数错误 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

---

## 接口列表

### 1. 学年管理

#### 1.1 获取学年列表

```
GET /api/academic-years
```

**响应示例**：

```json
[
  {
    "id": 1,
    "name": "2025-2026",
    "startDate": "2025-09-01",
    "endDate": "2026-08-31",
    "status": "PUBLISHED"
  },
  {
    "id": 2,
    "name": "2026-2027",
    "startDate": "2026-09-01",
    "endDate": "2027-08-31",
    "status": "DRAFT"
  }
]
```

#### 1.2 创建学年

```
POST /admin/academic-year
```

**请求参数**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| name | String | 是 | 学年名称，如"2027-2028" |
| startYear | Integer | 是 | 起始年份，如2027 |

**请求示例**：

```
POST /admin/academic-year?name=2027-2028&startYear=2027
```

**响应**：成功返回200，失败返回错误信息。

#### 1.3 发布学年

```
POST /admin/publish/{yearId}
```

**路径参数**：

| 参数 | 类型 | 说明 |
|------|------|------|
| yearId | Long | 学年ID |

---

### 2. 学期管理

#### 2.1 获取学期列表

```
GET /admin/semesters?yearId={yearId}
```

**请求参数**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| yearId | Long | 是 | 学年ID |

**响应示例**：

```json
[
  {
    "id": 1,
    "term": "FALL",
    "startDate": "2025-09-01",
    "endDate": "2026-01-15",
    "weekCount": 18
  },
  {
    "id": 2,
    "term": "SPRING",
    "startDate": "2026-02-20",
    "endDate": "2026-07-05",
    "weekCount": 16
  }
]
```

---

### 3. 活动事件管理

#### 3.1 获取活动事件列表

```
GET /api/events?yearId={yearId}
```

**请求参数**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| yearId | Long | 是 | 学年ID |

**响应示例**：

```json
[
  {
    "id": "event1",
    "title": "开学典礼",
    "start": "2025-09-01",
    "end": "2025-09-01",
    "eventType": "CEREMONY",
    "display": null,
    "color": "#4caf50"
  },
  {
    "id": "holiday1",
    "title": "国庆节",
    "start": "2025-10-01",
    "end": "2025-10-07",
    "display": "background",
    "color": "#f44336"
  }
]
```

**字段说明**：

| 字段 | 类型 | 说明 |
|------|------|------|
| id | String | 事件唯一标识 |
| title | String | 事件标题 |
| start | String | 开始日期（ISO格式） |
| end | String | 结束日期（ISO格式，可选） |
| eventType | String | 活动类型（SPORTS/EXAM/CEREMONY/OTHER） |
| display | String | 显示方式（null为普通事件，background为背景事件） |
| color | String | 事件颜色 |

#### 3.2 创建活动事件

```
POST /admin/event?semesterId={semesterId}
```

**请求参数**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| semesterId | Long | 是 | 学期ID |

**请求体**：

```json
{
  "title": "秋季运动会",
  "eventType": "SPORTS",
  "startDate": "2025-10-15",
  "endDate": "2025-10-17",
  "description": "全校秋季运动会",
  "status": "CONFIRMED",
  "priority": 1
}
```

**字段说明**：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| title | String | 是 | 活动名称 |
| eventType | String | 是 | 活动类型（SPORTS/EXAM/CEREMONY/OTHER） |
| startDate | String | 是 | 开始日期 |
| endDate | String | 否 | 结束日期 |
| description | String | 否 | 活动描述 |
| status | String | 否 | 状态（CONFIRMED/TENTATIVE/CANCELLED） |
| priority | Integer | 否 | 优先级（1-5） |

---

### 4. 节假日管理

#### 4.1 获取节假日列表

```
GET /api/holidays?yearId={yearId}
```

或

```
GET /api/holidays/by-year/{yearId}
```

**响应示例**：

```json
[
  {
    "id": 1,
    "name": "国庆节",
    "type": "LEGAL",
    "startDate": "2025-10-01",
    "endDate": "2025-10-07",
    "recurring": true
  },
  {
    "id": 2,
    "name": "寒假",
    "type": "SCHOOL",
    "startDate": "2026-01-16",
    "endDate": "2026-02-19",
    "recurring": false
  }
]
```

#### 4.2 创建节假日

```
POST /api/holidays/create
```

**请求体**：

```json
{
  "yearId": 1,
  "name": "寒假",
  "type": "SCHOOL",
  "startDate": "2026-01-16",
  "endDate": "2026-02-19",
  "recurring": false
}
```

**字段说明**：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| yearId | Long | 是 | 学年ID |
| name | String | 是 | 节假日名称 |
| type | String | 是 | 类型（LEGAL/SCHOOL/OTHER） |
| startDate | String | 是 | 开始日期 |
| endDate | String | 是 | 结束日期 |
| recurring | Boolean | 否 | 是否年度重复 |

#### 4.3 删除节假日

```
DELETE /api/holidays/delete/{id}
```

**响应示例**：

```json
{
  "success": true,
  "message": "删除成功"
}
```

#### 4.4 生成中国法定节假日

```
POST /api/holidays/generate-chinese-holidays?yearId={yearId}&year={year}
```

**请求参数**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| yearId | Long | 是 | 学年ID |
| year | Integer | 是 | 年份 |

**响应示例**：

```json
{
  "success": true,
  "message": "成功生成 7 个法定节假日",
  "count": 7
}
```

---

### 5. 模板管理

#### 5.1 导出学年模板

```
GET /api/template/export/{yearId}
```

**响应**：返回JSON格式的模板文件。

**响应示例**：

```json
{
  "academicYear": {
    "name": "2025-2026",
    "startDate": "2025-09-01",
    "endDate": "2026-08-31"
  },
  "semesters": [
    {
      "term": "FALL",
      "startDate": "2025-09-01",
      "endDate": "2026-01-15",
      "weekCount": 18
    }
  ],
  "events": [...],
  "holidays": [...]
}
```

#### 5.2 导入学年模板

```
POST /api/template/import
```

**请求参数**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| file | File | 是 | 模板文件（JSON格式） |
| yearOffset | Integer | 否 | 年份偏移（默认1） |

**请求方式**：multipart/form-data

**响应示例**：

```json
{
  "success": true,
  "message": "成功导入学年模板，创建学年：2026-2027"
}
```

---

### 6. 校历导出

#### 6.1 导出iCal格式

```
GET /api/calendar/ical?yearId={yearId}
```

**请求参数**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| yearId | Long | 是 | 学年ID |

**响应**：
- Content-Type: `text/calendar; charset=utf-8`
- 文件名: `university-calendar.ics`

**iCal内容示例**：

```
BEGIN:VCALENDAR
VERSION:2.0
PRODID:-//University Calendar System//EN
CALSCALE:GREGORIAN
METHOD:PUBLISH

BEGIN:VEVENT
DTSTART:20250901
DTEND:20250901
SUMMARY:开学典礼
DESCRIPTION:2025-2026学年开学典礼
END:VEVENT

END:VCALENDAR
```

---

### 7. 自然年校历

自然年校历从第一年春节后开始，到第二年春节后结束，支持跨学年展示校历。

#### 7.1 获取自然年信息

```
GET /api/natural-year/info?year={year}
```

**请求参数**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| year | Integer | 否 | 自然年年份，默认当前年 |

**响应示例**：

```json
{
  "year": 2026,
  "name": "2026自然年",
  "startDate": "2026-02-28",
  "endDate": "2027-02-27",
  "springFestivalCurrent": "2026-02-17",
  "springFestivalNext": "2027-02-06"
}
```

**字段说明**：

| 字段 | 类型 | 说明 |
|------|------|------|
| year | Integer | 自然年年份 |
| name | String | 自然年名称 |
| startDate | String | 开始日期（春节后11天） |
| endDate | String | 结束日期（次年春节后10天） |
| springFestivalCurrent | String | 当年春节日期 |
| springFestivalNext | String | 次年春节日期 |

#### 7.2 获取自然年校历事件

```
GET /api/natural-year/events?year={year}
```

**请求参数**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| year | Integer | 否 | 自然年年份，默认当前年 |

**响应示例**：

```json
[
  {
    "id": "event-1",
    "title": "春季学期开学",
    "start": "2026-02-28",
    "end": "2026-02-28",
    "eventType": "CEREMONY",
    "color": "#4caf50"
  },
  {
    "id": "holiday-1",
    "title": "清明节",
    "start": "2026-04-04",
    "end": "2026-04-06",
    "display": "background",
    "color": "#f44336"
  }
]
```

#### 7.3 导出自然年iCal格式

```
GET /api/natural-year/ical?year={year}
```

**请求参数**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| year | Integer | 否 | 自然年年份，默认当前年 |

**响应**：
- Content-Type: `text/calendar; charset=utf-8`
- 文件名: `natural-year-{year}-calendar.ics`

---

## 数据模型

### AcademicYear（学年）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 学年ID |
| name | String | 学年名称 |
| startDate | LocalDate | 开始日期 |
| endDate | LocalDate | 结束日期 |
| status | Enum | 状态（DRAFT/PUBLISHED/ARCHIVED） |

### Semester（学期）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 学期ID |
| term | Enum | 学期类型（FALL/SPRING） |
| startDate | LocalDate | 开始日期 |
| endDate | LocalDate | 结束日期 |
| weekCount | Integer | 授课周数 |
| academicYear | AcademicYear | 所属学年 |

### CalendarEvent（活动事件）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 事件ID |
| title | String | 事件标题 |
| eventType | Enum | 活动类型（SPORTS/EXAM/CEREMONY/OTHER） |
| startDate | LocalDate | 开始日期 |
| endDate | LocalDate | 结束日期 |
| description | String | 事件描述 |
| status | Enum | 状态（CONFIRMED/TENTATIVE/CANCELLED） |
| priority | Integer | 优先级 |
| semester | Semester | 所属学期 |

### Holiday（节假日）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 节假日ID |
| name | String | 节假日名称 |
| type | Enum | 类型（LEGAL/SCHOOL/OTHER） |
| startDate | LocalDate | 开始日期 |
| endDate | LocalDate | 结束日期 |
| recurring | Boolean | 是否年度重复 |
| academicYear | AcademicYear | 所属学年 |

---

## 错误处理

### 错误响应格式

```json
{
  "success": false,
  "message": "错误描述",
  "error": "ERROR_CODE"
}
```

### 常见错误码

| 错误码 | 说明 |
|--------|------|
| YEAR_NOT_FOUND | 学年不存在 |
| SEMESTER_NOT_FOUND | 学期不存在 |
| EVENT_NOT_FOUND | 事件不存在 |
| INVALID_DATE | 日期格式错误 |
| TEMPLATE_PARSE_ERROR | 模板解析失败 |
| ICAL_GENERATION_ERROR | iCal生成失败 |

---

## 附录

### 活动类型枚举

| 值 | 说明 | 颜色 |
|----|------|------|
| SPORTS | 校运会 | #ff9800 |
| EXAM | 考试 | #f44336 |
| CEREMONY | 典礼 | #4caf50 |
| OTHER | 其他 | #2196f3 |

### 节假日类型枚举

| 值 | 说明 |
|----|------|
| LEGAL | 法定节假日 |
| SCHOOL | 学校假期 |
| OTHER | 其他 |

### 学年状态枚举

| 值 | 说明 |
|----|------|
| DRAFT | 草稿 |
| PUBLISHED | 已发布 |
| ARCHIVED | 已归档 |

---

**版本**：v1.0.0  
**更新日期**：2026-05-31