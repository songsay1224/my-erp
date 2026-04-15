-- ????? ????? ??? (CREATE IF NOT EXISTS + ALTER IF NOT EXISTS ??)
-- ???? ????? ????????? ????
-- ===== ????=====
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
  tech_grade VARCHAR(50) NULL,
  job_names VARCHAR(255) NULL,
  identification_number VARCHAR(20) NULL,
  age INT NULL,
  years_of_service INT NULL,
  email VARCHAR(100) NULL,
  birth_date DATE NULL,
  termination_date DATE NULL,
  hr_memo VARCHAR(1000) NULL,
  gender VARCHAR(10) NULL,
  nationality VARCHAR(50) NULL,
  phone VARCHAR(30) NULL,
  bank_name VARCHAR(50) NULL,
  bank_account VARCHAR(50) NULL,
  bank_holder VARCHAR(50) NULL,
  labor_contract_start DATE NULL,
  labor_contract_end DATE NULL,
  probation_start DATE NULL,
  probation_end DATE NULL,
  probation_pay_rate INT NULL,
  income_type VARCHAR(30) NULL,
  wage_contract_start DATE NULL,
  wage_contract_end DATE NULL,
  monthly_salary BIGINT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (emp_no),
  UNIQUE KEY uk_employees_login_username (login_username)
);

ALTER TABLE employees ADD COLUMN login_username VARCHAR(50) NULL;
ALTER TABLE employees ADD COLUMN name_en VARCHAR(100) NULL;
ALTER TABLE employees ADD COLUMN gender VARCHAR(10) NULL;
ALTER TABLE employees ADD COLUMN nationality VARCHAR(50) NULL;
ALTER TABLE employees ADD COLUMN phone VARCHAR(30) NULL;
ALTER TABLE employees ADD COLUMN bank_name VARCHAR(50) NULL;
ALTER TABLE employees ADD COLUMN bank_account VARCHAR(50) NULL;
ALTER TABLE employees ADD COLUMN bank_holder VARCHAR(50) NULL;
ALTER TABLE employees ADD COLUMN labor_contract_start DATE NULL;
ALTER TABLE employees ADD COLUMN labor_contract_end DATE NULL;
ALTER TABLE employees ADD COLUMN probation_start DATE NULL;
ALTER TABLE employees ADD COLUMN probation_end DATE NULL;
ALTER TABLE employees ADD COLUMN probation_pay_rate INT NULL;
ALTER TABLE employees ADD COLUMN income_type VARCHAR(30) NULL;
ALTER TABLE employees ADD COLUMN wage_contract_start DATE NULL;
ALTER TABLE employees ADD COLUMN wage_contract_end DATE NULL;
ALTER TABLE employees ADD COLUMN monthly_salary BIGINT NULL;
ALTER TABLE employees ADD COLUMN tech_grade VARCHAR(50) NULL;

-- ===== ?????=====
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

-- ===== ????=====
CREATE TABLE IF NOT EXISTS holidays (
  loc_date DATE NOT NULL,
  name VARCHAR(50) NOT NULL,
  is_holiday BOOLEAN NOT NULL DEFAULT TRUE,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (loc_date)
);

CREATE TABLE IF NOT EXISTS days_off_custom (
  id BIGINT NOT NULL AUTO_INCREMENT,
  loc_date DATE NOT NULL,
  name VARCHAR(50) NOT NULL,
  repeat_annually BOOLEAN NOT NULL DEFAULT FALSE,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_days_off_custom_loc_date (loc_date)
);

-- ===== ??? ??? =====
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

ALTER TABLE company_info ADD COLUMN seal_path VARCHAR(255) NULL;
ALTER TABLE company_info ADD COLUMN zip_code VARCHAR(10) NULL;
ALTER TABLE company_info ADD COLUMN address_detail VARCHAR(255) NULL;

-- ===== ????=====
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

-- ===== ??/???/?????=====
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

-- ===== ?????? =====
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

-- ===== ????=====
CREATE TABLE IF NOT EXISTS clients (
  id BIGINT NOT NULL AUTO_INCREMENT,
  client_type VARCHAR(20) NULL,
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
  region VARCHAR(50) NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
  memo VARCHAR(1000) NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_clients_status (status)
);

ALTER TABLE clients ADD COLUMN corporate_reg_no VARCHAR(20) NULL;
ALTER TABLE clients ADD COLUMN client_type VARCHAR(20) NULL;
ALTER TABLE clients ADD COLUMN region VARCHAR(50) NULL;
ALTER TABLE clients ADD COLUMN opened_date DATE NULL;
ALTER TABLE clients ADD COLUMN business_type VARCHAR(100) NULL;
ALTER TABLE clients ADD COLUMN business_item VARCHAR(100) NULL;
ALTER TABLE clients ADD COLUMN homepage VARCHAR(255) NULL;
ALTER TABLE clients ADD COLUMN bank_name VARCHAR(50) NULL;
ALTER TABLE clients ADD COLUMN bank_account VARCHAR(50) NULL;
ALTER TABLE clients ADD COLUMN bank_holder VARCHAR(50) NULL;
ALTER TABLE clients ADD COLUMN bank_stamp VARCHAR(255) NULL;
ALTER TABLE clients ADD COLUMN attachment_path VARCHAR(255) NULL;

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

