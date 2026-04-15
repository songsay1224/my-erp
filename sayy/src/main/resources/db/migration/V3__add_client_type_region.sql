ALTER TABLE clients
    ADD COLUMN client_type VARCHAR(20) NULL COMMENT '거래 구분 (매출/매입/매출매입)' AFTER id,
    ADD COLUMN region VARCHAR(50) NULL COMMENT '지역 (시도)' AFTER address_detail;
