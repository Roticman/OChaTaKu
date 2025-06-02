// db.js - 数据库连接配置
const mysql = require('mysql2');

const db = mysql.createPool({
  host: 'localhost',
  user: 'root',
  password: 'wangxin666', // ← 改成你的 MySQL 密码
  database: 'AndroidChat', // ← 改成你的数据库名
  waitForConnections: true,
  connectionLimit: 10,
  queueLimit: 0
});

module.exports = db;
