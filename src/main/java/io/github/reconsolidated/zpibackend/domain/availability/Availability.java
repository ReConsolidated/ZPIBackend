package io.github.reconsolidated.zpibackend.domain.availability;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.github.reconsolidated.zpibackend.domain.reservation.ReservationType;
import io.github.reconsolidated.zpibackend.domain.reservation.ScheduleSlot;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Availability {
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime startDateTime;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime endDateTime;
    private ReservationType type;

    public Availability(ScheduleSlot scheduleSlot) {
        this.startDateTime = scheduleSlot.getStartDateTime();
        this.endDateTime = scheduleSlot.getEndDateTime();
        this.type = scheduleSlot.getType();
    }

    public boolean overlap(Availability slot) {
        return startDateTime.isBefore(slot.endDateTime) && endDateTime.isAfter(slot.startDateTime);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Availability that)) {
            return false;
        }
        if (!startDateTime.equals(that.startDateTime)) {
            return false;
        }
        return endDateTime.equals(that.endDateTime);
    }

    @Override
    public int hashCode() {
        int result = startDateTime.hashCode();
        result = 31 * result + endDateTime.hashCode();
        result = 31 * result + type.hashCode();
        return result;
    }
}
