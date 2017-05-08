
package com.hungama.myplay.activity.util.gifview;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.util.AttributeSet;
import android.widget.ImageView;

public class GifDecoderViewOld extends ImageView {
	
	private String imagePath = null;

    private boolean mIsPlayingGif = false;

    private GifDecoderOld mGifDecoder;

    private Bitmap mTmpBitmap;

    final Handler mHandler = new Handler();

    final Runnable mUpdateResults = new Runnable() {
        public void run() {
            if (mTmpBitmap != null && !mTmpBitmap.isRecycled()) {
                GifDecoderViewOld.this.setImageBitmap(mTmpBitmap);
            }
        }
    };
    
    Context context;
    public GifDecoderViewOld(Context context) {    	
        super(context);
        this.context=context;
    }

//    public GifDecoderView(Context context, InputStream stream) {
//        super(context);
//    }

    /**
	 * @param context
	 * @param attrs
	 * @param defStyle
	 */
	public GifDecoderViewOld(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
        this.context=context;

	}

	/**
	 * @param context
	 * @param attrs
	 */
	public GifDecoderViewOld(Context context, AttributeSet attrs) {
		super(context, attrs);
        this.context=context;

	}

	private void playGif() throws Exception {
    	InputStream stream = null;
        mGifDecoder = new GifDecoderOld();
        try {
			stream = new FileInputStream(imagePath);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
        int status = mGifDecoder.read(stream);
//        System.out.println(" :::::::::::::: " + status);
        try{
	        if(status==0) {
		        mIsPlayingGif = true;
		
		        new Thread(new Runnable() {
		            public void run() {
		                final int n = mGifDecoder.getFrameCount();
		                final int ntimes = mGifDecoder.getLoopCount();
		                int repetitionCounter = 0;
		                do {
		                    for (int i = 0; i < n; i++) {
		                        mTmpBitmap = mGifDecoder.getFrame(i);
		                        int t = mGifDecoder.getDelay(i);
		                        mHandler.post(mUpdateResults);
		                        try {
		                            Thread.sleep(t);
		                        } catch (InterruptedException e) {
		                            e.printStackTrace();
		                        }
		                    }
		                    if(ntimes != 0) {
		                        repetitionCounter ++;
		                    }
		                } while (mIsPlayingGif && (repetitionCounter <= ntimes));
		            }
		        }).start();
	        } else{
	        	try{
	        		stream.close();
	        	}catch(Exception e){
	        		e.printStackTrace();
	        	}
	        	try{
//	        		setScaleType(ScaleType.CENTER_INSIDE);
	        		mTmpBitmap = BitmapFactory.decodeFile(imagePath);
	        		setImageBitmap(mTmpBitmap);
//	        		DisplayMetrics metrics = getResources().getDisplayMetrics();
//	        		Drawable mTmpDrawable = Utils.ResizeBitmap(context,metrics.densityDpi, metrics.widthPixels, new BitmapDrawable(mTmpBitmap));
//	        		setImageDrawable(mTmpDrawable);
//	        		setBackgroundDrawable(BitmapDrawable.createFromPath(imagePath));
	        	}catch(Exception e){
	        		e.printStackTrace();
	        		throw new Exception();
	        	} catch (Error e) {
	        		System.gc(); 
	        		System.runFinalization(); 
	        		System.gc(); 
	        		throw new Exception();
	        	}
	        }
        } catch (Exception e) {
        	e.printStackTrace();
        	throw new Exception();
		}
    }
    
    public void stopRendering() {
        mIsPlayingGif = true;
    }
    
    public void setImagePath(String path) throws Exception{
    	imagePath = path;
    	playGif();
    }
    
    public GifDecoderViewOld(Context context, InputStream stream) {    
    	super(context);
    	playGif(stream);
    }
    
	public void playGif(InputStream stream) {
		mGifDecoder = new GifDecoderOld();
		mGifDecoder.read(stream);
		mIsPlayingGif = true;
		
        new Thread(new Runnable() {
            public void run() {
                final int n = mGifDecoder.getFrameCount();
                final int ntimes = mGifDecoder.getLoopCount();
                int repetitionCounter = 0;
                do {
                    for (int i = 0; i < n; i++) {
                        mTmpBitmap = mGifDecoder.getFrame(i);
                        int t = mGifDecoder.getDelay(i);
                        mHandler.post(mUpdateResults);
                        try {
                            Thread.sleep(t);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    if(ntimes != 0) {
                        repetitionCounter ++;
                    }
                } while (mIsPlayingGif && (repetitionCounter <= ntimes));
            }
        }).start();
	}

	public void clear() {
		mIsPlayingGif = false;
	}
}
