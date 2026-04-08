package com.example.sayy.Service;

import com.example.sayy.DTO.EmployeeImportRowDTO;
import com.example.sayy.Entity.EmploymentStatus;
import com.example.sayy.Entity.EmploymentType;
import com.example.sayy.Entity.TaxScheme;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

@Service
public class EmployeeBulkImportService {

    private final EmployeeService employeeService;

    public EmployeeBulkImportService(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @Transactional
    public int importEmployeesOnlyOrThrow(List<EmployeeImportRowDTO> rows) {
        if (rows == null || rows.isEmpty()) {
            throw new IllegalArgumentException("가져올 데이터가 없습니다.");
        }
        long invalid = rows.stream().filter(r -> !r.isValid()).count();
        if (invalid > 0) {
            throw new IllegalStateException("유효하지 않은 행이 있어 반영할 수 없습니다. (오류 행: " + invalid + ")");
        }

        for (EmployeeImportRowDTO r : rows) {
            String empNo = r.getEmpNo().trim();
            String name = r.getName().trim();
            EmploymentType et = EmploymentType.valueOf(r.getEmploymentType().trim().toUpperCase(Locale.ROOT));
            EmploymentStatus status = EmploymentStatus.valueOf(r.getEmploymentStatus().trim().toUpperCase(Locale.ROOT));

            Boolean fourInsured = null;
            if (r.getFourInsured() != null && !r.getFourInsured().isBlank()) {
                fourInsured = Boolean.parseBoolean(r.getFourInsured().trim());
            }

            TaxScheme taxScheme = null;
            if (r.getTaxScheme() != null && !r.getTaxScheme().isBlank()) {
                taxScheme = TaxScheme.valueOf(r.getTaxScheme().trim().toUpperCase(Locale.ROOT));
            }

            LocalDate contractEndDate = null;
            if (r.getContractEndDate() != null && !r.getContractEndDate().isBlank()) {
                contractEndDate = LocalDate.parse(r.getContractEndDate().trim());
            }

            LocalDate hireDate = null;
            if (r.getHireDate() != null && !r.getHireDate().isBlank()) {
                hireDate = LocalDate.parse(r.getHireDate().trim());
            }

            LocalDate groupHireDate = null;
            if (r.getGroupHireDate() != null && !r.getGroupHireDate().isBlank()) {
                groupHireDate = LocalDate.parse(r.getGroupHireDate().trim());
            }

            LocalDate birthDate = null;
            if (r.getBirthDate() != null && !r.getBirthDate().isBlank()) {
                birthDate = LocalDate.parse(r.getBirthDate().trim());
            }

            LocalDate terminationDate = null;
            if (r.getTerminationDate() != null && !r.getTerminationDate().isBlank()) {
                terminationDate = LocalDate.parse(r.getTerminationDate().trim());
            }

            Integer age = null;
            if (r.getAge() != null && !r.getAge().isBlank()) {
                age = Integer.parseInt(r.getAge().trim());
            }

            Integer yearsOfService = null;
            if (r.getYearsOfService() != null && !r.getYearsOfService().isBlank()) {
                yearsOfService = Integer.parseInt(r.getYearsOfService().trim());
            }

            // 직원 생성만 수행(계정/초기비번 발급은 구성원 목록에서 진행)
            String loginUsername = r.getUsername() == null ? null : r.getUsername().trim();
            employeeService.createImported(
                    empNo,
                    name,
                    et,
                    status,
                    fourInsured,
                    taxScheme,
                    contractEndDate,
                    hireDate,
                    groupHireDate,
                    r.getPositionName(),
                    r.getIdentificationNumber(),
                    age,
                    yearsOfService,
                    r.getEmail(),
                    birthDate,
                    terminationDate,
                    loginUsername
            );
            // 주민등록번호에서 자동 추출된 성별 저장
            if (r.getGender() != null && !r.getGender().isBlank()) {
                employeeService.updatePersonalInfo(empNo, null, r.getGender(), null, null, null, null, null);
            }
        }

        return rows.size();
    }
}

