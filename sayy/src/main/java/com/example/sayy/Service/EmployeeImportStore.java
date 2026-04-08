package com.example.sayy.Service;

import com.example.sayy.DTO.EmployeeImportRowDTO;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class EmployeeImportStore {

    public record PreviewPayload(List<EmployeeImportRowDTO> rows, Instant createdAt) {}

    private final Map<String, PreviewPayload> previews = new ConcurrentHashMap<>();

    public String putPreview(List<EmployeeImportRowDTO> rows) {
        String token = UUID.randomUUID().toString();
        previews.put(token, new PreviewPayload(rows, Instant.now()));
        return token;
    }

    public PreviewPayload getPreview(String token) {
        return previews.get(token);
    }

    public PreviewPayload removePreview(String token) {
        return previews.remove(token);
    }
}

