package io.github.reconsolidated.zpibackend.domain.comment;

import io.github.reconsolidated.zpibackend.domain.appUser.AppUser;
import io.github.reconsolidated.zpibackend.domain.item.Item;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @ManyToOne(cascade = CascadeType.PERSIST)
    private Item item;
    @ManyToOne(cascade = CascadeType.PERSIST)
    private AppUser user;
    private LocalDateTime dateTime;
    @Column(length = 1000)
    private String content;
    private Double rating;
    private String nickname;
}
