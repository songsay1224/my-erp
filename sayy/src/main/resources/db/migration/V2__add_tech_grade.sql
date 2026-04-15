ALTER TABLE employees


    ADD COLUMN tech_grade VARCHAR(50) NULL COMMENT '기술등급 (예: 특급, 고급, 중급, 초급)' AFTER position_name;
