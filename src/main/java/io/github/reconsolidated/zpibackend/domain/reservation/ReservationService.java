package io.github.reconsolidated.zpibackend.domain.reservation;

import io.github.reconsolidated.zpibackend.domain.appUser.AppUser;
import io.github.reconsolidated.zpibackend.domain.appUser.AppUserService;
import io.github.reconsolidated.zpibackend.domain.availability.Availability;
import io.github.reconsolidated.zpibackend.domain.item.Item;
import io.github.reconsolidated.zpibackend.domain.item.ItemService;
import io.github.reconsolidated.zpibackend.domain.item.SubItem;
import io.github.reconsolidated.zpibackend.domain.reservation.dtos.ReservationDto;
import io.github.reconsolidated.zpibackend.domain.reservation.request.CheckAvailabilityRequestUnique;
import io.github.reconsolidated.zpibackend.domain.reservation.response.*;
import io.github.reconsolidated.zpibackend.domain.reservation.dtos.UserReservationDto;
import io.github.reconsolidated.zpibackend.domain.reservation.request.CheckAvailabilityRequest;
import io.github.reconsolidated.zpibackend.domain.store.Store;
import io.github.reconsolidated.zpibackend.domain.store.StoreService;
import io.github.reconsolidated.zpibackend.domain.storeConfig.CoreConfig;

import lombok.AllArgsConstructor;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
@AllArgsConstructor
public class ReservationService {

    private ReservationRepository reservationRepository;
    private StoreService storeService;
    private ItemService itemService;
    private AppUserService appUserService;

    public ReservationDto reserveItem(AppUser appUser, String storeName, ReservationDto reservationDto) {
        Item item = itemService.getItemFromStore(reservationDto.getItemId(), storeName);

        CoreConfig core = item.getStore().getStoreConfig().getCore();

        Optional<AppUser> appUserOptional = appUserService.getUserByEmail(reservationDto.getUserEmail());
        if (appUserOptional.isPresent()) {
            appUser = appUserOptional.get();
        }

        List<String> personalData = new ArrayList<>();
        for (String data : item.getStore().getStoreConfig().getAuthConfig().getRequiredPersonalData()) {
            personalData.add(reservationDto.getPersonalData().getOrDefault(data, ""));
        }

        if (core.getFlexibility()) {
            //reservations with schedule
            Schedule schedule = item.getSchedule();
            ScheduleSlot requestSlot = new ScheduleSlot(schedule, reservationDto.getStartDateTime(), reservationDto.getEndDateTime(),
                    reservationDto.getAmount());
            if (!schedule.verify(core, requestSlot)) {
                throw new IllegalArgumentException("Right slot is not available. Reservation is not possible!");
            }

            Reservation reservation = Reservation.builder()
                    .user(appUser)
                    .email(reservationDto.getUserEmail())
                    .item(item)
                    .subItemIdList(new ArrayList<>())
                    .personalData(personalData)
                    .startDateTime(reservationDto.getStartDateTime())
                    .endDateTime(reservationDto.getEndDateTime())
                    .amount(reservationDto.getAmount())
                    .message(reservationDto.getMessage())
                    .confirmed(!item.getStore().getStoreConfig().getAuthConfig().getConfirmationRequired())
                    .build();
            reservation.setStatus(LocalDateTime.now());
            if (!schedule.processReservation(core, reservation)) {
                throw new IllegalArgumentException("Unexpected error. Reservation is not possible!");
            }
            return new ReservationDto(reservationRepository.save(reservation));
        } else {
            if (core.getPeriodicity() || core.getSpecificReservation()) {
                //reservations with sub items
                ArrayList<SubItem> toReserve = new ArrayList<>();
                for (SubItem subItem : item.getSubItems()) {
                    for (Long subItemId: reservationDto.getSubItemIds()) {
                        if (subItem.getSubItemId().equals(subItemId)) {
                            toReserve.add(subItem);
                        }
                    }
                }
                for (SubItem subItem : toReserve) {
                    if (subItem.getAmount() >= reservationDto.getAmount()) {
                        subItem.setAmount(subItem.getAmount() - reservationDto.getAmount());
                    } else {
                        throw new IllegalArgumentException("This item has insufficient amount!");
                    }
                }
                Reservation reservation = Reservation.builder()
                        .user(appUser)
                        .email(reservationDto.getUserEmail())
                        .personalData(personalData)
                        .item(item)
                        .startDateTime(toReserve.get(0).getStartDateTime())
                        .endDateTime(toReserve.get(0).getEndDateTime())
                        .subItemIdList(reservationDto.getSubItemIds())
                        .amount(reservationDto.getAmount())
                        .message(reservationDto.getMessage())
                        .confirmed(!item.getStore().getStoreConfig().getAuthConfig().getConfirmationRequired())
                        .build();
                reservation.setStatus(LocalDateTime.now());
                return new ReservationDto(reservationRepository.save(reservation));
            } else {
                //simple reservations IDK if it will be useful
                if (item.getAmount() < reservationDto.getAmount()) {
                    throw new IllegalArgumentException();
                }
                item.setAmount(item.getAmount() - reservationDto.getAmount());
                Reservation reservation = Reservation.builder()
                        .user(appUser)
                        .email(reservationDto.getUserEmail())
                        .personalData(personalData)
                        .item(item)
                        .startDateTime(item.getSchedule().getAvailabilities().get(0).getStartDateTime())
                        .endDateTime(item.getSchedule().getAvailabilities().get(0).getEndDateTime())
                        .subItemIdList(new LinkedList<>())
                        .amount(reservationDto.getAmount())
                        .message(reservationDto.getMessage())
                        .confirmed(false)
                        .build();
                reservation.setStatus(LocalDateTime.now());
                return new ReservationDto(reservationRepository.save(reservation));
            }
        }
    }

