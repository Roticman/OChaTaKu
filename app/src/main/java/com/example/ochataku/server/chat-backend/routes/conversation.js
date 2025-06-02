const db = require('../db');
const express = require('express');
const router = express.Router();

// 添加一条会话记录
router.post('/add', (req, res) => {
    const {userId, peerId, peerName, isGroup, lastMessage, timestamp} = req.body;

    // === ✅ 新增校验逻辑：群聊不能设置备注名 ===
    const finalPeerName = isGroup ? null : peerName;

    const sql = `
        INSERT INTO conversation (a_id, b_id, group_id, is_group, last_message, timestamp)
        VALUES (?, ?, ?, ?, ?, ?)
        ON DUPLICATE KEY
            UPDATE last_message =
                       VALUES(last_message),
                   timestamp    =
                       VALUES(timestamp)
    `;

    db.query(sql, [userId, peerId, finalPeerName, isGroup, lastMessage, timestamp], (err, result) => {
        if (err) {
            console.error('数据库插入失败:', err);
            res.status(500).json({message: '服务器错误'});
        } else {
            res.status(200).json({message: '添加成功'});
        }
    });
});

router.post('/delete', (req, res) => {
    const { convId } = req.body;

    const deleteMessagesSql = `DELETE FROM message WHERE conv_id = ?`;
    const deleteConversationSql = `DELETE FROM conversation WHERE conv_id = ?`;

    db.query(deleteMessagesSql, [convId], (err1) => {
        if (err1) return res.status(500).json({ message: '删除消息失败' });

        db.query(deleteConversationSql, [convId], (err2) => {
            if (err2) return res.status(500).json({ message: '删除会话失败' });

            return res.status(200).json({ message: '删除成功' });
        });
    });
});



/**
 * 获取某用户的会话列表：
 *  • 私聊：is_group=0 且 user_id=?
 *  • 群聊：is_group=1 且 user 在 group_member 表里
 */
router.get('/list/:userId', (req, res) => {
    const {userId} = req.params;

    const sql = `
        SELECT c.*
        FROM conversation AS c
                 LEFT JOIN groupmember AS gm
                           ON c.group_id = gm.group_id AND gm.member_id = ?
        WHERE (c.is_group = 0 AND (c.a_id = ? OR c.b_id = ?))
           OR (c.is_group = 1 AND gm.member_id IS NOT NULL)
        ORDER BY c.timestamp DESC
    `;
    db.query(sql, [userId, userId, userId], (err, results) => {
        if (err) {
            console.error('获取会话列表失败:', err);
            return res.status(500).json({message: '服务器错误'});
        }
        res.status(200).json(results);
    });
});


