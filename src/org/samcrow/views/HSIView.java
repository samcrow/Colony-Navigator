package org.samcrow.views;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.SurfaceView;

/**
 * A view that displays a Horizontal Situation Indicator
 * @author samcrow
 */
public class HSIView extends SurfaceView {



	/* (non-Javadoc)
	 * @see android.view.View#onDraw(android.graphics.Canvas)
	 */
	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.onDraw(canvas);
	}

	public HSIView(Context context) {
		super(context);
	}

	public HSIView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public HSIView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

}
