package io.github.reconsolidated.zpibackend.domain.parameter;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum ParameterType {
    @JsonProperty("string") STRING,
    @JsonProperty("number") NUMBER,
    @JsonProperty("boolean") BOOLEAN
}
