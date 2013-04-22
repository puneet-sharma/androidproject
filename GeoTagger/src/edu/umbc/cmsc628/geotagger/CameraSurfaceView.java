package edu.umbc.cmsc628.geotagger;

import java.io.IOException;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

class CameraSurfaceView extends SurfaceView implements SurfaceHolder.Callback {

	Camera camera;
	CameraActivity camAct;

	CameraSurfaceView(Context context) {
		super(context);
		// Install a SurfaceHolder.Callback so we get notified when the
		// underlying surface is created and destroyed.
		SurfaceHolder holder = this.getHolder();
		holder.addCallback(this);
	}
	
	CameraSurfaceView(CameraActivity camAct) {
		super(camAct.getApplicationContext());
		this.camAct=camAct;
		// Install a SurfaceHolder.Callback so we get notified when the
		// underlying surface is created and destroyed.
		SurfaceHolder holder = this.getHolder();
		holder.addCallback(this);
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {

		// IMPORTANT: We must call startPreview() on the camera before we take
		// any pictures
		camera.startPreview();
		//setCameraDisplayOrientation(camAct, Camera.CameraInfo.CAMERA_FACING_BACK, camera);
		takePicture(camAct);
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		try {
			// Open the Camera in preview mode
			this.camera = Camera.open();
			this.camera.setPreviewDisplay(holder);
		} catch (IOException ioe) {
			ioe.printStackTrace(System.out);
		}

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// Surface will be destroyed when replaced with a new screen
		// Always make sure to release the Camera instance
		camera.stopPreview();
		camera.release();
		camera = null;
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		// TODO Auto-generated method stub
	}

	public void takePicture(PictureCallback imageCallback) {
		System.err.println("imageCallback: "+imageCallback);
		System.err.println("camera :"+ camera);
		camera.takePicture(null, null, imageCallback);
	}
}