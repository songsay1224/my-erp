package com.example.sayy.Config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.servlet.FlashMap;
import org.springframework.web.servlet.support.RequestContextUtils;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({MaxUploadSizeExceededException.class, MultipartException.class})
    public String handleMultipartTooLarge(Exception ex, HttpServletRequest request, HttpServletResponse response) {
        // 413 등 업로드 오류가 나면 사용자 경험을 위해 드로어로 복귀 + flash 메시지
        FlashMap flashMap = new FlashMap();
        flashMap.put("error", "업로드 파일 용량이 너무 큽니다. (최대 50MB)");
        var manager = RequestContextUtils.getFlashMapManager(request);
        if (manager != null) {
            manager.saveOutputFlashMap(flashMap, request, response);
        }

        String target = "/admin/settings?drawer=company";
        String referer = request.getHeader("Referer");
        if (referer != null && referer.contains("/admin/settings") && referer.contains("drawer=")) {
            int idx = referer.indexOf("/admin/settings");
            if (idx >= 0) target = referer.substring(idx);
        }
        return "redirect:" + target;
    }

    // 일부 환경에서는 용량 초과가 IllegalStateException으로 올라오는 케이스가 있어 방어
    @ExceptionHandler(IllegalStateException.class)
    public String handleIllegalState(IllegalStateException ex, HttpServletRequest request, HttpServletResponse response) {
        String msg = ex.getMessage() == null ? "" : ex.getMessage().toLowerCase();
        if (msg.contains("exceed") || msg.contains("size") || msg.contains("request") || msg.contains("upload")) {
            return handleMultipartTooLarge(ex, request, response);
        }
        throw ex;
    }
}

