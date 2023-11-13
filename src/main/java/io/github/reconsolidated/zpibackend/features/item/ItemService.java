package io.github.reconsolidated.zpibackend.features.item;

import io.github.reconsolidated.zpibackend.authentication.appUser.AppUser;
import io.github.reconsolidated.zpibackend.features.item.dtos.ItemDto;
import io.github.reconsolidated.zpibackend.features.item.dtos.ItemListDto;
import io.github.reconsolidated.zpibackend.features.item.dtos.ItemStatus;
import io.github.reconsolidated.zpibackend.features.parameter.ParameterRepository;
import io.github.reconsolidated.zpibackend.features.store.Store;
import io.github.reconsolidated.zpibackend.features.store.StoreService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ItemService {
    private final ItemRepository itemRepository;
    private final ParameterRepository parameterRepository;
    private final StoreService storeService;

    public Item getItem(Long itemId) {
        return itemRepository.findById(itemId).orElseThrow();
    }

    public ItemListDto getItems(AppUser currentUser, Long storeId) {
        Store store = storeService.getStore(currentUser, storeId);
        if (!store.getStoreConfig().getOwner().getAppUserId().equals(currentUser.getId())) {
            throw new RuntimeException("You are not the owner of this store");
        }
        return new ItemListDto(itemRepository
                .findAllByStore_Id(storeId)
                .stream()
                .map((item) -> new ItemDto(item, getItemStatus(item.getItemId())))
                .collect(Collectors.toList()));
    }

    public Item createItem(AppUser currentUser, Long storeId, ItemDto itemDto) {
        Store store = storeService.getStore(currentUser, storeId);
        if (!store.getStoreConfig().getOwner().getAppUserId().equals(currentUser.getId())) {
            throw new RuntimeException("You are not the owner of this store");
        }
        Item item = new Item(store, itemDto);
        item = itemRepository.save(item);
        parameterRepository.saveAll(item.getCustomAttributeList());
        return item;
    }

    public ItemStatus getItemStatus(Long itemId) {
        Item item = getItem(itemId);
        return new ItemStatus();
    }

    public ItemDto updateItem(AppUser currentUser, Long itemId, ItemDto itemDto) {
        Item item = getItem(itemId);
        Store store = item.getStore();
        if (!store.getStoreConfig().getOwner().getAppUserId().equals(currentUser.getId())) {
            throw new RuntimeException("You are not the owner of this store");
        }
        Item newItem = new Item(store, itemDto);
        return new ItemDto(itemRepository.save(newItem), getItemStatus(item.getItemId()));
    }

    public ItemDto activateItem(AppUser currentUser, Long itemId) {
        Item item = getItem(itemId);
        Store store = item.getStore();
        if (!store.getStoreConfig().getOwner().getAppUserId().equals(currentUser.getId())) {
            throw new RuntimeException("You are not the owner of this store");
        }
        item.setActive(true);
        return new ItemDto(itemRepository.save(item), getItemStatus(item.getItemId()));
    }

    public ItemDto deactivateItem(AppUser currentUser, Long itemId) {
        Item item = getItem(itemId);
        Store store = item.getStore();
        if (!store.getStoreConfig().getOwner().getAppUserId().equals(currentUser.getId())) {
            throw new RuntimeException("You are not the owner of this store");
        }
        item.setActive(false);
        return new ItemDto(itemRepository.save(item), getItemStatus(item.getItemId()));
    }

    public void deleteItem(AppUser currentUser, Long itemId) {
        Item item = getItem(itemId);
        Store store = item.getStore();
        if (!store.getStoreConfig().getOwner().getAppUserId().equals(currentUser.getId())) {
            throw new RuntimeException("You are not the owner of this store");
        }
        if (!item.getReservations().isEmpty()) {
            throw new RuntimeException("Can't delete item with reservations");
        }
        itemRepository.deleteById(itemId);
    }
}
