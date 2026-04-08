-- =====================================================
-- V1: 전체 초기 스키마 (현재 운영 중인 DB 기준)
-- =====================================================

-- 구성원
CREATE TABLE IF NOT EXISTS employees (
  emp_no VARCHAR(30) NOT NULL,
  login_username VARCHAR(50) NULL,
  name VARCHAR(50) NOT NULL,
  name_en VARCHAR(100) NULL,
  employment_type VARCHAR(20) NULL,
  employment_status VARCHAR(20) NULL,
  four_insured BOOLEAN NULL,
  tax_scheme VARCHAR(30) NULL,
  contract_end_date DATE NULL,
  hire_date DATE NULL,
  group_hire_date DATE NULL,
  position_name VARCHAR(100) NULL,
  job_names VARCHAR(255) NULL,
  identification_number VARCHAR(20) NULL,
  age INT NULL,
  years_of_service INT NULL,
  email VARCHAR(100) NULL,
  birth_date DATE NULL,
  termination_date DATE NULL,
  hr_memo VARCHAR(1000) NULL,
  -- 개인정보
  gender VARCHAR(10) NULL,
  nationality VARCHAR(50) NULL,
  phone VARCHAR(30) NULL,
  bank_name VARCHAR(50) NULL,
  bank_account VARCHAR(50) NULL,
  bank_holder VARCHAR(50) NULL,
  -- 계약정보 (근로계약)
  labor_contract_start DATE NULL,
  labor_contract_end DATE NULL,
  probation_start DATE NULL,
  probation_end DATE NULL,
  probation_pay_rate INT NULL,
  -- 계약정보 (임금계약)
  income_type VARCHAR(30) NULL,
  wage_contract_start DATE NULL,
  wage_contract_end DATE NULL,
  monthly_salary BIGINT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (emp_no),
  UNIQUE KEY uk_employees_login_username (login_username)
);

-- 사용자 계정
CREATE TABLE IF NOT EXISTS users (
  username VARCHAR(50) NOT NULL,
  pwd VARCHAR(255) NOT NULL,
  role VARCHAR(20) NOT NULL DEFAULT 'USER',
  enabled BOOLEAN NOT NULL DEFAULT TRUE,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  employee_no VARCHAR(30) NULL,
  PRIMARY KEY (username),
  UNIQUE KEY uk_users_employee_no (employee_no),
  CONSTRAINT fk_users_employee
    FOREIGN KEY (employee_no) REFERENCES employees(emp_no)
);

-- 공휴일
CREATE TABLE IF NOT EXISTS holidays (
  loc_date DATE NOT NULL,
  name VARCHAR(50) NOT NULL,
  is_holiday BOOLEAN NOT NULL DEFAULT TRUE,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (loc_date)
);

-- 커스텀 휴무일
CREATE TABLE IF NOT EXISTS days_off_custom (
  id BIGINT NOT NULL AUTO_INCREMENT,
  loc_date DATE NOT NULL,
  name VARCHAR(50) NOT NULL,
  repeat_annually BOOLEAN NOT NULL DEFAULT FALSE,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_days_off_custom_loc_date (loc_date)
);

-- 회사 정보 (단일 레코드: id=1)
CREATE TABLE IF NOT EXISTS company_info (
  id INT NOT NULL,
  company_name VARCHAR(100) NOT NULL,
  ceo_name VARCHAR(50) NULL,
  phone VARCHAR(30) NULL,
  founded_date DATE NULL,
  zip_code VARCHAR(10) NULL,
  address VARCHAR(255) NULL,
  address_detail VARCHAR(255) NULL,
  business_reg_no VARCHAR(30) NULL,
  corporate_reg_no VARCHAR(30) NULL,
  logo_path VARCHAR(255) NULL,
  seal_path VARCHAR(255) NULL,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
);

-- 조직도
CREATE TABLE IF NOT EXISTS org_units (
  id BIGINT NOT NULL AUTO_INCREMENT,
  name VARCHAR(100) NOT NULL,
  parent_id BIGINT NULL,
  sort_order INT NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_org_units_parent (parent_id),
  CONSTRAINT fk_org_units_parent
    FOREIGN KEY (parent_id) REFERENCES org_units(id)
    ON DELETE RESTRICT
);

