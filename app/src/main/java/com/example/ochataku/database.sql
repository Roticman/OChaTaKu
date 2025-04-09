-- 1. 用户表
CREATE TABLE users (
    id            BIGINT PRIMARY KEY AUTO_INCREMENT,   -- 用户 ID
    username      VARCHAR(50) UNIQUE NOT NULL,        -- 用户名（唯一）
    email         VARCHAR(100) UNIQUE NOT NULL,       -- 邮箱（唯一）
    password_hash VARCHAR(255) NOT NULL,             -- 哈希加密的密码
    avatar_url    VARCHAR(255),                       -- 头像链接
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP -- 注册时间
);

-- 2. 私聊消息表
CREATE TABLE private_messages (
    id           BIGINT PRIMARY KEY AUTO_INCREMENT,
    sender_id    BIGINT NOT NULL,        -- 发送者
    receiver_id  BIGINT NOT NULL,        -- 接收者
    content      TEXT NOT NULL,          -- 消息内容
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_read      BOOLEAN DEFAULT FALSE,  -- 是否已读
    deleted_at   TIMESTAMP NULL,         -- 软删除
    FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (receiver_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX (receiver_id, is_read),        -- 查询未读消息
    INDEX (deleted_at)                    -- 软删除查询
);

-- 3. 群组表
CREATE TABLE groups (
    id         BIGINT PRIMARY KEY AUTO_INCREMENT,
    name       VARCHAR(100) NOT NULL,        -- 群组名称
    owner_id   BIGINT NOT NULL,              -- 群主 ID
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 4. 群组成员表
CREATE TABLE group_members (
    id         BIGINT PRIMARY KEY AUTO_INCREMENT,
    group_id   BIGINT NOT NULL,
    user_id    BIGINT NOT NULL,
    joined_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (group_id) REFERENCES groups(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE (group_id, user_id) -- 防止重复加入
);

-- 5. 群聊消息表
CREATE TABLE group_messages (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    group_id    BIGINT NOT NULL,       -- 群组 ID
    sender_id   BIGINT NOT NULL,       -- 发送者 ID
    content     TEXT NOT NULL,         -- 消息内容
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_at  TIMESTAMP NULL,        -- 软删除
    FOREIGN KEY (group_id) REFERENCES groups(id) ON DELETE CASCADE,
    FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX (group_id, created_at),
    INDEX (deleted_at) -- 软删除查询
);

-- 6. 商品表（谷子交易）
CREATE TABLE items (
    id            BIGINT PRIMARY KEY AUTO_INCREMENT,
    owner_id      BIGINT NOT NULL,          -- 发布者
    title         VARCHAR(255) NOT NULL,    -- 商品标题
    description   TEXT,                      -- 商品描述
    price         DECIMAL(10,2) NOT NULL,   -- 价格
    status        ENUM('available', 'sold') DEFAULT 'available', -- 交易状态
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 7. 订单表
CREATE TABLE orders (
    id           BIGINT PRIMARY KEY AUTO_INCREMENT,
    buyer_id     BIGINT NOT NULL,         -- 买家 ID
    item_id      BIGINT NOT NULL,         -- 商品 ID
    status       ENUM('pending', 'completed', 'cancelled') DEFAULT 'pending', -- 订单状态
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (buyer_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (item_id) REFERENCES items(id) ON DELETE CASCADE
);

-- 8. 交易记录表
CREATE TABLE transactions (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id    BIGINT NOT NULL,          -- 关联订单
    seller_id   BIGINT NOT NULL,          -- 卖家 ID
    buyer_id    BIGINT NOT NULL,          -- 买家 ID
    amount      DECIMAL(10,2) NOT NULL,   -- 成交金额
    completed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- 交易完成时间
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    FOREIGN KEY (seller_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (buyer_id) REFERENCES users(id) ON DELETE CASCADE
);
