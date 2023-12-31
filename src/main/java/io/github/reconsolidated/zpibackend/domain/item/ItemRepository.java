package io.github.reconsolidated.zpibackend.domain.item;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {
    List<Item> findAllByStore_Id(Long storeId);

    Optional<Item> findByStoreStoreNameAndItemId(String storeName, Long itemId);

}