    public List<CheckAvailabilityResponse> checkAvailabilityNotUnique(CheckAvailabilityRequest request) {

        Item item = itemService.getItem(request.getItemId());
        Schedule schedule = item.getSchedule();
        CoreConfig core = item.getStore().getStoreConfig().getCore();

        if (core.getUniqueness()) {
            throw new IllegalArgumentException("For core with unique items use \"/refetch\" endpoint!");
        }

        ScheduleSlot requestSlot = new ScheduleSlot(schedule, request.getStartDate(), request.getEndDate(), request.getAmount());
        if (schedule.verify(core, requestSlot)) {
            return Collections.singletonList(CheckAvailabilityResponseSuccess.builder()
                    .itemId(request.getItemId())
                    .amount(request.getAmount())
                    .startDate(request.getStartDate())
                    .endDate(request.getEndDate())
                    .build());
        } else {
            List<ScheduleSlot> suggestions = schedule.suggest(core, requestSlot);
            if (suggestions.isEmpty()) {
                return Collections.singletonList(CheckAvailabilityResponseFailure.builder()
                        .itemId(item.getItemId())
                        .amount(request.getAmount())
                        .build());
            } else {
                List<CheckAvailabilityResponse> result = new ArrayList<>();
                for (int i = 0; i < (Math.min(suggestions.size(), 3)); i++) {
                    result.add(new CheckAvailabilityResponseSuggestion(
                            i,
                            item.getItemId(),
                            suggestions.get(i),
                            item.getSchedule().getAvailabilitiesForSubItems(suggestions.get(i).getAvailableItemsIndexes())));
                }
                return result;
            }
        }
    }

    public CheckAvailabilityResponseUnique checkAvailabilityUnique(CheckAvailabilityRequestUnique request) {

        Item item = itemService.getItem(request.getItemId());
        return new CheckAvailabilityResponseUnique(
                item.getItemId(),
                request.getAmount(),
                item.getSchedule().getAvailableScheduleSlots()
                        .stream()
                        .filter(scheduleSlot -> scheduleSlot.getCurrAmount() >= request.getAmount())
                        .map(Availability::new)
                        .toList());
    }

