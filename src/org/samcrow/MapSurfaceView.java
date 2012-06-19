/**
 * Let's see where this goes.
 */
package org.samcrow;

import android.content.Context;
import android.graphics.Canvas;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;

/**
 * @author Sam Crow Handles drawing the map
 */
public class MapSurfaceView extends SurfaceView implements Callback {

	private MapThread thread;

	/**
	 * @param context
	 */
	public MapSurfaceView(Context context) {
		super(context);

		thread = new MapThread(this.getHolder());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.view.SurfaceHolder.Callback#surfaceChanged(android.view.SurfaceHolder
	 * , int, int, int)
	 */
	@Override
	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.view.SurfaceHolder.Callback#surfaceCreated(android.view.SurfaceHolder
	 * )
	 */
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		if (!thread.isAlive()) {
			thread.start();
		}
		thread.setRunning(true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.SurfaceHolder.Callback#surfaceDestroyed(android.view.
	 * SurfaceHolder)
	 */
	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		thread.setRunning(false);
	}

	private class MapThread extends Thread {

		private boolean running = false;

		private SurfaceHolder holder;

		public MapThread(SurfaceHolder holder) {
			this.holder = holder;
		}

		public synchronized void setRunning(boolean running) {
			this.running = running;
		}

		@Override
		public void run() {
			while (!this.isInterrupted()) {
				if (running) {
					Canvas canvas = null;
					try {
						canvas = holder.lockCanvas();

						canvas.drawColor(android.R.color.holo_orange_dark);
					} finally {
						holder.unlockCanvasAndPost(canvas);
					}
				}
			}
		}
	}
}
