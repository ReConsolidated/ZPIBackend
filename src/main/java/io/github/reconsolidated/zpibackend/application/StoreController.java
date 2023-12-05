package io.github.reconsolidated.zpibackend.application;

import io.github.reconsolidated.zpibackend.domain.appUser.AppUser;
import io.github.reconsolidated.zpibackend.domain.store.Store;
import io.github.reconsolidated.zpibackend.infrastracture.currentUser.CurrentUser;
import io.github.reconsolidated.zpibackend.domain.store.StoreService;
import io.github.reconsolidated.zpibackend.domain.store.dtos.StoreNameDto;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping(value = "/stores", produces = MediaType.APPLICATION_JSON_VALUE)
@Validated
public class StoreController {
    private final StoreService storeService;


    @GetMapping("/{storeName}")
    public ResponseEntity<?> getStore(@PathVariable String storeName) {
        return ResponseEntity.ok(storeService.getStore(storeName));
    }

    @GetMapping("/all")
    public ResponseEntity<List<StoreNameDto>> listStores() {
        return ResponseEntity.ok(storeService.listStores()
                .stream()
                .sorted(Comparator.comparingLong(Store::getId).reversed())
                .map(store -> new StoreNameDto(store.getStoreConfig()))
                .toList());
    }

    @GetMapping
    public ResponseEntity<List<StoreNameDto>> listAllStores(@CurrentUser AppUser currentUser) {
        return ResponseEntity.ok(storeService.listStores(currentUser)
                .stream()
                .sorted(Comparator.comparingLong(Store::getId).reversed())
                .map(store -> new StoreNameDto(store.getStoreConfig()))
                .toList());
    }

    @DeleteMapping("/{storeName}")
    public ResponseEntity<?> deleteStore(@CurrentUser AppUser currentUser, @PathVariable String storeName) {
        storeService.deleteStore(currentUser, storeName);
        return ResponseEntity.ok(true);
    }
}
