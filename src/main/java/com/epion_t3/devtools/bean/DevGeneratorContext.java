/* Copyright (c) 2017-2020 Nozomu Takashima. */
package com.epion_t3.devtools.bean;

import com.epion_t3.core.common.bean.spec.ET3Spec;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class DevGeneratorContext implements Serializable {

    /**
     * 実行オプション.
     */
    private ExecuteOptions executeOptions;

    /**
     * Locale毎に分割した機能モデル.
     */
    private Map<String, FunctionModel> functionModelMap = new HashMap<>();

    /**
     * 機能設計情報.
     */
    private ET3Spec spec;

}
