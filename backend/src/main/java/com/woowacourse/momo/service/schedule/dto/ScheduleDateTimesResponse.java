package com.woowacourse.momo.service.schedule.dto;

import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import com.woowacourse.momo.domain.schedule.Schedule;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public record ScheduleDateTimesResponse(
        LocalDate date,
        @JsonFormat(shape = Shape.STRING, pattern = "HH:mm", timezone = "Asia/Seoul") List<LocalTime> times
) {

    public static List<ScheduleDateTimesResponse> from(List<Schedule> schedules) {
        Map<LocalDate, List<LocalTime>> results = schedules.stream()
                .collect(Collectors.groupingBy(Schedule::date, mapping(Schedule::time, toList())));

        return results.keySet().stream()
                .map(date -> new ScheduleDateTimesResponse(date, results.get(date)))
                .toList();
    }
}