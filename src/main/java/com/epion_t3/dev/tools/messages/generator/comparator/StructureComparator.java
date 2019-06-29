package com.epion_t3.dev.tools.messages.generator.comparator;

import com.epion_t3.core.common.bean.spec.Structure;

import java.util.Comparator;

public class StructureComparator implements Comparator<Structure> {

    private static final StructureComparator instance = new StructureComparator();

    private StructureComparator() {
        // Do Nothing...
    }

    public static StructureComparator getInstance() {
        return instance;
    }

    @Override
    public int compare(Structure o1, Structure o2) {
        return o1.getOrder().compareTo(o2.getOrder());
    }
}
