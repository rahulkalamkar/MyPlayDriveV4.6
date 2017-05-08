package com.hungama.myplay.activity.communication;

import java.util.Comparator;

public class PriorityElementComparator< T extends PriorityElement > implements Comparator< T > {
    @Override
    public int compare( T lhs, T rhs ) {
        return rhs.getPriority( ) - lhs.getPriority( );
    }
}
