
# 📱 OChaTaKu 聊天系统

OChaTaKu 是一个基于 Kotlin Android、Node.js 和 MySQL 构建的即时通讯应用，支持私聊、群聊、头像昵称展示、消息发送与媒体上传等功能。

---

## 🚀 功能特性

- **双端通信**：支持私聊和群聊，自动识别并展示用户或群组信息。
- **消息模块**：支持文本和图片消息，群聊中展示群内昵称，自动刷新会话列表。
- **用户模块**：注册、登录、用户信息展示，提供简洁的用户信息接口。
- **群组模块**：群组基本信息展示，包括群公告、成员数、群头像等字段。

---

## 🛠 技术栈

- **前端（移动端）**：Kotlin、Jetpack Compose、Room、Hilt
- **后端**：Node.js、Express
- **数据库**：MySQL 8.x
- **图片加载**：Coil
- **网络通信**：Retrofit、OkHttp
- **构建工具**：Gradle、KSP（Kotlin Symbol Processing）

---

## 📂 项目结构

```
OChaTaKu/
├── app/                       # Android 应用
│   ├── data/local             # Room 实体与 Dao
│   ├── service/               # Retrofit 接口
│   ├── ui/                    # Jetpack Compose 界面
│   └── viewmodel/             # ViewModel 层
├── chat-backend/              # Node.js 后端
│   ├── routes/                # 路由模块（user.js, group.js, message.js...）
│   ├── app.js                 # 入口文件
│   └── db.js                  # 数据库连接配置
└── database/
    └── schema.sql             # MySQL 数据库结构
```

---

## 📦 数据表结构简要

### `conversation` 表

| 字段         | 类型    | 说明               |
|--------------|---------|--------------------|
| conv_id      | BIGINT  | 主键，自增         |
| user_id      | BIGINT  | 当前用户 ID        |
| peer_id      | BIGINT  | 对方 ID（用户/群） |
| is_group     | BOOLEAN | 是否为群聊         |
| last_message | TEXT    | 最后一条消息       |
| timestamp    | BIGINT  | 时间戳             |

### `message` 表

| 字段         | 类型    | 说明               |
|--------------|---------|--------------------|
| id           | BIGINT  | 主键，自增         |
| sender_id    | BIGINT  | 发送者 ID          |
| conv_id      | BIGINT  | 所属会话 ID        |
| content      | TEXT    | 消息内容           |
| media_url    | TEXT    | 媒体文件 URL       |
| message_type | VARCHAR | 消息类型（text/image） |
| timestamp    | BIGINT  | 时间戳             |

---

## ⚙️ 快速开始

### Android 项目

1. 使用 Android Studio 打开 `OChaTaKu` 项目。
2. 配置本地 IP 和后端接口地址。
3. 运行 App。

### 后端 Node.js

```bash
cd chat-backend
npm install
node app.js
```

确保 `.env` 或 `db.js` 中的数据库配置正确。

---

## 📸 截图示例

> *建议在此处插入聊天界面、会话界面、发送消息界面等截图，展示产品功能亮点。*

---

## 📄 License

MIT License © 2025 [Roticman](https://github.com/Roticman)
