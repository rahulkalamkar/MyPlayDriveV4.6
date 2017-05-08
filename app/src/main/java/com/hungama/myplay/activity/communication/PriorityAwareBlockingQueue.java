package com.hungama.myplay.activity.communication;

import java.util.concurrent.TimeUnit;

public class PriorityAwareBlockingQueue< E > extends PriorityBlockingQueue< E > {
    @Override
    public boolean offer( E e ) {
        if ( e instanceof PriorityElement ) {
            PriorityElement pe = (PriorityElement) e;
            if ( pe.getPriority( ) < 0 ) {
                return false;
            }
        }
        return super.offer( e );
    }
    
    @Override
    public boolean add( E e ) {
        if ( e instanceof PriorityElement ) {
            PriorityElement pe = (PriorityElement) e;
            if ( pe.getPriority( ) < 0 ) {
                throw new IllegalStateException( "Immediate mode element detected" );
            }
        }
        return super.add( e );
    }
    
    @Override
    public void put( E e ) throws InterruptedException {
        super.put( e );
    }
    
    @Override
    public boolean offer( E e, long timeout, TimeUnit unit ) throws InterruptedException {
        return super.offer( e, timeout, unit );
    }
}
