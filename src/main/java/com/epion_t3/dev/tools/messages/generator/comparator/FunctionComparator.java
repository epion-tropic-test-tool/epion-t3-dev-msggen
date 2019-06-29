package com.epion_t3.dev.tools.messages.generator.comparator;

import com.epion_t3.core.common.bean.spec.Function;

import java.util.Comparator;

public class FunctionComparator implements Comparator<Function> {

    private static final FunctionComparator instance = new FunctionComparator();

    private FunctionComparator() {
        // Do Nothing...
    }

    public static FunctionComparator getInstance() {
        return instance;
    }

    @Override
    public int compare(Function o1, Function o2) {
        return o1.getOrder().compareTo(o2.getOrder());
    }

}
