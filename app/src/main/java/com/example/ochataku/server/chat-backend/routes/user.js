const db = require('../db');
const express = require('express');
const bcrypt = require('bcrypt');
const router = express.Router();
const multer = require('multer');
const path = require('path');
const fs = require('fs');

// 设置 multer 存储配置
const storage = multer.diskStorage({
    destination: (req, file, cb) => {
        const uploadDir = path.join(__dirname, '../uploads');
        if (!fs.existsSync(uploadDir)) {
            fs.mkdirSync(uploadDir);
        }
        cb(null, uploadDir);
    },
    filename: (req, file, cb) => {
        const uniqueSuffix = Date.now() + '-' + Math.round(Math.random() * 1E9);
        const ext = path.extname(file.originalname);
        cb(null, `avatar-${uniqueSuffix}${ext}`);
    }
});

const upload = multer({storage: storage});

// 获取用户详细信息的路由
router.get('/profile/:id', (req, res) => {
    const userId = req.params.id;

    // 确保用户 ID 存在
    if (!userId) {
        return res.status(400).json({message: 'User ID is required'});
    }

    // 查询用户信息（不返回 password）
    const query = `
        SELECT user_id,
               username,
               avatar,
               phone,
               email,
               gender,
               region,
               signature,
               birth_date
        FROM user
        WHERE user_id = ?
    `;

    db.query(query, [userId], (err, results) => {
        if (err) {
            console.error('Database query error:', err);
            return res.status(500).json({message: 'Server error'});
        }

        if (results.length === 0) {
            return res.status(404).json({message: 'User not found'});
        }

        // 返回用户信息（不包括密码）
        res.status(200).json(results[0]);
    });
});


// 获取用户信息的路由
router.get('/:id', (req, res) => {
    const userId = req.params.id;

    // 确保用户 ID 存在
    if (!userId) {
        return res.status(400).json({message: 'User ID is required'});
    }

    // 查询用户信息
    db.query('SELECT user_id, username FROM user WHERE user_id = ?', [userId], (err, results) => {
        if (err) {
            console.error('Database query error:', err);
            return res.status(500).json({message: 'Server error'});
        }

        if (results.length === 0) {
            return res.status(404).json({message: 'User not found'});
        }

        // 返回用户信息
        res.status(200).json(results[0]);
    });
});


// 获取用户基础信息（用于会话展示）
router.get('/simple/:id', (req, res) => {
    const userId = req.params.id;

    // 参数检查
    if (!userId) {
        return res.status(400).json({message: 'User ID is required'});
    }

    const sql = 'SELECT username, avatar FROM user WHERE user_id = ?';

    db.query(sql, [userId], (err, results) => {
        if (err) {
            console.error('Database query error:', err);
            return res.status(500).json({message: 'Server error'});
        }

        if (results.length === 0) {
            return res.status(404).json({message: 'User not found'});
        }

        // 只返回必要字段
        const user = results[0];
        res.status(200).json({
            name: user.username,
            avatar: user.avatar
        });
    });
});


// 注册路由（不再处理文件上传）
router.post('/register', async (req, res) => {
    const {username, password, avatarUrl} = req.body;
    if (!username || !password) {
        return res.status(400).json({message: 'Username, password are required'});
    }

    // 检查用户名
    db.query('SELECT * FROM user WHERE username = ?', [username], (err, results) => {
        if (err) return res.status(500).json({message: 'Server error'});
        if (results.length > 0) {
            return res.status(400).json({message: 'Username already exists'});
        }

        // 加密密码并保存
        bcrypt.hash(password, 10, (err, hashed) => {
            if (err) return res.status(500).json({message: 'Server error'});
            const sql = 'INSERT INTO user (username, password, avatar) VALUES (?, ?, ?)';
            db.query(sql, [username, hashed, avatarUrl || ''], (err) => {
                if (err) return res.status(500).json({message: 'Server error'});
                res.status(201).json({message: 'User registered successfully'});
            });
        });
    });
});


// 登录接口
router.post('/login', (req, res) => {
    const {username, password} = req.body;

    if (!username || !password) {
        return res.status(400).json({message: '用户名和密码不能为空'});
    }

    // 查询用户是否存在
    db.query('SELECT * FROM user WHERE username = ?', [username], (err, results) => {
        if (err) {
            console.error('数据库查询错误:', err);
            return res.status(500).json({message: '服务器错误'});
        }

        if (results.length === 0) {
            return res.status(401).json({message: '用户不存在'});
        }

        const user = results[0];

        // 比较密码（bcrypt 解密）
        bcrypt.compare(password, user.password, (err, isMatch) => {
            if (err) {
                console.error('密码比较错误:', err);
                return res.status(500).json({message: '服务器错误'});
            }

            if (!isMatch) {
                return res.status(401).json({message: '密码错误'});
            }

            // 登录成功，返回基本用户信息
            res.status(200).json({
                message: '登录成功',
                user: {
                    userId: user.user_id ?? -1,
                    username: user.username ?? "",
                    password: "", // 出于安全考虑，密码不返回
                    nickname: user.nickname ?? "",
                    avatar: user.avatar ?? "",
                    phone: user.phone ?? "",
                    email: user.email ?? "",
                    gender: user.gender ?? 0,
                    region: user.region ?? "",
                    signature: user.signature ?? "",
                    birthDate: user.birth_date ?? ""
                }
            });
        });
    });
});

