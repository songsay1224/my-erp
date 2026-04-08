package com.example.sayy.DTO;

import java.util.Map;

/**
 * FullCalendar event JSON 형태에 맞춘 DTO
 * - 참고: https://fullcalendar.io/docs/event-object
 */
public class CalendarEventDTO {
    private String id;
    private String title;
    private String start; // ISO date string (YYYY-MM-DD)
    private boolean allDay = true;
    private String color; // optional
    private Map<String, Object> extendedProps; // optional

    public CalendarEventDTO() {}

    public CalendarEventDTO(String id, String title, String start, boolean allDay, String color, Map<String, Object> extendedProps) {
        this.id = id;
        this.title = title;
        this.start = start;
        this.allDay = allDay;
        this.color = color;
        this.extendedProps = extendedProps;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public boolean isAllDay() {
        return allDay;
    }

    public void setAllDay(boolean allDay) {
        this.allDay = allDay;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public Map<String, Object> getExtendedProps() {
        return extendedProps;
    }

    public void setExtendedProps(Map<String, Object> extendedProps) {
        this.extendedProps = extendedProps;
    }
}