-- 구성원 경력
CREATE TABLE IF NOT EXISTS employee_careers (
  id BIGINT NOT NULL AUTO_INCREMENT,
  emp_no VARCHAR(30) NOT NULL,
  company_name VARCHAR(100) NOT NULL,
  department VARCHAR(100) NULL,
  position VARCHAR(100) NULL,
  start_date DATE NOT NULL,
  end_date DATE NULL,
  description VARCHAR(500) NULL,
  sort_order INT NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_employee_careers_emp_no (emp_no),
  CONSTRAINT fk_employee_careers_employee
    FOREIGN KEY (emp_no) REFERENCES employees(emp_no) ON DELETE CASCADE
);

-- 구성원 학력
CREATE TABLE IF NOT EXISTS employee_educations (
  id BIGINT NOT NULL AUTO_INCREMENT,
  emp_no VARCHAR(30) NOT NULL,
  school_name VARCHAR(100) NOT NULL,
  major VARCHAR(100) NULL,
  degree VARCHAR(30) NULL,
  start_date DATE NULL,
  end_date DATE NULL,
  graduation_status VARCHAR(20) NULL,
  sort_order INT NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_employee_educations_emp_no (emp_no),
  CONSTRAINT fk_employee_educations_employee
    FOREIGN KEY (emp_no) REFERENCES employees(emp_no) ON DELETE CASCADE
);

-- 구성원 자격증
CREATE TABLE IF NOT EXISTS employee_certificates (
  id BIGINT NOT NULL AUTO_INCREMENT,
  emp_no VARCHAR(30) NOT NULL,
  cert_name VARCHAR(100) NOT NULL,
  issuer VARCHAR(100) NULL,
  acquired_date DATE NULL,
  expiry_date DATE NULL,
  sort_order INT NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_employee_certificates_emp_no (emp_no),
  CONSTRAINT fk_employee_certificates_employee
    FOREIGN KEY (emp_no) REFERENCES employees(emp_no) ON DELETE CASCADE
);

-- 구성원 첨부 문서
CREATE TABLE IF NOT EXISTS employee_documents (
  id BIGINT NOT NULL AUTO_INCREMENT,
  emp_no VARCHAR(30) NOT NULL,
  original_name VARCHAR(255) NOT NULL,
  stored_name VARCHAR(255) NOT NULL,
  file_size BIGINT NOT NULL DEFAULT 0,
  content_type VARCHAR(100) NULL,
  category VARCHAR(50) NULL,
  uploaded_by VARCHAR(50) NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_employee_documents_emp_no (emp_no),
  CONSTRAINT fk_employee_documents_employee
    FOREIGN KEY (emp_no) REFERENCES employees(emp_no) ON DELETE CASCADE
);

-- 구성원 조직 배치
CREATE TABLE IF NOT EXISTS employee_org_assignments (
  id BIGINT NOT NULL AUTO_INCREMENT,
  emp_no VARCHAR(30) NOT NULL,
  org_unit_id BIGINT NULL,
  title_name VARCHAR(100) NULL,
  org_leader BOOLEAN NOT NULL DEFAULT FALSE,
  sort_order INT NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_employee_org_assignments_emp_no (emp_no),
  KEY idx_employee_org_assignments_org_unit_id (org_unit_id),
  CONSTRAINT fk_employee_org_assignments_employee
    FOREIGN KEY (emp_no) REFERENCES employees(emp_no) ON DELETE CASCADE,
  CONSTRAINT fk_employee_org_assignments_org
    FOREIGN KEY (org_unit_id) REFERENCES org_units(id) ON DELETE SET NULL
);

-- 거래처
CREATE TABLE IF NOT EXISTS clients (
  id BIGINT NOT NULL AUTO_INCREMENT,
  name VARCHAR(100) NOT NULL,
  business_reg_no VARCHAR(30) NULL,
  corporate_reg_no VARCHAR(20) NULL,
  ceo_name VARCHAR(50) NULL,
  industry VARCHAR(100) NULL,
  phone VARCHAR(30) NULL,
  email VARCHAR(100) NULL,
  zip_code VARCHAR(10) NULL,
  address VARCHAR(255) NULL,
  address_detail VARCHAR(255) NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
  memo VARCHAR(1000) NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_clients_status (status)
);

