package com.epion_t3.dev.tools.messages.generator.bean;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

import java.io.Serializable;

@Builder
@Getter
public class Property implements Serializable {

    @NonNull
    private String key;

    @NonNull
    private String value;
}
