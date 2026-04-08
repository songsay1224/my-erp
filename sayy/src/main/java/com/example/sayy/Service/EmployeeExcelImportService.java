package com.example.sayy.Service;

import com.example.sayy.DTO.EmployeeImportRowDTO;
import com.example.sayy.Mapper.EmployeeMapper;
import com.example.sayy.Mapper.UserMapper;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.ss.util.NumberToTextConverter;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class EmployeeExcelImportService {
    private static final Logger log = LoggerFactory.getLogger(EmployeeExcelImportService.class);
    public static final List<String> TEMPLATE_HEADERS = List.of(
            "emp_no",
            "name",
            "employment_type",
            "employment_status",
            "four_insured",
            "tax_scheme",
            "hire_date",
            "group_hire_date",
            "position_name",
            "identification_number",
            "email",
            "birth_date",
            "termination_date",
            "contract_end_date",
            "username"
    );

    private static final List<String> REQUIRED_XLSX_HEADERS = List.of("emp_no", "name", "employment_type");

    private static final Map<String, String> HEADER_ALIASES = Map.ofEntries(
            Map.entry("employee number", "emp_no"),
            Map.entry("사번", "emp_no"),
            Map.entry("사원번호", "emp_no"),
            Map.entry("직원번호", "emp_no"),
            Map.entry("empno", "emp_no"),
            Map.entry("emp_no", "emp_no"),

            Map.entry("employee name", "name"),
            Map.entry("이름", "name"),
            Map.entry("성명", "name"),
            Map.entry("name", "name"),

            Map.entry("status", "employment_status"),
            Map.entry("상태", "employment_status"),
            Map.entry("재직상태", "employment_status"),
            Map.entry("employmentstatus", "employment_status"),
            Map.entry("employment_status", "employment_status"),

            Map.entry("title", "position_name"),
            Map.entry("직위", "position_name"),
            Map.entry("직급", "position_name"),
            Map.entry("position", "position_name"),
            Map.entry("position_name", "position_name"),

            Map.entry("identification number", "identification_number"),
            Map.entry("identificationnumber", "identification_number"),
            Map.entry("identification_number", "identification_number"),
            Map.entry("주민등록번호", "identification_number"),
            Map.entry("주민번호", "identification_number"),

            Map.entry("email", "email"),
            Map.entry("이메일", "email"),
            Map.entry("메일", "email"),

            Map.entry("age", "age"),
            Map.entry("나이", "age"),

            Map.entry("years", "years_of_service"),
            Map.entry("yearsofservice", "years_of_service"),
            Map.entry("years_of_service", "years_of_service"),
            Map.entry("근속년수", "years_of_service"),
            Map.entry("근속연수", "years_of_service"),

            Map.entry("birthday", "birth_date"),
            Map.entry("birthdate", "birth_date"),
            Map.entry("birth_date", "birth_date"),
            Map.entry("생년월일", "birth_date"),

            Map.entry("start date", "hire_date"),
            Map.entry("startdate", "hire_date"),
            Map.entry("hiredate", "hire_date"),
            Map.entry("hire_date", "hire_date"),
            Map.entry("입사일", "hire_date"),

            Map.entry("group hire date", "group_hire_date"),
            Map.entry("grouphiredate", "group_hire_date"),
            Map.entry("group_hire_date", "group_hire_date"),
            Map.entry("그룹입사일", "group_hire_date"),

            Map.entry("end date", "termination_date"),
            Map.entry("enddate", "termination_date"),
            Map.entry("terminationdate", "termination_date"),
            Map.entry("termination_date", "termination_date"),
            Map.entry("퇴사일", "termination_date"),

            Map.entry("고용형태", "employment_type"),
            Map.entry("employmenttype", "employment_type"),
            Map.entry("employment_type", "employment_type"),

            Map.entry("4대보험", "four_insured"),
            Map.entry("4대 보험", "four_insured"),
            Map.entry("fourinsured", "four_insured"),
            Map.entry("four_insured", "four_insured"),

            Map.entry("세금", "tax_scheme"),
            Map.entry("세금방식", "tax_scheme"),
            Map.entry("taxscheme", "tax_scheme"),
            Map.entry("tax_scheme", "tax_scheme"),

            Map.entry("계약종료일", "contract_end_date"),
            Map.entry("contractenddate", "contract_end_date"),
            Map.entry("contract_end_date", "contract_end_date"),

            Map.entry("아이디", "username"),
            Map.entry("계정", "username"),
            Map.entry("username", "username"),
            Map.entry("loginid", "username"),
            Map.entry("로그인아이디", "username")
    );

    private final EmployeeMapper employeeMapper;
    private final UserMapper userMapper;

    public EmployeeExcelImportService(EmployeeMapper employeeMapper, UserMapper userMapper) {
        this.employeeMapper = employeeMapper;
        this.userMapper = userMapper;
    }

    public byte[] generateTemplateXlsx() {
        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet("employees");
            Sheet listSheet = wb.createSheet("_lists");

            Row header = sheet.createRow(0);
            CellStyle headerStyle = wb.createCellStyle();
            Font font = wb.createFont();
            font.setBold(true);
            headerStyle.setFont(font);

            for (int i = 0; i < TEMPLATE_HEADERS.size(); i++) {
                Cell c = header.createCell(i);
                c.setCellValue(TEMPLATE_HEADERS.get(i));
                c.setCellStyle(headerStyle);
                sheet.setColumnWidth(i, 18 * 256);
            }

            sheet.createFreezePane(0, 1);

            CellStyle dateStyle = wb.createCellStyle();
            short df = wb.createDataFormat().getFormat("yyyy-mm-dd");
            dateStyle.setDataFormat(df);

            int listCol = 0;
            String employmentTypeList = createNamedList(wb, listSheet, listCol++,
                    List.of("정규직", "계약직", "프리랜서"), "EmploymentTypeList");
            String employmentStatusList = createNamedList(wb, listSheet, listCol++,
                    List.of("재직", "휴직", "퇴사"), "EmploymentStatusList");
            String fourInsuredList = createNamedList(wb, listSheet, listCol++,
                    List.of("가입", "미가입"), "FourInsuredList");
            String taxSchemeList = createNamedList(wb, listSheet, listCol++,
                    List.of("근로소득(원천징수)", "사업소득(3.3%)"), "TaxSchemeList");

            Row ex = sheet.createRow(1);
            ex.createCell(0).setCellValue("20260001");
            ex.createCell(1).setCellValue("홍길동");
            ex.createCell(2).setCellValue("정규직");
            ex.createCell(3).setCellValue("재직");
            ex.createCell(4).setCellValue("가입");
            ex.createCell(5).setCellValue("근로소득(원천징수)");
            createDateCell(ex, 6, "2026-03-25", dateStyle);
            createDateCell(ex, 7, "2026-03-25", dateStyle);
            ex.createCell(8).setCellValue("대리");
            ex.createCell(9).setCellValue("960101-1******");
            ex.createCell(10).setCellValue("hong@example.com");
            createDateCell(ex, 11, "", dateStyle);
            createDateCell(ex, 12, "", dateStyle);
            createDateCell(ex, 13, "", dateStyle);
            ex.createCell(14).setCellValue("honggildong");

            int firstDataRow = 1;
            int lastDataRow = 1000;
            addDropdownNamed(sheet, firstDataRow, lastDataRow, TEMPLATE_HEADERS.indexOf("employment_type"), employmentTypeList);
            addDropdownNamed(sheet, firstDataRow, lastDataRow, TEMPLATE_HEADERS.indexOf("employment_status"), employmentStatusList);
            addDropdownNamed(sheet, firstDataRow, lastDataRow, TEMPLATE_HEADERS.indexOf("four_insured"), fourInsuredList);
            addDropdownNamed(sheet, firstDataRow, lastDataRow, TEMPLATE_HEADERS.indexOf("tax_scheme"), taxSchemeList);

            if (wb instanceof XSSFWorkbook xssf) {
                xssf.setSheetHidden(wb.getSheetIndex(listSheet), true);
            }

            wb.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("템플릿 생성 실패: " + e.getMessage(), e);
        }
    }

    public List<EmployeeImportRowDTO> parseAndValidate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("업로드 파일이 비어있습니다.");
        }
        String original = Optional.ofNullable(file.getOriginalFilename()).orElse("");
        String lower = original.toLowerCase(Locale.ROOT);
        if (lower.endsWith(".xlsx")) {
            return parseXlsx(file);
        }
        if (lower.endsWith(".csv")) {
            return parseCsv(file);
        }
        throw new IllegalArgumentException("xlsx 또는 csv 파일만 업로드할 수 있습니다.");
    }

    private List<EmployeeImportRowDTO> parseXlsx(MultipartFile file) {
        try (InputStream is = file.getInputStream(); Workbook wb = WorkbookFactory.create(is)) {
            Sheet sheet = wb.getNumberOfSheets() > 0 ? wb.getSheetAt(0) : null;
            if (sheet == null) throw new IllegalArgumentException("엑셀 시트가 없습니다.");

            Row headerRow = sheet.getRow(0);
            if (headerRow == null) throw new IllegalArgumentException("헤더 행(1행)이 없습니다.");

            Map<String, Integer> col = readHeaderMap(headerRow);
            for (String h : REQUIRED_XLSX_HEADERS) {
                if (!col.containsKey(h)) {
                    throw new IllegalArgumentException("필수 헤더가 없습니다: " + h);
                }
            }

            DataFormatter fmt = new DataFormatter();
            List<EmployeeImportRowDTO> rows = new ArrayList<>();
            Set<String> seenEmpNo = new HashSet<>();
            Set<String> seenUsername = new HashSet<>();

            int last = sheet.getLastRowNum();
            for (int r = 1; r <= last; r++) {
                Row row = sheet.getRow(r);
                if (row == null) continue;

                EmployeeImportRowDTO dto = new EmployeeImportRowDTO();
                dto.setRowNum(r + 1);
                dto.setEmpNo(getCellString(fmt, row, col.get("emp_no")));
                dto.setName(getCellString(fmt, row, col.get("name")));
                dto.setEmploymentType(getCellString(fmt, row, col.get("employment_type")));
                dto.setEmploymentStatus(getCellString(fmt, row, col.get("employment_status")));
                dto.setFourInsured(getCellString(fmt, row, col.get("four_insured")));
                dto.setTaxScheme(getCellString(fmt, row, col.get("tax_scheme")));
                dto.setHireDate(getCellDateString(fmt, row, col.get("hire_date")));
                dto.setGroupHireDate(getCellDateString(fmt, row, col.get("group_hire_date")));
                dto.setPositionName(getCellString(fmt, row, col.get("position_name")));
                dto.setIdentificationNumber(getCellString(fmt, row, col.get("identification_number")));
                dto.setEmail(getCellString(fmt, row, col.get("email")));
                dto.setBirthDate(getCellDateString(fmt, row, col.get("birth_date")));
                dto.setAge(getCellString(fmt, row, col.get("age")));
                dto.setYearsOfService(getCellString(fmt, row, col.get("years_of_service")));
                dto.setTerminationDate(getCellDateString(fmt, row, col.get("termination_date")));
                dto.setContractEndDate(getCellDateString(fmt, row, col.get("contract_end_date")));
                dto.setUsername(getCellString(fmt, row, col.get("username")));

                if (isEffectivelyEmpty(dto)) continue;
                validateAndNormalize(dto, seenEmpNo, seenUsername);
                rows.add(dto);
            }

            if (rows.isEmpty()) {
                throw new IllegalArgumentException("가져올 데이터가 없습니다(2행부터 데이터를 입력하세요).");
            }
            return rows;
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("엑셀 파싱 실패: " + e.getMessage(), e);
        }
    }

    private List<EmployeeImportRowDTO> parseCsv(MultipartFile file) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String headerLine = br.readLine();
            if (headerLine == null) {
                throw new IllegalArgumentException("CSV 헤더가 없습니다.");
            }

            Map<String, Integer> col = readHeaderMap(parseCsvLine(headerLine));
            if (!col.containsKey("emp_no") || !col.containsKey("name")) {
                throw new IllegalArgumentException("CSV 헤더에 Employee Number/Employee Name(또는 사번/이름)이 필요합니다.");
            }

            List<EmployeeImportRowDTO> rows = new ArrayList<>();
            Set<String> seenEmpNo = new HashSet<>();
            Set<String> seenUsername = new HashSet<>();

            String line;
            int rowNum = 1;
            while ((line = br.readLine()) != null) {
                rowNum++;
                List<String> cells = parseCsvLine(line);

                EmployeeImportRowDTO dto = new EmployeeImportRowDTO();
                dto.setRowNum(rowNum);
                dto.setEmpNo(getCsv(cells, col.get("emp_no")));
                dto.setName(getCsv(cells, col.get("name")));
                dto.setEmploymentType(getCsv(cells, col.get("employment_type")));
                dto.setEmploymentStatus(getCsv(cells, col.get("employment_status")));
                dto.setFourInsured(getCsv(cells, col.get("four_insured")));
                dto.setTaxScheme(getCsv(cells, col.get("tax_scheme")));
                dto.setHireDate(getCsv(cells, col.get("hire_date")));
                dto.setGroupHireDate(getCsv(cells, col.get("group_hire_date")));
                dto.setPositionName(getCsv(cells, col.get("position_name")));
                dto.setIdentificationNumber(getCsv(cells, col.get("identification_number")));
                dto.setEmail(getCsv(cells, col.get("email")));
                dto.setBirthDate(getCsv(cells, col.get("birth_date")));
                dto.setAge(getCsv(cells, col.get("age")));
                dto.setYearsOfService(getCsv(cells, col.get("years_of_service")));
                dto.setTerminationDate(getCsv(cells, col.get("termination_date")));
                dto.setContractEndDate(getCsv(cells, col.get("contract_end_date")));
                dto.setUsername(getCsv(cells, col.get("username")));

                if (isBlank(dto.getEmploymentType())) {
                    dto.setEmploymentType("REGULAR");
                }
                if (isBlank(dto.getEmploymentStatus())) {
                    dto.setEmploymentStatus("ACTIVE");
                }
                if (isBlank(dto.getFourInsured())) {
                    dto.setFourInsured("true");
                }
                if (isBlank(dto.getTaxScheme())) {
                    dto.setTaxScheme("SALARY_WITHHOLDING");
                }
                if (isBlank(dto.getGroupHireDate())) {
                    dto.setGroupHireDate(dto.getHireDate());
                }

                if (isEffectivelyEmpty(dto)) continue;
                validateAndNormalize(dto, seenEmpNo, seenUsername);
                rows.add(dto);
            }

            if (rows.isEmpty()) {
                throw new IllegalArgumentException("가져올 데이터가 없습니다.");
            }
            return rows;
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("CSV 파싱 실패: " + e.getMessage(), e);
        }
    }

    private void validateAndNormalize(EmployeeImportRowDTO dto, Set<String> seenEmpNo, Set<String> seenUsername) {
        if (isBlank(dto.getEmpNo())) {
            dto.addError("emp_no(사번)가 비어있습니다.");
        } else {
            dto.setEmpNo(dto.getEmpNo().trim());
            if (!dto.getEmpNo().matches("^[0-9]+$")) {
                dto.addError("emp_no(사번)은 숫자만 가능합니다.");
            }
        }
        if (isBlank(dto.getName())) {
            dto.addError("name(이름)이 비어있습니다.");
        } else {
            dto.setName(dto.getName().trim());
        }

        String employmentType = normalizeEmploymentType(dto.getEmploymentType());
        if (employmentType == null) {
            dto.addError("employment_type 값이 올바르지 않습니다(정규직/계약직/프리랜서).");
        } else {
            dto.setEmploymentType(employmentType);
        }

        String employmentStatus = normalizeEmploymentStatus(dto.getEmploymentStatus());
        if (!isBlank(dto.getEmploymentStatus()) && employmentStatus == null) {
            dto.addError("employment_status 값이 올바르지 않습니다(재직/휴직/퇴사).");
        }
        dto.setEmploymentStatus(employmentStatus == null ? "ACTIVE" : employmentStatus);

        String fourInsured = normalizeBoolean(dto.getFourInsured());
        if (!isBlank(dto.getFourInsured()) && fourInsured == null) {
            dto.addError("four_insured는 true/false 또는 가입/미가입 형태만 가능합니다.");
        }
        dto.setFourInsured(fourInsured);

        String taxScheme = normalizeTaxScheme(dto.getTaxScheme());
        if (!isBlank(dto.getTaxScheme()) && taxScheme == null) {
            dto.addError("tax_scheme 값이 올바르지 않습니다.");
        }
        dto.setTaxScheme(taxScheme);

        dto.setHireDate(normalizeDate(dto.getHireDate(), "hire_date", dto));
        dto.setGroupHireDate(normalizeDate(dto.getGroupHireDate(), "group_hire_date", dto));
        dto.setBirthDate(normalizeDate(dto.getBirthDate(), "birth_date", dto));
        dto.setTerminationDate(normalizeDate(dto.getTerminationDate(), "termination_date", dto));
        dto.setContractEndDate(normalizeDate(dto.getContractEndDate(), "contract_end_date", dto));

        if (dto.getHireDate() != null && dto.getGroupHireDate() == null) {
            dto.setGroupHireDate(dto.getHireDate());
        }
        if (dto.getHireDate() == null) {
            dto.addError("hire_date(입사일)가 비어있습니다.");
        }
        if (!isBlank(dto.getEmail())) {
            String email = dto.getEmail().trim();
            dto.setEmail(email);
            if (!email.contains("@")) {
                dto.addError("email 형식이 올바르지 않습니다.");
            }
        }
        if (!isBlank(dto.getPositionName())) {
            dto.setPositionName(dto.getPositionName().trim());
        }
        if (!isBlank(dto.getIdentificationNumber())) {
            String normalizedId = normalizeIdentificationNumber(dto.getIdentificationNumber());
            if (normalizedId == null) {
                dto.addError("identification_number(주민등록번호) 형식이 올바르지 않습니다.");
            } else {
                dto.setIdentificationNumber(normalizedId);
                LocalDate birthFromId = parseBirthDateFromIdentificationNumber(normalizedId);
                if (birthFromId != null) {
                    dto.setBirthDate(birthFromId.toString());
                    dto.setAge(String.valueOf(calculateAge(birthFromId)));
                }
                // 주민등록번호 7번째 자리로 성별 자동 추출
                if (isBlank(dto.getGender())) {
                    String genderFromId = parseGenderFromIdentificationNumber(normalizedId);
                    if (genderFromId != null) {
                        dto.setGender(genderFromId);
                    }
                }
            }
        }
        if (dto.getBirthDate() != null) {
            dto.setAge(String.valueOf(calculateAge(LocalDate.parse(dto.getBirthDate()))));
        }
        if (!isBlank(dto.getUsername())) {
            dto.setUsername(dto.getUsername().trim());
        } else {
            dto.setUsername(null);
        }

        if (dto.getHireDate() != null) {
            dto.setYearsOfService(String.valueOf(calculateYearsOfService(LocalDate.parse(dto.getHireDate()))));
        }

        if ("CONTRACT".equals(dto.getEmploymentType()) || "FREELANCER".equals(dto.getEmploymentType())) {
            if (dto.getContractEndDate() == null) {
                dto.addError("계약직/프리랜서는 contract_end_date(계약 종료일)가 필수입니다.");
            }
        }
        if ("FREELANCER".equals(dto.getEmploymentType())) {
            if (dto.getFourInsured() != null && !"false".equals(dto.getFourInsured())) {
                dto.addError("프리랜서 four_insured는 false(미가입)만 허용됩니다.");
            }
            dto.setFourInsured("false");
            dto.setTaxScheme("BUSINESS_INCOME_3_3");
        } else {
            if (dto.getFourInsured() == null) {
                dto.setFourInsured("true");
            }
            if (dto.getTaxScheme() == null) {
                dto.setTaxScheme("SALARY_WITHHOLDING");
            }
        }

        if (dto.getHireDate() != null && dto.getTerminationDate() != null) {
            LocalDate hire = LocalDate.parse(dto.getHireDate());
            LocalDate termination = LocalDate.parse(dto.getTerminationDate());
            if (termination.isBefore(hire)) {
                dto.addError("termination_date(퇴사일)가 hire_date(입사일)보다 빠릅니다.");
            }
        }

        if (!isBlank(dto.getEmpNo())) {
            if (!seenEmpNo.add(dto.getEmpNo())) {
                dto.addError("파일 내 emp_no 중복입니다: " + dto.getEmpNo());
            }
            if (employeeMapper.existsByEmpNo(dto.getEmpNo()) == 1) {
                dto.addError("이미 존재하는 사번(emp_no)입니다: " + dto.getEmpNo());
            }
        }
        if (!isBlank(dto.getUsername())) {
            if (!seenUsername.add(dto.getUsername())) {
                dto.addError("파일 내 username 중복입니다: " + dto.getUsername());
            }
            if (userMapper.existsByUsername(dto.getUsername()) == 1) {
                dto.addError("이미 존재하는 username 입니다: " + dto.getUsername());
            }
            if (employeeMapper.existsByLoginUsername(dto.getUsername()) == 1) {
                dto.addError("이미 존재하는 아이디(login_username)입니다: " + dto.getUsername());
            }
        }
    }

    private String normalizeDate(String raw, String fieldName, EmployeeImportRowDTO dto) {
        if (isBlank(raw)) return null;
        LocalDate parsed = parseFlexibleDate(raw.trim());
        if (parsed == null) {
            dto.addError(fieldName + " 형식이 올바르지 않습니다(예: 2026-12-31).");
            return raw;
        }
        return parsed.toString();
    }

    private boolean isEffectivelyEmpty(EmployeeImportRowDTO dto) {
        return isBlank(dto.getEmpNo())
                && isBlank(dto.getName())
                && isBlank(dto.getEmploymentType())
                && isBlank(dto.getEmploymentStatus())
                && isBlank(dto.getPositionName())
                && isBlank(dto.getIdentificationNumber())
                && isBlank(dto.getHireDate())
                && isBlank(dto.getEmail())
                && isBlank(dto.getBirthDate())
                && isBlank(dto.getTerminationDate())
                && isBlank(dto.getUsername());
    }

    private String createNamedList(Workbook wb, Sheet listSheet, int colIdx, List<String> options, String baseName) {
        Row header = listSheet.getRow(0);
        if (header == null) header = listSheet.createRow(0);
        header.createCell(colIdx).setCellValue(baseName);
        for (int i = 0; i < options.size(); i++) {
            Row row = listSheet.getRow(i + 1);
            if (row == null) row = listSheet.createRow(i + 1);
            row.createCell(colIdx).setCellValue(options.get(i));
        }
        Name name = wb.createName();
        name.setNameName(baseName);
        String colLetter = CellReference.convertNumToColString(colIdx);
        name.setRefersToFormula("'" + listSheet.getSheetName() + "'!$" + colLetter + "$2:$" + colLetter + "$" + (options.size() + 1));
        return baseName;
    }

    private void createDateCell(Row row, int idx, String value, CellStyle dateStyle) {
        Cell cell = row.createCell(idx);
        if (value != null && !value.isBlank()) {
            cell.setCellValue(value);
        }
        cell.setCellStyle(dateStyle);
    }

    private void addDropdownNamed(Sheet sheet, int firstRow, int lastRow, int colIdx, String namedRange) {
        if (colIdx < 0) return;
        DataValidationHelper helper = sheet.getDataValidationHelper();
        DataValidationConstraint constraint = helper.createFormulaListConstraint(namedRange);
        CellRangeAddressList addressList = new CellRangeAddressList(firstRow, lastRow, colIdx, colIdx);
        DataValidation validation = helper.createValidation(constraint, addressList);
        validation.setShowErrorBox(true);
        sheet.addValidationData(validation);
    }

    private Map<String, Integer> readHeaderMap(Row headerRow) {
        DataFormatter fmt = new DataFormatter();
        List<String> headers = new ArrayList<>();
        short last = headerRow.getLastCellNum();
        for (int i = 0; i < last; i++) {
            headers.add(fmt.formatCellValue(headerRow.getCell(i)));
        }
        return readHeaderMap(headers);
    }

    private Map<String, Integer> readHeaderMap(List<String> headers) {
        Map<String, Integer> col = new HashMap<>();
        for (int i = 0; i < headers.size(); i++) {
            String raw = headers.get(i);
            if (raw == null || raw.isBlank()) continue;
            col.putIfAbsent(toCanonicalHeader(raw), i);
        }
        return col;
    }

    private String toCanonicalHeader(String raw) {
        String s = raw == null ? "" : raw.replace("\uFEFF", "").trim();
        if (s.isEmpty()) return s;
        String compact = s.toLowerCase(Locale.ROOT)
                .replaceAll("[\\s\\-_.()\\[\\]{}*/\\\\:]", "");
        String alias = HEADER_ALIASES.get(s.toLowerCase(Locale.ROOT));
        if (alias != null) return alias;
        alias = HEADER_ALIASES.get(compact);
        if (alias != null) return alias;

        if (compact.contains("empnumber")) return "emp_no";
        if (compact.contains("employeename")) return "name";
        if (compact.contains("employmentstatus")) return "employment_status";
        if (compact.contains("employmenttype")) return "employment_type";
        if (compact.contains("grouphiredate")) return "group_hire_date";
        if (compact.contains("identificationnumber")) return "identification_number";
        if (compact.contains("yearsofservice")) return "years_of_service";
        if (compact.contains("hiredate") || compact.contains("startdate")) return "hire_date";
        if (compact.contains("terminationdate") || compact.contains("enddate")) return "termination_date";
        if (compact.contains("birthdate") || compact.contains("birthday")) return "birth_date";
        if (compact.equals("age")) return "age";
        if (compact.contains("positionname") || compact.equals("title")) return "position_name";
        return s;
    }

    private String getCellString(DataFormatter fmt, Row row, Integer colIdx) {
        if (colIdx == null) return null;
        Cell cell = row.getCell(colIdx);
        if (cell == null) return null;
        CellType type = cell.getCellType();
        if (type == CellType.FORMULA) {
            type = cell.getCachedFormulaResultType();
        }
        if (type == CellType.NUMERIC && !DateUtil.isCellDateFormatted(cell)) {
            String numericText = NumberToTextConverter.toText(cell.getNumericCellValue());
            return cleanCellText(numericText);
        }
        return cleanCellText(fmt.formatCellValue(cell));
    }

    private String getCellDateString(DataFormatter fmt, Row row, Integer colIdx) {
        if (colIdx == null) return null;
        Cell cell = row.getCell(colIdx);
        if (cell == null) return null;
        try {
            if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
                return cell.getLocalDateTimeCellValue().toLocalDate().toString();
            }
        } catch (Exception ignored) {
        }
        return getCellString(fmt, row, colIdx);
    }

    private List<String> parseCsvLine(String line) {
        List<String> out = new ArrayList<>();
        if (line == null) return out;
        StringBuilder cur = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (ch == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    cur.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (ch == ',' && !inQuotes) {
                out.add(cleanCsvValue(cur.toString()));
                cur.setLength(0);
            } else {
                cur.append(ch);
            }
        }
        out.add(cleanCsvValue(cur.toString()));
        return out;
    }

    private String cleanCsvValue(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        if (trimmed.isEmpty() || "NaN".equalsIgnoreCase(trimmed)) return null;
        return trimmed;
    }

    private String getCsv(List<String> cells, Integer idx) {
        if (idx == null || idx < 0 || idx >= cells.size()) return null;
        return cleanCsvValue(cells.get(idx));
    }

    private String cleanCellText(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private LocalDate parseFlexibleDate(String s) {
        if (s == null || s.isBlank()) return null;
        String v = s.trim();
        try {
            return LocalDate.parse(v, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (Exception ignored) {
        }
        DateTimeFormatter ymdVar = new DateTimeFormatterBuilder()
                .parseCaseInsensitive()
                .appendPattern("yyyy")
                .appendPattern("[-/.]")
                .appendPattern("M")
                .appendPattern("[-/.]")
                .appendPattern("d")
                .toFormatter(Locale.ROOT);
        try {
            return LocalDate.parse(v, ymdVar);
        } catch (Exception ignored) {
        }
        try {
            return LocalDate.parse(v, DateTimeFormatter.BASIC_ISO_DATE);
        } catch (Exception ignored) {
        }
        DateTimeFormatter mdy = new DateTimeFormatterBuilder()
                .parseCaseInsensitive()
                .appendPattern("M")
                .appendPattern("[-/.]")
                .appendPattern("d")
                .appendPattern("[-/.]")
                .appendPattern("yyyy")
                .toFormatter(Locale.ROOT);
        try {
            return LocalDate.parse(v, mdy);
        } catch (Exception ignored) {
        }
        return null;
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private String normalizeEmploymentType(String raw) {
        if (raw == null) return null;
        String compact = raw.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", "");
        String upper = compact.toUpperCase(Locale.ROOT);
        if (Set.of("REGULAR", "CONTRACT", "FREELANCER").contains(upper)) return upper;
        if (compact.contains("정규")) return "REGULAR";
        if (compact.contains("계약")) return "CONTRACT";
        if (compact.contains("프리")) return "FREELANCER";
        return null;
    }

    private String normalizeEmploymentStatus(String raw) {
        if (raw == null || raw.isBlank()) return null;
        String compact = raw.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", "");
        String upper = compact.toUpperCase(Locale.ROOT);
        if (Set.of("ACTIVE", "LEAVE", "TERMINATED").contains(upper)) return upper;
        if (compact.contains("재직")) return "ACTIVE";
        if (compact.contains("휴직")) return "LEAVE";
        if (compact.contains("퇴사")) return "TERMINATED";
        return null;
    }

    private String normalizeBoolean(String raw) {
        if (raw == null || raw.isBlank()) return null;
        String compact = raw.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", "");
        if (Set.of("true", "t", "1", "y", "yes", "o").contains(compact)) return "true";
        if (Set.of("false", "f", "0", "n", "no", "x").contains(compact)) return "false";
        if (compact.contains("미가입") || compact.contains("아니")) return "false";
        if (compact.contains("가입") || compact.contains("예")) return "true";
        return null;
    }

    private String normalizeTaxScheme(String raw) {
        if (raw == null || raw.isBlank()) return null;
        String compact = raw.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", "");
        String upper = compact.toUpperCase(Locale.ROOT);
        if (Set.of("SALARY_WITHHOLDING", "BUSINESS_INCOME_3_3").contains(upper)) return upper;
        if (compact.contains("원천") || compact.contains("근로") || compact.contains("급여")) return "SALARY_WITHHOLDING";
        if (compact.contains("3.3") || compact.contains("3,3") || compact.contains("사업")) return "BUSINESS_INCOME_3_3";
        return null;
    }

    private String normalizeIdentificationNumber(String raw) {
        if (raw == null || raw.isBlank()) return null;
        String trimmed = raw.trim();
        // 숫자 / 마스킹문자(*) / x,X 만 남기고 나머지 구분자는 제거
        String compact = trimmed.replaceAll("[^0-9xX*]", "");
        compact = compact.replace('x', '*').replace('X', '*');

        // 허용 예:
        // - 960101-1******
        // - 9601011******
        // - 960101-1234567
        // - 9601011234567
        // - 960101-*******
        // - 960101
        // - 960101-1xxxxxx
        if (!compact.matches("^\\d{6}([\\d*]{0,7})$")) {
            return null;
        }
        // DB에도 하이픈 포함 형태로 저장 (앞6자리-뒷자리)
        if (compact.length() > 6) {
            return compact.substring(0, 6) + "-" + compact.substring(6);
        }
        return compact;
    }

    private LocalDate parseBirthDateFromIdentificationNumber(String raw) {
        if (raw == null) return null;
        String compact = raw.replaceAll("[^0-9*]", "");
        if (compact.length() < 6) return null;

        for (String candidate : buildIdentificationBirthCandidates(compact)) {
            if (candidate.length() < 6) continue;
            String yy = candidate.substring(0, 2);
            String mm = candidate.substring(2, 4);
            String dd = candidate.substring(4, 6);
            int century = inferCenturyFromIdentificationNumber(candidate, yy);
            // century=1900, yy="96" -> 1900+96=1996 (정수 덧셈)
            int fullYear = century + Integer.parseInt(yy);
            LocalDate parsed = parseFlexibleDate(fullYear + "-" + mm + "-" + dd);
            if (parsed != null) {
                return parsed;
            }
        }
        return null;
    }

    private List<String> buildIdentificationBirthCandidates(String compact) {
        List<String> candidates = new ArrayList<>();

        // Excel 숫자 셀에 주민등록번호를 넣으면 맨 앞 0이 사라져 12자리/11자리로 읽히는 경우가 있다.
        // 이런 경우를 우선 복구 시도한 뒤, 원본 값도 후보로 함께 검사한다.
        if (compact.length() == 11) {
            candidates.add("00" + compact);
            candidates.add("0" + compact);
        } else if (compact.length() == 12) {
            candidates.add("0" + compact);
        }
        candidates.add(compact);
        return candidates;
    }

    private int inferCenturyFromIdentificationNumber(String compact, String yy) {
        if (compact != null && compact.length() >= 7) {
            char code = compact.charAt(6);
            switch (code) {
                case '1':
                case '2':
                case '5':
                case '6':
                    return 1900;
                case '3':
                case '4':
                case '7':
                case '8':
                    return 2000;
                case '9':
                case '0':
                    return 1800;
                default:
                    break;
            }
        }

        // 성별코드가 마스킹/누락된 경우에는 2자리 연도를 현재 연도와 비교해 1900/2000년대를 추정
        int year2 = Integer.parseInt(yy);
        int currentYear2 = LocalDate.now().getYear() % 100;
        return year2 <= currentYear2 ? 2000 : 1900;
    }

    private String parseGenderFromIdentificationNumber(String normalized) {
        if (normalized == null) return null;
        String compact = normalized.replaceAll("[^0-9]", "");
        if (compact.length() < 7) return null;
        char code = compact.charAt(6);
        return switch (code) {
            case '1', '3', '5', '7' -> "남성";
            case '2', '4', '6', '8' -> "여성";
            default -> null;
        };
    }

    private int calculateAge(LocalDate birthDate) {
        LocalDate now = LocalDate.now();
        int age = now.getYear() - birthDate.getYear();
        if (now.getMonthValue() < birthDate.getMonthValue()
                || (now.getMonthValue() == birthDate.getMonthValue() && now.getDayOfMonth() < birthDate.getDayOfMonth())) {
            age--;
        }
        return age;
    }

    private int calculateYearsOfService(LocalDate hireDate) {
        LocalDate now = LocalDate.now();
        int years = now.getYear() - hireDate.getYear();
        if (now.getMonthValue() < hireDate.getMonthValue()
                || (now.getMonthValue() == hireDate.getMonthValue() && now.getDayOfMonth() < hireDate.getDayOfMonth())) {
            years--;
        }
        return Math.max(years, 0);
    }
}

