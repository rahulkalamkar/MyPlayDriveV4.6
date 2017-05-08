package com.hungama.myplay.activity.communication;

import java.util.Comparator;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class PriorityAwareThreadPoolExecutor extends ThreadPoolExecutor {
    
    private Comparator< PriorityElement > mComparator =
            new PriorityElementComparator< PriorityElement >( );
    
    public PriorityAwareThreadPoolExecutor( int corePoolSize, int maximumPoolSize,
            long keepAliveTime, TimeUnit unit, RejectedExecutionHandler handler ) {
        super( corePoolSize, maximumPoolSize, keepAliveTime, unit,
                new PriorityAwareBlockingQueue< Runnable >( ), handler );
    }
    
    public PriorityAwareThreadPoolExecutor( int corePoolSize, int maximumPoolSize,
            long keepAliveTime, TimeUnit unit, ThreadFactory threadFactory,
            RejectedExecutionHandler handler ) {
        super( corePoolSize, maximumPoolSize, keepAliveTime, unit,
                new PriorityAwareBlockingQueue< Runnable >( ), threadFactory, handler );
    }
    
    public PriorityAwareThreadPoolExecutor( int corePoolSize, int maximumPoolSize,
            long keepAliveTime, TimeUnit unit, ThreadFactory threadFactory ) {
        super( corePoolSize, maximumPoolSize, keepAliveTime, unit,
                new PriorityAwareBlockingQueue< Runnable >( ), threadFactory );
    }
    
    public PriorityAwareThreadPoolExecutor( int corePoolSize, int maximumPoolSize,
            long keepAliveTime, TimeUnit unit ) {
        super( corePoolSize, maximumPoolSize, keepAliveTime, unit,
                new PriorityAwareBlockingQueue< Runnable >( ) );
    }
    
    public void setComparator( Comparator< PriorityElement > comparator ) {
        this.mComparator = comparator;
    }
    
    @Override
    protected < T > RunnableFuture< T > newTaskFor( Callable< T > callable ) {
        RunnableFuture< T > future = super.newTaskFor( callable );
        if ( callable instanceof MutablePriorityElement ) {
            MutablePriorityElement element = (MutablePriorityElement) callable;
            future = new PriorityAwareFuture< T >( future, element );
        } else {
            future = new BlankPriorityFuture< T >( future );
        }
        return future;
    }
    
    @SuppressWarnings( "unchecked" )
    @Override
    protected < T > RunnableFuture< T > newTaskFor( Runnable runnable, T value ) {
        if ( runnable instanceof BlankPriorityFuture< ? > ) {
            return (RunnableFuture< T >) runnable;
        }
        
        RunnableFuture< T > future = super.newTaskFor( runnable, value );
        if ( runnable instanceof MutablePriorityElement ) {
            MutablePriorityElement element = (MutablePriorityElement) runnable;
            future = new PriorityAwareFuture< T >( future, element );
        } else {
            future = new BlankPriorityFuture< T >( future );
        }
        return future;
    }
    
    @Override
    public Future< ? > submit( Runnable task ) {
        try {
            return super.submit( task );
        } catch ( RejectedExecutionException e ) {
            if ( ! ( task instanceof MutablePriorityElement ) ) {
                throw e;
            }
            MutablePriorityElement element = (MutablePriorityElement) task;
            element.setPriority( WebServicePriorities.IMMEDIATE );
            return super.submit( task );
        }
    }
    
    @Override
    public < T > Future< T > submit( Runnable task, T result ) {
        try {
            return super.submit( task, result );
        } catch ( RejectedExecutionException e ) {
            if ( ! ( task instanceof MutablePriorityElement ) ) {
                throw e;
            }
            MutablePriorityElement element = (MutablePriorityElement) task;
            element.setPriority( WebServicePriorities.IMMEDIATE );
            return super.submit( task, result );
        }
    }
    
    @Override
    public < T > Future< T > submit( Callable< T > task ) {
        try {
            return super.submit( task );
        } catch ( RejectedExecutionException e ) {
            if ( ! ( task instanceof MutablePriorityElement ) ) {
                throw e;
            }
            MutablePriorityElement element = (MutablePriorityElement) task;
            element.setPriority( WebServicePriorities.IMMEDIATE );
            return super.submit( task );
        }
    }
    
    @Override
    public void execute( Runnable command ) {
        if ( ! ( command instanceof MutablePriorityElement ) ) {
            command = new BlankPriorityRunnable( command );
        }
        super.execute( command );
        if ( ! ( command instanceof PriorityAwareFuture< ? > ) ) {
            return;
        }
        PriorityAwareFuture< ? > future = (PriorityAwareFuture< ? >) command;
        future.getPriorityObservable( ).addObserver( future );
    }
    
    @Override
    protected void beforeExecute( Thread t, Runnable r ) {
        super.beforeExecute( t, r );
        if ( ! ( r instanceof PriorityAwareFuture< ? > ) ) {
            return;
        }
        PriorityAwareFuture< ? > future = (PriorityAwareFuture< ? >) r;
        future.getPriorityObservable( ).deleteObserver( future );
    }
    
    protected class BlankPriorityRunnable implements Runnable, MutablePriorityElement,
            Comparable< PriorityElement > {
        private Runnable mTargetRunnable;
        
        public BlankPriorityRunnable( Runnable runnable ) {
            super( );
            this.mTargetRunnable = runnable;
        }
        
        @Override
        public void run( ) {
            mTargetRunnable.run( );
        }
        
        @Override
        public int getPriority( ) {
            return WebServicePriorities.MAX;
        }
        
        @Override
        public void setPriority( int priority ) {
        }
        
        @Override
        public Observable getPriorityObservable( ) {
            return null;
        }
        
        @Override
        public int compareTo( PriorityElement another ) {
            return 0;
        }
    }
    
    protected class BlankPriorityFuture< T > extends BlankPriorityRunnable implements
            RunnableFuture< T >, MutablePriorityElement, Comparable< PriorityElement > {
        private RunnableFuture< T > mTargetFuture;
        
        public BlankPriorityFuture( RunnableFuture< T > future ) {
            super( future );
            this.mTargetFuture = future;
        }
        
        @Override
        public boolean cancel( boolean mayInterruptIfRunning ) {
            return mTargetFuture.cancel( mayInterruptIfRunning );
        }
        
        @Override
        public boolean isCancelled( ) {
            return mTargetFuture.isCancelled( );
        }
        
        @Override
        public boolean isDone( ) {
            return mTargetFuture.isDone( );
        }
        
        @Override
        public T get( ) throws InterruptedException, ExecutionException {
            return mTargetFuture.get( );
        }
        
        @Override
        public T get( long timeout, TimeUnit unit ) throws InterruptedException,
                ExecutionException, TimeoutException {
            return mTargetFuture.get( timeout, unit );
        }
        
        @Override
        public void run( ) {
            mTargetFuture.run( );
        }
    }
    
    protected class PriorityAwareFuture< T > extends BlankPriorityFuture< T > implements
            Comparable< PriorityElement >, Observer {
        private MutablePriorityElement mTargetElement;
        
        public PriorityAwareFuture( RunnableFuture< T > future, MutablePriorityElement element ) {
            super( future );
            this.mTargetElement = element;
        }
        
        @Override
        public int getPriority( ) {
            return mTargetElement.getPriority( );
        }
        
        @Override
        public void setPriority( int priority ) {
            mTargetElement.setPriority( priority );
        }
        
        @Override
        public Observable getPriorityObservable( ) {
            return mTargetElement.getPriorityObservable( );
        }
        
        @Override
        public int compareTo( PriorityElement another ) {
            return mComparator.compare( mTargetElement, another );
        }
        
        @Override
        public synchronized void update( Observable observable, Object data ) {
            boolean removed = remove( this );
            if ( removed ) {
                submit( this );
            }
        }
    }
}
