/* Copyright (c) 2017-2020 Nozomu Takashima. */
package com.epion_t3.devtools.comparator;

import com.epion_t3.core.common.bean.spec.Description;
import com.epion_t3.core.common.bean.spec.Function;

import java.util.Comparator;

/**
 * 詳細情報を並び替えるためのComparator.
 */
public class DescriptionComparator implements Comparator<Description> {

    private static final DescriptionComparator instance = new DescriptionComparator();

    private DescriptionComparator() {
        // Do Nothing...
    }

    public static DescriptionComparator getInstance() {
        return instance;
    }

    @Override
    public int compare(Description o1, Description o2) {
        return o1.getOrder().compareTo(o2.getOrder());
    }

}
