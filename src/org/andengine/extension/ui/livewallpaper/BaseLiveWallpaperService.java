package org.andengine.extension.ui.livewallpaper;

import org.andengine.engine.options.EngineOptions;
import org.andengine.entity.scene.Scene;
import org.andengine.extension.opengl.GLWallpaperService;
import org.andengine.opengl.view.ConfigChooser;
import org.andengine.opengl.view.IRendererListener;
import org.andengine.opengl.view.EngineRenderer;
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

	private EngineOptions mEngineOptions;
	private org.andengine.engine.Engine mEngine;

	private boolean mGamePaused;
	private boolean mGameCreated;
	private boolean mCreateGameCalled;
	private boolean mOnReloadResourcesScheduled;

	// ===========================================================
	// Constructors
	// ===========================================================

	@Override
	public void onCreate() {
		Debug.d(this.getClass().getSimpleName() + ".onCreate");

		super.onCreate();

		this.mGamePaused = true;

		this.mEngineOptions = this.onCreateEngineOptions();
		this.mEngine = this.onCreateEngine(this.mEngineOptions);

		this.applyEngineOptions();
	}

	@Override
	public org.andengine.engine.Engine onCreateEngine(final EngineOptions pEngineOptions) {
		return new org.andengine.engine.Engine(pEngineOptions);
	}

	@Override
	public synchronized void onSurfaceCreated() {
		Debug.d(this.getClass().getSimpleName() + ".onSurfaceCreated");

		if(this.mGameCreated) {
			this.onReloadResources();
		} else {
			if(this.mCreateGameCalled) {
				this.mOnReloadResourcesScheduled = true;
			} else {
				this.mCreateGameCalled = true;
				this.onCreateGame();
			}
		}
	}

	@Override
	public void onSurfaceChanged(final int pWidth, final int pHeight) {
		Debug.d(this.getClass().getSimpleName() + ".onSurfaceChanged(Width=" + pWidth + ",  Height=" + pHeight + ")");
	}

	protected void onCreateGame() {
		Debug.d(this.getClass().getSimpleName() + ".onCreateGame");
		final OnPopulateSceneCallback onPopulateSceneCallback = new OnPopulateSceneCallback() {
			@Override
			public void onPopulateSceneFinished() {
				try {
					Debug.d(this.getClass().getSimpleName() + ".onGameCreated");

					BaseLiveWallpaperService.this.onGameCreated();
				} catch(final Throwable pThrowable) {
					Debug.e(this.getClass().getSimpleName() + ".onGameCreated failed.", pThrowable);
				}

				BaseLiveWallpaperService.this.onResumeGame();
			}
		};

		final OnCreateSceneCallback onCreateSceneCallback = new OnCreateSceneCallback() {
			@Override
			public void onCreateSceneFinished(final Scene pScene) {
				BaseLiveWallpaperService.this.mEngine.setScene(pScene);

				try {
					Debug.d(this.getClass().getSimpleName() + ".onPopulateScene");

					BaseLiveWallpaperService.this.onPopulateScene(pScene, onPopulateSceneCallback);
				} catch(final Throwable pThrowable) {
					Debug.e(this.getClass().getSimpleName() + ".onPopulateScene failed.", pThrowable);
				}
			}
		};

		final OnCreateResourcesCallback onCreateResourcesCallback = new OnCreateResourcesCallback() {
			@Override
			public void onCreateResourcesFinished() {
				try {
					Debug.d(this.getClass().getSimpleName() + ".onCreateScene");

					BaseLiveWallpaperService.this.onCreateScene(onCreateSceneCallback);
				} catch(final Throwable pThrowable) {
					Debug.e(this.getClass().getSimpleName() + ".onCreateScene failed.", pThrowable);
				}
			}
		};

		try {
			Debug.d(this.getClass().getSimpleName() + ".onCreateResources");

			this.onCreateResources(onCreateResourcesCallback);
		} catch(final Throwable pThrowable) {
			Debug.e(this.getClass().getSimpleName() + ".onCreateGame failed.", pThrowable);
		}
	}

	@Override
	public synchronized void onGameCreated() {
		this.mGameCreated = true;

		/* Since the potential asynchronous resource creation,
		 * the surface might already be invalid
		 * and a resource reloading might be necessary. */
		if(this.mOnReloadResourcesScheduled) {
			this.mOnReloadResourcesScheduled = false;
			try {
				this.onReloadResources();
			} catch(final Throwable pThrowable) {
				Debug.e(this.getClass().getSimpleName() + ".onReloadResources failed.", pThrowable);
			}
		}
	}

	protected void onResume() {
		Debug.d(this.getClass().getSimpleName() + ".onResume");
	}

	@Override
	public void onResumeGame() {
		Debug.d(this.getClass().getSimpleName() + ".onResumeGame");

		this.mEngine.start();

		this.mGamePaused = false;
	}

	@Override
	public void onReloadResources() {
		Debug.d(this.getClass().getSimpleName() + ".onReloadResources");

		this.mEngine.onReloadResources();
		this.onResumeGame();
	}

	protected void onPause(){
		Debug.d(this.getClass().getSimpleName() + ".onPause");

		if(!this.mGamePaused) {
			this.onPauseGame();
		}
	}

	@Override
	public void onPauseGame() {
		Debug.d(this.getClass().getSimpleName() + ".onPauseGame");

		this.mGamePaused = true;

		this.mEngine.stop();
	}

	@Override
	public void onDestroy() {
		Debug.d(this.getClass().getSimpleName() + ".onDestroy");

		super.onDestroy();

		this.mEngine.onDestroy();

		try {
			this.onDestroyResources();
		} catch (final Throwable pThrowable) {
			Debug.e(this.getClass().getSimpleName() + ".onDestroyResources failed.", pThrowable);
		}

		this.onGameDestroyed();
	}

	@Override
	public void onDestroyResources() throws Exception {
		Debug.d(this.getClass().getSimpleName() + ".onDestroyResources");

		if(this.mEngine.getEngineOptions().getAudioOptions().needsMusic()) {
			this.mEngine.getMusicManager().releaseAll();
		}

		if(this.mEngine.getEngineOptions().getAudioOptions().needsSound()) {
			this.mEngine.getSoundManager().releaseAll();
		}
	}

	@Override
	public synchronized void onGameDestroyed() {
		Debug.d(this.getClass().getSimpleName() + ".onGameDestroyed");

		this.mGameCreated = false;
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

	@Override
	public Engine onCreateEngine() {
		return new BaseWallpaperGLEngine(this);
	}

	// ===========================================================
	// Methods
	// ===========================================================

	protected void onTap(final int pX, final int pY) {
		this.mEngine.onTouch(null, MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN, pX, pY, 0));
	}

	protected void onDrop(final int pX, final int pY) {

	}

	protected void applyEngineOptions() {

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

		private EngineRenderer mRenderer;
		private ConfigChooser mConfigChooser;

		// ===========================================================
		// Constructors
		// ===========================================================

		public BaseWallpaperGLEngine(final IRendererListener pRendererListener) {
			if(this.mConfigChooser == null) {
				this.mConfigChooser = new ConfigChooser(BaseLiveWallpaperService.this.mEngine.getEngineOptions().getRenderOptions().isMultiSampling());
			}
			this.setEGLConfigChooser(this.mConfigChooser);

			this.mRenderer = new EngineRenderer(BaseLiveWallpaperService.this.mEngine, this.mConfigChooser, pRendererListener);
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

			BaseLiveWallpaperService.this.getEngine().onReloadResources();
			BaseLiveWallpaperService.this.onResume();
		}

		@Override
		public void onPause() {
			super.onPause();

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