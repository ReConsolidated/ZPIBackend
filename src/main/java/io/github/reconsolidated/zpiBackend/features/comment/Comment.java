package io.github.reconsolidated.zpiBackend.features.comment;

import io.github.reconsolidated.zpiBackend.authentication.appUser.AppUser;
import io.github.reconsolidated.zpiBackend.features.item.Item;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Data
@Builder
@NoArgsConstructor
public class Comment {

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Long id;
    @ManyToOne
    private Item item;
    @ManyToOne
    private AppUser user;
    private LocalDateTime dateTime;
    private String content;
}
