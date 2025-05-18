const express = require('express')
const multer = require('multer')
const path = require('path')
const fs = require('fs')
const db = require('../db')
const router = express.Router()

// 获取群组基础信息（用于会话展示）
router.get('/simple/:id', (req, res) => {
    const groupId = req.params.id;

    // 参数校验
    if (!groupId) {
        return res.status(400).json({message: 'Group ID is required'});
    }

    const sql = 'SELECT group_name, avatar FROM `group` WHERE group_id = ?';

    db.query(sql, [groupId], (err, results) => {
        if (err) {
            console.error('Database query error:', err);
            return res.status(500).json({message: 'Server error'});
        }

        if (results.length === 0) {
            return res.status(404).json({message: 'Group not found'});
        }

        const group = results[0];
        res.status(200).json({
            name: group.group_name,
            avatar: group.avatar
        });
    });
});

// 查询用户所在的所有群聊信息
router.get('/list/:userId', (req, res) => {
    const { userId } = req.params;

    const sql = `
        SELECT g.*
        FROM groupmember gm
        JOIN \`group\` g ON gm.group_id = g.group_id
        WHERE gm.member_id = ?
    `;

    db.query(sql, [userId], (err, results) => {
        if (err) {
            console.error('获取群聊列表失败:', err);
            return res.status(500).json({ message: '服务器错误' });
        }
        res.status(200).json(results);
    });
});


// 更新群组头像字段
router.post('/update_avatar', (req, res) => {
    const {group_id, avatar_url} = req.body;

    if (!group_id || !avatar_url) {
        return res.status(400).json({message: 'group_id 和 avatar_url 是必需的'});
    }

    const sql = 'UPDATE `group` SET avatar = ? WHERE group_id = ?';

    db.query(sql, [avatar_url, group_id], (err, result) => {
        if (err) {
            console.error('更新 group avatar 失败:', err);
            return res.status(500).json({message: '服务器错误，更新失败'});
        }

        res.status(200).json({message: '群组头像已更新成功'});
    });
});


module.exports = router