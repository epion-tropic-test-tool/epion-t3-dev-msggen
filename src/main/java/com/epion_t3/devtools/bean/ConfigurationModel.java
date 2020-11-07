/* Copyright (c) 2017-2020 Nozomu Takashima. */
package com.epion_t3.devtools.bean;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 設定定義出力モデル.
 *
 * @author Nozomu Takashima
 */
@Getter
@Setter
public class ConfigurationModel implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * ID.
     */
    private String id;

    /**
     * コマンド概要.
     */
    private List<String> summaries;

    /**
     * コマンド機能説明.
     */
    private List<String> descriptions;

    /**
     * YAML構成.
     */
    private String structure;

    /**
     * コマンド構成詳細.
     */
    private List<String> structureDescriptions;

    /**
     * 機能説明を追加.
     *
     * @param contents 機能説明
     */
    public void addDescription(String contents) {
        if (descriptions == null) {
            descriptions = new ArrayList<>();
        }
        descriptions.add(contents);
    }

    /**
     * 概要を追加.
     *
     * @param contents 概要説明
     */
    public void addSummary(String contents) {
        if (summaries == null) {
            summaries = new ArrayList<>();
        }
        summaries.add(contents);
    }

    /**
     * 構成詳細を追加.
     *
     * @param contents 構成詳細
     */
    public void addStructureDescription(String contents) {
        if (structureDescriptions == null) {
            structureDescriptions = new ArrayList<>();
        }
        structureDescriptions.add(contents);
    }

}
