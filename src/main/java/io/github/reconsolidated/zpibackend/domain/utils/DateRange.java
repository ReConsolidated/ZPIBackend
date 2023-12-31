package io.github.reconsolidated.zpibackend.domain.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@AllArgsConstructor
@Getter
public class DateRange {
    private final int millisecondsInDay = 86400000;
    private Date start;
    private Date end;

    public List<Date> getDaysBetween() {
        List<Date> result = new ArrayList<>();
        Date current = new Date(start.getTime());
        while (current.before(end)) {
            result.add(new Date(current.getTime()));
            current.setTime(current.getTime() + millisecondsInDay);
        }
        result.add(end);
        return result;
    }
}