router.post('/get_or_create', (req, res) => {
    const { userId, peerId, isGroup } = req.body;

    if (!userId || !peerId || isGroup === undefined) {
        return res.status(400).json({ message: '参数缺失' });
    }

    const now = Date.now();
    const defaultMessage = isGroup ? '欢迎加入群聊！' : '你们已经是好友了，开始聊天吧';

    if (isGroup) {
        // ✅ 群聊查找是否已有对应会话
        const findGroupSql = `
            SELECT conv_id
            FROM conversation
            WHERE group_id = ? AND is_group = 1
        `;

        db.query(findGroupSql, [peerId], (err, results) => {
            if (err) {
                console.error('查找群聊会话失败:', err);
                return res.status(500).json({ message: '服务器错误' });
            }

            if (results.length > 0) {
                return res.status(200).json({ convId: results[0].conv_id });
            }

            // 插入新群聊会话
            const insertGroupConvSql = `
                INSERT INTO conversation (a_id, b_id, group_id, is_group, last_message, timestamp)
                VALUES (NULL, NULL, ?, 1, ?, ?)
            `;

            db.query(insertGroupConvSql, [peerId, defaultMessage, now], (err2, result2) => {
                if (err2) {
                    console.error('创建群聊会话失败:', err2);
                    return res.status(500).json({ message: '创建失败' });
                }

                const convId = result2.insertId;

                const insertMsgSql = `
                    INSERT INTO message (sender_id, conv_id, is_group, content, timestamp, message_type)
                    VALUES (?, ?, 1, ?, ?, 'text')
                `;

                db.query(insertMsgSql, [userId, convId, defaultMessage, now], (err3) => {
                    if (err3) {
                        console.error('插入群聊欢迎消息失败:', err3);
                        return res.status(500).json({ message: '会话已建但消息失败' });
                    }

                    return res.status(200).json({ convId });
                });
            });
        });
    } else {
        // ✅ 私聊逻辑
        const a_id = Math.min(userId, peerId);
        const b_id = Math.max(userId, peerId);

        const findSql = `
            SELECT conv_id
            FROM conversation
            WHERE a_id = ? AND b_id = ? AND is_group = 0
        `;

        db.query(findSql, [a_id, b_id], (err, results) => {
            if (err) {
                console.error('查找私聊会话失败:', err);
                return res.status(500).json({ message: '服务器错误' });
            }

            if (results.length > 0) {
                return res.status(200).json({ convId: results[0].conv_id });
            }

            const insertConvSql = `
                INSERT INTO conversation (a_id, b_id, group_id, is_group, last_message, timestamp)
                VALUES (?, ?, NULL, 0, ?, ?)
            `;

            db.query(insertConvSql, [a_id, b_id, defaultMessage, now], (err2, result2) => {
                if (err2) {
                    console.error('创建私聊会话失败:', err2);
                    return res.status(500).json({ message: '创建失败' });
                }

                const convId = result2.insertId;

                const insertMsgSql = `
                    INSERT INTO message (sender_id, conv_id, is_group, content, timestamp, message_type)
                    VALUES (?, ?, 0, ?, ?, 'text')
                `;

                db.query(insertMsgSql, [userId, convId, defaultMessage, now], (err3) => {
                    if (err3) {
                        console.error('插入私聊打招呼消息失败:', err3);
                        return res.status(500).json({ message: '会话已建但消息失败' });
                    }

                    return res.status(200).json({ convId });
                });
            });
        });
    }
});



router.get('/list/group_member/:convId', (req, res ) => {
    const convId = req.params.convId;

    // 1. 先从 conversation 查出 group_id
    const sqlGroup = `SELECT group_id
                      FROM conversation
                      WHERE conv_id = ?`;
    db.query(sqlGroup, [convId], (err, results) => {
        if (err) {
            console.error('查询 group_id 失败:', err);
            return res.status(500).json({message: '服务器错误'});
        }
        if (results.length === 0 || !results[0].group_id) {
            // 没找到会话或不是群聊
            return res.json([]);
        }
        const groupId = results[0].group_id;

        // 2. 根据 group_id 去 groupmember 和 user 表查 avatar，最多 9 个
        const sqlMembers = `
            SELECT gm.member_id AS userId, u.avatar
            FROM groupmember AS gm
                     JOIN \`user\` AS u
                          ON gm.member_id = u.user_id
            WHERE gm.group_id = ?
            LIMIT 9
        `;
        db.query(sqlMembers, [groupId], (err2, members) => {
            if (err2) {
                console.error('查询群成员失败:', err2);
                return res.status(500).json({message: '服务器错误'});
            }
            // 返回 [{ userId, avatar }, ...]
            res.json(members);
        });
    });
});


router.get('/user_simple/:id', (req, res) => {
    const userId = req.params.id;

    const sql = 'SELECT username, avatar FROM user WHERE user_id = ?';

    db.query(sql, [userId], (err, results) => {
        if (err) {
            console.error('获取用户信息失败:', err);
            return res.status(500).json({message: '服务器错误'});
        }
        if (results.length === 0) {
            return res.status(404).json({message: '用户不存在'});
        }
        res.status(200).json(results[0]);
    });
});


router.get('/group_simple/:id', (req, res) => {
    const groupId = req.params.id;

    const sql = 'SELECT  group_name, avatar FROM `group` WHERE group_id = ?';

    db.query(sql, [groupId], (err, results) => {
        if (err) {
            console.error('获取群组信息失败:', err);
            return res.status(500).json({message: '服务器错误'});
        }
        if (results.length === 0) {
            return res.status(404).json({message: '群组不存在'});
        }
        res.status(200).json(results[0]);
    });
});


module.exports = router;