    public List<Reservation> getUserReservations(Long currentUserId, String storeName) {
        return reservationRepository.findByUser_IdAndItemStoreStoreName(currentUserId, storeName)
                .stream().peek((reservation -> reservation.setStatus(LocalDateTime.now()))).toList();
    }

    public List<UserReservationDto> getUserReservationsDto(Long currentUserId, String storeName) {

        return getUserReservations(currentUserId, storeName)
                .stream()
                .peek((reservation -> reservation.setStatus(LocalDateTime.now())))
                .map(reservation -> new UserReservationDto(reservation,
                                reservation.getItem()
                                        .getSubItemsListDto()
                                        .subItems()
                                        .stream()
                                        .filter(subItemDto -> reservation.getSubItemIdList().contains(subItemDto.getId()))
                                        .toList()
                    )
                ).toList();
    }

    public List<ReservationDto> getStoreReservations(AppUser currentUser, String storeName) {
        if (!currentUser.getId().equals(storeService.getStore(storeName).getOwnerAppUserId())) {
            throw new IllegalArgumentException("Only owner can get all reservations");
        }

        return reservationRepository.findByItemStoreStoreName(storeName)
                .stream()
                .peek((reservation -> reservation.setStatus(LocalDateTime.now())))
                .map(ReservationDto::new)
                .toList();
    }

    public List<Reservation> getItemReservations(Long itemId) {
        return reservationRepository.findByItemItemId(itemId)
                .stream()
                .peek((reservation -> reservation.setStatus(LocalDateTime.now())))
                .toList();
    }

    public void deletePastReservation(AppUser appUser, Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId).orElseThrow();
        reservationRepository.delete(reservation);
    }

    public void deleteReservationTotal(AppUser appUser, Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId).orElseThrow();
        Item item = reservation.getItem();
        if (!appUser.getId().equals(item.getStore().getOwnerAppUserId())) {
            throw new IllegalArgumentException("Only owners of store can delete!");
        }
        reservationRepository.delete(reservation);
    }

    public void deleteReservation(AppUser appUser, Long reservationId) {

        Reservation reservation = reservationRepository.findById(reservationId).orElseThrow();
        Item item = reservation.getItem();
        if (!appUser.getId().equals(item.getStore().getOwnerAppUserId()) && (reservation.getUser() == null ||
                !appUser.getId().equals(reservation.getUser().getId()))) {
            throw new IllegalArgumentException("Only owners of store or reservation cen cancel it!");
        }
        Schedule schedule = item.getSchedule();
        CoreConfig core = item.getStore().getStoreConfig().getCore();

        if (core.getFlexibility()) {
            schedule.processReservationRemoval(core, reservation);
        } else {
            if (core.getPeriodicity() || core.getSpecificReservation()) {
                ArrayList<SubItem> toReserve = new ArrayList<>();
                for (SubItem subItem : item.getSubItems()) {
                    for (Long subItemId: reservation.getSubItemIdList()) {
                        if (subItem.getSubItemId().equals(subItemId)) {
                            toReserve.add(subItem);
                        }
                    }
                }
                for (SubItem subItem : toReserve) {
                    subItem.setAmount(subItem.getAmount() + reservation.getAmount());
                }
            } else {
                item.setAmount(item.getAmount() + reservation.getAmount());
            }
        }
        reservation.setStatus(appUser.getId().equals(reservation.getUser().getId()) ?
                ReservationStatus.CANCELLED_BY_USER : ReservationStatus.CANCELLED_BY_ADMIN);
        reservationRepository.save(reservation);
    }

    public boolean confirm(AppUser currentUser, String storeName, Long reservationId) {
        Store store = storeService.getStore(storeName);
        if (!currentUser.getId().equals(store.getOwnerAppUserId())) {
            throw new IllegalArgumentException("Only owners of store can confirm reservations!");
        }
        Reservation toConfirm = reservationRepository.findById(reservationId).orElseThrow();
        toConfirm.setConfirmed(true);
        reservationRepository.save(toConfirm);
        return true;
    }
}
