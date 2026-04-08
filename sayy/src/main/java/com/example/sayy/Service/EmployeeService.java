package com.example.sayy.Service;

import com.example.sayy.Mapper.EmployeeMapper;
import com.example.sayy.Mapper.EmployeeOrgAssignmentMapper;
import com.example.sayy.Mapper.UserMapper;
import com.example.sayy.Entity.EmploymentStatus;
import com.example.sayy.Entity.EmploymentType;
import com.example.sayy.Entity.EmployeeEntity;
import com.example.sayy.Entity.EmployeeOrgAssignmentEntity;
import com.example.sayy.Entity.TaxScheme;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class EmployeeService {
    private final EmployeeMapper employeeMapper;
    private final EmployeeOrgAssignmentMapper employeeOrgAssignmentMapper;
    private final UserMapper userMapper;

    public EmployeeService(EmployeeMapper employeeMapper,
                           EmployeeOrgAssignmentMapper employeeOrgAssignmentMapper,
                           UserMapper userMapper) {
        this.employeeMapper = employeeMapper;
        this.employeeOrgAssignmentMapper = employeeOrgAssignmentMapper;
        this.userMapper = userMapper;
    }

    public List<EmployeeEntity> findAll() {
        return employeeMapper.selectAllWithUser();
    }

    public List<EmployeeEntity> findAllActive() {
        return employeeMapper.selectByFiltersWithUser(null, EmploymentStatus.ACTIVE.name(), null, null);
    }

    public List<EmployeeEntity> findByEmploymentType(EmploymentType employmentType) {
        return employeeMapper.selectByEmploymentTypeWithUser(employmentType.name());
    }

    public List<EmployeeEntity> findByFilters(EmploymentType employmentType,
                                              EmploymentStatus employmentStatus,
                                              String keyword,
                                              String sort) {
        return employeeMapper.selectByFiltersWithUser(
                employmentType == null ? null : employmentType.name(),
                employmentStatus == null ? null : employmentStatus.name(),
                normalizeKeyword(keyword),
                normalizeSort(sort)
        );
    }

    public EmployeeEntity findOneWithUserOrThrow(String empNo) {
        if (empNo == null || empNo.isBlank()) {
            throw new IllegalArgumentException("empNo is required");
        }
        EmployeeEntity e = employeeMapper.selectWithUserByEmpNo(empNo.trim());
        if (e == null) {
            throw new IllegalArgumentException("Employee not found: " + empNo);
        }
        return e;
    }

    public List<EmployeeOrgAssignmentEntity> listOrgAssignments(String empNo) {
        if (empNo == null || empNo.isBlank()) return List.of();
        return employeeOrgAssignmentMapper.selectByEmpNo(empNo.trim());
    }

    public long countAll() {
        return employeeMapper.countAll();
    }

    public long countByEmploymentType(EmploymentType employmentType) {
        return employeeMapper.countByEmploymentType(employmentType.name());
    }

    public long countByEmploymentStatus(EmploymentStatus employmentStatus) {
        return employeeMapper.countByEmploymentStatus(employmentStatus.name());
    }

    @Transactional
    public void create(String empNo,
                       String name,
                       EmploymentType employmentType,
                       EmploymentStatus employmentStatus,
                       Boolean fourInsured,
                       TaxScheme taxScheme,
                       LocalDate contractEndDate,
                       LocalDate hireDate,
                       LocalDate groupHireDate,
                       String positionName,
                       String identificationNumber,
                       Integer age,
                       Integer yearsOfService,
                       String email,
                       LocalDate birthDate,
                       LocalDate terminationDate,
                       String loginUsername) {
        if (empNo == null || empNo.isBlank()) {
            throw new IllegalArgumentException("empNo is required");
        }
        String normalizedEmpNo = empNo.trim();
        if (!normalizedEmpNo.matches("^[0-9]+$")) {
            throw new IllegalArgumentException("사번은 숫자만 입력 가능합니다.");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name is required");
        }
        if (employmentType == null) {
            throw new IllegalArgumentException("고용 형태는 필수입니다.");
        }
        if (employeeMapper.existsByEmpNo(normalizedEmpNo) == 1) {
            throw new IllegalStateException("Employee already exists: " + empNo);
        }

        EmployeeEntity employee = new EmployeeEntity();
        employee.setEmpNo(normalizedEmpNo);
        employee.setName(name.trim());
        if (loginUsername != null && !loginUsername.isBlank()) {
            employee.setLoginUsername(loginUsername.trim());
        } else {
            employee.setLoginUsername(null);
        }

        // 고용 형태에 따른 정책 기본값/검증
        employee.setEmploymentType(employmentType);
        employee.setEmploymentStatus(employmentStatus != null ? employmentStatus : EmploymentStatus.ACTIVE);
        employee.setHireDate(hireDate);
        employee.setGroupHireDate(groupHireDate);
        employee.setPositionName(normalizeText(positionName, 100));
        employee.setIdentificationNumber(normalizeText(identificationNumber, 20));
        employee.setAge(age);
        employee.setYearsOfService(yearsOfService);
        employee.setEmail(normalizeText(email, 100));
        employee.setBirthDate(birthDate);
        employee.setTerminationDate(terminationDate);
        switch (employmentType) {
            case REGULAR -> {
                employee.setFourInsured(fourInsured != null ? fourInsured : Boolean.TRUE);
                employee.setTaxScheme(TaxScheme.SALARY_WITHHOLDING);
                employee.setContractEndDate(null);
            }
            case CONTRACT -> {
                if (contractEndDate == null) {
                    throw new IllegalArgumentException("계약직은 계약 종료일이 필요합니다.");
                }
                employee.setFourInsured(fourInsured != null ? fourInsured : Boolean.TRUE);
                // 현재 MVP: 계약직은 근로소득(원천징수)만 허용
                if (taxScheme != null && taxScheme != TaxScheme.SALARY_WITHHOLDING) {
                    throw new IllegalArgumentException("계약직 세금 방식은 근로소득(원천징수)만 선택 가능합니다.");
                }
                employee.setTaxScheme(TaxScheme.SALARY_WITHHOLDING);
                employee.setContractEndDate(contractEndDate);
            }
            case FREELANCER -> {
                if (contractEndDate == null) {
                    throw new IllegalArgumentException("프리랜서는 계약 종료일이 필요합니다.");
                }
                employee.setFourInsured(Boolean.FALSE);
                // 현재 MVP: 프리랜서는 사업소득(3.3%) 고정
                if (taxScheme != null && taxScheme != TaxScheme.BUSINESS_INCOME_3_3) {
                    throw new IllegalArgumentException("프리랜서 세금 방식은 사업소득(3.3%)만 선택 가능합니다.");
                }
                employee.setTaxScheme(TaxScheme.BUSINESS_INCOME_3_3);
                employee.setContractEndDate(contractEndDate);
            }
        }

        employeeMapper.insert(employee);
    }

    // 기존 코드 호환(화면의 단건 등록 등)
    @Transactional
    public void create(String empNo,
                       String name,
                       EmploymentType employmentType,
                       Boolean fourInsured,
                       TaxScheme taxScheme,
                       LocalDate contractEndDate) {
        create(empNo, name, employmentType, EmploymentStatus.ACTIVE, fourInsured, taxScheme, contractEndDate,
                null, null, null, null, null, null, null, null, null, null);
    }

    @Transactional
    public void create(String empNo,
                       String name,
                       EmploymentType employmentType,
                       Boolean fourInsured,
                       TaxScheme taxScheme,
                       LocalDate contractEndDate,
                       String loginUsername) {
        create(empNo, name, employmentType, EmploymentStatus.ACTIVE, fourInsured, taxScheme, contractEndDate,
                null, null, null, null, null, null, null, null, null, loginUsername);
    }

    @Transactional
    public void createImported(String empNo,
                               String name,
                               EmploymentType employmentType,
                               EmploymentStatus employmentStatus,
                               Boolean fourInsured,
                               TaxScheme taxScheme,
                               LocalDate contractEndDate,
                               LocalDate hireDate,
                               LocalDate groupHireDate,
                               String positionName,
                               String identificationNumber,
                               Integer age,
                               Integer yearsOfService,
                               String email,
                               LocalDate birthDate,
                               LocalDate terminationDate,
                               String loginUsername) {
        create(empNo, name, employmentType, employmentStatus, fourInsured, taxScheme, contractEndDate,
                hireDate, groupHireDate, positionName, identificationNumber, age, yearsOfService,
                email, birthDate, terminationDate, loginUsername);
    }

    @Transactional
    public void deleteEmployee(String empNo, boolean deleteAccountToo) {
        if (empNo == null || empNo.isBlank()) {
            throw new IllegalArgumentException("empNo is required");
        }
        String normalizedEmpNo = empNo.trim();

        EmployeeEntity employee = employeeMapper.selectWithUserByEmpNo(normalizedEmpNo);
        if (employee == null) {
            throw new IllegalArgumentException("Employee not found: " + normalizedEmpNo);
        }

        if (employee.getUser() != null && employee.getUser().getUsername() != null) {
            if (!deleteAccountToo) {
                throw new IllegalStateException("계정이 발급된 인사정보입니다. (계정도 함께 삭제를 선택하세요)");
            }
            if (employee.getEmploymentType() != EmploymentType.REGULAR) {
                throw new IllegalStateException("계정 삭제(계정 포함 삭제)는 정규직만 가능합니다.");
            }
            // FK(users.employee_no -> employees.emp_no) 때문에 계정을 먼저 삭제해야 함
            userMapper.deleteByUsername(employee.getUser().getUsername());
        }

        employeeMapper.deleteByEmpNo(normalizedEmpNo);
    }

    @Transactional
    public void updateHrProfile(String empNo,
                                LocalDate hireDate,
                                LocalDate groupHireDate,
                                String positionName,
                                String jobNames,
                                String hrMemo,
                                List<Long> orgUnitIds,
                                List<String> titleNames,
                                List<Boolean> orgLeaders) {
        if (empNo == null || empNo.isBlank()) {
            throw new IllegalArgumentException("empNo is required");
        }
        String normalizedEmpNo = empNo.trim();
        EmployeeEntity exists = employeeMapper.selectByEmpNo(normalizedEmpNo);
        if (exists == null) {
            throw new IllegalArgumentException("Employee not found: " + normalizedEmpNo);
        }

        EmployeeEntity e = new EmployeeEntity();
        e.setEmpNo(normalizedEmpNo);
        e.setHireDate(hireDate);
        e.setGroupHireDate(groupHireDate);
        e.setPositionName(normalizeText(positionName, 100));
        e.setJobNames(normalizeJobNames(jobNames, 255));
        e.setHrMemo(normalizeText(hrMemo, 1000));

        employeeMapper.updateHrInfo(e);

        // 겸직/조직·직책: 간단히 전체 교체 방식
        employeeOrgAssignmentMapper.deleteByEmpNo(normalizedEmpNo);
        List<EmployeeOrgAssignmentEntity> rows = normalizeAssignments(normalizedEmpNo, orgUnitIds, titleNames, orgLeaders);
        for (EmployeeOrgAssignmentEntity row : rows) {
            employeeOrgAssignmentMapper.insert(row);
        }
    }

    @Transactional
    public void updateContractInfo(String empNo,
                                   LocalDate laborContractStart,
                                   LocalDate laborContractEnd,
                                   LocalDate probationStart,
                                   LocalDate probationEnd,
                                   Integer probationPayRate,
                                   String incomeType,
                                   LocalDate wageContractStart,
                                   LocalDate wageContractEnd,
                                   Long monthlySalary) {
        if (empNo == null || empNo.isBlank()) throw new IllegalArgumentException("empNo is required");
        String normalizedEmpNo = empNo.trim();
        if (employeeMapper.selectByEmpNo(normalizedEmpNo) == null) {
            throw new IllegalArgumentException("Employee not found: " + normalizedEmpNo);
        }
        EmployeeEntity e = new EmployeeEntity();
        e.setEmpNo(normalizedEmpNo);
        e.setLaborContractStart(laborContractStart);
        e.setLaborContractEnd(laborContractEnd);
        e.setProbationStart(probationStart);
        e.setProbationEnd(probationEnd);
        e.setProbationPayRate(probationPayRate);
        e.setIncomeType(normalizeText(incomeType, 30));
        e.setWageContractStart(wageContractStart);
        e.setWageContractEnd(wageContractEnd);
        e.setMonthlySalary(monthlySalary);
        employeeMapper.updateContractInfo(e);
    }

    @Transactional
    public void updatePersonalInfo(String empNo,
                                   String nameEn,
                                   String gender,
                                   String nationality,
                                   String phone,
                                   String bankName,
                                   String bankAccount,
                                   String bankHolder) {
        if (empNo == null || empNo.isBlank()) {
            throw new IllegalArgumentException("empNo is required");
        }
        String normalizedEmpNo = empNo.trim();
        if (employeeMapper.selectByEmpNo(normalizedEmpNo) == null) {
            throw new IllegalArgumentException("Employee not found: " + normalizedEmpNo);
        }
        EmployeeEntity e = new EmployeeEntity();
        e.setEmpNo(normalizedEmpNo);
        e.setNameEn(normalizeText(nameEn, 100));
        e.setGender(normalizeText(gender, 10));
        e.setNationality(normalizeText(nationality, 50));
        e.setPhone(normalizeText(phone, 30));
        e.setBankName(normalizeText(bankName, 50));
        e.setBankAccount(normalizeText(bankAccount, 50));
        e.setBankHolder(normalizeText(bankHolder, 50));
        employeeMapper.updatePersonalInfo(e);
    }

    private static String normalizeText(String v, int maxLen) {
        if (v == null) return null;
        String s = v.trim();
        if (s.isEmpty()) return null;
        if (s.length() > maxLen) {
            return s.substring(0, maxLen);
        }
        return s;
    }

    private static String normalizeJobNames(String raw, int maxLen) {
        if (raw == null) return null;
        String s = raw.trim();
        if (s.isEmpty()) return null;
        // "a, b ,c" -> "a, b, c"
        String[] parts = s.split(",");
        List<String> cleaned = new ArrayList<>();
        for (String p : parts) {
            String c = p.trim();
            if (!c.isEmpty()) cleaned.add(c);
        }
        if (cleaned.isEmpty()) return null;
        String out = String.join(", ", cleaned);
        if (out.length() > maxLen) out = out.substring(0, maxLen);
        return out;
    }

    private static String normalizeKeyword(String keyword) {
        if (keyword == null) return null;
        String s = keyword.trim();
        return s.isEmpty() ? null : s;
    }

    private static String normalizeSort(String sort) {
        if (sort == null || sort.isBlank()) return "createdAtDesc";
        return switch (sort.trim()) {
            case "createdAtDesc", "hireDateDesc", "hireDateAsc", "nameAsc", "empNoAsc" -> sort.trim();
            default -> "createdAtDesc";
        };
    }

    private static List<EmployeeOrgAssignmentEntity> normalizeAssignments(String empNo,
                                                                         List<Long> orgUnitIds,
                                                                         List<String> titleNames,
                                                                         List<Boolean> orgLeaders) {
        int n = maxSize(orgUnitIds, titleNames, orgLeaders);
        if (n <= 0) return List.of();

        List<EmployeeOrgAssignmentEntity> out = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            Long orgUnitId = (orgUnitIds == null || i >= orgUnitIds.size()) ? null : orgUnitIds.get(i);
            String titleName = (titleNames == null || i >= titleNames.size()) ? null : titleNames.get(i);
            Boolean leader = (orgLeaders == null || i >= orgLeaders.size()) ? Boolean.FALSE : orgLeaders.get(i);

            boolean empty = orgUnitId == null
                    && (titleName == null || titleName.isBlank())
                    && (leader == null || leader == Boolean.FALSE);
            if (empty) continue;

            EmployeeOrgAssignmentEntity row = new EmployeeOrgAssignmentEntity();
            row.setEmpNo(empNo);
            row.setOrgUnitId(orgUnitId);
            row.setTitleName(normalizeText(titleName, 100));
            row.setOrgLeader(leader != null && leader);
            row.setSortOrder(out.size());
            out.add(row);
        }
        return out;
    }

    @SafeVarargs
    private static int maxSize(List<?>... lists) {
        int max = 0;
        if (lists == null) return 0;
        for (List<?> l : lists) {
            if (l == null) continue;
            max = Math.max(max, l.size());
        }
        return max;
    }
}