-- ===== ?????? =====
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

ALTER TABLE projects ADD COLUMN project_code VARCHAR(50) NULL;
ALTER TABLE projects ADD COLUMN approved_date DATE NULL;
ALTER TABLE projects ADD COLUMN pm_emp_no VARCHAR(30) NULL;
ALTER TABLE projects ADD COLUMN executive_emp_no VARCHAR(30) NULL;
ALTER TABLE projects ADD COLUMN business_place VARCHAR(100) NULL;
ALTER TABLE projects ADD COLUMN category_major VARCHAR(100) NULL;
ALTER TABLE projects ADD COLUMN category_minor VARCHAR(100) NULL;
ALTER TABLE projects ADD COLUMN division VARCHAR(100) NULL;
ALTER TABLE projects ADD COLUMN department VARCHAR(100) NULL;
ALTER TABLE projects ADD COLUMN org_unit_id BIGINT NULL;
ALTER TABLE projects ADD COLUMN participation_type VARCHAR(50) NULL;
ALTER TABLE projects ADD COLUMN supply_type VARCHAR(50) NULL;
ALTER TABLE projects ADD COLUMN sales_emp_no VARCHAR(30) NULL;
ALTER TABLE projects ADD COLUMN customer_agency VARCHAR(200) NULL;
ALTER TABLE projects ADD COLUMN customer_ordering VARCHAR(200) NULL;
ALTER TABLE projects ADD COLUMN contract_name VARCHAR(300) NULL;
ALTER TABLE projects ADD COLUMN contract_date DATE NULL;
ALTER TABLE projects ADD COLUMN contract_start DATE NULL;
ALTER TABLE projects ADD COLUMN contract_end DATE NULL;
ALTER TABLE projects ADD COLUMN as_start DATE NULL;
ALTER TABLE projects ADD COLUMN as_end DATE NULL;
ALTER TABLE projects ADD COLUMN our_share_rate INT NULL DEFAULT 100;
ALTER TABLE projects ADD COLUMN our_amount BIGINT NULL;
ALTER TABLE projects ADD COLUMN our_amount_vat_ex BIGINT NULL;
ALTER TABLE projects ADD COLUMN contract_amount_vat_ex BIGINT NULL;
ALTER TABLE projects ADD COLUMN payment_condition VARCHAR(200) NULL;
ALTER TABLE projects ADD COLUMN defect_period INT NULL DEFAULT 0;
ALTER TABLE projects ADD COLUMN penalty_rate DECIMAL(5,2) NULL DEFAULT 0;
ALTER TABLE projects ADD COLUMN management_year INT NULL COMMENT '????';
ALTER TABLE projects ADD COLUMN category_l3 VARCHAR(100) NULL COMMENT 'L3 ??';

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

-- ?? ??
CREATE TABLE IF NOT EXISTS contract_sales (
  id BIGINT NOT NULL AUTO_INCREMENT,
  project_id BIGINT NOT NULL,
  contract_name VARCHAR(200) NULL COMMENT '???',
  customer_agency VARCHAR(200) NULL COMMENT '????',
  contract_amount BIGINT NULL COMMENT '????',
  accumulated_progress BIGINT NULL COMMENT '????',
  memo VARCHAR(500) NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_contract_sales_project_id (project_id),
  CONSTRAINT fk_contract_sales_project
    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE
);

-- ?? ??
CREATE TABLE IF NOT EXISTS contract_performance (
  id BIGINT NOT NULL AUTO_INCREMENT,
  project_id BIGINT NOT NULL,
  customer_agency VARCHAR(200) NULL COMMENT '????',
  contract_name VARCHAR(200) NULL COMMENT '????',
  business_amount BIGINT NULL COMMENT '????',
  supply_type VARCHAR(50) NULL COMMENT '????',
  payment_rate DECIMAL(5,2) NULL COMMENT '???',
  contract_amount BIGINT NULL COMMENT '????',
  completion_date DATE NULL COMMENT '???',
  business_period VARCHAR(100) NULL COMMENT '????',
  category_l1 VARCHAR(100) NULL COMMENT 'L1',
  category_l2 VARCHAR(100) NULL COMMENT 'L2',
  category_l3 VARCHAR(100) NULL COMMENT 'L3',
  memo VARCHAR(500) NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_contract_performance_project_id (project_id),
  CONSTRAINT fk_contract_performance_project
    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE
);

-- ?? ??
CREATE TABLE IF NOT EXISTS contract_purchases (
  id BIGINT NOT NULL AUTO_INCREMENT,
  project_id BIGINT NOT NULL,
  purchase_type VARCHAR(50) NULL COMMENT '??',
  project_name VARCHAR(200) NULL COMMENT '?????',
  subcontract_name VARCHAR(200) NULL COMMENT '????',
  vendor_name VARCHAR(200) NULL COMMENT '????',
  contract_date DATE NULL COMMENT '?????',
  contract_start DATE NULL COMMENT '?????',
  contract_end DATE NULL COMMENT '?????',
  supply_amount BIGINT NULL COMMENT '????',
  vat_amount BIGINT NULL COMMENT '????',
  total_amount BIGINT NULL COMMENT '????',
  memo VARCHAR(500) NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_contract_purchases_project_id (project_id),
  CONSTRAINT fk_contract_purchases_project
    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE
);