-- 거래처 담당자
CREATE TABLE IF NOT EXISTS client_contacts (
  id BIGINT NOT NULL AUTO_INCREMENT,
  client_id BIGINT NOT NULL,
  name VARCHAR(50) NOT NULL,
  department VARCHAR(100) NULL,
  position VARCHAR(100) NULL,
  phone VARCHAR(30) NULL,
  email VARCHAR(100) NULL,
  is_primary BOOLEAN NOT NULL DEFAULT FALSE,
  sort_order INT NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_client_contacts_client_id (client_id),
  CONSTRAINT fk_client_contacts_client
    FOREIGN KEY (client_id) REFERENCES clients(id) ON DELETE CASCADE
);

-- 프로젝트
CREATE TABLE IF NOT EXISTS projects (
  id BIGINT NOT NULL AUTO_INCREMENT,
  name VARCHAR(200) NOT NULL,
  client_id BIGINT NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'ESTIMATE',
  start_date DATE NULL,
  end_date DATE NULL,
  contract_amount BIGINT NULL,
  description VARCHAR(2000) NULL,
  memo VARCHAR(1000) NULL,
  -- 기본정보 추가 필드
  project_code VARCHAR(50) NULL,
  approved_date DATE NULL,
  pm_emp_no VARCHAR(30) NULL,
  executive_emp_no VARCHAR(30) NULL,
  business_place VARCHAR(100) NULL,
  category_major VARCHAR(100) NULL,
  category_minor VARCHAR(100) NULL,
  division VARCHAR(100) NULL,
  department VARCHAR(100) NULL,
  org_unit_id BIGINT NULL,
  participation_type VARCHAR(50) NULL,
  supply_type VARCHAR(50) NULL,
  sales_emp_no VARCHAR(30) NULL,
  -- 계약 일반 필드
  customer_agency VARCHAR(200) NULL,
  customer_ordering VARCHAR(200) NULL,
  contract_name VARCHAR(300) NULL,
  contract_date DATE NULL,
  contract_start DATE NULL,
  contract_end DATE NULL,
  as_start DATE NULL,
  as_end DATE NULL,
  our_share_rate INT NULL DEFAULT 100,
  our_amount BIGINT NULL,
  our_amount_vat_ex BIGINT NULL,
  contract_amount_vat_ex BIGINT NULL,
  payment_condition VARCHAR(200) NULL,
  defect_period INT NULL DEFAULT 0,
  penalty_rate DECIMAL(5,2) NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_projects_client_id (client_id),
  KEY idx_projects_status (status),
  CONSTRAINT fk_projects_client
    FOREIGN KEY (client_id) REFERENCES clients(id) ON DELETE SET NULL
);

-- 프로젝트 담당자
CREATE TABLE IF NOT EXISTS project_members (
  id BIGINT NOT NULL AUTO_INCREMENT,
  project_id BIGINT NOT NULL,
  emp_no VARCHAR(30) NOT NULL,
  role VARCHAR(100) NULL,
  is_leader BOOLEAN NOT NULL DEFAULT FALSE,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_project_members (project_id, emp_no),
  KEY idx_project_members_project_id (project_id),
  KEY idx_project_members_emp_no (emp_no),
  CONSTRAINT fk_project_members_project
    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
  CONSTRAINT fk_project_members_employee
    FOREIGN KEY (emp_no) REFERENCES employees(emp_no) ON DELETE CASCADE
);

-- 계약서
CREATE TABLE IF NOT EXISTS contracts (
  id BIGINT NOT NULL AUTO_INCREMENT,
  project_id BIGINT NOT NULL,
  title VARCHAR(200) NOT NULL,
  contract_type VARCHAR(30) NULL,
  contract_date DATE NULL,
  amount BIGINT NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
  original_name VARCHAR(255) NULL,
  stored_name VARCHAR(255) NULL,
  file_size BIGINT NULL,
  content_type VARCHAR(100) NULL,
  memo VARCHAR(1000) NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_contracts_project_id (project_id),
  CONSTRAINT fk_contracts_project
    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE
);
