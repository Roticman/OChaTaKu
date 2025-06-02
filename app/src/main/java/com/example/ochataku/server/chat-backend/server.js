const express = require('express');
const http = require('http');
const {Server} = require('socket.io');
const cors = require('cors');
const bodyParser = require('body-parser');
const path = require('path');
const messageModule = require('./routes/message'); // 包含 router 和 setSocketInstance


const app = express();
const server = http.createServer(app);

// 静态托管 upload 目录
app.use('/upload', express.static(path.join(__dirname, 'upload')));

// 引入统一的上传路由
app.use(require('./routes/upload'));

// ✅ 初始化 socket.io
const io = new Server(server, {
    cors: {
        origin: '*', // 可根据你的前端地址限制
        methods: ['GET', 'POST'],
    },
});

// ✅ 设置 socket 实例供 message.js 使用
messageModule.setSocketInstance(io);

// ✅ 基础中间件
app.use(express.json());
app.use(cors());
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({extended: true}));

// ✅ 路由配置
const conversationRoutes = require('./routes/conversation');
const userRoutes = require('./routes/user');
const groupRoutes = require('./routes/group');
const contactRoutes = require('./routes/contact');

// ✅ 使用各类路由
app.use('/api/conversation', conversationRoutes);
app.use('/api/user', userRoutes);
app.use('/api/group', groupRoutes);
app.use('/api/message', messageModule.router); // 注意是 .router！
app.use('/api/contact', contactRoutes);

// ✅ Socket.io 连接逻辑
io.on('connection', (socket) => {
    console.log(`📡 用户已连接: ${socket.id}`);

    socket.on('join', (room) => {
        socket.join(room);
        console.log(`✅ 用户加入房间: ${room}`);
    });

    socket.on('disconnect', () => {
        console.log(`❌ 用户断开连接: ${socket.id}`);
    });
});

// ✅ 启动服务
const PORT = 3000;
server.listen(PORT, () => {
    console.log(`Server is running on http://localhost:${PORT}`);
});
