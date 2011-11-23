package org.andengine.extension.ui.livewallpaper;

import org.andengine.engine.options.EngineOptions;
import org.andengine.entity.scene.Scene;
import org.andengine.extension.opengl.GLWallpaperService;
import org.andengine.opengl.view.ConfigChooser;
import org.andengine.opengl.view.RenderSurfaceView.IRendererListener;
import org.andengine.opengl.view.RenderSurfaceView.Renderer;
import org.andengine.sensor.accelerometer.IAccelerometerListener;
import org.andengine.sensor.orientation.IOrientationListener;
import org.andengine.ui.IGameInterface;
import org.andengine.util.debug.Debug;

import android.app.WallpaperManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.MotionEvent;

/**
 * (c) Nicolas Gramlich 2010
 * (c) Zynga 2011
 *
 * @author Nicolas Gramlich <ngramlich@zynga.com>
 * @since 7:32:25 PM - Nov 3, 2011
 */
public abstract class BaseLiveWallpaperService extends GLWallpaperService implements IGameInterface, IRendererListener {
	// ===========================================================
	// Constants
	// ===========================================================

	// ===========================================================
	// Fields
	// ===========================================================

	private org.andengine.engine.Engine mEngine;

	// ===========================================================
	// Constructors
	// ===========================================================

	@Override
	public void onCreate() {
		super.onCreate();

		this.mEngine = this.onLoadEngine();
		this.applyEngineOptions(this.mEngine.getEngineOptions());

		this.onLoadResources();
		final Scene scene = this.onLoadScene();
		this.mEngine.onLoadComplete(scene);
		this.onLoadComplete();
		this.mEngine.start();
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	public org.andengine.engine.Engine getEngine() {
		return this.mEngine;
	}

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================

	protected void onPause(){
		this.mEngine.stop();
	}

	protected void onResume(){
		this.mEngine.start();
	}

	@Override
	public Engine onCreateEngine() {
		return new BaseWallpaperGLEngine(this);
	}

	@Override
	public void onPauseGame() {

	}

	@Override
	public void onResumeGame() {

	}

	@Override
	public void onUnloadResources() {

	}

	@Override
	public void onSurfaceCreated() {
		Debug.d("onSurfaceCreated");
	}

	@Override
	public void onSurfaceChanged(final int pWidth, final int pHeight) {
		Debug.d("onSurfaceChanged: pWidth=" + pWidth + "  pHeight=" + pHeight);
	}

	// ===========================================================
	// Methods
	// ===========================================================

	protected void onTap(final int pX, final int pY) {
		this.mEngine.onTouch(null, MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN, pX, pY, 0));
	}

	protected void onDrop(final int pX, final int pY) {

	}

	protected void applyEngineOptions(final EngineOptions pEngineOptions) {

	}

	protected boolean enableVibrator() {
		return this.mEngine.enableVibrator(this);
	}

	protected boolean enableAccelerometerSensor(final IAccelerometerListener pAccelerometerListener) {
		return this.mEngine.enableAccelerometerSensor(this, pAccelerometerListener);
	}

	protected boolean enableOrientationSensor(final IOrientationListener pOrientationListener) {
		return this.mEngine.enableOrientationSensor(this, pOrientationListener);
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

	protected class BaseWallpaperGLEngine extends GLEngine {
		// ===========================================================
		// Constants
		// ===========================================================

		// ===========================================================
		// Fields
		// ===========================================================

		private Renderer mRenderer;
		private ConfigChooser mConfigChooser;

		// ===========================================================
		// Constructors
		// ===========================================================

		public BaseWallpaperGLEngine(final IRendererListener pRendererListener) {
			if(this.mConfigChooser == null) {
				this.mConfigChooser = new ConfigChooser(BaseLiveWallpaperService.this.mEngine.getEngineOptions().getRenderOptions().isMultiSampling());
			}
			this.setEGLConfigChooser(this.mConfigChooser);

			this.mRenderer = new Renderer(BaseLiveWallpaperService.this.mEngine, this.mConfigChooser, pRendererListener);
			this.setRenderer(this.mRenderer);
			this.setRenderMode(GLEngine.RENDERMODE_CONTINUOUSLY);
		}

		// ===========================================================
		// Getter & Setter
		// ===========================================================

		// ===========================================================
		// Methods for/from SuperClass/Interfaces
		// ===========================================================

		@Override
		public Bundle onCommand(final String pAction, final int pX, final int pY, final int pZ, final Bundle pExtras, final boolean pResultRequested) {
			if(pAction.equals(WallpaperManager.COMMAND_TAP)) {
				BaseLiveWallpaperService.this.onTap(pX, pY);
			} else if (pAction.equals(WallpaperManager.COMMAND_DROP)) {
				BaseLiveWallpaperService.this.onDrop(pX, pY);
			}

			return super.onCommand(pAction, pX, pY, pZ, pExtras, pResultRequested);
		}

		@Override
		public void onResume() {
			super.onResume();

			BaseLiveWallpaperService.this.getEngine().onResume();
			BaseLiveWallpaperService.this.onResume();
		}

		@Override
		public void onPause() {
			super.onPause();

			BaseLiveWallpaperService.this.getEngine().onPause();
			BaseLiveWallpaperService.this.onPause();
		}

		@Override
		public void onDestroy() {
			super.onDestroy();

			this.mRenderer = null;
		}

		// ===========================================================
		// Methods
		// ===========================================================

		// ===========================================================
		// Inner and Anonymous Classes
		// ===========================================================
	}
}