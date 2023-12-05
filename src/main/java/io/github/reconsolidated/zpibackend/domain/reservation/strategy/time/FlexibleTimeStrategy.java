package io.github.reconsolidated.zpibackend.domain.reservation.strategy.time;

import io.github.reconsolidated.zpibackend.domain.reservation.Reservation;
import io.github.reconsolidated.zpibackend.domain.reservation.ReservationType;
import io.github.reconsolidated.zpibackend.domain.reservation.Schedule;
import io.github.reconsolidated.zpibackend.domain.reservation.ScheduleSlot;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public interface FlexibleTimeStrategy {

    boolean verifyScheduleSlots(Schedule schedule, List<ScheduleSlot> scheduleSlots, ScheduleSlot reservationSlot);

    List<ScheduleSlot> prepareSchedule(Schedule schedule, Reservation reservation, List<ScheduleSlot> toReserve);

    default boolean checkSlotsContinuity(List<ScheduleSlot> scheduleSlots) {
        for (int i = 0; i < scheduleSlots.size() - 1; i++) {
            if (!scheduleSlots.get(i).getEndDateTime().equals(scheduleSlots.get(i + 1).getStartDateTime()) &&
                    !(scheduleSlots.get(i).getType() == ReservationType.OVERNIGHT &&
                            scheduleSlots.get(i + 1).getType() == ReservationType.MORNING)) {
                return false;
            }
        }
        return true;
    }

    default List<ScheduleSlot> fillGaps(Schedule schedule, Reservation reservation, List<ScheduleSlot> scheduleSlots) {

        //needed to initialize slots with currentAmount = 0 that were fully reserved and removed
        List<Long> allSubItems = new ArrayList<>();
        for (int i = 0; i < reservation.getItem().getInitialAmount(); i++) {
            allSubItems.add(((long) i));
        }
        if (scheduleSlots.isEmpty()) {
            scheduleSlots.add(new ScheduleSlot(
                    schedule,
                    reservation.getStartDateTime(),
                    reservation.getEndDateTime(),
                    allSubItems.size(),
                    allSubItems));
        }
        //adding starting slot of reservation if it was fully reserved and removed
        if (!scheduleSlots.get(0).getStartDateTime().isEqual(reservation.getStartDateTime())) {
            scheduleSlots.add(0, new ScheduleSlot(
                    schedule,
                    reservation.getStartDateTime(),
                    scheduleSlots.get(0).getStartDateTime(),
                    allSubItems.size(),
                    allSubItems));
        }
        //adding ending slot of reservation if it was fully reserved and removed
        if (!scheduleSlots.get(scheduleSlots.size() - 1).getEndDateTime().isEqual(reservation.getEndDateTime())) {
            scheduleSlots.add(new ScheduleSlot(
                    schedule,
                    scheduleSlots.get(scheduleSlots.size() - 1).getEndDateTime(),
                    reservation.getEndDateTime(),
                    allSubItems.size(),
                    allSubItems));
        }
        //filling all possible gaps in time range of reservation
        ScheduleSlot prev = scheduleSlots.get(0);
        for (int i = 1; i < scheduleSlots.size(); i++) {
            if (!prev.getType().equals(ReservationType.OVERNIGHT) &&
                    scheduleSlots.get(i).getType().equals(ReservationType.MORNING)) {
                scheduleSlots.add(i + 1, new ScheduleSlot(
                        schedule,
                        prev.getEndDateTime(),
                        scheduleSlots.get(i).getEndDateTime(),
                        allSubItems.size(),
                        allSubItems));
            }
            if (!scheduleSlots.get(i).getType().equals(ReservationType.MORNING) &&
                    !prev.getEndDateTime().equals(scheduleSlots.get(i).getStartDateTime())) {
                scheduleSlots.add(i, new ScheduleSlot(
                        schedule,
                        prev.getEndDateTime(),
                        scheduleSlots.get(i).getStartDateTime(),
                        allSubItems.size(),
                        allSubItems));
            }
            prev = scheduleSlots.get(i);
        }
        return scheduleSlots
                .stream()
                .filter(slot -> !slot.getType().equals(ReservationType.MORNING) &&
                        !slot.getType().equals(ReservationType.OVERNIGHT)).collect(Collectors.toList());
    }
}
