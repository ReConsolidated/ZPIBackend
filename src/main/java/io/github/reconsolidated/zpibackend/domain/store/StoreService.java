package io.github.reconsolidated.zpibackend.domain.store;

import io.github.reconsolidated.zpibackend.domain.appUser.AppUser;
import io.github.reconsolidated.zpibackend.domain.reservation.ReservationService;
import io.github.reconsolidated.zpibackend.domain.reservation.dtos.ReservationDto;
import io.github.reconsolidated.zpibackend.domain.store.dtos.CreateStoreDto;
import io.github.reconsolidated.zpibackend.domain.storeConfig.StoreConfig;
import io.github.reconsolidated.zpibackend.domain.storeConfig.StoreConfigService;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StoreService {
    private final StoreRepository storeRepository;
    private final StoreConfigService storeConfigService;
    @Lazy
    @Autowired
    private ReservationService reservationService;

    public Store getStore(String storeName) {
        return storeRepository.findByStoreName(storeName).orElseThrow();
    }

    public Store createStore(AppUser currentUser, CreateStoreDto dto) {
        StoreConfig storeConfig = storeConfigService.getStoreConfig(dto.getStoreConfigId());
        Store store = new Store(storeConfig);
        return storeRepository.save(store);
    }

    /**
     * @param currentUser
     * @return list of stores that are owned by curren user
     */
    public List<Store> listStores(AppUser currentUser) {
        return storeRepository.findAllByOwnerAppUserId(currentUser.getId());
    }

    /**
     * @return list of all stores in database
     */
    public List<Store> listStores() {
        return storeRepository.findAll();
    }

    public void saveStore(Store store) {
        storeRepository.save(store);
    }

    public void deleteStore(AppUser currentUser, String storeName) {
        Store store = getStore(storeName);
        if (store.getStoreConfig().getOwner() == null || store.getStoreConfig().getOwner().getAppUserId() == null
                || !store.getStoreConfig().getOwner().getAppUserId().equals(currentUser.getId())) {
            throw new IllegalArgumentException("You are not the owner of this Store Config. You cannot edit it.");
        }
        for(ReservationDto reservation : reservationService.getStoreReservations(currentUser, storeName)) {
            reservationService.deleteReservationTotal(currentUser, reservation.getId());
        }
        storeRepository.delete(store);
    }
}
