/* Copyright (c) 2017-2020 Nozomu Takashima. */
package com.epion_t3.devtools.exception;

public class GeneratorException extends RuntimeException {

    public GeneratorException(String message) {
        super(message);
    }

    public GeneratorException(String message, Throwable t) {
        super(message, t);
    }

}
