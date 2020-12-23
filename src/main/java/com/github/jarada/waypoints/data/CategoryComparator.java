package com.github.jarada.waypoints.data;

import java.util.Comparator;

public class CategoryComparator implements Comparator<Category> {

    @Override
    public int compare(Category o1, Category o2) {
        if (o1.getOrder() != o2.getOrder())
            return Integer.compare(o1.getOrder(), o2.getOrder());
        if (o1.isOrderSet() && !o2.isOrderSet())
            return -1;
        if (o2.isOrderSet() && !o1.isOrderSet())
            return 1;
        return o1.getName().compareTo(o2.getName());
    }
    
}
