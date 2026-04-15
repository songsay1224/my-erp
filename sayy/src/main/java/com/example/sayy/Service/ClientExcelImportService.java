package com.example.sayy.Service;

import com.example.sayy.DTO.ClientImportRowDTO;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class ClientExcelImportService {

    // 템플릿 헤더 (CSV/Excel 공통)
    public static final List<String> TEMPLATE_HEADERS = List.of(
            "구분", "등록번호", "법인명(단체명)", "대표자",
            "법인등록번호", "지역", "주소", "상세주소", "대표번호", "홈페이지"
    );

    private static final Map<String, String> HEADER_ALIASES = Map.ofEntries(
            Map.entry("구분", "client_type"),
            Map.entry("등록번호", "business_reg_no"),
            Map.entry("사업자등록번호", "business_reg_no"),
            Map.entry("법인명(단체명)", "name"),
            Map.entry("법인명", "name"),
            Map.entry("단체명", "name"),
            Map.entry("거래처명", "name"),
            Map.entry("대표자", "ceo_name"),
            Map.entry("법인등록번호", "corporate_reg_no"),
            Map.entry("지역", "region"),
            Map.entry("주소", "address"),
            Map.entry("상세주소", "address_detail"),
            Map.entry("대표번호", "phone"),
            Map.entry("홈페이지", "homepage")
    );

    /** CSV 또는 XLSX 파일을 파싱해서 DTO 목록 반환 */
    public List<ClientImportRowDTO> parse(MultipartFile file) throws Exception {
        String filename = file.getOriginalFilename() != null ? file.getOriginalFilename().toLowerCase() : "";
        if (filename.endsWith(".csv")) {
            return parseCsv(file);
        } else if (filename.endsWith(".xlsx") || filename.endsWith(".xls")) {
            return parseXlsx(file);
        } else {
            throw new IllegalArgumentException("CSV 또는 Excel(.xlsx) 파일만 지원합니다.");
        }
    }

    /** CSV 파싱 */
    private List<ClientImportRowDTO> parseCsv(MultipartFile file) throws Exception {
        List<ClientImportRowDTO> result = new ArrayList<>();
        // UTF-8 시도 후 EUC-KR 폴백
        Charset cs = StandardCharsets.UTF_8;
        try {
            byte[] bytes = file.getBytes();
            // BOM 제거
            String content = new String(bytes, cs);
            if (content.startsWith("\uFEFF")) content = content.substring(1);
            String[] lines = content.split("\\r?\\n");
            if (lines.length == 0) throw new IllegalArgumentException("파일이 비어 있습니다.");

            Map<String, Integer> colMap = buildColMap(splitCsvLine(lines[0]));
            for (int i = 1; i < lines.length; i++) {
                String line = lines[i].trim();
                if (line.isEmpty()) continue;
                String[] cells = splitCsvLine(line);
                ClientImportRowDTO dto = mapCells(cells, colMap, i + 1);
                result.add(dto);
            }
        } catch (Exception e) {
            // EUC-KR로 재시도
            byte[] bytes = file.getBytes();
            String content = new String(bytes, Charset.forName("EUC-KR"));
            String[] lines = content.split("\\r?\\n");
            if (lines.length == 0) throw new IllegalArgumentException("파일이 비어 있습니다.");
            result.clear();
            Map<String, Integer> colMap = buildColMap(splitCsvLine(lines[0]));
            for (int i = 1; i < lines.length; i++) {
                String line = lines[i].trim();
                if (line.isEmpty()) continue;
                String[] cells = splitCsvLine(line);
                ClientImportRowDTO dto = mapCells(cells, colMap, i + 1);
                result.add(dto);
            }
        }
        return result;
    }

    /** XLSX 파싱 */
    private List<ClientImportRowDTO> parseXlsx(MultipartFile file) throws Exception {
        List<ClientImportRowDTO> result = new ArrayList<>();
        try (Workbook wb = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = wb.getSheetAt(0);
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) throw new IllegalArgumentException("헤더 행이 없습니다.");

            Map<String, Integer> colMap = new HashMap<>();
            for (Cell cell : headerRow) {
                String key = toCanonical(getCellString(cell));
                if (key != null) colMap.put(key, cell.getColumnIndex());
            }

            for (int r = 1; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null) continue;
                String[] cells = new String[row.getLastCellNum()];
                for (int c = 0; c < cells.length; c++) {
                    cells[c] = getCellString(row.getCell(c));
                }
                ClientImportRowDTO dto = mapCells(cells, colMap, r + 1);
                result.add(dto);
            }
        }
        return result;
    }

    private Map<String, Integer> buildColMap(String[] headers) {
        Map<String, Integer> map = new HashMap<>();
        for (int i = 0; i < headers.length; i++) {
            String key = toCanonical(headers[i].trim());
            if (key != null) map.put(key, i);
        }
        return map;
    }

    private ClientImportRowDTO mapCells(String[] cells, Map<String, Integer> colMap, int rowNum) {
        ClientImportRowDTO dto = new ClientImportRowDTO();
        dto.setRowNum(rowNum);
        dto.setClientType(get(cells, colMap, "client_type"));
        dto.setBusinessRegNo(get(cells, colMap, "business_reg_no"));
        dto.setName(get(cells, colMap, "name"));
        dto.setCeoName(get(cells, colMap, "ceo_name"));
        dto.setCorporateRegNo(get(cells, colMap, "corporate_reg_no"));
        dto.setRegion(get(cells, colMap, "region"));
        dto.setAddress(get(cells, colMap, "address"));
        dto.setAddressDetail(get(cells, colMap, "address_detail"));
        dto.setPhone(get(cells, colMap, "phone"));
        dto.setHomepage(get(cells, colMap, "homepage"));

        // 검증
        if (dto.getName() == null || dto.getName().isBlank()) {
            dto.setErrorMessage("법인명(단체명)은 필수입니다.");
        }
        return dto;
    }

    private String get(String[] cells, Map<String, Integer> colMap, String key) {
        Integer idx = colMap.get(key);
        if (idx == null || idx >= cells.length) return null;
        String v = cells[idx];
        return (v == null || v.isBlank()) ? null : v.trim();
    }

    private String toCanonical(String raw) {
        if (raw == null) return null;
        String trimmed = raw.trim();
        return HEADER_ALIASES.getOrDefault(trimmed, null);
    }

    /** CSV 한 줄을 셀 배열로 분리 (쌍따옴표 처리) */
    private String[] splitCsvLine(String line) {
        List<String> tokens = new ArrayList<>();
        boolean inQuote = false;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                if (inQuote && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    sb.append('"'); i++;
                } else {
                    inQuote = !inQuote;
                }
            } else if (c == ',' && !inQuote) {
                tokens.add(sb.toString());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        tokens.add(sb.toString());
        return tokens.toArray(new String[0]);
    }

    private String getCellString(Cell cell) {
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> {
                double d = cell.getNumericCellValue();
                yield d == Math.floor(d) ? String.valueOf((long) d) : String.valueOf(d);
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> {
                try { yield cell.getStringCellValue().trim(); }
                catch (Exception e) { yield String.valueOf(cell.getNumericCellValue()); }
            }
            default -> "";
        };
    }

    /** 다운로드용 템플릿 XLSX 생성 */
    public byte[] generateTemplate() throws Exception {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("거래처목록");

            CellStyle headerStyle = wb.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            Font font = wb.createFont();
            font.setBold(true);
            headerStyle.setFont(font);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            CellStyle reqStyle = wb.createCellStyle();
            reqStyle.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
            reqStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            reqStyle.setBorderBottom(BorderStyle.THIN);
            reqStyle.setFont(font);
            reqStyle.setAlignment(HorizontalAlignment.CENTER);

            Row headerRow = sheet.createRow(0);
            String[] headers = TEMPLATE_HEADERS.toArray(new String[0]);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                // 법인명은 필수 (노란색)
                cell.setCellStyle(i == 2 ? reqStyle : headerStyle);
            }

            // 컬럼 너비
            int[] widths = {8, 16, 25, 10, 18, 8, 30, 20, 14, 24};
            for (int i = 0; i < widths.length && i < headers.length; i++) {
                sheet.setColumnWidth(i, widths[i] * 256);
            }

            // 구분 드롭다운
            var dvHelper = sheet.getDataValidationHelper();
            var constraint = dvHelper.createExplicitListConstraint(new String[]{"매출", "매입", "매출매입"});
            var addressList = new org.apache.poi.ss.util.CellRangeAddressList(1, 500, 0, 0);
            var dv = dvHelper.createValidation(constraint, addressList);
            dv.setShowErrorBox(true);
            sheet.addValidationData(dv);

            // 예시 데이터
            Row ex = sheet.createRow(1);
            String[] example = {"매출", "113-86-22134", "주식회사 예시거래처", "홍길동",
                    "110111-3906950", "서울", "서울특별시 구로구 디지털로 100", "501호", "02-1234-5678", "https://example.com"};
            for (int i = 0; i < example.length; i++) {
                ex.createCell(i).setCellValue(example[i]);
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            wb.write(out);
            return out.toByteArray();
        }
    }
}
