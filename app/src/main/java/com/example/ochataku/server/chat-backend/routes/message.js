const express = require('express');
const router = express.Router();
const db = require('../db');
let io = null;
const path = require('path');
const fs = require('fs');

function setSocketInstance(socketInstance) {
    io = socketInstance;
}

router.post('/send', (req, res) => {
    const {
        conv_id,
        a_id,
        b_id,
        group_id,
        is_group,
        sender_id,
        content,
        timestamp,
        message_type,
        media_url
    } = req.body;

    const isGroup = is_group ? 1 : 0;

    // 构造显示内容
    let displayMessage = '';
    switch (message_type) {
        case 'text':
            displayMessage = content;
            break;
        case 'image':
            displayMessage = '[图片]';
            break;
        case 'voice':
            displayMessage = '[语音]';
            break;
        case 'video':
            displayMessage = '[视频]';
            break;
        default:
            displayMessage = '[文件]';
    }

    // 如果已存在会话，直接写入消息（不使用事务）
    if (conv_id) {
        insertMessageWithUpdate(conv_id, displayMessage, sender_id, content, timestamp, isGroup, message_type, media_url, res);
    } else {
        // 否则使用事务：创建会话 + 写消息 + 更新会话
        db.getConnection((err, connection) => {
            if (err) {
                console.error('获取连接失败:', err);
                return res.status(500).json({message: '发送失败'});
            }

            connection.beginTransaction(err => {
                if (err) {
                    console.error('开启事务失败:', err);
                    connection.release();
                    return res.status(500).json({message: '发送失败'});
                }

                // 插入 conversation 表
                let convSql = '';
                let convVals = [];

                if (isGroup) {
                    convSql = `
                        INSERT INTO conversation (is_group, group_id)
                        VALUES (?, ?) ON DUPLICATE KEY
                        UPDATE conv_id = LAST_INSERT_ID(conv_id)
                    `;
                    convVals = [isGroup, group_id];
                } else {
                    const minId = Math.min(a_id, b_id);
                    const maxId = Math.max(a_id, b_id);
                    convSql = `
                        INSERT INTO conversation (a_id, b_id, is_group)
                        VALUES (?, ?, ?) ON DUPLICATE KEY
                        UPDATE conv_id = LAST_INSERT_ID(conv_id)
                    `;
                    convVals = [minId, maxId, isGroup];
                }

                connection.query(convSql, convVals, (err, result) => {
                    if (err) {
                        console.error('插入会话失败:', err);
                        return connection.rollback(() => {
                            connection.release();
                            return res.status(500).json({message: '发送失败（插入会话）'});
                        });
                    }

                    const newConvId = result.insertId;

                    // 插入 message 表
                    const msgSql = `
                        INSERT INTO message (conv_id, sender_id, is_group, content, timestamp, message_type, media_url)
                        VALUES (?, ?, ?, ?, ?, ?, ?)
                    `;
                    const msgVals = [
                        newConvId,
                        sender_id,
                        isGroup,
                        content,
                        timestamp,
                        message_type,
                        media_url
                    ];

                    connection.query(msgSql, msgVals, (err2) => {
                        if (err2) {
                            console.error('插入消息失败:', err2);
                            return connection.rollback(() => {
                                connection.release();
                                return res.status(500).json({message: '发送失败（写入消息）'});
                            });
                        }

                        // 更新 conversation.last_message
                        const updateSql = `
                            UPDATE conversation
                            SET last_message = ?,
                                timestamp    = NOW()
                            WHERE conv_id = ?
                        `;
                        connection.query(updateSql, [displayMessage, timestamp, newConvId], (err3) => {
                            if (err3) {
                                console.error('更新会话失败:', err3);
                                return connection.rollback(() => {
                                    connection.release();
                                    return res.status(500).json({message: '发送失败（更新会话）'});
                                });
                            }

                            // 提交事务
                            connection.commit(err4 => {
                                if (err4) {
                                    console.error('提交事务失败:', err4);
                                    return connection.rollback(() => {
                                        connection.release();
                                        return res.status(500).json({message: '发送失败（提交）'});
                                    });
                                }

                                connection.release();

                                // 查询用户信息，发回响应 + socket 推送
                                sendResponseAndPush(io, newConvId, sender_id, content, timestamp, isGroup, message_type, media_url, res);
                            });
                        });
                    });
                });
            });
        });
    }
});

function insertMessageWithUpdate(convId, displayMessage, sender_id, content, timestamp, isGroup, message_type, media_url, res) {
    const sqlMsg = `
        INSERT INTO message (conv_id, sender_id, is_group, content, timestamp, message_type, media_url)
        VALUES (?, ?, ?, ?, ?, ?, ?)
    `;
    const valsMsg = [
        convId,
        sender_id,
        isGroup,
        content,
        timestamp,
        message_type,
        media_url
    ];

    db.query(sqlMsg, valsMsg, (err, results) => {
        if (err) {
            console.error('消息写入失败:', err);
            return res.status(500).json({message: '发送失败'});
        }

        const messageId = results.insertId; // 获取插入消息的主键 id

        const updateSql = `
            UPDATE conversation
            SET last_message = ?,
                timestamp    = ?
            WHERE conv_id = ?
        `;
        db.query(updateSql, [displayMessage, timestamp, convId], (err2) => {
            if (err2) {
                console.error('更新会话信息失败:', err2);
            }
        });

        sendResponseAndPush(io, convId, sender_id, content, timestamp, isGroup, message_type, media_url, res, messageId);
    });
}


