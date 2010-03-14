package org.anddev.andengine.ui.livewallpaper;

import net.rbgrn.opengl.GLWallpaperService;

import org.anddev.andengine.engine.EngineOptions;
import org.anddev.andengine.entity.Scene;
import org.anddev.andengine.opengl.view.RenderSurfaceView;
import org.anddev.andengine.sensor.accelerometer.IAccelerometerListener;
import org.anddev.andengine.ui.IGameInterface;
import org.anddev.andengine.util.Debug;

import android.content.Intent;
import android.opengl.GLSurfaceView.Renderer;

public abstract class BaseWallpaperService extends GLWallpaperService implements IGameInterface {
	// ===========================================================
	// Constants
	// ===========================================================

	// ===========================================================
	// Fields
	// ===========================================================
	
	private org.anddev.andengine.engine.Engine mEngine;

	// ===========================================================
	// Constructors
	// ===========================================================

	@Override
	public void onCreate() {
		Debug.d("onCreate");
		super.onCreate();
		
		this.mEngine = this.onLoadEngine();
		applyEngineOptions(this.mEngine.getEngineOptions());

		this.onLoadResources();
		final Scene scene = this.onLoadScene();
		this.mEngine.onLoadComplete(scene);
		this.onLoadComplete();
		this.mEngine.start();
	}
	
	@Override
	public void onRebind(Intent pIntent) {
		Debug.d("onRebind");
		super.onRebind(pIntent);
	}
	
	@Override
	public boolean onUnbind(Intent pIntent) {
		Debug.d("onUnbind");
		return super.onUnbind(pIntent);
	}
	
	@Override
	public void onDestroy() {
		Debug.d("onDestroy");
		super.onDestroy();
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================
	
	public org.anddev.andengine.engine.Engine getEngine() {
		return this.mEngine;
	}

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================

	protected abstract void onPause();

	@Override
	public Engine onCreateEngine() {
		return new BaseWallpaperEngine();
	}

	// ===========================================================
	// Methods
	// ===========================================================
	
	private void applyEngineOptions(final EngineOptions pEngineOptions) {
		
	}

	protected void enableAccelerometer(final IAccelerometerListener pAccelerometerListener) {
		this.mEngine.enableAccelerometer(this, pAccelerometerListener);
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

	class BaseWallpaperEngine extends GLEngine {
		// ===========================================================
		// Fields
		// ===========================================================
		
		private Renderer mRenderer;

		// ===========================================================
		// Constructors
		// ===========================================================

		public BaseWallpaperEngine() {
			this.mRenderer = new RenderSurfaceView.Renderer(BaseWallpaperService.this.mEngine);
			this.setRenderer(this.mRenderer);
			this.setRenderMode(RENDERMODE_CONTINUOUSLY);
		}

		// ===========================================================
		// Methods for/from SuperClass/Interfaces
		// ===========================================================
		
		@Override
		public void onResume() {
			super.onResume();
			BaseWallpaperService.this.getEngine().reloadTextures();
		}
		
		@Override
		public void onPause() {
			super.onPause();
			BaseWallpaperService.this.onPause();
		}

		@Override
		public void onDestroy() {
			super.onDestroy();
			if (this.mRenderer != null) {
				// mRenderer.release();
			}
			this.mRenderer = null;
		}
	}
}