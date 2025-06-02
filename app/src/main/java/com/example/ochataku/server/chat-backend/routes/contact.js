const express = require('express');
const db = require('../db');
const router = express.Router();

// 添加联系人接口
router.post('/add_contact', (req, res) => {
    const {user_id, peer_id} = req.body;

    if (!user_id || !peer_id) {
        return res.status(400).json({message: '缺少 user_id 或 peer_id'});
    }

    const sql = 'INSERT INTO contact (user_id, peer_id) VALUES (?, ?)';
    db.query(sql, [user_id, peer_id], (err, result) => {
        if (err) {
            console.error('添加联系人失败', err);
            return res.status(500).json({message: '服务器内部错误'});
        }

        res.status(200).json({message: '添加成功', id: result.insertId});
    });
});

// 发送好友请求接口
router.post('/send_request', (req, res) => {
    const {from_user_id, to_user_id} = req.body;

    if (!from_user_id || !to_user_id) {
        return res.status(400).json({message: '缺少 from_user_id 或 to_user_id'});
    }

    if (from_user_id === to_user_id) {
        return res.status(400).json({message: '不能添加自己为好友'});
    }

    const checkRequestSql = `
        SELECT *
        FROM friendrequest
        WHERE from_user_id = ?
          AND to_user_id = ?
          AND status = 0
    `;
    db.query(checkRequestSql, [from_user_id, to_user_id], (err1, requestResults) => {
        if (err1) {
            console.error('检查好友请求失败:', err1);
            return res.status(500).json({message: '服务器内部错误'});
        }

        if (requestResults.length > 0) {
            return res.status(400).json({message: '已有待处理的好友请求'});
        }

        const checkContactSql = `
            SELECT *
            FROM contact
            WHERE (user_id = ? AND peer_id = ?)
               OR (user_id = ? AND peer_id = ?)
        `;
        db.query(checkContactSql, [from_user_id, to_user_id, to_user_id, from_user_id], (err2, contactResults) => {
            if (err2) {
                console.error('检查联系人失败:', err2);
                return res.status(500).json({message: '服务器内部错误'});
            }

            if (contactResults.length > 0) {
                return res.status(400).json({message: '你们已经是好友了'});
            }

            const insertSql = `
                INSERT INTO friendrequest (from_user_id, to_user_id)
                VALUES (?, ?)
            `;
            db.query(insertSql, [from_user_id, to_user_id], (err3, insertResult) => {
                if (err3) {
                    console.error('插入好友请求失败:', err3);
                    return res.status(500).json({message: '服务器内部错误'});
                }

                res.status(200).json({message: '好友请求发送成功', id: insertResult.insertId});
            });
        });
    });
});

// 处理好友请求接口
router.post('/handle_request', (req, res) => {
    const {request_id, action} = req.body;

    if (!request_id || !['accept', 'reject'].includes(action)) {
        return res.status(400).json({message: '参数错误'});
    }

    const getRequestSql = 'SELECT * FROM friendrequest WHERE request_id = ?';
    db.query(getRequestSql, [request_id], (err1, requests) => {
        if (err1) {
            console.error('查询请求失败:', err1);
            return res.status(500).json({message: '服务器内部错误'});
        }

        if (requests.length === 0) {
            return res.status(404).json({message: '好友请求不存在'});
        }

        const request = requests[0];

        if (request.status !== 0) {
            return res.status(400).json({message: '该请求已处理'});
        }

        if (action === 'accept') {
            // 先查询双方用户名
            const userSql = 'SELECT user_id, username FROM user WHERE user_id IN (?, ?)';
            db.query(userSql, [request.from_user_id, request.to_user_id], (errUsers, userResults) => {
                if (errUsers) {
                    console.error('查询用户信息失败:', errUsers);
                    return res.status(500).json({message: '服务器内部错误'});
                }

                if (userResults.length !== 2) {
                    return res.status(400).json({message: '用户信息不完整'});
                }

                const fromUser = userResults.find(u => u.user_id === request.from_user_id);
                const toUser = userResults.find(u => u.user_id === request.to_user_id);

                const insertContacts = `
                    INSERT INTO contact (user_id, peer_id, remark_name)
                    VALUES (?, ?, ?),
                           (?, ?, ?)
                `;
                const params = [
                    request.from_user_id, request.to_user_id, toUser.username,
                    request.to_user_id, request.from_user_id, fromUser.username
                ];

                db.query(insertContacts, params, (err2) => {
                    if (err2) {
                        console.error('插入联系人失败:', err2);
                        return res.status(500).json({message: '服务器内部错误'});
                    }

                    db.query('UPDATE friendrequest SET status = 1 WHERE request_id = ?', [request_id], (err3) => {
                        if (err3) {
                            console.error('更新请求状态失败:', err3);
                            return res.status(500).json({message: '服务器内部错误'});
                        }

                        res.status(200).json({message: '好友请求已接受'});
                    });
                });
            });
        } else {
            db.query('UPDATE friendrequest SET status = 2 WHERE request_id = ?', [request_id], (err4) => {
                if (err4) {
                    console.error('更新请求状态失败:', err4);
                    return res.status(500).json({message: '服务器内部错误'});
                }

                res.status(200).json({message: '好友请求已拒绝'});
            });
        }
    });
});

// 获取指定用户收到的好友请求
router.get('/friend_requests/:id', (req, res) => {
    const toUserId = req.params.id;

    if (!toUserId) {
        return res.status(400).json({message: 'toUser ID is required'});
    }

    db.query('SELECT * FROM friendrequest WHERE to_user_id = ?', [toUserId], (err, results) => {
        if (err) {
            console.error('Database query error:', err);
            return res.status(500).json({message: 'Server error'});
        }

        if (results.length === 0) {
            return res.status(404).json({message: 'User not found'});
        }

        res.status(200).json(results);
    });
});

// 获取指定用户的联系人列表
router.get('/:userId', (req, res) => {
    const {userId} = req.params;

    const sql = 'SELECT * FROM contact WHERE user_id = ?';
    db.query(sql, [userId], (err, results) => {
        if (err) return res.status(500).send(err);
        res.send(results);
    });
});

// 获取联系人简要信息
router.get('/contact_simple/:id', (req, res) => {
    const userId = req.params.id;

    if (!userId) {
        return res.status(400).json({message: 'User ID is required'});
    }

    const sql = 'SELECT user_id, username, avatar FROM user WHERE user_id = ?';
    db.query(sql, [userId], (err, results) => {
        if (err) {
            console.error('Database query error:', err);
            return res.status(500).json({message: 'Server error'});
        }

        if (results.length === 0) {
            return res.status(404).json({message: 'User not found'});
        }

        const user = results[0];
        res.status(200).json({
            user_id: user.user_id,
            name: user.username,
            avatar: user.avatar
        });
    });
});

module.exports = router;
