package io.github.reconsolidated.zpibackend.domain.item;

import io.github.reconsolidated.zpibackend.domain.availability.Availability;
import io.github.reconsolidated.zpibackend.domain.item.dtos.SubItemListDto;
import io.github.reconsolidated.zpibackend.domain.item.dtos.ItemDto;
import io.github.reconsolidated.zpibackend.domain.item.dtos.SubItemInfoDto;
import io.github.reconsolidated.zpibackend.domain.parameter.Parameter;
import io.github.reconsolidated.zpibackend.domain.parameter.ParameterSettings;
import io.github.reconsolidated.zpibackend.domain.parameter.ParameterStringSettings;
import io.github.reconsolidated.zpibackend.domain.reservation.ReservationType;
import io.github.reconsolidated.zpibackend.domain.reservation.Schedule;
import io.github.reconsolidated.zpibackend.domain.store.Store;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Item {

    @Id
    @GeneratedValue(generator = "item_generator")
    private Long itemId;
    @JoinColumn(name = "store_id")
    @ManyToOne(cascade = CascadeType.PERSIST)
    private Store store;
    private Boolean active;
    private String title;
    private String subtitle;
    @Column(length = 1000)
    private String description;
    private String image;
    @OnDelete(action = OnDeleteAction.CASCADE)
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private Schedule schedule = new Schedule(this, new ArrayList<>());
    @OnDelete(action = OnDeleteAction.CASCADE)
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private Schedule initialSchedule = new Schedule(this, new ArrayList<>());
    @OrderBy("id ASC")
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "item", orphanRemoval = true)
    private List<Parameter> customAttributeList;
    @Builder.Default
    private Integer amount = 1;
    @Builder.Default
    private Integer initialAmount = 1;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "item", orphanRemoval = true)
    private List<SubItem> subItems;

    public Item(Store store, ItemDto itemDto) {
        this.store = store;
        this.active = itemDto.getActive();
        this.title = itemDto.getAttributes().getTitle();
        this.subtitle = itemDto.getAttributes().getSubtitle();
        this.description = itemDto.getAttributes().getDescription();
        this.image = itemDto.getAttributes().getImage();
        this.amount = itemDto.getAmount() == null ?
                (itemDto.getSubItems() == null ? 1 : itemDto.getSubItems().size()) : itemDto.getAmount();
        this.initialAmount = this.amount;
        for (int i = 0; i < store.getStoreConfig().getCustomAttributesSpec().size(); i++) {
            ParameterSettings curr = store.getStoreConfig().getCustomAttributesSpec().get(i);
            if (curr instanceof ParameterStringSettings currString) {
                if (currString.getLimitValues() &&
                        (currString.getIsRequired() || (itemDto.getCustomAttributeList().get(i).getValue() != null
                                && !Objects.equals(itemDto.getCustomAttributeList().get(i).getValue(), "")))) {
                    int finalI = i;
                    if (currString.getPossibleValues()
                            .stream()
                            .noneMatch(value -> value.equals(itemDto.getCustomAttributeList().get(finalI).getValue()))) {
                        throw new IllegalArgumentException("Parameter " + curr.getName()
                                + " doesn't have one of possible values");
                    }
                }
            }
        }
        this.customAttributeList = itemDto.getCustomAttributeList()
                .stream()
                .map((p) -> new Parameter(p, this))
                .collect(Collectors.toList());
        if (store.getStoreConfig().getCore().getFlexibility()) {
            this.schedule = new Schedule(this, itemDto.getSchedule().getScheduledRanges());
            this.initialSchedule = new Schedule(this, itemDto.getSchedule().getScheduledRanges());
            initialSchedule.setAvailableScheduleSlots(initialSchedule
                            .getAvailableScheduleSlots()
                            .stream()
                            .filter(scheduleSlot ->
                            scheduleSlot.getType() != ReservationType.MORNING &&
                                    scheduleSlot.getType() != ReservationType.OVERNIGHT)
                            .toList());
        } else {
            this.schedule = new Schedule(this,
                    List.of(new Availability(
                            itemDto.getSchedule().getStartDateTime(),
                            itemDto.getSchedule().getEndDateTime() == null ?
                                    itemDto.getSchedule().getStartDateTime() :
                                    itemDto.getSchedule().getEndDateTime(),
                            ReservationType.NONE)));
            this.initialSchedule = new Schedule(this,
                    List.of(new Availability(
                            itemDto.getSchedule().getStartDateTime(),
                            itemDto.getSchedule().getEndDateTime() == null ?
                                    itemDto.getSchedule().getStartDateTime() :
                                    itemDto.getSchedule().getEndDateTime(),
                            ReservationType.NONE)));
            this.initialSchedule.setAvailableScheduleSlots(
                    initialSchedule.getAvailableScheduleSlots());
        }
        this.subItems = itemDto.getSubItems() == null ?
                new ArrayList<>() :
                itemDto.getSubItems().stream().map(subItemDto -> new SubItem(subItemDto, this)).toList();
    }

    public SubItemListDto getSubItemsListDto() {
        ArrayList<SubItemInfoDto> subItemsDto = new ArrayList<>();
        for (SubItem subItem : subItems) {
            subItemsDto.add(subItem.toSubItemInfoDto());
        }
        return new SubItemListDto(subItemsDto);
    }

    public void setAvailableSchedule(List<Availability> availabilities) {
        this.schedule = new Schedule(this, availabilities);
    }

    public SubItemInfoDto toSubItemDto() {
        return new SubItemInfoDto(itemId, title, subtitle);
    }

    public boolean isFixedPast() {
        if (store.getStoreConfig().getCore().getFlexibility()) {
            return false;
        }
        if (store.getStoreConfig().getCore().getPeriodicity()) {
            return subItems.stream().noneMatch(subItem -> subItem.getStartDateTime().isAfter(LocalDateTime.now()));
        } else {
            return schedule != null &&
                    !schedule.getAvailableScheduleSlots().isEmpty() &&
                    schedule.getAvailableScheduleSlots().get(0).getEndDateTime().isBefore(LocalDateTime.now());
        }
    }
}
