
# 📱 OChaTaKu 聊天系统

一个支持私聊、群聊、音视频、语音消息、AI 助手、语言切换与主题自定义的完整即时通讯系统，包含 Android 前端、Node.js 后端及 MySQL 数据库，适合学习、实践与展示。

---

## 📌 项目特点

- 支持 **私聊 / 群聊**，消息类型包括文字、图片、语音、视频
- 引入 **AI 会话助手**（支持开启 / 关闭）
- 支持 **语音长按录音**、全屏图片 / 视频预览、消息引用 / 删除
- 实现 **好友添加、请求管理、联系人备注与头像** 功能
- 支持 **用户注册、登录、修改资料、注销账号**
- 支持 **中英文语言切换、深浅主题切换**
- 前端使用 Jetpack Compose + MVVM，后端基于 Express + MySQL
- 本地缓存：Room 数据库 + DataStore，支持离线访问与偏好持久化
- 多媒体上传与访问支持（语音、视频、图片、头像）

---

## 📱 前端技术栈（Android）

- Jetpack Compose + Kotlin + MVVM
- Retrofit + Hilt + Coil + Room + DataStore
- Socket.IO 实现聊天室房间连接
- 状态管理使用 StateFlow / MutableState
- 多语言、多主题支持（含 DataStore 持久化）

---

## 🌐 后端技术栈（Node.js）

- Node.js + Express 框架
- MySQL 数据库
- Socket.IO 实时通信
- Multer 实现头像与媒体上传
- 模块结构清晰：用户、联系人、消息、群组、会话等

---

## 🗃 数据库结构（MySQL）

数据库设计包括以下主要表：

- `user`：用户信息
- `contact`：联系人关系
- `friendrequest`：好友请求记录
- `conversation`：会话信息
- `message`：聊天消息记录
- `group`：群组信息
- `groupmember`：群成员关系
- `mediaresource`：上传的媒体文件
- `usersetting`：用户偏好设置

完整建表脚本参见项目中的 [`Android.sql`](./app/src/main/java/com/example/ochataku/MySQL/Android.sql)。

---

## 🚀 快速启动指南

### ✅ 数据库启动

使用MySQLWorkBench导入[`Android.sql`](./app/src/main/java/com/example/ochataku/MySQL/Android.sql)。


### ✅ 后端启动

后端项目文件地址[`chatbackend`](./app/src/main/java/com/example/ochataku/server)。

```bash
cd server
npm install
node server.js
```

默认运行于：

```
http://localhost:3000
```

### ✅ 数据库初始化

```bash
mysql -u root -p < Android.sql
```

请确保本地 MySQL 中已创建 `android` 数据库，并根据情况修改 `db.js` 配置。

### ✅ Android 前端运行

1. 使用 Android Studio 打开项目
2. 运行 `MainActivity.kt`
3. 修改 `ApiClient.kt` 中的 `BASE_URL` 为实际后端地址

---

## ✨ 项目亮点

* Jetpack Compose 构建响应式 UI，UI 逻辑清晰
* 使用 Room + Retrofit + Hilt 完善架构体系
* AI 助手集成，提升交互智能性
* 本地数据库与远程服务同步逻辑健全
* 支持文件上传、消息缓存、断网可用

---

## 🧑‍💻 作者

由 **\[Roticman]** 独立完成，作为本科毕业设计项目。欢迎参考、学习和提出建议！

---

## 📄 License

本项目仅供学习交流，版权归作者所有，禁止商用。

```

