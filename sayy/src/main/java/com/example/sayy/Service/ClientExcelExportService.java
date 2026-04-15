package com.example.sayy.Service;

import com.example.sayy.Entity.ClientEntity;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ClientExcelExportService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static final String[] HEADERS = {
            "No.", "구분", "사업자등록번호", "법인명(단체명)", "대표자",
            "법인등록번호", "개업연월일", "지역", "주소", "상세주소",
            "업태", "종목", "대표번호", "이메일", "홈페이지",
            "은행", "계좌번호", "예금자명",
            "상태", "메모"
    };

    public byte[] export(List<ClientEntity> clients) throws IOException {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("거래처목록");

            // 헤더 스타일
            CellStyle headerStyle = wb.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            Font headerFont = wb.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            // 헤더 행
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < HEADERS.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(HEADERS[i]);
                cell.setCellStyle(headerStyle);
            }

            // 컬럼 너비
            int[] widths = {6, 8, 16, 25, 10, 18, 12, 8, 30, 20, 12, 12, 14, 22, 24, 10, 16, 10, 8, 20};
            for (int i = 0; i < widths.length; i++) {
                sheet.setColumnWidth(i, widths[i] * 256);
            }

            // 데이터 행
            CellStyle dataStyle = wb.createCellStyle();
            dataStyle.setBorderBottom(BorderStyle.THIN);
            dataStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            int rowNum = 1;
            for (ClientEntity c : clients) {
                Row row = sheet.createRow(rowNum++);
                int col = 0;
                setCell(row, col++, String.valueOf(rowNum - 1), dataStyle);
                setCell(row, col++, c.getClientType() != null ? c.getClientType() : "매출", dataStyle);
                setCell(row, col++, c.getBusinessRegNoFormatted(), dataStyle);
                setCell(row, col++, c.getName(), dataStyle);
                setCell(row, col++, c.getCeoName(), dataStyle);
                setCell(row, col++, c.getCorporateRegNoFormatted(), dataStyle);
                setCell(row, col++, c.getOpenedDate() != null ? c.getOpenedDate().format(DATE_FMT) : "", dataStyle);
                setCell(row, col++, c.getRegion(), dataStyle);
                setCell(row, col++, c.getAddress(), dataStyle);
                setCell(row, col++, c.getAddressDetail(), dataStyle);
                setCell(row, col++, c.getBusinessType(), dataStyle);
                setCell(row, col++, c.getBusinessItem(), dataStyle);
                setCell(row, col++, c.getPhone(), dataStyle);
                setCell(row, col++, c.getEmail(), dataStyle);
                setCell(row, col++, c.getHomepage(), dataStyle);
                setCell(row, col++, c.getBankName(), dataStyle);
                setCell(row, col++, c.getBankAccount(), dataStyle);
                setCell(row, col++, c.getBankHolder(), dataStyle);
                setCell(row, col++, c.getStatusLabel(), dataStyle);
                setCell(row, col++, c.getMemo(), dataStyle);
            }

            // 자동 필터
            sheet.setAutoFilter(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, HEADERS.length - 1));

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            wb.write(out);
            return out.toByteArray();
        }
    }

    private void setCell(Row row, int col, String value, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(value != null ? value : "");
        cell.setCellStyle(style);
    }
}