// 查询用户信息 + 推送 + 响应
function sendResponseAndPush(io, conv_id, sender_id, content, timestamp, is_group, message_type, media_url, res, messageId) {
    const sqlUser = 'SELECT username, avatar FROM user WHERE user_id = ?';
    db.query(sqlUser, [sender_id], (err2, userResults) => {
        if (err2 || userResults.length === 0) {
            console.error('查询发送者失败:', err2);
            return res.status(500).json({message: '发送者信息获取失败'});
        }

        const {username, avatar} = userResults[0];

        const messagePayload = {
            id: messageId,
            conv_id,
            sender_id,
            sender_name: username,
            sender_avatar: avatar,
            content,
            timestamp,
            is_group,
            message_type,
            media_url
        };

        if (io) {
            io.emit(`chat:${conv_id}`, messagePayload);
        }

        res.status(200).json({message: '发送成功', conv_id, media_url});
    });
}

router.get('/:convId', (req, res) => {
    const {convId} = req.params;

    const sql = `
        SELECT *
        FROM message
        WHERE conv_id = ?
        ORDER BY timestamp ASC
    `;
    db.query(sql, [convId], (err, results) => {
        if (err) {
            console.error('查询失败:', err);
            return res.status(500).json({message: '查询失败'});
        }
        res.status(200).json(results);
    });
});

// 上传根路径
const uploadRoot = path.join(__dirname, '../upload');

router.delete('/delete/:id', (req, res) => {
    const messageId = req.params.id;
    const userId = req.body.user_id || req.query.user_id; // 🧠 建议通过 token 校验，此处简化为参数传递

    if (!userId) {
        return res.status(400).json({message: '缺少 user_id 参数'});
    }

    const getMessageSql = 'SELECT * FROM message WHERE id = ?';
    db.query(getMessageSql, [messageId], (err1, results) => {
        if (err1) {
            console.error('查询消息失败:', err1);
            return res.status(500).json({message: '查询失败'});
        }

        if (results.length === 0) {
            return res.status(404).json({message: '消息不存在'});
        }

        const message = results[0];

        if (message.sender_id !== userId) {
            return res.status(403).json({message: '无权删除他人消息'});
        }

        // ✅ 删除媒体文件
        if (message.media_url) {
            // 去掉前缀 "/upload/"，得到相对路径，例如 "messages/voices/xxx.tmp"
            const relativePath = message.media_url.replace(/^\/?upload\/?/, '');
            const filePath = path.join(uploadRoot, relativePath);

            console.log('删除文件路径:', filePath);

            fs.unlink(filePath, (err2) => {
                if (err2 && err2.code !== 'ENOENT') {
                    console.error('删除文件失败:', err2);
                    return res.status(500).json({message: '删除文件失败'});
                }
                deleteMessageRecord();
            });
        } else {
            deleteMessageRecord();
        }


        function deleteMessageRecord() {
            const deleteSql = 'DELETE FROM message WHERE id = ?';
            db.query(deleteSql, [messageId], (err3) => {
                if (err3) {
                    console.error('删除消息失败:', err3);
                    return res.status(500).json({message: '删除失败'});
                }
                res.status(200).json({message: '消息删除成功'});
            });
        }
    });
});


// // 删除消息接口（支持媒体文件）
// router.delete('/delete/:id', (req, res) => {
//     const messageId = req.params.id;
//
//     const getMessageSql = 'SELECT * FROM message WHERE id = ?';
//     db.query(getMessageSql, [messageId], (err1, results) => {
//         if (err1) {
//             console.error('查询消息失败:', err1);
//             return res.status(500).json({ message: '查询失败' });
//         }
//
//         if (results.length === 0) {
//             return res.status(404).json({ message: '消息不存在' });
//         }
//
//         const message = results[0];
//
//         // 检查是否包含文件路径字段（假设字段叫 file_url）
//         if (message.file_url) {
//             const filePath = path.join(uploadRoot, message.file_url);
//             fs.unlink(filePath, (err2) => {
//                 if (err2 && err2.code !== 'ENOENT') {
//                     console.error('删除文件失败:', err2);
//                     return res.status(500).json({ message: '删除文件失败' });
//                 }
//                 console.log('文件已删除或不存在:', filePath);
//                 // 删除数据库记录
//                 deleteMessageRecord();
//             });
//         } else {
//             // 纯文本消息，直接删数据库
//             deleteMessageRecord();
//         }
//
//         function deleteMessageRecord() {
//             const deleteSql = 'DELETE FROM message WHERE id = ?';
//             db.query(deleteSql, [messageId], (err3) => {
//                 if (err3) {
//                     console.error('删除消息失败:', err3);
//                     return res.status(500).json({ message: '删除失败' });
//                 }
//                 res.status(200).json({ message: '消息删除成功' });
//             });
//         }
//     });
// });

module.exports = {
    router,
    setSocketInstance
};
