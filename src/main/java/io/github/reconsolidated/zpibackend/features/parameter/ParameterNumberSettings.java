package io.github.reconsolidated.zpibackend.features.parameter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class ParameterNumberSettings extends ParameterSettings {
    protected String units;
    protected Integer maxValue;
    protected Integer minValue;
}
