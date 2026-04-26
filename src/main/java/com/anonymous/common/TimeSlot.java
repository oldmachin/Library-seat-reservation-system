package com.anonymous.common;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class TimeSlot {

    private final static DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private final String code;

    private final LocalTime startTime;

    private final LocalTime endTime;

    public TimeSlot(String code, LocalTime startTime, LocalTime endTime) {
        this.code = code;
        this.startTime = startTime.withNano(0);
        this.endTime = endTime.withNano(0);
    }

    public String getCode() {
        return code;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public String getLabel() {
        return startTime.format(FORMATTER) + "-" + endTime.format(FORMATTER);
    }

    public static List<TimeSlot> defaultSlots() {
        List<TimeSlot> slots = new ArrayList<>();

        LocalTime current = LocalTime.of(8, 0);
        LocalTime dayEnd = LocalTime.of(21, 30);

        int index = 1;
        while (current.isBefore(dayEnd)) {
            LocalTime next = current.plusMinutes(30);
            slots.add(new TimeSlot("S" + index, current, next));
            current = next;
            index++;
        }

        return slots;
    }
}