router.post('/search_user', (req, res) => {
    const { query } = req.body;

    if (!query || typeof query !== 'string') {
        return res.status(400).json({ message: '缺少搜索关键字' });
    }

    const sql = 'SELECT user_id, username, avatar FROM user WHERE username LIKE ? LIMIT 20';
    const params = [`%${query.trim()}%`];

    db.query(sql, params, (err, results) => {
        if (err) {
            console.error('搜索用户失败:', err);
            return res.status(500).json({ message: '服务器错误' });
        }
        res.status(200).json(results);
    });
});

// 删除账户接口
router.post('/deactivate/:id', (req, res) => {
    const userId = req.params.id;

    if (!userId) {
        return res.status(400).json({ message: '缺少 user_id 参数' });
    }

    const updateUserSql = `
        UPDATE user 
        SET 
            username = CONCAT('deleted_', user_id),
            avatar = '/update/users/avatar/avatar_deleted.png',
            nickname = '用户已注销',
            phone = NULL,
            email = NULL,
            region = NULL,
            signature = NULL,
            birth_date = NULL
        WHERE user_id = ?
    `;

    const updateMediaSql = `
        UPDATE mediaresource 
        SET user_id = NULL 
        WHERE user_id = ?
    `;

    const removeFromGroupsSql = `
        DELETE FROM groupmember 
        WHERE member_id = ?
    `;

    const updateFriendRequestsSql = `
        UPDATE friendrequest 
        SET request_msg = '（对方已注销）'
        WHERE from_user_id = ? OR to_user_id = ?
    `;

    db.getConnection((err, conn) => {
        if (err) return res.status(500).json({ message: '数据库连接失败' });

        conn.beginTransaction(async err => {
            if (err) {
                conn.release();
                return res.status(500).json({ message: '开启事务失败' });
            }

            try {
                await conn.promise().query(updateUserSql, [userId]);
                await conn.promise().query(updateMediaSql, [userId]);
                await conn.promise().query(removeFromGroupsSql, [userId]);
                await conn.promise().query(updateFriendRequestsSql, [userId, userId]);

                conn.commit(err => {
                    conn.release();
                    if (err) return res.status(500).json({ message: '事务提交失败' });
                    res.status(200).json({ message: '用户已注销，数据处理完成' });
                });
            } catch (e) {
                conn.rollback(() => conn.release());
                console.error('注销失败:', e);
                res.status(500).json({ message: '用户注销失败' });
            }
        });
    });
});

// 修改密码接口
router.post('/change_password', async (req, res) => {
    const { userId, currentPassword, newPassword } = req.body;

    if (!userId || !currentPassword || !newPassword) {
        return res.status(400).json({ message: '缺少必要参数' });
    }

    // 先查找用户
    db.query('SELECT * FROM user WHERE user_id = ?', [userId], async (err, results) => {
        if (err) {
            console.error('数据库查询错误:', err);
            return res.status(500).json({ message: '服务器错误' });
        }

        if (results.length === 0) {
            return res.status(404).json({ message: '用户不存在' });
        }

        const user = results[0];

        // 验证当前密码
        const isMatch = await bcrypt.compare(currentPassword, user.password);
        if (!isMatch) {
            return res.status(401).json({ message: '当前密码不正确' });
        }

        // 加密新密码并更新
        bcrypt.hash(newPassword, 10, (err, hashedPassword) => {
            if (err) {
                console.error('密码加密失败:', err);
                return res.status(500).json({ message: '服务器错误' });
            }

            db.query('UPDATE user SET password = ? WHERE user_id = ?', [hashedPassword, userId], (err) => {
                if (err) {
                    console.error('密码更新失败:', err);
                    return res.status(500).json({ message: '服务器错误' });
                }

                res.status(200).json({ message: '密码修改成功' });
            });
        });
    });
});

router.post('/update_profile', async (req, res) => {
    let {
        userId,
        username,
        phone,
        email,
        gender,
        birthday,
        region,
        signature
    } = req.body;

// 强制转换 gender 为 int（如无效则默认 0）
    gender = parseInt(gender);
    if (isNaN(gender)) gender = 0;


    if (!userId || !username) {
        return res.status(400).json({ message: '缺少必要参数：userId 或 username' });
    }

    const sql = `
        UPDATE user SET
            username = ?,
            phone = ?,
            email = ?,
            gender = ?,
            birth_date = ?,
            region = ?,
            signature = ?
        WHERE user_id = ?
    `;

    const values = [username, phone, email, gender, birthday, region, signature, userId];

    db.query(sql, values, (err, result) => {
        if (err) {
            console.error('更新用户信息失败:', err);
            return res.status(500).json({ message: '服务器错误' });
        }

        if (result.affectedRows === 0) {
            return res.status(404).json({ message: '用户不存在' });
        }

        res.status(200).json({ message: '用户信息更新成功' });
    });
});



module.exports = router;
