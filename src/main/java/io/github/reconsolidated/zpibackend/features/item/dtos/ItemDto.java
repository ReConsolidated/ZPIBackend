package io.github.reconsolidated.zpibackend.features.item.dtos;

import io.github.reconsolidated.zpibackend.features.availability.Availability;
import io.github.reconsolidated.zpibackend.features.item.Item;
import io.github.reconsolidated.zpibackend.features.item.SubItem;
import io.github.reconsolidated.zpibackend.features.parameter.Parameter;
import io.github.reconsolidated.zpibackend.features.reservation.ScheduleSlot;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ItemDto {
    private Long id;
    private Boolean active;
    private String title;
    private String subtitle;
    private String description;
    private String image;
    private List<Parameter> customAttributeList;
    private List<SubItem> subItems;
    private List<Availability> availabilities;
    private Integer capacity;
    private Long availableAmount;
    private Double mark;
    private LocalDateTime earliestStart;
    private LocalDateTime latestEnd;

    public ItemDto(Item item) {
        this.id = item.getItemId();
        this.active = item.getActive();
        this.title = item.getTitle();
        this.subtitle = item.getSubtitle();
        this.description = item.getDescription();
        this.image = item.getImage();
        this.customAttributeList = item.getCustomAttributeList();
        this.capacity = item.getAmount();
        this.availableAmount = Long.valueOf(item.getAmount());
        this.mark = 3.0;
        this.subItems = item.getSubItems();
        this.availabilities = item.getSchedule().getAvailabilities();

        for (ScheduleSlot slot : item.getSchedule().getAvailableScheduleSlots()) {
            if (earliestStart == null ||
                    isEarlier(slot.getStartDateTime(), earliestStart)) {
                earliestStart = slot.getStartDateTime();
            }
            if (latestEnd == null ||
                    isEarlier(latestEnd, slot.getEndDateTime())) {
                latestEnd = slot.getEndDateTime();
            }
        }
    }

    private boolean isEarlier(LocalDateTime one, LocalDateTime other) {
        if (one.getHour() < other.getHour()) {
            return true;
        }
        if (one.getHour() > other.getHour()) {
            return false;
        }
        return one.getMinute() < other.getMinute();
    }
}
