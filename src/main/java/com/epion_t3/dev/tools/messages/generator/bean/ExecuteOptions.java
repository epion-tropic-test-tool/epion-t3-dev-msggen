package com.epion_t3.dev.tools.messages.generator.bean;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class ExecuteOptions implements Serializable {

    private static final long serialVersionUID = 1L;

    private String target;

    private String messageOutput;

    private String javaOutput;

    private String docOutput;

}
