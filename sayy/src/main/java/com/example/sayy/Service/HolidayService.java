package com.example.sayy.Service;

import com.example.sayy.Entity.HolidayEntity;
import com.example.sayy.Mapper.HolidayMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class HolidayService {
    private final HolidayMapper holidayMapper;
    private final ObjectMapper objectMapper;

    @Value("${api.service-key}")
    private String serviceKey;

    public HolidayService(HolidayMapper holidayMapper, ObjectMapper objectMapper) {
        this.holidayMapper = holidayMapper;
        this.objectMapper = objectMapper;
    }

    /**
     * 공공데이터포털(SpcdeInfoService)에서 연 단위 공휴일 정보를 조회합니다.
     */
    public List<HolidayEntity> fetchHolidays(int year) {
        try {
            String url = buildUrl(year);
            String json = httpGet(url);
            return parse(json);
        } catch (Exception e) {
            throw new IllegalStateException("휴일 API 조회 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 조회한 공휴일 정보를 DB(holidays)에 upsert 적재합니다.
     */
    public SyncResult syncHolidays(int year) {
        List<HolidayEntity> items = fetchHolidays(year);
        int processed = 0;
        for (HolidayEntity h : items) {
            holidayMapper.upsert(h);
            processed++;
        }
        return new SyncResult(year, items.size(), processed);
    }

    private String buildUrl(int year) {
        String base = "http://apis.data.go.kr/B090041/openapi/service/SpcdeInfoService/getRestDeInfo";
        String sb = base + "?serviceKey=" + encode(serviceKey) +
                "&_type=json" +
                "&solYear=" + year +
                "&numOfRows=400";
        return sb;
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private String httpGet(String url) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();
        HttpResponse<String> res = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (res.statusCode() < 200 || res.statusCode() >= 300) {
            throw new IllegalStateException("HTTP " + res.statusCode() + ": " + res.body());
        }
        return res.body();
    }

    private List<HolidayEntity> parse(String json) throws Exception {
        List<HolidayEntity> out = new ArrayList<>();
        JsonNode root = objectMapper.readTree(json);

        JsonNode itemNode = root.path("response").path("body").path("items").path("item");
        if (itemNode.isMissingNode() || itemNode.isNull()) {
            return out;
        }

        if (itemNode.isArray()) {
            for (JsonNode n : itemNode) {
                HolidayEntity h = toHoliday(n);
                if (h != null) out.add(h);
            }
        } else if (itemNode.isObject()) {
            HolidayEntity h = toHoliday(itemNode);
            if (h != null) out.add(h);
        }

        return out;
    }

    private HolidayEntity toHoliday(JsonNode n) {
        String dateName = n.path("dateName").asText(null);
        String locdateStr = n.path("locdate").asText(null); // yyyyMMdd
        String isHolidayStr = n.path("isHoliday").asText("Y");

        if (locdateStr == null || locdateStr.isBlank()) return null;
        LocalDate locDate = LocalDate.parse(locdateStr, DateTimeFormatter.BASIC_ISO_DATE);

        HolidayEntity h = new HolidayEntity();
        h.setLocDate(locDate);
        h.setName(dateName == null ? "공휴일" : dateName);
        h.setHoliday("Y".equalsIgnoreCase(isHolidayStr));
        return h;
    }

    public record SyncResult(int year, int fetchedCount, int processedCount) {}
}