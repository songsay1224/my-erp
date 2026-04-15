package com.example.sayy.Service;

import com.example.sayy.Entity.CertificateEntity;
import com.example.sayy.Entity.EducationEntity;
import com.example.sayy.Entity.EmployeeEntity;
import com.example.sayy.Mapper.CareerMapper;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class EmployeeExcelExportService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static final String[] HEADERS = {
            "사번", "이름", "영문이름", "상태", "고용형태",
            "직위", "기술등급", "부서", "이메일",
            "입사일", "그룹입사일", "퇴사일", "계약종료일", "근속년수",
            "생년월일", "나이", "성별", "국적",
            "주민등록번호", "휴대전화",
            "은행", "계좌번호", "예금주",
            "근로계약시작", "근로계약종료", "수습시작", "수습종료", "수습급여율(%)",
            "소득유형", "임금계약시작", "임금계약종료", "월기본급(원)"
    };

    private static final String[] EDU_HEADERS = {
            "사번", "이름", "학교명", "전공", "학위", "입학일", "졸업일", "졸업상태"
    };

    private static final String[] CERT_HEADERS = {
            "사번", "이름", "자격증명", "발급기관", "취득일", "만료일"
    };

    private final CareerMapper careerMapper;

    public EmployeeExcelExportService(CareerMapper careerMapper) {
        this.careerMapper = careerMapper;
    }

    public byte[] export(List<EmployeeEntity> employees) throws IOException {
        try (XSSFWorkbook wb = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = wb.createSheet("인사정보");

            // 열 너비 설정 (기술등급 컬럼 추가)
            int[] colWidths = {
                    14, 12, 18, 10, 12,
                    12, 12, 16, 24,
                    14, 14, 14, 14, 10,
                    14, 8, 8, 10,
                    20, 16,
                    12, 18, 12,
                    14, 14, 14, 14, 12,
                    12, 14, 14, 16
            };
            for (int i = 0; i < colWidths.length; i++) {
                sheet.setColumnWidth(i, colWidths[i] * 256);
            }

            // 헤더 스타일
            CellStyle headerStyle = wb.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            Font headerFont = wb.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 10);
            headerStyle.setFont(headerFont);

            // 데이터 스타일
            CellStyle dataStyle = wb.createCellStyle();
            dataStyle.setBorderBottom(BorderStyle.THIN);
            dataStyle.setBorderTop(BorderStyle.THIN);
            dataStyle.setBorderLeft(BorderStyle.THIN);
            dataStyle.setBorderRight(BorderStyle.THIN);
            dataStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            CellStyle numberStyle = wb.createCellStyle();
            numberStyle.cloneStyleFrom(dataStyle);
            DataFormat fmt = wb.createDataFormat();
            numberStyle.setDataFormat(fmt.getFormat("#,##0"));
            numberStyle.setAlignment(HorizontalAlignment.RIGHT);

            // 헤더 행
            Row headerRow = sheet.createRow(0);
            headerRow.setHeightInPoints(20);
            for (int i = 0; i < HEADERS.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(HEADERS[i]);
                cell.setCellStyle(headerStyle);
            }

            // 데이터 행
            int rowIdx = 1;
            for (EmployeeEntity e : employees) {
                Row row = sheet.createRow(rowIdx++);
                row.setHeightInPoints(16);

                int col = 0;
                setStr(row, col++, e.getEmpNo(), dataStyle);
                setStr(row, col++, e.getName(), dataStyle);
                setStr(row, col++, e.getNameEn(), dataStyle);
                setStr(row, col++, e.getEmploymentStatus() != null ? e.getEmploymentStatus().getLabel() : null, dataStyle);
                setStr(row, col++, e.getEmploymentType() != null ? e.getEmploymentType().getLabel() : null, dataStyle);
                setStr(row, col++, e.getPositionName(), dataStyle);
                setStr(row, col++, e.getTechGrade(), dataStyle);
                setStr(row, col++, e.getDepartmentName(), dataStyle);
                setStr(row, col++, e.getEmail(), dataStyle);
                setDate(row, col++, e.getHireDate(), dataStyle);
                setDate(row, col++, e.getGroupHireDate(), dataStyle);
                setDate(row, col++, e.getTerminationDate(), dataStyle);
                setDate(row, col++, e.getContractEndDate(), dataStyle);
                setNum(row, col++, e.getYearsOfServiceCalculated() != null ? e.getYearsOfServiceCalculated().longValue() : null, numberStyle);
                setDate(row, col++, e.getBirthDate(), dataStyle);
                setNum(row, col++, e.getAgeCalculated() != null ? e.getAgeCalculated().longValue() : null, numberStyle);
                setStr(row, col++, e.getGenderResolved(), dataStyle);
                setStr(row, col++, e.getNationality(), dataStyle);
                setStr(row, col++, e.getIdentificationNumberFormatted(), dataStyle);
                setStr(row, col++, e.getPhone(), dataStyle);
                setStr(row, col++, e.getBankName(), dataStyle);
                setStr(row, col++, e.getBankAccount(), dataStyle);
                setStr(row, col++, e.getBankHolder(), dataStyle);
                setDate(row, col++, e.getLaborContractStart(), dataStyle);
                setDate(row, col++, e.getLaborContractEnd(), dataStyle);
                setDate(row, col++, e.getProbationStart(), dataStyle);
                setDate(row, col++, e.getProbationEnd(), dataStyle);
                setNum(row, col++, e.getProbationPayRate() != null ? e.getProbationPayRate().longValue() : null, numberStyle);
                setStr(row, col++, e.getIncomeType(), dataStyle);
                setDate(row, col++, e.getWageContractStart(), dataStyle);
                setDate(row, col++, e.getWageContractEnd(), dataStyle);
                setNum(row, col++, e.getMonthlySalary(), numberStyle);
            }

            // 헤더 행 고정
            sheet.createFreezePane(0, 1);

            // 자동 필터
            sheet.setAutoFilter(new CellRangeAddress(0, 0, 0, HEADERS.length - 1));

            // ===== 학력 시트 =====
            buildEducationSheet(wb, employees, headerStyle, dataStyle);

            // ===== 자격증 시트 =====
            buildCertificateSheet(wb, employees, headerStyle, dataStyle);

            wb.write(out);
            return out.toByteArray();
        }
    }

    private void buildEducationSheet(XSSFWorkbook wb, List<EmployeeEntity> employees,
                                     CellStyle headerStyle, CellStyle dataStyle) {
        Sheet sheet = wb.createSheet("학력");
        int[] widths = {14, 12, 20, 16, 12, 14, 14, 12};
        for (int i = 0; i < widths.length; i++) sheet.setColumnWidth(i, widths[i] * 256);

        Row hRow = sheet.createRow(0);
        hRow.setHeightInPoints(20);
        for (int i = 0; i < EDU_HEADERS.length; i++) {
            Cell c = hRow.createCell(i);
            c.setCellValue(EDU_HEADERS[i]);
            c.setCellStyle(headerStyle);
        }
        sheet.createFreezePane(0, 1);
        sheet.setAutoFilter(new CellRangeAddress(0, 0, 0, EDU_HEADERS.length - 1));

        int rowIdx = 1;
        for (EmployeeEntity e : employees) {
            List<EducationEntity> edus = careerMapper.selectEducationsByEmpNo(e.getEmpNo());
            for (EducationEntity edu : edus) {
                Row row = sheet.createRow(rowIdx++);
                row.setHeightInPoints(16);
                int col = 0;
                setStr(row, col++, e.getEmpNo(), dataStyle);
                setStr(row, col++, e.getName(), dataStyle);
                setStr(row, col++, edu.getSchoolName(), dataStyle);
                setStr(row, col++, edu.getMajor(), dataStyle);
                setStr(row, col++, edu.getDegree(), dataStyle);
                setDate(row, col++, edu.getStartDate(), dataStyle);
                setDate(row, col++, edu.getEndDate(), dataStyle);
                setStr(row, col++, edu.getGraduationStatus(), dataStyle);
            }
        }
    }

    private void buildCertificateSheet(XSSFWorkbook wb, List<EmployeeEntity> employees,
                                       CellStyle headerStyle, CellStyle dataStyle) {
        Sheet sheet = wb.createSheet("자격증");
        int[] widths = {14, 12, 24, 20, 14, 14};
        for (int i = 0; i < widths.length; i++) sheet.setColumnWidth(i, widths[i] * 256);

        Row hRow = sheet.createRow(0);
        hRow.setHeightInPoints(20);
        for (int i = 0; i < CERT_HEADERS.length; i++) {
            Cell c = hRow.createCell(i);
            c.setCellValue(CERT_HEADERS[i]);
            c.setCellStyle(headerStyle);
        }
        sheet.createFreezePane(0, 1);
        sheet.setAutoFilter(new CellRangeAddress(0, 0, 0, CERT_HEADERS.length - 1));

        int rowIdx = 1;
        for (EmployeeEntity e : employees) {
            List<CertificateEntity> certs = careerMapper.selectCertificatesByEmpNo(e.getEmpNo());
            for (CertificateEntity cert : certs) {
                Row row = sheet.createRow(rowIdx++);
                row.setHeightInPoints(16);
                int col = 0;
                setStr(row, col++, e.getEmpNo(), dataStyle);
                setStr(row, col++, e.getName(), dataStyle);
                setStr(row, col++, cert.getCertName(), dataStyle);
                setStr(row, col++, cert.getIssuer(), dataStyle);
                setDate(row, col++, cert.getAcquiredDate(), dataStyle);
                setDate(row, col++, cert.getExpiryDate(), dataStyle);
            }
        }
    }

    private void setStr(Row row, int col, String val, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(val != null ? val : "");
        cell.setCellStyle(style);
    }

    private void setDate(Row row, int col, LocalDate val, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(val != null ? val.format(DATE_FMT) : "");
        cell.setCellStyle(style);
    }

    private void setNum(Row row, int col, Long val, CellStyle style) {
        Cell cell = row.createCell(col);
        if (val != null) cell.setCellValue(val);
        else cell.setCellValue("");
        cell.setCellStyle(style);
    }
}
