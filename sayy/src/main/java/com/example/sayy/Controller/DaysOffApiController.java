package com.example.sayy.Controller;

import com.example.sayy.DTO.CalendarEventDTO;
import com.example.sayy.Entity.DayOffCustomEntity;
import com.example.sayy.Entity.HolidayEntity;
import com.example.sayy.Service.DaysOffService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/admin/api/days-off")
public class DaysOffApiController {

    private final DaysOffService daysOffService;

    public DaysOffApiController(DaysOffService daysOffService) {
        this.daysOffService = daysOffService;
    }

    @GetMapping("/events")
    public List<CalendarEventDTO> events(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
                                         @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        // FullCalendar: end는 보통 exclusive (다음날/다음달 0시)로 넘어옴
        LocalDate endInclusive = end.minusDays(1);
        int fromYear = start.getYear();
        int toYear = endInclusive.getYear();

        // 커스텀(해당 연도) + 반복(전체) → 연도별로 날짜에 매핑
        List<DayOffCustomEntity> repeatAll = daysOffService.getRepeatCustomDaysOff();

        List<CalendarEventDTO> out = new ArrayList<>();

        for (int y = fromYear; y <= toYear; y++) {
            // 해당 연도 커스텀
            List<DayOffCustomEntity> exact = daysOffService.getCustomDaysOff(y);

            // date -> custom entity (우선순위: exact > repeat)
            Map<LocalDate, DayOffCustomEntity> customByDate = new HashMap<>();
            for (DayOffCustomEntity c : exact) {
                if (c.getLocDate() != null) customByDate.put(c.getLocDate(), c);
            }
            for (DayOffCustomEntity c : repeatAll) {
                if (c.getLocDate() == null) continue;
                LocalDate mapped;
                try {
                    mapped = c.getLocDate().withYear(y);
                } catch (Exception ignore) {
                    // 예: 2/29 반복을 평년으로 매핑 불가 → 스킵
                    continue;
                }
                customByDate.putIfAbsent(mapped, c);
            }

            // 커스텀 이벤트 추가 (기간 필터)
            for (Map.Entry<LocalDate, DayOffCustomEntity> e : customByDate.entrySet()) {
                LocalDate d = e.getKey();
                if (d.isBefore(start) || d.isAfter(endInclusive)) continue;

                DayOffCustomEntity c = e.getValue();
                long customId = (c.getId() == null) ? -1L : c.getId();

                Map<String, Object> props = new LinkedHashMap<>();
                props.put("type", "custom");
                props.put("customId", customId);
                props.put("repeatAnnually", c.isRepeatAnnually());
                props.put("originDate", c.getLocDate() == null ? null : c.getLocDate().toString());

                out.add(new CalendarEventDTO(
                        "custom-" + customId + "-" + d,
                        c.getName() == null ? "커스텀 휴무" : c.getName(),
                        d.toString(),
                        true,
                        "#2563eb",
                        props
                ));
            }

            // 공휴일 이벤트 추가 (기간 필터)
            List<HolidayEntity> holidays = daysOffService.getHolidays(y);
            for (HolidayEntity h : holidays) {
                if (!h.isHoliday()) continue;
                LocalDate d = h.getLocDate();
                if (d == null) continue;
                if (d.isBefore(start) || d.isAfter(endInclusive)) continue;

                Map<String, Object> props = new LinkedHashMap<>();
                props.put("type", "holiday");

                out.add(new CalendarEventDTO(
                        "holiday-" + d,
                        h.getName() == null ? "공휴일" : h.getName(),
                        d.toString(),
                        true,
                        "#ef4444",
                        props
                ));
            }
        }

        // 정렬(날짜 → 타입)
        out.sort(Comparator.comparing(CalendarEventDTO::getStart).thenComparing(CalendarEventDTO::getId));
        return out;
    }

    public record UpsertCustomRequest(
            String date,
            String name,
            boolean repeatAnnually
    ) {}

    @PostMapping("/custom")
    public DayOffCustomEntity upsertCustom(@RequestBody UpsertCustomRequest req) {
        LocalDate d = (req.date == null || req.date.isBlank()) ? null : LocalDate.parse(req.date);
        daysOffService.upsertCustom(d, req.name, req.repeatAnnually);
        // upsert 후 id를 알기 위해 date로 재조회 (loc_date UNIQUE 전제)
        return daysOffService.findCustomByLocDate(d);
    }

    @DeleteMapping("/custom/{id}")
    public Map<String, Object> deleteCustom(@PathVariable long id) {
        daysOffService.deleteCustom(id);
        return Map.of("deleted", true, "id", id);
    }
}

