package com.example.sayy.Entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class EmployeeDocumentEntity {
    private Long id;
    private String empNo;
    private String originalName;
    private String storedName;
    private Long fileSize;
    private String contentType;
    private String category;       // 계약서 / 증명서 / 기타
    private String uploadedBy;
    private LocalDateTime createdAt;

    /** 파일 크기를 사람이 읽기 좋은 단위로 변환 */
    public String getFileSizeFormatted() {
        if (fileSize == null || fileSize < 0) return "-";
        if (fileSize < 1024) return fileSize + " B";
        if (fileSize < 1024 * 1024) return String.format("%.1f KB", fileSize / 1024.0);
        return String.format("%.1f MB", fileSize / (1024.0 * 1024));
    }
}
