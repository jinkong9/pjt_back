CREATE TABLE IF NOT EXISTS rental_notice_cache (
    notice_id VARCHAR(40) PRIMARY KEY,
    title VARCHAR(300) NOT NULL,
    region_name VARCHAR(80),
    notice_type VARCHAR(80),
    detail_type VARCHAR(80),
    status VARCHAR(40),
    notice_date VARCHAR(20),
    close_date VARCHAR(20),
    detail_url VARCHAR(1000),
    ccr_cnnt_sys_ds_cd VARCHAR(10),
    upp_ais_tp_cd VARCHAR(10),
    ais_tp_cd VARCHAR(10),
    spl_inf_tp_cd VARCHAR(10),
    source VARCHAR(20),
    cached_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS analysis_snapshot (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    label VARCHAR(120) NOT NULL,
    latitude DOUBLE NOT NULL,
    longitude DOUBLE NOT NULL,
    radius_m INT NOT NULL,
    commercial_count INT NOT NULL,
    traffic_event_count INT NOT NULL,
    score INT NOT NULL,
    risk_level VARCHAR(30) NOT NULL,
    source VARCHAR(20) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS members (
    user_id VARCHAR(50) PRIMARY KEY,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    phone VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS favorite_deals (
    user_id VARCHAR(50) NOT NULL,
    deal_no INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, deal_no)
);

CREATE TABLE IF NOT EXISTS notices (
    notice_id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    writer VARCHAR(50) NOT NULL,
    popup_enabled BOOLEAN DEFAULT FALSE,
    pinned BOOLEAN DEFAULT FALSE,
    visible BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS transfers (
    transfer_id INT AUTO_INCREMENT PRIMARY KEY,
    writer_id VARCHAR(50) NOT NULL,
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    status VARCHAR(30) NOT NULL,
    address VARCHAR(255) NOT NULL,
    detail_address VARCHAR(255),
    floor VARCHAR(30),
    exclusive_area DECIMAL(10, 2),
    deposit_amount INT NOT NULL DEFAULT 0,
    monthly_rent_amount INT NOT NULL DEFAULT 0,
    maintenance_fee INT NOT NULL DEFAULT 0,
    transfer_fee INT NOT NULL DEFAULT 0,
    contract_end_date DATE,
    move_in_date DATE,
    contact_phone VARCHAR(50),
    view_count INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS transfer_images (
    image_id INT AUTO_INCREMENT PRIMARY KEY,
    transfer_id INT NOT NULL,
    image_url VARCHAR(1000) NOT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_transfer_images_transfer
        FOREIGN KEY (transfer_id) REFERENCES transfers(transfer_id)
        ON DELETE CASCADE
);
