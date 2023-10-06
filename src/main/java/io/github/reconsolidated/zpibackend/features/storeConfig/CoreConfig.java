package io.github.reconsolidated.zpibackend.features.storeConfig;

import org.hibernate.Hibernate;
import lombok.*;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.Objects;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class CoreConfig {
    @Id
    @GeneratedValue(generator = "core_config_generator")
    private Long coreConfigId;
    private Boolean simultaneous;
    private Boolean uniqueness;
    private Boolean flexibility;
    private Boolean granularity;
    private Boolean periodicity;
    private Boolean specificReservation;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CoreConfig that)) {
            return false;
        }
        return Objects.equals(coreConfigId, that.coreConfigId) &&
                Objects.equals(simultaneous, that.simultaneous) &&
                Objects.equals(uniqueness, that.uniqueness) &&
                Objects.equals(flexibility, that.flexibility) &&
                Objects.equals(granularity, that.granularity) &&
                Objects.equals(periodicity, that.periodicity) &&
                Objects.equals(specificReservation, that.specificReservation);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}