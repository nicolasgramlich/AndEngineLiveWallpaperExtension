package org.anddev.andengine.ui.livewallpaper;

import net.rbgrn.opengl.GLWallpaperService;

import org.anddev.andengine.engine.EngineOptions;
import org.anddev.andengine.entity.Scene;
import org.anddev.andengine.opengl.view.RenderSurfaceView;
import org.anddev.andengine.sensor.accelerometer.AccelerometerListener;

import android.opengl.GLSurfaceView.Renderer;

public abstract class BaseWallpaperService extends GLWallpaperService {
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
		super.onCreate();
		
		this.mEngine = this.onLoadEngine();
		applyEngineOptions(this.mEngine.getEngineOptions());

		this.onLoadResources();
		this.mEngine.setScene(this.onLoadScene());
		this.onLoadComplete();
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================

	protected abstract Scene onLoadScene();

	protected abstract void onLoadResources();

	protected abstract void onLoadComplete();

	protected abstract org.anddev.andengine.engine.Engine onLoadEngine();

	@Override
	public Engine onCreateEngine() {
		return new WallpaperEngine();
	}

	// ===========================================================
	// Methods
	// ===========================================================
	
	private void applyEngineOptions(EngineOptions pEngineOptions) {
		
	}

	protected void enableAccelerometer(final AccelerometerListener pAccelerometerListener) {
		this.mEngine.enableAccelerometer(this, pAccelerometerListener);
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

	class WallpaperEngine extends GLEngine {
		// ===========================================================
		// Fields
		// ===========================================================
		
		private Renderer mRenderer;

		// ===========================================================
		// Constructors
		// ===========================================================

		public WallpaperEngine() {
			super();
			this.mRenderer = new RenderSurfaceView.Renderer(BaseWallpaperService.this.mEngine);
			this.setRenderer(this.mRenderer);
			this.setRenderMode(RENDERMODE_CONTINUOUSLY);
		}

		// ===========================================================
		// Methods for/from SuperClass/Interfaces
		// ===========================================================

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