package com.epion_t3.dev.tools.messages.generator.bean;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * コマンド出力モデル.
 *
 * @author takashno
 */
@Getter
@Setter
public class CommandOutputModel implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * ID.
     */
    private String id;

    /**
     * アサートコマンド.
     */
    private Boolean assertCommand = false;

    /**
     * エビデンスコマンド.
     */
    private Boolean evidenceCommand = false;

    /**
     * コマンド概要.
     */
    private List<String> summary;

    /**
     * コマンド機能説明.
     */
    private List<String> function;

    private String structure;

    /**
     * コマンド構成詳細.
     */
    private List<String> structureDescription;


    /**
     * 機能説明を追加.
     *
     * @param contents 機能説明
     */
    public void addFunction(String contents) {
        if (function == null) {
            function = new ArrayList<>();
        }
        function.add(contents);
    }

    /**
     * 概要を追加.
     *
     * @param contents 概要説明
     */
    public void addSummary(String contents) {
        if (summary == null) {
            summary = new ArrayList<>();
        }
        summary.add(contents);
    }

    /**
     * 構成詳細を追加.
     *
     * @param contents 構成詳細
     */
    public void addStructureDescription(String contents) {
        if (structureDescription == null) {
            structureDescription = new ArrayList<>();
        }
        structureDescription.add(contents);
    }


}
