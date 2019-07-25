package com.epion_t3.devtools.bean;

import lombok.*;

import java.io.Serializable;

/**
 * プロパティ出力モデル.
 *
 * @author takashno
 */
@Getter
@Setter
public class Property implements Serializable {

    private String key;

    private String value;
}
