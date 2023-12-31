package io.github.reconsolidated.zpibackend.domain.storeConfig.dtos;

import io.github.reconsolidated.zpibackend.domain.storeConfig.Owner;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OwnerDto {
    private Long ownerId;
    private String name;
    private String logoSrc;
    private String phone;
    private String email;
    private String color;
    private String storeName;

    public OwnerDto(Owner owner) {
        this.ownerId = owner.getOwnerId();
        this.name = owner.getStoreName();
        this.logoSrc = owner.getImageUrl();
        this.phone = owner.getPhone();
        this.email = owner.getEmail();
        this.color = owner.getColor();
        this.storeName = owner.getStoreName().replaceAll("[ /]", "_");
    }

    public Owner toOwner(Long ownerUserId) {
        return Owner.builder()
                .ownerId(ownerId)
                .appUserId(ownerUserId)
                .storeName(name)
                .imageUrl(logoSrc)
                .phone(phone)
                .email(email)
                .color(color)
                .build();
    }
}
