package io.github.reconsolidated.zpibackend.domain.storeConfig;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Owner {

    @Id
    @GeneratedValue(generator = "owner_generator")
    private Long ownerId;
    private Long appUserId;
    @Builder.Default
    private String email = "";
    @Builder.Default
    @JsonProperty("logoSrc")
    private String imageUrl = "";
    @Builder.Default
    private String phone = "";
    @Builder.Default
    private String color = "";
    @Builder.Default
    @JsonProperty("name")
    private String storeName = "";
}
