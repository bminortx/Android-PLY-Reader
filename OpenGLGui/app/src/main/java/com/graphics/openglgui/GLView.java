/*
 *  Example 10: Surface view
 *  These sites were incredibly useful while learning this task:
 *  http://androidbook.com/item/4231
 *  http://www.learnopengles.com/tag/vertex-shader/
 *  http://www.learnopengles.com/tag/vertex-shader/
 *  http://developer.android.com/training/graphics/opengl/touch.html
 */

package com.graphics.openglgui;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

import java.io.IOException;
import java.io.InputStream;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

class GLView extends GLSurfaceView {
  private Renderer renderer;
  ScaleGestureDetector SGD;
  private float mPreviousX;
  private float mPreviousY;

  //  Constructor
  public GLView(Context context, AttributeSet attributeSet) {
    super(context, attributeSet);
    // Create an OpenGL ES 2.0 context
    setEGLContextClientVersion(2);
    // Set the Renderer for drawing on the GLSurfaceView
    renderer = new Renderer(context);
    setRenderer(renderer);
    SGD = new ScaleGestureDetector(context, new ScaleListener());
  }

  private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
    @Override
    public boolean onScale(ScaleGestureDetector detector) {
      renderer.scale *= detector.getScaleFactor();
      return true;
    }
  }

  @Override
  public boolean onTouchEvent(MotionEvent e) {
    // MotionEvent reports input details from the touch screen
    // and other input controls. In this case, you are only
    // interested in events where the touch position changed.
    float x = e.getX();
    float y = e.getY();
    int motionaction = e.getAction() & MotionEvent.ACTION_MASK;
    switch (motionaction) {
      case MotionEvent.ACTION_DOWN:
        // Prevent jumping around.
        mPreviousX = x;
        mPreviousY = y;
        break;
      case MotionEvent.ACTION_MOVE:
        if (renderer != null) {
          float deltaX = (x - mPreviousX) / 2f;
          float deltaY = (y - mPreviousY) / 2f;
          renderer.mDeltaX += deltaX;
          renderer.mDeltaY += deltaY;
        }
        mPreviousX = x;
        mPreviousY = y;
        break;
    }
    SGD.onTouchEvent(e);
    return true;
  }

  // Called when reset button is pressed.
  public void Reset() {
    renderer.scale = 1;
    Matrix.setIdentityM(renderer.mAccumulatedRotation, 0);
  }

  public static class Renderer implements GLSurfaceView.Renderer {
    private Cube cube;
    private Mesh mesh;
    // Intrinsic Matrices
    private final float[] mModelMatrix = new float[16];
    private final float[] mViewMatrix = new float[16];
    // Projection matrix is set in onSurfaceChanged()
    private final float[] mProjectionMatrix = new float[16];
    private final float[] mMVPMatrix = new float[16];
    // Rotations for our touch movements
    private final float[] mAccumulatedRotation = new float[16];
    private final float[] mCurrentRotation = new float[16];
    public volatile float mDeltaX;
    public volatile float mDeltaY;
    public volatile float scale = 1;
    public Context current_context;
    private InputStream plyInput;


    public Renderer(Context context) {
      current_context = context;
      // Just in case we don't use the PLY in the future,
      // we need to give the user the option of switching out.
      plyInput = context.getResources().openRawResource(R.raw.controller_ascii);
    }

    public void onSurfaceCreated(GL10 gl,EGLConfig config) {
      GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.4f);
      // Initialize the accumulated rotation matrix
      Matrix.setIdentityM(mAccumulatedRotation, 0);
      // TODO(bminortx): put in loader for mesh
      cube = new Cube();
      try {
        mesh = new Mesh(plyInput);
      } catch (IOException e) {
        e.printStackTrace();
      }
      try {
        mesh.createProgram();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    public void onDrawFrame(GL10 gl) {
      float[] mTemporaryMatrix = new float[16];
      GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
      // Model, View, and Projection
      Matrix.setIdentityM(mModelMatrix, 0);
      Matrix.scaleM(mModelMatrix, 0, scale, scale, scale);
      Matrix.setLookAtM(mViewMatrix, 0, 0, 0, -3, 0f, 0f, 0f, 0f, 1.0f, 0.0f);
      // Set a matrix that contains the current rotation.
      // Code below adapted from http://www.learnopengles.com/rotating-an-object-with-touch-events/
      Matrix.setIdentityM(mCurrentRotation, 0);
      Matrix.rotateM(mCurrentRotation, 0, mDeltaX, 0.0f, 1.0f, 0.0f);
      Matrix.rotateM(mCurrentRotation, 0, mDeltaY, -1.0f, 0.0f, 0.0f);
      mDeltaX = 0.0f;
      mDeltaY = 0.0f;
      // Multiply the current rotation by the accumulated rotation,
      // and then set the accumulated rotation to the result.
      Matrix.multiplyMM(mTemporaryMatrix, 0,
              mCurrentRotation, 0,
              mAccumulatedRotation, 0);
      System.arraycopy(mTemporaryMatrix, 0,
              mAccumulatedRotation, 0, 16);
      // Rotate the cube taking the overall rotation into account.
      Matrix.multiplyMM(mTemporaryMatrix, 0,
              mModelMatrix, 0,
              mAccumulatedRotation, 0);
      System.arraycopy(mTemporaryMatrix, 0, mModelMatrix, 0, 16);
      // Calculate the projection and view transformation
      Matrix.multiplyMM(mTemporaryMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);
      Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mTemporaryMatrix, 0);
      // Draw shape
      mesh.draw(mMVPMatrix);
    }

    public void onSurfaceChanged(GL10 gl,int width,int height) {
      GLES20.glViewport(0, 0, width, height);
      float ratio = (float) width / height;
      // this projection matrix is applied to object coordinates
      // in the onDrawFrame() method
      Matrix.frustumM(mProjectionMatrix, 0, -ratio, ratio, -1, 1, 1, 100);
    }
  }
}
