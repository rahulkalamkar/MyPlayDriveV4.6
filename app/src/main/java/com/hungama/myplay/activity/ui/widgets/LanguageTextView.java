/**
 * 
 */
package com.hungama.myplay.activity.ui.widgets;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;

import com.reverie.customcomponent.RevTextView;

/**
 * @author XTPL
 * 
 */
public class LanguageTextView extends RevTextView {

	/**
	 * @param arg0
	 */
	public LanguageTextView(Context arg0) {
		super(arg0);
	}

	public void append(String string) {
		setText(getText() + string);
	}

	/**
	 * @param arg0
	 * @param arg1
	 */
	public LanguageTextView(Context arg0, AttributeSet arg1) {
		super(arg0, arg1);
	}

	@Override
	protected void onDraw(Canvas arg0) {
		try {
			super.onDraw(arg0);
		} catch (Exception e) {
			e.printStackTrace();
		} catch (Error e) {
			e.printStackTrace();
		}
	}
}
