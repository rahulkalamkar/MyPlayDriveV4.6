package com.hungama.hungamamusic.lite.carmode.view;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.SeekBar;

public class VerticalSeekBar extends SeekBar {
	/**
	 * The angle by which the SeekBar view should be rotated.
	 */
	private static final int ROTATION_ANGLE = 90;

	/**
	 * A change listener registrating start and stop of tracking. Need an own listener because the listener in SeekBar
	 * is private.
	 */
	private OnSeekBarChangeListener mOnSeekBarChangeListener;

	// JAVADOC:OFF
	/**
	 * Standard constructor to be implemented for all views.
	 *
	 */
	public VerticalSeekBar(final Context context) {
		super(context);
	}

	/**
	 * Standard constructor to be implemented for all views.
	 *
	 */
	public VerticalSeekBar(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
	}

	/**
	 * Standard constructor to be implemented for all views.
	 *
	 */
	public VerticalSeekBar(final Context context, final AttributeSet attrs) {
		super(context, attrs);
	}

	// JAVADOC:ON

	/*
	 * (non-Javadoc) ${see_to_overridden}
	 */
	@Override
	protected final void onSizeChanged(final int width, final int height, final int oldWidth, final int oldHeight) {
		super.onSizeChanged(height, width, oldHeight, oldWidth);
	}

	/*
	 * (non-Javadoc) ${see_to_overridden}
	 */
	@Override
	protected final synchronized void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
		super.onMeasure(heightMeasureSpec, widthMeasureSpec);
		setMeasuredDimension(getMeasuredHeight(), getMeasuredWidth());
	}

	/*
	 * (non-Javadoc) ${see_to_overridden}
	 */
	@Override
	protected final void onDraw(final Canvas c) {
		c.rotate(ROTATION_ANGLE);
		c.translate(0, -getWidth());

		super.onDraw(c);
	}

	/*
	 * (non-Javadoc) ${see_to_overridden}
	 */
	@Override
	public final void setOnSeekBarChangeListener(final OnSeekBarChangeListener l) {
		mOnSeekBarChangeListener = l;
		super.setOnSeekBarChangeListener(l);
	}

	
	/*
	 * (non-Javadoc) ${see_to_overridden}
	 */
	@Override
	public final boolean onTouchEvent(final MotionEvent event) {
		if (!isEnabled()) {
			return false;
		}

		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			//setProgress(getMax() - (int) (getMax() * event.getY() / getHeight()));
			setProgress(((int) ((getMax() * (event.getY()) - getMax()) / getHeight())));
			mOnSeekBarChangeListener.onProgressChanged(this, ((int) ((getMax() * (event.getY()) - getMax()) / getHeight())), true);
			mOnSeekBarChangeListener.onStartTrackingTouch(this);
			//onSizeChanged(getWidth(), getHeight(), 0, 0);

			break;

		case MotionEvent.ACTION_MOVE:
			//setProgress(getMax() - (int) (getMax() * event.getY() / getHeight()));
			setProgress(((int) ((getMax() * (event.getY()) - getMax()) / getHeight())));
			mOnSeekBarChangeListener.onProgressChanged(this, ((int) ((getMax() * (event.getY()) - getMax()) / getHeight())), true);
			//onSizeChanged(getWidth(), getHeight(), 0, 0);


			break;

		case MotionEvent.ACTION_UP:
			//setProgress(getMax() - (int) (getMax() * event.getY() / getHeight()));
			setProgress(((int) ((getMax() * (event.getY()) - getMax()) / getHeight())));
			mOnSeekBarChangeListener.onProgressChanged(this, ((int) ((getMax() * (event.getY()) - getMax()) / getHeight())), true);

			mOnSeekBarChangeListener.onStopTrackingTouch(this);
			//onSizeChanged(getWidth(), getHeight(), 0, 0);

			break;

		case MotionEvent.ACTION_CANCEL:
			mOnSeekBarChangeListener.onStopTrackingTouch(this);
			break;

		default:
			break;
		}

		return true;
	}

	/*
	 * (non-Javadoc) ${see_to_overridden}
	 */
	@Override
	public final void setProgress(final int progress) {
		super.setProgress(progress);
	//	mOnSeekBarChangeListener.onProgressChanged(this, progress, false);

		onSizeChanged(getWidth(), getHeight(), 0, 0);
	}
}