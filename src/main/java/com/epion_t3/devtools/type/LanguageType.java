/* Copyright (c) 2017-2020 Nozomu Takashima. */
package com.epion_t3.devtools.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum LanguageType {

    JA_JP("a");

    private String locale;
}
