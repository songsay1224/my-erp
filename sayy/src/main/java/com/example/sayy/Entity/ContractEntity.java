package com.example.sayy.Entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class ContractEntity {
    private Long id;
    private Long projectId;
    private String title;
    private String contractType;   // MAIN / ADDITIONAL / AMENDMENT
    private LocalDate contractDate;
    private Long amount;
    private String status;         // DRAFT / REVIEW / SIGNED
    private String originalName;
    private String storedName;
    private Long fileSize;
    private String contentType;
    private String memo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public String getContractTypeLabel() {
        if (contractType == null) return "-";
        return switch (contractType) {
            case "MAIN"       -> "본계약";
            case "ADDITIONAL" -> "추가계약";
            case "AMENDMENT"  -> "변경계약";
            default           -> contractType;
        };
    }

    public String getStatusLabel() {
        if (status == null) return "-";
        return switch (status) {
            case "DRAFT"  -> "초안";
            case "REVIEW" -> "검토중";
            case "SIGNED" -> "서명완료";
            default       -> status;
        };
    }

    public String getStatusBadgeClass() {
        if (status == null) return "text-bg-light";
        return switch (status) {
            case "DRAFT"  -> "text-bg-secondary";
            case "REVIEW" -> "text-bg-warning";
            case "SIGNED" -> "text-bg-success";
            default       -> "text-bg-light";
        };
    }

    public String getFileSizeFormatted() {
        if (fileSize == null || fileSize < 0) return "-";
        if (fileSize < 1024) return fileSize + " B";
        if (fileSize < 1024 * 1024) return String.format("%.1f KB", fileSize / 1024.0);
        return String.format("%.1f MB", fileSize / (1024.0 * 1024));
    }
}
