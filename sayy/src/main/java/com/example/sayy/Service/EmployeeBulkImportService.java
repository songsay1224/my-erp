package com.example.sayy.Service;

import com.example.sayy.DTO.EmployeeImportRowDTO;
import com.example.sayy.Entity.EmploymentStatus;
import com.example.sayy.Entity.EmploymentType;
import com.example.sayy.Entity.TaxScheme;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class EmployeeBulkImportService {

    private final EmployeeService employeeService;
    private final CareerService careerService;

    public EmployeeBulkImportService(EmployeeService employeeService,
                                     CareerService careerService) {
        this.employeeService = employeeService;
        this.careerService = careerService;
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
            if (fourInsured == null) fourInsured = true; // 기본값

            TaxScheme taxScheme = null;
            if (r.getTaxScheme() != null && !r.getTaxScheme().isBlank()) {
                taxScheme = TaxScheme.valueOf(r.getTaxScheme().trim().toUpperCase(Locale.ROOT));
            }
            if (taxScheme == null) taxScheme = TaxScheme.SALARY_WITHHOLDING; // 기본값

            LocalDate contractEndDate = parseDate(r.getContractEndDate());
            LocalDate hireDate = parseDate(r.getHireDate());
            LocalDate groupHireDate = parseDate(r.getGroupHireDate());
            if (groupHireDate == null) groupHireDate = hireDate;
            LocalDate birthDate = parseDate(r.getBirthDate());
            LocalDate terminationDate = parseDate(r.getTerminationDate());

            Integer age = parseInt(r.getAge());
            Integer yearsOfService = parseInt(r.getYearsOfService());

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

            // 성별 저장
            if (r.getGender() != null && !r.getGender().isBlank()) {
                employeeService.updatePersonalInfo(empNo, null, r.getGender(), null, null, null, null, null);
            }

            // 기술등급 + 직군·직종 저장 (jobNames에 직군/직종 결합해서 저장)
            if (hasText(r.getTechGrade()) || hasText(r.getJobGroup()) || hasText(r.getJobType())) {
                try {
                    // 직군, 직종을 콤마로 합쳐 jobNames에 저장
                    List<String> jobParts = new ArrayList<>();
                    if (hasText(r.getJobGroup())) jobParts.add(r.getJobGroup());
                    if (hasText(r.getJobType())) jobParts.add(r.getJobType());
                    String jobNames = jobParts.isEmpty() ? null : String.join(", ", jobParts);

                    employeeService.updateHrProfile(
                            empNo,
                            hireDate,
                            groupHireDate,
                            r.getPositionName(),
                            r.getTechGrade(),
                            jobNames,
                            null,
                            List.of(), List.of(), List.of()
                    );
                } catch (Exception ignored) {}
            }

            // 학력 저장
            if (hasText(r.getSchoolName())) {
                LocalDate gradDate = null;
                if (hasText(r.getGraduationYear())) {
                    try {
                        int year = (int) Double.parseDouble(r.getGraduationYear().trim());
                        gradDate = LocalDate.of(year, 2, 28);
                    } catch (Exception ignored) {}
                }
                careerService.addEducation(
                        empNo,
                        r.getSchoolName().trim(),
                        r.getMajor(),
                        r.getDegree(),
                        null,
                        gradDate,
                        "졸업"
                );
            }

            // 자격증 저장
            for (String certName : parseCertificates(r.getCertificates())) {
                careerService.addCertificate(empNo, certName, null, null, null);
            }
        }

        return rows.size();
    }

    private LocalDate parseDate(String s) {
        if (s == null || s.isBlank()) return null;
        try { return LocalDate.parse(s.trim()); } catch (Exception e) { return null; }
    }

    private Integer parseInt(String s) {
        if (s == null || s.isBlank()) return null;
        try { return Integer.parseInt(s.trim()); } catch (Exception e) { return null; }
    }

    private boolean hasText(String s) { return s != null && !s.isBlank(); }

    /** "1. 정보처리기사\n2. OCJP" 형태 파싱 */
    private List<String> parseCertificates(String raw) {
        if (raw == null || raw.isBlank()) return List.of();
        List<String> result = new ArrayList<>();
        String[] parts = raw.split("(?=\\d+\\.\\s)|[\\n\\r]+");
        Pattern numDot = Pattern.compile("^\\d+\\.\\s*(.+)$");
        for (String part : parts) {
            String p = part.trim();
            if (p.isBlank()) continue;
            Matcher m = numDot.matcher(p);
            if (m.matches()) {
                String certName = m.group(1).trim();
                if (!certName.isBlank()) result.add(certName);
            } else {
                result.add(p);
            }
        }
        return result;
    }
}
