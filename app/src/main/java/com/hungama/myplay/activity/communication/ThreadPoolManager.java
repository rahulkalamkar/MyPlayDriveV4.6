package com.hungama.myplay.activity.communication;

import android.util.Log;

import java.net.CookieManager;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class ThreadPoolManager {
    private final static int CORES=Runtime.getRuntime().availableProcessors();
    private static final int CORE_POOL_SIZE = CORES*2;
    private static final int MAX_POOL_SIZE =CORE_POOL_SIZE*2;
    private static ThreadPoolManager sInstance = null;
    
    public synchronized static final ThreadPoolManager getInstance( ) {
        Log.e("CORE","CORE:"+CORE_POOL_SIZE+ " > MAX> "+MAX_POOL_SIZE);
        if (sInstance == null) {
            sInstance = new ThreadPoolManager( );
        }
        return sInstance;
    }

//    private static int NUMBER_OF_CORES =
//           ;

    private ExecutorService mPool;
    private CookieManager mCookieManager;
    
    private ThreadPoolManager( )
    {
        mPool = new PriorityAwareThreadPoolExecutor( CORE_POOL_SIZE, MAX_POOL_SIZE, 1,
                        TimeUnit.SECONDS );
        mCookieManager = new CookieManager( );
        CookieManager.setDefault( mCookieManager );
    }
    
    /**
     * This method clears the WebService requests queue from PriorityAwareThreadPoolExecutor.
     */
    public void clearRequestsQueue()
    {
    	((PriorityAwareThreadPoolExecutor)mPool).getQueue().clear();
    }
    
    public < T > Future< ? > submit( Runnable task ) {
        return mPool.submit( task );
    }
    
    public ExecutorService getExecutor( ) {
        return mPool;
    }
    
    public CookieManager getCookieManager( ) {
        return mCookieManager;
    }
}
