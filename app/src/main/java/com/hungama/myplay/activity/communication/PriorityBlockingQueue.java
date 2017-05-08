package com.hungama.myplay.activity.communication;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.SortedSet;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class PriorityBlockingQueue< E > implements BlockingQueue< E > {
    private static final int DEFAULT_CAPACITY = 11;
    
    protected final ReentrantLock mLock = new ReentrantLock( );
    protected final Condition mCondition = mLock.newCondition( );
    
    protected final PriorityQueue< E > mInternalQueue;
    
    public PriorityBlockingQueue( ) {
        mInternalQueue = new PriorityQueue< E >( );
    }
    
    public PriorityBlockingQueue( Collection< ? extends E > c ) {
        mInternalQueue = new PriorityQueue< E >( c );
    }
    
    public PriorityBlockingQueue( Comparator< ? super E > comparator ) {
        mInternalQueue = new PriorityQueue< E >( DEFAULT_CAPACITY, comparator );
    }
    
    public PriorityBlockingQueue( int initialCapacity, Comparator< ? super E > comparator ) {
        mInternalQueue = new PriorityQueue< E >( initialCapacity, comparator );
    }
    
    public PriorityBlockingQueue( int initialCapacity ) {
        mInternalQueue = new PriorityQueue< E >( initialCapacity );
    }
    
    public PriorityBlockingQueue( PriorityQueue< ? extends E > c ) {
        mInternalQueue = new PriorityQueue< E >( c );
    }
    
    public PriorityBlockingQueue( SortedSet< ? extends E > c ) {
        mInternalQueue = new PriorityQueue< E >( c );
    }
    
    @Override
    public boolean offer( E e ) {
        mLock.lock( );
        try {
            boolean result = mInternalQueue.offer( e );
            mCondition.signal( );
            return result;
        } finally {
            mLock.unlock( );
        }
    }
    
    @Override
    public boolean add( E e ) {
        mLock.lock( );
        try {
            boolean result = mInternalQueue.add( e );
            mCondition.signal( );
            return result;
        } finally {
            mLock.unlock( );
        }
    }
    
    @Override
    public void put( E e ) throws InterruptedException {
        mLock.lockInterruptibly( );
        try {
            mInternalQueue.offer( e );
            mCondition.signal( );
        } finally {
            mLock.unlock( );
        }
    }
    
    @Override
    public boolean offer( E e, long timeout, TimeUnit unit ) throws InterruptedException {
        mLock.lockInterruptibly( );
        try {
            boolean result = mInternalQueue.offer( e );
            mCondition.signal( );
            return result;
        } finally {
            mLock.unlock( );
        }
    }
    
    @Override
    public E poll( ) {
        mLock.lock( );
        try {
            return mInternalQueue.poll( );
        } finally {
            mLock.unlock( );
        }
    }
    
    @Override
    public E remove( ) {
        mLock.lock( );
        try {
            return mInternalQueue.remove( );
        } finally {
            mLock.unlock( );
        }
    }
    
    @Override
    public E take( ) throws InterruptedException {
        mLock.lockInterruptibly( );
        try {
            while ( mInternalQueue.isEmpty( ) ) {
                mCondition.await( );
            }
            return mInternalQueue.poll( );
        } finally {
            mLock.unlock( );
        }
    }
    
    @Override
    public E poll( long timeout, TimeUnit unit ) throws InterruptedException {
        long nanos = unit.toNanos( timeout );
        mLock.lock( );
        try {
            while ( mInternalQueue.isEmpty( ) && nanos > 0 ) {
                nanos = mCondition.awaitNanos( nanos );
            }
            return mInternalQueue.poll( );
        } finally {
            mLock.unlock( );
        }
    }
    
    @Override
    public E peek( ) {
        mLock.lock( );
        try {
            return mInternalQueue.peek( );
        } finally {
            mLock.unlock( );
        }
    }
    
    @Override
    public E element( ) {
        mLock.lock( );
        try {
            return mInternalQueue.element( );
        } finally {
            mLock.unlock( );
        }
    }
    
    @Override
    public boolean isEmpty( ) {
        mLock.lock( );
        try {
            return mInternalQueue.isEmpty( );
        } finally {
            mLock.unlock( );
        }
    }
    
    @Override
    public int size( ) {
        mLock.lock( );
        try {
            return mInternalQueue.size( );
        } finally {
            mLock.unlock( );
        }
    }
    
    @Override
    public int remainingCapacity( ) {
        return Integer.MAX_VALUE;
    }
    
    @Override
    public boolean contains( Object o ) {
        mLock.lock( );
        try {
            return mInternalQueue.contains( o );
        } finally {
            mLock.unlock( );
        }
    }
    
    @Override
    public boolean remove( Object o ) {
        mLock.lock( );
        try {
            return mInternalQueue.remove( o );
        } finally {
            mLock.unlock( );
        }
    }
    
    @Override
    public void clear( ) {
        mLock.lock( );
        try {
            mInternalQueue.clear( );
        } finally {
            mLock.unlock( );
        }
    }
    
    @Override
    public boolean containsAll( Collection< ? > collection ) {
        mLock.lock( );
        try {
            for ( Object object : collection ) {
                if ( !mInternalQueue.contains( object ) ) {
                    return false;
                }
            }
            return true;
        } finally {
            mLock.unlock( );
        }
    }
    
    @Override
    public boolean addAll( Collection< ? extends E > collection ) {
        boolean modified = false;
        for ( E e : collection ) {
            modified |= offer( e );
        }
        return modified;
    }
    
    @Override
    public boolean removeAll( Collection< ? > collection ) {
        boolean modified = false;
        for ( Object object : collection ) {
            modified |= remove( object );
        }
        return modified;
    }
    
    @Override
    public boolean retainAll( Collection< ? > collection ) {
        boolean modified = false;
        Iterator< E > iterator = iterator( );
        while ( iterator.hasNext( ) ) {
            E e = (E) iterator.next( );
            boolean present = collection.contains( e );
            if ( !present ) {
                iterator.remove( );
                modified = true;
            }
        }
        return modified;
    }
    
    @Override
    public int drainTo( Collection< ? super E > c ) {
        return drainTo( c, Integer.MAX_VALUE );
    }
    
    @Override
    public int drainTo( Collection< ? super E > c, int maxElements ) {
        if ( c == null ) {
            throw new NullPointerException( "Collection cannot be null" );
        } else if ( this.equals( c ) || mInternalQueue.equals( c ) ) {
            throw new IllegalArgumentException( "Cannot drain queue to itself" );
        }
        
        int count = 0;
        Iterator< E > iterator = iterator( );
        for ( ; count < maxElements && iterator.hasNext( ); count++ ) {
            E e = (E) iterator.next( );
            iterator.remove( );
            c.add( e );
        }
        return count;
    }
    
    @Override
    public Iterator< E > iterator( ) {
        mLock.lock( );
        try {
            return mInternalQueue.iterator( );
        } finally {
            mLock.unlock( );
        }
    }
    
    @Override
    public Object[] toArray( ) {
        mLock.lock( );
        try {
            return mInternalQueue.toArray( );
        } finally {
            mLock.unlock( );
        }
    }
    
    @Override
    public < T > T[] toArray( T[] array ) {
        mLock.lock( );
        try {
            return mInternalQueue.toArray( array );
        } finally {
            mLock.unlock( );
        }
    }
}
