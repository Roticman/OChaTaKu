// routes/upload.js
const express = require('express');
const multer  = require('multer');
const path    = require('path');
const fs      = require('fs');

const router = express.Router();

// 根目录：upload
const baseUpload = path.join(__dirname, '../upload');
// 子目录
const voiceDir  = path.join(baseUpload, 'messages', 'voices');
const videoDir  = path.join(baseUpload, 'messages', 'videos');
const imageDir  = path.join(baseUpload, 'messages', 'images');
const usersAvatarDir = path.join(baseUpload, 'users',    'avatar');
const groupsAvatarDir = path.join(baseUpload, 'groups',    'avatar');

// 确保目录存在
[voiceDir, videoDir, imageDir, usersAvatarDir, groupsAvatarDir].forEach(dir => {
  if (!fs.existsSync(dir)) {
    fs.mkdirSync(dir, { recursive: true });
  }
});

// 存储配置工厂
function storageFactory(destDir, prefix) {
  return multer.diskStorage({
    destination: (req, file, cb) => cb(null, destDir),
    filename:    (req, file, cb) => {
      const ext  = path.extname(file.originalname);
      const name = `${prefix}-${Date.now()}-${Math.round(Math.random()*1e9)}`;
      cb(null, name + ext);
    }
  });
}

// 不同类型的 upload middlewares
const uploadVoice  = multer({ storage: storageFactory(voiceDir,  'voice') });
const uploadVideo  = multer({ storage: storageFactory(videoDir,  'video') });
const uploadImage  = multer({ storage: storageFactory(imageDir,  'image') });
const uploadUserAvatar = multer({ storage: storageFactory(usersAvatarDir, 'avatar') });
const uploadGroupAvatar = multer({ storage: storageFactory(groupsAvatarDir, 'avatar') });

// 语音上传
router.post(
  '/api/upload/messages/voices',
  uploadVoice.single('file'),
  (req, res) => {
    if (!req.file) {
      return res.status(400).json({ message: '语音文件未上传' });
    }
    res.json({ url: `/upload/messages/voices/${req.file.filename}` });
  }
);

// 视频上传
router.post(
  '/api/upload/messages/videos',
  uploadVideo.single('file'),
  (req, res) => {
    if (!req.file) {
      return res.status(400).json({ message: '视频文件未上传' });
    }
    res.json({ url: `/upload/messages/videos/${req.file.filename}` });
  }
);

// 图片上传
router.post(
  '/api/upload/messages/images',
  uploadImage.single('file'),
  (req, res) => {
    if (!req.file) {
      return res.status(400).json({ message: '图片文件未上传' });
    }
    res.json({ url: `/upload/messages/images/${req.file.filename}` });
  }
);

// 用户头像上传
router.post(
  '/api/upload/users/avatar',
  uploadUserAvatar.single('avatar'),
  (req, res) => {
    if (!req.file) {
      return res.status(400).json({ message: '头像文件未上传' });
    }
    res.json({ url: `/upload/users/avatar/${req.file.filename}` });
  }
);

//群头像上传
router.post(
    '/api/upload/groups/avatar',
    uploadGroupAvatar.single('avatar'),
    (req, res) => {
        if (!req.file) {
            return res.status(400).json({ message: '头像文件未上传' });
        }
        res.json({ url: `/upload/groups/avatar/${req.file.filename}` });
    }
);

module.exports = router;